package com.royasoft.vwt.soa.sundry.insideBuy.impl.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 购物车实体类
 * 
 * @Author: yucong
 * @Since: 2019年3月25日
 */
@Entity
@Table(name = "vwt_ins_shopping_cart")
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = -177499962592407216L;

    /** 主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "vwt_ins_shopping_cart_gen")
    @SequenceGenerator(name = "vwt_ins_shopping_cart_gen", sequenceName = "vwt_ins_shopping_cart_seq")
    private Long id;

    /** 用户ID */
    @Column(name = "phone_id")
    private Long phoneId;

    /** 商品ID */
    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "goods_info_id")
    private GoodsInfo goodsInfo;

    /** 商品型号ID */
    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "goods_model_id")
    private GoodsModel goodsModel;

    /** 商品数量 */
    @Column(name = "goods_count")
    private Integer goodsCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(Long phoneId) {
        this.phoneId = phoneId;
    }

    public GoodsInfo getGoodsInfo() {
        return goodsInfo;
    }

    public void setGoodsInfo(GoodsInfo goodsInfo) {
        this.goodsInfo = goodsInfo;
    }

    public GoodsModel getGoodsModel() {
        return goodsModel;
    }

    public void setGoodsModel(GoodsModel goodsModel) {
        this.goodsModel = goodsModel;
    }

    public Integer getGoodsCount() {
        return goodsCount;
    }

    public void setGoodsCount(Integer goodsCount) {
        this.goodsCount = goodsCount;
    }

}
