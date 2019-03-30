/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.packet;

import io.netty.channel.Channel;

/**
 * 异步队列使用的packet
 * 
 * @author jxue
 *
 */
public class QueuePacket {
    /**
     * 构造器
     * 
     * @param msg 待处理报文
     * @param channel 相关连接
     * @param function_id 请求功能号ID
     * @param user_id 请求用户ID
     * @param request_body 请求参数体
     */
    public QueuePacket(Object msg, Channel channel, String function_id, String user_id, String request_body, String tel_number) {
        this.msg = msg;
        this.channel = channel;
        this.function_id = function_id;
        this.user_id = user_id;
        this.request_body = request_body;
        this.tel_number = tel_number;
    }

    /** 待处理报文 **/
    private Object msg = null;

    /** 相关连接 **/
    private Channel channel = null;

    /** 请求功能号ID */
    private String function_id = "";

    /** 请求用户ID */
    private String user_id = "";

    /** 请求参数体 */
    private String request_body = "";

    private String tel_number = "";

    public String getFunction_id() {
        return function_id;
    }

    public void setFunction_id(String function_id) {
        this.function_id = function_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getRequest_body() {
        return request_body;
    }

    public void setRequest_body(String request_body) {
        this.request_body = request_body;
    }

    public Object getMsg() {
        return msg;
    }

    public QueuePacket setMsg(Object msg) {
        this.msg = msg;
        return this;
    }

    public Channel getChannel() {
        return channel;
    }

    public QueuePacket setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public String getTel_number() {
        return tel_number;
    }

    public void setTel_number(String tel_number) {
        this.tel_number = tel_number;
    }

}
