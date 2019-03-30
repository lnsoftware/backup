/************************************************
 *  Copyright © 2002-2016 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.vo;

import java.io.Serializable;

/**
 * 当前系统用户信息类
 * 
 * @author mnt
 *
 */
public class CurrentSysUser implements Serializable {

    private static final long serialVersionUID = -7226967142844957940L;
    // 用户ID
    private String userId;
    // 登录名
    private String loginName;
    // 用户名称
    private String userName;
    // 用户联系电话
    private String telNum;
    // 用户所属企业
    private String corpId;
    // 用户所属企业名称
    private String corpName;
    // 用户所属省/地市/区域编码
    private String userCityArea;
    // 用户地市名称
    private String userCityAreaName;
    // 角色Id
    private Integer roleId;
    // 角色名称
    private String roleName;
    // 如果部门管理员，则有部门id
    private String deptId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getUserCityArea() {
        return userCityArea;
    }

    public void setUserCityArea(String userCityArea) {
        this.userCityArea = userCityArea;
    }

    public String getUserCityAreaName() {
        return userCityAreaName;
    }

    public void setUserCityAreaName(String userCityAreaName) {
        this.userCityAreaName = userCityAreaName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }

    public String getCorpName() {
        return corpName;
    }

    public void setCorpName(String corpName) {
        this.corpName = corpName;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

}
