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
@Table(name = "vwt_inside_goods_info")
public class GoodsInfo implements Serializable {

	private static final long serialVersionUID = 2474056886715683672L;

	/** 主键 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "vwt_inside_goods_info_gen")
	@SequenceGenerator(name = "vwt_inside_goods_info_gen", sequenceName = "INSIDE_GOODS_INFO_SEQ")
	@Column(name = "id")
	private Long id;

	/** 商品名称 */
	@Column(name = "name")
	private String name;

	/** 商品图片 */
	@Column(name = "logo_url")
	private String logoUrl;

	@Column(name = "opt_time")
	private Date optTime;

	@Column(name = "opt_user_id")
	private String optUserId;

	/** 商品详情列表 */
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "goodsInfo")
	private List<GoodsDetail> detailList;

	@Column(name = "sort")
	private Long sort;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public Date getOptTime() {
		return optTime;
	}

	public void setOptTime(Date optTime) {
		this.optTime = optTime;
	}

	public String getOptUserId() {
		return optUserId;
	}

	public void setOptUserId(String optUserId) {
		this.optUserId = optUserId;
	}

	public List<GoodsDetail> getDetailList() {
		return detailList;
	}

	public void setDetailList(List<GoodsDetail> detailList) {
		this.detailList = detailList;
	}

	public Long getSort() {
		return sort;
	}

	public void setSort(Long sort) {
		this.sort = sort;
	}

}
