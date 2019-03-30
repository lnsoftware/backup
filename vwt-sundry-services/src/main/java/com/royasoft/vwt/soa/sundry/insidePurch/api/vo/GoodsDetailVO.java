package com.royasoft.vwt.soa.sundry.insidePurch.api.vo;

import java.io.Serializable;

public class GoodsDetailVO implements Serializable {

	private static final long serialVersionUID = 2474056886715683672L;

	/** 主键 */
	private Long id;

	/** 父节点 */
	private GoodsInfoVO goodsInfo;

	/** 内部价格 */
	private double insidePrice;

	/** 市场价 */
	private double outPrice;

	/** 可选类型 */
	private String typeName;

	/** 排序 */
	private Long sort;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GoodsInfoVO getGoodsInfo() {
		return goodsInfo;
	}

	public void setGoodsInfo(GoodsInfoVO goodsInfo) {
		this.goodsInfo = goodsInfo;
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

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Long getSort() {
		return sort;
	}

	public void setSort(Long sort) {
		this.sort = sort;
	}
}
