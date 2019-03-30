package com.royasoft.vwt.cag.util.mq;

import java.io.Serializable;

/**
 * 工作圈回复推送实体类
 * 
 * @author qinp
 * 
 * @since 0.0.1
 */
public class WorkCircleReply implements Serializable {

	private static final long serialVersionUID = 8192234749393477690L;

	/** 发送人姓名 */
	private String sendPersonName;

	/** 发送人手机号 */
	private String sendPersonCell;

	/** 接收人姓名 */
	private String receiverPersonName;

	/** 接收人手机号 */
	private String receiverPersonCell;

	/** 说说内容 */
	private String content;
	
	/** 评论内容 */
	private String replyContent;

	/** 回复消息id 用于回复去重 */
	private String replyId;

	/** 说说id */
	private String messageId;

	/** 回复时间 */
	private String sendTime;

	/** 赞的人员列表 逗号隔开 */
	private String memberNames;
	
	/** 说说图片 */
	private String filePath;

	/** 赞的手机列表 逗号隔开 */
	private String memberCells;

	/** 类型 评论为1 赞为2 取消赞为3 */
	private int type;
	
	/** 说说类型 1:文本2:图片 4:图文 其中2、4需要传filePath */
	private int sendType;
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getSendPersonName() {
		return sendPersonName;
	}

	public void setSendPersonName(String sendPersonName) {
		this.sendPersonName = sendPersonName;
	}

	public String getSendPersonCell() {
		return sendPersonCell;
	}

	public void setSendPersonCell(String sendPersonCell) {
		this.sendPersonCell = sendPersonCell;
	}

	public String getReceiverPersonName() {
		return receiverPersonName;
	}

	public void setReceiverPersonName(String receiverPersonName) {
		this.receiverPersonName = receiverPersonName;
	}

	public String getReceiverPersonCell() {
		return receiverPersonCell;
	}

	public void setReceiverPersonCell(String receiverPersonCell) {
		this.receiverPersonCell = receiverPersonCell;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getReplyContent() {
		return replyContent;
	}

	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}

	public String getReplyId() {
		return replyId;
	}

	public void setReplyId(String replyId) {
		this.replyId = replyId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getMemberNames() {
		return memberNames;
	}

	public void setMemberNames(String memberNames) {
		this.memberNames = memberNames;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getMemberCells() {
		return memberCells;
	}

	public void setMemberCells(String memberCells) {
		this.memberCells = memberCells;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSendType() {
		return sendType;
	}

	public void setSendType(int sendType) {
		this.sendType = sendType;
	}
}
