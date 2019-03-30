/************************************************
 *  Copyright © 2002-2016 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.util.mq;

/**
 * 新消息redis实体类
 * @author ZHOUKQ
 *
 */
public class RedisAction {
	 /** 类型 */
    private int head;

    /** 来源 */
    private String source;
    
    /** 消息内容 */
    private String message;

    /** 创建时间 */
    private String createTime;

	public int getHead() {
		return head;
	}

	public void setHead(int head) {
		this.head = head;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
