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
 * @author daizl
 * 
 * @since 0.0.1
 */
public class UserAndCorpRocketMqUtil {
    public static final String userRecordQueue = "usersRecordQueue";
    public static final String corpRecordQueue = "corpsRecordQueue";

    public static DefaultMQProducer producer;
    public static final String SOURCE = "CAG";

    public static final int SQUARE_HEAD = 5;

    private static final Logger logger = LoggerFactory.getLogger(UserAndCorpRocketMqUtil.class);

    public static synchronized void init(String nameAddr) {
        try {
            producer = new DefaultMQProducer("cagUserAndCorpProducer");
            producer.setNamesrvAddr(nameAddr);
            producer.setInstanceName("UserAndCorpProducer");
            producer.setDefaultTopicQueueNums(1);
            producer.start();
        } catch (Exception e) {
            logger.error("初始化mq异常,nameAddr:{},e:{}", nameAddr, e);
        }
    }

    public static boolean send(String topic, String message) {
        try {
            logger.debug("普通入mq,topic:{},message:{}", topic, message);
            Message msg = new Message(topic, topic, UUID.randomUUID().toString(), message.getBytes("utf-8"));
            logger.debug("UserAndCorpRocketMqUtil,topic:{}", topic);
            producer.send(msg);
            return true;
        } catch (Exception e) {
            logger.error("入mq异常,topic:{},message:{},e:{}", topic, message, e);
            return false;
        }
    }

}
