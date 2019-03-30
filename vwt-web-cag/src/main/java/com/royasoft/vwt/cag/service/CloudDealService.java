/*
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 */

package com.royasoft.vwt.cag.service;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.vo.InterfaceVo;
import com.royasoft.vwt.cag.vo.Portal;
import com.royasoft.vwt.common.security.AESUtil;
import com.royasoft.vwt.common.security.RSAUtil;
import com.royasoft.vwt.cag.vo.Response;
import io.netty.channel.Channel;
/**
 * cloud服务调用
 * 
 * @author MB
 *
 */
@Component
public class CloudDealService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RestTemplate restTemplate;

    public void processCloud(Map<String,String> paramMap, Channel channel) throws UnsupportedEncodingException {
    	Portal portal=new Portal();
    	
        String function_id = paramMap.get("function_id"); // 获取功能ID
        String user_id = paramMap.get("user_id"); // 获取用户ID
        String request_body = paramMap.get("request_body");// 获取参数实体
        String tel_number = paramMap.get("tel_number");
        
        InterfaceVo interfaceVo = ParamConfig.interfaceVoMap.get(function_id);
        try {
            // 解密参数
        	request_body=decryptRequestBodyCloud(user_id, request_body, interfaceVo.getEncodeType());
        } catch (Exception e) {
            logger.error("解密异常:{}", e);
            ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1005, ""));
        }
        
        portal.setFunction_id(function_id);
        portal.setRequest_body(request_body);
        portal.setTel_number(tel_number);
        portal.setUser_id(user_id);
        

        logger.debug("cloud请求,url:{},portal:{}", interfaceVo.getRequestUrl(), JSON.toJSONString(portal));

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", " application/json;charset=UTF-8");
        HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(portal), headers);

        Response businessProcessResult = null;
        // 检查是否包含ip
        if (iscontains(interfaceVo.getRequestUrl())) {
            // 包含ip的访问模式,方便开发
            RestTemplate restTem = new RestTemplate();
            businessProcessResult = restTem.postForObject(interfaceVo.getRequestUrl(), request, Response.class);
        } else {
            businessProcessResult = restTemplate.postForObject(interfaceVo.getRequestUrl(), request, Response.class);
        }
        // 处理业务

        logger.debug("cloud响应,url:{},portal:{}", interfaceVo.getRequestUrl(), JSON.toJSONString(businessProcessResult));

        if(null!=businessProcessResult){
            String res_body=encodeResponseMsgCloud(user_id, JSON.toJSONString(businessProcessResult.getResponse_body()), interfaceVo);
            businessProcessResult.setResponse_body(res_body);
            ResponsePackUtil.responseStatusOK(channel, JSON.toJSONString(businessProcessResult));
        }else{
            ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
        }

    }

    public String decryptRequestBodyCloud(String user_id,String request_body,String encodeType) throws Exception {
        if (StringUtils.isEmpty(request_body))
            return "";

        switch (encodeType) {
            case "AES":
                return decodeParamsAES(user_id, request_body, user_id);
            case "RSA":
                return decodeParamsRSA(user_id, request_body);
            case "CLEAR":
                return request_body;
            default:
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
    
    private String encodeResponseMsgCloud(String user_id, String response_body,InterfaceVo interfaceVo) {
    	try {
	    	switch (interfaceVo.getEncodeType().toUpperCase()) {
	            case "AES":
					return AESUtil.encode(user_id, response_body);
	            case "RSA":
	                String rsa_private_key = ParamConfig.rsa_private_key;
	                return RSAUtil.encryptPrivateKey(response_body, rsa_private_key);
	            default:
	            	return response_body;
	        }
    	} catch (Exception e) {
			logger.error("返回值加密失败,e:{}",e);
			return null;
		}
    }
    
    /**
     * 是否包含ip
     * 
     * @param url url
     * @return true|fasle
     */
    private static boolean iscontains(String url) {

        String ipRegex = "\\d{2,3}([.]\\d{1,3}){3}:\\d{2,5}";

        Pattern pattern = Pattern.compile(ipRegex);
        Matcher matcher = pattern.matcher(url);
        return matcher.find();

    }
}
