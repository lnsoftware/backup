package com.royasoft.vwt.soa.sundry.insideBuy.impl.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 商品类型
 *
 * @Author: yucong
 * @Since: 2019年3月25日
 */
@Entity
@Table(name = "vwt_ins_goods_type")
public class GoodsType implements Serializable {

    private static final long serialVersionUID = 215853941118995387L;

    /** 主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "vwt_ins_goods_type_gen")
    @SequenceGenerator(name = "vwt_ins_goods_type_gen", sequenceName = "vwt_ins_goods_type_seq")
    private Long id;

    // 一个商品类型下有多个商品
    // @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "goodsType")
    // private List<GoodsInfo> goodsInfos;

    /** 商品类别 */
    @Column(name = "goods_type")
    private String goodsType;

    /** 排序 */
    @Column(name = "type_order")
    private Integer typeOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public Integer getTypeOrder() {
        return typeOrder;
    }

    public void setTypeOrder(Integer typeOrder) {
        this.typeOrder = typeOrder;
    }

    @Override
    public String toString() {
        return "[" + goodsType + typeOrder + "]";
    }

}
