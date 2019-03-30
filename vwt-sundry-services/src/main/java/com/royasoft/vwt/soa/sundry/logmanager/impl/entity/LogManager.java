/************************************************
 *  Copyright © 2002-2016 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.logmanager.impl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 日志管理实体类,对应vwt_log_manage表
 * 
 * @author jiangft
 * 
 * @Date 2016-3-25
 */
@Entity
@Table(name = "vwt_log_manage")
public class LogManager implements Serializable {

    private static final long serialVersionUID = 1115905867263743338L;

    /** 日志id */
    @Id
    @GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "assigned")
    @Column(name = "logid")
    private String logid;

    /** 文件名称(管理平台展示) */
    @Column(name = "filename")
    private String filename;

    /** 文件路径 */
    @Column(name = "filepath")
    private String filepath;

    /** 用户id */
    @Column(name = "userid")
    private String userid;

    /** 手机号 */
    @Column(name = "phonenumber")
    private String phonenumber;

    /** 0客户端手动上传，1管理平台拉取 */
    @Column(name = "action")
    private String action;

    /** 上传时间 */
    @Column(name = "updatetime")
    private Date updatetime;

    public String getLogid() {
        return logid;
    }

    public void setLogid(String logid) {
        this.logid = logid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

}
