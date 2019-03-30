/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.soa.business.im.api.interfaces.ImSquareInterface;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 服务号数据查询
 *
 * @Author:jiangft
 * @Since:2016年8月25日
 */
@Scope("prototype")
@Service
public class SquareMessage implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SquareMessage.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private ImSquareInterface imSquareInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.squareMessage_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id();
                    String user_id = ""; // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("服务号数据查询(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        case FunctionIdConstant.SQUAREMESSAGE:
                            resInfo = getSquareMessage(request, request_body, user_id, tel_number);
                            break;
                        default:
                            break;
                    }
                    logger.debug("服务号数据查询(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Throwable e) {
                logger.error("服务号数据查询异常", e);
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {

            }

        }
    }

    /**
     * 服务号数据查询
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getSquareMessage(HttpRequest request, String requestBody, String userId, String telNum) {
        logger.debug("服务号数据查询（入口）,requestBody:{},userId:{}", requestBody, userId);

        try {
            if (StringUtils.isEmpty(requestBody)) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, "");
            }
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String serviceId = requestJson.getString("serviceId");
            String lastStartKey = requestJson.getString("lastStartKey");
            logger.debug("serviceId：{},lastStartKey：{}", serviceId, lastStartKey);
            if (StringUtils.isEmpty(serviceId) || StringUtils.isEmpty(lastStartKey)) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, "");
            }
            Map<String, Object> map = imSquareInterface.findSquareMessageByUserId(userId, serviceId, lastStartKey);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, map);
        } catch (Exception e) {
            logger.error("服务号数据查询异常e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");

        }

    }

}
