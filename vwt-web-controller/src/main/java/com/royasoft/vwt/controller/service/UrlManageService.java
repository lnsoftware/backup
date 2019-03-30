/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

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
import com.royasoft.vwt.controller.util.BaseConstant;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.StringUtils;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.urlmanage.api.interfaces.UrlManageInterface;
import com.royasoft.vwt.soa.business.urlmanage.api.vo.UrlManageVo;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 二维码url业务处理类
 *
 * @Author:huangtao
 */
@Scope("prototype")
@Service
public class UrlManageService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(UrlManageService.class);

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
    private UrlManageInterface urlmanageInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.urlmanage_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String tel_number = queue_packet.getTel_number();
                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    /***************************** 业务逻辑处理 *********************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        // 保存二维码url
                        case FunctionIdConstant.URLMANAGESAVE:
                            resInfo = saveUrlManage(request_body);
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
                    // 响应成功
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("二维码url业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
                channel.close();
            }
        }
    }
    
   
    
    
    /**
     * 保存二维码url
     * 
     * @return
     */
    public String saveUrlManage(String requestBody) {
        logger.debug("保存二维码url,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = trim(requestJson.getString("sessionid"));
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpId = "";
         try {
         sessionJson = JSONObject.parseObject(session);
         corpId = sessionJson.getString("corpId");
         logger.debug("corpId:{}", corpId);
         } catch (Exception e) {
         logger.error("获取session--------->", session);
         logger.error("获取corpid报错", e);
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9904, "");
         }
        String url = trim(requestJson.getString("url"));
        UrlManageVo urlManageVo = new UrlManageVo();
        urlManageVo.setCorpId(corpId);
        urlManageVo.setUrl(url);
        try {
        	UrlManageVo vo= urlmanageInterface.saveUrlManage(urlManageVo);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, vo);
        } catch (Exception e) {
            logger.error("保存二维码url调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
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
}
