package com.royasoft.vwt.cag.util.mq;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 发送任务回执
 * 
 * @author Liu Hui
 * 
 */
public class SendTaskReceipt {

    private int id;

    private int sendTaskId; // 发送任务ID

    private String content; // 回执内容

    private String fromUserId; // 回执用户

    private String receiverName;// 回执用户姓名

    private int status; // 回执状态（0:未读1：已读）

    private String receiveTime; // 回执时间

    private String receiveTime1;// 回执时间（特定格式:年月日时分）

    private String createTime; // 创建时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSendTaskId() {
        return sendTaskId;
    }

    public void setSendTaskId(int sendTaskId) {
        this.sendTaskId = sendTaskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getReceiveTime1() {
        receiveTime1 = "";
        if (null != receiveTime && !"".equals(receiveTime)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat perFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date formatDate = format.parse(receiveTime);
                receiveTime1 = perFormat.format(formatDate);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return receiveTime1;
    }

    public void setReceiveTime1(String receiveTime1) {
        this.receiveTime1 = receiveTime1;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
