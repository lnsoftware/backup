package com.royasoft.vwt.soa.sundry.insidePurch.api.vo;

import java.io.Serializable;
import java.util.List;

public class OrderRecordInfoVO implements Serializable {

	private static final long serialVersionUID = 2474056886715683672L;

	/** 主键 */
	private Long id;

	private String userId;

	private String telNum;

	private String userName;

	private String jobNum;

	private String deptName;

	private String orderTime;

	private String name;

	private String typeName;

	private double insidePrice;

	private double outPrice;

	private double sumPrice;

	private Long orderCount;

	private String recAddress;

	/** 商品详情列表 */
	private List<OrderRecordVO> recordList;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTelNum() {
		return telNum;
	}

	public void setTelNum(String telNum) {
		this.telNum = telNum;
	}

	public String getJobNum() {
		return jobNum;
	}

	public void setJobNum(String jobNum) {
		this.jobNum = jobNum;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}

	public Long getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(Long orderCount) {
		this.orderCount = orderCount;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<OrderRecordVO> getRecordList() {
		return recordList;
	}

	public void setRecordList(List<OrderRecordVO> recordList) {
		this.recordList = recordList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public double getInsidePrice() {
		return insidePrice;
	}

	public void setInsidePrice(double insidePrice) {
		this.insidePrice = insidePrice;
	}

	public double getOutPrice() {
		return outPrice;
	}

	public void setOutPrice(double outPrice) {
		this.outPrice = outPrice;
	}

	public double getSumPrice() {
		return sumPrice;
	}

	public void setSumPrice(double sumPrice) {
		this.sumPrice = sumPrice;
	}

	public String getRecAddress() {
		return recAddress;
	}

	public void setRecAddress(String recAddress) {
		this.recAddress = recAddress;
	}

}
