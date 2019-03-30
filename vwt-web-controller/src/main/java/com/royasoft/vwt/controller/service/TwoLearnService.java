/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.BaseConstant;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.upload.FastDFSUtil;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.twolearn.api.interfaces.TwoLearnInterface;
import com.royasoft.vwt.soa.twolearn.api.vo.TwoLearnVideoVo;

/**
 * 两学一做业务处理类
 *
 * @Author:wuyf
 * @Since:2016年6月24日
 */
@Scope("prototype")
@Service
public class TwoLearnService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TwoLearnService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private ZkUtil zkUtil;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private TwoLearnInterface twoLearnInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.twoLearn_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String tel_number = queue_packet.getTel_number();
                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    /***************************** 业务逻辑处理 *********************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        // 分页展示视频统计
                        case FunctionIdConstant.GETVIDEOLEARNDETAIL:
                            resInfo = getVideoLearnDetail(request_body);
                            break;
                        // 分页展示已看视频，已看完，企业所有人员
                        case FunctionIdConstant.GETVIDEOLEARNLIST:
                            resInfo = getVideoLearnList(request_body);
                            break;
                        // 导出视频统计
                        case FunctionIdConstant.EXPORTVIDEOSTATISTICS:
                            resInfo = exportVideoStatistics(request_body);
                            break;
                        // 分页展示视频列表
                        case FunctionIdConstant.GETVIDEOLIST:
                            resInfo = getVideoList(request_body);
                            break;
                        // 新增视频信息
                        case FunctionIdConstant.INSERTVIDEO:
                            resInfo = insertVideo(request_body);
                            break;
                        // 修改视频信息
                        case FunctionIdConstant.UPDATEVIDEO:
                            resInfo = updateVideo(request_body);
                            break;
                        // 获取视频详细信息
                        case FunctionIdConstant.GETVIDEO:
                            resInfo = getVideo(request_body);
                            break;
                        // 刷新视频状态时间
                        case FunctionIdConstant.FRESHENVIDEO:
                            resInfo = freshenVideo(request_body);
                            break;
                        case FunctionIdConstant.UPDATEVIDEOCOMPLETESTATE:
                            resInfo = updateVideoCompletestate(request_body);
                            break;
                        case FunctionIdConstant.UPDATEVIDEOPUBLISHSTATE:
                            resInfo = updateVideoPublishstate(request_body);
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
                    // 响应成功
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("素材中心业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
                channel.close();
            }
        }
    }

    /**
     * 分页展示视频统计
     * 
     * @return
     */
    public String getVideoLearnDetail(String requestBody) {
        logger.debug("分页展示视频统计,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = trim(requestJson.getString("sessionid"));
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpid = "";
         try {
         sessionJson = JSONObject.parseObject(session);
         corpid = sessionJson.getString("corpId");
         logger.debug("corpid:{}", corpid);
         } catch (Exception e) {
         logger.error("获取session--------->", session);
         logger.error("获取corpid报错", e);
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
         }

        String videoTitle = trim(requestJson.getString("videoTitle"));
        String page = trim(requestJson.getString("page"));
        String row = trim(requestJson.getString("row"));
        int pageIndex = 1;
        int pageSize = 10;
        try {
            pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
        }
        try {
            pageSize = Integer.parseInt(row);
        } catch (Exception e) {
        }
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(twoLearnInterface.findVideoStatisticsOfPage(pageIndex, pageSize, videoTitle, corpid, null));
        } catch (Exception e) {
            logger.error("分页展示视频统计调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
    }

    /**
     * 分页展示已看视频，已看完，企业所有人员
     * 
     * @return
     */
    public String getVideoLearnList(String requestBody) {
        logger.debug("分页展示已看视频，已看完，企业所有人员,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = trim(requestJson.getString("sessionid"));
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpid = "";
         try {
         sessionJson = JSONObject.parseObject(session);
         corpid = sessionJson.getString("corpId");
         logger.debug("corpid:{}", corpid);
         } catch (Exception e) {
         logger.error("获取session--------->", session);
         logger.error("获取corpid报错", e);
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
         }
        String type = trim(requestJson.getString("type"));
        String userName = trim(requestJson.getString("userName"));
        String videoId = trim(requestJson.getString("videoId"));
        String userTel = trim(requestJson.getString("userTel"));
        String page = trim(requestJson.getString("page"));
        String row = trim(requestJson.getString("row"));
        String completestate = trim(requestJson.getString("completestate"));
        if ("".equals(videoId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        int pageIndex = 1;
        int pageSize = 10;
        try {
            pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
        }
        try {
            pageSize = Integer.parseInt(row);
        } catch (Exception e) {
        }
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            if ("1".equals(type)) {
                // 已看人员列表
                return ResponsePackUtil.buildPack(twoLearnInterface.findViewVideoProgressOfPage(pageIndex, pageSize, corpid, userName, userTel, videoId, completestate, null));
            } else if ("2".equals(type)) {
                // 已看完人员列表
                return ResponsePackUtil.buildPack(twoLearnInterface.findCompleteVideoProgressOfPage(pageIndex, pageSize, corpid, userName, userTel, videoId, null));
            } else if ("3".equals(type)) {
                // 企业所有人
                return ResponsePackUtil.buildPack(twoLearnInterface.findCorpVideoProgressOfPage(pageIndex, pageSize, videoId, corpid, userName, userTel, completestate, null));
            } else {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
            }
        } catch (Exception e) {
            logger.error("分页展示已看视频，已看完，企业所有人员调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
    }

    /**
     * 导出视频统计
     * 
     * @return
     */
    public String exportVideoStatistics(String requestBody) {
        logger.debug("已看视频，已看完，企业所有人员导出视频统计,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = trim(requestJson.getString("sessionid"));
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpid = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            corpid = sessionJson.getString("corpId");
            logger.debug("corpid:{}", corpid);
        } catch (Exception e) {
            logger.error("获取session--------->", session);
            logger.error("获取corpid报错", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        }
        String type = trim(requestJson.getString("type"));
        String userName = trim(requestJson.getString("userName"));
        String videoId = trim(requestJson.getString("videoId"));
        String completestate = trim(requestJson.getString("completestate"));
        if ("".equals(videoId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        String userTel = trim(requestJson.getString("userTel"));
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            byte[] b = null;
            if ("1".equals(type)) {
                // 已看人员列表
                b = twoLearnInterface.ViewVideoProgressExport(corpid, userName, userTel, videoId, completestate);
            } else if ("2".equals(type)) {
                // 已看完人员列表
                b = twoLearnInterface.CompleteVideoProgressExport(corpid, userName, userTel, videoId);
            } else if ("3".equals(type)) {
                // 企业所有人
                b = twoLearnInterface.corpVideoProgressExport(videoId, corpid, userName, userTel, completestate);
            } else {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
            }
            if (null == b)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3103, "");
            String url = FastDFSUtil.uploadFile(b, "xls");
            String trackerAddr = zkUtil.findData(BaseConstant.fastDFSNode);
            return ResponsePackUtil.buildPack("0000", trackerAddr + url);
        } catch (Exception e) {
            logger.error("已看视频，已看完，企业所有人员导出视频统计调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
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

    /**
     * 分页展示视频列表
     * 
     * @return
     */
    public String getVideoList(String requestBody) {
        logger.debug("分页展示视频列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String videoTitle = trim(requestJson.getString("videoTitle"));
        String page = trim(requestJson.getString("page"));
        String row = trim(requestJson.getString("row"));
        int pageIndex = 1;
        int pageSize = 10;
        try {
            if (!"".equals(page))
                pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
        }
        try {
            if (!"".equals(pageSize))
                pageSize = Integer.parseInt(row);
        } catch (Exception e) {
        }
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(twoLearnInterface.findVideoOfPage(pageIndex, pageSize, videoTitle, null));
        } catch (Exception e) {
            logger.error("分页展示视频列表调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
    }

    /**
     * 新增视频信息
     * 
     * @return
     */
    public String insertVideo(String requestBody) {
        logger.debug("新增视频信息,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String videoTitle = trim(requestJson.getString("videoTitle"));
        String videoImgUrl = trim(requestJson.getString("videoImgUrl"));
        String videoTimeSpent = trim(requestJson.getString("videoTimeSpent"));
        String videoExplain = trim(requestJson.getString("videoExplain"));
        String videoType = trim(requestJson.getString("videoType"));
        if ("".equals(videoTitle) || "".equals(videoTimeSpent) || "".equals(videoType))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        if (0 <= videoImgUrl.indexOf("/group")) {
            videoImgUrl = videoImgUrl.substring(videoImgUrl.indexOf("/group"));
        }
        long date = 0l;
        try {
            date = Long.parseLong(videoTimeSpent);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        }
        try {
            TwoLearnVideoVo vo = new TwoLearnVideoVo();
            vo.setVideoTitle(videoTitle);
            vo.setVideoImgUrl(videoImgUrl);
            vo.setVideoTimeSpent(date);
            vo.setVideoExplain(videoExplain);
            vo.setPublishState("1");
            vo.setVideoType(videoType);
            vo.setCompleteState("0");
            vo.setTransCoding("0");
            vo.setVideoCreatetime(new Date());
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack(twoLearnInterface.addVideo(vo));
        } catch (Exception e) {
            logger.error("新增视频信息调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
    }

    /**
     * 修改视频信息
     * 
     * @return
     */
    public String updateVideo(String requestBody) {
        logger.debug("修改视频信息,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String videoTitle = trim(requestJson.getString("videoTitle"));
        String videoImgUrl = trim(requestJson.getString("videoImgUrl"));
        String videoId = trim(requestJson.getString("videoId"));
        String videoExplain = trim(requestJson.getString("videoExplain"));
        if ("".equals(videoTitle))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        if (0 <= videoImgUrl.indexOf("/group")) {
            videoImgUrl = videoImgUrl.substring(videoImgUrl.indexOf("/group"));
        }

        try {
            TwoLearnVideoVo vo = twoLearnInterface.findVideoVoByVideoId(videoId);
            vo.setVideoTitle(videoTitle);
            vo.setVideoImgUrl(videoImgUrl);
            vo.setVideoExplain(videoExplain);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack(twoLearnInterface.saveVideo(vo));
        } catch (Exception e) {
            logger.error("修改视频信息调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
    }

    /**
     * 获取单个视频信息
     * 
     * @return
     */
    public String getVideo(String requestBody) {
        logger.debug("获取单个视频信息,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String videoId = trim(requestJson.getString("videoId"));
        if ("".equals(videoId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(twoLearnInterface.findVideoByVideoId(videoId));
        } catch (Exception e) {
            logger.error("获取单个视频信息调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
    }

    /**
     * 刷新视频状态时间
     * 
     * @return
     */
    public String freshenVideo(String requestBody) {
        logger.debug("获取单个视频信息,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String videoId = trim(requestJson.getString("videoId"));
        if ("".equals(videoId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(twoLearnInterface.updateVideoUpdateTime(videoId));
        } catch (Exception e) {
            logger.error("获取单个视频信息调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
    }

    /**
     * 修改视频上传状态
     * 
     * @return
     */
    public String updateVideoCompletestate(String requestBody) {
        logger.debug("获取单个视频信息,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String videoId = trim(requestJson.getString("videoId"));
        if ("".equals(videoId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        try {
            TwoLearnVideoVo vo = twoLearnInterface.findVideoVoByVideoId(videoId);
            vo.setCompleteState("1");
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack(twoLearnInterface.saveVideo(vo));
        } catch (Exception e) {
            logger.error("获取单个视频信息调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
    }

    /**
     * 修改视频是否发布
     * 
     * @return
     */
    public String updateVideoPublishstate(String requestBody) {
        logger.debug("获取单个视频信息,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String videoId = trim(requestJson.getString("videoId"));
        if ("".equals(videoId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3101, "");
        try {
            TwoLearnVideoVo vo = twoLearnInterface.findVideoVoByVideoId(videoId);
            vo.setPublishState("0");
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack(twoLearnInterface.saveVideo(vo));
        } catch (Exception e) {
            logger.error("获取单个视频信息调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3102, "");
        }
    }

}
