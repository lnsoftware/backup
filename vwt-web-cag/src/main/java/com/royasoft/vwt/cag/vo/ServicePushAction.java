package com.royasoft.vwt.cag.vo;

import java.io.Serializable;

/**
 * 服务号推送
 * 
 * @author jxue
 * 
 */
public class ServicePushAction implements Serializable {

    private static final long serialVersionUID = -2921533314359387780L;

    /** 请求id **/
    private String requestId = "";

    /** 请求类型 1-服务号 2-139邮箱 3-服务号删除 */
    private int type;

    /** 请求内容 */
    private ServicePush content ;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ServicePush getContent() {
        return content;
    }

    public void setContent(ServicePush content) {
        this.content = content;
    }
}
