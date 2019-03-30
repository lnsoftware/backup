package com.royasoft.vwt.soa.sundry.unregisteRemind.impl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 未激活人员短信提醒时间表 对应实体
 * @author daizl
 *
 */
@Entity
@Table(name="vwt_ungiste_remind")
public class UnregisteRemind implements Serializable{
    
    private static final long serialVersionUID = 2474056886715683672L;

    /** 主键 */
    @Id
    @GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "assigned")
    @Column(name = "id")
    private String id;
    
    /**企业id*/
    @Column(name="corpid")
    private String corpId;
    
    /**最近一次提醒时间*/
    @Column(name="lastsendtime")
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
