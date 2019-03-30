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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.soa.uic.oaaccount.api.Vo.OAaccountInfoVo;
import com.royasoft.vwt.soa.uic.oaaccount.api.interfaces.OAaccountInfoInterface;

/**
 * 获取OA账号
 * 
 * @author ZHOUKQ
 *
 */
@Scope("prototype")
@Service
public class OAaccountService implements Runnable{
    
    private static final Logger logger = LoggerFactory.getLogger(OAaccountService.class);
   
    @Autowired
    private OAaccountInfoInterface oAaccountInfoInterface;//OA账号服务接口
    
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
                queue_packet = ServicesQueue.oaaccount_queue.take();// 获取队列处理数据
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
                        case FunctionIdConstant.GETOAACCOUNTINFO:
                            res = getOAaccountInfo(request_body, user_id);
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
     * 根据人员ID获取OA账号信息
     * @param request_body
     * @param user_id
     * @return
     */
    public String getOAaccountInfo(String request_body,String user_id){
        logger.debug("获取OA账号信息，request_body{},user_id{}",request_body,user_id);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String memId=requestJson.getString("memId");
        logger.debug("获取OA账号信息(解析body),memId{}",memId);
        if(!StringUtils.stringIsNotNull(memId)){
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1007, "");
        }
        try {
            OAaccountInfoVo oaVo=  oAaccountInfoInterface.findByMemId(memId);
            logger.debug("获取OA账号信息,oaVo{}",null==oaVo?"null":JSON.toJSONString(oaVo));
            String oaaccount="";
            if(null!=oaVo){
                oaaccount=oaVo.getOaaccount();
            }
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("OAaccount", oaaccount);
            /** 加密返回body */
            String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), user_id);
            return ResponsePackUtil.buildPack("0000", resBody);
        } catch (Exception e) {
           logger.error("获取OA账号信息异常",e);
           return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4002, "");
        }
    }
    
}
