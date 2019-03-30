package com.royasoft.vwt.cag.vo;

import java.io.Serializable;

/**
 * 操作日志流水vo
 *
 * @Author:MB
 * @Since:2016年4月5日
 */
public class OperationLogVO implements Serializable {

    private static final long serialVersionUID = 3701119946182375313L;

    /** UUID,去重 */
    private String uuid;

    /** 用户id */
    private String userId;

    /** 用户手机号 */
    private String mobile;

    /** 请求ip */
    private String ip;

    /** 请求内容 */
    private String requestMsg;

    /** 请求是否成功 0000-成功 */
    private String responseStatus;

    /** 模块编码 */
    private String model;

    /** 操作码 */
    private String operation;

    /** 备注 */
    private String remark;

    /** 操作时间 */
    private Long operationTime;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRequestMsg() {
        return requestMsg;
    }

    public void setRequestMsg(String requestMsg) {
        this.requestMsg = requestMsg;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Long getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Long operationTime) {
        this.operationTime = operationTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
