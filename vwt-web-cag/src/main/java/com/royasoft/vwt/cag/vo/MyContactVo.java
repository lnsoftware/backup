/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.vo;

import java.io.Serializable;

/**
 * 个人常用联系人
 * 
 * @author mnt
 *
 */
public class MyContactVo implements Serializable {

    private static final long serialVersionUID = 6378506909217381474L;

    private String ctId;// 常用联系人 ID号
    private String memId;// 用户ID
    private String memberName;// 名称
    private String telNum;// 手机号码
    private String partName;// 部门

    public String getCtId() {
        return ctId;
    }

    public void setCtId(String ctId) {
        this.ctId = ctId;
    }

    public String getMemId() {
        return memId;
    }

    public void setMemId(String memId) {
        this.memId = memId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

}
