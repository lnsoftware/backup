package com.royasoft.vwt.cag.util.mq;

import java.io.Serializable;

/**
 * 任务推送实体类
 * 
 * @author yuj
 * 
 * @since 0.0.1
 */
public class ImTaskAction implements Serializable {

    private static final long serialVersionUID = -6612620509666317640L;

    /** 请求类型 1为create 2为update 3为cancel*/ 
    private int type;
    
    private String request_id;
    
    private String to_role_id;
    
    /** 任务id*/
    private long msg_id;

    /** 任务标题 */
    private String title;
    
    /** 任务发起人姓名 */
    private String fromUsername;
    
    /** 发送时间 */
    private String sendDate;

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
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
