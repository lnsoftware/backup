/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.GetParamsForUrlUtils;
import com.royasoft.vwt.controller.util.ResponsePackUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Http服务器handler,只做业务分拣，具体业务交由业务异步services处理
 * 
 * @author jxue
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    /**
     * 获取到客户端请求，进行业务拆分
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {

            if (msg instanceof HttpRequest) {
                logger.debug("入口msg:{}", msg);
                HttpRequest request = (HttpRequest) msg;
                String uri = request.getUri();
                logger.debug("获取到客户端请求，进行业务拆分,请求地址:{}", uri);
                if (uri.equals("/favicon.ico")) {
                    ResponsePackUtil.CalibrationParametersFailure(ctx);
                } else if (uri.indexOf("/controller") < 0 && uri.indexOf("upload") < 0) { // 如果不是/controller地址请求，则不处理
                    ResponsePackUtil.CalibrationParametersFailure(ctx, "uri:" + uri + ";不是/controller地址请求");
                } else {
                    String function_id = "";
                    if (uri.indexOf("uploadtw") > 0) {
                        function_id = "2111";
                        ServicesQueue.vote_queue.put(new QueuePacket(msg, ctx.channel(), function_id, "", "", ""));
                    } else if (uri.indexOf("baiduUpload") > 0) {
                        function_id = uri.substring(uri.lastIndexOf("/") + 1);// 获取功能ID
                        // function_id = "9311";
                        // 专供百度富文本编辑器上传初始化用
                        String callback = "";
                        JSONObject paramJson = new JSONObject();
                        if (function_id.indexOf("?") > 0) {
                            String functions[] = function_id.split("\\?");
                            function_id = functions[0];
                            callback = functions[1];
                            callback = callback.substring(callback.lastIndexOf("=") + 1);

                            if (callback.contains("image")) {
                                paramJson.put("image", callback);
                            } else {
                                paramJson.put("callback", callback);
                            }
                        }
                        ServicesQueue.baiduUpload_queue.put(new QueuePacket(msg, ctx.channel(), function_id, "", paramJson.toJSONString(), ""));
                    } else if (uri.indexOf("uploadfm") > 0) {
                        function_id = "2105";
                        ServicesQueue.vote_queue.put(new QueuePacket(msg, ctx.channel(), function_id, "", "", ""));
                    } else {
                        JSONObject paramJson = GetParamsForUrlUtils.getHttpJsonParams(msg);

                        logger.debug("获取到客户端请求，进行业务拆分(获取参数),paramJSON:{}", null != paramJson ? paramJson.toJSONString() : "");
                        /*
                         * if (null == paramJson || paramJson.isEmpty()) { ResponsePackUtil.CalibrationParametersFailure(ctx, "获取参数失败！"); return; }
                         */

                        if (null == paramJson) {
                            paramJson = new JSONObject();
                        }
                        function_id = uri.substring(uri.lastIndexOf("/") + 1);// 获取功能ID
                        String user_id = paramJson.getString("user_id");// 获取用户id
                        String tel_number = paramJson.getString("tel_number");// 获取用户手机号
                        logger.debug("请求参数,function_id:{},user_id:{},tel_number:{}", function_id, user_id, tel_number);

                        if (StringUtils.isEmpty(function_id))
                            ResponsePackUtil.cagHttpResponse(ctx.channel(), ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, ""));

                        if (StringUtils.isEmpty(function_id))
                            ResponsePackUtil.cagHttpResponse(ctx.channel(), ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, ""));

                        if (FunctionIdConstant.materialCenterFunctionIdList.contains(function_id)) {
                            ServicesQueue.materialCenter_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.userFeedbackUrlFunctionIdList.contains(function_id)) {
                            ServicesQueue.useFeedback_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.voteFunctionIdList.contains(function_id)) {
                            ServicesQueue.vote_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.internetAuthFunctionIdList.contains(function_id)) {
                            ServicesQueue.internetAuth_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.addressFunctionIdList.contains(function_id)) {
                            ServicesQueue.address_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.redisManagerFunctionIdList.contains(function_id)) {
                            ServicesQueue.redis_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.graphicSourceFunctionIdList.contains(function_id)) {
                            ServicesQueue.graphicSource_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.announceFunctionIdList.contains(function_id)) {
                            ServicesQueue.announce_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.workTeamFunctionIdList.contains(function_id)) {
                            ServicesQueue.workteam_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.twoLearnFunctionIdList.contains(function_id)) {
                            ServicesQueue.twoLearn_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.circleFunctionIdList.contains(function_id)) {
                            ServicesQueue.circle_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.keyWordFunctionIdList.contains(function_id)) {
                            ServicesQueue.keyWords_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.squareMessageFunctionIdList.contains(function_id)) {
                            ServicesQueue.squareMessage_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.imGroupFunctionIdList.contains(function_id)) {
                            ServicesQueue.imGroup_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.festivalList.contains(function_id)) {
                            ServicesQueue.festival_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.squeareFeedbackList.contains(function_id)) {
                            ServicesQueue.sqfeedback_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.exportFunctionIdList.contains(function_id)) {
                        	ServicesQueue.export_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.deptManagerFunctionIdList.contains(function_id)) {
                            ServicesQueue.deptManager_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.urlmanageFunctionIdList.contains(function_id)) {
                        	ServicesQueue.urlmanage_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else if (FunctionIdConstant.insidePurchFunctionIdList.contains(function_id)) {
                        	ServicesQueue.inside_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                        } else {
                            ResponsePackUtil.CalibrationParametersFailure(ctx, "请求地址不存在！");
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger.error("请求异常", e);
            ResponsePackUtil.CalibrationParametersFailure(ctx, "请求异常");
        }
    }
}
