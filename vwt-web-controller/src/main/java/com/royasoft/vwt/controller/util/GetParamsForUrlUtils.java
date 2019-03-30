/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * 获取参数工具类
 * 
 * @author ZHOUKQ
 *
 */
public class GetParamsForUrlUtils {
    private static final Logger logger = LoggerFactory.getLogger(GetParamsForUrlUtils.class);

    /**
     * 获取请求json参数对象
     * 
     * @param msg
     * @return JSONObject
     */
    public static JSONObject getHttpJsonParams(Object msg) {
        try {
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;
                ByteBuf buf = content.content();
                String jsonParam = buf.toString(io.netty.util.CharsetUtil.UTF_8);
                logger.debug("jsonParam:{}",jsonParam);
                buf.release();
                if (null != jsonParam && !"".equals(jsonParam)) {
                    // 字符串json 转json 对象
                    return JSONObject.parseObject(jsonParam);
                }
            }
            
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                Map<String, String> resultMap = getHttpParams(request);
                return (JSONObject) JSONObject.toJSON(resultMap);
            }
        } catch (Exception e) {
            logger.error("获取参数失败！", e.getMessage());
        }
        return null;
    }

    public static Map<String, String> getHttpParams(HttpRequest request) {
        try {
            return GetParamsForUrlUtils.getParamsMap(request);
        } catch (Exception e) {
            logger.error("获取参数失败！", e.getMessage());
            return null;
        }
    }

    /**
     * 获取请求参数
     * 
     * @param decoderQuery
     * @param uriAttributes
     * @return
     */
    public static Map<String, String> getParamsMap(HttpRequest request) {
        Map<String, String> resultMap = new HashMap<String, String>();

        // 获取请求参数map集合
        String function_id = "";
        String user_id = "";
        String request_body = "";
        String tel_number = "";
        // GET方式请求
        if (request.getMethod().equals(HttpMethod.GET)) {
            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
                if (attr.getKey().equals("function_id")) {
                    function_id = attr.getValue().get(0);
                } else if (attr.getKey().equals("user_id")) {
                    user_id = attr.getValue().get(0);
                } else if (attr.getKey().equals("request_body")) {
                    request_body = attr.getValue().get(0);
                } else if (attr.getKey().equals("tel_number")) {
                    tel_number = attr.getValue().get(0);
                }
            }
        }
        // POST方式请求
        if (request.getMethod().equals(HttpMethod.POST)) { // 处理POST请求

            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
            // 读取从客户端传过来的参数
            InterfaceHttpData postFunctionId = decoder.getBodyHttpData("function_id");
            InterfaceHttpData postUserId = decoder.getBodyHttpData("user_id");
            InterfaceHttpData postRequestBody = decoder.getBodyHttpData("request_body");
            InterfaceHttpData postTelNumber = decoder.getBodyHttpData("tel_number");

            function_id = getInterfaceHttpDataParamVaule(postFunctionId);
            user_id = getInterfaceHttpDataParamVaule(postUserId);
            request_body = getInterfaceHttpDataParamVaule(postRequestBody);
            try {
                tel_number = getInterfaceHttpDataParamVaule(postTelNumber);
            } catch (Exception e) {
                logger.error("!!!!!!!!!!!!!!!!!!!!!!!!获取请求参数异常,请检查请求参数是否为4个等情况.....", e);
            }
        }

        resultMap.put("function_id", function_id);
        resultMap.put("user_id", user_id);
        resultMap.put("request_body", request_body);
        resultMap.put("tel_number", tel_number);

        return resultMap;
    }

    public static Map<String, String> getHttpParamsVGP(HttpRequest request) {
        try {
            return GetParamsForUrlUtils.getParamsMapVGP(request);
        } catch (Exception e) {
            logger.error("获取参数失败！", e.getMessage());
            return null;
        }
    }

    /**
     * 获取请求参数
     * 
     * @param decoderQuery
     * @param uriAttributes
     * @return
     */
    public static Map<String, String> getParamsMapVGP(HttpRequest request) {
        Map<String, String> resultMap = new HashMap<String, String>();

        // 获取请求参数map集合
        String function_id = "";
        String request_body = "";
        String tel_number = "";
        // GET方式请求
        if (request.getMethod().equals(HttpMethod.GET)) {
            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
                if (attr.getKey().equals("function_id")) {
                    function_id = attr.getValue().get(0);
                } else if (attr.getKey().equals("request_body")) {
                    request_body = attr.getValue().get(0);
                } else if (attr.getKey().equals("tel_number")) {
                    tel_number = attr.getValue().get(0);
                }
            }
        }
        // POST方式请求
        if (request.getMethod().equals(HttpMethod.POST)) { // 处理POST请求
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
            // 读取从客户端传过来的参数
            InterfaceHttpData postFunctionId = decoder.getBodyHttpData("function_id");
            InterfaceHttpData postRequestBody = decoder.getBodyHttpData("request_body");
            InterfaceHttpData postTelNumber = decoder.getBodyHttpData("tel_number");
            function_id = getInterfaceHttpDataParamVaule(postFunctionId);
            request_body = getInterfaceHttpDataParamVaule(postRequestBody);
            try {
                tel_number = getInterfaceHttpDataParamVaule(postTelNumber);
            } catch (Exception e) {
                logger.error("!!!!!!!!!!!!!!!!!!!!!!!!获取请求参数异常,请检查请求参数是否为4个等情况.....", e);
            }
        }

        resultMap.put("function_id", function_id);
        resultMap.put("request_body", request_body);
        resultMap.put("tel_number", tel_number);

        return resultMap;
    }

    /**
     * POST请求获取参数值
     * 
     * @param value
     * @return String
     */
    private static String getInterfaceHttpDataParamVaule(InterfaceHttpData value) {
        String res = "";
        if (value.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) value;
            try {
                res = attribute.getValue();
            } catch (IOException e) {
                logger.error("POST请求获取参数值失败", e.getMessage());
                return "";
            }
        }
        return res;
    }
}
