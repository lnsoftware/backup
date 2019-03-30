/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.PinyinTool;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.mq.UserAndCorpRocketMqUtil;
import com.royasoft.vwt.cag.util.upload.FileUploadUtil;
import com.royasoft.vwt.cag.vo.CorpMemberLevelVO;
import com.royasoft.vwt.cag.vo.CorpPrefectInfoVO;
import com.royasoft.vwt.cag.vo.UserLogVO;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.dictionary.api.vo.DictionaryVo;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendProvinceSmsInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.interfaces.HlwCorpAuthInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.interfaces.SmsSwitchInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.vo.HlwCorpAuthVO;
import com.royasoft.vwt.soa.business.hlwAuth.api.vo.SmsSwitchVO;
import com.royasoft.vwt.soa.business.imAttention.api.interfaces.ImAttentionInterface;
import com.royasoft.vwt.soa.business.imAttention.api.vo.Response;
import com.royasoft.vwt.soa.systemsettings.platform.api.interfaces.AccountManagerInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.vo.AccountManegerVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.clique.api.interfaces.CliqueInfoInterface;
import com.royasoft.vwt.soa.uic.contact.api.interfaces.ContactInterface;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.HLWMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 互联网企业认证业务处理
 *
 * @Author:MB
 * @Since:2016年5月18日
 */
@Scope("prototype")
@Service
public class HlwAuthService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HlwAuthService.class);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private DatabaseInterface databaseInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Autowired
    private CorpInterface corpInterface;

    @Autowired
    private DepartMentInterface departMentInterface;
    @Autowired
    private MemberInfoUtil memberInfoUtil;

    @Autowired
    private CliqueInfoInterface cliqueInfoInterface;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private DictionaryInterface dictionaryInterface;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private HLWMemberInfoInterface hlwMemberInfoInterface;

    @Autowired
    private HlwCorpAuthInterface hlwCorpAuthInterface;

    @Autowired
    private ContactInterface contactInterface;

    @Autowired
    private AccountManagerInterface accountManagerInterface;

    @Autowired
    private SmsSwitchInterface smsSwitchInterface;

    @Autowired
    private SendProvinceSmsInterface sendProvinceSmsInterface;

    @Autowired
    private ImAttentionInterface imAttentionInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.hlwAuth_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String tel_number = queue_packet.getTel_number();
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    logger.debug("互联网企业认证业务处理(入口),function_id:{},user_id:{},tel_number:{},request_body:{}", function_id, user_id, tel_number, request_body);
                    /***************************** 业务逻辑处理 *********************************************/

                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果
                    switch (function_id) {
                        case FunctionIdConstant.HLW_AUTH_GETPREFECTINFO:
                            resInfo = getPrefectInfo(request_body, user_id);
                            break;
                        case FunctionIdConstant.HLW_AUTH_PREFECTCORP:
                            resInfo = prefectCorpInfo(request_body, user_id);
                            break;
                        case FunctionIdConstant.HLW_AUTH_SUBMITPREFECT:
                            resInfo = submitAuthInfo(request_body, user_id);
                            break;
                        case FunctionIdConstant.HLW_AUTH_UPLOADOFFICIAL:
                            resInfo = uploadOfficial(request_body, user_id);
                            break;
                        case FunctionIdConstant.ADDRESS_DEPARTMENT_ADD:
                            resInfo = addAddressDepartMent(request_body, user_id);
                            break;
                        case FunctionIdConstant.ADDRESS_DEPARTMENT_UPDATE:
                            resInfo = updateAddressDepartMent(request_body, user_id);
                            break;
                        case FunctionIdConstant.ADDRESS_DEPARTMENT_DELETE:
                            resInfo = deleteAddressDepartMent(request_body, user_id);
                            break;
                        case FunctionIdConstant.HLW_MEMBER_ADD:
                            resInfo = addMemberByAdmin(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.HLW_MEMBER_UPDATE:
                            resInfo = updateMemberByAdmin(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.HLW_MEMBER_DELETE:
                            resInfo = deleteMemberByAdmin(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.IM_ATTEND:
                            resInfo = doImAttend(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.IM_CANCEL_ATTEND:
                            resInfo = doCancelImAttend(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.IM_GET_ATTENTION:
                            resInfo = getAttention(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.IM_ATTEND_BATCH:
                            resInfo = doImAttendBatch(user_id, tel_number, request_body);
                            break;
                        default:
                            break;
                    }
                    logger.debug("互联网企业认证业务处理(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("互联网企业认证业务处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }
        }
    }

    /**
     * 获取企业认证信息
     * 
     * @param requestBody 请求内容
     * @param userId
     * @return
     * @Description:
     */
    public String getPrefectInfo(String requestBody, String user_Id) {
        logger.debug("获取企业认证信息,requestBody:{},user_Id:{}", requestBody, user_Id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String userId = requestJson.getString("userId");
        String corpId = requestJson.getString("corpId");

        logger.debug("获取企业认证信息(解析body),userId:{},corpId:{}", userId, corpId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, corpId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2501, "");
        logger.debug("获取企业认证信息(clientUserVO),userId:{},corpId:{},clientUserVO:{}", userId, corpId, JSON.toJSONString(clientUserVO));

        CorpVO corpVO = corpInterface.findById(corpId);
        logger.debug("获取企业认证信息(corpVO),userId:{},corpId:{},corpVO:{}", userId, corpId, null == corpVO ? "" : JSON.toJSONString(clientUserVO));
        if (null == corpVO || corpVO.getFromchannel() != 7)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2502, "");

        String corpMobile = corpVO.getCorpMobilephone();
        logger.debug("获取企业认证信息(corpMobile),userId:{},corpId:{},corpMobile:{}", userId, corpId, corpMobile);
        if (null == corpMobile || "".equals(corpMobile) || !corpMobile.equals(clientUserVO.getTelNum()))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2503, "");

        List<HlwCorpAuthVO> hlwCorpAuthVOs = hlwCorpAuthInterface.findByCorpId(corpId);
        logger.debug("获取企业认证信息(hlwCorpAuthVO),userId:{},corpId:{},hlwCorpAuthVO:{}", userId, corpId, null == hlwCorpAuthVOs ? 0 : hlwCorpAuthVOs.size());
        HlwCorpAuthVO hlwCorpAuthVONotSubmit = null;
        if (null != hlwCorpAuthVOs && !hlwCorpAuthVOs.isEmpty()) {
            for (HlwCorpAuthVO hlwCorpAuthVO : hlwCorpAuthVOs) {
                if (!hlwCorpAuthVO.getDealFlag().equals("1") && !hlwCorpAuthVO.getDealFlag().equals("6")) {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2507, "");
                } else if (hlwCorpAuthVO.getDealFlag().equals("1")) {
                    hlwCorpAuthVONotSubmit = hlwCorpAuthVO;
                    break;
                }
            }
        }
        JSONObject resJson = getCorpAuthInfo(hlwCorpAuthVONotSubmit);
        logger.debug("获取企业认证信息(resJson),userId:{},corpId:{},resJson:{}", userId, corpId, resJson.toJSONString());

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), user_Id));
    }

    /**
     * 完善企业基本信息
     * 
     * @param requestBody 请求内容
     * @param userId
     * @return
     * @Description:
     */
    public String prefectCorpInfo(String requestBody, String user_Id) {
        logger.debug("完善企业基本信息,requestBody:{},user_Id:{}", requestBody, user_Id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String userId = requestJson.getString("userId");
        String corpId = requestJson.getString("corpId");
        String corpName = requestJson.getString("corpName");
        String contactPersonName = requestJson.getString("contactPersonName");
        String contactTel = requestJson.getString("contactTel");
        String regionName = requestJson.getString("regionName");
        String corpAddress = requestJson.getString("corpAddress");
        String corpMemberNumber = requestJson.getString("corpMemberNumber");

        logger.debug("完善企业基本信息(解析body),userId:{},corpId:{},corpName:{},contactPersonName:{},contactTel:{},regionName:{},corpAddress:{},corpMemberNumber:{}", userId, corpId, corpName,
                contactPersonName, contactTel, regionName, corpAddress, corpMemberNumber);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, corpId, corpName, contactPersonName, contactTel, regionName, corpAddress, corpMemberNumber))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        DictionaryVo dictionaryVo = dictionaryInterface.findDictionaryByDictIdAndKey(61L, corpMemberNumber);
        if (null == dictionaryVo)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2501, "");
        logger.debug("完善企业基本信息(clientUserVO),userId:{},corpId:{},clientUserVO:{}", userId, corpId, JSON.toJSONString(clientUserVO));

        CorpVO corpVO = corpInterface.findById(corpId);
        logger.debug("完善企业基本信息(corpVO),userId:{},corpId:{},corpVO:{}", userId, corpId, null == corpVO ? "" : JSON.toJSONString(clientUserVO));
        if (null == corpVO || corpVO.getFromchannel() != 7)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2502, "");

        String corpMobile = corpVO.getCorpMobilephone();
        logger.debug("完善企业基本信息(corpMobile),userId:{},corpId:{},corpMobile:{}", userId, corpId, corpMobile);
        if (null == corpMobile || "".equals(corpMobile) || !corpMobile.equals(clientUserVO.getTelNum()))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2503, "");

        List<HlwCorpAuthVO> hlwCorpAuthVOs = hlwCorpAuthInterface.findByCorpId(corpId);
        logger.debug("获取企业认证信息(hlwCorpAuthVO),userId:{},corpId:{},hlwCorpAuthVO:{}", userId, corpId, null == hlwCorpAuthVOs ? 0 : hlwCorpAuthVOs.size());
        HlwCorpAuthVO hlwCorpAuthVONotSubmit = null;
        if (null != hlwCorpAuthVOs) {
            for (HlwCorpAuthVO hlwCorpAuthVO : hlwCorpAuthVOs) {
                if (!hlwCorpAuthVO.getDealFlag().equals("1") && !hlwCorpAuthVO.getDealFlag().equals("6")) {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2507, "");
                } else if (hlwCorpAuthVO.getDealFlag().equals("1")) {
                    hlwCorpAuthVONotSubmit = hlwCorpAuthVO;
                    break;
                }
            }
        }

        List<CorpVO> corpVOs = corpInterface.findCorpByCorpName(corpName);
        logger.debug("完善企业基本信息(corpVOs),corpVOs:{}", null == corpVOs ? "" : JSON.toJSONString(corpVOs));
        if (null != corpVOs && !corpVOs.isEmpty()) {
            for (CorpVO corpVO2 : corpVOs) {
                logger.debug("完善企业基本信息(对比企业是否存在),corpVO2:{},corpId:{},boo:{}", corpVO2.getCorpId(), corpId, !corpVO2.getCorpId().equals(corpId));
                if (!corpVO2.getCorpId().equals(corpId))
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2505, "");
            }
        }

        try {
            String regionCode = dictionaryInterface.findDictionaryByIdKeyDesc("地市", regionName).getDictKey();
            if (null == hlwCorpAuthVONotSubmit) {
                HlwCorpAuthVO hlwCorpAuthVOSave = new HlwCorpAuthVO();
                hlwCorpAuthVOSave.setAuthId(UUID.randomUUID().toString());
                hlwCorpAuthVOSave.setCorpAddress(corpAddress);
                hlwCorpAuthVOSave.setCorpCity(regionCode);
                hlwCorpAuthVOSave.setCorpId(corpId);
                hlwCorpAuthVOSave.setCorpMemberNumber(corpMemberNumber);
                hlwCorpAuthVOSave.setCorpName(corpName);
                hlwCorpAuthVOSave.setDealFlag("1");
                hlwCorpAuthVOSave.setDeleteFlag("0");
                hlwCorpAuthVOSave.setLinkName(contactPersonName);
                hlwCorpAuthVOSave.setLinkTel(contactTel);
                hlwCorpAuthInterface.save(hlwCorpAuthVOSave);
            } else {
                hlwCorpAuthVONotSubmit.setCorpAddress(corpAddress);
                hlwCorpAuthVONotSubmit.setCorpCity(regionCode);
                hlwCorpAuthVONotSubmit.setCorpMemberNumber(corpMemberNumber);
                hlwCorpAuthVONotSubmit.setCorpName(corpName);
                hlwCorpAuthVONotSubmit.setLinkName(contactPersonName);
                hlwCorpAuthVONotSubmit.setLinkTel(contactTel);
                hlwCorpAuthInterface.save(hlwCorpAuthVONotSubmit);
            }
        } catch (Exception e) {
            logger.error("完善企业基本信息异常,requestBody:{},user_Id:{}", requestBody, user_Id, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2504, "");
        }

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 上传企业公函
     * 
     * @param requestBody 请求内容
     * @param userId
     * @return
     * @Description:
     */
    public String uploadOfficial(String requestBody, String user_Id) {
        logger.debug("上传企业公函,requestBody:{},user_Id:{}", requestBody, user_Id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String userId = requestJson.getString("userId");
        String corpId = requestJson.getString("corpId");

        logger.debug("上传企业公函(解析body),userId:{},corpId:{}", userId, corpId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, corpId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2501, "");
        logger.debug("上传企业公函(clientUserVO),userId:{},corpId:{},clientUserVO:{}", userId, corpId, JSON.toJSONString(clientUserVO));

        CorpVO corpVO = corpInterface.findById(corpId);
        logger.debug("上传企业公函(corpVO),userId:{},corpId:{},corpVO:{}", userId, corpId, null == corpVO ? "" : JSON.toJSONString(clientUserVO));
        if (null == corpVO || corpVO.getFromchannel() != 7)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2502, "");

        String corpMobile = corpVO.getCorpMobilephone();
        logger.debug("上传企业公函(corpMobile),userId:{},corpId:{},corpMobile:{}", userId, corpId, corpMobile);
        if (null == corpMobile || "".equals(corpMobile) || !corpMobile.equals(clientUserVO.getTelNum()))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2503, "");

        List<HlwCorpAuthVO> hlwCorpAuthVOs = hlwCorpAuthInterface.findByCorpId(corpId);
        logger.debug("上传企业公函(hlwCorpAuthVO),userId:{},corpId:{},hlwCorpAuthVO:{}", userId, corpId, null == hlwCorpAuthVOs ? 0 : hlwCorpAuthVOs.size());
        HlwCorpAuthVO hlwCorpAuthVONotSubmit = null;
        if (null != hlwCorpAuthVOs) {
            for (HlwCorpAuthVO hlwCorpAuthVO : hlwCorpAuthVOs) {
                if (!hlwCorpAuthVO.getDealFlag().equals("1") && !hlwCorpAuthVO.getDealFlag().equals("6")) {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2507, "");
                } else if (hlwCorpAuthVO.getDealFlag().equals("1")) {
                    hlwCorpAuthVONotSubmit = hlwCorpAuthVO;
                    break;
                }
            }
        }
        String filePath = "";
        try {
            filePath = FileUploadUtil.uploadFile(msg);
            if (null == filePath || "".equals(filePath))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2506, "");
            if (null == hlwCorpAuthVONotSubmit) {
                HlwCorpAuthVO hlwCorpAuthVOSave = new HlwCorpAuthVO();
                hlwCorpAuthVOSave.setAuthId(UUID.randomUUID().toString());
                hlwCorpAuthVOSave.setCorpId(corpId);
                hlwCorpAuthVOSave.setDealFlag("1");
                hlwCorpAuthVOSave.setDeleteFlag("0");
                hlwCorpAuthVOSave.setOfficial(filePath);
                hlwCorpAuthInterface.save(hlwCorpAuthVOSave);
            } else {
                hlwCorpAuthVONotSubmit.setOfficial(filePath);
                hlwCorpAuthInterface.save(hlwCorpAuthVONotSubmit);
            }
        } catch (Exception e) {
            logger.error("上传企业公函异常,requestBody:{},user_Id:{}", requestBody, user_Id, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2506, "");
        }

        JSONObject resJson = new JSONObject();
        resJson.put("officialPath", filePath);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), user_Id));
    }

    /**
     * 提交企业认证信息
     * 
     * @param requestBody 请求内容
     * @param userId
     * @return
     * @Description:
     */
    public String submitAuthInfo(String requestBody, String user_Id) {
        logger.debug("提交企业认证信息,requestBody:{},user_Id:{}", requestBody, user_Id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String userId = requestJson.getString("userId");
        String corpId = requestJson.getString("corpId");

        logger.debug("提交企业认证信息(解析body),userId:{},corpId:{}", userId, corpId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, corpId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2501, "");
        logger.debug("提交企业认证信息(clientUserVO),userId:{},corpId:{},clientUserVO:{}", userId, corpId, JSON.toJSONString(clientUserVO));

        CorpVO corpVO = corpInterface.findById(corpId);
        logger.debug("提交企业认证信息(corpVO),userId:{},corpId:{},corpVO:{}", userId, corpId, null == corpVO ? "" : JSON.toJSONString(clientUserVO));
        if (null == corpVO || corpVO.getFromchannel() != 7)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2502, "");

        String corpMobile = corpVO.getCorpMobilephone();
        logger.debug("提交企业认证信息(corpMobile),userId:{},corpId:{},corpMobile:{}", userId, corpId, corpMobile);
        if (null == corpMobile || "".equals(corpMobile) || !corpMobile.equals(clientUserVO.getTelNum()))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2503, "");

        List<HlwCorpAuthVO> hlwCorpAuthVOs = hlwCorpAuthInterface.findByCorpId(corpId);
        logger.debug("提交企业认证信息(hlwCorpAuthVO),userId:{},corpId:{},hlwCorpAuthVO:{}", userId, corpId, null == hlwCorpAuthVOs ? 0 : hlwCorpAuthVOs.size());
        HlwCorpAuthVO hlwCorpAuthVONotSubmit = null;
        if (null != hlwCorpAuthVOs) {
            for (HlwCorpAuthVO hlwCorpAuthVO : hlwCorpAuthVOs) {
                if (!hlwCorpAuthVO.getDealFlag().equals("1") && !hlwCorpAuthVO.getDealFlag().equals("6")) {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2507, "");
                } else if (hlwCorpAuthVO.getDealFlag().equals("1")) {
                    hlwCorpAuthVONotSubmit = hlwCorpAuthVO;
                    break;
                }
            }
        }

        if (null == hlwCorpAuthVONotSubmit || null == hlwCorpAuthVONotSubmit.getCorpName() || "".equals(hlwCorpAuthVONotSubmit.getCorpName())) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2509, "");
        } else if (null == hlwCorpAuthVONotSubmit.getOfficial() || "".equals(hlwCorpAuthVONotSubmit.getOfficial())) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2510, "");
        } else {
            try {
                hlwCorpAuthVONotSubmit.setDealFlag("2");
                hlwCorpAuthVONotSubmit.setRegistDate(new Date());
                hlwCorpAuthInterface.save(hlwCorpAuthVONotSubmit);
            } catch (Exception e) {
                logger.error("提交企业认证信息异常,requestBody:{},user_Id:{}", requestBody, user_Id, e);
                return ResponsePackUtil.buildPack(ResponseInfoConstant.Fail2511, "");
            }
        }
        sendRegionSms(hlwCorpAuthVONotSubmit.getCorpCity());
        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 获取企业认证填写信息
     * 
     * @param hlwCorpAuthVO
     * @return
     * @Description:
     */
    private JSONObject getCorpAuthInfo(HlwCorpAuthVO hlwCorpAuthVO) {
        JSONObject jsonObject = new JSONObject();
        List<String> regionNames = getAllRegionName();
        List<CorpMemberLevelVO> corpMemberLevelVOs = getMemberLevel();
        jsonObject.put("regionInfo", regionNames);
        jsonObject.put("memberLevel", corpMemberLevelVOs);

        String corpPrefectStatus = "0";
        String corpPrefectInfo = "";
        String corpOfficeStatus = "0";
        String corpOfficeInfo = "";
        if (null != hlwCorpAuthVO) {
            String corpName = hlwCorpAuthVO.getCorpName();
            String official = hlwCorpAuthVO.getOfficial();

            if (null != corpName && !"".equals(corpName)) {
                corpPrefectStatus = "1";
                corpPrefectInfo = JSON.toJSONString(getPrefectInfo(hlwCorpAuthVO));
            }

            if (null != official && !"".equals(official)) {
                corpOfficeStatus = "1";
                corpOfficeInfo = hlwCorpAuthVO.getOfficial();
            }
        }

        jsonObject.put("corpPrefectStatus", corpPrefectStatus);
        jsonObject.put("corpPrefectInfo", corpPrefectInfo);
        jsonObject.put("corpOfficeStatus", corpOfficeStatus);
        jsonObject.put("corpOfficeInfo", corpOfficeInfo);

        return jsonObject;
    }

    /**
     * 获取企业认证填写的基本信息
     * 
     * @param hlwCorpAuthVO
     * @return
     * @Description:
     */
    private CorpPrefectInfoVO getPrefectInfo(HlwCorpAuthVO hlwCorpAuthVO) {
        DictionaryVo dictionaryVo = dictionaryInterface.findDictionaryByDictIdAndKey(51L, hlwCorpAuthVO.getCorpCity());
        CorpPrefectInfoVO corpPrefectInfoVO = new CorpPrefectInfoVO();
        corpPrefectInfoVO.setContactPersonName(hlwCorpAuthVO.getLinkName());
        corpPrefectInfoVO.setContactTel(hlwCorpAuthVO.getLinkTel());
        corpPrefectInfoVO.setCorpAddress(hlwCorpAuthVO.getCorpAddress());
        corpPrefectInfoVO.setCorpId(hlwCorpAuthVO.getCorpId());
        corpPrefectInfoVO.setCorpMemberNumber(hlwCorpAuthVO.getCorpMemberNumber());
        corpPrefectInfoVO.setCorpName(hlwCorpAuthVO.getCorpName());
        corpPrefectInfoVO.setRegionName(null == dictionaryVo ? "" : dictionaryVo.getDictKeyDesc());
        return corpPrefectInfoVO;

    }

    /**
     * 获取字典表地市信息
     * 
     * @return
     * @Description:
     */
    private List<String> getAllRegionName() {
        List<DictionaryVo> dictionaryVos = dictionaryInterface.findDictionaryByDictIdAndDictDesc(51L, "地市");
        if (null == dictionaryVos || dictionaryVos.isEmpty())
            return null;
        List<String> regionInfo = new ArrayList<String>();
        for (DictionaryVo dictionaryVo : dictionaryVos) {
            if (!"省直".equals(dictionaryVo.getDictKeyDesc()) && !"省测试".equals(dictionaryVo.getDictKeyDesc()))
                regionInfo.add(dictionaryVo.getDictKeyDesc());
        }
        /** 注意：是根据的汉字的拼音的字母排序的，而不是根据汉字一般的排序方法 */
        Collections.sort(regionInfo, Collator.getInstance(java.util.Locale.CHINA));
        return regionInfo;
    }

    /**
     * 获取字典表地市信息
     * 
     * @return
     * @Description:
     */
    private List<CorpMemberLevelVO> getMemberLevel() {
        List<DictionaryVo> dictionaryVos = dictionaryInterface.findDictionaryByDictIdAndDictDesc(61L, "互联网企业人数级别");
        if (null == dictionaryVos || dictionaryVos.isEmpty())
            return null;
        List<CorpMemberLevelVO> corpMemberLevelVOs = new ArrayList<CorpMemberLevelVO>();
        for (DictionaryVo dictionaryVo : dictionaryVos) {
            CorpMemberLevelVO corpMemberLevelVO = new CorpMemberLevelVO();
            corpMemberLevelVO.setDesc(dictionaryVo.getDictValue());
            corpMemberLevelVO.setId(dictionaryVo.getDictKey());
            corpMemberLevelVOs.add(corpMemberLevelVO);
        }
        return corpMemberLevelVOs;
    }

    /**
     * 添加通讯录部门
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String addAddressDepartMent(String requestBody, String userId) {
        logger.debug("添加通讯录部门：requestBody:{},userId{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpId = requestJson.getString("corpId");
        String partName = requestJson.getString("partName");
        String parentPartId = requestJson.getString("parentPartId");

        // 验证参数值
        if (null == corpId || "".equals(corpId) || null == partName && "".equals(partName) || null == parentPartId || "".equals(parentPartId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, "");
        }
        // 管理员用户不存在
        ClientUserVO adminVO = clientUserInterface.findById(userId);
        logger.debug("验证 管理员用户是否存在userId:{},result={}", userId, null != adminVO ? true : false);
        if (null == adminVO) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2601, "");
        }
        // 验证企业是否存在
        CorpVO corpVO = corpInterface.findById(corpId);
        logger.debug("验证企业是否存在corpId:{},result={}", corpId, null != corpVO ? JSON.toJSONString(corpVO) : "");
        if (null == corpVO) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2602, "");
        }
        // 验证该用户是否该企业管理员
        if (!corpVO.getCorpMobilephone().equals(adminVO.getTelNum())) {
            logger.debug("该用户不是否企业管理员userId:{},corpId:{},getCorpMobilephone:{},getTelNum:{}", userId, corpId, corpVO.getCorpMobilephone(), adminVO.getTelNum());
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2603, "");
        }
        // 验证上级部门是否存在
        DepartMentVO departMentVo = departMentInterface.findById(parentPartId);
        logger.debug("验证上级部门是否存在parentPartId:{},result={}", parentPartId, null != departMentVo ? JSON.toJSONString(departMentVo) : "");
        if (null == departMentVo) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2604, "");
        }
        // 验证添加的部门是否已存在
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("EQ_partName", partName);
        params.put("EQ_parentDeptNum", parentPartId);
        params.put("EQ_corpId", corpId);
        List<DepartMentVO> departVo_list = departMentInterface.findByCondition(params, null);
        logger.debug("验证添加的部门是否已存在params:{},result={}", params, null != departMentVo ? JSON.toJSONString(departMentVo) : "");
        if (null != departVo_list && departVo_list.size() > 0) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2605, "");
        }
        // 保存添加部门
        DepartMentVO departMentVO = new DepartMentVO();
        departMentVO.setActTime(new Date());
        departMentVO.setCorpId(corpId);
        departMentVO.setCorpStatus("1");
        departMentVO.setFromChannel("7");
        departMentVO.setParentDeptNum(parentPartId);
        departMentVO.setPartFullName(departMentVo.getPartFullName() + "/" + partName);
        departMentVO.setPartName(partName);
        departMentVO.setSort(1L);
        DepartMentVO saveVo = departMentInterface.save(departMentVO);
        if (null == saveVo) {
            logger.debug("添加的部门错误..departMentVO:{}", departMentVO);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2606, "");
        }
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 修改通讯录部门
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String updateAddressDepartMent(String requestBody, String userId) {
        logger.debug("修改通讯录部门：requestBody:{},userId{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpId = requestJson.getString("corpId");// 体验企业id
        String partName = requestJson.getString("partName");// 新的部门名称
        String partId = requestJson.getString("partId");// 被修改的部门Id

        // 验证参数值
        if (null == corpId || "".equals(corpId) || null == partName && "".equals(partName) || null == partId || "".equals(partId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, "");
        }
        // 管理员用户不存在
        ClientUserVO adminVO = clientUserInterface.findById(userId);
        logger.debug("验证 管理员用户是否存在userId:{},result={}", userId, null != adminVO ? true : false);
        if (null == adminVO) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2601, "");
        }
        // 验证企业是否存在
        CorpVO corpVO = corpInterface.findById(corpId);
        logger.debug("验证企业是否存在corpId:{},result={}", corpId, null != corpVO ? JSON.toJSONString(corpVO) : "");
        if (null == corpVO) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2602, "");
        }
        // 验证该用户是否该企业管理员
        if (!corpVO.getCorpMobilephone().equals(adminVO.getTelNum())) {
            logger.debug("该用户不是否企业管理员userId:{},corpId:{},getCorpMobilephone:{},getTelNum:{}", userId, corpId, corpVO.getCorpMobilephone(), adminVO.getTelNum());
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2603, "");
        }
        // 验证部门是否存在
        DepartMentVO departMentVo = departMentInterface.findById(partId);
        logger.debug("验证部门是否存在partId:{},result={}", partId, null != departMentVo ? JSON.toJSONString(departMentVo) : "");
        if (null == departMentVo) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2607, "");
        }
        // 验证修改后部门名称，同级目录是否已存在
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("EQ_partName", partName);
        params.put("EQ_parentDeptNum", departMentVo.getParentDeptNum());
        params.put("EQ_corpId", corpId);
        List<DepartMentVO> departVo_list = departMentInterface.findByCondition(params, null);
        logger.debug("验证修改后部门名称，同级目录是否已存在params:{},result={}", params, null != departMentVo ? JSON.toJSONString(departMentVo) : "");
        if (null != departVo_list && departVo_list.size() > 0) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2608, "");
        }
        // 设置新的部门名称
        String partFullName = departMentVo.getPartFullName();
        departMentVo.setPartFullName(partFullName.substring(0, partFullName.lastIndexOf("/")) + "/" + partName);
        departMentVo.setPartName(partName);
        DepartMentVO saveVo = departMentInterface.save(departMentVo);
        if (null == saveVo) {
            logger.debug("修改部门错误..departMentVO:{}", departMentVo);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2609, "");
        }

        // 修改部门下所属人员部门名称
        boolean resultStatus = updateHlwMemberPartName(departMentVo);
        logger.debug("修改部门下所属人员部门名称 {}:", resultStatus);
        // 成功返回
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 删除通讯录部门
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String deleteAddressDepartMent(String requestBody, String userId) {
        logger.debug("删除通讯录部门：requestBody:{},userId{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpId = requestJson.getString("corpId");// 体验企业id
        String partId = requestJson.getString("partId");// 被删除部门id

        // 验证参数值
        if (null == corpId || "".equals(corpId) || null == partId || "".equals(partId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, "");
        }
        // 管理员用户不存在
        ClientUserVO adminVO = clientUserInterface.findById(userId);
        logger.debug("验证 管理员用户是否存在userId:{},result={}", userId, null != adminVO ? true : false);
        if (null == adminVO) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2601, "");
        }
        // 验证企业是否存在
        CorpVO corpVO = corpInterface.findById(corpId);
        logger.debug("验证企业是否存在corpId:{},result={}", corpId, null != corpVO ? JSON.toJSONString(corpVO) : "");
        if (null == corpVO) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2602, "");
        }
        // 验证该用户是否该企业管理员
        if (!corpVO.getCorpMobilephone().equals(adminVO.getTelNum())) {
            logger.debug("该用户不是否企业管理员userId:{},corpId:{},getCorpMobilephone:{},getTelNum:{}", userId, corpId, corpVO.getCorpMobilephone(), adminVO.getTelNum());
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2603, "");
        }
        // 验证部门是否存在
        DepartMentVO departMentVo = departMentInterface.findById(partId);
        logger.debug("验证部门是否存在partId:{},result={}", partId, null != departMentVo ? JSON.toJSONString(departMentVo) : "");
        if (null == departMentVo) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2607, "");
        }
        // 判断当前删除部门是否有子部门，
        int departCount = departMentInterface.findCountByParentIdAndCorpId(partId, corpId);
        logger.debug("当前删除部门下子部门数:partId:{},departCount:{}", partId, departCount);
        if (departCount > 0) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2610, "");
        }
        // 判断当前删除部门是否有人员
        int memberCount = memberInfoUtil.findMemberCountByDeptNum(partId);
        logger.debug("当前删除部门下人员数:partId:{},memberCount:{}", partId, memberCount);
        if (memberCount > 0) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2610, "");
        }
        // 删除部门
        if (!departMentInterface.deleteDepartMentById(partId, userId)) {
            logger.debug("删除部门失败partId:{},userId:{}", partId, userId);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2611, "");
        }
        // 成功返回
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 互联网管理员增加人员
     * 
     * @param user_id
     * @param tel_number
     * @param request_body
     * @return
     */
    public String addMemberByAdmin(String user_id, String tel_number, String request_body) {
        logger.debug("互联网管理员增加人员-user_id{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String userId = requestJson.getString("userId");// 必选 用户id
            String corpId = requestJson.getString("corpId");// 必选 体验企业id
            String partId = requestJson.getString("partId");// 必选 所属部门id
            String memberName = requestJson.getString("memberName");// 必选 人员姓名
            String memberTel = requestJson.getString("memberTel");// 必选 人员手机号码
            String memberDuty = requestJson.getString("memberDuty");// 可选 人员职务
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(userId, true, 32) || !StringUtils.checkParam(corpId, true, 32) || !StringUtils.checkParam(partId, true, 32) || !StringUtils.checkParam(memberName, true, 50)
                    || !StringUtils.checkParam(memberTel, true, 11) || !StringUtils.checkParam(memberDuty, false, 100))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            /** 校验该管理员是否存在 */
            ClientUserVO adminVO = clientUserInterface.findById(user_id);
            if (null == adminVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2601, "");

            /** 校验该企业是否存在 */
            CorpVO corpVO = corpInterface.findById(corpId);
            if (null == corpVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2602, "");

            /** 校验该管理员是否为企业管理员 */
            if (!adminVO.getTelNum().equals(corpVO.getCorpMobilephone()))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2603, "");

            /** 校验该部门是否存在 */
            DepartMentVO departMentVO = departMentInterface.findById(partId);
            if (null == departMentVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2607, "");

            /** 校验该部门是否包含该人员 */
            List<MemberInfoVO> list = memberInfoUtil.findMemberInfosByTelNum(memberTel, 7L);
            if (list != null && list.size() > 0) {
                for (MemberInfoVO memberInfoVO : list) {
                    if (partId.equals(memberInfoVO.getDeptId()))
                        return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2612, "");
                }
            }

            /** ————保存用户信息———— */
            List<ClientUserVO> clientList = clientUserInterface.findByTelNum(memberTel);
            MemberInfoVO memberInfoVORes = null;
            /** 如果该用户先前激活过 */
            if (null != clientList && !clientList.isEmpty()) {
                memberInfoVORes = doCreateMemberInfo(memberTel, memberName, memberDuty, departMentVO, corpVO.getCorpRegion(), corpVO.getCorpArea(), "0");
                /** 保存用户信息到clientuser表 */
                doCreateClientUser(memberTel, memberName, clientList.get(0).getPwd(), departMentVO, memberInfoVORes, clientList.get(0).getPrivateKey(), corpVO.getCorpRegion(), corpVO.getCorpArea());
            } else {
                memberInfoVORes = doCreateMemberInfo(memberTel, memberName, memberDuty, departMentVO, corpVO.getCorpRegion(), corpVO.getCorpArea(), "1");
            }
        } catch (Exception e) {
            logger.error("互联网管理员增加人员异常-user_id{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
        }
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 互联网管理员修改人员信息
     * 
     * @param user_id
     * @param tel_number
     * @param request_body
     * @return
     */
    public String updateMemberByAdmin(String user_id, String tel_number, String request_body) {
        logger.debug("互联网管理员增加人员-user_id{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String userId = requestJson.getString("userId");// 必选 用户id
            String corpId = requestJson.getString("corpId");// 必选 体验企业id
            String editMemberId = requestJson.getString("editMemberId");// 必选 被修改者用户id
            String editPartId = requestJson.getString("editPartId");// 必选 修改后所属部门id
            String editMemberName = requestJson.getString("editMemberName");// 必选 修改后人员姓名
            String editMemberDuty = requestJson.getString("editMemberDuty");// 可选 修改后人员职务
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(userId, true, 32) || !StringUtils.checkParam(corpId, true, 32) || !StringUtils.checkParam(editMemberId, true, 32)
                    || !StringUtils.checkParam(editPartId, true, 32) || !StringUtils.checkParam(editMemberName, true, 50) || !StringUtils.checkParam(editMemberDuty, false, 100))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            /** 校验该管理员是否存在 */
            ClientUserVO adminVO = clientUserInterface.findById(user_id);
            if (null == adminVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2601, "");

            /** 校验该企业是否存在 */
            CorpVO corpVO = corpInterface.findById(corpId);
            if (null == corpVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2602, "");

            /** 校验该管理员是否为企业管理员 */
            if (!adminVO.getTelNum().equals(corpVO.getCorpMobilephone()))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2603, "");

            /** 校验该部门是否存在 */
            DepartMentVO departMentVO = departMentInterface.findById(editPartId);
            if (null == departMentVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2607, "");

            /** 校验被修改的用户是否存在 */
            MemberInfoVO memberInfoVO = memberInfoUtil.findMemberInfoById(editMemberId, 7L);
            if (null == memberInfoVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2614, "");

            /** 如果人员变更了部门 */
            if (!editPartId.equals(memberInfoVO.getDeptId())) {
                /** 校验该部门下是否已存在该成员 */
                List<MemberInfoVO> memberList = memberInfoUtil.findMemberInfosByTelNum(memberInfoVO.getTelNum(), 7L);
                if (memberList != null && memberList.size() > 0) {
                    for (MemberInfoVO memberInfoVO2 : memberList) {
                        if (editPartId.equals(memberInfoVO2.getDeptId()))
                            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2618, "");
                    }
                }
            }

            /** 修改用户信息 */
            if (!doUpdateUserInfo(editMemberId, editMemberName, editPartId, editMemberDuty, memberInfoVO, corpVO, departMentVO))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2615, "");
        } catch (Exception e) {
            logger.error("互联网管理员增加人员异常-user_id{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
        }

        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 互联网管理员删除人员信息
     * 
     * @param user_id
     * @param tel_number
     * @param request_body
     * @return
     */
    public String deleteMemberByAdmin(String user_id, String tel_number, String request_body) {
        logger.debug("互联网管理员增加人员-user_id{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String userId = requestJson.getString("userId");// 必选 用户id
            String corpId = requestJson.getString("corpId");// 必选 体验企业id
            String deleteMemberId = requestJson.getString("deleteMemberId");// 必选 被删除用户id
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(userId, true, 32) || !StringUtils.checkParam(corpId, true, 32) || !StringUtils.checkParam(deleteMemberId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            if (userId.equals(deleteMemberId))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2619, "");

            /** 校验该管理员是否存在 */
            ClientUserVO adminVO = clientUserInterface.findById(user_id);
            if (null == adminVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2601, "");

            /** 校验该企业是否存在 */
            CorpVO corpVO = corpInterface.findById(corpId);
            if (null == corpVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2602, "");

            /** 校验该管理员是否为企业管理员 */
            if (!adminVO.getTelNum().equals(corpVO.getCorpMobilephone()))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2603, "");

            /** 校验被删除用户是否存在 */
            MemberInfoVO memberInfoVO = hlwMemberInfoInterface.findHLWById(deleteMemberId);
            if (null == memberInfoVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2616, "");

            /** 删除用户 */
            if (!doDeleteUser(memberInfoVO, user_id, corpVO))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2617, "");
        } catch (Exception e) {
            logger.error("互联网管理员增加人员异常-user_id{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
        }
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 管理员添加人员(互联网)
     * 
     * @param telNum
     * @param memberName
     * @param password
     * @param duty
     * @param enterpriseVO
     * @return
     * @Description:
     */
    private MemberInfoVO doCreateMemberInfo(String telNum, String memberName, String duty, DepartMentVO enterpriseVO, String regionCode, String areaCode, String activeFlag) {
        logger.debug("doCreateMemberInfo(互联网),telNum:{},memberName:{},duty:{},,enterpriseVO:{},regionCode:{},areaCode:{}", telNum, memberName, duty, JSON.toJSONString(enterpriseVO), regionCode,
                areaCode);
        try {
            Long sortMax = hlwMemberInfoInterface.findMaxSortByCorpId(enterpriseVO.getCorpId());
            MemberInfoVO memberInfoVO = new MemberInfoVO();
            memberInfoVO.setUserCreateTime(new Date());
            memberInfoVO.setCorpId(enterpriseVO.getCorpId());
            memberInfoVO.setCreatTime(new Date());
            memberInfoVO.setDeptId(enterpriseVO.getDeptId());
            memberInfoVO.setFromChannel(7L);
            memberInfoVO.setMemberName(memberName);
            memberInfoVO.setMemStatus(activeFlag);
            memberInfoVO.setOperationTime(new Date());
            memberInfoVO.setPartName(enterpriseVO.getPartName());
            memberInfoVO.setTelNum(telNum);
            memberInfoVO.setSpell(PinyinTool.getFullSpell(memberName));
            memberInfoVO.setFirstSpell(PinyinTool.getFirstSpell(memberName));
            memberInfoVO.setClique("1");
            memberInfoVO.setVisitAuth(0L);
            memberInfoVO.setRoleAuth(0L);
            memberInfoVO.setSort(null == sortMax ? 1L : sortMax + 1);
            if (null != duty && !"".equals(duty))
                memberInfoVO.setDuty(duty);
            MemberInfoVO memberInfoVORes = hlwMemberInfoInterface.save(memberInfoVO);
            if (null == memberInfoVORes)
                return null;

            /** 导入成功保存信息，提供给经分数据 */
            UserLogVO userLogVo = new UserLogVO();
            userLogVo.setAreaCode(areaCode);
            userLogVo.setCityCode(regionCode);
            userLogVo.setCorpId(enterpriseVO.getCorpId());
            userLogVo.setDealFlag(0);
            userLogVo.setDealTime(sdf.format(new Date()));
            userLogVo.setIMEI("");
            userLogVo.setMobile(telNum);
            userLogVo.setSeq(System.nanoTime());
            userLogVo.setUserId(memberInfoVORes.getMemId());
            userLogVo.setUuid(UUID.randomUUID().toString());
            UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.userRecordQueue, JSON.toJSONString(userLogVo));

            return memberInfoVORes;
        } catch (Exception e) {
            logger.error("doCreateMemberInfo(互联网)异常,telNum:{},memberName:{},duty:{},,enterpriseVO:{},regionCode:{},areaCode:{}", telNum, memberName, duty, JSON.toJSONString(enterpriseVO), regionCode,
                    areaCode);
            return null;
        }
    }

    /**
     * 激活人员(互联网管理员添加人员)
     * 
     * @param telNum
     * @param memberName
     * @param password
     * @param enterpriseVO
     * @param memberInfoVORes
     * @return
     * @Description:
     */
    private boolean doCreateClientUser(String telNum, String memberName, String password, DepartMentVO enterpriseVO, MemberInfoVO memberInfoVORes, String privateKey, String regionCode, String areaCode) {
        logger.debug("doCreateClientUser(互联网管理员添加人员),telNum:{},memberName:{},password:{},enterpriseVO:{},memberInfoVORes:{},privateKey:{},regionCode:{},areaCode:{}", telNum, memberName, password,
                JSON.toJSONString(enterpriseVO), JSON.toJSONString(memberInfoVORes), privateKey, regionCode, areaCode);
        try {
            ClientUserVO clientUserVO = new ClientUserVO();
            clientUserVO.setCorpId(enterpriseVO.getCorpId());
            clientUserVO.setCreateTime(new Date());
            clientUserVO.setDeptId(enterpriseVO.getDeptId());
            clientUserVO.setFromChannel(7L);
            clientUserVO.setPwd(password);
            clientUserVO.setTelNum(telNum);
            clientUserVO.setUserId(memberInfoVORes.getMemId());
            clientUserVO.setUserName(memberName);
            clientUserVO.setUserState("Y");
            clientUserVO.setClique("1");
            if (null != privateKey && !"".equals(privateKey))
                clientUserVO.setPrivateKey(privateKey);
            ClientUserVO clientUserVORes = clientUserInterface.saveUser(clientUserVO);
            if (null == clientUserVORes)
                return false;

            memberInfoVORes.setMemStatus("1");
            memberInfoUtil.saveMemberInfo(memberInfoVORes, memberInfoVORes.getFromChannel());

            /** 激活成功后保存信息到mq，提供给经分数据 */
            UserLogVO userLogVo = new UserLogVO();
            userLogVo.setAreaCode(areaCode);
            userLogVo.setCityCode(regionCode);
            userLogVo.setCorpId(enterpriseVO.getCorpId());
            userLogVo.setDealFlag(1);
            userLogVo.setDealTime(sdf.format(new Date()));
            userLogVo.setIMEI("");
            userLogVo.setMobile(telNum);
            userLogVo.setSeq(System.nanoTime());
            userLogVo.setUserId(clientUserVORes.getUserId());
            userLogVo.setUuid(UUID.randomUUID().toString());
            UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.userRecordQueue, JSON.toJSONString(userLogVo));
        } catch (Exception e) {
            logger.error("doCreateClientUser(互联网管理员添加人员)异常,telNum:{},memberName:{},password:{},enterpriseVO:{},memberInfoVORes:{},privateKey:{},regionCode:{},areaCode:{}", telNum, memberName,
                    password, JSON.toJSONString(enterpriseVO), JSON.toJSONString(memberInfoVORes), privateKey, regionCode, areaCode);
            return false;
        }
        return true;
    }

    /**
     * 修改用户信息（member_info,client_user）
     * 
     * @param editMemberId
     * @param editMemberName
     * @param editPartId
     * @param editMemberDuty
     * @param memberInfoVO
     * @param corpVO
     * @return
     */
    public boolean doUpdateUserInfo(String editMemberId, String editMemberName, String editPartId, String editMemberDuty, MemberInfoVO memberInfoVO, CorpVO corpVO, DepartMentVO departMentVO) {
        logger.debug("doUpdateUserInfo(互联网管理员添加人员),editMemberId:{},editMemberName:{},editPartId:{},editMemberDuty:{},memberInfoVO:{},corpVO:{}", editMemberId, editMemberName, editPartId,
                editMemberDuty, JSON.toJSONString(memberInfoVO), JSON.toJSONString(corpVO));
        /** 修改memberinfo表 */
        try {
            memberInfoVO.setOperationTime(new Date());
            memberInfoVO.setMemberName(editMemberName);
            memberInfoVO.setDeptId(editPartId);
            memberInfoVO.setPartName(departMentVO.getPartName());
            if (!org.springframework.util.StringUtils.isEmpty(editMemberDuty))
                memberInfoVO.setDuty(editMemberDuty);
            if (null == memberInfoUtil.saveMemberInfo(memberInfoVO, 7L))
                return false;

            /** 修改client_user表 */
            ClientUserVO clientUserVO = clientUserInterface.findById(editMemberId);
            int dealFlag = 0;
            if (null != clientUserVO) {
                dealFlag = 1;
                clientUserVO.setUserName(editMemberName);
                clientUserVO.setDeptId(editPartId);
                if (null == clientUserInterface.saveUser(clientUserVO))
                    return false;
            }

            /** 修改成功保存信息，提供给经分数据 */
            UserLogVO userLogVo = new UserLogVO();
            userLogVo.setAreaCode(corpVO.getCorpArea());
            userLogVo.setCityCode(corpVO.getCorpRegion());
            userLogVo.setCorpId(memberInfoVO.getCorpId());
            userLogVo.setDealFlag(dealFlag);
            userLogVo.setDealTime(sdf.format(new Date()));
            userLogVo.setIMEI("");
            userLogVo.setMobile(memberInfoVO.getTelNum());
            userLogVo.setSeq(System.nanoTime());
            userLogVo.setUserId(memberInfoVO.getMemId());
            userLogVo.setUuid(UUID.randomUUID().toString());
            UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.userRecordQueue, JSON.toJSONString(userLogVo));
        } catch (Exception e) {
            logger.error("doUpdateUserInfo(互联网管理员添加人员),editMemberId:{},editMemberName:{},editPartId:{},editMemberDuty:{},memberInfoVO:{},corpVO:{}", editMemberId, editMemberName, editPartId,
                    editMemberDuty, JSON.toJSONString(memberInfoVO), JSON.toJSONString(corpVO));
            return false;
        }
        return true;
    }

    /**
     * 删除人员信息
     * 
     * @param memberInfoVO
     * @param deleteMemberId
     * @return
     */
    public boolean doDeleteUser(MemberInfoVO memberInfoVO, String deleteMemberId, CorpVO corpVO) {
        if (!hlwMemberInfoInterface.deleteByMemberId(memberInfoVO.getMemId(), deleteMemberId))
            return false;
        contactInterface.deleteContactByFromUserIdOrToUserId(memberInfoVO.getMemId());
        /** 修改成功保存信息，提供给经分数据 */
        UserLogVO userLogVo = new UserLogVO();
        userLogVo.setAreaCode(corpVO.getCorpArea());
        userLogVo.setCityCode(corpVO.getCorpRegion());
        userLogVo.setCorpId(memberInfoVO.getCorpId());
        userLogVo.setDealFlag(2);
        userLogVo.setDealTime(sdf.format(new Date()));
        userLogVo.setIMEI("");
        userLogVo.setMobile(memberInfoVO.getTelNum());
        userLogVo.setSeq(System.nanoTime());
        userLogVo.setUserId(memberInfoVO.getMemId());
        userLogVo.setUuid(UUID.randomUUID().toString());
        UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.userRecordQueue, JSON.toJSONString(userLogVo));
        return true;
    }

    /**
     * 修改部门下所属人员部门名称
     * 
     * @param vo
     * @return
     */
    public boolean updateHlwMemberPartName(DepartMentVO departMent) {
        logger.debug("修改部门下所属人员部门名称,Vo对象:{}", JSON.toJSON(departMent));
        if (departMent == null)
            return false;
        try {
            String id = departMent.getDeptId();
            if (id != null && departMent != null) {// 修改操作
                // 修改互联网通讯录表中的 部门名称
                Map<String, Object> conditions = new HashMap<String, Object>();
                conditions.put("EQ_deptId", id);
                List<MemberInfoVO> memberList = hlwMemberInfoInterface.findHLWMemberInfoByCondition(conditions, null);
                for (MemberInfoVO info : memberList) {
                    info.setPartName(departMent.getPartName());
                    info.setOperationTime(new Date());
                    hlwMemberInfoInterface.save(info);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("修改部门下所属人员部门名称异常", e);
        }
        return false;
    }

    /**
     * 企业认证提交后发送短信至地市管理员
     * 
     * @param regionId
     * @Description:
     */
    private void sendRegionSms(String regionId) {
        List<SmsSwitchVO> smsSwitchVOs = smsSwitchInterface.findByTypeAndAttr("1", regionId);
        logger.debug("企业认证提交后发送短信至地市管理员,regionId:{},smsSwitchVOs:{}", regionId, null == smsSwitchVOs ? 0 : smsSwitchVOs.size());
        if (null != smsSwitchVOs && !smsSwitchVOs.isEmpty())
            return;

        List<AccountManegerVo> accountManegerVos = accountManagerInterface.findByRegionidAndRoleid(regionId, 5);
        logger.debug("企业认证提交后发送短信至地市管理员,regionId:{},accountManegerVos:{}", regionId, null == accountManegerVos ? 0 : accountManegerVos.size());
        if (null == accountManegerVos || accountManegerVos.isEmpty())
            return;
        List<String> sendTel = new ArrayList<String>();
        for (AccountManegerVo accountManegerVo : accountManegerVos) {
            String telNum = accountManegerVo.getTelnum();
            if (null == telNum || "".equals(telNum) || sendTel.contains(telNum))
                continue;
            String smsContent = "您收到一个V网通企业注册申请，请尽快登录管理平台处理！【V网通】";
            sendProvinceSmsInterface.sendCommonSms(telNum, smsContent);
            sendTel.add(telNum);
        }
    }

    /**
     * 群组关注人员
     * 
     * @param user_id
     * @param tel_number
     * @param request_body
     * @return
     * @Description:
     */
    public String doImAttend(String user_id, String tel_number, String request_body) {
        logger.debug("群组关注人员-入口,user_id{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String attendFrom = requestJson.getString("attendFrom");// 关注者
            String attendTo = requestJson.getString("attendTo");// 被关注者列表
            /** 校验参数完整性 */
            if (!StringUtils.checkParamNull(attendFrom, attendTo))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            Response response = imAttentionInterface.saveImAttention(attendFrom, attendTo);
            logger.debug("群组关注人员-结果,user_id{},telnum:{},requestbody:{},response:{}", user_id, tel_number, request_body, JSON.toJSONString(response));

            if (null == response)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
            Object resBody = response.getResponse_body();
            if (null != resBody && !"".equals(resBody))
                response.setResponse_body(ResponsePackUtil.encryptData(String.valueOf(resBody), user_id));
            /** 加密返回body */
            return JSON.toJSONString(response);
        } catch (Exception e) {
            logger.error("群组关注人员异常,user_id{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
        }
    }

    /**
     * 群组取消关注
     * 
     * @param user_id
     * @param tel_number
     * @param request_body
     * @return
     * @Description:
     */
    public String doCancelImAttend(String user_id, String tel_number, String request_body) {
        logger.debug("群组取消关注-入口,user_id{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String cancelAttendId = requestJson.getString("cancelAttendId");// 取消关注关系id列表
            /** 校验参数完整性 */
            if (!StringUtils.checkParamNull(cancelAttendId))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            Response response = imAttentionInterface.cancelImAttention(cancelAttendId);
            logger.debug("群组取消关注-结果,user_id{},telnum:{},requestbody:{},response:{}", user_id, tel_number, request_body, JSON.toJSONString(response));

            if (null == response)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
            Object resBody = response.getResponse_body();
            if (null != resBody && !"".equals(resBody))
                response.setResponse_body(ResponsePackUtil.encryptData(String.valueOf(resBody), user_id));
            /** 加密返回body */
            return JSON.toJSONString(response);
        } catch (Exception e) {
            logger.error("群组取消关注异常,user_id{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
        }
    }

    /**
     * 群组批量关注取消关注接口
     * 
     * @param user_id
     * @param tel_number
     * @param request_body
     * @return
     * @Description:
     */
    public String doImAttendBatch(String user_id, String tel_number, String request_body) {
        logger.debug("群组批量关注取消关注接口-入口,user_id{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String attendFrom = requestJson.getString("attendFrom");// 关注者
            String attendTo = requestJson.getString("attendTo");// 被关注者列表
            String cancelAttendId = requestJson.getString("cancelAttendId");// 取消关注关系id列表

            /** 校验参数完整性 */
            if (!StringUtils.checkParamNull(attendFrom) || (!StringUtils.checkParamNull(attendTo) && !StringUtils.checkParamNull(cancelAttendId)))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            Response response = imAttentionInterface.saveImAttentionBatch(attendFrom, attendTo, cancelAttendId);
            logger.debug("群组批量关注取消关注接口-结果,user_id{},telnum:{},requestbody:{},response:{}", user_id, tel_number, request_body, JSON.toJSONString(response));

            if (null == response)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
            Object resBody = response.getResponse_body();
            if (null != resBody && !"".equals(resBody))
                response.setResponse_body(ResponsePackUtil.encryptData(String.valueOf(resBody), user_id));
            /** 加密返回body */
            return JSON.toJSONString(response);
        } catch (Exception e) {
            logger.error("群组批量关注取消关注接口异常,user_id{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
        }
    }

    /**
     * 获取用户关注成员列表
     * 
     * @param user_id
     * @param tel_number
     * @param request_body
     * @return
     * @Description:
     */
    public String getAttention(String user_id, String tel_number, String request_body) {
        logger.debug("获取用户关注成员列表-入口,user_id{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String userId = requestJson.getString("userId");// 取消关注关系id列表
            /** 校验参数完整性 */
            if (!StringUtils.checkParamNull(userId))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            Response response = imAttentionInterface.getAllAttentionInfo(userId);

            logger.debug("获取用户关注成员列表-结果,user_id{},telnum:{},requestbody:{},response:{}", user_id, tel_number, request_body, JSON.toJSONString(response));
            if (null == response)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
            Object resBody = response.getResponse_body();
            if (null != resBody && !"".equals(resBody))
                response.setResponse_body(ResponsePackUtil.encryptData(String.valueOf(resBody), user_id));
            /** 加密返回body */
            return JSON.toJSONString(response);
        } catch (Exception e) {
            logger.error("获取用户关注成员列表异常,user_id{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
        }
    }
}
