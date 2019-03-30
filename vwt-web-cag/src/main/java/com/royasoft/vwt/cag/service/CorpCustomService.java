/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
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
import com.royasoft.vwt.soa.uic.corpcustom.api.interfaces.CorpCustomInterface;
import com.royasoft.vwt.soa.uic.corpcustom.api.vo.CorpCustomVO;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

@Scope("prototype")
@Service
public class CorpCustomService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CorpCustomService.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private CorpCustomInterface corpCustomInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.corpCustom_queue.take();// 获取队列处理数据
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
                        case FunctionIdConstant.CORPCUSTOM:
                            resInfo = getCorpCustom(request_body, user_id);
                            break;
                        case FunctionIdConstant.CORPCUSTOMFLAG:
                            resInfo = getCorpCustomFlag(request_body, user_id);
                            break;
                        default:
                            break;
                    }
                    logger.error("resInfo------------------------>{}", resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("企业定制logo处理类异常", e);
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }

        }
    }

    /**
     * 获取企业定制logo
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月4日
     */
    public String getCorpCustom(String requestBody, String userId) {
        logger.debug("获取企业定制logo,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpid = requestJson.getString("corpid");
        /** 校验参数 */
        if (null == corpid || "".equals(corpid)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1081, "");
        }

        CorpCustomVO vo = corpCustomInterface.findCorpCustomById(corpid);

        if (null == vo) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1082, "");
        }
        JSONObject resJso = new JSONObject();

        String corpimg = null == vo.getCorpimg() ? "" : vo.getCorpimg();
        String deptimg = null == vo.getDeptimg() ? "" : vo.getDeptimg();
        Long updatetime = null == vo.getUpdatetime() ? 0 : vo.getUpdatetime().getTime();

        resJso.put("corp", corpimg);
        resJso.put("dept", deptimg);
        resJso.put("updatetime", updatetime);

        String resBody = ResponsePackUtil.encryptData(resJso.toJSONString(), userId);

        return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 获取用户表更新标志位
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月4日
     */
    public String getCorpCustomFlag(String requestBody, String userId) {
        logger.debug("获取用户表更新标志位,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpid = trim(requestJson.getString("corpid"));
        String updatetime = trim(requestJson.getString("updatetime"));
        /** 校验参数 */
        if (null == corpid || "".equals(corpid)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1083, "");
        }
        Long time = updatetime.isEmpty() ? 0 : Long.parseLong(updatetime);

        CorpCustomVO vo = corpCustomInterface.findCorpCustomById(corpid);
        JSONObject resJso = new JSONObject();
        if (null == vo) {
            resJso.put("logoflag", false);
            return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJso.toJSONString(), userId));
        }

        Long xx = vo.getUpdatetime() == null ? 0 : vo.getUpdatetime().getTime();
        if (xx > time) {
            resJso.put("logoflag", true);
        } else {
            resJso.put("logoflag", false);
        }

        String resBody = ResponsePackUtil.encryptData(resJso.toJSONString(), userId);

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
