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
import com.royasoft.vwt.soa.business.workteam.api.interfaces.CircleInterface;
import com.royasoft.vwt.soa.business.workteam.api.vo.CircleInfoVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;

/**
 * 圈子管理业务处理类
 *
 * @Author:huangs
 * @Since:2016年8月23日
 */
@Scope("prototype")
@Service
public class CircleService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CircleService.class);

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
    private CircleInterface circleInterface;
    
    @Autowired
    private DepartMentInterface departMentInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.circle_queue.take();// 获取队列处理数据
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
                    // 分页查询所有的圈子
                        case FunctionIdConstant.CIRCLE_QUERY:
                            resInfo = findCirclreForPage(request_body);
                            break;
                        // 添加圈子
                        case FunctionIdConstant.CIRCLE_SAVE:
                            resInfo = addCircle(request_body);
                            break;
                        // 删除圈子
                        case FunctionIdConstant.CIRCLE_DELETE:
                            resInfo = deleteCircle(request_body);
                            break;
                        // 查询圈子人员
                        case FunctionIdConstant.CIRCLE_MEMBER:
                            resInfo = findCircleMember(request_body);
                            break;
                        // 修改圈子
                        case FunctionIdConstant.CIRCLE_UPDATE:
                            resInfo = updateCircle(request_body);
                            break;
                        // 删除圈子人员
                        case FunctionIdConstant.CIRCLE_MEMBER_DELETE:
                            resInfo = deleteCircleMember(request_body);
                            break;
                        // 根据圈子ID删除圈子人员
                        case FunctionIdConstant.CIRCLE_MEMBER_DELETE_CICRLEID:
                            resInfo = deleteCircleMemberByCircleId(request_body);
                            break;
                        // 添加圈子人员
                        case FunctionIdConstant.CIRCLE_MEMBER_SAVE:
                            resInfo = addCircleMember(request_body);
                            break;
                            // 部门树
                        case FunctionIdConstant.CIRCLETREE:
                            resInfo = circleControlTree(request_body);
                            break;
                            // 部门树到人
                        case FunctionIdConstant.CIRCLETREECORP:
                            resInfo = circleControlTreeMem(request_body);
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
                logger.error("圈子管理业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
                channel.close();
            }
        }
    }

    /**
     * 分页查询所有的圈子
     * 
     * @return
     */
    public String findCirclreForPage(String requestBody) {
        logger.debug("分页显示圈子管理,requestBody:{}", requestBody);
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
        Map<String, Object> conditions = new HashMap<String, Object>();
        String page = trim(requestJson.getString("page"));
        String row = trim(requestJson.getString("row"));
        try {
            String circleName = trim(requestJson.getString("circleName"));
            if (null != circleName && !"".equals(circleName))
                conditions.put("LIKE_circleName", circleName);

        } catch (Exception e) {
        }
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
            Map<String, Object> map = circleInterface.findCircleInfoVo(pageIndex, pageSize, conditions, null);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, map);
        } catch (Exception e) {
            logger.error("分页展示圈子调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9102, "");
        }
    }

    /**
     * 添加圈子
     * 
     * @return
     */
    public String addCircle(String requestBody) {
        logger.debug("添加圈子,requestBody:{}", requestBody);
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
        String circleName = trim(requestJson.getString("circleName"));
        String introduction = trim(requestJson.getString("introduction"));
        if ("".equals(circleName) || "".equals(introduction))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9101, "");

        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            CircleInfoVo circleInfoVo = new CircleInfoVo();
            circleInfoVo.setCircleName(circleName);
            circleInfoVo.setIntroduction(introduction);
            circleInfoVo.setCreateTime(new Date());
            circleInfoVo.setCreateUserId(corpid);
            circleInterface.save(circleInfoVo);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("添加圈子调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9102, "");
        }
    }

    /**
     * 删除圈子
     * 
     * @return
     */
    public String deleteCircle(String requestBody) {
        logger.debug("删除圈子,requestBody:{}", requestBody);
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
        String circleId = trim(requestJson.getString("circleId"));
        if (null == circleId || "".equals(circleId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9101, "");
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            String[] circleIds = circleId.split(",");
            for (int i = 0; i < circleIds.length; i++) {
                circleInterface.deleteByCircleId(circleIds[i]);
            }
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("删除圈子调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9102, "");
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
     * 分页展示圈子人员
     * 
     * @return
     */
    public String findCircleMember(String requestBody) {
        logger.debug("分页展示圈子人员,requestBody:{}", requestBody);
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
        String circleId = trim(requestJson.getString("circleId"));
        String page = trim(requestJson.getString("page"));
        String row = trim(requestJson.getString("row"));
        if ("".equals(circleId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9101, "");

        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("EQ_circleId", circleId);

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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, circleInterface.findMemberByPage(pageIndex, pageSize, conditions, null));
        } catch (Exception e) {
            logger.error("分页展示圈子人员调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9102, "");
        }
    }

    /**
     * 修改圈子信息
     * 
     * @return
     */
    public String updateCircle(String requestBody) {
        logger.debug("修改圈子信息,requestBody:{}", requestBody);
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
        String circleId = trim(requestJson.getString("circleId"));
        String circleName = trim(requestJson.getString("circleName"));
        String introduction = trim(requestJson.getString("introduction"));
        if ("".equals(circleName) || "".equals(introduction) || "".equals(circleId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9101, "");

        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            CircleInfoVo circleInfoVo = circleInterface.findByCircleId(circleId);
            if (null == circleInfoVo)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9103, "");

            circleInfoVo.setCircleId(circleId);
            circleInfoVo.setCircleName(circleName);
            circleInfoVo.setIntroduction(introduction);
            circleInfoVo.setCreateTime(new Date());
            circleInfoVo.setCreateUserId(corpid);
            circleInterface.save(circleInfoVo);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("修改圈子信息调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9102, "");
        }
    }

    /**
     * 根据圈子ID删除圈子人员
     * 
     * @return
     */
    public String deleteCircleMemberByCircleId(String requestBody) {
        logger.debug("根据圈子ID删除圈子人员,requestBody:{}", requestBody);
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
        String circleId = trim(requestJson.getString("circleId"));
        if ("".equals(circleId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9101, "");

        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            circleInterface.deleteMenberByCircleId(circleId);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("根据圈子ID删除圈子人员服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9102, "");
        }
    }

    /**
     * 删除圈子人员
     * 
     * @return
     */
    public String deleteCircleMember(String requestBody) {
        logger.debug("删除圈子人员,requestBody:{}", requestBody);
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
        String circleId = trim(requestJson.getString("circleId"));
        String memId = trim(requestJson.getString("memId"));
        if ("".equals(memId) || "".equals(circleId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9101, "");

        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            String[] menIds = memId.split(",");
            for (int i = 0; i < menIds.length; i++) {
                circleInterface.delMember(circleId, menIds[i]);
            }
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("删除圈子人员调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9102, "");
        }
    }

    /**
     * 添加圈子人员
     * 
     * @return
     */
    public String addCircleMember(String requestBody) {
        logger.debug("添加圈子人员,requestBody:{}", requestBody);
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
        String circleId = trim(requestJson.getString("circleId"));
        String memId = trim(requestJson.getString("memId"));
        String partId = trim(requestJson.getString("partId"));
        if ("".equals(circleId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9101, "");

        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            circleInterface.saveMember(circleId, partId, memId);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } catch (Exception e) {
            logger.error("添加圈子人员调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9102, "");
        }
    }
    
    /**
     * 部门树
     * 
     * @param requestBody
     * @return
     * @author huangshuai 2016年10月27日
     */
    public String circleControlTree(String requestBody) {
        logger.debug("获取部门树,requestBody:{}", requestBody);
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
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2001, "");
        }

        List<DepartMentVO> list = null;

        list = departMentInterface.findByCorpId(corpid);

        if (null == list || list.size() <= 0) {
            // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2014, list);
            logger.debug("该企业下无部门corpid:{}", corpid);
            list = new ArrayList<DepartMentVO>();
        }

        return ResponsePackUtil.buildPack("0000", list);
    }

    /**
     * 部门树到人
     * 
     * @param requestBody
     * @return
     * @author huangshuai 2016年10月27日
     */
    public String circleControlTreeMem(String requestBody) {
        logger.debug("部门树到人,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String deptid = trim(requestJson.getString("deptid"));

        List<ClientUserVO> list = null;
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("EQ_deptId", deptid);

        list = clientUserInterface.findByCondition(conditions, null);
        if (null == list || list.size() <= 0) {
            logger.debug("该部门下无人员deptid:{}", deptid);
            list = new ArrayList<ClientUserVO>();
        }

        return ResponsePackUtil.buildPack("0000", list);
    }
    
}
