package com.royasoft.vwt.soa.sundry.insidePurch.api.vo;

import java.io.Serializable;

public class OrderRecordVO implements Serializable {

	private static final long serialVersionUID = 2474056886715683672L;

	/** 主键 */
	private Long id;

	private String orderTime;

	private String userName;

	private String telNum;

	private String jobNum;

	private String deptName;

	private String name;

	private String typeName;

	private long orderCount;

	private double insidePrice;

	private double outPrice;

	private double sumPrice;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public long getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(long orderCount) {
		this.orderCount = orderCount;
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

}
