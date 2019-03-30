/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.queue;

import java.util.concurrent.LinkedBlockingQueue;

import com.royasoft.vwt.controller.packet.QueuePacket;

/**
 * 异步处理队列
 * 
 * @author jxue
 *
 */
public class ServicesQueue {

    /** 登陆业务队列 */
    public static LinkedBlockingQueue<QueuePacket> loginAuth_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 用户反馈队列 */
    public static LinkedBlockingQueue<QueuePacket> useFeedback_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 邀请体系业务队列 */
    public static LinkedBlockingQueue<QueuePacket> inviteSystem_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 图文推送业务队列 */
    public static LinkedBlockingQueue<QueuePacket> materialCenter_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 投票功能业务队列 */
    public static LinkedBlockingQueue<QueuePacket> vote_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 互联网认证 */
    public static LinkedBlockingQueue<QueuePacket> internetAuth_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 通讯录相关队列 */
    public static LinkedBlockingQueue<QueuePacket> address_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 素材相关队列 */
    public static LinkedBlockingQueue<QueuePacket> graphicSource_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 公告相关对列 */
    public static LinkedBlockingQueue<QueuePacket> announce_queue = new LinkedBlockingQueue<QueuePacket>();

    /** redis相关对列 */
    public static LinkedBlockingQueue<QueuePacket> redis_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 工作圈黑名单相关队列 */
    public static LinkedBlockingQueue<QueuePacket> workteam_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 两学一做相关队列 */
    public static LinkedBlockingQueue<QueuePacket> twoLearn_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 圈子管理相关队列 */
    public static LinkedBlockingQueue<QueuePacket> circle_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 关键词相关队列 */
    public static LinkedBlockingQueue<QueuePacket> keyWords_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 服务号信息 */
    public static LinkedBlockingQueue<QueuePacket> squareMessage_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 群聊相关队列 */
    public static LinkedBlockingQueue<QueuePacket> imGroup_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 百度富文本编辑器上传图片相关队列 */
    public static LinkedBlockingQueue<QueuePacket> baiduUpload_queue = new LinkedBlockingQueue<QueuePacket>();
    
    /** 节日欢迎图相关队列 */
    public static LinkedBlockingQueue<QueuePacket> festival_queue = new LinkedBlockingQueue<QueuePacket>();
    
    /** 服务号反馈功能队列 */
    public static LinkedBlockingQueue<QueuePacket> sqfeedback_queue = new LinkedBlockingQueue<QueuePacket>();  
    
    /** 导出Excel相关队列 */
    public static LinkedBlockingQueue<QueuePacket> export_queue = new LinkedBlockingQueue<QueuePacket>();
    
    /** 部门管理相关队列 */
    public static LinkedBlockingQueue<QueuePacket> deptManager_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 二维码url相关队列 */
    public static LinkedBlockingQueue<QueuePacket> urlmanage_queue = new LinkedBlockingQueue<QueuePacket>();
    
    /** 内购手机相关队列 */
    public static LinkedBlockingQueue<QueuePacket> inside_queue = new LinkedBlockingQueue<QueuePacket>();


}
