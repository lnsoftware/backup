/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.util;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;

/**
 * im入mq
 * 
 * @author ICT-Dev
 *
 */
public class RocketMqUtil {
    public static final String BuinessPushQueue = "BuinessPushQueue";
    public static DefaultMQProducer producer;

    private static final Logger logger = LoggerFactory.getLogger(RocketMqUtil.class);

    public static synchronized void init(String nameAddr) {
        try {
            producer = new DefaultMQProducer("ProducerGroupName");
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
    
    public static void shutdown() {
        producer.shutdown();
    }

}
