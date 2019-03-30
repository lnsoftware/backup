package com.royasoft.vwt.soa.sundry.insideBuy.impl.entity;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 商品信息实体类
 * 
 * @Author: yucong
 * @Since: 2019年3月25日
 */
@Entity
@Table(name = "vwt_ins_goods_info")
public class GoodsInfo implements Serializable {
    
    private static final long serialVersionUID = 6583568123375399707L;

    /** 主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "vwt_ins_goods_info_gen")
    @SequenceGenerator(name = "vwt_ins_goods_info_gen", sequenceName = "vwt_ins_goods_info_seq")
    private Long id;

    /** 商品编号 */
    @Column(name = "goods_num")
    private Long goodsNum;

	/** 商品机型编码 */
	@Column(name = "model_num")
	private String modelNum;

	/** 限购数量 */
	@Column(name = "limit_count")
	private Integer limitCount;

    /** 商品名称 */
    @Column(name = "goods_name", length = 50)
    private String goodsName;

    /** 商品描述 */
    @Column(name = "description", length = 50)
    private String description;

    /** 商品图片 */
    @Column(name = "picture")
    private String picture;

    /** 商品类型 */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "goods_type_id", referencedColumnName = "id")
    private GoodsType goodsType;

    /** 一个商品下有多个型号 */
    @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "goodsInfo")
    private List<GoodsModel> goodsModels;

    /** 商品品牌 */
    @Column(name = "brand", length = 20)
    private String brand;

    /** 商品状态 */
    @Column(name = "status")
    private Integer status;

    /** 更新时间 */
    @Column(name = "update_time")
    private Date updateTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(Long goodsNum) {
		this.goodsNum = goodsNum;
	}

	public String getModelNum() {
		return modelNum;
	}

	public void setModelNum(String modelNum) {
		this.modelNum = modelNum;
	}

	public Integer getLimitCount() {
		return limitCount;
	}

	public void setLimitCount(Integer limitCount) {
		this.limitCount = limitCount;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public GoodsType getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(GoodsType goodsType) {
		this.goodsType = goodsType;
	}

	public List<GoodsModel> getGoodsModels() {
		return goodsModels;
	}

	public void setGoodsModels(List<GoodsModel> goodsModels) {
		this.goodsModels = goodsModels;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
