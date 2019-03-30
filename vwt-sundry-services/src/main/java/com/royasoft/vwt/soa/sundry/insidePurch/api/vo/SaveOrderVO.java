package com.royasoft.vwt.soa.sundry.insidePurch.api.vo;

import java.io.Serializable;
import java.util.Date;

public class SaveOrderVO implements Serializable {

	private static final long serialVersionUID = 2474056886715683672L;

	private Long orderCount;

	private Long goodsDetailId;

	public Long getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(Long orderCount) {
		this.orderCount = orderCount;
	}

	public Long getGoodsDetailId() {
		return goodsDetailId;
	}

	public void setGoodsDetailId(Long goodsDetailId) {
		this.goodsDetailId = goodsDetailId;
	}

}
