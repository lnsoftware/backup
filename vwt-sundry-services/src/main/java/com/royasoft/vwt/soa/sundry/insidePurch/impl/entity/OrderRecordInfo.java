package com.royasoft.vwt.soa.sundry.insidePurch.impl.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "vwt_inside_order_info")
public class OrderRecordInfo implements Serializable {

	private static final long serialVersionUID = 2474056886715683672L;

	/** 主键 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "vwt_inside_order_info_gen")
	@SequenceGenerator(name = "vwt_inside_order_info_gen", sequenceName = "INSIDE_ORDER_INFO_SEQ")
	@Column(name = "id")
	private Long id;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "tel_num")
	private String telNum;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "job_num")
	private String jobNum;

	@Column(name = "dept_num")
	private String deptName;

	@Column(name = "order_time")
	private Date orderTime;

	@Column(name = "order_count")
	private Long orderCount;

	/** 收货地址 */
	@Column(name = "rec_address")
	private String recAddress;

	/** 商品详情列表 */
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "orderRecordInfo")
	private List<OrderRecord> recordList;

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

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
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

	public List<OrderRecord> getRecordList() {
		return recordList;
	}

	public void setRecordList(List<OrderRecord> recordList) {
		this.recordList = recordList;
	}

	public String getRecAddress() {
		return recAddress;
	}

	public void setRecAddress(String recAddress) {
		this.recAddress = recAddress;
	}

}
