/************************************************
 * Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ParaUtil;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.conllection.api.interfaces.CollectionInterface;
import com.royasoft.vwt.soa.business.conllection.api.vo.CollectionVo;
import com.royasoft.vwt.soa.business.login.api.interfaces.AlreadyLoginInterface;
import com.royasoft.vwt.soa.business.login.api.vo.AlreadyLoginVo;
import com.royasoft.vwt.soa.sundry.clientversion.api.interfaces.ClientVersionInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.corpcustom.api.interfaces.CorpCustomInterface;
import com.royasoft.vwt.soa.uic.corpcustom.api.vo.CorpCustomVO;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 收藏功能业务处理
 * 
 * @author huangshuai
 * @Since:2016年03月1日
 */
@Scope("prototype")
@Service
public class CollectionService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CollectionService.class);

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
    private MemberInfoUtil memberInfoUtil;

    @Autowired
    private CollectionInterface collectionInterface;

    // 定义验证token的缓存
    private final String CheckTokenSpace = "ROYASOFT:SHAND:CHECK:TOKEN:";

    // 定义获取登录信息的缓存
    private final String LoginTokenSpace = "ROYASOFT:SHAND:LOGIN:TOKEN:";

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private AlreadyLoginInterface alreadyLoginInterface;

    @Autowired
    private ClientVersionInterface clientVersionInterface;

    @Autowired
    private CorpInterface corpInterface;

    @Autowired
    private CorpCustomInterface corpCustomInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.conllection_queue.take();// 获取队列处理数据
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

                    if (function_id == null || function_id.length() <= 0) {
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
            case FunctionIdConstant.SAVECOLLECTION:// 添加收藏内容
                res = savecollection(user_id, request_body);
                break;
            case FunctionIdConstant.DELETECOLLECTION:// 删除收藏内容
                res = deletecollection(user_id, request_body);
                break;
            case FunctionIdConstant.QUERYCOLLECTION:// 查询收藏内容
                res = querycollection(user_id, request_body);
                break;
            /** 山东扫码登录，pc生成token入缓存 */
            case FunctionIdConstant.SAVELOGINTOKEN:
                res = saveLoginToken(user_id, request_body);
                break;
            /** 山东扫码登录，客户端扫码根据token取信息 */
            case FunctionIdConstant.FINDMSGBYTOKEN:
                res = findMsgByToken(user_id, request_body);
                break;
            /** 客户端允许登录，传入token和登录信息到缓存 */
            case FunctionIdConstant.ALLOWLOGIN:
                res = allowLogin(user_id, request_body);
                break;
            /** 山东扫码登录，pc定时根据token取登录信息 */
            case FunctionIdConstant.FINDLOGINBYTOKEN:
                res = findLoginByToken(user_id, request_body);
                break;
            default:
                res = ResponsePackUtil.returnFaileInfo(); // 未知请求
        }
        return res;
    }

    /**
     * 保存收藏信息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String savecollection(String userId, String requestBody) {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String telNum = requestJson.getString("telNum");// 用户号码
        String picurl = requestJson.getString("picurl"); // 图片地址
        String fileurl = requestJson.getString("fileurl");// 文件地址，分享的链接
        String title = requestJson.getString("title"); // 标题
        String type = requestJson.getString("type");// 类型1-文本 2-图片 3-语音 4-位置 5-视频 6 -文件 7-名片 8-分享链接
        String content = requestJson.getString("content"); // 内容
        String fromName = requestJson.getString("fromname"); // 被收藏的用户ID
        String userName = requestJson.getString("userName"); // 被收藏的用户id

        if (!StringUtils.stringIsNotNull(userId)) {
            logger.debug("收藏信息,userId参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        if (!StringUtils.stringIsNotNull(telNum)) {
            logger.debug("收藏信息,telNum参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        CollectionVo resultVo = new CollectionVo();
        try {
            CollectionVo collectionVo = new CollectionVo();
            collectionVo.setMemId(userId);
            collectionVo.setPicurl(picurl);
            collectionVo.setFileurl(fileurl);
            collectionVo.setTelNum(telNum);
            collectionVo.setTitle(title);
            collectionVo.setType(type);
            collectionVo.setContent(content);
            collectionVo.setCreateTime(new Date());
            collectionVo.setReserve1(fromName);
            collectionVo.setReserve2(userName);
            resultVo = collectionInterface.save(collectionVo);
            logger.debug("保存收藏信息信息结果，map{}", JSON.toJSONString(resultVo));
        } catch (Exception e) {
            logger.error("保存收藏信息信息异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROR);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultVo", resultVo);
        return jsonObject.toString();
    }

    /**
     * 删除收藏信息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String deletecollection(String userId, String requestBody) {
        logger.debug("删除收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String colId = requestJson.getString("colId"); // 收藏ID

        if (!StringUtils.stringIsNotNull(colId)) {
            logger.debug("删除收藏信息,colId参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        try {
            CollectionVo collectionVo = collectionInterface.findByCollectionId(colId);
            logger.debug("收藏信息{}", collectionVo);
            if (null == collectionVo || !collectionVo.getMemId().equals(userId))
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_MEM_ERROY);

            collectionInterface.deleteByCollectionId(colId);
        } catch (Exception e) {
            logger.error("删除收藏信息信息异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROR);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        return jsonObject.toString();
    }

    /**
     * 分页查询收藏信息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String querycollection(String userId, String requestBody) {
        logger.debug("分页查询收藏信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        if (!StringUtils.stringIsNotNull(userId)) {
            logger.debug("查询收藏信息,userId参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        String page = requestJson.getString("page");
        String row = requestJson.getString("row");
        int pageIndex = 1;
        int pageSize = 10;
        if (StringUtils.checkParamNull(page))
            pageIndex = Integer.parseInt(page);
        if (StringUtils.checkParamNull(row))
            pageSize = Integer.parseInt(row);
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_memId", userId);
            // content titile reserve2模糊
            String key = requestJson.getString("key");
            logger.debug("key为============================:{}",key);
            if (!org.springframework.util.StringUtils.isEmpty(key)) {
                conditions.put("ORLIKE_content", key);
                conditions.put("ORLIKE_reserve2", key);
            }

            Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
            sortMap.put("createTime", false);
            result = collectionInterface.findCollectionByPage(pageIndex, pageSize, conditions, sortMap);
            logger.debug("收藏接口返回数据{}",JSONObject.toJSONString(result));
            if (null == result)
                result = new HashMap<String, Object>();

        } catch (Exception e) {
            logger.error("分页查询收藏信息异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROR);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.COLLECTION_SUCC);
        jsonObject.put("resultMap", result);
        return jsonObject.toString();
    }

    /**
     * 保存pc端信息到缓存中 留作判断
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String saveLoginToken(String userId, String requestBody) {
        logger.debug("保存pc端信息到缓存中,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String token = requestJson.getString("token"); // token值
        if (!StringUtils.stringIsNotNull(token)) {
            logger.debug("token参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SDTOKEN_ERROY);
        }

        try {
            boolean flag = redisInterface.setString(CheckTokenSpace + token, token, 120);
            logger.debug("保存入缓存,flag{},key{}", flag, CheckTokenSpace + token);
        } catch (Exception e) {
            logger.error("保存检测token异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SDTOKEN_ERROR);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.SDTOKEN_SUCC);
        return jsonObject.toString();
    }

    /**
     * 客户端扫码判断
     * 
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String findMsgByToken(String userId, String requestBody) {
        logger.debug("客户端扫码判断信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String token = requestJson.getString("token"); // token值
        if (!StringUtils.stringIsNotNull(token)) {
            logger.debug("token参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SDTOKEN_ERROY);
        }

        try {
            String resultToken = redisInterface.getString(CheckTokenSpace + token);
            if (null == resultToken || "".equals(resultToken)) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SDTOKEN_ERROY);
            } else {
                redisInterface.del(CheckTokenSpace + token);
            }
        } catch (Exception e) {
            logger.error("保存检测token异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SDTOKEN_ERROR);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.SDTOKEN_SUCC);
        jsonObject.put("resultValue ", token);
        return jsonObject.toString();
    }


    /**
     * 客户端允许登录接口
     * 
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String allowLogin(String userId, String requestBody) {
        logger.debug("客户端允许登录接口信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String token = requestJson.getString("token"); // token值
        String telNum = requestJson.getString("telNum"); // 手机号码
        String memId = requestJson.getString("userId"); // 用户Id
        if (!StringUtils.stringIsNotNull(token)) {
            logger.debug("token参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        if (!StringUtils.stringIsNotNull(telNum)) {
            logger.debug("telNum参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }
        if (!StringUtils.stringIsNotNull(memId)) {
            logger.debug("memId参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.COLLECTION_ERROY);
        }

        try {
            JSONObject json = new JSONObject();
            json.put("telNum", telNum);
            json.put("memId", memId);
            boolean flag = redisInterface.setString(LoginTokenSpace + token, JSON.toJSONString(json), 120);
            logger.debug("保存入缓存,flag{},key{}", flag, CheckTokenSpace + token, LoginTokenSpace + token);
        } catch (Exception e) {
            logger.error("保存检测token异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SDTOKEN_ERROR);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.SDTOKEN_SUCC);
        return jsonObject.toString();
    }

    /**
     * pc端定时到缓存中取消息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String findLoginByToken(String userId, String requestBody) {
        logger.debug("获取收藏信息信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String token = requestJson.getString("token"); // token值
        String clientVersion = requestJson.getString("clientVersion");// 客户端版本
        String clientModel = "pc";// 客户端类型
        String imei = requestJson.getString("imei");// 手机唯一imei
        String clientType = "pc";// 客户端类型（android/ios）
        String aesKey = requestJson.getString("aesKey");// AES加密密钥
        if (!StringUtils.stringIsNotNull(token)) {
            logger.debug("收藏信息,token参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SDTOKEN_ERROY);
        }
        String username = "";
        try {
            String loginMsg = redisInterface.getString(LoginTokenSpace + token);
            JSONObject loginObj = StringUtils.strisJsonStr(loginMsg);
            username = loginObj.getString("telNum");
        } catch (Exception e) {
        }
        // String username="18001585412";
        if (!StringUtils.stringIsNotNull(username)) {
            logger.debug("收藏信息,username参数为空");
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SDTOKEN_ERROY);
        }
        logger.debug("扫码登陆(解析body),username:{},clientVersion:{},clientModel:{},imei:{},clientType:{},aesKey:{}", username, clientVersion, clientModel, imei,
                clientType, aesKey);

        /** 判断该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(username);

        /** 保存imei和clientType信息 */
        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(username);
        if (!saveImeiAndClientType(clientUserVOs, imei, clientType, username))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1029, "");
        logger.debug("保存imei和clientType信息成功,username:{},imei:{},clientType:{}", username, imei, clientType);

        /** 记录登陆客户端设备信息 */
        saveClientInfo(clientType, clientVersion, clientModel, username);

        /** 记录登陆号码和时间 */
        try {
            AlreadyLoginVo alreadyLoginVo = new AlreadyLoginVo();
            alreadyLoginVo.setMemberTel(username);
            alreadyLoginVo.setFirstLoginTime(new Date());
            alreadyLoginInterface.saveAlreadyLogin(alreadyLoginVo);
        } catch (Exception e) {
        }
        String res = getResponseInfo(memberInfoVOs, clientUserVOs, username);
        if (null == res || "".equals(res))
            ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(res, aesKey));
    }

    /**
     * 保存imei和clientType信息
     * 
     * @param clientUserVORes
     * @param imei
     * @param clientType
     * @return
     * @Description:
     */
    private boolean saveImeiAndClientType(List<ClientUserVO> clientUserVORes, String imei, String clientType, String username) {
        logger.debug("保存imei和clientType信息,clientUserVORes:{},imei:{},clientType:{}", JSONObject.toJSONString(clientUserVORes), imei, clientType);
        try {
            for (ClientUserVO clientUserVO : clientUserVORes) {
                clientUserVO.setImsi(imei);
                clientUserVO.setClientType(clientType);
                clientUserInterface.saveUser(clientUserVO);
            }
        } catch (Exception e) {
            logger.error("保存imei和clientType信息异常,clientUserVORes:{},imei:{},clientType:{}", JSONObject.toJSONString(clientUserVORes), imei, clientType, e);
        }
        return true;
    }

    /**
     * 记录登录客户端设备信息
     * 
     * @param clientType
     * @param clientVersion
     * @param clientModel
     * @param userName
     * @Description:
     */
    private void saveClientInfo(String clientType, String clientVersion, String clientModel, String userName) {
        clientVersionInterface.addLogonLog(userName, clientType, clientVersion, clientModel);
    }


    /**
     * 组装返回信息
     * 
     * @param memberInfoVOs
     * @return
     * @Description:
     */
    private String getResponseInfo(List<MemberInfoVO> memberInfoVOs, List<ClientUserVO> clientUserVOs, String username) {
        logger.debug("组装返回信息,memberInfoVOs:{},clientUserVO:{}", JSONObject.toJSONString(memberInfoVOs), JSONObject.toJSONString(clientUserVOs));
        JSONObject jsonObject = getUserInfo(memberInfoVOs, clientUserVOs, username);
        logger.debug("组装返回信息(基础信息),memberInfoVOs:{},clientUserVOs:{},jsonObject:{}", JSONObject.toJSONString(memberInfoVOs),
                JSONObject.toJSONString(clientUserVOs), JSONObject.toJSONString(jsonObject));
        return JSONObject.toJSONString(jsonObject);
    }

    /**
     * 获取基础信息
     * 
     * @param memberInfoVOs
     * @return
     * @Description:
     */
    private JSONObject getUserInfo(List<MemberInfoVO> memberInfoVOs, List<ClientUserVO> clientUserVOs, String username) {
        JSONObject jsonObject = new JSONObject();
        logger.debug("获取基础信息,memberInfoVOs:{}", JSONObject.toJSONString(memberInfoVOs));

        boolean PwdIsNull = true;
        for (ClientUserVO clientUserVO : clientUserVOs) {
            if (null != clientUserVO.getPwd() && !"".equals(clientUserVO.getPwd())) {
                PwdIsNull = false;
                break;
            }
        }
        jsonObject.put("PwdIsNull", PwdIsNull);
        jsonObject.put("telNum", username);
        jsonObject.put("companyList", getMemberInfos(memberInfoVOs));
        jsonObject.put("appStoreUrl", ParamConfig.appstore_url);
        jsonObject.put("numberLimit", ParamConfig.number_limit);
        jsonObject.put("VFCurl", ParamConfig.vfc_url);
        jsonObject.put("SSOURL", ParamConfig.sso_url);
        jsonObject.put("shareUrl", ParamConfig.share_url);
        jsonObject.put("newFileUrl", ParamConfig.file_server_url);
        logger.debug("获取基础信息(jsonObject),jsonObject:{}", jsonObject.toJSONString());
        return jsonObject;
    }

    /**
     * 获取一人多职信息
     * 
     * @param memberInfoVOs
     * @return
     * @Description:
     */
    private JSONArray getMemberInfos(List<MemberInfoVO> memberInfoVOs) {
        logger.debug("获取一人多职信息,memberInfoVOs:{}", memberInfoVOs.size());
        JSONArray jsonArray = new JSONArray();
        try {
            for (MemberInfoVO memberInfoVO : memberInfoVOs) {
                String corpId = memberInfoVO.getCorpId();
                logger.debug("corpid:{}", corpId);
                CorpVO corpVO = corpInterface.findById(corpId);
                JSONObject jsonObject = new JSONObject();
                ClientUserVO clientUserVO = clientUserInterface.findById(memberInfoVO.getMemId());
                CorpCustomVO customVO = corpCustomInterface.findCorpCustomById(corpId);
                jsonObject.put("companyName", corpVO.getCorpName());
                jsonObject.put("departmentName", memberInfoVO.getPartName());
                jsonObject.put("memberID", memberInfoVO.getMemId());
                jsonObject.put("headPhotoUrl", null == clientUserVO || null == clientUserVO.getAvatar() ? "" : clientUserVO.getAvatar());
                jsonObject.put("userName", memberInfoVO.getMemberName());
                jsonObject.put("corpId", corpId);
                jsonObject.put("CLIQUE_ID", null == clientUserVO || null == clientUserVO.getClique() ? "" : clientUserVO.getClique());
                jsonObject.put("shortPhoneNumber", null == memberInfoVO.getShortNum() ? "" : memberInfoVO.getShortNum());
                jsonObject.put("emailAddr ", null == memberInfoVO.getEmail() ? "" : memberInfoVO.getEmail());

                jsonObject.put("shortName", null == customVO || null == customVO.getShortname() ? "" : customVO.getShortname());

                boolean internetManager = false;
                if (null != corpVO.getFromchannel() && !"".equals(corpVO.getFromchannel()) && corpVO.getFromchannel() == 7
                        && corpVO.getCorpMobilephone().equals(memberInfoVO.getTelNum()))
                    internetManager = true;
                jsonObject.put("internetManager", internetManager);
                jsonArray.add(jsonObject);
            }
            logger.debug("获取一人多职信息,jsonArray:{}", jsonArray.toJSONString());
        } catch (Exception e) {
            logger.error("获取一人多职信息异常", e);
        }

        return jsonArray;

    }

}
