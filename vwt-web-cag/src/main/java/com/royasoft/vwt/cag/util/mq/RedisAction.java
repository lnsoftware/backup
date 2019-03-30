package com.royasoft.vwt.cag.util.mq;

/**
 * 新消息redis实体类
 * @author Administrator
 *
 */
public class RedisAction {
	 /** 消息表示主键 */
    private Long id;
	 /** 类型 */
    private int head;

    /** 来源 */
    private String source;
    
    /** 消息内容 */
    private Object message;

    /** 创建时间 */
    private String createTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
