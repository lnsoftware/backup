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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.mq.LogRocketMqUtil;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.systemsettings.versionupdate.api.interfaces.VersionupdateInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

/**
 * 版本更新处理类
 *
 * @Author:MB
 * @Since:2015年11月22日
 */
@Scope("prototype")
@Service
public class VersionUpdateService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(VersionUpdateService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private DatabaseInterface databaseInterface;

    @Autowired
    private VersionupdateInterface versionupdateInterface;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private MemberInfoUtil memberInfoUtil;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.version_queue.take();// 获取队列处理数据
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
                        case FunctionIdConstant.VERSIONUPDATE:
                            resInfo = checkVersion(request_body);
                            break;
                        case FunctionIdConstant.VERSIONUPDATENEW:
                            resInfo = checkVersionNew(request_body);
                            break;
                        case FunctionIdConstant.ZT_DOWNLOAD:
                            resInfo = vwtDownloadCount(request_body);
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
        String type = requestJson.getString("type");// 客户端类型
        String versionName = requestJson.getString("versionName");// 待用
        logger.debug("检查版本(解析requestBody),requestBody:{},type:{}", requestBody, type);
        if (!StringUtils.checkParamNull(type) || (!type.equalsIgnoreCase("IOS") && (!type.equalsIgnoreCase("ANDROID")) && (!type.equalsIgnoreCase("AppStoreVer"))))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1031, "");
        /** 获取客户端类型 */
        String clientType = "";
        if (type.equalsIgnoreCase("ANDROID"))
            clientType = "android";
        else if (type.equalsIgnoreCase("AppStoreVer"))
            clientType = "appstore";
        else if (type.equalsIgnoreCase("ios"))
            clientType = "ios";
        else
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1031, "");

        /** 获取版本更新信息 */
        Map<String, String> versionInfo = versionupdateInterface.getVersionInfo(clientType);
        versionInfo.put("appStore_enforce", ParamConfig.appstore_enforce);
        if (null == versionInfo || versionInfo.size() == 0)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1032, "");

        if (clientType.equalsIgnoreCase("android")) {
            if (null != versionName && !"".equals(versionName)) {
                String[] verNames = versionName.split("\\.");
                if (verNames.length == 2) {
                    String verNew = versionInfo.get("ver");
                    if (null != verNew && !"".equals(verNew)) {
                        String[] verNews = verNew.split("\\.");
                        if (verNews.length == 3) {
                            versionInfo.put("ver", verNews[0] + "." + verNews[1] + verNews[2]);
                        }
                    }
                }
            }
        }

        return ResponsePackUtil.buildPack("0000", JSONObject.toJSONString(versionInfo));
    }

    /**
     * 检查版本
     * 
     * @param requestBody
     * @return
     * @Description:
     */
    public String checkVersionNew(String requestBody) {
        logger.debug("---新的检查版本更新---,requestBody:{}", requestBody);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1031, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String type = requestJson.getString("type");// 客户端类型
        String telNum = requestJson.getString("telNum");// 客户端类型
        String versionName = requestJson.getString("versionName");// 待用
        logger.debug("检查版本(解析requestBody),requestBody:{},type:{}", requestBody, type);
        if (!StringUtils.checkParamNull(type) || (!type.equalsIgnoreCase("IOS") && (!type.equalsIgnoreCase("ANDROID")) && (!type.equalsIgnoreCase("AppStoreVer"))))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1031, "");
        /** 获取客户端类型 */
        String clientType = "";
        if (type.equalsIgnoreCase("ANDROID"))
            clientType = "android";
        else if (type.equalsIgnoreCase("AppStoreVer"))
            clientType = "appstore";
        else if (type.equalsIgnoreCase("ios"))
            clientType = "ios";
        else
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1031, "");

        List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(telNum);// memberInfoInterface.findByTelNum(telNum);
        Map<String, String> versionInfo = new HashMap<String, String>();
        if (null == memberInfoVOs || memberInfoVOs.isEmpty()) {
            versionInfo = versionupdateInterface.getVersionInfoNew(clientType, null, telNum);
        } else {
            // String memberId = memberInfoVOs.get(0).getMemId();
            List<String> corpIdList = new ArrayList<String>();
            for (MemberInfoVO memberInfoVO : memberInfoVOs) {
                corpIdList.add(memberInfoVO.getCorpId());
            }
            /** 获取版本更新信息 */
            versionInfo = versionupdateInterface.getVersionInfoNew(clientType, corpIdList, telNum);
        }

        logger.debug("新版本更新,versionInfo:{}", JSON.toJSONString(versionInfo));
        if (null == versionInfo || versionInfo.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1032, "");

        if (clientType.equalsIgnoreCase("android")) {
            if (null != versionName && !"".equals(versionName)) {
                String[] verNames = versionName.split("\\.");
                logger.debug("新版本更新,verNames:{}", verNames.length);
                if (verNames.length == 2) {
                    String verNew = versionInfo.get("ver");
                    if (null != verNew && !"".equals(verNew)) {
                        String[] verNews = verNew.split("\\.");
                        logger.debug("新版本更新,verNews:{}", verNews.length);
                        if (verNews.length == 3) {
                            versionInfo.put("ver", verNews[0] + "." + verNews[1] + verNews[2]);
                        }
                    }
                }
            }
        }

        return ResponsePackUtil.buildPack("0000", JSONObject.toJSONString(versionInfo));
    }

    /**
     * 掌厅下载计数
     * 
     * @param requestBody
     * @return
     * @Description:
     */
    public String vwtDownloadCount(String requestBody) {
        logger.debug("掌厅下载计数,requestBody:{}", requestBody);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String clientType = requestJson.getString("clientType");// 客户端类型
        String ditch = requestJson.getString("ditch");// 来源渠道
        logger.debug("掌厅下载计数(解析requestBody),clientType:{},ditch:{}", clientType, ditch);
        if (!StringUtils.checkParamNull(clientType, ditch) || (!clientType.equalsIgnoreCase("android") && !clientType.equalsIgnoreCase("ios")))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        LogRocketMqUtil.send(LogRocketMqUtil.vwtDownloadQueue, requestJson.toJSONString());

        return ResponsePackUtil.buildPack("0000", "");
    }
}
