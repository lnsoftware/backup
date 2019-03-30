package com.royasoft.vwt.soa.sundry.insidePurch.impl.entity;

import java.io.Serializable;

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
@Table(name = "vwt_inside_goods_detail")
public class GoodsDetail implements Serializable {

	private static final long serialVersionUID = 2474056886715683672L;

	/** 主键 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "vwt_inside_goods_detail_gen")
	@SequenceGenerator(name = "vwt_inside_goods_detail_gen", sequenceName = "INSIDE_GOODS_DETAIL_SEQ")
	@Column(name = "id")
	private Long id;

	/** 父节点 */
	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "info_id")
	private GoodsInfo goodsInfo;

	/**内部价格*/
	@Column(name="inside_price")
	private double insidePrice;
	
	/**市场价*/
	@Column(name="out_price")
	private double outPrice;
	
	/**可选类型*/
	@Column(name="type_name")
	private String typeName;
	
	/**排序*/
	@Column(name="sort")
	private Long sort;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GoodsInfo getGoodsInfo() {
		return goodsInfo;
	}

	public void setGoodsInfo(GoodsInfo goodsInfo) {
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
