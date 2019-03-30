/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.vo;

import java.io.Serializable;

/**
 * 发送消息VO
 *@Author:MB
 *@Since:2015年10月21日
 */
public class ImSendInfoVO implements Serializable {

    private static final long serialVersionUID = -3088570921300594830L;
    
    /** 编号 */
    private int id;
    /** 发送时间 */
    private String sendDate;
    /** 发送用户 */
    private String fromUser;
    /** 接收用户 */
    private String users;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 随机数 */
    private String uuid;
    /** 文件目录 */
    private String filePath;

    private String reserve1;
    private String reserve2;
    private String reserve3;
    private String reserve4;

    public ImSendInfoVO() {
        sendDate = String.valueOf(System.currentTimeMillis());
    }

    public ImSendInfoVO(int id) {
        this.id = id;
        sendDate = String.valueOf(System.currentTimeMillis());
    }

    public String getReserve1() {
        return reserve1;
    }

    public String getReserve4() {
        return reserve4;
    }

    public void setReserve4(String reserve4) {
        this.reserve4 = reserve4;
    }

    public void setReserve1(String reserve1) {
        this.reserve1 = reserve1;
    }

    public String getReserve2() {
        return reserve2;
    }

    public void setReserve2(String reserve2) {
        this.reserve2 = reserve2;
    }

    public String getReserve3() {
        return reserve3;
    }

    public void setReserve3(String reserve3) {
        this.reserve3 = reserve3;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
