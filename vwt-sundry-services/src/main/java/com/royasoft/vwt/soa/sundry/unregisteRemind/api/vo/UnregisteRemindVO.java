package com.royasoft.vwt.soa.sundry.unregisteRemind.api.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 未激活人员短信提醒时间表 VO
 * 
 * @author daizl
 *
 */
public class UnregisteRemindVO implements Serializable {

    private static final long serialVersionUID = 2474056886715683672L;

    /** 主键 */
    private String id;

    /** 企业id */
    private String corpId;

    /** 最近一次提醒时间 */
    private Date lastSendTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public Date getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime(Date lastSendTime) {
        this.lastSendTime = lastSendTime;
    }
}
