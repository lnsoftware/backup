package com.royasoft.vwt.cag.vo;

import java.io.Serializable;

/**
 * 企业变更实体
 * 
 * @author daizl
 *
 */
public class CorpLogVO implements Serializable {

    private static final long serialVersionUID = -4136831395131949105L;

    /** UUID,去重 */
    private String uuid;

    /** 操作顺序(纳秒) */
    private long seq;

    /** 企业Id */
    private String corpId;

    /** 企业名 */
    private String corpName;

    /** 地市编码 */
    private String corpRegion;

    /** 区域编码 */
    private String corpArea;

    /** 客户经理手机号 */
    private String custManagerMobile;

    /** 客户经理姓名 */
    private String custManagerName;

    private String customerId;

    private String Bossid;

    /** 1：V网通,4：村务通,5：校讯通,6：通讯助手,7：互联网体验 */
    private Integer FROMCHANNEL;

    private String other;

    /** 0 新增，1修改，2删除 */
    private Integer dealFlag;

    /** 是否修改客户经理：1是，2否 */
    private Integer changeCustomer;

    /** 处理时间 yyyy-MM-dd HH:mm:ss */
    private String dealTime;

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public String getCorpName() {
        return corpName == null ? "" : corpName;
    }

    public void setCorpName(String corpName) {
        this.corpName = corpName;
    }

    public String getCorpRegion() {
        return corpRegion;
    }

    public void setCorpRegion(String corpRegion) {
        this.corpRegion = corpRegion;
    }

    public String getCorpArea() {
        return corpArea;
    }

    public void setCorpArea(String corpArea) {
        this.corpArea = corpArea;
    }

    public String getCustManagerMobile() {
        return custManagerMobile;
    }

    public void setCustManagerMobile(String custManagerMobile) {
        this.custManagerMobile = custManagerMobile;
    }

    public String getCustManagerName() {
        return custManagerName;
    }

    public void setCustManagerName(String custManagerName) {
        this.custManagerName = custManagerName;
    }

    public String getBossid() {
        return Bossid;
    }

    public void setBossid(String bossid) {
        Bossid = bossid;
    }

    public Integer getFROMCHANNEL() {
        return FROMCHANNEL;
    }

    public void setFROMCHANNEL(Integer fROMCHANNEL) {
        FROMCHANNEL = fROMCHANNEL;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public Integer getDealFlag() {
        switch (dealFlag) {
            case 0:
                return 0;
            case 1:
                return 0;
            case 2:
                return 1;
            default:
                return 0;
        }
    }

    public void setDealFlag(Integer dealFlag) {
        this.dealFlag = dealFlag;
    }

    public String getDealTime() {
        return dealTime;
    }

    public void setDealTime(String dealTime) {
        this.dealTime = dealTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getChangeCustomer() {
        return changeCustomer;
    }

    public void setChangeCustomer(Integer changeCustomer) {
        this.changeCustomer = changeCustomer;
    }

    @Override
    public String toString() {
        return getCorpId() + "\t" + getDealFlag() + "\t" + getDealTime() + "\t" + getCorpName().replaceAll("\t|\r|\n", "") + "\t" + getCorpRegion() + "\t" + getCorpArea() + "\t"
                + getCustManagerName() + "\t" + getCustManagerMobile() + "\t" + getBossid() + "\t" + getFROMCHANNEL() + "\t" + getOther();
    }
}
