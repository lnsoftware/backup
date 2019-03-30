/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.soa.systemsettings.mailconfig.api.interfaces.MailConfigInterface;
import com.royasoft.vwt.soa.systemsettings.mailconfig.api.vo.MailConfigVo;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

@Scope("prototype")
@Service
public class MailConfigService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MailConfigService.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private MailConfigInterface mailconfigInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.mailBox_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;

                    String function_id = queue_packet.getFunction_id();

                    String user_id = queue_packet.getUser_id(); // 获取用户ID

                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    String tel_number = queue_packet.getTel_number();
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        case FunctionIdConstant.MAILRECOMMEND:
                            resInfo = getMailRecommend(request_body, user_id);
                            break;
                        case FunctionIdConstant.MAILCONFIG:
                            resInfo = getMailConfig(request_body, user_id);
                            break;
                        default:
                            break;
                    }
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("邮箱设置处理类异常", e);
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }

        }
    }

    /**
     * 客户端获取域名
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月4日
     */
    public String getMailRecommend(String requestBody, String userId) {
        logger.debug("获取邮箱域名列表,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpid = trim(requestJson.getString("corpid"));
        logger.debug("获取邮箱域名列表,corpid:{}", corpid);
        /** 校验参数 */
        if (corpid.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1072, "");
        }

        Map<String, Object> conditions = new HashMap<String, Object>();

        conditions.put("EQ_mailtype", "1");// 推荐邮箱
        List<MailConfigVo> list1 = mailconfigInterface.findAllMailConfig(conditions);

        conditions.clear();
        conditions.put("EQ_mailtype", "2");// 常用邮箱
        List<MailConfigVo> list2 = mailconfigInterface.findAllMailConfig(conditions);

        conditions.clear();
        conditions.put("EQ_corpid", corpid);
        conditions.put("EQ_mailtype", "0"); // 企业邮箱
        List<MailConfigVo> list0 = mailconfigInterface.findAllMailConfig(conditions);

        List<MailConfigVo> list = new ArrayList<MailConfigVo>();
        list.addAll(list0);
        list.addAll(list1);
        list.addAll(list2);
        if (null == list || list.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1071, "");
        }
        JSONObject resJso = new JSONObject();
        // 企业邮箱
        for (MailConfigVo vo : list0) {
            String domain = trim(vo.getMaildomain());
            vo.setMaildomain(domain.isEmpty() ? "" : "@" + domain);
        }
        resJso.put("0", list0);

        // 推荐邮箱
        for (MailConfigVo vo : list1) {
            String domain = trim(vo.getMaildomain());
            vo.setMaildomain(domain.isEmpty() ? "" : "@" + domain);
        }
        resJso.put("1", list1);

        // 常用邮箱
        for (MailConfigVo vo : list2) {
            String domain = trim(vo.getMaildomain());
            vo.setMaildomain(domain.isEmpty() ? "" : "@" + domain);
        }
        resJso.put("2", list2);

        String resBody = ResponsePackUtil.encryptData(resJso.toJSONString(), userId);
        return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 客户端获取具体某个邮箱的配置信息
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月4日
     */
    public String getMailConfig(String requestBody, String userId) {

        logger.debug("获取推荐邮箱,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpid = trim(requestJson.getString("corpid"));
        String domain = trim(requestJson.getString("domain"));

        /** 校验参数 */
        if (domain.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1072, "");
        }

        Map<String, Object> conditions = new HashMap<String, Object>();

        conditions.put("EQ_maildomain", domain);
        if (!corpid.isEmpty()) {
            conditions.put("EQ_corpid", corpid);
        }
        List<MailConfigVo> list = mailconfigInterface.findAllMailConfig(conditions);

        if (null == list || list.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1071, "");
        }

        String resJso = JSONObject.toJSONString(list.get(0));

        String resBody = ResponsePackUtil.encryptData(resJso, userId);
        return ResponsePackUtil.buildPack("0000", resBody);

    }

    /**
     * trim
     * 
     * @param obj
     * @return
     * @author Jiangft 2016年5月19日
     */
    public static String trim(Object obj) {
        return (obj == null) ? "" : obj.toString().trim();
    }

}
