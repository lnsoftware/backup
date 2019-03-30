/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.soa.twolearn.api.interfaces.TwoLearnInterface;

/**
 * 美丽江苏业务模块
 *
 * @Author:wuyf
 * @Since:2016年5月20日
 */
@Scope("prototype")
@Service
public class BeautyJSService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(BeautyJSService.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private TwoLearnInterface twoLearnInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.beautyJS_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id();
                    String user_id = ""; // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("投票业务模块(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        case FunctionIdConstant.BEAUTYJSPRAISE:
                            resInfo = beautyJSPraise(request_body, user_id);
                            break;
                        case FunctionIdConstant.BEAUTYJSDISCUSS:
                            resInfo = beautyJSDiscuss(request_body, user_id);
                            break;
                        case FunctionIdConstant.GETDISCUSSANDPRAISE:
                            resInfo = getDiscussAndPraise(request_body, user_id);
                            break;
                        case FunctionIdConstant.CANCELBEAUTYJSPRAISE:
                            resInfo = cancelBeautyJSPraise(request_body, user_id);
                            break;
                        case FunctionIdConstant.GETBEAUTYJSLIST:
                            resInfo = getbeautyJSList(request_body, user_id);
                            break;
                        case FunctionIdConstant.BEAUTYJSENTERDETAIL:
                            resInfo = beautyJSEnterDetail(request_body, user_id);
                            break;
                        default:
                            break;
                    }
                    logger.debug("美丽江苏业务模块(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponseH5(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("美丽江苏业务模块异常", e);
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
     * 美丽江苏点赞保存
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String beautyJSPraise(String requestBody, String userId) {
        logger.debug("美丽江苏点赞保存,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String personId = trim(requestJson.getString("personId"));
        String videoId = trim(requestJson.getString("videoId"));
        logger.debug("美丽江苏点赞保存(解析body),personId:{},videoId:{}", personId, videoId);
        /** 校验参数 */
        if ("".equals(videoId) || "".equals(personId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1101, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(twoLearnInterface.beautyPraise(personId, videoId), ser);
        } catch (Exception e) {
            logger.error("美丽江苏点赞保存调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1102, "");
        }
    }

    /**
     * 评论保存
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String beautyJSDiscuss(String requestBody, String userId) {
        logger.debug("美丽江苏评论保存,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String personId = trim(requestJson.getString("personId"));
        String videoId = trim(requestJson.getString("videoId"));
        String content = trim(requestJson.getString("content"));
        logger.debug("美丽江苏评论保存(解析body),personId:{},videoId:{},content:{}", personId, videoId, content);
        /** 校验参数 */
        if ("".equals(videoId) || "".equals(personId) || "".equals(content))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1101, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(twoLearnInterface.beautyDiscuss(personId, videoId, content), ser);
        } catch (Exception e) {
            logger.error("美丽江苏评论保存调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1102, "");
        }
    }

    /**
     * trim
     * 
     * @param obj
     * @return
     * @author Jiangft 2016年5月19日
     */
    public static String trim(Object obj) {
        return (obj == null) ? "" : obj.toString().trim();
    }

    /**
     * 展示评论和点赞
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getDiscussAndPraise(String requestBody, String userId) {
        logger.debug("美丽江苏展示评论和点赞,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String personId = trim(requestJson.getString("personId"));
        String videoId = trim(requestJson.getString("videoId"));
        String refreshFlag = trim(requestJson.getString("refreshFlag"));
        String sort = trim(requestJson.getString("sort"));
        String row = trim(requestJson.getString("row"));
        logger.debug("美丽江苏展示评论和点赞(解析body),personId:{},videoId:{},refreshFlag:{},sort:{},row:{}", refreshFlag, videoId, personId, sort, row);
        /** 校验参数 */
        if ("".equals(videoId) || "".equals(personId) || "".equals(refreshFlag) || "".equals(sort))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1101, "");
        int pageSize = 10;
        int flag = 1;
        long flagSort = 0L;
        try {
            flag = Integer.parseInt(refreshFlag);
            flagSort = Long.parseLong(sort);
            pageSize = Integer.parseInt(row);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1101, "");
        }
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack(twoLearnInterface.getdiscusse(personId, videoId, flag, pageSize, flagSort), ser);
        } catch (Exception e) {
            logger.error("美丽江苏展示评论和点赞调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1102, "");
        }
    }

    /**
     * 取消点赞
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String cancelBeautyJSPraise(String requestBody, String userId) {
        logger.debug("美丽江苏取消点赞,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String personId = trim(requestJson.getString("personId"));
        String videoId = trim(requestJson.getString("videoId"));
        logger.debug("美丽江苏取消点赞(解析body),personId:{},videoId:{}", personId, videoId);
        /** 校验参数 */
        if ("".equals(videoId) || "".equals(personId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1101, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(twoLearnInterface.cancelBeautyJSPraise(personId, videoId), ser);
        } catch (Exception e) {
            logger.error("美丽江苏取消点赞调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1102, "");
        }
    }

    /**
     * 美丽江苏列表展示
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getbeautyJSList(String requestBody, String userId) {
        logger.debug("美丽江苏列表展示,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String personId = trim(requestJson.getString("personId"));
        logger.debug("美丽江苏列表展示(解析body),personId:{}", personId);
        /** 校验参数 */
        if ("".equals(personId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1101, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(twoLearnInterface.getBeautyJSlist(personId), ser);
        } catch (Exception e) {
            logger.error("美丽江苏列表展示调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1102, "");
        }
    }

    /**
     * 美丽江苏——进入详情界面(增加一次观看次数+获取点赞状态)
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String beautyJSEnterDetail(String requestBody, String userId) {
        logger.debug("美丽江苏进入详情界面,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String personId = trim(requestJson.getString("personId"));
        String videoId = trim(requestJson.getString("videoId"));
        logger.debug("美丽江苏进入详情界面(解析body),personId:{},videoId:{}", personId, videoId);
        /** 校验参数 */
        if ("".equals(videoId) || "".equals(personId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1101, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(twoLearnInterface.enterDetail(videoId,personId), ser);
        } catch (Exception e) {
            logger.error("美丽江苏进入详情界面调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1102, "");
        }
    }
}
