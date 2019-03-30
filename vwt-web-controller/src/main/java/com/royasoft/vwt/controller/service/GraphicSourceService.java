/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.Base64Util;
import com.royasoft.vwt.controller.util.BaseConstant;
import com.royasoft.vwt.controller.util.OperateImage;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.RoyaUtils;
import com.royasoft.vwt.controller.util.StringUtils;
import com.royasoft.vwt.controller.util.SystemUtils;
import com.royasoft.vwt.controller.util.ZxingUtil;
import com.royasoft.vwt.controller.util.upload.FastDFSUtil;
import com.royasoft.vwt.controller.util.upload.FileUploadUtil;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareInterface;
import com.royasoft.vwt.soa.business.square.api.vo.GraphicSourceVo;
import com.royasoft.vwt.soa.graphicpush.api.interfaces.GraphicSourceInterface;
import com.royasoft.vwt.soa.graphicpush.api.utils.Response;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 素材中心业务处理类
 *
 * @Author:wuyf
 * @Since:2016年6月6日
 */
@Scope("prototype")
@Service
public class GraphicSourceService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(GraphicSourceService.class);

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

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private SquareInterface squareInterface;

    @Autowired
    private GraphicSourceInterface graphicSourceInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.graphicSource_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                String ip = ""; // 定义访问的IP值
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String tel_number = queue_packet.getTel_number();
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    ip = SystemUtils.getIpAddrByRequest(request); // 获取到的IP地址
                    logger.debug("获取到的访问端IP为：{}", ip);
                    /***************************** 业务逻辑处理 *********************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果
                    switch (function_id) {
                        // 素材中心上传图片
                        case FunctionIdConstant.GRAPHICSOURCEUPLOAD:
                            resInfo = upload();
                            break;
                        // 保存素材
                        case FunctionIdConstant.INSERTGRAPHICSOURCE:
                            resInfo = insertGraphicsource(request_body);
                            break;
                        // 查询素材详情
                        case FunctionIdConstant.FINDGRAPHIC:
                            resInfo = findGraphic(request_body, ip);
                            break;
                        // 裁剪图片
                        case FunctionIdConstant.CUTGRAPHICPIC:
                            resInfo = cutGraphicPic(request_body, ip);
                            break;
                        // 获取素材列表(小v团队)
                        case FunctionIdConstant.GETGRAPHICSOURCELIST:
                            resInfo = getGraphicSourceList(request_body);
                            break;
                        // 获取素材列表
                        case FunctionIdConstant.GETNEWGRAPHICSOURCELIST:
                            resInfo = getNewGraphicSourceList(request_body);
                            break;
                        case FunctionIdConstant.DELETEGRAPHICSOURCE:
                            resInfo = deleteGraphicSource(request_body);
                            break;
                        case FunctionIdConstant.CREATEGRAPHICSOURCECODE:
                            resInfo = createGraphicSourceCode(request_body);
                            break;
                        case FunctionIdConstant.GETGRAPHICSOURCEPREVIEW:
                            resInfo = getGraphicSourcePreview(request_body);
                            break;
                        case FunctionIdConstant.GETGRAPHICSOURCECONTENT:
                            resInfo = getGraphicSourceContent(request_body);
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
                // channel.close();
            }
        }
    }

    /**
     * 图文编辑器上传图片
     * 
     * @return
     * @author Jiangft 2016年5月26日
     */
    public String upload() {
        logger.debug("图文编辑器上传图片");
        String filePath = "";
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            filePath = FileUploadUtil.uploadFile(msg);
            if (null == filePath || "".equals(filePath)) {
                model.put("error", 1);
                model.put("url", filePath);
                return JSONObject.toJSONString(model);
            }
        } catch (Exception e) {
            logger.error("上传文件失败", e);
            model.put("error", 1);
            model.put("url", filePath);
            return JSONObject.toJSONString(model);
        }
        String fastDFSNode = BaseConstant.fastDFSNode;
        String trackerAddr = "";
        try {
            trackerAddr = zkUtil.findData(fastDFSNode);
        } catch (Exception e) {
        }
        model.put("error", 0);
        model.put("url", trackerAddr + filePath);
        return JSONObject.toJSONString(model);
    }

    /**
     * 保存素材
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Wuyf 2016年5月4日
     */
    public String insertGraphicsource(String requestBody) {
        logger.debug("保存素材,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        JSONArray list = requestJson.getJSONArray("list");
        String sessionid = trim(requestJson.getString("sessionid"));
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        int roleId = 0;
        String corpId = "";
        String userId = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            roleId = Integer.parseInt(sessionJson.getString("roleId"));
            if (BaseConstant.ROLENAME_ADMIN_CORP == roleId||BaseConstant.ROLENAME_ADMIN_DEPT == roleId) {
                corpId = sessionJson.getString("corpId");
                userId = sessionJson.getString("userId");
            } else if (BaseConstant.ROLENAME_ADMIN_CUSTOMER == roleId) {
                corpId = "0";
                userId = sessionJson.getString("userId");
            } else if (BaseConstant.ROLENAME_ADMIN_SYSTEM == roleId || BaseConstant.ROLENAME_ADMIN_PLATFORM == roleId) {
                // 平台，系统管理员
                corpId = "-2";
                userId = sessionJson.getString("userId");
            } else {
                corpId = "-1";
                userId = sessionJson.getString("userCityArea");
            }
            logger.debug("页面提交素材信息,roleId:{},corpId:{},userId:{},list:{}", roleId, corpId, userId, list);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3051, "");
        }
        if (null == list || list.size() <= 0 || list.size() > 4)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3052, "");
        try {
            logger.debug("保存素材,list:{},corpId:{},userId:{}", list, corpId, userId);
            Object o = graphicSourceInterface.saveGraphicsource(list, corpId, userId);
            logger.debug("保存素材调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3065, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("保存素材请求服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3053, "");
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
     * 查询素材
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Wuyf 2016年6月13日
     */
    public String findGraphic(String requestBody, String ip) {
        logger.debug("查询素材详情修改,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String mainId = trim(requestJson.getString("mainId"));
        logger.debug("根据mainId查询素材详情进入修改页面,mainId:{}", mainId);
        if (null == mainId || "".equals(mainId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3054, "");
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            Map<String, Object> condition = new HashMap<String, Object>();
            condition.put("EQ_mainId", mainId);
            Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
            sortMap.put("id", true);
            Map<String, Object> m = squareInterface.findGraphicOfPage(1, 10, condition, sortMap);
            if (null != m && Integer.parseInt(m.get("total").toString()) > 0) {
                Map<String, Object> map = null;
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                @SuppressWarnings("unchecked")
                List<GraphicSourceVo> list1 = (List<GraphicSourceVo>) m.get("content");
                logger.debug("根据mainId查询素材详情list,mainId:{},list:{}", mainId, JSON.toJSONString(list));
                // 获取zk中内网IP开始段
                String zkInnerStartIp = "";
                try {
                    zkInnerStartIp = zkUtil.findData(BaseConstant.fastDFSInnerStartNode);
                } catch (Exception e) {
                }
                for (int i = 0; i < list1.size(); i++) {
                    map = new HashMap<String, Object>();
                    map.put("id", list1.get(i).getId());
                    map.put("graphicTitle", list1.get(i).getGraphicTitle());
                    // 图片路径
                    // 获取文件服务器路径
                    String src = "";
                    if (null == list1.get(i).getGraphicPic()) {
                    } else if (list1.get(i).getGraphicPic().startsWith("/group")) {
                        String trackerAddr = "";
                        try {
                            if (ip.startsWith(zkInnerStartIp) && StringUtils.stringIsNotNull(zkInnerStartIp))
                                trackerAddr = zkUtil.findData(BaseConstant.fastDFSInnerNode);
                            else
                                trackerAddr = zkUtil.findData(BaseConstant.fastDFSNode);
                        } catch (Exception e) {
                            logger.error("查询fastDFSNode端口异常", e);
                        }
                        src = trackerAddr + list1.get(i).getGraphicPic();
                    } else if (list1.get(i).getGraphicPic().startsWith("/image")) {
                        String trackerAddr = "";
                        try {
                            if (ip.startsWith(zkInnerStartIp) && StringUtils.stringIsNotNull(zkInnerStartIp))
                                trackerAddr = zkUtil.findData(BaseConstant.NGINX_INNER_ADDRESS);
                            else
                                trackerAddr = zkUtil.findData(BaseConstant.NGINX_ADDRESS);
                        } catch (Exception e) {
                            logger.error("查询NGINX_ADDRESS端口异常", e);
                        }
                        src = trackerAddr + list1.get(i).getGraphicPic();
                    }
                    map.put("graphicPic", src);
                    String graphicSourceType = list1.get(i).getGraphicSourceType();
                    map.put("graphicSourceType", graphicSourceType);
                    if (BaseConstant.GRAPHIC_SOURCE_TYPE_URL.equals(graphicSourceType)) {
                        map.put("customContent", list1.get(i).getConnectUrl());
                    } else {
                        map.put("customContent", list1.get(i).getCustomContent());
                    }
                    map.put("mainId", list1.get(i).getMainId());
                    list.add(map);
                }
                model.put("list", list);
                logger.debug("根据mainId查询素材详情,mainId:{},model", mainId, JSON.toJSONString(model));
                return ResponsePackUtil.buildPack("0000", model);
            } else {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3055, "");
            }
        } catch (Exception e) {
            logger.error("根据mainId查询素材详情进入修改页面异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3056, "");
        }
    }

    /**
     * 裁剪图片
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Wuyf 2016年6月13日
     */
    public String cutGraphicPic(String requestBody, String ip) {
        logger.debug("裁剪素材图片,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String graphicPic = trim(requestJson.getString("imgurl"));
        String x = trim(requestJson.getString("x"));
        String y = trim(requestJson.getString("y"));
        String w = trim(requestJson.getString("w"));
        String h = trim(requestJson.getString("h"));
        String maxw = trim(requestJson.getString("maxw"));
        String maxh = trim(requestJson.getString("maxh"));
        logger.debug("根据graphicPic路径下载图片并上传,graphicPic{}", graphicPic);
        if ("".equals(graphicPic) || "".equals(x) || "".equals(y) || "".equals(w) || "".equals(h) || "".equals(maxw) || "".equals(maxh))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3057, "");
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            String contextPath = RoyaUtils.getLaunchPath(GraphicSourceService.class);
            graphicPic = graphicPic.substring(graphicPic.indexOf("/group"));
            String type = graphicPic.substring(graphicPic.lastIndexOf("."));
            String type1 = graphicPic.substring(graphicPic.lastIndexOf(".") + 1);
            contextPath = contextPath.replace("\\", "/");
            String folderName = "/graphicPic/";
            File file1 = new File(contextPath + folderName);
            if (!file1.exists() && !file1.isDirectory()) {
                file1.mkdirs();
            }
            Long name = System.currentTimeMillis();
            File targetFile = new File(contextPath + folderName, name + type);
            byte[] fileBytesDown = FastDFSUtil.getFileBytesFromDFS1(graphicPic);
            if (null == fileBytesDown || fileBytesDown.length <= 0) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3058, "");
            }
            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.write(fileBytesDown);
            if (null != fos)
                fos.close();
            String path = contextPath + folderName + name + type;
            BufferedImage image = ImageIO.read(targetFile);
            int height = image.getHeight();
            int width = image.getWidth();
            // 根据比例计算实际裁剪尺寸
            int x1 = Integer.parseInt(x) * width / Integer.parseInt(maxw);
            int y1 = Integer.parseInt(y) * height / Integer.parseInt(maxh);
            int w1 = Integer.parseInt(w) * width / Integer.parseInt(maxw);
            int h1 = Integer.parseInt(h) * height / Integer.parseInt(maxh);
            // 根据宽度裁剪正方形图片
            OperateImage o = new OperateImage(x1, y1, w1, h1, type1);
            o.setSrcpath(path);
            o.setSubpath(path);
            o.cut();
            String fileUrlDfs = FastDFSUtil.uploadFile(path);// 上传文件服务器
            targetFile.delete();
            String trackerAddr = "";
            // 获取zk中内网IP开始段
            String zkInnerStartIp = "";
            try {
                zkInnerStartIp = zkUtil.findData(BaseConstant.fastDFSInnerStartNode);
            } catch (Exception e) {
            }
            try {
                if (ip.startsWith(zkInnerStartIp) && StringUtils.stringIsNotNull(zkInnerStartIp))
                    trackerAddr = zkUtil.findData(BaseConstant.fastDFSInnerNode);
                else
                    trackerAddr = zkUtil.findData(BaseConstant.fastDFSNode);
            } catch (Exception e) {
            }
            model.put("graphicPic", trackerAddr + fileUrlDfs);
            model.put("graphicPic1", fileUrlDfs);
            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.debug("裁剪素材图片异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3059, "");
        }
    }

    // 图文存在html给客户端使用
    public String contentToTxt(String fileForder, String fileName, String content) {
        String str = new String(); // 原有txt内容
        String s1 = new String();// 内容更新
        String end = new String();
        String fileUrlDfs = null;
        str = "<!DOCTYPE html><html><head><title>详情</title><meta http-equiv='keywords' content='keyword1,keyword2,keyword3'>" + "<meta http-equiv='description' content='this is my page'>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"><meta http-equiv='content-type' content='text/html; charset=utf-8'>"
                + "<style>img{width:100%; margin-top:2px;margin-bottom:2px;} .footer {margin:20px 0px 15px 20px;width: 90%;float:left;font-family:'Microsoft YaHei',微软雅黑;color:#666666;}</style></head><body><div style=\"width:96%; margin-left:2%;margin-left:2%;margin-top:0px\">";
        end = "</div><div class='footer'><font id='announceRecord' name='announceRecord' style='font-size:14px'>阅读人数</font></div></body></html>";
        try {
            String classPath = RoyaUtils.getLaunchPath(GraphicSourceService.class);
            String fileSourceFolder = classPath + fileForder;
            File file = new File(fileSourceFolder);
            if (!file.exists()) {
                file.mkdirs();// 创建目录
            }
            String filePath = fileSourceFolder + "/" + fileName;
            File f = new File(filePath);
            if (f.exists()) {
            } else {
                f.createNewFile();// 不存在则创建
            }
            s1 = str + content + end;
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "utf-8"));
            output.write(s1);
            output.close();
            fileUrlDfs = FastDFSUtil.uploadFile(filePath);// 上传文件服务器
            // 删除本地文件
            f.delete();
            return fileUrlDfs;
        } catch (Exception e) {
            logger.error("根据图文生成html异常：", e);
            return fileUrlDfs;
        }
    }

    /**
     * 获取素材列表(小v团队)
     * 
     * @return
     */
    public String getGraphicSourceList(String requestBody) {
        logger.debug("获取素材列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = trim(requestJson.getString("sessionid"));// omc sessionid
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        int roleId = 0;
        Map<String, Object> condition = new HashMap<String, Object>();
        try {
            sessionJson = JSONObject.parseObject(session);
            roleId = Integer.parseInt(sessionJson.getString("roleId"));
            if (BaseConstant.ROLENAME_ADMIN_CORP == roleId) {
                // 企业管理员
                condition.put("EQ_corpId", sessionJson.getString("corpId"));
            } else if (BaseConstant.ROLENAME_ADMIN_CUSTOMER == roleId) {
                // 客户经理
                condition.put("EQ_userID", sessionJson.getString("userId"));
                condition.put("EQ_corpId", "0");
            } else if (BaseConstant.ROLENAME_ADMIN_SYSTEM == roleId || BaseConstant.ROLENAME_ADMIN_PLATFORM == roleId) {
                // 平台，系统管理员
                condition.put("EQ_userID", sessionJson.getString("userId"));
                condition.put("EQ_corpId", "-2");
            } else {
                // 省，地市管理员
                condition.put("EQ_userID", sessionJson.getString("userCityArea"));
                condition.put("EQ_corpId", "-1");
            }
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3051, "");
        }
        String graphicTitle = trim(requestJson.getString("graphicTitle"));// 素材主标题(支持模糊查询)
        String page = trim(requestJson.getString("page"));// 页数
        String row = trim(requestJson.getString("row"));// 行数
        int pageIndex = 1;
        int pageSize = 10;
        try {
            if (!"".equals(page))
                pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3060, "");
        }
        try {
            if (!"".equals(row))
                pageSize = Integer.parseInt(row);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3060, "");
        }
        if (!"".equals(graphicTitle)) {
            condition.put("LIKE_graphicTitle", graphicTitle);
        }
        // 只查询素材主标题
        condition.put("EQ_isMain", BaseConstant.GRAPHIC_IS_MAIN);
        try {
            Map<String, Object> model = new HashMap<String, Object>();
            logger.debug("获取素材列表,condition:{},pageIndex:{},pageSize:{}", condition, pageIndex, pageSize);
            Map<String, Object> m = squareInterface.findGraphicOfPage(pageIndex, pageSize, condition, null);
            logger.debug("获取素材列表服务返回结果,response:{}", JSON.toJSONString(m));
            if (null == m) {
                model.put("total", 0);
                model.put("items", "");
                model.put("page", 1);
                model.put("pageNum", 1);
            } else {
                int total = Integer.parseInt(m.get("total").toString());
                model.put("total", total);
                model.put("page", pageIndex);
                model.put("pageNum", total % pageSize == 0 ? (total / pageSize) : (total / pageSize + 1));
                @SuppressWarnings("unchecked")
                List<GraphicSourceVo> list = (List<GraphicSourceVo>) m.get("content");
                List<Map<String, Object>> gridList = new ArrayList<Map<String, Object>>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (GraphicSourceVo gv : list) {
                    Map<String, Object> gdt = new HashMap<String, Object>();
                    gdt.put("mainId", gv.getMainId());
                    gdt.put("graphicTitle", gv.getGraphicTitle());
                    gdt.put("createTime", sdf.format(gv.getCreateTime()));
                    gridList.add(gdt);
                }
                model.put("items", gridList);
            }
            logger.debug("获取素材列表返回结果,response:{}", JSON.toJSONString(model));
            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.error("获取素材列表调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3061, "");
        }
    }

    /**
     * 获取素材列表
     * 
     * @return
     */
    public String getNewGraphicSourceList(String requestBody) {
        logger.debug("获取素材列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = trim(requestJson.getString("sessionid"));// omc sessionid
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        int roleId = 0;
        Map<String, Object> condition = new HashMap<String, Object>();
        try {
            sessionJson = JSONObject.parseObject(session);
            roleId = Integer.parseInt(sessionJson.getString("roleId"));
            if (BaseConstant.ROLENAME_ADMIN_CORP == roleId) {
                // 企业管理员
                condition.put("EQ_corpId", sessionJson.getString("corpId"));
            } else if (BaseConstant.ROLENAME_ADMIN_DEPT == roleId) {
                // 部门管理员
                condition.put("EQ_userID", sessionJson.getString("userId"));
                condition.put("EQ_corpId", sessionJson.getString("corpId"));
            } else if (BaseConstant.ROLENAME_ADMIN_CUSTOMER == roleId) {
                // 客户经理
                condition.put("EQ_userID", sessionJson.getString("userId"));
                condition.put("EQ_corpId", "0");
            } else if (BaseConstant.ROLENAME_ADMIN_SYSTEM == roleId || BaseConstant.ROLENAME_ADMIN_PLATFORM == roleId) {
                // 平台，系统管理员
                condition.put("EQ_userID", sessionJson.getString("userId"));
                condition.put("EQ_corpId", "-2");
            } else {
                // 省，地市管理员
                condition.put("EQ_userID", sessionJson.getString("userCityArea"));
                condition.put("EQ_corpId", "-1");
            }
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3051, "");
        }
        String graphicTitle = trim(requestJson.getString("graphicTitle"));// 素材主标题(支持模糊查询)
        String page = trim(requestJson.getString("index"));// 页数
        String row = trim(requestJson.getString("pagesize"));// 行数
        String ispush = trim(requestJson.getString("ispush"));// 推送状态 0未推送，1已推送
        int pageIndex = 1;
        int pageSize = 10;
        try {
            if (!"".equals(page))
                pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3060, "");
        }
        try {
            if (!"".equals(row))
                pageSize = Integer.parseInt(row);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3060, "");
        }
        if (!"".equals(graphicTitle)) {
            condition.put("LIKE_graphicTitle", graphicTitle);
        }
        if ("0".equals(ispush) || "1".equals(ispush)) {
            condition.put("EQ_ispush", ispush);
        }
        // 只查询素材主标题
        condition.put("EQ_isMain", BaseConstant.GRAPHIC_IS_MAIN);
        // 封装sortMap
        Map<String, Boolean> sortMap = new HashMap<>();
        sortMap.put("createTime", false);
        try {
            logger.debug("获取素材列表,condition:{},sortMap:{},pageIndex:{},pageSize:{}", JSON.toJSONString(condition), JSON.toJSONString(sortMap), pageIndex, pageSize);
            Object o = graphicSourceInterface.getGraphicSourceList(pageIndex, pageSize, condition, sortMap);
            logger.debug("获取素材列表服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3062, "");
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack(o, ser);
        } catch (Exception e) {
            logger.error("获取素材列表调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3061, "");
        }
    }

    /**
     * 素材删除
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String deleteGraphicSource(String requestBody) {
        logger.debug("删除素材,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String mainId = trim(requestJson.getString("mainId"));// 素材mainId
        if ("".equals(mainId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3063, "");
        try {
            logger.debug("根据mainId删除素材,mainId:{}", mainId);
            Object o = graphicSourceInterface.deleteGraphicById(mainId);
            logger.debug("根据mainId删除素材调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3064, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("根据mainId删除素材请求服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3067, "");
        }
    }

    /**
     * 生成图文内容二维码
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Wuyf 2016年8月10日
     */
    public String createGraphicSourceCode(String requestBody) {
        logger.debug("生成图文内容二维码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String customContent = requestJson.getString("customContent");// 图文内容
        String graphicTitle = requestJson.getString("graphicTitle");// 图文标题
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        String url = requestJson.getString("url");// 预览页面url
        if (StringUtils.isEmpty(customContent) || graphicTitle.length() > 100 || StringUtils.isEmpty(url))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3052, "");
        ByteArrayOutputStream out = null;
        try {
            logger.debug("保存图文预览内容,id:{},graphicTitle:{},customContent:{}", id, graphicTitle, customContent.length());
            Response o = graphicSourceInterface.saveGraphicsourcePreview(id, graphicTitle, customContent);
            logger.debug("保存图文预览内容请求服务返回结果,response:{}", JSON.toJSONString(o));
            if (!"0000".equals(o.getResponse_code())) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3065, "");
            }
            url = url + "?id=" + id;
            BufferedImage image = ZxingUtil.createImage(url, null);
            out = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", out);
            logger.debug("生成二维码图片流大小", out.size());
            return ResponsePackUtil.buildPack("0000", Base64Util.encodeBytes(out.toByteArray()));
        } catch (IOException e) {
            logger.error("生成二维码图片流异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3067, "");
        } catch (Exception e) {
            logger.error("生成图文内容二维码异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3067, "");
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 根据图文预览id获取预览内容
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Wuyf 2016年8月10日
     */
    public String getGraphicSourcePreview(String requestBody) {
        logger.debug("根据图文预览id获取预览内容,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String id = requestJson.getString("id");
        if (StringUtils.isEmpty(id))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3052, "");
        try {
            logger.debug("根据图文预览id获取预览内容,id:{}", id);
            Response o = graphicSourceInterface.getGraphicsourcePreview(id);
            logger.debug("根据图文预览id获取预览内容请求服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3068, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("根据图文预览id获取预览内容请求服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3067, "");
        }
    }

    /**
     * 根据素材id获取素材内容
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Wuyf 2016年8月10日
     */
    public String getGraphicSourceContent(String requestBody) {
        logger.debug("根据素材id获取素材内容,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String gid = requestJson.getString("gid");
        if (StringUtils.isEmpty(gid))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3052, "");
        Long graphicId = 0L;
        try {
            graphicId = Long.parseLong(gid);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3052, "");
        }
        try {
            logger.debug("根据素材id获取素材内容,id:{}", gid);
            Response o = graphicSourceInterface.getGraphicsourceContent(graphicId);
            logger.debug("根据素材id获取素材内容请求服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3069, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("根据素材id获取素材内容请求服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3067, "");
        }
    }
}
