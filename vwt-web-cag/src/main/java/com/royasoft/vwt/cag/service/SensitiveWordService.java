/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ParaUtil;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.soa.business.sensitivewords.api.interfaces.SensitiveWordInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;

/**
 * 同步敏感词
 * 
 * @author ZHOUKQ
 *
 */
@Scope("prototype")
@Service
public class SensitiveWordService implements Runnable{
    
    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordService.class);
    @Autowired
    private SensitiveWordInterface sensitiveWordInterface;
    
    @Autowired
    private ClientUserInterface clientUserInterface;
    
    @Autowired
    private OperationLogService operationLogService;//操作日志接口

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;
    
    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.sensitiveword_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String tel_number = queue_packet.getTel_number();
                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    /***************************** 业务逻辑处理 *********************************************/

                    String res = "";// 响应结果
                    switch (function_id) {
                        case FunctionIdConstant.GETSENSITIVEWORD:
                            res = getSensitiveWord(user_id);
                            break;
                        default:
                            res = ResponsePackUtil.returnFaileInfo(); // 未知请求
                            break;
                    }
                    // 响应成功
                    ResponsePackUtil.responseStatusOK(channel, res);
                    String responseStatus = ResponsePackUtil.getResCode(res);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                }
            } catch (Exception e) {
                logger.error("任务业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }
        }
        
    }
    /**
     * 同步敏感词
     * @param request_body
     * @param user_id
     * @return
     */
    public String getSensitiveWord(String user_id){
        logger.debug("同步敏感词信息，user_id{}",user_id);
        try {
            //验证当前用户
            ClientUserVO clientUserVO = clientUserInterface.findById(user_id);
            if (null == clientUserVO){
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");
            }
            String sensitiveword=sensitiveWordInterface.findSensitiveWord("c");
            logger.debug("获取敏感词，sensitiveword{}",sensitiveword);
            JSONObject json=new JSONObject();
            json.put("sensitiveword", sensitiveword);
            json.put("code", ParaUtil.SUCC_CODE);
            return json.toString();
        } catch (Exception e) {
            logger.error("获取敏感词异常",e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4003, "");
        }
    
    }
}
