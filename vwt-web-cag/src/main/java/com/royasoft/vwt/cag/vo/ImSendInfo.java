package com.royasoft.vwt.cag.vo;

public class ImSendInfo {

	private Long id;// ��Ӧid
	private String sendDate;// ����ʱ��
	private String fromUser;// ������
	private String users;// ��Ϣ������
	private String title;// ����
	private String content;// ����
	private String uuid;// ����ID
	private String filePath;// �ϴ��ļ���ַ

	private String reserve1;// ��չ�ֶ�1
	private String reserve2;// ��չ�ֶ�2
	private String reserve3;// ��չ�ֶ�3
	private String reserve4;
	public ImSendInfo() {
		sendDate = String.valueOf(System.currentTimeMillis());
	}

	public ImSendInfo(Long id) {
		this.id = id;
		sendDate = String.valueOf(System.currentTimeMillis());
	}

	public String getReserve1() {
		return reserve1;
	}

	public String getReserve4() {
		return reserve4;
	}

	public void setReserve4(String reserve4) {
		this.reserve4 = reserve4;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	public String getReserve2() {
		return reserve2;
	}

	public void setReserve2(String reserve2) {
		this.reserve2 = reserve2;
	}

	public String getReserve3() {
		return reserve3;
	}

	public void setReserve3(String reserve3) {
		this.reserve3 = reserve3;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public String getUsers() {
		return users;
	}

	public void setUsers(String users) {
		this.users = users;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
