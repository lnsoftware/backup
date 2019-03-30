/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.queue;

import java.util.concurrent.LinkedBlockingQueue;

import com.royasoft.vwt.cag.packet.QueuePacket;

/**
 * 异步处理队列
 * 
 * @author jxue
 *
 */
public class ServicesQueue {

    /** 登录队列 **/
    public static LinkedBlockingQueue<QueuePacket> login_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 工作圈业务队列 */
    public static LinkedBlockingQueue<QueuePacket> WorkTeam_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 任务业务队列 */
    public static LinkedBlockingQueue<QueuePacket> sendTask_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 设置业务队列 */
    public static LinkedBlockingQueue<QueuePacket> setting_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 2.1版本设置业务队列 */
    public static LinkedBlockingQueue<QueuePacket> settingNew_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 会议业务队列 */
    public static LinkedBlockingQueue<QueuePacket> meeting_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 公告业务队列 */
    public static LinkedBlockingQueue<QueuePacket> announce_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 登陆业务队列 */
    public static LinkedBlockingQueue<QueuePacket> loginAuth_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 通讯录业务队列 */
    public static LinkedBlockingQueue<QueuePacket> address_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 积分业务队列 */
    public static LinkedBlockingQueue<QueuePacket> integral_queue = new LinkedBlockingQueue<QueuePacket>();

    public static LinkedBlockingQueue<QueuePacket> integralH5_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 版本更新业务队列 */
    public static LinkedBlockingQueue<QueuePacket> version_queue = new LinkedBlockingQueue<QueuePacket>();
    /** pc版本更新业务队列 */
    public static LinkedBlockingQueue<QueuePacket> versionPc_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 版本更新业务队列 */
    public static LinkedBlockingQueue<QueuePacket> versionVGP_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 红包处理队列 */
    public static LinkedBlockingQueue<QueuePacket> redpacket_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 多角色工作台业务队列 */
    public static LinkedBlockingQueue<QueuePacket> workBench_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 邀请体系业务队列 */
    public static LinkedBlockingQueue<QueuePacket> inviteSystem_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 互联网企业认证业务队列 */
    public static LinkedBlockingQueue<QueuePacket> hlwAuth_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 邮箱管理业务队列 */
    public static LinkedBlockingQueue<QueuePacket> mailBox_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 企业或部门logo业务队列 */
    public static LinkedBlockingQueue<QueuePacket> corpCustom_queue = new LinkedBlockingQueue<QueuePacket>();

    /** 投票业务队列 */
    public static LinkedBlockingQueue<QueuePacket> vote_queue = new LinkedBlockingQueue<QueuePacket>();
    /** IMS通讯录业务队列 */
    public static LinkedBlockingQueue<QueuePacket> ims_queue = new LinkedBlockingQueue<QueuePacket>();
    /** OA账号业务队列 **/
    public static LinkedBlockingQueue<QueuePacket> oaaccount_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 二学一做业务队列 **/
    public static LinkedBlockingQueue<QueuePacket> twoLearn_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 美丽江苏业务队列 **/
    public static LinkedBlockingQueue<QueuePacket> beautyJS_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 敏感词业务队列 **/
    public static LinkedBlockingQueue<QueuePacket> sensitiveword_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 同步免打扰状态业务队列 */
    public static LinkedBlockingQueue<QueuePacket> noDisturb_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 山东收藏功能队列 */
    public static LinkedBlockingQueue<QueuePacket> conllection_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 服务号反馈功能队列 */
    public static LinkedBlockingQueue<QueuePacket> sqfeedback_queue = new LinkedBlockingQueue<QueuePacket>();
    /** 山东OA功能列表*/
    public static LinkedBlockingQueue<QueuePacket> shandongoa_queue = new LinkedBlockingQueue<QueuePacket>();
    
    /** 二维码url功能列表*/
    public static LinkedBlockingQueue<QueuePacket> urlmanage_queue = new LinkedBlockingQueue<QueuePacket>();
    
    /** 公告业务队列 */
    public static LinkedBlockingQueue<QueuePacket> insidePurch_queue = new LinkedBlockingQueue<QueuePacket>();

}
