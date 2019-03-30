package com.royasoft.vwt.soa.sundry.insidePurch.impl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "vwt_inside_order")
public class OrderRecord implements Serializable {

	private static final long serialVersionUID = 2474056886715683672L;

	/** 主键 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "vwt_inside_order_gen")
	@SequenceGenerator(name = "vwt_inside_order_gen", sequenceName = "INSIDE_ORDER_SEQ")
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

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "detail_id")
	private GoodsDetail goodsDetail;
	
	/** 父节点 */
	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "order_info_id")
	private OrderRecordInfo orderRecordInfo;

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

	public GoodsDetail getGoodsDetail() {
		return goodsDetail;
	}

	public void setGoodsDetail(GoodsDetail goodsDetail) {
		this.goodsDetail = goodsDetail;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public OrderRecordInfo getOrderRecordInfo() {
		return orderRecordInfo;
	}

	public void setOrderRecordInfo(OrderRecordInfo orderRecordInfo) {
		this.orderRecordInfo = orderRecordInfo;
	}

}
