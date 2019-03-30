package com.royasoft.vwt.soa.sundry.insideBuy.impl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 订单实体类
 * 
 * @Author: yucong
 * @Since: 2019年3月25日
 */
@Entity
@Table(name = "vwt_ins_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1579942579968721745L;

    /** 主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "vwt_ins_order_gen")
    @SequenceGenerator(name = "vwt_ins_order_gen", sequenceName = "vwt_ins_order_seq")
    private Long id;

    /** 用户ID */
    @Column(name = "phone_id")
    private Long phoneId;

    /** 商品ID */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "goods_info_id", referencedColumnName = "")
    private GoodsInfo goodsInfo;

    /** 商品型号ID */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "goods_model_id")
    private GoodsModel goodsModel;

    /** 商品数量 */
    @Column(name = "goods_count")
    private Integer goodsCount;
    
    /** 用户姓名 */
    @Column(name = "username")
    private String username;
    
    /** 手机号码 */
    @Column(name = "phone_num")
    private Long phoneNum;
    
    /** 自提营业厅 */
    @Column(name = "business_hall")
    private String businessHall;

    /** 派送地址 */
    @Column(name = "address", length = 50)
    private String address;

    /** 预约时间 */
    @Column(name = "create_time")
    private Date createTime;

    /** 分组 uuid */
    @Column(name = "order_group")
    private String orderGroup;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(Long phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getBusinessHall() {
        return businessHall;
    }

    public void setBusinessHall(String businessHall) {
        this.businessHall = businessHall;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getOrderGroup() {
        return orderGroup;
    }

    public void setOrderGroup(String orderGroup) {
        this.orderGroup = orderGroup;
    }

}
