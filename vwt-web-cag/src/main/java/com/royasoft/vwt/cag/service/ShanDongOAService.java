/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ParaUtil;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.XmlToJsonUtil;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;

/**
 * 山东OAhtml5处理接口
 * 
 * @author huangshuai
 * @Since:2016年12月26日
 */
@Scope("prototype")
@Service
public class ShanDongOAService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ShanDongOAService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;
    /** 数据调用接口 */
    @Autowired
    private DatabaseInterface databaseInterface;
    @Autowired
    private OperationLogService operationLogService;
    
    //定义验证token的缓存
    private final String OASESSION = "ROYASOFT:SHAND:OAHTML:MEMBER:";
    
    @Autowired
    private RedisInterface redisInterface;
    
    String url="http://10.19.98.12:9001/weboa/MobileInterface";
    
    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.shandongoa_queue.take();// 获取队列处理数据
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
                        ResponsePackUtil.CalibrationParametersFailure(channel, "收藏业务请求参数校验失败！");
                    } else {
                        // 收藏具体业务分层跳转
                        res = CollectionBusinessLayer(channel, request, function_id, user_id, request_body, msg);
                    }
                    // 响应成功
                    ResponsePackUtil.responseStatusOK(channel, res);
                    operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", "0000");
                }
            } catch (Exception e) {
                logger.error("收藏业务逻辑处理异常", e);
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
     * 收藏功能分块请求
     * 
     * @param function_id
     * @param user_id
     * @param request_body
     * @param msg
     * @return
     */
    private String CollectionBusinessLayer(Channel channel, HttpRequest request, String function_id, String user_id, String request_body, Object msg) {
        String res = "";
        switch (function_id) {
            case FunctionIdConstant.OASTATICLOGIN://  ＯＡ登录接口，静态密码登录
                res = oaStaticLogin(user_id,request_body);
                break;
            case FunctionIdConstant.OAJIUGONG:// ＯＡ首页九宫格接口
                res = oajiugong(user_id,request_body);
                break;
            case FunctionIdConstant.OADOLIST://  ＯＡ待办列表接口
                res = oadoList(user_id,request_body);
                break;
            case FunctionIdConstant.OADOFORM:// ＯＡ待办详情接口
                res = oadoForm(user_id,request_body);
                break;
            case FunctionIdConstant.OAWRITESUG:// ＯＡ意见填写接口
                res = oaWriteSug(user_id,request_body);
                break;
            case FunctionIdConstant.OASAVESUG:// ＯＡ意见保存接口
                res = oaSaveSug(user_id,request_body);
                break;
            case FunctionIdConstant.OANEXTITEM:// ＯＡ人员选择接口
                res = oaNextItem(user_id,request_body);
                break;
            case FunctionIdConstant.OASAVENEXTITEM:// ＯＡ待办提交接口 
                res = oaSaveNextItem(user_id,request_body);
                break;
            case FunctionIdConstant.OADONELIST:// ＯＡ已办列表接口
                res = oadoNeList(user_id,request_body);
                break;
            case FunctionIdConstant.OADONEFORM:// ＯＡ已办详情接口
                res = oadoNeForm(user_id,request_body);
                break;
            case FunctionIdConstant.OATRACINGSUG://ＯＡ流程跟踪接口
                res = oaTracingSug(user_id,request_body);
                break;
            default:
                res = ResponsePackUtil.returnFaileInfo(); // 未知请求
        }
        return res;
    }
    
    /**
     * 保存登录信息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oaStaticLogin( String userId,String requestBody)  {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String memId = requestJson.getString("userName"); // 用户ID
        String pwd = requestJson.getString("pwd"); //密码
        String loginType = "0";
        
        if (!StringUtils.stringIsNotNull(memId)) {
            logger.debug("登录信息,memId参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        if (!StringUtils.stringIsNotNull(pwd)) {
            logger.debug("登录信息,pwd参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("p_User_Id", memId);
        map.put("PASSWORD", pwd);
        map.put("loginType", loginType);
        
        String url2="http://10.19.98.12:9001/Js_Login_Check";
        String  result=XmlToJsonUtil.getOAJSON(url2, map);
        boolean flag= redisInterface.setString(OASESSION+memId,result);
        logger.debug("保存入缓存,flag{},key{}", flag,OASESSION+memId,flag);
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", result);
        return jsonObject.toString();
    }
    
    /**
     * ＯＡ首页九宫格接口
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oajiugong( String userId,String requestBody)  {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String memId = requestJson.getString("userName"); // 用户ID
        
        if (!StringUtils.stringIsNotNull(memId)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "jiuGong");
        map.put("userName", memId);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo",  XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString().replace("@", "");
    }
    
    
    /**
     * ＯＡ待办列表
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oadoList( String userId,String requestBody)  {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String userName = requestJson.getString("userName"); // 用户ID
        String curPage = requestJson.getString("curPage"); // 用户ID
        if(null==curPage||"".equals(curPage))
            curPage="1";
        
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "todoList");
        map.put("userName", userName);
        map.put("curPage", curPage);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString();
    }
    
    /**
     * ＯＡ待办详情
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oadoForm( String userId,String requestBody)  {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String userName = requestJson.getString("userName"); // 用户ID
        String orgId = requestJson.getString("orgId"); // 用户ID
        String mdlId = requestJson.getString("mdlId"); // 用户ID
        String id = requestJson.getString("id"); // 用户ID
        String type = requestJson.getString("type"); // 0发文、1收文、2绿色通道
        
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "toDoForm");
        map.put("userName", userName);
        map.put("orgId", orgId);
        map.put("id", id);
        map.put("type", type);
        map.put("mdlId", mdlId);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo",  XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString().replace("@", "");
    }
    
    /**
     * ＯＡ意见填写接口
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oaWriteSug( String userId,String requestBody)  {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String userName = requestJson.getString("userName"); // 用户ID
        String mdlId = requestJson.getString("mdlId"); // 用户ID
        String documentId = requestJson.getString("documentId"); // 用户ID
        String type = requestJson.getString("type"); // 用户ID
        
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "writeSug");
        map.put("userName", userName);
        map.put("documentId", documentId);
        map.put("mdlId", mdlId);
        map.put("type", type);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString().replace("@", "");
    }
    
    /**
     * ＯＡ意见保存接口
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oaSaveSug( String userId,String requestBody)  {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String userName = requestJson.getString("userName"); // 用户ID
        String documentId = requestJson.getString("documentId"); // 用户ID
        String mdlId = requestJson.getString("mdlId"); // 用户ID
        String type = requestJson.getString("type"); // 用户ID
        String sug = requestJson.getString("sug"); // 用户ID
        String selectonClusionIds = requestJson.getString("selectonClusionIds"); // 用户ID
        
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "saveSug");
        map.put("userName", userName);
        map.put("documentId", documentId);
        map.put("mdlId", mdlId);
        map.put("type", type);
        map.put("sug", sug);
        map.put("selectonClusionIds", selectonClusionIds);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString().replace("@", "");
    }
    
    /**
     * ＯＡ人员选择接口
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oaNextItem( String userId,String requestBody)  {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String userName = requestJson.getString("userName"); // 用户ID
        String documentId = requestJson.getString("documentId"); // 用户ID
        String mdlId = requestJson.getString("mdlId"); // 用户ID
        String type = requestJson.getString("type"); // 用户ID
        String sug = requestJson.getString("sug"); // 用户ID
        String from = requestJson.getString("from"); // 用户ID
        String selectonClusionIds = requestJson.getString("selectonClusionIds"); // 用户ID
        
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "inputNextItemForXmap");
        map.put("userName", userName);
        map.put("documentId", documentId);
        map.put("mdlId", mdlId);
        map.put("type", type);
        map.put("from", from);
        map.put("sug", sug);
        map.put("selectonClusionIds", selectonClusionIds);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString().replace("@", "");
    }
    
    /**
     * 待办提交接口
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oaSaveNextItem( String userId,String requestBody)  {
        logger.debug("获取待办提交信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String userName = requestJson.getString("userName"); // 用户ID
        String documentId = requestJson.getString("documentId"); // 用户ID
        String mdlId = requestJson.getString("mdlId"); // 用户ID
        String type = requestJson.getString("type"); // 用户ID
        String nextItemIds = requestJson.getString("nextItemIds"); // 用户ID
        String users = requestJson.getString("users"); // 用户ID
        
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "saveNextItem");
        map.put("userName", userName);
        map.put("documentId", documentId);
        map.put("mdlId", mdlId);
        map.put("type", type);
        map.put("nextItemIds", nextItemIds);
        map.put("users", users);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString().replace("@", "");
    }
    
    /**
     * 已办列表、分页接口
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oadoNeList( String userId,String requestBody)  {
        logger.debug("获取已办列表、分页接口信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String userName = requestJson.getString("userName"); // 用户ID
        String curPage = requestJson.getString("curPage"); // 用户ID
        String subject = requestJson.getString("subject"); // 用户ID
        String markId = requestJson.getString("markId"); // 用户ID
        if(null==curPage||"".equals(curPage))
            curPage="1";
        
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "doneList");
        map.put("userName", userName);
        map.put("curPage", curPage);
        map.put("subject", subject);
        map.put("markId", markId);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString().replace("@", "");
    }
    
    /**
     * 已办详情接口
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oadoNeForm( String userId,String requestBody)  {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String userName = requestJson.getString("userName"); // 用户ID
        String mdlId = requestJson.getString("mdlId"); // 用户ID
        String orgId = requestJson.getString("orgId"); // 用户ID
        String id = requestJson.getString("id"); // 用户ID
        String type = requestJson.getString("type"); // 用户ID
        
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "doneForm");
        map.put("userName", userName);
        map.put("mdlId", mdlId);
        map.put("orgId", orgId);
        map.put("id", id);
        map.put("type", type);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString().replace("@", "");
    }
    
    /**
     * 流程跟踪接口
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String oaTracingSug( String userId,String requestBody)  {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String userName = requestJson.getString("userName"); // 用户ID
        String mdlId = requestJson.getString("mdlId"); // 用户ID
        String documentId = requestJson.getString("documentId"); // 用户ID
        String type = requestJson.getString("type"); // 用户ID
        
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("登录信息,用户参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        
        Map<String,String>  map=new HashMap<String,String>();
        map.put("cmd", "sug");
        map.put("userName", userName);
        map.put("documentId", documentId);
        map.put("type", type);
        map.put("mdlId", mdlId);
       // /weboa/MobileInterface?cmd=attach&documentId=10431&userName=lixiuchuan&id=10000098&docId=10000098&mdlId=101165
        String  result=XmlToJsonUtil.getOAJSON(url, map);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", XmlToJsonUtil.getJSONFromXml(result));
        return jsonObject.toString().replace("@", "");
    }
}
