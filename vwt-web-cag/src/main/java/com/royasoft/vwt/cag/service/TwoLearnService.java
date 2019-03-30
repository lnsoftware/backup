package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.soa.twolearn.api.interfaces.TwoLearnInterface;
import com.royasoft.vwt.soa.twolearn.api.utils.Response;
import com.royasoft.vwt.soa.twolearn.api.vo.TwoLearnDiscussVO;
import com.royasoft.vwt.soa.twolearn.api.vo.TwoLearnProgressVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;

/**
 * 二学一做模块
 *
 * @Since:2016年6月23日
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
    private OperationLogService operationLogService;

    @Autowired
    private ClientUserInterface clientUserInterface;

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
                    String function_id = queue_packet.getFunction_id();
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("二学一做业务模块(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        case FunctionIdConstant.twoLearn_saveVideoTime:
                            resInfo = saveVideoTime(request_body, user_id);
                            break;
                        case FunctionIdConstant.twoLearn_commentList:
                            resInfo = commentList(request_body, user_id);
                            break;
                        case FunctionIdConstant.twoLearn_saveComment:
                            resInfo = saveComment(request_body, user_id);
                            break;
                        case FunctionIdConstant.twoLearn_videoList:
                            resInfo = videoList(request_body, user_id);
                            break;
                        case FunctionIdConstant.twoLearn_videoDetail:
                            resInfo = videoDetail(request_body, user_id);
                            break;
                        default:
                            break;
                    }
                    logger.debug("二学一做业务模块(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponseH5(channel, resInfo);

                    // 添加日志
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponseH5(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("IMS通讯录业务模块异常", e);
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
     * 视频列表
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String videoList(String requestBody, String userId) {
        logger.debug("视频列表,requestBody:{},userId{}", requestBody, userId);
        Response response = twoLearnInterface.findVideoList(userId);
        logger.debug("视频列表返回{}", JSON.toJSONString(response));

        SerializeConfig ser = new SerializeConfig();
        ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
        return ResponsePackUtil.buildPack(response, ser);

    }

    /**
     * 视频详情
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String videoDetail(String requestBody, String userId) {
        logger.debug("视频详情,requestBody:{},userId{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String videoId = requestJson.getString("videoId");// 视频ID
        // 校验参数
        if (!StringUtils.stringIsNotNull(videoId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2901, "");
        }
        Response response = twoLearnInterface.findVideoDetail(videoId);
        logger.debug(" 视频详情返回{}", JSON.toJSONString(response));
        return JSON.toJSONString(response);
    }

    /**
     * 保存视频学习时长度
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    private String saveVideoTime(String requestBody, String userId) {
        logger.debug("保存观看视频时长,requestBody:{},userId", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String videoId = requestJson.getString("videoId");// 视频ID
        String studyTime = requestJson.getString("studyTime");// 学习时长
        // 校验参数
        if (!StringUtils.stringIsNotNull(videoId) || !StringUtils.stringIsNotNull(studyTime) || !StringUtils.stringIsNotNull(userId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2901, "");
        }
        // 用户是否存在
        TwoLearnProgressVo twoLearnProgressVo = new TwoLearnProgressVo();
        twoLearnProgressVo.setVideoId(videoId);
        twoLearnProgressVo.setUserId(userId);
        twoLearnProgressVo.setStudyTime(Long.parseLong(studyTime));
        Response response = twoLearnInterface.saveTwoLearnVideoProgress(twoLearnProgressVo);
        logger.debug("保存视频学习时长返回{}", JSON.toJSONString(response));
        return JSON.toJSONString(response);
    }

    /**
     * 保存 视频评论
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String saveComment(String requestBody, String userId) {
        logger.debug("视频保存评论,requestBody:{},userId{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String videoId = requestJson.getString("videoId");// 视频ID
        String content = requestJson.getString("content");// 评论内容
        // 参数校验
        if (!StringUtils.stringIsNotNull(userId) || !StringUtils.stringIsNotNull(videoId) || !StringUtils.stringIsNotNull(content)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2901, "");
        }
        // 保存
        TwoLearnDiscussVO twoLearnDiscussVO = new TwoLearnDiscussVO();
        twoLearnDiscussVO.setVideoId(videoId);
        twoLearnDiscussVO.setContent(content);
        twoLearnDiscussVO.setPersonId(userId);
        twoLearnDiscussVO.setTime(new Date());
        twoLearnDiscussVO.setType(1);
        Response response = twoLearnInterface.saveVideoComment(twoLearnDiscussVO);
        logger.debug("保存 视频评论返回{}", JSON.toJSONString(response));
        return JSON.toJSONString(response);
    }

    /**
     * 视频评论列表
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String commentList(String requestBody, String userId) {
        logger.debug("视频评论列表,requestBody:{},userId{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String videoId = requestJson.getString("videoId");// 视频ID
        String refreshFlag = requestJson.getString("refreshFlag");//1刷新，0不刷新
        String sort = requestJson.getString("sort");// 最小的一个排序
        String row = requestJson.getString("row"); // 显示多少条
        // 参数校验
        if (!StringUtils.stringIsNotNull(videoId) || !StringUtils.stringIsNotNull(refreshFlag) || !StringUtils.stringIsNotNull(sort)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2901, "");
        }
        int pageSize = 10;
        int flag = 1;
        long flagSort = 0L;
        try {
            flag = Integer.parseInt(refreshFlag);
            flagSort = Long.parseLong(sort);
            pageSize = Integer.parseInt(row);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2901, "");
        }

        SerializeConfig ser = new SerializeConfig();
        ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        Response response = twoLearnInterface.findVideoCommentList(videoId, flag, pageSize, flagSort);
        response.getResponse_body();
        logger.debug("视频评论列表返回{}", JSON.toJSONString(response));
        return ResponsePackUtil.buildPack(response, ser);
    }
}
