package com.royasoft.vwt.controller.util;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.royasoft.vwt.common.security.AESUtil;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.vo.Response;

public class ResponsePackUtil {
    public static final Logger logger = LoggerFactory.getLogger(ResponsePackUtil.class);

    public static void CalibrationParametersFailure(ChannelHandlerContext ctx) {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("favicon.ico".getBytes("UTF-8")));
            // response.headers().set("Content-Type", "text/plain");
            response.headers().set("Content-Type", "application/json;charset=utf-8");
            response.headers().set("Content-Length", response.content().readableBytes());
            ctx.writeAndFlush(response);
        } catch (Exception e) {
            logger.error("响应异常", e);
        } finally {
            // ctx.close();
        }
    }

    /**
     * 参数校验错误
     * 
     * @param ctx
     * @throws UnsupportedEncodingException
     */
    public static void CalibrationParametersFailure(ChannelHandlerContext ctx, String msg) {
        try {
            logger.debug("BAD_REQUEST-400-CalibrationParametersFailure", msg);
            Map<String, String> map = new HashMap<String, String>();
            map.put("result", "300");
            map.put("resultMsg", msg);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(map.toString().getBytes("UTF-8")));
            // response.headers().set("Content-Type", "text/plain");
            response.headers().set("Content-Type", "application/json;charset=utf-8");
            response.headers().set("Content-Length", response.content().readableBytes());
            ctx.writeAndFlush(response);
        } catch (Exception e) {
            logger.error("响应异常", e);
        } finally {
            // ctx.close();
        }
    }

    /**
     * 参数校验错误
     * 
     * @param channel
     * @throws UnsupportedEncodingException
     */
    public static void CalibrationParametersFailure(Channel channel, String msg) {
        try {
            logger.debug("BAD_REQUEST-400-CalibrationParametersFailure", msg);
            Map<String, String> map = new HashMap<String, String>();
            map.put("result", "300");
            map.put("resultMsg", msg);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(map.toString().getBytes("UTF-8")));
            // response.headers().set("Content-Type", "text/plain");
            response.headers().set("Content-Type", "application/json;charset=utf-8");
            response.headers().set("Content-Length", response.content().readableBytes());
            channel.writeAndFlush(response);
        } catch (Exception e) {
            logger.error("响应异常", e);
        } finally {
            // channel.close();
        }
    }

    /**
     * 返回错误信息（状态：404），未找到对应的请求信息
     * 
     * @return
     */
    public static String returnFaileInfo() {
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        jsonObject.put("result", 404);
        jsonObject.put("resultMap", "未找到对应的请求信息");
        return jsonObject.toString();
    }

    /**
     * 响应异常信息
     * 
     * @param channel
     * @throws UnsupportedEncodingException
     */
    public static void responseStatusFailure(Channel channel, String msg) throws UnsupportedEncodingException {
        try {
            logger.debug("BAD_REQUEST-400-responseStatusFailure");
            Map<String, String> map = new HashMap<String, String>();
            map.put("result", "300");
            map.put("resultMsg", msg);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(map.toString().getBytes("UTF-8")));
            response.headers().set("Content-Type", "text/plain");
            response.headers().set("Content-Length", response.content().readableBytes());
            channel.writeAndFlush(response);
        } finally {
            // channel.close();
        }
    }

    /**
     * 打印报文
     * 
     * @param channel
     * @throws UnsupportedEncodingException
     */
    public static void buildPack(Channel channel, String code, String body) throws UnsupportedEncodingException {
        try {
            logger.debug("BAD_REQUEST-400-buildPack");
            Response res = new Response();
            res.setResponse_code(code);
            res.setResponse_body(body);
            res.setResponse_desc(ResponseInfoConstant.responseMap.get(code));
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(JSON.toJSONString(res).getBytes("UTF-8")));
            response.headers().set("Content-Type", "text/plain");
            response.headers().set("Content-Length", response.content().readableBytes());
            channel.writeAndFlush(response);
        } finally {
            // channel.close();
        }
    }

    /**
     * 响应成功结果
     * 
     * @param channel
     * @throws UnsupportedEncodingException
     */
    public static void responseStatusFaile(Channel channel, String res) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("result", HttpResponseStatus.EXPECTATION_FAILED);
        map.put("resultMsg", res);
        FullHttpResponse response = null;
        try {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EXPECTATION_FAILED, Unpooled.wrappedBuffer(map.toString().getBytes("UTF-8")));
            response.headers().set("Content-Type", "text/plain");
            response.headers().set("Content-Length", response.content().readableBytes());
            channel.writeAndFlush(response);
        } catch (Exception e) {

        } finally {
            // channel.close();
        }
    }

    /**
     * 响应成功结果
     * 
     * @param channel
     * @throws UnsupportedEncodingException
     */
    public static void responseStatusOK(Channel channel, String res) throws UnsupportedEncodingException {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(res.getBytes("UTF-8")));
            response.headers().set("Content-Type", "text/plain");
            response.headers().set("Content-Length", response.content().readableBytes());
            channel.writeAndFlush(response);
        } finally {
            // channel.close();
        }
    }

    /************************************************************************ 以下为原新vgp返回封装 ***********************************************************************************/
    /**
     * 打包应答报文
     * 
     * @param head
     * @param response
     * @return
     */
    public static String buildPack(String code, Object body) {
        Response response = new Response();
        response.setResponse_code(code);
        response.setResponse_body(body);
        response.setResponse_desc(ResponseInfoConstant.responseMap.get(code));
        return JSON.toJSONString(response, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 打包应答报文
     * 
     * @param head
     * @param response
     * @return
     */
    public static String buildPack(String code, Object body, SerializeConfig ser) {
        Response response = new Response();
        response.setResponse_code(code);
        response.setResponse_body(body);
        response.setResponse_desc(ResponseInfoConstant.responseMap.get(code));
        return JSON.toJSONString(response, ser, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 打包应答报文
     * 
     * @param head
     * @param response
     * @return
     */
    public static String buildPack(Object response) {
        return JSON.toJSONString(response, SerializerFeature.WriteMapNullValue);
    }
    
    /**
     * 打包应答报文
     * 
     * @param head
     * @param response
     * @return
     */
    public static String buildPack(Object response, SerializeConfig ser) {
        return JSON.toJSONString(response, ser, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 对参数进行对称加密
     * 
     * @param data 待加密数据
     * @param userKey 密钥
     * @return
     * @Description:
     */
    public static String encryptData(String data, String userKey) {
        try {
            data = AESUtil.encode(userKey, data);
        } catch (Exception e) {
            logger.error("应答报文加密出错", e);
            return "";
        }
        return data;
    }

    /**
     * 根据返回信息获取相应code
     * 
     * @param resInfo
     * @return
     * @Description:
     */
    public static String getResCode(String resInfo) {
        Response response = JSON.parseObject(resInfo, Response.class);
        return response.getResponse_code();
    }

    /**
     * 根据返回信息获取相应body
     * 
     * @param resInfo
     * @return
     * @Description:
     */
    public static String getResBody(String resInfo) {
        Response response = JSON.parseObject(resInfo, Response.class);
        return response.getResponse_body() + "";
    }

    /**
     * cag标准响应
     * 
     * @param channel
     * @param res
     * @Description:
     */
    public static void cagHttpResponse(Channel channel, String res) {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(res.getBytes("utf-8")));
            // response.headers().set("Content-Type", "text/plain");
            response.headers().set("Content-Type", "application/json;charset=utf-8");
            response.headers().set("Content-Length", response.content().readableBytes());
            response.headers().set("Access-Control-Allow-Origin", "*");
            channel.writeAndFlush(response);
        } catch (Exception e) {
            logger.error("cag标准响应异常,res:{}", res, e);
        } finally {
            // channel.close();
        }
    }

    /**
     * cagJSP标准响应
     * 
     * @param channel
     * @param res
     * @Description:
     */
    public static void cagHttpJspResponse(Channel channel, String res) {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(res.getBytes("utf-8")));
            response.headers().set("Content-Type", "text/html");
            // response.headers().set("Content-Type", "application/json;charset=utf-8");
            response.headers().set("Content-Length", response.content().readableBytes());
            response.headers().set("Access-Control-Allow-Origin", "*");
            channel.writeAndFlush(response);
        } catch (Exception e) {
            logger.error("cag标准响应异常,res:{}", res, e);
        } finally {
            // channel.close();
        }
    }

    /**
     * cagTEXT标准响应
     * 
     * @param channel
     * @param res
     * @Description:
     */
    public static void cagHttpStringResponse(Channel channel, String res) {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(res.getBytes("utf-8")));
            response.headers().set("Content-Type", "text/plain");
            // response.headers().set("Content-Type", "application/json;charset=utf-8");
            response.headers().set("Content-Length", response.content().readableBytes());
            response.headers().set("Access-Control-Allow-Origin", "*");
            channel.writeAndFlush(response);
        } catch (Exception e) {
            logger.error("cag标准响应异常,res:{}", res, e);
        } finally {
            // channel.close();
        }
    }

}
