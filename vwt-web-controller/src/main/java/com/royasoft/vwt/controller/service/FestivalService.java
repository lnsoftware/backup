/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.festival.api.interfaces.FestivalInterface;
import com.royasoft.vwt.soa.business.festival.api.vo.FestivalVo;

/**
 * 节日欢迎图处理类
 *
 * @Author:huangs
 * @Since:2016年8月23日
 */
@Scope("prototype")
@Service
public class FestivalService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(FestivalService.class);

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
    private FestivalInterface festivalInterface;
    

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.festival_queue.take();// 获取队列处理数据
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
                        // 分页查询所有的欢迎图
                        case FunctionIdConstant.FINDFESTIVAL:
                            resInfo = findfestivalForPage(request_body);
                            break;
                        // 添加欢迎图
                        case FunctionIdConstant.FESTIVALSAVE:
                            resInfo = addFestival(request_body);
                            break;
                        // 删除欢迎图
                        case FunctionIdConstant.DELETEFESTIVAL:
                            resInfo = deleteFestival(request_body);
                            break;
                        // 修改欢迎图    
                        case FunctionIdConstant.UPDATEFESTIVAL:
                            resInfo = updateFestival(request_body);
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
                logger.error("节日欢迎图业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
                channel.close();
            }
        }
    }

    /**
     * 分页查询所有的欢迎图
     * 
     * @return
     */
    public String findfestivalForPage(String requestBody) {
        logger.debug("分页显示欢迎图,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9901, "");
        }
        Map<String, Object> conditions = new HashMap<String, Object>();
        String festivalId = trim(requestJson.getString("festivalId"));
        if(null!=festivalId&&!"".equals(festivalId)){
            conditions.put("EQ_festivalId", festivalId);
        }
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
            Map<String, Object> map = festivalInterface.findFestivalByPage(pageIndex, pageSize, conditions, null);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, changeFestival(map));
        } catch (Exception e) {
            logger.error("分页显示欢迎图调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9902, "");
        }
    }
    
    /**
     * 转换欢迎图时间，地址给h5
     * 
     * @return
     */
   @SuppressWarnings("unchecked")
   public  Map<String, Object>  changeFestival(Map<String, Object> map){
       Map<String, Object>  newMap=new HashMap<String, Object>();
       List<Map<String, Object>>  feslist=new ArrayList<Map<String,Object>>();
       SimpleDateFormat  sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       String fastDFSNode = BaseConstant.fastDFSNode;
       String trackerAddr = "";
       try {
           trackerAddr = zkUtil.findData(fastDFSNode);
       } catch (Exception e) {
       }
       if(null!=map){
           newMap.put("total", map.get("total"));
           List<FestivalVo> volist=(List<FestivalVo>) map.get("content");
           if(null!=volist&&!volist.isEmpty()){
               for(int i=0;i<volist.size();i++){
                try{
                    Map<String, Object>  festival=new HashMap<String, Object>();
                    festival.put("festivalId", volist.get(i).getFestivalId());
                    festival.put("festivalName", volist.get(i).getFestivalName());
                    festival.put("beginTime",sdf.format(volist.get(i).getBeginTime()));
                    festival.put("endTime",sdf.format(volist.get(i).getEndTime()));
                    festival.put("festivalTime",sdf.format(volist.get(i).getFestivalTime()));
                    festival.put("festivalImg", volist.get(i).getFestivalImg());
                    festival.put("allImgPath", trackerAddr+volist.get(i).getFestivalImg());
                    festival.put("type", volist.get(i).getType());
                    feslist.add(festival);
                }catch(Exception e){
                }
               }
               
           }
           newMap.put("content", feslist);
           
       }
       return newMap;
   }

    /**
     * 修改欢迎图
     * 
     * @return
     */
    public String updateFestival(String requestBody) {
        logger.debug("修改欢迎图,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9101, "");
        }
        String festivalId = trim(requestJson.getString("festivalId"));
        String festivalName = trim(requestJson.getString("festivalName"));
        String begin_time = trim(requestJson.getString("beginTime"));
        String end_time = trim(requestJson.getString("endTime"));
        String filePath =  trim(requestJson.getString("filePath"));
        
        SimpleDateFormat  sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> conditions = new HashMap<String, Object>();
        try {
            conditions.put("LTE_beginTime",sdf.parse(end_time));
            conditions.put("GTE_endTime",sdf.parse(end_time));
            List<FestivalVo> listFestivalVoE = festivalInterface.findFestivalByConditions(conditions);
            conditions.clear();
            conditions.put("LTE_beginTime",sdf.parse(begin_time));
            conditions.put("GTE_endTime",sdf.parse(begin_time));
            List<FestivalVo> listFestivalVoB = festivalInterface.findFestivalByConditions(conditions);
            if((null!=listFestivalVoE&&!listFestivalVoE.isEmpty())||(null!=listFestivalVoB&&!listFestivalVoB.isEmpty()))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9903, "");
            
        } catch (ParseException e1) {
        }
        /*try {
            filePath = FileUploadUtil.uploadFile(msg);
        } catch (Exception e) {
            logger.error("上传文件失败", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9901, ""); 
        }*/
        if ("".equals(festivalName) || "".equals(begin_time)||"".equals(end_time)||"".equals(festivalId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9901, "");

        try {
            SerializeConfig ser = new SerializeConfig();
            SimpleDateFormat  simple=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            FestivalVo festivalVo = festivalInterface.findByFestivalId(festivalId);
            festivalVo.setBeginTime(simple.parse(begin_time));
            festivalVo.setEndTime(simple.parse(end_time));
            if(null!=filePath&&!"".equals(filePath))
                festivalVo.setFestivalImg(filePath);
            
            festivalVo.setFestivalName(festivalName);
            festivalInterface.save(festivalVo);
           return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("添加欢迎图调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9902, "");
        }
    }
    
    /**
     * 删除欢迎图
     * 
     * @return
     */
    public String deleteFestival(String requestBody) {
        logger.debug("删除欢迎图,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9901, "");
        }
        String festivalId = trim(requestJson.getString("festivalId"));
        if (null == festivalId || "".equals(festivalId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9901, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            String[] festivalIds = festivalId.split(",");
            for (int i = 0; i < festivalIds.length; i++) {
                festivalInterface.deleteByFestivalId(festivalIds[i]);
            }
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("删除欢迎图调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9902, "");
        }
    }
    
    /**
     * 添加欢迎图
     * 
     * @return
     */
    public String addFestival(String requestBody) {
        logger.debug("添加欢迎图,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9101, "");
        }
        String festivalName = trim(requestJson.getString("festivalName"));
        String begin_time = trim(requestJson.getString("beginTime"));
        String end_time = trim(requestJson.getString("endTime"));
        String filePath =  trim(requestJson.getString("filePath"));
        
        SimpleDateFormat  sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> conditions = new HashMap<String, Object>();
        try {
            conditions.put("LTE_beginTime",sdf.parse(end_time));
            conditions.put("GTE_endTime",sdf.parse(end_time));
            List<FestivalVo> listFestivalVoE = festivalInterface.findFestivalByConditions(conditions);
            conditions.clear();
            conditions.put("LTE_beginTime",sdf.parse(begin_time));
            conditions.put("GTE_endTime",sdf.parse(begin_time));
            List<FestivalVo> listFestivalVoB = festivalInterface.findFestivalByConditions(conditions);
            if((null!=listFestivalVoE&&!listFestivalVoE.isEmpty())||(null!=listFestivalVoB&&!listFestivalVoB.isEmpty()))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9903, "");
            
        } catch (ParseException e1) {
        }
        
       /* try {
           filePath = FileUploadUtil.uploadFile(msg);
        } catch (Exception e) {
            logger.error("上传文件失败", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9901, ""); 
        }*/
        if ("".equals(festivalName) || "".equals(begin_time)||"".equals(end_time))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9901, "");

        try {
            SerializeConfig ser = new SerializeConfig();
            SimpleDateFormat  simple=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            FestivalVo festivalVo = new FestivalVo();
            festivalVo.setBeginTime(simple.parse(begin_time));
            festivalVo.setEndTime(simple.parse(end_time));
            festivalVo.setFestivalImg(filePath);
            festivalVo.setFestivalName(festivalName);
            festivalVo.setFestivalTime(new Date());
            festivalInterface.save(festivalVo);
           return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("添加欢迎图调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9902, "");
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
}
