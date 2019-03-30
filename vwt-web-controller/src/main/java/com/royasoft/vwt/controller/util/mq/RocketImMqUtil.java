/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.util.mq;

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
public class RocketImMqUtil {
    private static final Logger logger = LoggerFactory.getLogger(RocketImMqUtil.class);

    public static final String logMessageQueue = "logmessagequeue";
    public static final String RoleManagerQueue = "roleManagerQueue";
    public static final String omclogManageQueue = "omcLogManageQueue";// omc管理平台操作日志

    public static DefaultMQProducer producer;

    public static synchronized void init(String nameAddr) {
        logger.info("RocketImMqUtil_Addr--===========--->" + nameAddr);
        try {
            producer = new DefaultMQProducer("OMC_ProducerGroupName2");
            producer.setNamesrvAddr(nameAddr);
            producer.setInstanceName("Omc_IM_Producer");
            producer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean send(String topic, String message) {
        try {
            Message msg = new Message(topic, topic, UUID.randomUUID().toString(), message.getBytes("utf-8"));
            logger.debug("msg---------->" + msg);
            logger.debug("producer---------->" + producer);
            producer.send(msg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
