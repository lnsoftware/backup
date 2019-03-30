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
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.IMSUtil;
import com.royasoft.vwt.cag.util.IntegralUtil;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.PinyinTool;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.mq.LogRocketMqUtil;
import com.royasoft.vwt.cag.util.mq.MsgPushUtil;
import com.royasoft.vwt.cag.util.mq.RocketMqUtil;
import com.royasoft.vwt.cag.util.mq.UserAndCorpRocketMqUtil;
import com.royasoft.vwt.cag.util.upload.FileUploadUtil;
import com.royasoft.vwt.cag.vo.CorpLogVO;
import com.royasoft.vwt.cag.vo.UserLogVO;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.dictionary.api.vo.DictionaryVo;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendSmsInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.VerifyCodeInterface;
import com.royasoft.vwt.soa.business.invite.api.interfaces.InviteShareInterface;
import com.royasoft.vwt.soa.business.invite.api.vo.InviteShareVo;
import com.royasoft.vwt.soa.business.meeting.api.interfaces.MeetingInterface;
import com.royasoft.vwt.soa.business.meeting.api.vo.ImsErrorVO;
import com.royasoft.vwt.soa.integral.api.interfaces.IntegralInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.SignInterface;
import com.royasoft.vwt.soa.sundry.memberactive.api.interfaces.MemberActiveInterface;
import com.royasoft.vwt.soa.systemsettings.gatedlaunch.api.interfaces.GatedlaunchInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.clique.api.interfaces.CliqueInfoInterface;
import com.royasoft.vwt.soa.uic.clique.api.vo.CliqueInfoVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.customer.api.interfaces.CustomerInterface;
import com.royasoft.vwt.soa.uic.customer.api.vo.CustomerVo;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.HLWMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 设置业务处理
 *
 * @Author:MB
 * @Since:2016年3月7日
 */
@Scope("prototype")
@Service
public class SettingService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SettingService.class);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private InviteShareInterface inviteShareInterface;

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
    private SignInterface signInterface;

    @Autowired
    private MsgPushUtil msgPushUtil;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private IntegralInterface integralInterface;

    @Autowired
    private DictionaryInterface dictionaryInterface;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private GatedlaunchInterface gatedlaunchInterface;

    @Autowired
    private SendSmsInterface sendSmsInterface;

    @Autowired
    private VerifyCodeInterface verifyCodeInterface;

    @Autowired
    private MemberActiveInterface memberActiveInterface;

    @Autowired
    private HLWMemberInfoInterface hlwMemberInfoInterface;

    @Autowired
    private MeetingInterface meetingInterface;

    @Autowired
    private CustomerInterface customerInterface;

    @Autowired
    private IntegralUtil integralUtil;

    @Autowired
    private IMSUtil IMSUtil;

    @Autowired
    private CommonService commonService;

    private static final String INTERNET_USERINFO = "INTERNET:USERINFO:NO:";

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.setting_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String tel_number = queue_packet.getTel_number();
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    logger.debug("设置业务处理(入口),function_id:{},user_id:{},tel_number:{},request_body:{}", function_id, user_id, tel_number, request_body);
                    /***************************** 业务逻辑处理 *********************************************/

                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果
                    switch (function_id) {
                        case FunctionIdConstant.SENDSMS:
                            resInfo = sendVerifyCodeSMS(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.TOREGIST:
                            resInfo = sendActiveSmsVerifyCode(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.TOFORGET:
                            resInfo = sendForgetSmsVerifyCode(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.DOFORGET:
                            resInfo = checkForgetAndSetting(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.DOREGIST:
                            resInfo = checkActiveAndSetting(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.DOINTERNETREGIST:
                            resInfo = checkInternetAndSetting(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.PREFECTINFO:
                            resInfo = prefectInternetInfo(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.INTERNETADD:
                            resInfo = inviterMemberInfo(request_body, user_id);
                            break;
                        case FunctionIdConstant.GRAYRELEASE:
                            resInfo = findGrayReleaseByCorpID(request_body, user_id);
                            break;
                        case FunctionIdConstant.SHAREURL:
                            resInfo = shareUrl(request_body, user_id, msg);
                            break;
                        default:
                            break;
                    }
                    logger.debug("设置业务处理(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("设置业务处理异常", e);
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
     * 分享链接
     * 
     * @param request_body
     * @param user_id
     * @param msg
     * @return
     * @throws Exception
     */
    public String shareUrl(String request_body, String user_id, Object msg) throws Exception {
        logger.debug("分享链接user_id{},request_body{}", user_id, request_body);
        JSONObject obj = JSONObject.parseObject(request_body);
        JSONObject json = FileUploadUtil.uploadFileForSendTask(msg);
        obj.put("pathUrl", json.get("pathUrl"));
        JSONObject sendObj = new JSONObject();
        sendObj.put("type", "6");
        sendObj.put("content", obj.toJSONString());
        RocketMqUtil.send(RocketMqUtil.PushQueue, sendObj.toJSONString());
        return ResponsePackUtil.buildPack("0000", String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 下发短信验证码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String sendVerifyCodeSMS(String requestBody) {
        logger.debug("下发短信验证码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        logger.debug("下发短信验证码(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 校验该用户是否存在 */
        // List<Map<String, Object>> clientUsers = checkUserExist(telNum);
        // List<MemberInfoVO> memberInfoVOs = checkUserExist(telNum);
        // logger.debug("下发短信验证码(校验用户是否存在),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        // if (memberInfoVOs == null || memberInfoVOs.isEmpty()) {
        // int boo = databaseInterface.checkRegistTelNum(telNum);
        // logger.debug("下发短信验证码(校验用户是否在审核),telNum:{},boo:{}", telNum, boo);
        // if (boo == 3) {
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1019, getTipInfo("手机号码正在审核"));
        // }
        // }
        if (!commonService.checkVerifyCodeIsExist(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2707, telNum);
        
        if(!commonService.saveSmsCount(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2712, telNum);
        
        /** 生成短信验证码 */
        String verifyCode = getRandomString();
        String smsContent = getSmsContent("您好，您正在使用移动社区平台，动态密码是：", verifyCode, "，有效期为10分钟。安全提醒：切勿向他人泄露，以防上当受骗。", "【江苏移动】");
        logger.debug("下发短信验证码(下发短信),telNum:{},smsContent:{}", telNum, smsContent);

        if (!saveVerifyCode(telNum, verifyCode, smsContent))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, telNum);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * 激活用户校验
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String sendActiveSmsVerifyCode(String requestBody) {
        logger.debug("激活用户校验,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        logger.debug("激活用户校验(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 校验该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = checkUserExist(telNum);
        logger.debug("激活用户校验(校验用户是否存在),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        if (memberInfoVOs == null || memberInfoVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1013, telNum);
        // int boo = databaseInterface.checkRegistTelNum(telNum);
        // logger.debug("激活用户校验(校验用户是否在审核),telNum:{},boo:{}", telNum, boo);
        // if (boo == 3) {
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1019, getTipInfo("手机号码正在审核，请勿重复申请注册!"));
        // } else {

        /** 2016年3月22日 若用户不存在，则互联网注册（拓展用户互联网注册） */
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1013, getRegistUrl(telNum));
        // }
        // }

        /** 校验该用户是否注册 */
        List<ClientUserVO> clientUserVOs = checkUserActive(telNum);
        logger.debug("激活用户校验(校验该用户是否注册),telNum:{},clientUserVOs:{}", telNum, JSON.toJSONString(clientUserVOs));
        if (null != clientUserVOs && !clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1014, getTipInfo("手机号码已被注册，请直接登录！"));

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * 忘记密码用户校验
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String sendForgetSmsVerifyCode(String requestBody) {
        logger.debug("忘记密码用户校验,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        logger.debug("忘记密码用户校验(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 校验该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = checkUserExist(telNum);
        logger.debug("忘记密码用户校验(校验用户是否存在),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        if (memberInfoVOs == null || memberInfoVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1013, telNum);
        // int boo = databaseInterface.checkRegistTelNum(telNum);
        // logger.debug("忘记密码用户校验(校验用户是否在审核),telNum:{},boo:{}", telNum, boo);
        // if (boo == 3) {
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1019, getTipInfo("手机号码正在审核"));
        // } else if (boo == 2 || boo == 1) {
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1013, getRegistUrl(telNum));
        // } else {

        /** 2016年3月22日 若用户不存在，则互联网注册（拓展用户互联网注册） */
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1013, getTipInfo("您现在还不是V网通开通用户，请联系本单位客户经理或者拨打10086咨询"));
        // }
        // }

        /** 校验该用户是否注册 */
        List<ClientUserVO> clientUserVOs = checkUserActive(telNum);
        logger.debug("忘记密码用户校验(校验该用户是否注册),telNum:{},clientUserVOs:{}", telNum, JSON.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1015, getTipInfo("用户账号尚未注册，请先注册账号再登录"));

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * 激活校验验证码并设置密码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String checkActiveAndSetting(String requestBody) {
        logger.debug("激活校验验证码并设置密码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        String verifyCode = requestJson.getString("verifyCode");
        String password = requestJson.getString("password");
        String imei = requestJson.getString("imei");

        logger.debug("激活校验验证码并设置密码(解析body),telNum:{},verifyCode:{},password:{},imei:{}", telNum, verifyCode, password, imei);

        /** 校验参数 */
        if (!valicateParams(telNum, verifyCode, password))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 校验该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = checkUserExist(telNum);
        logger.debug("激活校验验证码并设置密码(校验用户),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        if (null == memberInfoVOs || memberInfoVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1013, telNum);

        /** 校验该用户是否注册 */
        List<ClientUserVO> clientUserVOs = checkUserActive(telNum);
        logger.debug("激活校验验证码并设置密码(校验该用户是否注册),telNum:{},clientUserVOs:{}", telNum, JSON.toJSONString(clientUserVOs));
        if (null != clientUserVOs && !clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1014, telNum);

        /** 校验短信验证码 */
        String codeReal = commonService.valicateVerifyCode(telNum);
        if (null == codeReal || "".equals(codeReal))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1020, getTipInfo("该验证码已失效"));

        if (!codeReal.equals(verifyCode))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1016, getTipInfo("验证码错误"));

        /** 移除redis验证码信息 */
        commonService.removeVerifyInfo(telNum);

        /** 激活用户后保存至vwt_client_user表 */
        boolean saveRes = saveClientUser(memberInfoVOs, password, imei);

        if (!saveRes)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, telNum);

        /** 保存激活信息 */
        // if (!saveMemberActive(memberInfoVOs)) {
        // logger.warn("保存激活信息异常,telNum:{},verifyCode:{},password:{},editRes:{}", telNum, verifyCode, password, editRes);
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
        // }

        /** 用户注册成功，获得积分 ，10注册新用户 */
        integralUtil.integralSigns(telNum, "10");

        // /** 激活后修改邀请记录表 vwt_share */
        // String corpId = memberInfoVOs.get(0).getCorpId() == null ? "" : memberInfoVOs.get(0).getCorpId();
        // String region = "";
        // if (!org.springframework.util.StringUtils.isEmpty(corpId)) {
        // CorpVO corpVO = corpInterface.findById(corpId);
        // if (corpVO != null) {
        // region = corpVO.getCorpRegion() == null ? "" : corpVO.getCorpRegion();
        // }
        // }
        // inviteShareInterface.updateInviteWhenActive(telNum, sdf.format(new Date()), region);
        /** 处理激活营销信息 */
        dealActiveInfo(telNum);

        // openIms(memberInfoVOs.get(0).getMemId(), telNum);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * IMS开户
     * 
     * @param userid
     * @param telnum
     */
    public void openIms(String userid, String telnum) {
        try {
            String result = IMSUtil.registeOrcancelIMS(telnum, ParamConfig.ims_cmd_open);
            logger.debug("IMS开户结果:{},telnum:{},cmd:{}", result, telnum, ParamConfig.ims_cmd_open);
            if (!org.springframework.util.StringUtils.isEmpty(result)) {
                JSONObject json = JSONObject.parseObject(result);
                // 调用HTTP接口开户IMS失败
                if (!"200".equals(json.getString("status"))) {
                    ImsErrorVO iev = new ImsErrorVO();
                    iev.setId(UUID.randomUUID().toString());
                    iev.setTelnum(telnum);
                    iev.setTime(new Date());
                    iev.setUserid(userid);
                    meetingInterface.saveImsError(iev);
                }
            }
        } catch (Exception e) {
            logger.error("记录IMS开户失败信息 异常,telnum:{},e:{}", telnum, e);
        }
    }

    /**
     * 激活后修改邀请记录表
     * 
     * @param memberInfoVOs
     */
    public void updateShareInfo(String telNum) {
        try {
            List<InviteShareVo> list = inviteShareInterface.findInviteShareListByTelnum(telNum);
            if (list == null || list.isEmpty())
                return;
            InviteShareVo isv = list.get(0);
            isv.setActivetime(sdf.format(new Date()));
            isv.setType(1);
            inviteShareInterface.saveInviteRecord(isv);
        } catch (Exception e) {
            logger.error("激活后修改邀请记录表异常,telnum:{},e:{}", telNum, e);
        }
    }

    /**
     * 忘记密码校验验证码并设置密码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String checkForgetAndSetting(String requestBody) {
        logger.debug("忘记密码校验验证码并设置密码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        String verifyCode = requestJson.getString("verifyCode");
        String password = requestJson.getString("password");

        logger.debug("忘记密码校验验证码并设置密码(解析body),telNum:{},verifyCode:{},password:{}", telNum, verifyCode, password);

        /** 校验参数 */
        if (!valicateParams(telNum, verifyCode, password))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 校验该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = checkUserExist(telNum);
        logger.debug("忘记密码校验验证码并设置密码(校验用户是否存在),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        if (null == memberInfoVOs || memberInfoVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1013, telNum);

        /** 校验该用户是否注册 */
        List<ClientUserVO> clientUserVOs = checkUserActive(telNum);
        logger.debug("忘记密码校验验证码并设置密码(校验该用户是否注册),telNum:{},clientUserVOs:{}", telNum, JSON.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1015, telNum);

        /** 校验短信验证码并编辑密码 */
        /** 校验短信验证码 */
        String codeReal = commonService.valicateVerifyCode(telNum);
        if (null == codeReal || "".equals(codeReal))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1020, getTipInfo("该验证码已失效"));

        if (!codeReal.equals(verifyCode))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1016, getTipInfo("验证码错误"));

        /** 移除redis验证码信息 */
        commonService.removeVerifyInfo(telNum);

        /** 修改vwt_client_user表用户密码 */
        boolean saveRes = editClientUserPwd(clientUserVOs, password);

        if (!saveRes)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, telNum);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * 互联网个人注册验证码校验及设置密码 6006
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String checkInternetAndSetting(String requestBody) {
        logger.debug("互联网个人注册验证码校验及设置密码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        String verifyCode = requestJson.getString("verifyCode");
        String password = requestJson.getString("password");
        String imei = requestJson.getString("imei");

        logger.debug("互联网个人注册验证码校验及设置密码(解析body),telNum:{},verifyCode:{},password:{}", telNum, verifyCode, password);

        /** 校验参数 */
        if (!valicateParams(telNum, verifyCode, password))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 校验该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = checkUserExist(telNum);
        logger.debug("互联网个人注册验证码校验及设置密码(校验用户),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));

        /** 业务标识，若为1为互联网注册，0则为普通注册 */
        int activeFlag = 1;
        if (null != memberInfoVOs && !memberInfoVOs.isEmpty())
            activeFlag = 0;

        /** 校验该用户是否注册 */
        List<ClientUserVO> clientUserVOs = checkUserActive(telNum);
        logger.debug("互联网个人注册验证码校验及设置密码(校验该用户是否注册),telNum:{},clientUserVOs:{}", telNum, JSON.toJSONString(clientUserVOs));
        if (null != clientUserVOs && !clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1014, telNum);

        /** 校验短信验证码 */
        String codeReal = commonService.valicateVerifyCode(telNum);
        if (null == codeReal || "".equals(codeReal))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1020, getTipInfo("该验证码已失效"));

        if (!codeReal.equals(verifyCode))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1016, getTipInfo("验证码错误"));

        /** 移除redis验证码信息 */
        commonService.removeVerifyInfo(telNum);

        if (activeFlag == 1) {
            JSONObject redisJson = new JSONObject();
            redisJson.put("telNum", telNum);
            redisJson.put("password", password);
            redisInterface.setString(INTERNET_USERINFO + telNum, redisJson.toJSONString(), null == ParamConfig.internet_user_timeout ? 600 : Integer.valueOf(ParamConfig.internet_user_timeout));
        } else {
            /** 激活用户后保存至vwt_client_user表 */
            boolean saveRes = saveClientUser(memberInfoVOs, password, imei);

            if (!saveRes)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, telNum);

            /** 保存激活信息 */
            // if (!saveMemberActive(memberInfoVOs)) {
            // logger.warn("保存激活信息异常,telNum:{},verifyCode:{},password:{}", telNum, verifyCode, password);
            // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
            // }

            /** 用户注册成功，获得积分 ，10注册新用户 */
            integralUtil.integralSigns(telNum, "10");

            /** 处理激活营销信息 */
            dealActiveInfo(telNum);

            // /** 用户激活后维护邀请有礼数据 */
            // String corpId = memberInfoVOs.get(0).getCorpId();
            // String region = "";
            // if (!org.springframework.util.StringUtils.isEmpty(corpId)) {
            // CorpVO corpVO = corpInterface.findById(corpId);
            // if (corpVO != null) {
            // region = corpVO.getCorpRegion() == null ? "" : corpVO.getCorpRegion();
            // }
            // }
            // inviteShareInterface.updateInviteWhenActive(telNum, sdf.format(new Date()), region);

            /** IMS 开户 */
            // openIms(memberInfoVOs.get(0).getMemId(), telNum);
        }

        List<String> regionNames = getAllRegionName();
        JSONObject returnJson = new JSONObject();
        returnJson.put("operationFlag", activeFlag);
        returnJson.put("regionInfo", regionNames);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", returnJson.toJSONString());
    }

    /**
     * 互联网个人注册完善资料 6007 //member,corp
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String prefectInternetInfo(String requestBody) {
        logger.debug("互联网个人注册完善资料,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        String memberName = requestJson.getString("memberName");
        String corpName = requestJson.getString("corpName");
        String regionName = requestJson.getString("regionName");

        logger.debug("互联网个人注册完善资料(解析body),telNum:{},memberName:{},corpName:{},regionName:{}", telNum, memberName, corpName, regionName);

        /** 校验参数 */
        if (!valicateParams(telNum, memberName, corpName, regionName))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 判断注册信息是否在redis中 */
        String registAccountInfo = redisInterface.getString(INTERNET_USERINFO + telNum);
        logger.debug("互联网个人注册完善资料(判断注册信息是否在redis中),telNum:{},memberName:{},corpName:{},regionName:{},registAccountInfo:{}", telNum, memberName, corpName, regionName, registAccountInfo);
        if (null == registAccountInfo || "".equals(registAccountInfo))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1046, telNum);

        JSONObject registAccountJson = JSON.parseObject(registAccountInfo);

        List<CorpVO> corpVOs = corpInterface.findCorpByCorpName(corpName);
        if (null != corpVOs && !corpVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1047, telNum);

        /** 体验集团开户 */
        DepartMentVO corpCreateRes = doCreateCorp(telNum, corpName, memberName, regionName);
        if (null == corpCreateRes)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1048, telNum);

        DictionaryVo dictionaryVoRegion = dictionaryInterface.findDictionaryByIdKeyDesc("地市", regionName);
        String regionCode = null == dictionaryVoRegion ? "" : dictionaryVoRegion.getDictKey();
        MemberInfoVO memberInfoVORes = doCreateMemberInfo(telNum, memberName, "", corpCreateRes, regionCode, regionCode);
        if (null == memberInfoVORes)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1049, telNum);

        boolean clientUserRes = doCreateClientUser(telNum, memberName, registAccountJson.getString("password"), corpCreateRes, memberInfoVORes, "", regionCode, regionCode, corpName);
        if (!clientUserRes)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1050, telNum);
        /** 处理激活营销信息 */
        dealActiveInfo(telNum);
        /** 用户注册成功，获得积分 ，10注册新用户 */
        integralUtil.integralSigns(telNum, "10");

        // /** 用户激活后维护邀请有礼数据 */
        // String corpId = memberInfoVORes.getCorpId();
        // String region = "";
        // if (!org.springframework.util.StringUtils.isEmpty(corpId)) {
        // CorpVO corpVO = corpInterface.findById(corpId);
        // if (corpVO != null) {
        // region = corpVO.getCorpRegion() == null ? "" : corpVO.getCorpRegion();
        // }
        // }
        // inviteShareInterface.updateInviteWhenActive(telNum, sdf.format(new Date()), region);

        /** IMS 开户 */
        openIms(memberInfoVORes.getMemId(), telNum);

        /** 开户完成清除redis */
        redisInterface.del(INTERNET_USERINFO + telNum);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * 互联网添加人员 6009 //member
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String inviterMemberInfo(String requestBody, String userId) {
        logger.debug("互联网添加人员,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpUserId = requestJson.getString("userId");
        String inviterTelNum = requestJson.getString("inviterTelNum");
        String inviterName = requestJson.getString("inviterName");
        String inviterDuty = requestJson.getString("inviterDuty");

        logger.debug("互联网添加人员(解析body),corpUserId:{},inviterTelNum:{},inviterName:{},inviterDuty:{}", corpUserId, inviterTelNum, inviterName, inviterDuty);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(corpUserId, inviterName, inviterTelNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, "");

        ClientUserVO clientUserVO = clientUserInterface.findById(corpUserId);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1063, "");

        CorpVO corpVO = corpInterface.findById(clientUserVO.getCorpId());
        logger.debug("互联网添加人员(查询企业),corpUserId:{},inviterTelNum:{},inviterName:{},inviterDuty:{},corpVO:{}", corpUserId, inviterTelNum, inviterName, inviterDuty, JSON.toJSONString(corpVO));
        if (null == corpVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1051, "");

        List<DepartMentVO> enterpriseVOs = departMentInterface.findByCorpId(corpVO.getCorpId());
        logger.debug("互联网添加人员(查询部门),corpUserId:{},inviterTelNum:{},inviterName:{},inviterDuty:{},enterpriseVOs:{}", corpUserId, inviterTelNum, inviterName, inviterDuty,
                JSON.toJSONString(enterpriseVOs));
        if (null == enterpriseVOs || enterpriseVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1052, "");

        DepartMentVO deptEnterVo = null;
        for (DepartMentVO enterpriseVO : enterpriseVOs) {
            if (!enterpriseVO.getParentDeptNum().equals("1")) {
                deptEnterVo = enterpriseVO;
                break;
            }
        }

        List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(inviterTelNum);// memberInfoInterface.findByTelNum(inviterTelNum);
        logger.debug("互联网添加人员(添加人员是否存在),corpUserId:{},inviterTelNum:{},inviterName:{},inviterDuty:{},memberInfoVOs:{}", corpUserId, inviterTelNum, inviterName, inviterDuty,
                JSON.toJSONString(memberInfoVOs));
        if (null != memberInfoVOs && !memberInfoVOs.isEmpty()) {
            List<MemberInfoVO> memberInfoVOsInvs = memberInfoUtil.findMemberInfoVoByCorpIdAndTelNum(corpVO.getCorpId(), inviterTelNum);// memberInfoInterface.findMemberInfoVoByCorpIdAndTelNum(corpVO.getCorpId(),
                                                                                                                                       // inviterTelNum);
            if (null != memberInfoVOsInvs && memberInfoVOsInvs.size() > 0)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1053, "");
            MemberInfoVO memberInfoVORes = doCreateMemberInfo(inviterTelNum, inviterName, inviterDuty, deptEnterVo, corpVO.getCorpRegion(), corpVO.getCorpRegion());
            if (null == memberInfoVORes)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1054, "");
            List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(inviterTelNum);
            logger.debug("互联网添加人员(添加人员已注册),corpUserId:{},inviterTelNum:{},inviterName:{},inviterDuty:{},clientUserVOs:{}", corpUserId, inviterTelNum, inviterName, inviterDuty,
                    JSON.toJSONString(clientUserVOs));
            if (null != clientUserVOs && !clientUserVOs.isEmpty()) {
                boolean clientRes = doCreateClientUser(inviterTelNum, inviterName, clientUserVOs.get(0).getPwd(), deptEnterVo, memberInfoVORes, clientUserVOs.get(0).getPrivateKey(),
                        corpVO.getCorpRegion(), corpVO.getCorpArea(), corpVO.getCorpName());
                if (!clientRes)
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1054, "");
            }
        } else {
            MemberInfoVO memberInfoVORes = doCreateMemberInfo(inviterTelNum, inviterName, inviterDuty, deptEnterVo, corpVO.getCorpRegion(), corpVO.getCorpRegion());
            if (null == memberInfoVORes)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1054, "");
        }

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 获取灰度发布开关
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String findGrayReleaseByCorpID(String requestBody, String userId) {
        logger.debug("获取灰度发布开关,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpid = requestJson.getString("corpid");

        logger.debug("获取灰度发布开关(解析body),corpid:{}", corpid);
        /** 校验参数 */
        if (!StringUtils.checkParamNull(corpid))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1034, "");

        /** 灰度发布开关 */
        boolean grayRelease = gatedlaunchInterface.findGrayReleaseByCorpID(corpid);

        logger.debug("获取灰度发布开关,grayRelease:{}", grayRelease);

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("grayRelease", grayRelease);
        /** 加密返回body */
        // String userKey = getUserKeyByTelNum(userId);
        // if (null == userKey)
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
        String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userId);
        return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 处理激活营销活动信息
     * 
     * @param clientUser
     * @param telNum
     * @Description:
     */
    private void dealActiveInfo(String telNum) {
        logger.debug("处理激活营销活动信息入MQ，telNum:{}", telNum);
        if (null == telNum || "".equals(telNum))
            return;
        LogRocketMqUtil.send(RocketMqUtil.activeDealQueue, telNum);
    }

    /**
     * 获取提示信息
     * 
     * @param tip
     * @return
     * @Description:
     */
    private String getTipInfo(String tip) {
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("tipInfo", tip);
        return JSONObject.toJSONString(bodyJson);
    }

    /**
     * 获取注册url
     * 
     * @param telNum
     * @return
     * @Description:
     */
    // private String getRegistUrl(String telNum) {
    // JSONObject bodyJson = new JSONObject();
    // String registUrl = "/internet.do?act=toRegiste&telNum=" + telNum + "&type=0";
    // bodyJson.put("registUrl", registUrl);
    // return JSONObject.toJSONString(bodyJson);
    // }

    /**
     * 修改vwt_client_user表用户密码
     * 
     * @param clientUserVO
     * @param password
     * @return
     * @Description:
     */
    private boolean editClientUserPwd(List<ClientUserVO> clientUserVOs, String password) {
        for (ClientUserVO clientUserVO : clientUserVOs) {
            clientUserVO.setPwd(password);
            if (null == clientUserInterface.saveUser(clientUserVO))
                return false;
        }
        return true;
    }

    /**
     * 激活用户后保存至vwt_client_user表
     * 
     * @param clientUser
     * @param password
     * @return
     * @Description:
     */
    private boolean saveClientUser(List<MemberInfoVO> memberInfoVOs, String password, String imei) {
        logger.debug("激活用户后保存至vwt_client_user表,memberInfoVOs:{},password:{}", JSON.toJSONString(memberInfoVOs), password);
        try {
            for (MemberInfoVO memberInfoVO : memberInfoVOs) {
                ClientUserVO userVO = new ClientUserVO();
                userVO.setCorpId(memberInfoVO.getCorpId());
                userVO.setCreateTime(new Date());
                userVO.setDeptId(memberInfoVO.getDeptId());
                userVO.setFromChannel(memberInfoVO.getFromChannel());
                userVO.setPwd(password);
                userVO.setTelNum(memberInfoVO.getTelNum());
                userVO.setUserId(memberInfoVO.getMemId());
                userVO.setUserName(memberInfoVO.getMemberName());
                userVO.setUserState("Y");
                userVO.setImsi(imei);
                // userVO.setClique("1");
                clientUserInterface.saveUser(userVO);

                memberInfoVO.setMemStatus("1");
                memberInfoUtil.saveMemberInfo(memberInfoVO, memberInfoVO.getFromChannel());

                CorpVO corp = corpInterface.findById(memberInfoVO.getCorpId());
                /** 激活成功后保存信息到mq，提供给经分数据 */
                UserLogVO userLogVo = new UserLogVO();
                userLogVo.setAreaCode(corp == null ? "" : corp.getCorpArea());
                userLogVo.setCityCode(corp == null ? "" : corp.getCorpRegion());
                userLogVo.setCorpId(memberInfoVO.getCorpId());
                userLogVo.setDealFlag(1);
                userLogVo.setDealTime(sdf.format(new Date()));
                userLogVo.setIMEI(imei);
                userLogVo.setMobile(memberInfoVO.getTelNum());
                userLogVo.setSeq(System.nanoTime());
                userLogVo.setUserId(memberInfoVO.getMemId());
                userLogVo.setUuid(UUID.randomUUID().toString());
                UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.userRecordQueue, JSON.toJSONString(userLogVo));
                
                /** 推送用户欢迎语 */
                pushToNewUser(userVO, corp.getCorpName(), memberInfoVO.getPartName());

            }
        } catch (Exception e) {
            logger.error("激活用户后保存至vwt_client_user表异常,memberInfoVOs:{},password:{}", JSON.toJSONString(memberInfoVOs), password, e);
            return false;
        }
        return true;
    }

    /**
     * 保存短信验证码
     * 
     * @param telNum
     * @param verifyCode
     * @return
     * @Description:
     */
    private boolean saveVerifyCode(String telNum, String verifyCode, String content) {
        return sendSmsInterface.sendVerifyCodeSms(telNum, "1", content, verifyCode, 600000L);
    }

    /**
     * 组装短信内容
     * 
     * @param content
     * @param verifyCode
     * @param sign
     * @return
     * @Description:
     */
    private String getSmsContent(String content, String verifyCode, String period, String sign) {
        return content + verifyCode + period + sign;
    }

    /**
     * 返回四位随机数字
     * 
     * @return
     * @Description:
     */
    private String getRandomString() {
        Random random = new Random();
        String verifyCode = "";
        for (int i = 0; i < 4; i++) {
            String rand = String.valueOf(random.nextInt(10));
            verifyCode += rand;
        }
        return verifyCode;
    }

    /**
     * 检查用户是否存在
     * 
     * @param telNum
     * @return
     * @Description:
     */
    private List<MemberInfoVO> checkUserExist(String telNum) {
        return memberInfoUtil.findMemberInfosByTelNum(telNum);// memberInfoInterface.findByTelNum(telNum);
    }

    /**
     * 根据手机号码检查用户是否激活
     * 
     * @param telNum
     * @return
     * @Description:
     */
    private List<ClientUserVO> checkUserActive(String telNum) {
        return clientUserInterface.findByTelNum(telNum);
    }

    /**
     * 校验获取列表请求参数
     * 
     * @param pageIndex
     * @param pageSize
     * @param userName
     * @return
     * @Description:
     */
    private boolean valicateParams(String... strs) {
        for (String string : strs) {
            if (null == string || "".equals(string))
                return false;
        }
        return true;
    }

    /**
     * 保存激活信息
     * 
     * @param memberId
     * @param corpId
     * @Description:
     */
    private boolean saveMemberActive(List<MemberInfoVO> memberInfoVOs) {
        logger.debug("保存激活信息,memberInfoVOs:{}", JSON.toJSONString(memberInfoVOs));
        try {
            for (MemberInfoVO memberInfoVO : memberInfoVOs) {
                if (!memberActiveInterface.saveMemberActive(memberInfoVO.getMemId(), memberInfoVO.getCorpId()))
                    return false;
            }
        } catch (Exception e) {
            logger.error("保存激活信息异常,memberInfoVOs:{}", JSON.toJSONString(memberInfoVOs), e);
            return false;
        }
        return true;
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
            if (!"省直".equals(dictionaryVo.getDictKeyDesc())&&!"省测试".equals(dictionaryVo.getDictKeyDesc()))
                regionInfo.add(dictionaryVo.getDictKeyDesc());
        }
        /** 注意：是根据的汉字的拼音的字母排序的，而不是根据汉字一般的排序方法 */
        Collections.sort(regionInfo, Collator.getInstance(java.util.Locale.CHINA));
        return regionInfo;
    }

    /**
     * 新建企业
     * 
     * @param telNum
     * @param corpName
     * @param memberName
     * @param regionName
     * @return
     * @Description:
     */
    public DepartMentVO doCreateCorp(String telNum, String corpName, String memberName, String regionName) {
        logger.debug("新建企业,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName);

        try {
            DictionaryVo dictionaryVo = dictionaryInterface.findDictionaryByIdKeyDesc("行业", "电脑互联网");
            DictionaryVo dictionaryVoRegion = dictionaryInterface.findDictionaryByIdKeyDesc("地市", regionName);
            // 调oracle服务维护oracle
            CorpVO corpVo = new CorpVO();
            corpVo.setCorpName(corpName);
            corpVo.setCorpIndustry(null == dictionaryVo ? "" : dictionaryVo.getDictKey());
            corpVo.setCorpPersonname(memberName);
            corpVo.setCorpMobilephone(telNum);
            corpVo.setFromchannel(7L);
            corpVo.setPkState(1L);
            corpVo.setCorpStarttime(new Date());
            corpVo.setCorpArea(null == dictionaryVoRegion ? "" : dictionaryVoRegion.getDictKey());
            corpVo.setCorpRegion(null == dictionaryVoRegion ? "" : dictionaryVoRegion.getDictKey());
            logger.debug("新建企业(企业信息)，corpVo{}", JSON.toJSON(corpVo));
            CorpVO corpVoRes = corpInterface.saveCorp(corpVo);
            logger.debug("新建企业(新建后返回企业信息),corpVoRes:{}", JSON.toJSON(corpVoRes));
            if (corpVoRes != null) {
                DepartMentVO enterpriseVOCorp = new DepartMentVO();
                enterpriseVOCorp.setParentDeptNum("1");
                enterpriseVOCorp.setCorpId(corpVoRes.getCorpId());
                enterpriseVOCorp.setCorpStatus("1");
                enterpriseVOCorp.setFromChannel("7");
                enterpriseVOCorp.setPartFullName(corpName);
                enterpriseVOCorp.setPartName(corpName);
                enterpriseVOCorp.setSort(1L);
                enterpriseVOCorp.setActTime(new Date());

                logger.debug("新建企业(企业部门信息),enterpriseVOCorp{}", JSON.toJSON(enterpriseVOCorp));

                DepartMentVO enterpriseVOCorpRes = departMentInterface.save(enterpriseVOCorp);

                if (enterpriseVOCorpRes == null) {
                    logger.error("新建企业异常,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName);
                    return null;
                }

                DepartMentVO enterpriseVO = new DepartMentVO();
                enterpriseVO.setParentDeptNum(enterpriseVOCorpRes.getDeptId());
                enterpriseVO.setCorpId(corpVoRes.getCorpId());
                enterpriseVO.setCorpStatus("1");
                enterpriseVO.setFromChannel("7");
                enterpriseVO.setPartFullName(corpName + "/" + memberName + "的体验部门");
                enterpriseVO.setPartName(memberName + "的体验部门");
                enterpriseVO.setSort(2L);
                enterpriseVO.setActTime(new Date());

                logger.debug("新建企业(部门信息),enterpriseVO{}", JSON.toJSON(enterpriseVO));

                DepartMentVO enterprise = departMentInterface.save(enterpriseVO);

                if (enterprise == null) {
                    logger.error("新建企业异常,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName);
                    return null;
                }
                CliqueInfoVO cliqueInfoVO = new CliqueInfoVO();
                cliqueInfoVO.setId(databaseInterface.generateId("vwt_clique", "id"));
                cliqueInfoVO.setCliqueId(1L);
                cliqueInfoVO.setCliqueName("集团1");
                cliqueInfoVO.setCorpId(corpVoRes.getCorpId());
                cliqueInfoInterface.saveCliqueInfo(cliqueInfoVO);

                // 企业开户成功后，信息保存到mq，提供给经分数据
                CorpLogVO corpLogVo = new CorpLogVO();
                corpLogVo.setBossid("");
                corpLogVo.setChangeCustomer(0);
                corpLogVo.setCorpArea(corpVoRes.getCorpArea() == null ? "" : corpVoRes.getCorpArea());
                corpLogVo.setCorpRegion(corpVoRes.getCorpRegion() == null ? "" : corpVoRes.getCorpRegion());
                corpLogVo.setCorpId(corpVoRes.getCorpId());
                corpLogVo.setCorpName(corpVoRes.getCorpName());
                CustomerVo customerVo = customerInterface.findCustomerById(corpVoRes.getCustomerId());
                corpLogVo.setCustManagerMobile(customerVo == null ? "" : customerVo.getTelNum());
                corpLogVo.setCustManagerName(customerVo == null ? "" : customerVo.getName());
                corpLogVo.setCustomerId(corpVoRes.getCustomerId());
                corpLogVo.setDealFlag(0);
                corpLogVo.setDealTime(sdf.format(new Date()));
                corpLogVo.setFROMCHANNEL(7);
                corpLogVo.setOther("");
                corpLogVo.setSeq(System.nanoTime());
                corpLogVo.setUuid(UUID.randomUUID().toString());
                UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.corpRecordQueue, JSON.toJSONString(corpLogVo));
                return enterprise;
            } else {
                logger.error("新建企业异常,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName);
                return null;
            }
        } catch (Exception e) {
            logger.error("新建企业异常,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName, e);
            return null;
        }
    }

    /**
     * 创建人员(互联网注册)
     * 
     * @param telNum
     * @param memberName
     * @param password
     * @param duty
     * @param enterpriseVO
     * @return
     * @Description:
     */
    private MemberInfoVO doCreateMemberInfo(String telNum, String memberName, String duty, DepartMentVO enterpriseVO, String regionCode, String areaCode) {
        logger.debug("创建人员(互联网注册),telNum:{},memberName:{},enterpriseVO:{}", telNum, memberName, JSON.toJSONString(enterpriseVO));
        try {
            Long sortMax =0L;// hlwMemberInfoInterface.findMaxSortByCorpId(enterpriseVO.getCorpId());
            MemberInfoVO memberInfoVO = new MemberInfoVO();
            memberInfoVO.setUserCreateTime(new Date());
            memberInfoVO.setCorpId(enterpriseVO.getCorpId());
            memberInfoVO.setCreatTime(new Date());
            memberInfoVO.setDeptId(enterpriseVO.getDeptId());
            memberInfoVO.setFromChannel(7L);
            memberInfoVO.setMemberName(memberName);
            memberInfoVO.setMemStatus("1");
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
            logger.error("创建人员(互联网注册)异常,telNum:{},memberName:{},enterpriseVO:{}", telNum, memberName, JSON.toJSONString(enterpriseVO), e);
            return null;
        }
    }

    /**
     * 激活人员(互联网注册)
     * 
     * @param telNum
     * @param memberName
     * @param password
     * @param enterpriseVO
     * @param memberInfoVORes
     * @return
     * @Description:
     */
    private boolean doCreateClientUser(String telNum, String memberName, String password, DepartMentVO enterpriseVO, MemberInfoVO memberInfoVORes, String privateKey, String regionCode,
            String areaCode, String corpName) {
        logger.debug("激活人员(互联网注册),telNum:{},memberName:{},password:{},memberInfoVORes:{},enterpriseVO:{},privateKey:{}", telNum, memberName, password, JSON.toJSONString(memberInfoVORes),
                JSON.toJSONString(enterpriseVO), privateKey);
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

            pushToNewUser(clientUserVORes, corpName, enterpriseVO.getPartName());

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
            logger.error("激活人员(互联网注册),telNum:{},memberName:{},password:{},memberInfoVORes:{},enterpriseVO:{}", telNum, memberName, password, JSON.toJSONString(memberInfoVORes),
                    JSON.toJSONString(enterpriseVO), e);
            return false;
        }
        return true;
    }

    /**
     * 获取用户对应的密钥
     * 
     * @param telNum
     * @return
     * @Description:
     */
    // private String getUserKeyByTelNum(String userId) {
    // logger.debug("获取用户对应的密钥,userId:{}", userId);
    // try {
    // ClientUserVO clientUserVO = clientUserInterface.findById(userId);
    // logger.debug("获取用户对应的密钥,userId:{},clientUserVO:{}", userId, clientUserVO);
    // if (null == clientUserVO)
    // return null;
    // logger.debug("获取用户对应的密钥,userId:{},privateKey:{}", userId, clientUserVO.getPrivateKey());
    // return clientUserVO.getPrivateKey();
    // } catch (Exception e) {
    // logger.error("获取用户对应的密钥异常,userId:{}", userId, e);
    // return null;
    // }
    //
    // }

    /**
     * 用户激活时，进行欢迎提醒
     * 
     * @param clientUserVO
     * @param departMentVO
     * @param content
     */
    private void pushToNewUser(ClientUserVO clientUserVO, String corpName, String departName) {
        /** 推送服务号消息 */
        msgPushUtil.pushVwtHelpToNewUser(clientUserVO.getUserId());
        /** 推送文字提示 */
        // XXX（企业名称），欢迎XX（用户）加入
        String content = "欢迎加入" + corpName;
        msgPushUtil.pushHlwNewUser(clientUserVO.getUserId(), content, clientUserVO.getCorpId(), clientUserVO.getDeptId(), departName);
    }
}
