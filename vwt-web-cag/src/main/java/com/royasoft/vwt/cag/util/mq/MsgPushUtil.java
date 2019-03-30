package com.royasoft.vwt.cag.util.mq;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.service.WorkBenchService;
import com.royasoft.vwt.cag.vo.WorkBenchAction;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareInterface;
import com.royasoft.vwt.soa.business.square.api.vo.SquareVo;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

@Component
@Scope("singleton")
public class MsgPushUtil {

    private static final Logger logger = LoggerFactory.getLogger(MsgPushUtil.class);

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** 服务号-关于V网通 */
    private static final String VWT_ABOUTVWT_NAMESPACE = "VWT:SERVICENO:ABOUTVWT";

    @Autowired
    private SquareInterface squareInterface;

    @Autowired
    private static MemberInfoInterface memberInfoInterface;

    @Autowired
    WorkBenchService workBenchService;

    @Autowired
    private ActionRecordUtil actionRecordUtil;

    @Autowired
    private RedisInterface redisInterface;

    private static final List<String> vip = new ArrayList<String>();
    static {
        vip.add("15805180188");
        vip.add("13905189360");
        vip.add("13505192662");
        vip.add("13809000106");
        vip.add("13901587226");
        vip.add("13901588667");
        vip.add("13905189984");
        vip.add("13905189836");
        vip.add("13905189087");
        vip.add("13905189816");
        vip.add("13905172972");
    }

    public static void sendSingleMsg(String connectType, String type, String message, String telNum, String topic, String position, String uuid, String clientType, String imsi, String apnsContent) {
        if (position.equals("34")) {
            if (!vip.contains(telNum)) {
                apnsContent = "";
                uuid = "";
            }

        }
        String sendMsg = createMsgContent(message, telNum, type, position, uuid, clientType, imsi, apnsContent);
        MQProvideUtil.send(connectType, sendMsg);
    }

    public static void sendMenuMsg(String serviceId, long msg_id, String cellPhone, WorkBenchAction message, int type, String imsi, String apnsContent) {
        com.alibaba.fastjson.JSONObject obj = new com.alibaba.fastjson.JSONObject();
        obj.put("serviceId", serviceId);
        obj.put("msgId", msg_id);
        obj.put("roleId", cellPhone);//
        obj.put("content", message);
        obj.put("type", type);
        obj.put("apnsContent", apnsContent);
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("content", obj);
        jsonObject.put("type", 1);
        RocketMqUtil.send("PushQueue", jsonObject.toJSONString());
    }

    public static void sendCorpMsg(String connectType, String topic, JSONObject message, String corpIds, String position, String apnsContent) {
        String[] corpIdArray = corpIds.split(",");
        for (String corpId : corpIdArray) {
            List<MemberInfoVO> members = memberInfoInterface.findByCorpId(corpId);
            if (null != members && !members.isEmpty()) {
                for (MemberInfoVO menberInfo : members) {
                    String uuid = UUID.randomUUID().toString();
                    message.put("uuid", uuid);

                    sendSingleMsg("COMMON", "1", message.toString(), menberInfo.getTelNum(), topic, position, uuid, "", "", apnsContent);
                }
            }
        }
    }

    public static void sendCorpMsgUUID(String connectType, String topic, JSONObject message, String corpIds, String position, String apnsContent) {
        String[] corpIdArray = corpIds.split(",");
        for (String corpId : corpIdArray) {
            List<MemberInfoVO> members = memberInfoInterface.findByCorpId(corpId);
            if (null != members && !members.isEmpty()) {
                for (MemberInfoVO menberInfo : members) {
                    String uuid = UUID.randomUUID().toString();
                    message.put("uuid", uuid);
                    String realUUID = "";
                    if (vip.contains(menberInfo.getTelNum()))
                        realUUID = uuid;
                    sendSingleMsg("COMMON", "1", message.toString(), menberInfo.getTelNum(), topic, position, realUUID, "", "", apnsContent);
                }
            }
        }
    }

    public static void sendSingleMsgUUID(String connectType, String type, String message, String telNum, String topic, String position, String uuid, String clientType, String imsi, String apnsContent) {
        if (position.equals("34")) {
            if (!vip.contains(telNum)) {
                apnsContent = "";
                uuid = "";
            }

        }

        String sendMsg = createMsgContent(message, telNum, type, position, uuid, clientType, imsi, apnsContent);
        MQProvideUtil.send(connectType, sendMsg);
    }

    private static String createMsgContent(String message, String telNum, String type, String position, String uuid, String clientType, String imsi, String apnsContent) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("telNum", telNum);
        jsonObject.put("content", message);
        jsonObject.put("position", position);
        jsonObject.put("uuid", uuid);
        jsonObject.put("clientType", clientType);
        jsonObject.put("imsi", imsi);
        jsonObject.put("apnsContent", apnsContent);
        return jsonObject.toString();
    }

    public static void sendSingleMsg1(String connectType, String type, String message, String telNum, String topic, String position, String uuid, String clientType, String imsi, String apnsContent,
            String taskInfo) {
        String sendMsg = createMsgContent1(message, telNum, type, position, uuid, clientType, imsi, apnsContent, taskInfo);
        MQProvideUtil.send(connectType, sendMsg);
    }

    private static String createMsgContent1(String message, String telNum, String type, String position, String uuid, String clientType, String imsi, String apnsContent, String taskInfo) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("telNum", telNum);
        jsonObject.put("taskInfo", taskInfo);
        jsonObject.put("content", message);
        jsonObject.put("position", position);
        jsonObject.put("uuid", uuid);
        jsonObject.put("clientType", clientType);
        jsonObject.put("imsi", imsi);
        jsonObject.put("apnsContent", apnsContent);
        return jsonObject.toString();
    }

    public void pushHlwNewUser(String userId, String content, String corpId, String deptId, String deptName) {
        JSONObject json = new JSONObject();
        json.put("roleId", userId);
        json.put("text", content);
        json.put("corpId", corpId);
        json.put("deptId", deptId);
        json.put("deptName", deptName);
        json.put("serverTime", System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", json);
        jsonObject.put("type", 1);
        jsonObject.put("requestId", UUID.randomUUID().toString());
        jsonObject.put("roleId", userId);
        RocketMqUtil.send("BuinessPushQueue", jsonObject.toJSONString());
    }

    public void pushVwtHelpToNewUser(String userId) {
        try {
            // 入redis
            SquareVo squareVO = squareInterface.findSquareById(ParamConfig.ABOUTVWT_ID);
            if (squareVO == null) {
                logger.error("新注册用户服务号推送异常,对应id服务号不存在:{}", ParamConfig.ABOUTVWT_ID);
                return;
            }
            WorkBenchAction message = workBenchService.servicePushTextNew(squareVO.getName(), squareVO.getLogo(), ParamConfig.ABOUTVWT_ID, ParamConfig.ABOUTVWT_TIPS, 1);
            String msgIdStr = redisInterface.getString(VWT_ABOUTVWT_NAMESPACE);
            long msgId = 0;
            if (StringUtils.isEmpty(msgIdStr)) {
                RedisAction ra = new RedisAction();
                ra.setMessage(message);
                ra.setSource(RocketMqUtil.SOURCE);
                ra.setHead(RocketMqUtil.SQUARE_HEAD);
                ra.setCreateTime(dateFormat.format(new Date()));
                msgId = actionRecordUtil.save(ra);
                redisInterface.setString(VWT_ABOUTVWT_NAMESPACE, msgId + "");
            } else {
                msgId = Long.parseLong(msgIdStr);
            }
            logger.debug("新用户帮助服务号推送,serviceid:{},msgid:{},userid:{},message:{},type:{}", ParamConfig.ABOUTVWT_ID, msgId, userId, JSON.toJSONString(message), 2);
            MsgPushUtil.sendMenuMsg(ParamConfig.ABOUTVWT_ID, msgId, userId, message, 2, "", "");
        } catch (Exception e) {
            logger.error("新注册用户服务号推送异常,userId:{},serviceId:{},content:{},e:{}", userId, ParamConfig.ABOUTVWT_ID, ParamConfig.ABOUTVWT_TIPS, e);
        }
    }

}
