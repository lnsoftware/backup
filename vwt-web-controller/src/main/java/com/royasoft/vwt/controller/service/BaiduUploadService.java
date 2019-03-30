/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.BaseConstant;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.upload.FileUploadUtil;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

@Scope("prototype")
@Service
public class BaiduUploadService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(BaiduUploadService.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ZkUtil zkUtil;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.baiduUpload_queue.take();// 获取队列处理数据
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
                        // 图文编辑器上传图片
                        case FunctionIdConstant.BAIDUUPLOAD:
                            resInfo = twUpload(request_body);
                            break;
                        default:
                            break;
                    }
                    if (FunctionIdConstant.BAIDUUPLOAD.equals(function_id)) {// 图文上传
                        ResponsePackUtil.cagHttpStringResponse(channel, resInfo);
                        String responseStatus = ResponsePackUtil.getResCode(resInfo);
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    } else {
                        ResponsePackUtil.cagHttpResponse(channel, resInfo);
                        String responseStatus = ResponsePackUtil.getResCode(resInfo);
                        if (null != responseStatus && !"".equals(responseStatus)) {
                            operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                        }
                    }

                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("百度富文本处理类异常", e);
            } finally {

            }

        }
    }

    /**
     * 图文编辑器上传图片
     * 
     * @return
     * @author Jiangft 2016年5月26日
     */
    public String twUpload(String requestBody) {
        logger.debug("图文编辑器上传图片,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String callBackJson = "{'imageActionName':'uploadimage','imageFieldName':'image','imageMaxSize':2048000,'imageAllowFiles':['.png','.jpg','.jpeg','.gif','.bmp'],'imageCompressEnable':true,'imageCompressBorder':1600,'imageInsertAlign':'none','imageUrlPrefix':'','imagePathFormat':'/server/ueditor/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}','scrawlActionName':'uploadscrawl','scrawlFieldName':'upfile','scrawlPathFormat':'/server/ueditor/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}','scrawlMaxSize':2048000,'scrawlUrlPrefix':'','scrawlInsertAlign':'none','snapscreenActionName':'uploadimage','snapscreenPathFormat':'/server/ueditor/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}','snapscreenUrlPrefix':'','snapscreenInsertAlign':'none','catcherLocalDomain':['127.0.0.1','localhost','img.baidu.com'],'catcherActionName':'catchimage','catcherFieldName':'source','catcherPathFormat':'/server/ueditor/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}','catcherUrlPrefix':'','catcherMaxSize':2048000,'catcherAllowFiles':['.png','.jpg','.jpeg','.gif','.bmp'],'videoActionName':'uploadvideo','videoFieldName':'upfile','videoPathFormat':'/server/ueditor/upload/video/{yyyy}{mm}{dd}/{time}{rand:6}','videoUrlPrefix':'','videoMaxSize':102400000,'videoAllowFiles':['.flv','.swf','.mkv','.avi','.rm','.rmvb','.mpeg','.mpg','.ogg','.ogv','.mov','.wmv','.mp4','.webm','.mp3','.wav','.mid'],'fileActionName':'uploadfile','fileFieldName':'upfile','filePathFormat':'/server/ueditor/upload/file/{yyyy}{mm}{dd}/{time}{rand:6}','fileUrlPrefix':'','fileMaxSize':51200000,'fileAllowFiles':['.png','.jpg','.jpeg','.gif','.bmp','.flv','.swf','.mkv','.avi','.rm','.rmvb','.mpeg','.mpg','.ogg','.ogv','.mov','.wmv','.mp4','.webm','.mp3','.wav','.mid','.rar','.zip','.tar','.gz','.7z','.bz2','.cab','.iso','.doc','.docx','.xls','.xlsx','.ppt','.pptx','.pdf','.txt','.md','.xml'],'imageManagerActionName':'listimage','imageManagerListPath':'/server/ueditor/upload/image/','imageManagerListSize':20,'imageManagerUrlPrefix':'','imageManagerInsertAlign':'none','imageManagerAllowFiles':['.png','.jpg','.jpeg','.gif','.bmp'],'fileManagerActionName':'listfile','fileManagerListPath':'/server/ueditor/upload/file/','fileManagerUrlPrefix':'','fileManagerListSize':20,'fileManagerAllowFiles':['.png','.jpg','.jpeg','.gif','.bmp','.flv','.swf','.mkv','.avi','.rm','.rmvb','.mpeg','.mpg','.ogg','.ogv','.mov','.wmv','.mp4','.webm','.mp3','.wav','.mid','.rar','.zip','.tar','.gz','.7z','.bz2','.cab','.iso','.doc','.docx','.xls','.xlsx','.ppt','.pptx','.pdf','.txt','.md','.xml']}";

        String callback = requestJson.getString("callback");
        logger.debug("callback————>:{}", callback);
        String image = requestJson.getString("image");
        logger.debug("image————>:{}", image);
        String filePath = "";
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            filePath = FileUploadUtil.uploadFile(msg);
        } catch (Exception e) {
            logger.error("上传文件失败", e);
            return callBackJson;
        }
        if (StringUtils.isEmpty(filePath)) {
            return callBackJson;
        }
        String hzm = filePath.substring(filePath.lastIndexOf(".") + 1);
        String fastDFSNode = BaseConstant.fastDFSNode;
        String trackerAddr = "";
        try {
            trackerAddr = zkUtil.findData(fastDFSNode);
        } catch (Exception e) {
            logger.error("获取" + fastDFSNode + "节点异常e:{}", e);
        }

        model.put("original", image);
        model.put("name", image);
        model.put("url", trackerAddr + filePath);
        model.put("state", "SUCCESS");
        model.put("type", hzm);

        return JSONObject.toJSONString(model);
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
