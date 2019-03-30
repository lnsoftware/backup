/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.ResponsePackUtil;

/**
 * 登陆鉴权类
 *
 * @Author:MB
 * @Since:2015年11月19日
 */
@Scope("prototype")
@Service
public class LoginAuthService implements Runnable {

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    private final Logger logger = LoggerFactory.getLogger(LoginAuthService.class);

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.loginAuth_queue.take();// 获取队列处理数据
                long t1 = System.currentTimeMillis();
                logger.info("==============开始时间:{}", t1);
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    logger.debug("登陆鉴权处理类(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /***************************** 业务逻辑处理 *********************************************/

                    String resInfo = ResponsePackUtil.buildPack("code", "");// 响应结果
                    logger.debug("登陆鉴权处理类(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);

                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("登陆鉴权业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {
                // channel.close();
            }
        }
    }
}
