/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.service.CloudDealService;
import com.royasoft.vwt.cag.service.IntegralSignService;
import com.royasoft.vwt.cag.util.GetParamsForUrlUtils;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.common.security.AESUtil;
import com.royasoft.vwt.common.security.RSAUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;

/**
 * Http服务器handler,只做业务分拣，具体业务交由业务异步services处理
 * 
 * @author jxue
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    public HttpServerHandler() {
    }

    /**
     * 获取到客户端请求，进行业务拆分
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                
                if(request.getMethod() == HttpMethod.TRACE){
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.wrappedBuffer("".getBytes()));
                    response.headers().set("Content-Type", "text/plain");
                    response.headers().set("Content-Length", response.content().readableBytes());
                    ctx.writeAndFlush(response);
                    ctx.close();
                    return;
                }
                
                String uri = request.getUri();
                logger.debug("获取到客户端请求，进行业务拆分,请求地址:{}", uri);

                if (uri.equals("/favicon.ico")) {
                    ResponsePackUtil.CalibrationParametersFailure(ctx);
                    ReferenceCountUtil.release(msg);
                } else if (uri.indexOf("/cag") < 0 && uri.indexOf("/vgp") < 0) { // 如果不是/cag地址请求，则不处理
                    ResponsePackUtil.CalibrationParametersFailure(ctx, "uri:" + uri + ";不是/cag地址请求");
                    ReferenceCountUtil.release(msg);
                } else if (uri.indexOf("/vgp") >= 0) {
                    Map<String, String> paramMap = GetParamsForUrlUtils.getHttpParamsVGP(request);
                    logger.debug("获取到客户端请求，进行业务拆分(获取参数Map),paramMap:{}", JSON.toJSONString(paramMap));
                    if (null == paramMap || paramMap.size() == 0) {
                        ResponsePackUtil.CalibrationParametersFailure(ctx, "获取参数失败！");
                        ReferenceCountUtil.release(msg);
                        return;
                    }
                    String function_id = paramMap.get("function_id"); // 获取功能ID
                    String request_body = paramMap.get("request_body");// 获取参数实体
                    String tel_number = paramMap.get("tel_number");

                    if (null == function_id || function_id.equals("") || null == request_body)
                        ResponsePackUtil.cagHttpResponse(ctx.channel(), ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, ""));

                    request_body = decodeParamsRSA(tel_number, request_body);

                    if (FunctionIdConstant.versionUrlFunctionIdList.contains(function_id)) {// 版本更新业务模块
                        ServicesQueue.versionVGP_queue.put(new QueuePacket(msg, ctx.channel(), function_id, "", request_body, tel_number));
                    } else {
                        ResponsePackUtil.CalibrationParametersFailure(ctx, "请求地址不存在！");
                        ResponsePackUtil.CalibrationParametersFailure(ctx, "请求地址不存在！");
                    }

                } else if (uri.indexOf("/html5") >= 0) {
                    JSONObject paramJson = GetParamsForUrlUtils.getHttpJsonParams(msg);

                    logger.debug("获取到客户端请求，进行业务拆分(获取参数),paramJSON:{}", null != paramJson ? paramJson.toJSONString() : "");
                    if (null == paramJson || paramJson.isEmpty()) {
                        ResponsePackUtil.CalibrationParametersFailure(ctx, "获取参数失败！");
                        ReferenceCountUtil.release(msg);
                        return;
                    }

                    String function_id = uri.substring(uri.lastIndexOf("/") + 1);// 获取功能ID
                    String user_id = paramJson.getString("user_id");// 获取用户id
                    String tel_number = paramJson.getString("tel_number");// 获取用户手机号
                    logger.debug("请求参数,function_id:{},user_id:{},tel_number:{}", function_id, user_id, tel_number);
                    if (StringUtils.isEmpty(function_id)) {
                        ResponsePackUtil.cagHttpResponse(ctx.channel(), ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, ""));
                        ReferenceCountUtil.release(msg);
                        return;
                    }
                    if (FunctionIdConstant.inviteSystemFunctionIdList.contains(function_id)) {// 邀请体系 业务模块
                        ServicesQueue.inviteSystem_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                    } else if (FunctionIdConstant.voteFunctionIdList.contains(function_id)) {// 投票业务模块
                        ServicesQueue.vote_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                    } else if (FunctionIdConstant.oleIMSFunctionIdList.contains(function_id)) {// IMS通讯录模块
                        ServicesQueue.ims_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                    } else if (FunctionIdConstant.beautyJSFunctionIdList.contains(function_id)) {// 美丽江苏
                        ServicesQueue.beautyJS_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                    } else if (FunctionIdConstant.twoLearnFunctioinIdList.contains(function_id)) {// 二学一做模块
                        ServicesQueue.twoLearn_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                    } else if (FunctionIdConstant.noDisturbList.contains(function_id)) {
                        ServicesQueue.noDisturb_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                    } else if (FunctionIdConstant.shandongOAList.contains(function_id)) {
                        ServicesQueue.shandongoa_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                    } else if (FunctionIdConstant.integralH5FunctionIdList.contains(function_id)) {// 积分H5模块
                        ServicesQueue.integralH5_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                    } else if (FunctionIdConstant.insidePurchFunctionIdList.contains(function_id)) {// 内购模块
                        ServicesQueue.insidePurch_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, paramJson.toJSONString(), tel_number));
                    } 
                    else {
                        ResponsePackUtil.CalibrationParametersFailure(ctx, "请求地址不存在！");
                        ReferenceCountUtil.release(msg);
                    }
                } else {
                    Map<String, String> paramMap = GetParamsForUrlUtils.getHttpParams(request);
                    logger.debug("获取到客户端请求，进行业务拆分(获取参数Map),paramMap:{}", JSON.toJSONString(paramMap));
                    if (null == paramMap || paramMap.size() == 0) {
                        ResponsePackUtil.CalibrationParametersFailure(ctx, "获取参数失败！");
                        ReferenceCountUtil.release(msg);
                        return;
                    }

                    String function_id = paramMap.get("function_id"); // 获取功能ID
                    
                    if (ParamConfig.interfaceVoMap.containsKey(function_id)) {
                    	CloudDealService cloudDealService=HttpServer.context.getBean(CloudDealService.class);
                    	cloudDealService.processCloud(paramMap,ctx.channel());
                    } else {
                    	String user_id = paramMap.get("user_id"); // 获取用户ID
                        String request_body = paramMap.get("request_body");// 获取参数实体
                        if ("2101".equals(function_id)) {
                            request_body = toStringHex(request_body);
                        }
                        String tel_number = paramMap.get("tel_number");
                        logger.debug("请求参数,function_id:{},user_id:{},request_body:{},tel_number:{}", function_id, user_id, request_body, tel_number);
                        if (null == function_id || function_id.equals("") || null == user_id || null == request_body) {
                            ResponsePackUtil.cagHttpResponse(ctx.channel(), ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, ""));
                            ReferenceCountUtil.release(msg);
                            return;
                        }

                        String request_body_decryption = decryptRequestBody(request_body, function_id, user_id);
                        logger.debug("解密请求体,function_id:{},user_id:{},request_body:{},request_body_decryption:{}", function_id, user_id, request_body, request_body_decryption);
                        if (null == request_body_decryption) {
                            logger.error("解密请求体为空{}", request_body_decryption);
                            ResponsePackUtil.cagHttpResponse(ctx.channel(), ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1005, ""));
                            ReferenceCountUtil.release(msg);
                            return;
                        }

                        if (FunctionIdConstant.workTeamUrlFunctionIdList.contains(function_id))// 工作圈业务模块请求
                            ServicesQueue.WorkTeam_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.taskUrlFunctionIdList.contains(function_id))// 任务业务模块
                            ServicesQueue.sendTask_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.settingUrlFunctionIdList.contains(function_id))// 设置业务模块
                            ServicesQueue.setting_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.announceUrlFunctionIdList.contains(function_id))// 公告业务模块
                            ServicesQueue.announce_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.loginAuthUrlFunctionIdList.contains(function_id))// 登陆业务模块
                            ServicesQueue.loginAuth_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.signInUrlFunctionIdList.contains(function_id))// 签到业务模块
                            ServicesQueue.integral_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.addressUrlFunctionIdList.contains(function_id))// 通讯录业务模块
                            ServicesQueue.address_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.versionUrlFunctionIdList.contains(function_id))// 版本更新业务模块
                            ServicesQueue.version_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.versionUrlFunctionIdList.contains(function_id))// 版本更新业务模块
                            ServicesQueue.versionPc_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.workBenchUrlFunctionIdList.contains(function_id))// 多角色工作台业务模块
                            ServicesQueue.workBench_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.hlwAuthUrlFunctionIdList.contains(function_id))// 多角色工作台业务模块
                            ServicesQueue.hlwAuth_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.mailConfigUrlFunctionIdList.contains(function_id))// 邮箱配置台业务模块
                            ServicesQueue.mailBox_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.corpCustomUrlFunctionIdList.contains(function_id))// 企业定制logo业务模块
                            ServicesQueue.corpCustom_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.settingNewUrlFunctionIdList.contains(function_id))// 2.1版本设置模块
                            ServicesQueue.settingNew_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.OAaccountInfoList.contains(function_id))
                            ServicesQueue.oaaccount_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.sensitivewordList.contains(function_id))
                            ServicesQueue.sensitiveword_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.collectionList.contains(function_id)) // pc收藏内容模块
                            ServicesQueue.conllection_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.pcversionlist.contains(function_id)) // pc版本更新模块
                            ServicesQueue.versionPc_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.squeareFeedbackList.contains(function_id)) // 服务号反馈
                            ServicesQueue.sqfeedback_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.urlmanageFunctionIdList.contains(function_id)) // 二维码url
                            ServicesQueue.urlmanage_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else if (FunctionIdConstant.noDisturbList.contains(function_id))
                            ServicesQueue.noDisturb_queue.put(new QueuePacket(msg, ctx.channel(), function_id, user_id, request_body_decryption, tel_number));
                        else{
                            ResponsePackUtil.CalibrationParametersFailure(ctx, "请求地址不存在！");
                            ReferenceCountUtil.release(msg);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("请求异常", e);
            ResponsePackUtil.CalibrationParametersFailure(ctx, "请求异常");
            ReferenceCountUtil.release(msg);
        }

    }

    /**
     * 对请求体进行解密
     * 
     * @param request_body
     * @param function_id
     * @param user_id
     * @return
     * @Description:
     */
    private String decryptRequestBody(String request_body, String function_id, String user_id) {
        logger.debug("对请求体进行解密,request_body:{},function_id:{},user_id:{}", request_body, function_id, user_id);
        try {
            String encodeType = FunctionIdConstant.encodeTypeMap.get(function_id);
            if (null == encodeType || "".equals(encodeType) || (!encodeType.equals("AES") && !encodeType.equals("RSA") && !encodeType.equals("CLEAR")))
                encodeType = "CLEAR";
            if (encodeType.equals("AES")) {
                if (StringUtils.isEmpty(user_id)) {
                    logger.error("AES解密失败,user_id为空{}", user_id);
                    return null;
                }

                return decodeParamsAES(user_id, request_body, user_id);
            } else if (encodeType.equals("RSA")) {
                return decodeParamsRSA(user_id, request_body);
            } else {
                return request_body;
            }
        } catch (Exception e) {
            logger.error("对请求体进行解密异常,request_body:{},function_id:{},user_id:{}", request_body, function_id, user_id, e);
            return null;
        }

    }

    /**
     * AES解密参数
     * 
     * @param param
     * @param key
     * @return
     * @Description:
     */
    private String decodeParamsAES(String user_id, String request_body, String key) {
        logger.debug("AES解密参数,user_id:{},key:{},request_body:{}", user_id, key, request_body);
        try {
            return AESUtil.decode(key, request_body);
        } catch (Exception e) {
            logger.error("AES解密异常,user_id:{},request_body:{},key:{}", user_id, request_body, key, e);
            return null;
        }
    }

    /**
     * RSA解密参数
     * 
     * @param param
     * @param key
     * @return
     * @Description:
     */
    private String decodeParamsRSA(String user_id, String request_body) {
        logger.debug("RSA解密参数,user_id:{},request_body:{}", user_id, request_body);
        String rsa_private_key = ParamConfig.rsa_private_key;
        logger.debug("RSA解密参数,user_id:{},rsa_private_key:{}", user_id, rsa_private_key);
        try {
            String body = RSAUtil.decryptPrivateKey(request_body, rsa_private_key);
            logger.debug("RSA解密参数,user_id:{},body:{}", user_id, body);
            return body;
        } catch (Exception e) {
            logger.error("RSA解密异常,user_id:{},request_body:{},rsa_private_key:{}", user_id, request_body, rsa_private_key, e);
            return null;
        }
    }

    // 转化十六进制编码为字符串
    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                logger.error(baKeyword[i] + "字符无法转换16进制");
            }
        }
        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            logger.error(baKeyword + "无法转换16进制");
        }
        return s;
    }
}
