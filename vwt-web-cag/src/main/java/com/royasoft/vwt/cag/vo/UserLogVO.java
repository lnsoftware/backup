package com.royasoft.vwt.cag.vo;

import java.io.Serializable;

/**
 * 人员变更实体
 * 
 * @author daizl
 *
 */
public class UserLogVO implements Serializable {

    private static final long serialVersionUID = 8521496469661381076L;

    /** UUID,去重 */
    private String uuid;

    /** 操作顺序(纳秒) */
    private long seq;

    /** 用户id */
    private String userId;

    /** 手机号 */
    private String mobile;

    /** 手机串号 */
    private String IMEI;

    /** 企业id */
    private String corpId;

    /** 0导入，1激活，2修改，3删除 */
    private Integer dealFlag;

    private String cityCode;

    private String areaCode;

    /** 处理时间 yyyy-MM-dd HH:mm:ss */
    private String dealTime;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getDealFlag() {
        return dealFlag;
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

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
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

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String iMEI) {
        IMEI = iMEI;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    @Override
    public String toString() {
        return getUserId() + "\t" + getDealFlag() + "\t" + getDealTime() + "\t" + getCorpId() + "\t" + getMobile() + "\t" + getCityCode() + "\t" + getAreaCode();
    }
}
