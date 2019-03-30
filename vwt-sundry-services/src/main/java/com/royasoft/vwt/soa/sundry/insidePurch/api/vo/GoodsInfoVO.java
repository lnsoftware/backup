package com.royasoft.vwt.soa.sundry.insidePurch.api.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;

public class GoodsInfoVO implements Serializable {

	private static final long serialVersionUID = 2474056886715683672L;

	/** 主键 */
	private Long id;

	/** 商品名称 */
	private String name;

	/** 商品图片 */
	private String logoUrl;

	private Date optTime;

	private String optUserId;

	/** 商品详情列表 */
	private List<GoodsDetailVO> detailList;

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

	public List<GoodsDetailVO> getDetailList() {
		return detailList;
	}

	public void setDetailList(List<GoodsDetailVO> detailList) {
		this.detailList = detailList;
	}

	public Long getSort() {
		return sort;
	}

	public void setSort(Long sort) {
		this.sort = sort;
	}

}
