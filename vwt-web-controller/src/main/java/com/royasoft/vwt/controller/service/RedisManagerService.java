/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.StringUtils;
import com.royasoft.vwt.soa.base.redis.api.interfaces.ImRedisInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;

/**
 * Redis管理业务处理类
 *
 * @Author:MQS
 * @Since:2016年5月23日
 */
@Scope("prototype")
@Service
public class RedisManagerService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RedisManagerService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private ImRedisInterface imRedisInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.redis_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    // String tel_number = queue_packet.getTel_number();
                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    /***************************** 业务逻辑处理 *********************************************/

                    String res = "";// 响应结果

                    if (function_id == null || function_id.length() <= 0 || request_body == null || request_body.length() <= 0) { // || user_id ==
                                                                                                                                  // null ||
                                                                                                                                  // user_id.length()
                                                                                                                                  // <= 0 ||
                        ResponsePackUtil.CalibrationParametersFailure(channel, "素材中心业务请求参数校验失败！");
                    } else {
                        // 素材中心具体业务分层跳转
                        res = RedisManagerBusinessLayer(channel, request, function_id, user_id, request_body, msg);
                    }
                    // 响应成功
                    ResponsePackUtil.responseStatusOK(channel, res);
                }
            } catch (Exception e) {
                logger.error("素材中心业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
                channel.close();
            }
        }
    }

    /**
     * Redis管理功能分块请求
     * 
     * @param function_id
     * @param user_id
     * @param request_body
     * @param msg
     * @return
     */
    private String RedisManagerBusinessLayer(Channel channel, HttpRequest request, String function_id, String user_id, String request_body, Object msg) {
        String res = "";
        switch (function_id) {
            case FunctionIdConstant.REDIS_MANAGE_QUERY:// Redis查询key
                res = getValueByKey(request_body);
                break;
            case FunctionIdConstant.REDIS_MANAGE_DELETE:// Redis删除key
                res = deleteRedisKey(request_body);
                break;
            default:
                res = ResponsePackUtil.returnFaileInfo(); // 未知请求
        }
        return res;
    }

    /**
     * Redis根据key查询值
     * 
     * @param requestBody
     * @return
     */
    private String getValueByKey(String requestBody) {
        logger.debug("Redis根据key查询值,requestBody:{}", requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            if (null == requestJson) {
                logger.debug("JSON格式异常,requestBody{}", requestBody);
                model.put("result", 400);
                model.put("resultMsg", "Redis根据key查询值失败");
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, model);
            }

            // key
            String key = requestJson.getString("queryKey");

            // redisType
            String redisType = requestJson.getString("redisType");

            // dataType
            String dataType = requestJson.getString("dataType");

            return redisGetValueByKey(redisType, key, dataType);
        } catch (Exception e) {
            logger.error("Redis根据key查询值异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 根据key查询值
     * 
     * @param redisType 0-redis,1-ImRedis
     * @param key
     * @param dataType key的数据类型 0-String类型，1-List类型，2-Set类型，3-Set Sort类型,4-Hash类型
     * @return
     */
    private String redisGetValueByKey(String redisType, String key, String dataType) {
        logger.debug("Redis根据key查询值,redisType:{},key:{},dataType:{}", redisType, key, dataType);
        Map<String, Object> model = new HashMap<String, Object>();
        if (null == key || key.equals("") || null == redisType || redisType.equals("") || null == dataType || dataType.equals("")) {
            logger.debug("Redis根据key查询值参数异常");
            model.put("result", 400);
            model.put("resultMsg", "Redis根据key查询值失败");
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }

        if ("0".equals(dataType)) {
            String str = "";
            if (redisType.equals("0")) {
                str = redisInterface.getString(key);
            } else {
                str = imRedisInterface.getString(key);
            }
            model.put("returnData", str);
        } else if ("1".equals(dataType)) {
            List<String> strList = new ArrayList<String>();
            if (redisType.equals("0")) {
                strList = redisInterface.getList(key);
            } else {
                strList = imRedisInterface.getList(key);
            }
            model.put("returnData", strList);
        } else if ("2".equals(dataType)) {
            Set<String> strSet = new HashSet<String>();
            if (redisType.equals("0")) {
                strSet = redisInterface.getSet(key);
            } else {
                strSet = imRedisInterface.getSet(key);
            }
            model.put("returnData", strSet);
        } else if ("3".equals(dataType)) {
            Set<String> strSet = new HashSet<String>();
            if (redisType.equals("0")) {
                strSet = redisInterface.getSortedSet(key, -1L);
            } else {
                strSet = imRedisInterface.getSortedSet(key, -1L);
            }
            model.put("returnData", strSet);
        } else if ("4".equals(dataType)) {
            String strHash = "";
            String[] strs = key.split(":");
            if (redisType.equals("0")) {
                strHash = redisInterface.hashGet(strs[0] + ":", strs[1]);
            } else {
                strHash = imRedisInterface.hashGet(strs[0] + ":", strs[1]);
            }
            model.put("returnData", strHash);
        }

        model.put("resultMsg", "Redis根据key查询值成功");
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
    }

    /**
     * Redis清除key
     * 
     * @param requestBody
     * @return
     */
    private String deleteRedisKey(String requestBody) {
        logger.debug("Redis清除key,requestBody:{}", requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            if (null == requestJson) {
                logger.debug("JSON格式异常,requestBody{}", requestBody);
                model.put("result", 400);
                model.put("resultMsg", "Redis清除key失败");
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, model);
            }

            // key
            String key = requestJson.getString("queryKey");

            // redisType
            String redisType = requestJson.getString("redisType");

            // dataType
            String dataType = requestJson.getString("dataType");

            return deleteKey(redisType, key, dataType);
        } catch (Exception e) {
            logger.error("Redis清除key异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * Redis删除Key
     * 
     * @param redisType 0-redis,1-ImRedis
     * @param key
     * @param dataType key的数据类型 0-String类型，1-List类型，2-Set类型，3-Set Sort类型,4-Hash类型
     * @return
     */
    private String deleteKey(String redisType, String key, String dataType) {
        logger.debug("Redis删除Key,redisType:{},key:{},dataType:{}", redisType, key, dataType);
        Map<String, Object> model = new HashMap<String, Object>();

        if (null == key || key.equals("") || null == redisType || redisType.equals("") || null == dataType || dataType.equals("")) {
            logger.debug("Redis根据Key清除数据异常");
            model.put("result", 400);
            model.put("resultMsg", "Redis根据Key清除数据失败");
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }

        if ("0".equals(dataType)) {
            Long delCount = 0L;
            if (redisType.equals("0")) {
                delCount = redisInterface.del(key);
            } else {
                delCount = imRedisInterface.del(key);
            }
            model.put("returnData", delCount);
        } else if ("1".equals(dataType)) {
            Long delCount = 0L;
            if (redisType.equals("0")) {
                delCount = redisInterface.del(key);
            } else {
                delCount = imRedisInterface.del(key);
            }
            model.put("returnData", delCount);
        } else if ("2".equals(dataType)) {
            Long delCount = 0L;
            if (redisType.equals("0")) {
                delCount = redisInterface.del(key);
            } else {
                delCount = imRedisInterface.del(key);
            }
            model.put("returnData", delCount);
        } else if ("3".equals(dataType)) {
            Long delCount = 0L;
            if (redisType.equals("0")) {
                delCount = redisInterface.del(key);
            } else {
                delCount = imRedisInterface.del(key);
            }
            model.put("returnData", delCount);
        } else if ("4".equals(dataType)) {
            boolean flag = false;
            String key1 = key.substring(0, key.lastIndexOf(":") + 1);
            String key2 = key.substring(key.lastIndexOf(":") + 1, key.length());

            if (redisType.equals("0")) {
                flag = redisInterface.hDel(key1, key2);
            } else {
                flag = imRedisInterface.hDel(key1, key2);
            }
            model.put("returnData", flag);
        }

        model.put("resultMsg", "Redis删除Key成功");
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
    }
}
