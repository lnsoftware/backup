package com.royasoft.vwt.cag.service;

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
import com.royasoft.vwt.soa.business.urlmanage.api.interfaces.UrlManageInterface;
import com.royasoft.vwt.soa.business.urlmanage.api.vo.UrlManageVo;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

/**
 * 二维码url模块
 *
 * @Author:huangtao
 */
@Scope("prototype")
@Service
public class UrlManageService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(UrlManageService.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private UrlManageInterface urlManageInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.urlmanage_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id();
                    String user_id = ""; // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("二维码url业务模块(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        case FunctionIdConstant.GETURLMANAGE:
                        	//查询二维码url
                            resInfo = getUrlManage(request_body);
                            break;
                        default:
                            break;
                    }
                    logger.debug("查询二维码url业务模块(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponseH5(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponseH5(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("查询二维码url业务模块异常", e);
                ResponsePackUtil.cagHttpResponseH5(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }

        }

    }

    /**
     * 查询二维码url
     * 
     * @param requestBody
     * @return
     */
    public String getUrlManage(String requestBody) {
        logger.debug("查询二维码url,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpId = trim(requestJson.getString("corpId"));
        /** 校验参数 */
        if (corpId.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, "");
        }
        UrlManageVo vo = urlManageInterface.findByCorpId(corpId);

        return ResponsePackUtil.buildPack("0000", vo);
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
