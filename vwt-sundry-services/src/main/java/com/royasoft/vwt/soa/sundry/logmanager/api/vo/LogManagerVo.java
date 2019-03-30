/************************************************
 *  Copyright © 2002-2016 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.logmanager.api.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 信息反馈VO
 * 
 * @author Jiangft
 * 
 * @Date 2016-3-25
 */
public class LogManagerVo implements Serializable {

    private static final long serialVersionUID = -1953670119163623756L;

    /** 日志id */
    private String logid;

    /** 文件名称 */
    private String filename;

    /** 文件路径 */
    private String filepath;

    /** 用户id */
    private String userid;

    /** 手机号码 */
    private String phonenumber;

    /** 0客户端手动上传，1管理平台拉取 */
    private String action;

    /** 上传时间 */
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
