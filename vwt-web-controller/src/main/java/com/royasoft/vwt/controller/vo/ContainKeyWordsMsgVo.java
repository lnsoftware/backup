/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.vo;

import java.io.Serializable;
import java.util.Date;
/**
 * 记录包含敏感词信息VO
 * 
 * @author ZHOUKQ
 *
 */
public class ContainKeyWordsMsgVo implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String id;

    /** 接收对象 **/
    private String receiveUser;

    /** 发起人手机号 **/
    private String telNum;

    /** 内容 **/
    private String content;

    /** 发起时间 **/
    private Date sendTime;

    /** 1：消息,2:发表工作圈消息 3：评论工作圈消息 **/
    private String fromChannel;

    /** 发起人名称 **/
    private String memberName;
    
    /** 发起人部门 **/
    private String partName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReceiveUser() {
        return receiveUser;
    }

    public void setReceiveUser(String receiveUser) {
        this.receiveUser = receiveUser;
    }

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getFromChannel() {
        return fromChannel;
    }

    public void setFromChannel(String fromChannel) {
        this.fromChannel = fromChannel;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }
    
    
}
