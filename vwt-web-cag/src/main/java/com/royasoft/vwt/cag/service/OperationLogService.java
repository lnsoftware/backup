/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.util.mq.LogRocketMqUtil;
import com.royasoft.vwt.cag.vo.OperationLogVO;

/**
 * 操作日志处理类
 *
 * @Author:ZHOUKQ
 * @Since:2016年3月1日
 */
@Scope("prototype")
@Component
public class OperationLogService {

    private final Logger logger = LoggerFactory.getLogger(OperationLogService.class);

    /**
     * 操作日志入MQ
     * 
     * @param ctx
     * @param request
     * @param userId
     * @param telNum
     * @param functionId
     * @param requestMsg
     * @param remark
     * @param responseStatus
     * @Description:
     */
    public void saveOperationLogNew(Channel ctx, HttpRequest request, String userId, String telNum, String functionId, String requestMsg, String remark, String responseStatus) {
        logger.debug("操作日志入MQ(参数),userId:{},telNum:{},functionId:{},requestMsg:{},remark:{},responseStatus:{}", userId, telNum, functionId, requestMsg, remark, responseStatus);
        if ((null == userId || "".equals(userId)) && (null == telNum || "".equals(telNum)))
            return;
        OperationLogVO operationLogVO = new OperationLogVO();
        operationLogVO.setUserId(userId);
        operationLogVO.setMobile(telNum);
        operationLogVO.setOperationTime(System.currentTimeMillis());
        operationLogVO.setUuid(UUID.randomUUID().toString());
        operationLogVO.setIp(getIP(ctx, request));
        String optCode = FunctionIdConstant.functionOptCodeMap.get(functionId);
        if (null == optCode || "".equals(optCode) || optCode.length() != 8)
            return;
        operationLogVO.setOperation(FunctionIdConstant.functionOptCodeMap.get(functionId));
        operationLogVO.setModel(FunctionIdConstant.functionOptCodeMap.get(functionId).substring(0, 5));
        operationLogVO.setRemark(remark);
        operationLogVO.setRequestMsg(requestMsg);
        operationLogVO.setResponseStatus(responseStatus);

        logger.debug("操作日志入MQ(完成),userId:{},telNum:{},functionId:{},requestMsg:{},remark:{},responseStatus:{}", userId, telNum, functionId, requestMsg, remark, responseStatus);
        LogRocketMqUtil.send(LogRocketMqUtil.logManageQueue, JSONObject.toJSONString(operationLogVO));
    }

    /**
     * 操作日志入MQ
     * 
     * @param ctx
     * @param request
     * @param userId
     * @param telNum
     * @param functionId
     * @param requestMsg
     * @param remark
     * @param responseStatus
     * @Description:
     */
    public void saveClientOperationLog(Channel ctx, HttpRequest request, String userId, String telNum, String operation, String requestMsg, String remark, String responseStatus) {
        logger.debug("操作日志入MQ(参数),userId:{},telNum:{},operation:{},requestMsg:{},remark:{},responseStatus:{}", userId, telNum, operation, requestMsg, remark, responseStatus);
        if ((null == userId || "".equals(userId)) && (null == telNum || "".equals(telNum)))
            return;
        OperationLogVO operationLogVO = new OperationLogVO();
        operationLogVO.setUserId(userId);
        operationLogVO.setMobile(telNum);
        operationLogVO.setOperationTime(System.currentTimeMillis());
        operationLogVO.setUuid(UUID.randomUUID().toString());
        operationLogVO.setIp(getIP(ctx, request));
        operationLogVO.setOperation(operation);
        operationLogVO.setModel(operation.substring(0, 5));
        operationLogVO.setRemark(remark);
        operationLogVO.setRequestMsg(requestMsg);
        operationLogVO.setResponseStatus(responseStatus);

        LogRocketMqUtil.send(LogRocketMqUtil.logManageQueue, JSONObject.toJSONString(operationLogVO));
    }

    /**
     * 获取请求客户端ip
     * 
     * @param ctx
     * @param request
     * @return
     * @Description:
     */
    private String getIP(Channel ctx, HttpRequest request) {
        try {
            String clientIP = request.headers().get("X-Forwarded-For");
            if (clientIP == null) {
                InetSocketAddress insocket = (InetSocketAddress) ctx.remoteAddress();
                clientIP = insocket.getAddress().getHostAddress();
            }
            logger.debug("获取请求客户端ip,clientIP:{}", clientIP);
            return clientIP;
        } catch (Exception e) {
            return "";
        }

    }
}
