/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.namespace.QName;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.BasicClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 短信发送工具类
 *
 * @Author:MB
 * @Since:2015年11月22日
 */
@Component
public class SmsUtil {

    public static final Logger logger = LoggerFactory.getLogger(SmsUtil.class);

    public static void sendSMS(String smsUrl, String mobile, String content) {
        SMSSend(smsUrl, mobile, content, "SMS", "1", "", "", "0", "短信验证码");
    }

    public static void sendSmsByWebService(String APID, String url, String mobile, String content) {
        sendMessage(APID, url, mobile, content, "SMS", "0", "", "", "0", "短信验证码");
    }

    /**
     * 通过webservice接口，发送消息到网关
     * 
     * @param mobile 发送的手机(APN发送用imei号)号码（逗号分隔）
     * @param content 内容
     * @param msgType 消息类型 SMS APN
     * @param returnFlag 是否要状态报告 1=是；0=否
     * @param validPeriod 超时时间
     * @param sendTime 发送时间
     * @param priority 优先级
     * @param subject 标题
     */
    private static void sendMessage(String APID, String url, String mobile, String content, String msgType, String returnFlag, String validPeriod, String sendTime, String priority, String subject) {
        logger.debug("发送短信,mobile:{},content:{},msgType:{}", mobile, content, msgType);
        String result = "";
        if (null != APID && !"".equals(APID) && null != url && !"".equals(url)) {
            String xml = "<Root><APID>" + APID + "</APID> " + "<MsgType>" + msgType + "</MsgType> " + "<ExtCode></ExtCode>" + "<MsgFmt>15</MsgFmt>" + "<RegDelivery>" + returnFlag + "</RegDelivery>"
                    + "<MsgContent>" + content + "</MsgContent>" + "<DstAddresses>" + mobile + "</DstAddresses>" + "<ValidPeriod>" + validPeriod + "</ValidPeriod>" + "<SendTime>" + sendTime
                    + "</SendTime>" + "<PRI>" + priority + "</PRI>" + "<Subject>" + subject + "</Subject>" + "</Root>";
            try {
                EngineConfiguration fileProvider = new BasicClientConfig();
                Service serv = new Service(fileProvider);
                Call call = (Call) serv.createCall();
                call.setTargetEndpointAddress(url);
                call.setOperationName(new QName(url, "APSubmitReq"));
                result = (String) call.invoke(new Object[] { xml });
                logger.debug("发送短信,推送返回结果为:{}", result);
            } catch (Exception ex) {
                logger.error("发送短信异常,APID:{},url:{},mobile:{},content:{},xml:{}", APID, url, mobile, content, xml, ex);
            }
        } else {
            logger.debug("发送短信,APID或url为空");
        }
    }

    /**
     * 通过http接口，发送短信
     * 
     * @param mobile 发送的手机号码
     * @param content 内容
     * @param msgType 消息类型 SMS APN
     * @param returnFlag 是否要状态报告 1=是；0=否
     * @param validPeriod 超时时间
     * @param sendTime 发送时间
     * @param priority 优先级
     * @param subject 标题
     */
    public static void SMSSend(String smsUrl, String mobile, String content, String msgType, String returnFlag, String validPeriod, String sendTime, String priority, String subject) {
        logger.debug("通过http接口，发送短信,smsUrl:{},mobile:{},content:{},msgType:{},returnFlag:{},validPeriod:{},sendTime:{},priority:{},subject:{}", smsUrl, mobile, content, msgType, returnFlag,
                validPeriod, sendTime, priority, subject);
        if (null == smsUrl || "".equals(smsUrl))
            return;
        try {
            sendSMS(smsUrl, mobile, content, "6005", subject, "");
        } catch (Exception ex) {
            logger.error("通过http接口，发送短信异常,smsUrl:{}", smsUrl, ex);
        }
    }

    /**
     * 发送短信
     * 
     * @param content 此参数是短信内容
     * @param mobilePhones 此参数是手机号码
     * @param priority 此参数是短信发送的优先级（1，2，3）其中3的级别最高
     * @param messageFlag 此参数是用于标识此条短信（可用于接收状态报告）
     * @param moduleName 此参数是发送短信的模块或系统的名字
     * @param exNumber 此参数是短信扩展码（比如企业接入号是：09995 设 ExNumber为9，那么用户收到消息时显示的号码为 099959）
     */
    public static String sendSMS(String strURL, String mobilePhones, String content, String messageFlag, String moduleName, String exNumber) {
        logger.debug("发送短信,strURL:{},mobilePhones:{},content:{},messageFlag:{},moduleName:{},exNumber:{}", strURL, mobilePhones, content, messageFlag, moduleName, exNumber);
        String token = "336e5a0e85b4a0485fbe54";
        String response = "";
        String priority = "3";
        try {
            strURL += "?MobilePhones=" + mobilePhones + "&Content=" + java.net.URLEncoder.encode(content, "utf-8") + "&Priority=" + priority + "&ExNumber=" + exNumber + "&MessageFlag=" + messageFlag
                    + "&ModuleName=" + moduleName + "&token=" + token;
            URL objURL = new URL(strURL);
            URLConnection objConn = objURL.openConnection();
            objConn.setDoInput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(objConn.getInputStream()));
            String line = br.readLine();
            while (line != null) {
                response += line;
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            logger.error("发送短信异常", e);
        }
        logger.debug("发送短信(返回response),strURL:{},mobilePhones:{},content:{},messageFlag:{},moduleName:{},exNumber:{},response:{}", strURL, mobilePhones, content, messageFlag, moduleName, exNumber,
                response);
        return response;
    }

}
