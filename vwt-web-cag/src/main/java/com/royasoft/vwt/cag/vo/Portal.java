/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.vo;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 请求响应工具类
 *
 * @Author:MB
 * @Since:2016年9月10日
 */
@JsonIgnoreProperties({ "servletRequest" })
public class Portal {

    // 功能号ID
    private String function_id;

    // 参数体
    private String request_body;

    private String resultCode;

    private String resultDesc;

    private Object resultContent;

    private String user_id;

    private String tel_number;

    // 远程地址
    private String remote_ip;
    
    //平台类型
    private String platform;

    //客户端版本号
    private String client_version;

    private String client_id;
   
    //客户端类型
    private String client_type;

    private String request_id;

    private String decodeKey;
    
    //终端类型
    private String terminalType; 
    //手机网络
    private String mobileNetwork;
    
    private String requestSource;

    public String getDecodeKey() {
        return decodeKey;
    }

    public void setDecodeKey(String decodeKey) {
        this.decodeKey = decodeKey;
    }

    @JSONField(serialize = false)
    private HttpServletRequest servletRequest;

    private Object response;

    public Portal() {

    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public Portal(String function_id, String request_body, String resultCode, String resultDesc, Object resultContent, String user_id, String tel_number, String remote_ip, String platform,
            String client_version, String client_id, String client_type, String request_id,String terminalType,String mobileNetwork, HttpServletRequest servletRequest,String requestSource) {
        this.function_id = function_id;
        this.request_body = request_body;
        this.resultCode = resultCode;
        this.resultDesc = resultDesc;
        this.resultContent = resultContent;
        this.user_id = user_id;
        this.tel_number = tel_number;
        this.remote_ip = remote_ip;
        this.platform = platform;
        this.client_version = client_version;
        this.client_id = client_id;
        this.servletRequest = servletRequest;
        this.client_type = client_type;
        this.request_id = request_id;
        this.terminalType = terminalType;
        this.mobileNetwork = mobileNetwork;
        this.requestSource=requestSource;
    }

    public String getFunction_id() {
        return function_id;
    }

    public void setFunction_id(String function_id) {
        this.function_id = function_id;
    }

    public String getRequest_body() {
        return request_body;
    }

    public void setRequest_body(String request_body) {
        this.request_body = request_body;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }

    public Object getResultContent() {
        return resultContent;
    }

    public void setResultContent(Object resultContent) {
        this.resultContent = resultContent;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTel_number() {
        return tel_number;
    }

    public void setTel_number(String tel_number) {
        this.tel_number = tel_number;
    }

    public String getRemote_ip() {
        return remote_ip;
    }

    public void setRemote_ip(String remote_ip) {
        this.remote_ip = remote_ip;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getClient_version() {
        return client_version;
    }

    public void setClient_version(String client_version) {
        this.client_version = client_version;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_type() {
        return client_type;
    }

    public void setClient_type(String client_type) {
        this.client_type = client_type;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public String toString() {
        return "function_id:" + function_id + ",tel_number:" + tel_number + ",user_id:" + user_id + ",request_body:" + request_body;
    }

	public String getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}

	public String getMobileNetwork() {
		return mobileNetwork;
	}

	public void setMobileNetwork(String mobileNetwork) {
		this.mobileNetwork = mobileNetwork;
	}

	public String getRequestSource() {
		return requestSource;
	}

	public void setRequestSource(String requestSource) {
		this.requestSource = requestSource;
	}

}
