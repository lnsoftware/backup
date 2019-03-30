package com.royasoft.vwt.cag.util.mq;

import java.io.Serializable;

/**
 * 工作圈请求实体类
 * 
 * @author qinp
 * 
 */
public class WorkCircleAction implements Serializable {

    private static final long serialVersionUID = -4606107486152609249L;

    /** 请求类型 1:新说说 2:提醒 */
    private int type;

    /**
     * 请求id,强制给 ""
     */
    private String request_id;

    /** 消息id */
    private long msg_id;

    /** 请求内容 */
    private String content;

    /** 手机号 */
    private String telNum;
    
    private String to_role_id;

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(long msg_id) {
        this.msg_id = msg_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getRequest_id() {
        return "";
    }

    public void setRequest_id(String requestId) {
        request_id = "";
    }

    public String getTo_role_id() {
        return to_role_id;
    }

    public void setTo_role_id(String to_role_id) {
        this.to_role_id = to_role_id;
    }

}
