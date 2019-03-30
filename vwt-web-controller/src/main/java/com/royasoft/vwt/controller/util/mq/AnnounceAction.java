/************************************************
 *  Copyright © 2002-2016 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.util.mq;

import java.io.Serializable;

/**
 * 公告推送实体类
 * 
 * @author qinp
 * 
 * @since 0.0.1
 */
public class AnnounceAction implements Serializable {

	private static final long serialVersionUID = -8947794222120407378L;

    /** 请求类型 1为create 2为update*/
    private int type;
    
    /** 公告id*/
    private long msg_id;

    /** 人员编号 */
    private String to_role_id="";

    /** 公告标题 */
    private String title;
    
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


    public String getTo_role_id() {
        return to_role_id;
    }

    public void setTo_role_id(String to_role_id) {
        this.to_role_id = to_role_id;
    }

    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}

}
