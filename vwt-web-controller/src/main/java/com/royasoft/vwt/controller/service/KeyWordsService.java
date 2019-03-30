/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

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

import com.alibaba.fastjson.JSON;
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
import com.royasoft.vwt.controller.vo.ContainKeyWordsMsgVo;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.sensitivewords.api.interfaces.SensitiveWordInterface;
import com.royasoft.vwt.soa.business.sensitivewords.api.vo.ErrorMsgVO;
import com.royasoft.vwt.soa.business.sensitivewords.api.vo.SensitiveWordVo;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 关键词业务处理类
 *
 * @Author:huangs
 * @Since:2016年8月23日
 */
@Scope("prototype")
@Service
public class KeyWordsService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(KeyWordsService.class);

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
    private SensitiveWordInterface sensitiveWordInterface;
    @Autowired
    private MemberInfoInterface memberInfoInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.keyWords_queue.take();// 获取队列处理数据
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
                    // 分页查询所有关键字
                        case FunctionIdConstant.KEYWORDS_QUERY:
                            resInfo = findKeyWords(request_body);
                            break;
                        // 添加关键字
                        case FunctionIdConstant.KEYWORDS_SAVE:
                            resInfo = addKeyWords(request_body);
                            break;
                        // 删除关键字
                        case FunctionIdConstant.KEYWORDS_DELETE:
                            resInfo = deleteKeyWords(request_body);
                            break;
                        case FunctionIdConstant.KEYWORDS_ERRORMSG:
                            resInfo = getErrorMsg(request_body);
                            break;
                        case FunctionIdConstant.SETKEYWORDS:
                            resInfo = SetSensitiveWordSwitch(request_body);
                            break;
                        case FunctionIdConstant.QUERYKEYWORDS:
                            resInfo = querySensitiveWordSwitch(request_body);
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
                logger.error("关键词管理业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
                channel.close();
            }
        }
    }

    /**
     * 查询包含敏感词错误信息
     * 
     * @param requestBody
     * @return
     */
    public String getErrorMsg(String requestBody) {
        logger.debug("查询包含敏感词错误信息,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9201, "");
        }
        Map<String, Object> conditions = new HashMap<String, Object>();
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
            Map<String, Boolean> sortmap = new HashMap<String, Boolean>();
            sortmap.put("sendTime", false);
            Map<String, Object> map = sensitiveWordInterface.findErrorMsgVOByPage(pageIndex, pageSize, conditions, sortmap);
            if (null != map && !map.isEmpty()) {
                map = getMap(map);
            }
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, map);
        } catch (Exception e) {
            logger.error("查询包含敏感词错误信息异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9601, "");
        }
    }

    /**
     * 重新封装map
     * 
     * @param map
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> map) {
        List<ContainKeyWordsMsgVo> result = new ArrayList<ContainKeyWordsMsgVo>();
        List<ErrorMsgVO> listErrorMsgVO = (List<ErrorMsgVO>) map.get("content");
        for (ErrorMsgVO errorMsgVO : listErrorMsgVO) {
            ContainKeyWordsMsgVo containKeyWordsMsgVo = new ContainKeyWordsMsgVo();
            containKeyWordsMsgVo.setId(errorMsgVO.getId());
            containKeyWordsMsgVo.setFromChannel("1".equals(errorMsgVO.getType()) ? "消息" : "工作圈");
            containKeyWordsMsgVo.setContent(errorMsgVO.getContent());
            containKeyWordsMsgVo.setReceiveUser(errorMsgVO.getReceiveUser());
            containKeyWordsMsgVo.setSendTime(errorMsgVO.getSendTime());
            try {
                MemberInfoVO mem = memberInfoInterface.findById(errorMsgVO.getFromUser());
                containKeyWordsMsgVo.setPartName(mem.getMemberName());
                containKeyWordsMsgVo.setTelNum(mem.getTelNum());
                containKeyWordsMsgVo.setMemberName(mem.getMemberName());
            } catch (Exception e) {
                logger.error("获取人员信息异常", e);
                continue;
            }
            result.add(containKeyWordsMsgVo);
        }
        logger.debug("获取敏感词日志,result{}", JSON.toJSONString(result));
        map.put("content", result);
        return map;
    }

    /**
     * 分页查询所有关键字
     * 
     * @return
     */
    public String findKeyWords(String requestBody) {
        logger.debug("分页查询所有关键字,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9201, "");
        }
        Map<String, Object> conditions = new HashMap<String, Object>();
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, sensitiveWordInterface.findSensitiveWordVoByPage(pageIndex, pageSize, conditions, null));
        } catch (Exception e) {
            logger.error("分页查询所有关键字调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9202, "");
        }
    }

    /**
     * 添加关键词
     * 
     * @return
     */
    public String addKeyWords(String requestBody) {
        logger.debug("添加关键词,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9201, "");
        }
        String word = trim(requestJson.getString("word"));
        String type = trim(requestJson.getString("type"));
        if ("".equals(word))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9201, "");

        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            SensitiveWordVo sensitiveWordVo = new SensitiveWordVo();
            sensitiveWordVo.setWord(word);
            sensitiveWordVo.setCreateTime(new Date());
            sensitiveWordVo.setType(type);
            sensitiveWordInterface.saveSensitiveWordVo(sensitiveWordVo);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("添加关键词调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9202, "");
        }
    }

    /**
     * 删除关键词
     * 
     * @return
     */
    public String deleteKeyWords(String requestBody) {
        logger.debug("删除关键词,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9201, "");
        }
        String keyId = trim(requestJson.getString("keyId"));
        if (null == keyId || "".equals(keyId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9201, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            String[] keyIds = keyId.split(",");
            for (int i = 0; i < keyIds.length; i++) {
                sensitiveWordInterface.deleteSensitiveWord(keyIds[i]);
            }
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("删除关键词调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9202, "");
        }
    }

    /**
     * 设置敏感词开关
     * 
     * @param requestBody
     * @return
     */
    private String SetSensitiveWordSwitch(String requestBody) {
        logger.debug("设置敏感词开关,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9201, "");
        }
        String type = trim(requestJson.getString("type"));
        if (null == type || "".equals(type))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9501, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));

            boolean falg = sensitiveWordInterface.editSensitiveWordSwitch(type);
            logger.debug("设置敏感词开关状态:falg{}", falg);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, falg);
        } catch (Exception e) {
            logger.error("设置敏感词开关状态调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9502, "");
        }
    }

    /**
     * 查询敏感词开关
     * 
     * @param requestBody
     * @return
     */
    private String querySensitiveWordSwitch(String requestBody) {
        logger.debug("查询敏感词开关,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9201, "");
        }
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, sensitiveWordInterface.querySensitiveWordSwitch());
        } catch (Exception e) {
            logger.error("设置敏感词开关状态调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9701, "");
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
