/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.Date;
import java.util.UUID;

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
import com.royasoft.vwt.soa.business.squeareFeedback.api.interfaces.SqueareFeedbackInterface;
import com.royasoft.vwt.soa.business.squeareFeedback.api.vo.SqueareFeedbackVo;

/**
 * 工作圈业务处理
 * 
 * @author ZHOUKQ
 * @Since:2016年03月1日
 */
@Scope("prototype")
@Service
public class SquareFeedbackService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SquareFeedbackService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;
    /** 服务号反馈接口 */
    @Autowired
    private SqueareFeedbackInterface squeareFeedbackInterface;

    @Autowired
    private OperationLogService operationLogService;
    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.sqfeedback_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID

                    String user_id = queue_packet.getUser_id(); // 获取用户ID

                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    String tel_number = queue_packet.getTel_number();

                    /**************************** 业务逻辑处理 *****************************************/

                    String res = "";// 响应结果
                    
                    if (function_id == null || function_id.length() <= 0){   
                        ResponsePackUtil.CalibrationParametersFailure(channel, "服务号反馈请求参数校验失败！");
                    } else {
                        // 工作圈具体业务分层跳转
                        res = sqfeedbackLayer(channel, request, function_id, user_id,tel_number, request_body, msg);
                    }
                    // 响应成功
                    ResponsePackUtil.responseStatusOK(channel, res);
                    // String responseStatus = ResponsePackUtil.getResCode(res);
                    // if (null != responseStatus && !"".equals(responseStatus))
                    operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", "0000");
                }
            } catch (Exception e) {
                logger.error("服务号反馈业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }
        }
    }

   
    
    
    /**
     * 服务号反馈分块请求
     * 
     * @param function_id
     * @param user_id
     * @param request_body
     * @param msg
     * @return
     */
    private String sqfeedbackLayer(Channel channel, HttpRequest request, String function_id, String user_id,String tel_number, String request_body, Object msg) {
        String res = "";
        switch (function_id) {
            case FunctionIdConstant.SQUEAREFEEDBACKSAVE:// 获取服务号反馈列表
                res = saveSqfeedbackInfo(user_id,tel_number, request_body);
                break;
            default:
                res = ResponsePackUtil.returnFaileInfo(); // 未知请求
        }
        return res;
    }

   
    /**
     * 保存服务号反馈信息
     * @return
     */
    private String saveSqfeedbackInfo(String user_id,String tel_number, String request_body) {
        SqueareFeedbackVo questionFeedBackVO = new SqueareFeedbackVo();
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String fkId=UUID.randomUUID().toString().replace("-", "");
        String squareName = requestJson.getString("squareName");
        String squareId = requestJson.getString("squareId");
        String question = requestJson.getString("question");
        String content = requestJson.getString("content");
        String membername = requestJson.getString("membername");
        String telNum = requestJson.getString("telNum");
        String memId = requestJson.getString("memId");
        questionFeedBackVO.setFkId(fkId);
        questionFeedBackVO.setSquareName(squareName);
        questionFeedBackVO.setSquareId(squareId);
        questionFeedBackVO.setMemId(memId);
        questionFeedBackVO.setTelNum(telNum);
        questionFeedBackVO.setMembername(membername);
        questionFeedBackVO.setQuestion(question);
        questionFeedBackVO.setContent(content);
        questionFeedBackVO.setFkFlag(0);
        questionFeedBackVO.setDelFlag(0);
        questionFeedBackVO.setQuestionDate(new Date());
        questionFeedBackVO.setCreateTime(new Date());
        
        SqueareFeedbackVo questionFeedBackresult = null;
        try {
            questionFeedBackresult = squeareFeedbackInterface.save(questionFeedBackVO);
        } catch (Exception e) {
            logger.error("进行反馈问题时异常", e);
        }

        if (null != questionFeedBackresult) {
        	 return ResponsePackUtil.buildPack("0000", "");
        } else {
        	return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL92024, "");
        }
      
    }

   

}
