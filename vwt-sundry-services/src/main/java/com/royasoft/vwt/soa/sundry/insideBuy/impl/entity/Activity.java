package com.royasoft.vwt.soa.sundry.insideBuy.impl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 活动实体类
 *
 * @Author:yucong
 * @Since:2019
 */

@Entity
@Table(name = "vwt_ins_activity")
public class Activity implements Serializable{

    private static final long serialVersionUID = 1670198930894079950L;

    /** 主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "vwt_ins_activity_gen")
    @SequenceGenerator(name = "vwt_ins_activity_gen", sequenceName = "vwt_ins_activity_seq")
    private Long id;

    /** 活动开始时间 */
    @Column(name = "start_time")
    private Date startTime;

    /** 活动结束时间 */
    @Column(name = "end_time")
    private Date endTime;

    /** 活动开始说明 */
    @Column(name = "start_introduce", length = 500, nullable = false)
    private String startIntroduce;

    /** 活动结束说明 */
    @Column(name = "end_introduce", length = 500, nullable = false)
    private String endIntroduce;

    /** 每个用户限购总数量 */
    @Column(name = "limit_total")
    private Integer limitTotal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStartIntroduce() {
        return startIntroduce;
    }

    public void setStartIntroduce(String startIntroduce) {
        this.startIntroduce = startIntroduce;
    }

    public String getEndIntroduce() {
        return endIntroduce;
    }

    public void setEndIntroduce(String endIntroduce) {
        this.endIntroduce = endIntroduce;
    }

    public Integer getLimitTotal() {
        return limitTotal;
    }

    public void setLimitTotal(Integer limitTotal) {
        this.limitTotal = limitTotal;
    }

}
