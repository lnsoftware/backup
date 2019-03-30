/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.royasoft.vwt.controller.config.ParamConfig;
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.Base64Util;
import com.royasoft.vwt.controller.util.MemberInfoUtil;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.RocketMqUtil;
import com.royasoft.vwt.controller.util.StringUtils;
import com.royasoft.vwt.controller.util.VerifycodeImageUtil;
import com.royasoft.vwt.controller.util.mq.AnnounceAction;
import com.royasoft.vwt.controller.util.mq.RedisAction;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.ImRedisInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendProvinceSmsInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceAnnexInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceContentInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceHisInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceInfoInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceReceiverInterface;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceAnnexVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceContentVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceHisVO;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceInfoVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceReceiverVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 公告处理类
 * 
 * @author daizl
 *
 */
@Scope("prototype")
@Service
public class AnnounceService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AnnounceService.class);

    private final String actionIdNameSpace = "ROYASOFT:VWT:ID:ACTION";

    private final String actionEntityNameSpace = "ROYASOFT:VWT:ENTITY:ACTION:";

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
    private DepartMentInterface departMentInterface;

    @Autowired
    private CorpInterface corpInterface;

    @Autowired
    private MemberInfoInterface memberInfoInterface;

    @Autowired
    private SendProvinceSmsInterface sendProvinceSmsInterface;

    @Autowired
    private AnnounceInfoInterface announceInfoInterface;

    @Autowired
    private AnnounceHisInterface announceHisInterface;

    @Autowired
    private AnnounceContentInterface announceContentInterface;

    @Autowired
    private AnnounceReceiverInterface announceReceiverInterface;

    @Autowired
    private AnnounceAnnexInterface announceAnnexInterface;

    @Autowired
    private DatabaseInterface databaseInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Autowired
    private ImRedisInterface imRedisInterface;

    @Autowired
    private MemberInfoUtil memberInfoUtil;

    @Autowired
    private ZkUtil zkUtil;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.announce_queue.take();// 获取队列处理数据
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
                    /** 获取部门树 */
                        case FunctionIdConstant.ANNOUNCE_DEPARTLIST:
                            resInfo = getDepartByCorpId(request_body);
                            break;
                        /** 根据部门节点获取人员 */
                        case FunctionIdConstant.ANNOUNCE_USERLIST:
                            resInfo = getMemberByDepartId(request_body);
                            break;
                        /** 增加公告 */
                        case FunctionIdConstant.ANNOUNCE_ADD:
                            resInfo = addAnnounce(request_body);
                            break;
                        /** 删除公告 */
                        case FunctionIdConstant.ANNOUNCE_DELETE:
                            resInfo = deleteAnnounce(request_body);
                            break;
                        /** 修改公告 */
                        case FunctionIdConstant.ANNOUNCE_EDIT:
                            resInfo = editAnnounce(request_body);
                            break;
                        /** 获取公告详情 */
                        case FunctionIdConstant.ANNOUNCE_GETDETAIL:
                            resInfo = getAnnounce(request_body);
                            break;
                        /** 获取公告列表 */
                        case FunctionIdConstant.ANNOUNCE_GETLIST:
                            resInfo = getAnnounceList(request_body);
                            break;
                        /** 公告置顶 */
                        case FunctionIdConstant.ANNOUNCE_TOP:
                            resInfo = topAnnounce(request_body);
                            break;
                        /** 公告发布/取消发布 */
                        case FunctionIdConstant.ANNOUNCE_PUBLISHANDCANCEL:
                        	resInfo = releaseAnnounce(request_body);
                        	break;
                        /** 公告获取图片验证码 */
                        case FunctionIdConstant.ANNOUNCE_GETVERIFYCODE:
                            resInfo = getVirifycode(request_body);
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
                logger.error("公告处理类异常:{}", e);
            } finally {

            }

        }
    }

    /**
     * 分页模糊查询公告列表
     * 
     * @param requestBody
     * @return
     */
    public String getAnnounceList(String requestBody) {
        logger.debug("查询公告列表,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionId = requestJson.getString("sessionid");
            String senderName = requestJson.getString("sendername");// 发布人姓名
            String announceTitle = requestJson.getString("announcetitle");// 公告名称
            String sendTime = requestJson.getString("sendtime");// 发布时间
            String createtime = requestJson.getString("createtime");// 创建时间
            String endTime = requestJson.getString("endtime");// 截止时间
            int index = requestJson.getIntValue("index");// 页码
            int pagesize = requestJson.getIntValue("pagesize");// 每页条数
            String status = requestJson.getString("status");// 发布状态0-不发布，1发布
            
            if (!StringUtils.checkParam(sessionId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String corpId = getCorpIdFromSession(sessionId);
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            Map<String, Object> conditions = new HashMap<String, Object>();
            if (!org.springframework.util.StringUtils.isEmpty(senderName))
                conditions.put("LIKE_sendPersonName", senderName);
            if (!org.springframework.util.StringUtils.isEmpty(announceTitle))
                conditions.put("LIKE_announceTitle", announceTitle);
            if (!org.springframework.util.StringUtils.isEmpty(sendTime)) {
                sendTime = sendTime + " 00:00:00";
                conditions.put("start_time_sendTime", sendTime);
            }
            if (!org.springframework.util.StringUtils.isEmpty(createtime)) {
            	createtime = createtime + " 00:00:00";
            	conditions.put("start_time_createTime", createtime);
            }
            if("1".equals(status)||"0".equals(status)){
            	conditions.put("EQ_status", status);
            }
            if (!org.springframework.util.StringUtils.isEmpty(endTime)) {
                endTime = getSpecifiedDayAfter(endTime) + " 00:00:00";
                conditions.put("end_time_stopTime", endTime);
            }
            conditions.put("EQ_corpId", corpId);
            LinkedHashMap<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();
            // Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
            sortMap.put("createTime", false);

            Map<String, Object> map = announceInfoInterface.findAllByPage(index, pagesize, conditions, sortMap);
            if (map == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");

            long total = (long) map.get("total");
            long count = total / pagesize;
            if (total % pagesize != 0)
                count++;
            map.put("pageNum", count);
            logger.debug("获取公告信息,requestbody:{},announceInfo:{},count:{}", requestBody, JSON.toJSONString(map), count);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack("0000", map, ser);
        } catch (Exception e) {
            logger.error("查询公告列表异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 删除公告
     * 
     * @param requestBody
     * @return
     */
    public String deleteAnnounce(String requestBody) {
        logger.debug("批量删除公告,requestBody{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String ids = requestJson.getString("ids");
            String sessionId = requestJson.getString("sessionid");
            if (!StringUtils.checkParam(sessionId, true, 50) || !StringUtils.checkParam(ids, true, -1))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String corpId = getCorpIdFromSession(sessionId);
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            String[] idArr = ids.split(";");
            for (int i = 0; i < idArr.length; i++) {
                long id = Long.parseLong(idArr[i]);
                AnnounceVo announceVo = announceInfoInterface.findAnnounceById(id);
                if (null != announceVo) {
                    boolean flag = announceInfoInterface.deleteById(id);
                    if (flag) {
                        announceHisInterface.saveAnnounceHis(transEntityToAnnounceInfoVo(announceVo));
                    }
                    logger.debug("公告，id{},删除状态{}", id, flag);
                }
            }
            return ResponsePackUtil.buildPack("0000", "");
        } catch (Exception e) {
            logger.error("批量删除公告异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 发布公告
     * 
     * @param requestBody
     * @return
     */
    public String releaseAnnounce(String requestBody) {
        logger.debug("发布/取消发布（草稿） 公告,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            long id = requestJson.getLongValue("id");// 公告id
            Integer status = requestJson.getIntValue("status");// 公告状态 0草稿 1发布
            String sessionId = requestJson.getString("sessionid");
            if (!StringUtils.checkParam(sessionId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String corpId = getCorpIdFromSession(sessionId);
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
            /** 校验公告是否存在 */
            AnnounceInfoVo announceInfoVO = announceInfoInterface.findById(id);
            if (null == announceInfoVO)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2061, "");

            announceInfoVO.setStatus(status);
            announceInfoVO = announceInfoInterface.saveAnnounceInfo(announceInfoVO);
            if (announceInfoVO == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2062, "");

            return ResponsePackUtil.buildPack("0000", "");
        } catch (Exception e) {
            logger.error("发布/取消发布（草稿） 公告异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }
    /**
     * 置顶/取消置顶 公告
     * 
     * @param requestBody
     * @return
     */
    public String topAnnounce(String requestBody) {
    	logger.debug("置顶/取消置顶 公告,requestBody:{}", requestBody);
    	try {
    		JSONObject requestJson = JSONObject.parseObject(requestBody);
    		long id = requestJson.getLongValue("id");// 公告id
    		long isTop = requestJson.getLongValue("istop");// 置顶标识
    		String sessionId = requestJson.getString("sessionid");
    		if (!StringUtils.checkParam(sessionId, true, 50) || !StringUtils.checkParam(isTop + "", true, 1) || id == 0)
    			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
    		
    		String corpId = getCorpIdFromSession(sessionId);
    		if (!StringUtils.checkParam(corpId, true, 32))
    			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
    		/** 校验公告是否存在 */
    		AnnounceInfoVo announceInfoVO = announceInfoInterface.findById(id);
    		if (null == announceInfoVO)
    			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2061, "");
    		
    		announceInfoVO.setIsTop(isTop);
    		announceInfoVO = announceInfoInterface.saveAnnounceInfo(announceInfoVO);
    		if (announceInfoVO == null)
    			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2062, "");
    		
    		return ResponsePackUtil.buildPack("0000", "");
    	} catch (Exception e) {
    		logger.error("置顶/取消置顶 公告异常,requestBody:{},e:{}", requestBody, e);
    		return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
    	}
    }

    /**
     * 获取公告详情
     * 
     * @param requestBody
     * @return
     */
    public String getAnnounce(String requestBody) {
        logger.debug("获取公告详情requestbody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionId = requestJson.getString("sessionid");
            long id = requestJson.getLongValue("id");// 公告id
            if (!StringUtils.checkParam(sessionId, true, 50) || id == 0)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String corpId = getCorpIdFromSession(sessionId);
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            CorpVO corpVo = corpInterface.findById(corpId);

            AnnounceVo announceVo = announceInfoInterface.findAnnounceById(id);
            if (null == announceVo)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2061, "");

            List<AnnounceContentVo> contentList = announceVo.getAnnounceContentVoList();
            StringBuffer contentStr = new StringBuffer();
            if (contentList != null && !contentList.isEmpty()) {
                /** 改造前，公告有多个段落，改造后编辑页只有一个段落，需要处理旧数据中的多个段落合并到一个段落中 */
                AnnounceContentVo dealAnnounceContent = new AnnounceContentVo();
                for (AnnounceContentVo announceContentVo : contentList) {
//                    String imgpath = announceContentVo.getAnnouncePic();
                    String content = announceContentVo.getAnnounceContent();
//                    if (!org.springframework.util.StringUtils.isEmpty(imgpath)) {
//
//                        contentStr.append("<img src='" + getFileUrl(imgpath) + imgpath + "'>");
//                    }
                    if (!org.springframework.util.StringUtils.isEmpty(content))
                        contentStr.append(content);
                }
                contentList.clear();
                dealAnnounceContent.setAnnounceContent(contentStr.toString());
                dealAnnounceContent.setAnnounceId(id);
                dealAnnounceContent.setAnnounceIndex(1L);
                dealAnnounceContent.setAnnouncePic("");
                contentList.add(dealAnnounceContent);
                announceVo.setAnnounceContentVoList(contentList);
            }
            /** 处理附件 */
            List<AnnounceAnnexVo> annexList = announceVo.getAnnounceAnnexVoList();
            if (annexList != null && annexList.size() > 0) {
                for (AnnounceAnnexVo announceAnnexVo : annexList) {
                    announceAnnexVo.setAnnexUrlAbsolute(getFileUrl(announceAnnexVo.getAnnexUrl()) + announceAnnexVo.getAnnexUrl());
                }
            }
            /** 处理公告封面 */
            AnnounceInfoVo announceInfoVo = announceVo.getAnnounceInfoVo();
            if (announceInfoVo != null) {
                announceInfoVo.setAnnounceContent(contentStr.toString());
                if (StringUtils.isEmpty(announceInfoVo.getAnnounceCover())) {
                	announceInfoVo.setAnnounceCoverAbsolute(getFileUrl(zkUtil.findData("/royasoft/vwt/controller/announcecover")) + zkUtil.findData("/royasoft/vwt/controller/announcecover"));
				}else{
					announceInfoVo.setAnnounceCoverAbsolute(getFileUrl(announceInfoVo.getAnnounceCover()) + announceInfoVo.getAnnounceCover());
				}
            }

            /** 处理接收人 */
            List<AnnounceReceiverVo> announceReceiverList = announceVo.getAnnounceReceiverList();
            if (announceReceiverList != null && announceReceiverList.size() > 0) {
                for (AnnounceReceiverVo announceReceiverVo : announceReceiverList) {
                    /** 部门 */
                    if (announceReceiverVo.getType() == 1) {
                        DepartMentVO departMentVO = departMentInterface.findById(announceReceiverVo.getReceiverId());
                        announceReceiverVo.setReceiverName(departMentVO == null ? "" : departMentVO.getPartName());
                    } else {
                        MemberInfoVO memberInfo = memberInfoUtil.findMemberInfoById(announceReceiverVo.getReceiverId(), corpVo.getFromchannel());
                        announceReceiverVo.setReceiverName(memberInfo == null ? "" : memberInfo.getMemberName());
                    }
                }
            }
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            System.out.println(JSON.toJSONString(announceVo));
            return ResponsePackUtil.buildPack("0000", announceVo, ser);
        } catch (Exception e) {
            logger.error("查询公告详情异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 新增公告
     * 
     * @param requestBody
     * @return
     */
    public String addAnnounce(String requestBody) {
        logger.debug("新增公告,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String isPushAllCorp = requestJson.getString("ispushallcorp");// 是否全企业推送,0否，1是 必选
            String sessionId = requestJson.getString("sessionid");// 必选
            long sendType = requestJson.getLong("sendtype");// 发布类型：1是部门，2是人员 必选
            String sendPersonName = requestJson.getString("sendpersonname");// 发布人名称
            String sendPersonId = requestJson.getString("sendpersonid");// 发布人ID
            String sendPartName = requestJson.getString("sendpartname");// 发布部门名称
            String sendPartId = requestJson.getString("sendpartid");// 发布部门ID
            String receiverId = requestJson.getString("receiverid");// 接收人员ID （;隔开）
            String receiverPartId = requestJson.getString("receiverpartid");// 接收部门ID（;隔开）
            String stopTime = requestJson.getString("stoptime");// 截止时间 必选
            long isTop = requestJson.getLongValue("istop");// 是否置顶（1置顶；0不置顶）可选
            String announceTitle = requestJson.getString("announcetitle");// 公告标题 必选
            String announceCover = requestJson.getString("announcecover");// 获取封面图片地址 必选
            String isSendMsg = requestJson.getString("issendmsg");// 是否需要短信推送（1短信推送；0不推送） 可选
            String content = requestJson.getString("content");// 公告正文 必选
            String annex = requestJson.getString("annex");// 公告附件 可选
            String code = requestJson.getString("code");// 验证码
            String sendTime = requestJson.getString("sendtime");// 开始时间
            Integer rateTimes = requestJson.getIntValue("rateTimes");// 频率 0 一次 1多次
            Integer status = requestJson.getIntValue("status");// 发布状态 0草稿 1发布
            
            String singlePic = requestJson.getString("singlePic");// 单图地址
            String checkUrl = requestJson.getString("checkUrl");// 单图点击地址
            
            String patternTag = "</?[a-zA-Z]+[^><]*>";
            String patternBlank = "(^\\s*)|(\\s*$)";
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(singlePic)){
                // 正则去除html标签 跟空字符串
            	String pic =  content.replaceAll(patternTag, "").replaceAll(patternBlank, "").replaceFirst("(&nbsp;)+", "").replaceAll("&nbsp;", " ");
            	if (org.apache.commons.lang3.StringUtils.isNotEmpty(pic)) {
            		singlePic = "";
				}
        }
            if (org.apache.commons.lang3.StringUtils.isEmpty(announceCover)){
            	announceCover = zkUtil.findData("/royasoft/vwt/controller/announcecover");
            }

            

            if (!StringUtils.checkParam(sessionId, true, 50) || 0 == sendType || !StringUtils.checkParam(stopTime, true, 20) | !StringUtils.checkParam(announceTitle, true, 60)
                    || !StringUtils.checkParam(content, true, -1) || !StringUtils.checkParam(sendPersonName, false, 30) || !StringUtils.checkParam(sendPartName, false, 30)
                    || !StringUtils.checkParam(sendPersonId, false, 32) || !StringUtils.checkParam(sendPartId, false, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String corpId = getCorpIdFromSession(sessionId);
            String userId = getUserIdFromSession(sessionId);
            if (!StringUtils.checkParam(corpId, true, 32) || !StringUtils.checkParam(userId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
            CorpVO corpVo = corpInterface.findById(corpId);
            if (corpVo == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2052, "");

            if ("1".equals(isSendMsg)) {
                String RightCode = redisInterface.getString(Constants.ANNOUNCECODE_NAMESPACE + corpId);
                logger.debug("正确密码:{},输入密码:{}", RightCode, code);
                if (!StringUtils.checkParam(code, true, 4) || !code.toLowerCase().equals(RightCode))
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2063, "");
            }

            sendTime = sendTime+" 00:00:00";
            stopTime = stopTime + " 23:59:59";
            String senderName = sendPersonName;
            String senderId = sendPersonId;
            /** 部门发送 */
            if (1 == sendType) {
                senderId = sendPartId;
                senderName = sendPartName;
            }
            /** 判断是否全企业推送 */
            if ("1".equals(isPushAllCorp)) {
                DepartMentVO depart = getCorpDepartInfo(corpId);
                receiverPartId = depart.getDeptId();
                List<String> list = new ArrayList<String>();
                list.add(receiverPartId);
                receiverPartId = JSON.toJSONString(list);
            }
            long id = databaseInterface.generateId("vwt_announce_info", "id");
            /** 保存公告正文 */
            saveContent(id, content, null,singlePic);
            /** 保存announce_annex表 */
            saveAnnex(id, annex, null);
            /** 保存announce_receiver表 */
            saveReceiveInfo(receiverPartId, 1L, id, null);
            saveReceiveInfo(receiverId, 0L, id, null);
            /** 保存announce_info表 */
            saveInfo(id, sendType, senderId, senderName, sendPartName, announceTitle, announceCover, isTop, stopTime,sendTime,rateTimes,checkUrl,status, corpId, null, corpVo.getFromchannel());
            //一次弹窗接受用户存redis
            if (0 ==rateTimes) {
            	// 获取推送用户id
            	List<String> toUsers = queryUserIds(receiverId, receiverPartId, corpId);
            	logger.debug("查询通讯录，toUsers{}", JSON.toJSONString(toUsers));
            	// 根据ID查询已激活用户信息
            	List<ClientUserVO> cvList = queryClientUserByListMenId(toUsers);
            	for (ClientUserVO clientUserVO : cvList) {
            		redisInterface.setString("ANNOUNCE:"+id+clientUserVO.getUserId(), clientUserVO.getUserId());
				}
			}
            /** 推送消息 */
//            push("create", receiverId, receiverPartId, corpId, announceTitle, isSendMsg, id);
            return ResponsePackUtil.buildPack("0000", "");
        } catch (Exception e) {
            logger.error("新增公告异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 修改公告
     * 
     * @param requestBody
     * @return
     */
    public String editAnnounce(String requestBody) {
        logger.debug("修改公告,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            long id = requestJson.getLongValue("id");// 公告id
            String isPushAllCorp = requestJson.getString("ispushallcorp");// 是否全企业推送,0否，1是 必选
            String sessionId = requestJson.getString("sessionid");// 必选
            long sendType = requestJson.getLong("sendtype");// 发布类型：1是部门，2是人员 必选
            String sendPersonName = requestJson.getString("sendpersonname");// 发布人名称
            String sendPersonId = requestJson.getString("sendpersonid");// 发布人ID
            String sendPartName = requestJson.getString("sendpartname");// 发布部门名称
            String sendPartId = requestJson.getString("sendpartid");// 发布部门ID
            String receiverId = requestJson.getString("receiverid");// 接收人员ID （;隔开）
            String receiverPartId = requestJson.getString("receiverpartid");// 接收部门ID（;隔开）
            String stopTime = requestJson.getString("stoptime");// 截止时间 必选
            long isTop = requestJson.getLongValue("istop");// 是否置顶（1置顶；0不置顶）可选
            String announceTitle = requestJson.getString("announcetitle");// 公告标题 必选
            String announceCover = requestJson.getString("announcecover");// 获取封面图片地址 必选
            String isSendMsg = requestJson.getString("issendmsg");// 是否需要短信推送（1短信推送；0不推送） 可选
            String content = requestJson.getString("content");// 公告正文 必选
            String annex = requestJson.getString("annex");// 公告附件 可选
            String code = requestJson.getString("code");
            String sendTime = requestJson.getString("sendtime");// 开始时间
            Integer rateTimes = requestJson.getIntValue("rateTimes");// 频率 0 一次 1多次
            Integer status = requestJson.getIntValue("status");// 发布状态 0草稿 1发布
            
            String checkUrl = requestJson.getString("checkUrl");// 单图点击地址
            String singlePic = requestJson.getString("singlePic");// 单图地址
            
            
        	String patternTag = "</?[a-zA-Z]+[^><]*>";
            String patternBlank = "(^\\s*)|(\\s*$)";
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(singlePic)){
                // 正则去除html标签 跟空字符串
            	String pic =  content.replaceAll(patternTag, "").replaceAll(patternBlank, "").replaceFirst("(&nbsp;)+", "").replaceAll("&nbsp;", " ");
            	if (org.apache.commons.lang3.StringUtils.isNotEmpty(pic)) {
            		singlePic = "";
				}
            }
            
            if (org.apache.commons.lang3.StringUtils.isEmpty(announceCover)){
            	announceCover = zkUtil.findData("/royasoft/vwt/controller/announcecover");
            }
            logger.info("singlePic:{},announceCover:{}",singlePic,announceCover);

            if (id == 0 || !StringUtils.checkParam(sessionId, true, 50) || 0 == sendType || !StringUtils.checkParam(stopTime, true, 20) | !StringUtils.checkParam(announceTitle, true, 60)
                    || !StringUtils.checkParam(content, true, -1) || !StringUtils.checkParam(sendPersonName, false, 30) || !StringUtils.checkParam(sendPartName, false, 30)
                    || !StringUtils.checkParam(sendPersonId, false, 32) || !StringUtils.checkParam(sendPartId, false, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String corpId = getCorpIdFromSession(sessionId);
            String userId = getUserIdFromSession(sessionId);
            if (!StringUtils.checkParam(corpId, true, 32) || !StringUtils.checkParam(userId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
            CorpVO corpVo = corpInterface.findById(corpId);
            if (corpVo == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2052, "");

            if ("1".equals(isSendMsg)) {
                String RightCode = redisInterface.getString(Constants.ANNOUNCECODE_NAMESPACE + corpId);
                logger.debug("正确密码:{},输入密码:{}", RightCode, code);
                if (!StringUtils.checkParam(code, true, 4) || !code.toLowerCase().equals(RightCode))
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2063, "");
            }
            sendTime = sendTime +  " 00:00:00";
            stopTime = stopTime + " 23:59:59";
            AnnounceVo announceVo = announceInfoInterface.findAnnounceById(id);
            if (null == announceVo)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2061, "");

            String senderName = sendPersonName;
            String senderId = sendPersonId;
            /** 部门发送 */
            if (1 == sendType) {
                senderId = sendPartId;
                senderName = sendPartName;
            }
            /** 判断是否全企业推送 */
            if ("1".equals(isPushAllCorp)) {
                DepartMentVO depart = getCorpDepartInfo(corpId);
                receiverPartId = depart.getDeptId();
                List<String> list = new ArrayList<String>();
                list.add(receiverPartId);
                receiverPartId = JSON.toJSONString(list);
            }

            /** 保存公告正文 */
            saveContent(id, content, announceVo.getAnnounceContentVoList(),singlePic);
            /** 保存announce_annex表 */
            saveAnnex(id, annex, announceVo.getAnnounceAnnexVoList());
            /** 保存announce_receiver表 */
            saveReceiveInfo(receiverPartId, 1L, id, announceVo.getAnnounceReceiverList());
            saveReceiveInfo(receiverId, 0L, id, announceVo.getAnnounceReceiverList());
            /** 保存announce_info表 */
            AnnounceInfoVo infoVo = saveInfo(id, sendType, senderId, senderName, sendPartName, announceTitle, announceCover, isTop, stopTime,sendTime,rateTimes,checkUrl,status,corpId, announceVo.getAnnounceInfoVo(), corpVo.getFromchannel());
           
          //一次弹窗接受用户存redis
            if (0 ==rateTimes) {
				
            	// 获取推送用户id
            	List<String> toUsers = queryUserIds(receiverId, receiverPartId, corpId);
            	logger.debug("查询通讯录，toUsers{}", JSON.toJSONString(toUsers));
            	// 根据ID查询已激活用户信息
            	List<ClientUserVO> cvList = queryClientUserByListMenId(toUsers);
            	for (ClientUserVO clientUserVO : cvList) {
            		redisInterface.setString("ANNOUNCE:"+id+clientUserVO.getUserId(), clientUserVO.getUserId());
				}
			}
            
            /** 推送消息当前时间大于开始时间 */
            if (new Date().getTime()>infoVo.getSendTime().getTime()) {
				
            	push("edit", receiverId, receiverPartId, corpId, announceTitle, isSendMsg, id);
			}
            return ResponsePackUtil.buildPack("0000", "");
        } catch (Exception e) {
            logger.error("修改公告异常:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 保存公告正文
     * 
     * @param announceId
     * @param content
     */
    public AnnounceContentVo saveContent(long announceId, String content, List<AnnounceContentVo> oldList,String singlePic) throws Exception {
        long announceContentId = databaseInterface.generateId("vwt_announce_content", "id");
        /** 修改情况下，维护历史数据 */
        if (null != oldList && !oldList.isEmpty()) {
            announceContentId = oldList.get(0).getId();
            /** 对于历史数据，存在多条的情况，删除除第一条以外的数据 */
            if (oldList.size() > 1) {
                for (int i = 0; i < oldList.size(); i++) {
                    if (i != 0)
                        announceContentInterface.deleteById(oldList.get(i).getId());
                }
            }
        }

        /** 保存announce_content表 */
        AnnounceContentVo announceContentVo = new AnnounceContentVo();
        announceContentVo.setId(announceContentId);
        announceContentVo.setAnnounceId(announceId);
        announceContentVo.setAnnounceContent(content);
        announceContentVo.setAnnouncePic(singlePic);
        announceContentVo.setAnnounceIndex(1L);
        return announceContentInterface.saveAnnounceContent(announceContentVo);
    }

    /**
     * 保存附件
     * 
     * @param announceId
     * @param annex
     * @return
     */
    public List<AnnounceAnnexVo> saveAnnex(long announceId, String annex, List<AnnounceAnnexVo> oldList) {
        JSONArray jsonArray = JSON.parseArray(annex);
        if (oldList != null && !oldList.isEmpty())
            announceAnnexInterface.deleteByAnnounceId(announceId);

        List<AnnounceAnnexVo> list = new ArrayList<AnnounceAnnexVo>();
        if (!jsonArray.isEmpty()) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject annexDetail = JSONObject.parseObject(jsonArray.get(i).toString());
                AnnounceAnnexVo announceAnnexVo = new AnnounceAnnexVo();
                String annexName = annexDetail.getString("annexName");
                String annexUrl = annexDetail.getString("annexUrl");
                double filelength = annexDetail.getDoubleValue("annexSize");
                announceAnnexVo.setAnnexSize(filelength);
                announceAnnexVo.setAnnexName(annexName);
                announceAnnexVo.setAnnexUrl(annexUrl);
                announceAnnexVo.setAnnounceId(announceId);
                announceAnnexVo.setId(databaseInterface.generateId("vwt_announce_annex", "id"));
                announceAnnexVo.setSort((long) (i + 1));
                announceAnnexVo = announceAnnexInterface.saveAnnounceAnnexVo(announceAnnexVo);
                list.add(announceAnnexVo);
            }
        }
        return list;
    }

    /**
     * 保存公告主表信息
     * 
     * @param id 公告id
     * @param sendType 发送类型,1-部门，2-个人
     * @param senderId 发送者id
     * @param senderName 发送者名称
     * @param sendPartName 发送部门名称
     * @param announceCover 公告封面
     * @param announceTitle 公告标题
     * @param isTop 是否置顶 0-否，1-是
     * @param stopTime 结束时间
     * @param corpId 企业id
     * @return
     * @throws Exception
     */
    public AnnounceInfoVo saveInfo(long id, long sendType, String senderId, String senderName, String sendPartName, String announceTitle, String announceCover, long isTop, String stopTime,
    		String sendTime,Integer rateTimes,String checkUrl,Integer status,String corpId, AnnounceInfoVo oldInfo, long fromchannel) throws Exception {
        AnnounceInfoVo announceInfoVo = new AnnounceInfoVo();
        /** 如果是新增的情况 */
        if (null == oldInfo) {
            announceInfoVo.setCreateTime(new Date());
            announceInfoVo.setPushStatus(0);
        } else {
            announceInfoVo = oldInfo;
        }
        announceInfoVo.setId(id);
        announceInfoVo.setSendType(sendType);
        announceInfoVo.setSendPerson(senderId);
        announceInfoVo.setAnnounceCover(announceCover);
        announceInfoVo.setAnnounceTitle(announceTitle);
        if (org.springframework.util.StringUtils.isEmpty(sendPartName)) {
            MemberInfoVO memberInfoVO = memberInfoUtil.findMemberInfoById(senderId, fromchannel);
            sendPartName = memberInfoVO == null ? "" : memberInfoVO.getPartName();
        }
        announceInfoVo.setPartName(sendPartName);
        announceInfoVo.setSendPersonName(senderName);
        announceInfoVo.setIsTop(isTop);
        announceInfoVo.setSendTime(sdf.parse(sendTime));
        announceInfoVo.setStopTime(sdf.parse(stopTime));
        announceInfoVo.setCorpId(corpId);
        announceInfoVo.setRateTimes(rateTimes);
        announceInfoVo.setStatus(status);
        announceInfoVo.setCheckUrl(checkUrl);
        return announceInfoInterface.saveAnnounceInfo(announceInfoVo);
    }

    /**
     * 保存接收人
     * 
     * @return
     */
    private boolean saveReceiveInfo(String receiveuserId, long type, Long announceId, List<AnnounceReceiverVo> oldList) {
        JSONArray receiveuserIds = JSONArray.parseArray(receiveuserId);
        if (receiveuserIds.isEmpty()) {
            return false;
        }
        if (null != oldList && !oldList.isEmpty()) {
            announceReceiverInterface.deleteByAnnounceId(announceId);
        }
        try {
            for (int i = 0; i < receiveuserIds.size(); i++) {
                AnnounceReceiverVo announceReceiverVo = new AnnounceReceiverVo();
                announceReceiverVo.setId(databaseInterface.generateId("vwt_announce_receiver", "id"));
                announceReceiverVo.setAnnounceId(announceId);
                announceReceiverVo.setReceiverId(receiveuserIds.getString(i));
                announceReceiverVo.setType(type);
                announceReceiverInterface.saveAnnounceReceiver(announceReceiverVo);
            }
        } catch (Exception e) {
            logger.error("保存接收人异常！", e);
            return false;
        }
        return true;
    }

    /**
     * 获取根节点部门
     * 
     * @param corpId
     * @return
     */
    public DepartMentVO getCorpDepartInfo(String corpId) {
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("EQ_corpId", corpId);
        conditions.put("EQ_parentDeptNum", "1");
        List<DepartMentVO> content = departMentInterface.findAllEnterprise(conditions, null);
        logger.debug("查询企业根部门信息，content{}", null == content ? "null" : content.size());
        if (null != content && !content.isEmpty()) {
            return content.get(0);
        }
        return null;
    }

    /**
     * 根据企业id获取部门树
     * 
     * @param requestBody
     * @return
     * @author daizl 2016年5月26日
     */
    public String getDepartByCorpId(String requestBody) {
        logger.debug("公告新增/编辑-获取部门,requestBody:{}", requestBody);

        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionid = requestJson.getString("sessionid");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            /** 获取企业id */
            String corpid = getCorpIdFromSession(sessionid);
            if (!StringUtils.checkParam(corpid, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            /** 根据企业id查询企业部门 */
            List<DepartMentVO> list = departMentInterface.findByCorpId(corpid);
            if (list == null || list.isEmpty())
                list = new ArrayList<DepartMentVO>();
            return ResponsePackUtil.buildPack("0000", list);
        } catch (Exception e) {
            logger.error("未注册人员-获取部门异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }

    }

    /**
     * 根据部门id获取人员
     * 
     * @param requestBody
     * @return
     * @author daizl 2016年5月26日
     */
    public String getMemberByDepartId(String requestBody) {
        logger.debug("公告新增/编辑-获取人员,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String deptid = requestJson.getString("deptid");
            String sessionid = requestJson.getString("sessionid");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50) || !StringUtils.checkParam(deptid, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            /** 获取企业id */
            String corpId = getCorpIdFromSession(sessionid);
            if (!StringUtils.checkParam(corpId, true, 32))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            /** 校验该企业是否存在 */
            CorpVO corpVO = corpInterface.findById(corpId);
            if (corpVO == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2052, "");

            /** 根据企业渠道，查询对应通讯录表 */
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_deptId", deptid);
            List<MemberInfoVO> list = memberInfoUtil.findByFromchannel(conditions, null, corpVO.getFromchannel() + "");

            return ResponsePackUtil.buildPack("0000", list);
        } catch (Exception e) {
            logger.error("未注册人员-获取人员异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    public String getCorpIdFromSession(String sessionId) {
        String session = redisInterface.getString(Constants.nameSpace + sessionId);
        JSONObject sessionJson = JSONObject.parseObject(session);
        if (null == sessionJson)
            return null;
        String corpId = sessionJson.getString("corpId");
        if (!StringUtils.checkParam(corpId, true, 32))
            return null;
        return corpId;
    }

    public String getUserIdFromSession(String sessionId) {
        String session = redisInterface.getString(Constants.nameSpace + sessionId);
        JSONObject sessionJson = JSONObject.parseObject(session);
        if (null == sessionJson)
            return null;
        String userId = sessionJson.getString("userId");
        if (!StringUtils.checkParam(userId, true, 50))
            return null;
        return userId;
    }

    /**
     * 封装公告历史信息对象
     * 
     * @param announceVo
     * @return
     */
    public AnnounceHisVO transEntityToAnnounceInfoVo(AnnounceVo announceVo) {
        AnnounceHisVO announceHisVO = new AnnounceHisVO();
        try {
            announceHisVO.setId(announceVo.getAnnounceInfoVo().getId());
            announceHisVO.setIsTop(announceVo.getAnnounceInfoVo().getIsTop());
            announceHisVO.setAnnounceCover(announceVo.getAnnounceInfoVo().getAnnounceCover());
            announceHisVO.setAnnounceTitle(announceVo.getAnnounceInfoVo().getAnnounceTitle());
            announceHisVO.setCreateTime(announceVo.getAnnounceInfoVo().getCreateTime());
            announceHisVO.setCorpId(announceVo.getAnnounceInfoVo().getCorpId());
            announceHisVO.setPartName(announceVo.getAnnounceInfoVo().getPartName());
            announceHisVO.setSendPerson(announceVo.getAnnounceInfoVo().getSendPerson());
            announceHisVO.setSendPersonName(announceVo.getAnnounceInfoVo().getSendPersonName());
            announceHisVO.setSendTime(announceVo.getAnnounceInfoVo().getSendTime());
            announceHisVO.setStopTime(announceVo.getAnnounceInfoVo().getStopTime());
            announceHisVO.setVersion(announceVo.getAnnounceInfoVo().getVersion());
            announceHisVO.setSendType(announceVo.getAnnounceInfoVo().getSendType());
            announceHisVO.setDetailHtmlUrl(announceVo.getAnnounceInfoVo().getDetailHtmlUrl());
            announceHisVO.setDeleteTime(new Date());
            if (null != announceVo.getAnnounceAnnexVoList() && !announceVo.getAnnounceAnnexVoList().isEmpty()) {
                announceHisVO.setAnnex(JSON.toJSONString(announceVo.getAnnounceAnnexVoList()));
            }
            if (null != announceVo.getAnnounceContentVoList() && !announceVo.getAnnounceContentVoList().isEmpty()) {
                announceHisVO.setContent(JSON.toJSONString(announceVo.getAnnounceContentVoList()));
            }
            if (null != announceVo.getAnnounceReceiverList() && !announceVo.getAnnounceReceiverList().isEmpty()) {
                announceHisVO.setReceiver(JSON.toJSONString(announceVo.getAnnounceReceiverList()));
            }
        } catch (Exception e) {
            logger.error("封装公告历史记录异常！", e);
        }
        return announceHisVO;
    }

    /**
     * 推送
     *
     * @param operation
     * @param toUserId
     * @param currentUser
     * @param announceTitle
     * @param pushMsg
     * @param announceId
     * @param createTime
     */
    private void push(final String operation, final String toUserId, final String toDeptId, final String corpId, String announceTitle, final String pushMsg, final long announceId) throws Exception {
        announceTitle = Base64Util.encodeBytes(announceTitle.getBytes("utf-8"));
        final String announceTitle1 = announceTitle;
        new Thread() {
            public void run() {
                // 获取推送用户id
                List<String> toUsers = queryUserIds(toUserId, toDeptId, corpId);
                logger.debug("查询通讯录，toUsers{}", JSON.toJSONString(toUsers));
                // 根据ID查询已激活用户信息
                List<ClientUserVO> cvList = queryClientUserByListMenId(toUsers);
                logger.debug("推送注册用户，cvList{}", JSON.toJSONString(cvList));
                int type = -1;
                if ("create".equals(operation)) {
                    type = 1;
                } else if ("edit".equals(operation)) {
                    type = 2;
                }
                if (null != cvList && cvList.size() > 0) {
                    // 入MQ
                    Date now = new Date();
                    AnnounceAction announceAction = new AnnounceAction();
                    announceAction.setSendDate(now.getTime() + "");
                    announceAction.setTitle(announceTitle1);
                    announceAction.setType(type);
                    long msgId = imRedisInterface.incr(actionIdNameSpace);
                    announceAction.setMsg_id(msgId);
                    String message = JSON.toJSONString(announceAction);
                    // 入redis
                    RedisAction ra = new RedisAction();
                    ra.setMessage(message);
                    ra.setSource("controller");
                    ra.setHead(7);
                    ra.setCreateTime(sdf.format(now));
                    imRedisInterface.setString(actionEntityNameSpace + msgId, JSON.toJSONString(ra));
                    // 推送
                    for (ClientUserVO cv : cvList) {
                        announceAction.setTo_role_id(cv.getUserId());
                        message = JSON.toJSONString(announceAction);
                        boolean falg = RocketMqUtil.send("AnnouncementQueue", message);
                        logger.debug("公告推送状态：falg{},详情:message{},ANNOUNCEMENT_CONNECTTYPE{}", falg, message, "AnnouncementQueue");
                    }
                    if ("1".equals(pushMsg)) {// 下发短信
                        String msgContent = "";
                        CorpVO corpVo = corpInterface.findById(corpId);
                        logger.debug("根据企业ID查询企业，corpVo{}", null == corpVo ? "null" : JSON.toJSONString(corpVo));
                        if (null != corpVo) {
                            if ("create".equals(operation)) {
                                msgContent = "您有一条公告，请及时登录V网通查看，发送VWT到10086获取应用下载地址【" + corpVo.getCorpName() + "】";
                            } else {
                                msgContent = "您有一条公告内容有更新，请及时登录V网通查看，发送VWT到10086获取应用下载地址【" + corpVo.getCorpName() + "】";
                            }

                            Set<String> tels = new HashSet<String>();
                            for (ClientUserVO clientUserVo : cvList) {
                                if (!org.springframework.util.StringUtils.isEmpty(clientUserVo.getTelNum())) {
                                    tels.add(clientUserVo.getTelNum());
                                }
                            }
                            List<String> telNums = new ArrayList<String>(tels);
                            List<String> list = sendProvinceSmsInterface.sendCommonSmsByList(telNums, msgContent);
                            logger.debug("公告短信推送内容{}，应推送{}条，推送状态{}", msgContent, telNums.size(), (null == list || list.isEmpty()) ? "false" : JSON.toJSONString(list));
                        }
                    }
                }
            };
        }.start();
    }

    /**
     * 查询用户ID集合
     * 
     * @param userIds
     * @param deptId
     * @return
     */
    private List<String> queryUserIds(String userId, String deptId, String corpId1) {
        JSONArray userArray = JSONArray.parseArray(userId);
        JSONArray deptArray = JSONArray.parseArray(deptId);
        List<String> listUsers = new ArrayList<String>();
        // 根据人员串获取人员
        if (!userArray.isEmpty()) {
            for (int i = 0; i < userArray.size(); i++) {
                listUsers.add(userArray.getString(i));
            }
        }
        // 根据部门串获取部门下所有人员
        if (!deptArray.isEmpty()) {
            List<DepartMentVO> lists = departMentInterface.findByCorpId(corpId1);
            if (null == lists || lists.size() < 1) {
                return listUsers;
            }
            // String[] receivers = deptId.split(";");
            String str = "";
            List<String> strList = new ArrayList<String>();
            for (int k = 0; k < deptArray.size(); k++) {
                String string = deptArray.getString(k);
                // 查询该部门下所有子部门
                List<String> integers = getAllChildNodes(lists, string, new ArrayList<String>());
                // 所有部门ID=该部门+子部门
                integers.add(string);
                // 拼接部门ID
                for (int i = 0; i < integers.size(); i++) {
                    if (str.length() + integers.get(i).length() > 1000) {
                        strList.add(str);
                        str = "";
                    }
                    str += integers.get(i) + ",";
                }

            }
            if (!"".equals(str)) {
                List<String> userstrs = getListUser(deptId, str, corpId1);
                listUsers.addAll(userstrs);
            }
            if (null != strList && !strList.isEmpty()) {
                for (String s : strList) {
                    List<String> users = getListUser(deptId, s, corpId1);
                    listUsers.addAll(users);
                }
            }
        }
        return listUsers;
    }

    /**
     * 获取部门的子部门ID
     * 
     * @param lists
     * @param orgNum
     * @param strs
     * @return
     */
    private List<String> getAllChildNodes(List<DepartMentVO> lists, String orgNum, List<String> strs) {
        List<DepartMentVO> subList = new ArrayList<DepartMentVO>();
        for (int i = 0; i < lists.size(); i++) {
            if (orgNum.equals(lists.get(i).getParentDeptNum())) {
                subList.add(lists.get(i));
            }
        }
        if (null == subList || subList.size() < 1) {
            return strs;
        }
        for (int i = 0; i < subList.size(); i++) {
            strs.add(subList.get(i).getDeptId());
            getAllChildNodes(lists, subList.get(i).getDeptId(), strs);
        }
        return strs;
    }

    /**
     * 根据用户ID集合查询注册用户
     * 
     * @param memIds
     * @return
     */
    private List<ClientUserVO> queryClientUserByListMenId(List<String> memIds) {
        List<ClientUserVO> listClientUserVO = new ArrayList<ClientUserVO>();
        for (int i = 0; i < memIds.size(); i++) {
            ClientUserVO clientUserVO = clientUserInterface.findById(memIds.get(i));
            if (null == clientUserVO) {
                continue;
            }
            listClientUserVO.add(clientUserVO);
        }
        return listClientUserVO;
    }

    /**
     * 根据企业ID以及部门ID串查询人员数据
     * 
     * @param str
     * @param corpId1
     * @return
     */
    private List<String> getListUser(String deptId, String str, String corpId1) {
        List<String> listUsers = new ArrayList<String>();
        str = str.substring(0, str.length() - 1);
        // 根据部门ID查询部门人员
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("EQ_corpId", corpId1);
        map.put("IN_deptId", str);
        List<MemberInfoVO> memVos = null;
        try {
            memVos = memberInfoInterface.findMemberInfoByCondition(map, null);
            if (null == memVos || memVos.isEmpty()) {
                return listUsers;
            }
            for (MemberInfoVO memberInfoVO : memVos) {
                listUsers.add(memberInfoVO.getMemId());
            }
        } catch (Exception e) {
            logger.error("根据部门ID、企业ID查询用户信息失败，deptId{},corpId{}", deptId, corpId1);
        }
        return listUsers;
    }

    /**
     * 获取图片验证码
     * 
     * @param requestBody
     * @return
     */
    public String getVirifycode(String requestBody) {
        logger.debug("公告-获取图片验证码,requestbody:{}", requestBody);
        try {
            JSONObject resJson = JSONObject.parseObject(requestBody);
            String sessionId = resJson.getString("sessionid");
            if (!StringUtils.checkParam(sessionId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            String userId = getUserIdFromSession(sessionId);
            if (!StringUtils.checkParam(userId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");

            int w = 200, h = 80;
            String verifyCode = VerifycodeImageUtil.generateVerifyCode(4);
            logger.debug("公告-验证码:{}", verifyCode);
            redisInterface.setString(Constants.ANNOUNCECODE_NAMESPACE + userId, verifyCode.toLowerCase(), 600);
            byte[] bytes = VerifycodeImageUtil.createVerifyCode(w, h, verifyCode);
            return ResponsePackUtil.buildPack("0000", Base64Util.encodeBytes(bytes));
        } catch (Exception e) {
            logger.error("获取图片验证码异常,requestbody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
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

    public String getSpecifiedDayAfter(String specifiedDay) throws Exception {
        Calendar c = Calendar.getInstance();
        Date date = new SimpleDateFormat("yy-MM-dd").parse(specifiedDay);
        c.setTime(date);
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day + 1);

        String dayAfter = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        return dayAfter;
    }
    
}
