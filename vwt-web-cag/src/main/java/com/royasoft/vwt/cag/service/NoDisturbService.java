/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.common.security.AESUtil;
import com.royasoft.vwt.soa.business.im.api.interfaces.ImGroupInterface;
import com.royasoft.vwt.soa.business.im.api.interfaces.ImMessageInterface;
import com.royasoft.vwt.soa.business.im.api.interfaces.ImSquareInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.CircleInterface;
import com.royasoft.vwt.soa.newssync.api.interfaces.NewsSyncInterface;
import com.royasoft.vwt.soa.newssync.api.vo.NewsSyncVo;

/**
 * 同步免打扰状态模块
 *
 * @Author:jiangft
 * @Since:2016年8月26日
 */
@Scope("prototype")
@Service
public class NoDisturbService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NoDisturbService.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ImGroupInterface imGroupInterface;

    @Autowired
    private ImMessageInterface imMessageInterface;

    @Autowired
    private ImSquareInterface imSquareInterface;

    @Autowired
    private NewsSyncInterface newsSyncInterface;
    
    @Autowired
    private CircleInterface circleInterface;
    
   
    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.noDisturb_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id();
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("同步免打扰状态、单聊、群聊模块(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        case FunctionIdConstant.NODISTURB:
                            resInfo = getNoDisturb(request_body, user_id);
                            break;
                        case FunctionIdConstant.SINGLECHAT:
                            resInfo = getSingleChat(request_body, user_id);
                            break;
                        case FunctionIdConstant.GROUPCHAT:
                            resInfo = getGroupChat(request_body, user_id);
                            break;
                        case FunctionIdConstant.FWHHISTORY:
                            resInfo = getFwhHistory(request_body, user_id);
                            break;
                        // 解密上传参数进行验证
                        case FunctionIdConstant.DECRYPT:
                            resInfo = decryptASE(request_body, user_id);
                            break;
                        // 获取新闻列表
                        case FunctionIdConstant.GETNEWSLIST:
                            resInfo = getNewsList(request_body, user_id);
                            break;
                        // 获取新闻详情
                        case FunctionIdConstant.GETNEWSINFO:
                            resInfo = getNewsInfo(request_body, user_id);
                            break;
                        //获取圈子里面的人员信息    
                        case FunctionIdConstant.CIRCLEMEMBERLIST:
                            resInfo = getCircleMemberList(request_body, user_id);
                            break;
                        //查询服务号免打扰信息 
                        case FunctionIdConstant.NODISTURB_SERVICE:
                            resInfo = getServiceNoDisturb(request_body, user_id);
                            break;
                        default:
                            break;
                    }
                    logger.debug("同步免打扰状态、单聊、群聊模块(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponseH5(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("同步免打扰状态、单聊、群聊模块异常", e);
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
     * 获取新闻列表
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getNewsList(String requestBody, String userId) {
        logger.debug("获取新闻列表，requestBody{},userId{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String pageNow = requestJson.getString("pageNow");
        String pageSize = requestJson.getString("pageSize");
        if (StringUtils.isEmpty(pageNow)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL92011, "当前页为空");
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = "10";
        }
        try {
            Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
            sortMap.put("pushTime", false);
            Map<String, Object> result = newsSyncInterface.findNewsByPage(Integer.valueOf(pageNow), Integer.valueOf(pageSize), new HashMap<String, Object>(), sortMap);
            logger.debug("获取新闻类别信息,result{}", JSON.toJSONString(result));
            return ResponsePackUtil.buildPack("0000", result);
        } catch (Exception e) {
            logger.error("获取新闻列表异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL92012, "获取新闻列表异常");
        }

    }

    /**
     * 获取新闻详情
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getNewsInfo(String requestBody, String userId) {
        logger.debug("获取新闻详情，requestBody{},userId{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String newsId = requestJson.getString("newsId");
        if (StringUtils.isEmpty(newsId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL92021, "新闻ID为空");
        }
        NewsSyncVo news = null;
        try {
            news = newsSyncInterface.findNewsSyncVoByNewsId(newsId);
            logger.debug("获取新闻详情,news{}", JSON.toJSONString(news));
            if(null==news){
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL92022, "获取新闻详情失败");
            }
        } catch (Exception e) {
            logger.error("获取新闻详情异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL92023, "获取新闻详情异常");
        }
        return ResponsePackUtil.buildPack("0000", news);
    }

    /**
     * 解密传入的参数
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String decryptASE(String requestBody, String userId) {
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String memId = requestJson.getString("userId");
        String aseDecrypt = requestJson.getString("aseDecrypt");
        String request_body_decryption = "";

        /** 校验参数 */
        try {
            request_body_decryption = AESUtil.decode(memId, toStringHex(aseDecrypt));
        } catch (Exception e) {
            logger.error("AES解密异常,user_id:{},request_body:{},key:{}", memId, aseDecrypt, memId, e);
        }
        String startKey = "";
        String taskId = "";
        String timestamp = "";
        String sendRoleId = "";
        String type = "";
        if (!"".equals(request_body_decryption)) {
            JSONObject json = JSONObject.parseObject(request_body_decryption);
            taskId = json.getString("taskId");
            timestamp = json.getString("timestamp");
            sendRoleId = json.getString("targetUserId");
            type = json.getString("type");
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("memId", memId);
        model.put("startKey", startKey);
        model.put("taskId", taskId);
        model.put("timestamp", timestamp);
        model.put("sendRoleId", sendRoleId);
        model.put("type", type);
        try {
            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.error("解密传入的参数：e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4005, "");
        }
    }

    /** 16进制转换成字符串 */
    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
            }
        }
        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
        }
        return s;
    }

    /**
     * 同步免打扰状态模块
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getNoDisturb(String requestBody, String userId) {

        logger.debug("同步免打扰状态模块（入口）,requestBody:{},userId:{}", requestBody, userId);
        /** 校验参数 */
        if (StringUtils.isEmpty(userId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        try {
            Set<String> set = imGroupInterface.getNoDisturbGroupIdsByUserId(userId);
            return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(JSONObject.toJSONString(set), userId));
        } catch (Exception e) {
            logger.error("同步免打扰状态服务异常：e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4005, "");
        }
    }
    

    /**
     * 同步服务号免打扰状态模块
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getServiceNoDisturb(String requestBody, String userId) {

        logger.debug("<服务号>同步免打扰状态模块（入口）,requestBody:{},userId:{}", requestBody, userId);
        /** 校验参数 */
        if (StringUtils.isEmpty(userId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        try {
            Set<String> set = imSquareInterface.getNoDisturbServiceIdsByUserId(userId);
            return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(JSONObject.toJSONString(set), userId));
        } catch (Exception e) {
            logger.error("<服务号>同步免打扰状态服务异常：e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4005, "");
        }
    }

    /**
     * 单聊消息查询模块
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getSingleChat(String requestBody, String userId) {
        logger.debug("单聊消息查询模块（入口）,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sendRoleId = requestJson.getString("sendRoleId");
        String memId = requestJson.getString("memId");
        String startKey = requestJson.getString("startKey");
        /** 校验参数 */
        if (StringUtils.isEmpty(memId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        if (StringUtils.isEmpty(sendRoleId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        try {
            Map<String, Object> map = imMessageInterface.getSingleMessageSendHistroy(memId, sendRoleId, startKey);
            return ResponsePackUtil.buildPack("0000", map);
        } catch (Exception e) {
            logger.error("单聊消息查询服务异常：e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4006, "");
        }
    }

    /**
     * 群聊消息查询模块
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getGroupChat(String requestBody, String userId) {
        logger.debug("群聊消息查询模块（入口）,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String groupId = requestJson.getString("groupId");
        String sendRoleId = requestJson.getString("sendRoleId");
        String startKey = requestJson.getString("startKey");
        /** 校验参数 */
        if (StringUtils.isEmpty(groupId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        if (StringUtils.isEmpty(sendRoleId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        try {
            Map<String, Object> map = imMessageInterface.getGroupMessageSendHistroy(groupId, sendRoleId, startKey);
            return ResponsePackUtil.buildPack("0000", map);
        } catch (Exception e) {
            logger.error("群聊消息查询服务异常：e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4007, "");
        }
    }

    /**
     * 服务号消息查询模块
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getFwhHistory(String requestBody, String userId) {
        logger.debug("服务号消息查询模块,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String memId = requestJson.getString("memId");
        String serviceId = requestJson.getString("serviceId");
        String lastStartKey = requestJson.getString("lastStartKey");
        /** 校验参数 */
        if (StringUtils.isEmpty(memId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        if (StringUtils.isEmpty(serviceId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        try {
            Map<String, Object> map = imSquareInterface.findSquareMessageByUserId(memId, serviceId, lastStartKey);
            return ResponsePackUtil.buildPack("0000", map);
        } catch (Exception e) {
            logger.error("群聊消息查询服务异常：e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4007, "");
        }
    }
    
    /**
     * 分页获取圈子人员
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getCircleMemberList(String requestBody, String userId) {
        logger.debug("获取圈子人员，requestBody{},userId{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String circleId =requestJson.getString("circleId");
        String page =requestJson.getString("page");
        String row =requestJson.getString("row");
        int pageIndex = 1;
        int pageSize = 10;
        try {
            if (!"".equals(page))
                pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL92011, "当前页为空");
        }
        try {
            if (!"".equals(pageSize))
                pageSize = Integer.parseInt(row);
        } catch (Exception e) {
        }
        
        try {
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_circleId", circleId);
            
            Map<String, Object> result =circleInterface.findMemberByPage(pageIndex, pageSize, conditions, null);
            logger.debug("获取圈子人员,result{}", JSON.toJSONString(result));
            return ResponsePackUtil.buildPack("0000", result);
        } catch (Exception e) {
            logger.error("获取圈子人员异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL92012, "获取圈子人员异常");
        }

    }
 
}
