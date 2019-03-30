/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.util.ArrayList;
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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.StringUtils;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendProvinceSmsInterface;
import com.royasoft.vwt.soa.sundry.unregisteRemind.api.interfaces.UnregisteRemindInterface;
import com.royasoft.vwt.soa.sundry.unregisteRemind.api.vo.UnregisteRemindVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.CWTMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.HLWMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.XXTMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 通讯录相关处理类 omc菜单中通讯录相关菜单:通讯录导入、注册人员、信息反馈
 * 
 * @author daizl
 *
 */
@Scope("prototype")
@Service
public class AddressService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private DepartMentInterface departMentInterface;

    @Autowired
    private CorpInterface corpInterface;

    @Autowired
    private MemberInfoInterface memberInfoInterface;

    @Autowired
    private HLWMemberInfoInterface HLWMemberInfoInterface;

    @Autowired
    private CWTMemberInfoInterface CWTMemberInfoInterface;

    @Autowired
    private XXTMemberInfoInterface XXTMemberInfoInterface;

    @Autowired
    private SendProvinceSmsInterface sendProvinceSmsInterface;

    @Autowired
    private UnregisteRemindInterface unregisteRemindInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.address_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;

                    String function_id = queue_packet.getFunction_id();

                    String user_id = queue_packet.getUser_id(); // 获取用户ID

                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    String tel_number = queue_packet.getTel_number();
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                    /** 获取部门树 */
                        case FunctionIdConstant.UNREGISTE_DEPART:
                            resInfo = getDepartByCorpId(request_body);
                            break;
                        /** 根据部门节点获取人员 */
                        case FunctionIdConstant.UNREGISTE_MEMBER:
                            resInfo = getUnregisteMemberByDepartId(request_body);
                            break;
                        /** 发送短信 */
                        case FunctionIdConstant.UNREGISTE_SENDMSG:
                            resInfo = sendMsgToUngiste(request_body);
                            break;
                        default:
                            break;
                    }
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("通讯录处理类异常:{}", e);
            } finally {

            }

        }
    }

    /**
     * 根据企业id获取部门树
     * 
     * @param requestBody
     * @return
     * @author daizl 2016年5月26日
     */
    public String getDepartByCorpId(String requestBody) {
        logger.debug("未注册人员-获取部门,requestBody:{}", requestBody);

        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionid = requestJson.getString("sessionid");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            /** 获取企业id */
            String session = redisInterface.getString(Constants.nameSpace + sessionid);
            JSONObject sessionJson = JSONObject.parseObject(session);
            if (null == sessionJson)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
            String corpid = sessionJson.getString("corpId");
            if (!StringUtils.checkParam(corpid, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            /** 根据企业id查询企业部门 */
            List<DepartMentVO> list = departMentInterface.findByCorpId(corpid);
            if (list == null || list.isEmpty())
                list = new ArrayList<DepartMentVO>();
            return ResponsePackUtil.buildPack("0000", list);
        } catch (Exception e) {
            logger.error("未注册人员-获取部门异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }

    }

    /**
     * 根据部门id获取未注册人员
     * 
     * @param requestBody
     * @return
     * @author daizl 2016年5月26日
     */
    public String getUnregisteMemberByDepartId(String requestBody) {
        logger.debug("未注册人员-获取人员,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String deptid = requestJson.getString("deptid");
            String sessionid = requestJson.getString("sessionid");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50) || !StringUtils.checkParam(deptid, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            /** 获取企业id */
            String session = redisInterface.getString(Constants.nameSpace + sessionid);
            JSONObject sessionJson = JSONObject.parseObject(session);
            if (null == sessionJson)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
            String corpId = sessionJson.getString("corpId");
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            /** 校验该企业是否存在 */
            CorpVO corpVO = corpInterface.findById(corpId);
            if (corpVO == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2052, "");

            /** 根据企业渠道，查询对应通讯录表 */
            List<MemberInfoVO> list = new ArrayList<MemberInfoVO>();
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_deptId", deptid);
            conditions.put("NE_memStatus", "1");
            // 来源渠道:1：V网通,4：村务通,5：校讯通,6：通讯助手,7：互联网
            switch (corpVO.getFromchannel() + "") {
                case "1":
                    list = memberInfoInterface.findMemberInfoByCondition(conditions, null);
                    break;
                case "4":
                    list = CWTMemberInfoInterface.findCWTMemberInfoByCondition(conditions, null);
                    break;
                case "5":
                    list = XXTMemberInfoInterface.findXXTMemberInfoByCondition(conditions, null);
                    break;
                case "6":
                    list = memberInfoInterface.findMemberInfoByCondition(conditions, null);
                    break;
                case "7":
                    list = HLWMemberInfoInterface.findHLWMemberInfoByCondition(conditions, null);
                    break;
                default:
                    break;
            }

            return ResponsePackUtil.buildPack("0000", list);
        } catch (Exception e) {
            logger.error("未注册人员-获取人员异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 发送提醒短信至未激活人员
     * 
     * @param requestBody
     * @return
     */
    public String sendMsgToUngiste(String requestBody) {
        logger.debug("未注册人员-短信提醒,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSON.parseObject(requestBody);
            String data = requestJson.getString("data");
            String sessionid = requestJson.getString("sessionid");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50) || org.springframework.util.StringUtils.isEmpty(data))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            /** 获取企业id */
            String session = redisInterface.getString(Constants.nameSpace + sessionid);
            JSONObject sessionJson = JSONObject.parseObject(session);
            if (null == sessionJson)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
            String corpId = sessionJson.getString("corpId");
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            /** 校验该企业是否存在 */
            CorpVO corpVO = corpInterface.findById(corpId);
            if (corpVO == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2052, "");

            /** 校验该企业本月是否已发送过 */
            UnregisteRemindVO urVO = unregisteRemindInterface.findByCorpId(corpId);
            Date now = new Date();
            if (null != urVO) {
                Date lastTime = urVO.getLastSendTime();
                if (now.getYear() == lastTime.getYear() && now.getMonth() == lastTime.getMonth())
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2053, "");
            } else {
                urVO = new UnregisteRemindVO();
                urVO.setId(UUID.randomUUID().toString());
                urVO.setCorpId(corpId);
            }
            urVO.setLastSendTime(now);
            urVO = unregisteRemindInterface.save(urVO);
            if (urVO == null)
                return null;

            JSONArray jsonArray = JSON.parseArray(data);
            List<String> hasSend = new ArrayList<String>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject member = JSONObject.parseObject(jsonArray.get(i).toString());
                String telnum = member.get("telnum") == null ? "" : member.get("telnum").toString();
                /** 发送短信 */
                if (!hasSend.contains(telnum)) {
                    hasSend.add(telnum);
                }
            }

            String content = "您好，" + corpVO.getCorpName() + "公司为您提供移动办公软件—「V网通」，用户名为手机号，首次登录点击客户端“我要注册”，即可设置密码登录。最新app下载地址为：http://112.4.17.117:10016/v/";
            sendProvinceSmsInterface.sendCommonSmsByList(hasSend, content);
            return ResponsePackUtil.buildPack("0000", "");
        } catch (Exception e) {
            logger.error("未注册人员-短信提醒异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }
}
