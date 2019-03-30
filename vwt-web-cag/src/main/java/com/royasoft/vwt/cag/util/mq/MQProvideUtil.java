package com.royasoft.vwt.cag.util.mq;

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class MQProvideUtil {

    public static final String SOURCE="cag";
    
    public static final String FirstLvQueue = "FirstLvQueue";

    public static final String SecLvQueue = "SecLvQueue";

    public static final String ThdLvQueue = "ThdLvQueue";

    public static final String IM_MSG = "IM";

    public static final String COMMON_MSG = "COMMON";

    public static final String LOW_MSG = "LOW";

    public static final String NEWVGP_MSG = "ActiveQueue";
    
    public static final String WORK_CIRCLE_CONNECTTYPE="WorkCircle";
    public static final Integer WORK_TYPE_NEW=1;
    public static final Integer WORK_TYPE_NOTICE=2;
    public static final Integer WORK_HEAD=6;
    
    public static final Integer IMTASK_HEAD=10;
    public static final String IMTASK_CONNECTTYPE="ImTask";
    public static final Integer IMTASK_TYPE_CREATE=1;
    public static final Integer IMTASK_TYPE_UPDATE=2;
    public static final Integer IMTASK_TYPE_CANCLE=3;

    public static Session sessionIM = null;
    public static Session sessionCOMMON = null;
    public static Session sessionLOW = null;
    public static Session sessionNEWVGP = null;
    public static MessageProducer producerIM = null;
    public static MessageProducer producerCOMMON = null;
    public static MessageProducer producerLOW = null;
    public static MessageProducer producerNEWVGP = null;

    /**
     * 初始化
     */
    public static void init(String url) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
        try {
            /** 即时消息专用连接 */
            Connection connectionIM = factory.createConnection();
            /** 一般消息专用连接 */
            Connection connectionCOMMON = factory.createConnection();
            /** 通知类消息专用连接 */
            Connection connectionLOW = factory.createConnection();
            /** 消息响应专用连接 */
            Connection connectionNEWVGP = factory.createConnection();
            connectionIM.setClientID(UUID.randomUUID().toString());
            connectionCOMMON.setClientID(UUID.randomUUID().toString());
            connectionLOW.setClientID(UUID.randomUUID().toString());
            connectionNEWVGP.setClientID(UUID.randomUUID().toString());
            connectionIM.start();
            connectionCOMMON.start();
            connectionLOW.start();
            connectionNEWVGP.start();
            sessionIM = connectionIM.createSession(false, Session.AUTO_ACKNOWLEDGE);
            sessionCOMMON = connectionCOMMON.createSession(false, Session.AUTO_ACKNOWLEDGE);
            sessionLOW = connectionLOW.createSession(false, Session.AUTO_ACKNOWLEDGE);
            sessionNEWVGP = connectionNEWVGP.createSession(false, Session.AUTO_ACKNOWLEDGE);
            initProduce(sessionIM, FirstLvQueue, "IM");
            initProduce(sessionCOMMON, SecLvQueue, "COMMON");
            initProduce(sessionLOW, ThdLvQueue, "LOW");
            initProduce(sessionNEWVGP, NEWVGP_MSG, "NEWVGP");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private static void initProduce(Session session, String topic, String type) {
        try {
            Destination destination = session.createQueue(topic);
            if ("IM".equals(type)) {
                producerIM = session.createProducer(destination);
                producerIM.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else if ("COMMON".equals(type)) {
                producerCOMMON = session.createProducer(destination);
                producerCOMMON.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else if ("LOW".equals(type)) {
                producerLOW = session.createProducer(destination);
                producerLOW.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else if ("NEWVGP".equals(type)) {
                producerNEWVGP = session.createProducer(destination);
                producerNEWVGP.setDeliveryMode(DeliveryMode.PERSISTENT);
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送方法
     * 
     * @param connectType IM即时消息 COMMON其他消息
     * @param topic
     * @param msg
     */
    public static void send(String connectType, String msg) {
        try {

            if (connectType.equals("IM")) {
                TextMessage message = sessionIM.createTextMessage(msg);
                producerIM.send(message);
            } else if (connectType.equals("COMMON")) {
                TextMessage message = sessionCOMMON.createTextMessage(msg);
                producerCOMMON.send(message);
            } else if (connectType.equals("LOW")) {
                TextMessage message = sessionLOW.createTextMessage(msg);
                producerLOW.send(message);
            } else if (connectType.equals("NEWVGP")) {
                TextMessage message = sessionNEWVGP.createTextMessage(msg);
                producerNEWVGP.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}