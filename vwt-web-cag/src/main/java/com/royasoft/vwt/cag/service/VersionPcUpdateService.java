package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

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
import com.royasoft.vwt.soa.systemsettings.versionupdate.api.interfaces.VersionPcUpdateInterface;
import com.royasoft.vwt.soa.systemsettings.versionupdate.api.vo.PcVersionVo;

/**
 * pc版本更新处理类
 *
 * @Author:huangtao
 * @Since:2015年11月22日
 */
@Scope("prototype")
@Service
public class VersionPcUpdateService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(VersionPcUpdateService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    
    @Autowired
    private VersionPcUpdateInterface versionPcUpdateInterface;
    @Autowired
    private OperationLogService operationLogService;
  

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.versionPc_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                logger.debug("version_queue:{}", ServicesQueue.version_queue.size());
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("版本更新处理类(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /***************************** 业务逻辑处理 *********************************************/

                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果
                    switch (function_id) {
                        case FunctionIdConstant.VERSIONPCUPDATE:
                            resInfo = checkVersion(request_body);
                            break;
                        default:
                            break;
                    }
                    logger.debug("版本更新处理类(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("版本更新处理类异常", e);
                // 响应客户端异常
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }
        }
    }

    /**
     * 检查版本
     * 
     * @param requestBody
     * @return
     * @Description:
     */
    public String checkVersion(String requestBody) {
        logger.debug("检查版本,requestBody:{}", requestBody);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1031, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String versionName = requestJson.getString("versionName");// 待用
        logger.debug("检查版本(解析requestBody),requestBody:{},versionName:{}", requestBody, versionName);
        Map<String,Object> conditions= new HashMap<String, Object>();
        conditions.put("GT_pcVersion",versionName);
        /** 获取版本更新信息 */
        List<PcVersionVo> versionInfo = versionPcUpdateInterface.getPcVersionInfo(conditions);
        if (null == versionInfo || versionInfo.size() == 0)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1032, "");
        
        return ResponsePackUtil.buildPack("0000", JSONObject.toJSONString(versionInfo.get(0)));
    }

}
