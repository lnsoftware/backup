/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util.mq;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;

/**
 * rocketMq工具类
 * 
 * @author qinp
 * 
 * @since 0.0.1
 */
public class RocketMqUtil {
    public static final String ImTaskQueue = "ImTaskQueue";
    public static final String WorkCircleQueue = "WorkCircleQueue";
    public static final String AnnouncementQueue = "AnnouncementQueue";
    public static final String AddressUpdateQueue = "AddressUpdateQueue";
    public static final String ImMeetingQueue = "ImMeetingQueue";
    public static final String PushQueue = "PushQueue";
    public static final String logManageQueue = "logsManageQueue";
    public static final String activeDealQueue = "activeDealQueue";
    public static final String VoicePushMessageTopic = "VoicePushMessageTopic";
    public static final String integralDealQueue = "integralDealQueue";

    public static DefaultMQProducer producer;
    public static final String SOURCE = "CAG";

    public static final int SQUARE_HEAD = 5;

    private static final Logger logger = LoggerFactory.getLogger(RocketMqUtil.class);

    public static synchronized void init(String nameAddr) {
        try {
            producer = new DefaultMQProducer("cagImProducer");
            producer.setNamesrvAddr(nameAddr);
            producer.setInstanceName("Producer");
            producer.start();
        } catch (Exception e) {
            logger.error("初始化mq异常,nameAddr:{},e:{}", nameAddr, e);
        }
    }

    public static boolean send(String topic, String message) {
        try {
            Message msg = new Message(topic, topic, UUID.randomUUID().toString(), message.getBytes("utf-8"));
            producer.send(msg);
            return true;
        } catch (Exception e) {
            logger.error("入mq异常,topic:{},message:{},e:{}", topic, message, e);
            return false;
        }
    }

}
