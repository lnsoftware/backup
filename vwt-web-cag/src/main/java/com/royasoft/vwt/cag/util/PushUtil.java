package com.royasoft.vwt.cag.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.service.ActionRecordService;
import com.royasoft.vwt.cag.util.mq.RocketMqUtil;
import com.royasoft.vwt.cag.vo.NewsItem;
import com.royasoft.vwt.cag.vo.RedisAction;
import com.royasoft.vwt.cag.vo.ServicePush;
import com.royasoft.vwt.cag.vo.WorkBenchAction;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareInterface;
import com.royasoft.vwt.soa.business.square.api.vo.SquareVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;

@Component
public class PushUtil {

    private final static Logger logger = LoggerFactory.getLogger(PushUtil.class);

    @Autowired
    private ClientUserInterface clientUserInterface;
    @Autowired
    private ActionRecordService actionRecordService;
    @Autowired
    private SquareInterface squareInterface;
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 推送文本
     * 
     * @param serviceId
     * @param telNum
     * @param content
     * @param fromType
     */
    public void pushTextNewIm(String serviceId, String telNum, String content, String fromType) throws Exception {
        SquareVo squareVo = getPushObject(serviceId, telNum);
        if (null != squareVo) {
            String serviceName = squareVo.getName();
            String serviceLogo = squareVo.getLogo();
            pushMenuTextMsg(serviceName, serviceLogo, serviceId, telNum, content, fromType);
        }
    }

    /**
     * 推送图文
     * 
     * @param serviceId
     * @param msg
     * @param cellPhone
     * @param fromType
     */
    public void pushGraphicNewIm(String serviceId, JSONObject msg, String cellPhone, String fromType) throws Exception {
        SquareVo squareVo = getPushObject(serviceId, cellPhone);
        if (null != squareVo) {
            String serviceName = squareVo.getName();
            String serviceLogo = squareVo.getLogo();
            pushMenuGraphicMsg(msg, serviceName, serviceLogo, serviceId, cellPhone, fromType);
        }
    }

    /**
     * 获取图文推送接收者
     * 
     * @param serviceId
     * @param corpId
     * @return
     */
    public SquareVo getPushObject(String serviceId, String corpId) {
        return squareInterface.findSquareById(serviceId);
    }
    
    
    /**
     * 文本消息入REDIS 且调用推送
     * 
     * @param serviceName
     * @param serviceLogo
     * @param serviceId
     * @param cellPhone
     * @param content
     * @param fromType
     */
    public void pushMenuTextMsg(String serviceName, String serviceLogo, String serviceId, String cellPhone, String content, String fromType)
            throws Exception {
        
        // 入redis
        WorkBenchAction message = servicePushTextNew(serviceName, serviceLogo, serviceId, content);
        RedisAction ra = new RedisAction();
        ra.setMessage(message);
        ra.setSource(RocketMqUtil.SOURCE);
        ra.setHead(RocketMqUtil.SQUARE_HEAD);
        ra.setCreateTime(dateFormat.format(new Date()));
        try {
            long msgId = actionRecordService.save(ra);
            sendMenuMsg(serviceId, msgId, cellPhone, message, 2, "", getApnsContent(content), fromType);
        } catch (Exception e) {
            logger.error("调用推送异常", e);
        }

    }

    /**
     * 图文消息入REDIS 且调用推送
     * 
     * @param msg
     * @param serviceName
     * @param serviceLogo
     * @param serviceId
     * @param cellPhone
     * @param fromType
     */
    public void pushMenuGraphicMsg(JSONObject msg, String serviceName, String serviceLogo, String serviceId, String cellPhone, String fromType)
            throws Exception {
        WorkBenchAction message = servicePushGraphicNew(msg, serviceName, serviceLogo, serviceId);
        RedisAction ra = new RedisAction();
        ra.setMessage(message);
        ra.setSource(RocketMqUtil.SOURCE);
        ra.setHead(RocketMqUtil.SQUARE_HEAD);
        ra.setCreateTime(dateFormat.format(new Date()));
        try {
            long msgId = actionRecordService.save(ra);
            sendMenuMsg(serviceId, msgId, cellPhone, message, 2, "", getApnsContent(msg.getString("title")), fromType);
        } catch (Exception e) {
            logger.error("调用推送异常", e);
        }

    }
    /**
     * 获取APNS推送内容
     * @param content
     * @return
     */
    public static String getApnsContent(String content){
        if(null==content||"".equals(content.trim())){
            return "";
        }
        content=content.trim();
        if(content.length()<=34){
            return content;
        }
        return content.substring(0,31)+"...";
    }
    /**
     * 消息入MQ
     * 
     * @param serviceId
     * @param msg_id
     * @param cellPhone
     * @param message
     * @param type
     * @param imsi
     * @param apnsContent
     * @param fromType
     */
    public void sendMenuMsg(String serviceId, long msg_id, String cellPhone, WorkBenchAction message, int type, String imsi, String apnsContent,
            String fromType) throws Exception {
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
        jsonObject.put("priority", fromType);// 优先级
        RocketMqUtil.send("PushQueue", jsonObject.toJSONString());
    }

    /**
     * 调用服务推送文本
     * 
     * @param mainId
     * @param serviceName
     * @param serviceLogo
     * @param serviceId
     * @param target_type
     * @param target
     */
    public WorkBenchAction servicePushTextNew(String serviceName, String serviceLogo, String serviceId, String content) {
        WorkBenchAction action = new WorkBenchAction();
        ServicePush push = new ServicePush();

        push.setMsgUUID(UUID.randomUUID().toString());
        push.setServiceName(serviceName);
        push.setContent(content);
        push.setMsgType("text");
        push.setServiceId(serviceId);
        push.setCreateTime(System.currentTimeMillis());
        push.setServiceType("");
        push.setLogoAddr(serviceLogo);
        push.setArticleCount(0);
        action.setContent(JSON.toJSONString(push));
        action.setType(1);

        return action;
    }

    /**
     * 调用服务推送图文
     * 
     * @param mainId
     * @param serviceName
     * @param serviceLogo
     * @param serviceId
     * @param target_type
     * @param target
     */
    public WorkBenchAction servicePushGraphicNew(JSONObject msgJson, String serviceName, String serviceLogo, String serviceId) {
        WorkBenchAction action = new WorkBenchAction();
        ServicePush push = new ServicePush();
        List<NewsItem> newsItemList = new ArrayList<NewsItem>();
        String graContent = msgJson.getString("content");
        JSONArray sourceVos = JSONArray.parseArray(graContent);
        for (int i = 0; i < sourceVos.size(); i++) {
            NewsItem newsItem = new NewsItem();
            String htmlUrl = sourceVos.getJSONObject(i).getString("clickUrl");
            String titlePicUrl = sourceVos.getJSONObject(i).getString("titlePicUrl");
            String titleDesc = sourceVos.getJSONObject(i).getString("titleDesc");
            if ("mainTitle".equals(sourceVos.getJSONObject(i).getString("type"))) {
                push.setMainTitle(titleDesc);

            }
            newsItem.setDescription("");
            newsItem.setId("");
            newsItem.setPicUrl(titlePicUrl);
            newsItem.setTitle(titleDesc);
            newsItem.setUrl(htmlUrl);
            newsItemList.add(newsItem);
        }

        push.setMsgUUID(UUID.randomUUID().toString());
        push.setServiceName(serviceName);
        push.setContent(newsItemList);
        push.setMsgType("news");
        push.setServiceId(serviceId);
        push.setCreateTime(System.currentTimeMillis());
        push.setServiceType("");
        push.setLogoAddr(serviceLogo);
        push.setArticleCount(sourceVos.size());
        action.setContent(JSON.toJSONString(push));
        action.setType(1);

        return action;
    }
}
