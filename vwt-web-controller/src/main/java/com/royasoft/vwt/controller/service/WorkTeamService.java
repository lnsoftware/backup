/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.royasoft.vwt.controller.config.ParamConfig;
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.StringUtils;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.blackLlist.api.interfaces.BlackListInterface;
import com.royasoft.vwt.soa.business.blackLlist.api.vo.BlackListVo;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamMessageInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamReplyInterface;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamMessageVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamReplyVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;

/**
 * 工作圈黑名单 处理类
 * 
 * @author daizl
 *
 */
@Scope("prototype")
@Service
public class WorkTeamService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WorkTeamService.class);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private BlackListInterface blackListInterface;

    @Autowired
    private WorkTeamInterface workTeamInterface;

    @Autowired
    private WorkTeamReplyInterface workTeamReplyInterface;

    @Autowired
    private MemberInfoInterface memberInfoInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Autowired
    private WorkTeamMessageInterface workTeamMessageInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.workteam_queue.take();// 获取队列处理数据
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
                        case FunctionIdConstant.BLACKLIST_PAGE:
                            resInfo = getBlackList(request_body);
                            break;
                        case FunctionIdConstant.BLACKLIST_ADD:
                            resInfo = addBlack(request_body);
                            break;
                        case FunctionIdConstant.BLACKLIST_DELETE:
                            resInfo = deleteBlack(request_body);
                            break;
                        case FunctionIdConstant.BLACKLIST_WHITE:
                            resInfo = getWhiteList(request_body);
                            break;
                        case FunctionIdConstant.WORKTEAM_REPLY_LIST:
                            resInfo = getReplyList(request_body);
                            break;
                        case FunctionIdConstant.WORKTEAM_LIST:
                            resInfo = getWorkTeamList(request_body);
                            break;
                        case FunctionIdConstant.WORKTEAM_DETAIL_INFO:
                            resInfo = getWorkTeamDetail(request_body);
                            break;
                        case FunctionIdConstant.WORKTEAM_DETAIL_REPLY:
                            resInfo = getWorkTeamReply(request_body);
                            break;
                        case FunctionIdConstant.WORKTEAM_DELETE_INFO:
                            resInfo = deleteWorkTeam(request_body);
                            break;
                        case FunctionIdConstant.WORKTEAM_DELETE_REPLY:
                            resInfo = deleteReply(request_body);
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
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("工作圈黑名单处理类异常:{}", e);
            } finally {

            }

        }
    }

    /**
     * 分页模糊查询获取黑名单列表
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String getBlackList(String requestBody) {
        logger.debug("获取黑名单列表,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            int index = requestJson.getIntValue("index");
            int pageSize = requestJson.getIntValue("pagesize");
            String sessionId = requestJson.getString("sessionid");// 必选
            String memberName = requestJson.getString("membername");
            String telnum = requestJson.getString("telnum");
            /** 校验参数完整性 */
            if (index == 0 || pageSize == 0 || !StringUtils.checkParam(sessionId, true, 36))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String corpId = getInfoFromSession(sessionId, "corpId");
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            /** 调用服务查询数据 */
            Map<String, Object> condition = new HashMap<String, Object>();
            condition.put("EQ_corpId", corpId);
            if (!org.springframework.util.StringUtils.isEmpty(memberName))
                condition.put("LIKE_memberName", memberName);
            if (!org.springframework.util.StringUtils.isEmpty(telnum))
                condition.put("LIKE_telNum", telnum);
            LinkedHashMap<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();
            sortMap.put("memberName", false);
            sortMap.put("telNum", false);
            Map<String, Object> map = blackListInterface.findBlackListOfPage(index, pageSize, condition, sortMap);
            if (map == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");

            long total = (long) map.get("total");
            long pageNum = total / pageSize;
            if (total % pageSize != 0)
                pageNum++;
            map.put("pageNum", pageNum);
            return ResponsePackUtil.buildPack("0000", map);
        } catch (Exception e) {
            logger.error("获取黑名单列表异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 添加黑名单
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String addBlack(String requestBody) {
        logger.debug("添加黑名单,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String members = requestJson.getString("members");
            String sessionId = requestJson.getString("sessionid");// 必选
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(members, true, -1) || !StringUtils.checkParam(sessionId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            String corpId = getInfoFromSession(sessionId, "corpId");
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            JSONArray jsonArray = JSONArray.parseArray(members);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = JSONObject.parseObject(jsonArray.get(i).toString());
                /** 保存数据 */
                BlackListVo blackListVo = new BlackListVo();
                blackListVo.setCorpId(corpId);
                blackListVo.setMemberName(json.get("membername").toString());
                blackListVo.setTelNum(json.get("telnum").toString());
                blackListVo = blackListInterface.save(blackListVo);
            }
            return ResponsePackUtil.buildPack("0000", "");
        } catch (Exception e) {
            logger.error("添加黑名单异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 删除黑名单
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String deleteBlack(String requestBody) throws Exception {
        logger.debug("添加黑名单,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String telnum = requestJson.getString("telnum");
            String sessionId = requestJson.getString("sessionid");// 必选
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(telnum, true, 11) || !StringUtils.checkParam(sessionId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            String corpId = getInfoFromSession(sessionId, "corpId");
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            /** 删除数据 */
            if (!blackListInterface.deleteByCorpIdAndTelNum(corpId, telnum))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3082, "");

            return ResponsePackUtil.buildPack("0000", "");
        } catch (Exception e) {
            logger.error("删除黑名单异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 获取未被加入黑名单的人员，一人多职的情况下只显示一条
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String getWhiteList(String requestBody) throws Exception {
        logger.debug("获取白名单列表,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            int index = requestJson.getIntValue("index");
            int pageSize = requestJson.getIntValue("pagesize");
            String telNum = requestJson.getString("telnum");
            String memberName = requestJson.getString("membername");
            String sessionId = requestJson.getString("sessionid");// 必选
            /** 校验参数完整性 */
            if (0 == index || 0 == pageSize || !StringUtils.checkParam(sessionId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            String corpId = getInfoFromSession(sessionId, "corpId");
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
            /** 查询该企业因存在于黑名单的人员手机号 */
            List<String> list = blackListInterface.findTelNumByCorpId(corpId);
            /** 查询白名单列表 */
            Map<String, Object> map = memberInfoInterface.findPageNotInBlacklist(index, pageSize, corpId, telNum, memberName, list);
            if (map == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");

            int total = (int) map.get("total");
            int pageNum = total / pageSize;
            if (total % pageSize != 0)
                pageNum++;
            map.put("pageNum", pageNum);

            return ResponsePackUtil.buildPack("0000", map);
        } catch (Exception e) {
            logger.error("获取白名单列表异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 分页模糊查询获取工作圈列表
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String getWorkTeamList(String requestBody) throws Exception {
        logger.debug("模糊查询工作圈列表,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            int index = requestJson.getIntValue("index");
            int pageSize = requestJson.getIntValue("pagesize");
            String key = requestJson.getString("key");// 关键字
            String sender = requestJson.getString("sender");// 发起人
            String sessionId = requestJson.getString("sessionid");// 必选
            /** 校验参数完整性 */
            if (0 == index || 0 == pageSize || !StringUtils.checkParam(sessionId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String corpId = getInfoFromSession(sessionId, "corpId");
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_pkCorp", corpId);
            Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
            sortMap.put("pkMessage", false);
            Map<String, Object> map = new HashMap<String, Object>();
            if (!org.springframework.util.StringUtils.isEmpty(key))
                conditions.put("LIKE_content", key);
            if (!org.springframework.util.StringUtils.isEmpty(sender)) {
                Map<String, Object> condition = new HashMap<String, Object>();
                condition.put("LIKE_userName", sender);
                condition.put("EQ_corpId", corpId);
                List<ClientUserVO> userlist = clientUserInterface.findByCondition(condition, null);
                if (userlist == null || userlist.size() == 0) {
                    map.put("content", "");
                    map.put("pageNum", 0);
                    return ResponsePackUtil.buildPack("0000", map);
                }
                String senders = "";
                if (userlist != null && userlist.size() > 0) {
                    for (ClientUserVO clientUserVO : userlist) {
                        senders = senders + clientUserVO.getUserId() + ",";
                    }
                    senders = senders.substring(0, senders.lastIndexOf(","));
                    conditions.put("IN_senderclientid", senders);
                }
            }
            map = workTeamMessageInterface.findAllByPage(index, pageSize, conditions, sortMap);

            if (map == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");

            List<WorkTeamMessageVo> voList = (List<WorkTeamMessageVo>) map.get("content");
            if (null != voList && voList.size() > 0) {
                for (WorkTeamMessageVo workTeamMessageVo : voList) {
                    String userId = workTeamMessageVo.getSenderclientid();
                    if (!org.springframework.util.StringUtils.isEmpty(userId)) {
                        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
                        workTeamMessageVo.setReserve3(clientUserVO == null ? "" : clientUserVO.getTelNum());
                    }
                }
            }
            long total = (long) map.get("total");
            long pageNum = total / pageSize;
            if (total % pageSize != 0)
                pageNum++;
            map.put("pageNum", pageNum);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack("0000", map, ser);
        } catch (Exception e) {
            logger.error("查询工作圈列表异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 分页模糊查询回复列表
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String getReplyList(String requestBody) throws Exception {
        logger.debug("模糊查询工作圈列表,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            int index = requestJson.getIntValue("index");
            int pageSize = requestJson.getIntValue("pagesize");
            String key = requestJson.getString("key");// 关键字
            String sender = requestJson.getString("sender");// 发起人
            String sessionId = requestJson.getString("sessionid");// 必选
            /** 校验参数完整性 */
            if (0 == index || 0 == pageSize || !StringUtils.checkParam(sessionId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            String corpId = getInfoFromSession(sessionId, "corpId");
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_pkCorp", corpId);
            LinkedHashMap<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();
            Map<String, Object> map = new HashMap<String, Object>();
            String ids = "";
            /** 获取该企业所有说说id */
            List<WorkTeamVo> list = workTeamInterface.findMessageByCondition(conditions, null);
            conditions.clear();
            if (null == list || list.size() == 0)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3071, "");
            for (WorkTeamVo workTeamVo : list) {
                WorkTeamMessageVo workTeamMessageVo = workTeamVo.getWorkTeamMessageVo();
                ids = ids + workTeamMessageVo.getPkMessage() + ",";
            }
            ids = ids.substring(0, ids.lastIndexOf(","));
            conditions.put("IN_pkMessage", ids);
            if (!org.springframework.util.StringUtils.isEmpty(key))
                conditions.put("LIKE_content", key);
            if (!org.springframework.util.StringUtils.isEmpty(sender)) {
                Map<String, Object> condition = new HashMap<String, Object>();
                condition.put("LIKE_userName", sender);
                condition.put("EQ_corpId", corpId);
                List<ClientUserVO> userlist = clientUserInterface.findByCondition(condition, null);
                if (userlist == null || userlist.size() == 0) {
                    map.put("content", "");
                    map.put("pageNum", 0);
                    return ResponsePackUtil.buildPack("0000", map);
                }

                String senders = "";
                if (userlist != null && userlist.size() > 0) {
                    for (ClientUserVO clientUserVO : userlist) {
                        senders = senders + clientUserVO.getUserId() + ",";
                    }
                    senders = senders.substring(0, senders.lastIndexOf(","));
                }
                conditions.put("IN_senderclientid", senders);
            }
            sortMap.put("pkMessage", false);
            sortMap.put("pkReply", false);
            conditions.put("EQ_sendtype", 1);// 只查询文本回复 1-文本 2-赞
            map = workTeamInterface.findReplyOfPage(index, pageSize, conditions, sortMap);
            if (map != null && !map.isEmpty()) {
                long total = (long) map.get("total");
                long pageNum = total / pageSize;
                if (total % pageSize != 0)
                    pageNum++;
                map.put("pageNum", pageNum);
            }
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack("0000", map, ser);
        } catch (Exception e) {
            logger.error("查询回复列表异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 获取说说详情-基础信息
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String getWorkTeamDetail(String requestBody) throws Exception {
        logger.debug("获取说说详情,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionid = requestJson.getString("sessionid");
            long id = requestJson.getLongValue("id");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50) || 0 == id)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            String corpId = getInfoFromSession(sessionid, "corpId");
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            WorkTeamVo workTeamVo = workTeamInterface.findMessageById(id);
            if (workTeamVo == null) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3071, "");
            }
            WorkTeamMessageVo workTeamMessageVo = workTeamVo.getWorkTeamMessageVo();
            /** 处理图片地址 */
            List<String> imageList = new ArrayList<String>();
            String images = workTeamMessageVo.getImage();
            if (!org.springframework.util.StringUtils.isEmpty(images)) {
                String[] arrs = images.split(";");
                for (String string : arrs) {
                    if (string.contains(","))
                        ;
                    string = string.substring(0, string.lastIndexOf(","));
                    imageList.add(getFileUrl(string) + string);
                }
            }
            /** 处理头像地址 */
            String userId = workTeamMessageVo.getSenderclientid();
            ClientUserVO clientUserVO = clientUserInterface.findById(userId);
            JSONObject resJson = new JSONObject();
            resJson.put("avatar", clientUserVO == null || org.springframework.util.StringUtils.isEmpty(clientUserVO.getAvatar()) ? "" : getFileUrl(clientUserVO.getAvatar()) + clientUserVO.getAvatar());
            resJson.put("workTeamMessageVo", workTeamMessageVo);
            resJson.put("imageList", imageList);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack("0000", resJson, ser);
        } catch (Exception e) {
            logger.error("获取主贴信息异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 获取说说详情-回复列表
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String getWorkTeamReply(String requestBody) throws Exception {
        logger.debug("获取某条说说回复列表,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionid = requestJson.getString("sessionid");
            long id = requestJson.getLongValue("id");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50) || 0 == id)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            Map<String, Object> conditions = new HashMap<String, Object>();
            LinkedHashMap<String, Boolean> sortMap = new LinkedHashMap<>();
            sortMap.put("pkReply", true);
            conditions.put("EQ_sendtype", 1);
            conditions.put("EQ_pkMessage", id);
            List<WorkTeamReplyVo> replyList = workTeamReplyInterface.findWkReplyByCondition(conditions, sortMap);

            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack("0000", replyList, ser);
        } catch (Exception e) {
            logger.error("根据主贴id获取回复列表异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 删除说说主贴
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String deleteWorkTeam(String requestBody) throws Exception {
        logger.debug("删除说说,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionid = requestJson.getString("sessionid");
            long id = requestJson.getLongValue("id");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50) || 0 == id)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String deleteUserId = getInfoFromSession(sessionid, "userId");
            if (!StringUtils.checkParam(deleteUserId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            workTeamInterface.deleteAndSaveHis(id, deleteUserId);
            return ResponsePackUtil.buildPack("0000", "");
        } catch (Exception e) {
            logger.error("删除说说主贴异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 删除回复
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String deleteReply(String requestBody) throws Exception {
        logger.debug("删除回复,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionid = requestJson.getString("sessionid");
            long replyId = requestJson.getLongValue("replyid");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50) || 0 == replyId)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String deleteUserId = getInfoFromSession(sessionid, "userId");
            if (!StringUtils.checkParam(deleteUserId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            workTeamInterface.deleteReplyAndSaveHis(replyId, deleteUserId);
            return ResponsePackUtil.buildPack("0000", "");
        } catch (Exception e) {
            logger.error("删除回复异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    public String getInfoFromSession(String sessionId, String key) {
        String session = redisInterface.getString(Constants.nameSpace + sessionId);
        JSONObject sessionJson = JSONObject.parseObject(session);
        if (null == sessionJson)
            return null;
        String corpId = sessionJson.getString(key);
        if (!StringUtils.checkParam(corpId, true, -1))
            return null;
        return corpId;
    }

    /**
     * 根据文件路径判断文件服务器地址
     * 
     * @param filepath
     * @return
     */
    public String getFileUrl(String filepath) {
        if (org.springframework.util.StringUtils.isEmpty(filepath))
            return null;
        return filepath.startsWith("/group") ? ParamConfig.FILE_SERVER_URL : ParamConfig.NGINX_ADDRESS;
    }
}
