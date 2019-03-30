/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.GetCalendarUtils;
import com.royasoft.vwt.cag.util.GetParamsForUrlUtils;
import com.royasoft.vwt.cag.util.PackageReturnTypeUtils;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.business.meeting.api.interfaces.MeetingInterface;
import com.royasoft.vwt.soa.business.meeting.api.vo.ImMeetingUserVo;
import com.royasoft.vwt.soa.business.meeting.api.vo.ImMeetingVo;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 
 * @author ZHOUKQ 会议模块
 */
@Scope("prototype")
public class MeetingService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkTeamService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;
    /** 保存日志接口 */
    private OperationLogService operationLogService;
    /** 会议服务接口 */
    private MeetingInterface meetingInterface;
    /** 查询通讯录接口 */
    private MemberInfoInterface memberInfoInterface;

    /** 数据服务接口 */
    private DatabaseInterface databaseInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.meeting_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();
                channel = queue_packet.getChannel();
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    // 获取请求参数返回Map集合
                    Map<String, String> paramMap = GetParamsForUrlUtils.getParamsMap(request);
                    String function_id = paramMap.get("function_id"); // 获取功能ID

                    String user_id = paramMap.get("user_id"); // 获取用户ID

                    String request_body = paramMap.get("request_body");// 获取参数实体

                    // operationLogService.saveOpLog(channel, request, user_id, function_id, request_body); // 保存操作日志

                    /**************************** 业务逻辑处理 *****************************************/

                    /** 响应结果 */
                    String res = "";
                    if (function_id == null || function_id.length() <= 0 || user_id == null || user_id.length() <= 0 || request_body == null || request_body.length() <= 0) {
                        ResponsePackUtil.CalibrationParametersFailure(channel, "工作圈业务请求参数校验失败！");
                    } else {

                        switch (function_id) {

                            default:
                                ResponsePackUtil.CalibrationParametersFailure(channel, "");
                        }
                    }
                    /** 响应成功 */
                    ResponsePackUtil.responseStatusOK(channel, res);
                }
            } catch (Exception e) {
                logger.error("工作圈业务逻辑处理异常", e);
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }
        }

    }

    /**
     * 获取会议明细
     * 
     * @return
     */
    public String getMeetingDetail(String user_id, String requestBody) {
        logger.debug("获取会议明细,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Map<String, Object> mapResult = new HashMap<String, Object>();// 返回结果Map
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        String meetingId = requestJson.getString("meetingId");// 会议ID
        if (!StringUtils.stringIsNotNull(meetingId)) {
            return ResponsePackUtil.getResponseStatus("500", "会议ID为空");
        }
        long PkMeeting = -1;
        try {
            // 将会议ID转换成Long类型
            PkMeeting = Long.parseLong(meetingId);
        } catch (NumberFormatException e1) {
            logger.error("会议类型转换异常", e1.getMessage());
            return ResponsePackUtil.getResponseStatus("300", "会议类型转换异常");
        }
        try {
            // 获取会议明细
            ImMeetingVo imMeetingVo = meetingInterface.findMeetingInfoById(PkMeeting);
            logger.debug("获取会议明细查询结果，imMeetingVo{}", imMeetingVo);
            jsonObject.put("resultData", PackageReturnTypeUtils.packageMeetingInfo(imMeetingVo));
            jsonObject.put("result", 200);
            jsonObject.put("resultMsg", "获取会议明细查询成功");
            mapResult.put("jsonObject", jsonObject);
            logger.debug("获取会议明细返回状态结果，mapResult{}", mapResult.toString());
            return mapResult.toString();
        } catch (Exception e) {
            logger.debug("获取会议明细查询异常", e.getMessage());
            return ResponsePackUtil.getResponseStatus("400", "获取会议明细查询异常");
        }
    }

    /**
     * 获取会议列表
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String getMeetingList(String user_id, String requestBody) {
        logger.debug("获取会议列表,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        int result = 200;
        String resultMsg = "会议列表获取成功";
        /** 分页起始页 */
        String pageIndex = requestJson.getString("pageIndex");
        /** 分页大小 */
        String pageSize = requestJson.getString("pageSize");
        /** 用户ID */
        String userId = requestJson.getString("userId");
        /** 参加状态 */
        String ifAttend = requestJson.getString("ifAttend");
        /** 会议状态标识:0-开始，1-等待，2-结束 */
        String status = requestJson.getString("status");

        if (!StringUtils.stringIsNotNull(pageIndex) || !StringUtils.stringIsNotNull(pageSize) || !StringUtils.stringIsNotNull(userId) || !StringUtils.stringIsNotNull(ifAttend)
                || !StringUtils.stringIsNotNull(status)) {
            return ResponsePackUtil.getResponseStatus("300", "参数校验失败！");
        }
        int page = -1;
        int rows = -1;

        try {
            page = Integer.valueOf(pageIndex);
            rows = Integer.valueOf(pageSize);
            Map<String, Object> conditions = new HashMap<String, Object>();

            conditions.put("EQ_pkMeeting", "");
            Map<String, Object> listMeetingInfoMap = meetingInterface.findAllMeetingInfoByPage(page, rows, conditions, null);
            /** 分页查询分页列表 */

            List<Object> lstMeeting = new ArrayList<Object>();
            int pageCount = (Integer) jsonObject.get("pageCount");
            if (Integer.parseInt(pageIndex) <= pageCount) {// 当前页码小于等于总页数
                // lstMeeting = (List) jsonObject.get("list");
            }
            jsonObject.clear();
            jsonObject.put("pageCount", pageCount);
            jsonObject.put("resultData", lstMeeting);
        } catch (Exception e) {
            result = 400;
            resultMsg = "获取会议列表时出错";
            e.printStackTrace();
        } finally {
            jsonObject.put("result", result);
            jsonObject.put("resultMsg", resultMsg);
            jsonObject.put("serverTime", GetCalendarUtils.getDateTime());
            mapResult.put("jsonObject", jsonObject);
        }
        logger.debug("获取会议列表返回结果集,mapResult:{}", mapResult.toString());
        return mapResult.toString();
    }

    /**
     * 创建会议
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String appendMeeting(String user_id, String requestBody) {
        logger.debug("创建会议,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Map<String, Object> jsonObject = new HashMap<String, Object>();

        String fromUserId = requestJson.getString("userId"); // 发起人id

        String fromUserName = requestJson.getString("username"); // 发起人姓名

        String subject = requestJson.getString("meetingTheme"); // 会议主题

        String content = requestJson.getString("meetingContent");// 会议内容

        String startTime = requestJson.getString("meetingTime");// 会议开始时间

        String status = requestJson.getString("meetingType");// 会议状态 进行中 已结束

        String userId = requestJson.getString("aboutId");// 相关人员ID

        String realName = requestJson.getString("aboutUsername"); // 相关人员姓名

        String endTime = requestJson.getString("endTime");// 结束时间

        // 装换日期格式 yyyy-MM-dd HH:mm:ss
        startTime = GetCalendarUtils.getChangeDateFormat(startTime);
        if (!StringUtils.stringIsNotNull(startTime)) {
            return ResponsePackUtil.getResponseStatus("304", "没有开始时间");
        }
        if (!StringUtils.stringIsNotNull(userId)) {
            return ResponsePackUtil.getResponseStatus("301", "没有参与者");
        }
        // 加上发送人
        userId += "," + fromUserId;
        if (!StringUtils.stringIsNotNull(realName)) {
            return ResponsePackUtil.getResponseStatus("302", "没有参与者姓名");
        }
        // 加上发送人姓名
        realName += "," + fromUserName;
        String[] arrUserId = userId.split(",");
        String[] arrRealName = realName.split(",");
        // 获取会议人数限制
        int limit = 100;
        String numberLimit = ParamConfig.number_limit;
        try {
            limit = Integer.parseInt(numberLimit);
        } catch (Exception e) {
            logger.error("获取任务人数限制数量出错", e.getMessage());
            return ResponsePackUtil.getResponseStatus("300", "获取任务人数限制数量出错");
        }
        // 参会人员不能超过规定的限制数量(包括自己)
        if (arrUserId.length > limit) {
            return ResponsePackUtil.getResponseStatus("305", "参会人数超过限制");
        }
        // 参与人和姓名数量不一致
        if (arrUserId.length != arrRealName.length) {
            return ResponsePackUtil.getResponseStatus("303", "参与者与姓名数量不一致");
        }
        try {
            long sTime = GetCalendarUtils.getNowDateTime(startTime).getTime();
            long cTime = GetCalendarUtils.getNowDateTime(GetCalendarUtils.getDateTime()).getTime();
            // 开始时间等于当前时间时默认会议状态开始
            long longStatus = -1;
            long longTalkStatus = -1;
            if (sTime == cTime) {
                longStatus = 0;// 开始
                longTalkStatus = 0;// 允许讨论
            } else if (sTime < cTime) {// 开始时间小于当前时间
                return ResponsePackUtil.getResponseStatus("306", "开始时间不能小于当前时间，请重新选择");
            } else {
                longStatus = 1;// 等待
            }
            // 其他数据也要封装以及获取附件信息
            String attach = "";// 附件信息
            long pkMeeting = databaseInterface.generateId("im_meeting", "pkMeeting");
            Date dateEndTime = null;
            if (StringUtils.stringIsNotNull(endTime)) {
                dateEndTime = GetCalendarUtils.getNowDateTime(GetCalendarUtils.getChangeDateFormat(endTime));
            }
            // 插入数据
            ImMeetingVo resImMeetingVo = meetingInterface.save(packageImMeetingVo(pkMeeting, subject, content, attach, null, fromUserId, fromUserName, GetCalendarUtils.getNowDateTime(startTime),
                    dateEndTime, longStatus, longTalkStatus, userId, realName, GetCalendarUtils.getDateTime(), 0L, null, null));
            logger.debug("会议数据插入返回结果集,resImMeetingVo:{}", JSON.toJSONString(resImMeetingVo));
            if (null == resImMeetingVo) {
                return ResponsePackUtil.getResponseStatus("300", "会议数据插入失败");
            }
            for (int i = 0; i < arrUserId.length; i++) {
                ImMeetingUserVo imMeetingUserVo = new ImMeetingUserVo();
                imMeetingUserVo.setPkMeeting(pkMeeting);
                imMeetingUserVo.setUserid(Long.parseLong(arrUserId[i]));
                imMeetingUserVo.setRealname(arrRealName[i]);
                // 插入人员关联表
                meetingInterface.save(imMeetingUserVo);
            }
            jsonObject.put("meetingId", pkMeeting);
            jsonObject.put("result", 200);
            jsonObject.put("resultMsg", "创建会议成功");
            mapResult.put("jsonObject", jsonObject);
            logger.debug("创建会议返回结果集,mapResult:{}", mapResult.toString());
            // 推送通知
            return mapResult.toString();
        } catch (Exception e) {
            logger.error("创建会议出现异常", e.getMessage());
            return ResponsePackUtil.getResponseStatus("300", "创建会议出现异常");
        }

    }

    /**
     * 结束会议
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String finishMeeting(String user_id, String requestBody) {
        logger.debug("结束会议,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        int result = 0;
        String resultMsg = "会议结束成功";
        /** 发起人id */
        String fromUserId = requestJson.getString("userId");
        /** 会议id */
        String pk_meeting = requestJson.getString("meetingId");
        /** 会议状态 */
        String status = requestJson.getString("meetingType");
        // 0-开始，1-等待，2-结束

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map.put("pk_meeting", pk_meeting);
            String summary = "";// 获取文件 this.getFilePath(request, "meetingImport", fromUserId);
            if (null != summary && !"".equals(summary.trim())) {
                map.put("summary", "");
            }
            map.put("endTime", GetCalendarUtils.getDateTime());
            if (null != status && !"".equals(status)) {
                if ("2".equals(status)) {// 结束会议
                    map.put("talkStatus", "1");// 讨论状态改成不能讨论
                }
                map.put("status", status);
            }
            updateMeeting(map);
        } catch (Exception e) {

        }
        jsonObject.put("result", result);
        jsonObject.put("resultMsg", resultMsg);
        mapResult.put("jsonObject", jsonObject);
        return mapResult.toString();
    }

    /**
     * 上传会议记要
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String uploadMeetingSummary(String user_id, String requestBody) {
        logger.debug("上传会议记要,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        int result = 0;
        String resultMsg = "上传会议纪要成功";
        /** 发起人id */
        String fromUserId = requestJson.getString("userId");
        /** 会议id */
        String pk_meeting = requestJson.getString("meetingId");

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            /** 根据会议ID查询会议记要 */
            Map<String, Object> meetingMap = null;// this.meetingService.getMeetingByPk(pk_meeting);
            if (null == meetingMap || null == meetingMap.get("status")) {
                jsonObject.put("result", 300);
                jsonObject.put("resultMsg", "会议记要为空");
                mapResult.put("jsonObject", jsonObject);
                return mapResult.toString();
            }

            String status = (String) meetingMap.get("status");
            if (!"1".equals(status)) {
                map.put("pk_meeting", pk_meeting);
                /** 获取上传信息 原接口是返回路径字符串多个分号分隔 */
                map.put("summary", "");

                // 更新数据
                updateMeeting(map);
                jsonObject.put("result", result);
                jsonObject.put("resultMsg", resultMsg);
                mapResult.put("jsonObject", jsonObject);
                return mapResult.toString();
            } else {// 会议未开始
                jsonObject.put("result", 501);
                jsonObject.put("resultMsg", "会议未开始");
                mapResult.put("jsonObject", jsonObject);
                return mapResult.toString();
            }

        } catch (Exception e) {
            jsonObject.put("result", 300);
            jsonObject.put("resultMsg", "上传会议纪要失败");
            mapResult.put("jsonObject", jsonObject);
            return mapResult.toString();
        }
    }

    /**
     * 上传会议附件
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String uploadAttach(String user_id, String requestBody) {
        logger.debug("上传会议附件,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        int result = 0;
        String resultMsg = "附件上传成功";
        /** 发起人id */
        String fromUserId = requestJson.getString("userId");
        /** 会议id */
        String pk_meeting = requestJson.getString("meetingId");

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map.put("pk_meeting", pk_meeting);
            // 获取附件路径
            map.put("attach", "");
            map.put("act", "upload");

            // 更新数据
            updateMeeting(map);
        } catch (Exception e) {
            jsonObject.put("result", 400);
            jsonObject.put("resultMsg", "发送失败");
            mapResult.put("jsonObject", jsonObject);
            return mapResult.toString();
        }

        jsonObject.put("result", result);
        jsonObject.put("resultMsg", resultMsg);
        mapResult.put("jsonObject", jsonObject);
        return mapResult.toString();
    }

    /**
     * 删除会议附件
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String removeAttach(String user_id, String requestBody) {
        logger.debug("删除会议附件,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        int result = 0;
        String resultMsg = "";
        /** 附件路径 */
        String attachPath = requestJson.getString("attachPath");
        /** 会议id */
        String pk_meeting = requestJson.getString("meetingId");
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map.put("pk_meeting", pk_meeting);
            /** 根据会议ID查询会议内容 */
            Map<String, Object> map2 = null; // meetingService.getMeetingByPk(pk_meeting);
            // 获取老的附件地址路劲
            String attachPath_old = (String) map2.get("attach");
            String attachPath_new = "";
            String[] attachPaths = attachPath_old.split(";");
            String[] delAttachPaths = attachPath.split(";");
            boolean isFind = false;
            // for (String ap : attachPaths) {
            // isFind = false;
            // for (String dap : delAttachPaths) {
            // if (ap.indexOf(dap) > -1) {
            // String filePath = ApplicationContextUtils.getApplicationContextPath() + ap;
            // File file = new File(filePath);
            // if (file.exists()) {
            // file.delete();
            // }
            // isFind = true;
            // break;
            // }
            // }
            // if (!isFind) {
            // attachPath_new += ap + ";";
            // }
            // }
            if (attachPath_new.endsWith(";")) {
                attachPath_new = attachPath_new.substring(0, attachPath_new.length() - 1);
            }
            map.put("attach", attachPath_new);
            updateMeeting(map2);
            jsonObject.put("result", result);
            jsonObject.put("resultMsg", resultMsg);
            mapResult.put("jsonObject", jsonObject);
            return mapResult.toString();
        } catch (Exception e) {
            jsonObject.put("result", 400);
            jsonObject.put("resultMsg", "删除附件");
            mapResult.put("jsonObject", jsonObject);
            return mapResult.toString();
        }

    }

    /**
     * 不参加会议成功
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String doNotAttendMeeting(String user_id, String requestBody) {
        logger.debug("不参加会议成功,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        int result = 0;
        String resultMsg = "不参加会议成功";
        /** 发起人id */
        String userId = requestJson.getString("userId");
        /** 会议id */
        String pk_meeting = requestJson.getString("meetingId");

        Map<String, Object> map = new HashMap<String, Object>();
        try {

            if (null == pk_meeting || "".equals(pk_meeting.trim())) {
                jsonObject.put("result", 500);
                jsonObject.put("resultMsg", "会议ID为空");
                mapResult.put("jsonObject", jsonObject);
                return mapResult.toString();
            }
            if (null == userId || "".equals(userId.trim())) {
                jsonObject.put("result", 501);
                jsonObject.put("resultMsg", "用户名为空");
                mapResult.put("jsonObject", jsonObject);
                return mapResult.toString();
            }
            // 根据发起人id查询会议信息
            Map<String, Object> meetingMap = null;
            if (null == meetingMap) {
                jsonObject.put("result", 502);
                jsonObject.put("resultMsg", "找不到会议相关信息");
                mapResult.put("jsonObject", jsonObject);
                return mapResult.toString();
            }
            String status = (String) meetingMap.get("status");
            if (null == status || "".equals(status) || "2".equals(status)) {
                jsonObject.put("result", 503);
                jsonObject.put("resultMsg", "会议已结束");
                mapResult.put("jsonObject", jsonObject);
                return mapResult.toString();
            }

            if (null != meetingMap.get("startTime") && !"".equals(map.get("startTime"))) {
                String startTime = meetingMap.get("startTime").toString();
                Date now = new Date();
                long nowMilliSeconds = now.getTime();
                long startMilliSeconds = 0;
                try {
                    Date meetingStartDate = GetCalendarUtils.getDateTime(startTime);
                    startMilliSeconds = meetingStartDate.getTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 时间未到会议开始前10分钟
                if ((startMilliSeconds - nowMilliSeconds) > (1000 * 60 * 10)) {
                    jsonObject.put("result", 504);
                    jsonObject.put("resultMsg", "开会前10分钟才能选择");
                    mapResult.put("jsonObject", jsonObject);
                    return mapResult.toString();
                }
            }
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("meetingId", pk_meeting);
            conditions.put("userId", userId);
            // 根据会议Id、用户Id查询会议相关人员信息
            List<Map<String, Object>> imMeetingUserMapList = null;// this.meetingDao.getMeetingParticipant(conditions);
            // 会议参与人信息为空
            if (null == imMeetingUserMapList || imMeetingUserMapList.isEmpty()) {
                jsonObject.put("result", 505);
                jsonObject.put("resultMsg", "您不是会议参与人");
                mapResult.put("jsonObject", jsonObject);
                return mapResult.toString();
            }
            Map<String, Object> imMeetingUserMap = imMeetingUserMapList.get(0);
            // 只有“未签到”的会议才能选择“不参加”会议
            if (null != imMeetingUserMap.get("isSignin") && !"".equals(imMeetingUserMap.get("isSignin"))) {
                String isSignin = imMeetingUserMap.get("isSignin").toString();
                if ("0" != isSignin && !"0".equals(isSignin)) {
                    jsonObject.put("result", 506);
                    jsonObject.put("resultMsg", "您已签到");
                    mapResult.put("jsonObject", jsonObject);
                    return mapResult.toString();
                }
            }
            try {
                map.put("ifAttend", 0);
                // 更新不参加会议
                int res = 0;// this.meetingDao.updateMeetingUser(map);
                if (res == 0) {
                    jsonObject.put("result", 507);
                    jsonObject.put("resultMsg", "参加状态修改失败");
                    mapResult.put("jsonObject", jsonObject);
                    return mapResult.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                jsonObject.put("result", 506);
                jsonObject.put("resultMsg", "您已签到");
                mapResult.put("jsonObject", jsonObject);
                return mapResult.toString();
            }
            jsonObject.put("result", result);
            jsonObject.put("resultMsg", resultMsg);
            mapResult.put("jsonObject", jsonObject);
            return mapResult.toString();
        } catch (Exception e) {
            jsonObject.put("result", 400);
            jsonObject.put("resultMsg", "不参加会议请求失败");
            mapResult.put("jsonObject", jsonObject);
            return mapResult.toString();
        }
    }

    // /**
    // * 恢复并签到
    // *
    // * @param user_id
    // * @param requestBody
    // * @return
    // */
    // public String attendAndSignIn(String user_id, String requestBody) {
    // logger.debug("恢复并签到,requestBody:{},userKey:{}", requestBody, user_id);
    // JSONObject requestJson = JSONObject.parseObject(requestBody);
    // Map<String, Object> mapResult = new HashMap<String, Object>();
    // Map<String, Object> jsonObject = new HashMap<String, Object>();
    // int result = 0;
    // String resultMsg = "恢复并签到成功";
    // /** 发起人id */
    // String userId = requestJson.getString("userId");
    // /** 会议id */
    // String pk_meeting = requestJson.getString("meetingId");
    //
    // Map<String, Object> map = new HashMap<String, Object>();
    // if (!StringUtils.stringIsNotNull(userId)) {
    // return ResponsePackUtil.getResponseStatus("500", "用户名为空");
    // }
    // if (!StringUtils.stringIsNotNull(pk_meeting)) {
    // return ResponsePackUtil.getResponseStatus("501", "会议ID为空");
    // }
    // try {
    // // 根据会议ID查询会议
    // Map meetingMap = this.meetingDao.getMeetingByPk(map);
    // if (!StringUtils.mapSOIsNotNull(meetingMap)) {
    // return ResponsePackUtil.getResponseStatus("502", "找不到会议相关信息");
    // }
    // String status = "";
    // if ("2".equals(status)) {
    // return ResponsePackUtil.getResponseStatus("503", "会议已结束");
    // }
    // String startTime = "";
    // if (null != startTime) {
    // long nowMilliSeconds = new Date().getTime();
    // long startMilliSeconds = 0;
    // try {
    // Date meetingStartDate = GetCalendarUtils.getDateTime(startTime);
    // startMilliSeconds = meetingStartDate.getTime();
    // } catch (Exception e) {
    // logger.error("转换会议开会时间出错", e.getMessage());
    // return ResponsePackUtil.getResponseStatus("300", "转换会议开会时间出错");
    // }
    // // 时间未到会议开始前10分钟
    // if ((startMilliSeconds - nowMilliSeconds) > (1000 * 60 * 10)) {
    // return ResponsePackUtil.getResponseStatus("504", "开会前10分钟才能选择");
    // }
    // }
    // // 获取参与人信息
    // List<Map<String, Object>> imMeetingUserMapList = this.meetingDao.getMeetingParticipant(conditions);
    // // 会议参与人信息为空
    // if (null == imMeetingUserMapList || imMeetingUserMapList.isEmpty()) {
    // return ResponsePackUtil.getResponseStatus("505", "您不是会议参与人");
    // }
    // Map<String, Object> imMeetingUserMap = imMeetingUserMapList.get(0);
    // // 只有“未签到”的会议才能选择“不参加”会议
    // if (null != imMeetingUserMap.get("isSignin")) {
    // String isSignin = imMeetingUserMap.get("isSignin").toString();
    // if ("0" != isSignin && !"0".equals(isSignin)) {
    // return ResponsePackUtil.getResponseStatus("506", "您已签到");
    // }
    // }
    // // 更新恢复并签到
    // int res = this.meetingDao.updateMeetingUser(map);
    // if (res == 0) {
    // return ResponsePackUtil.getResponseStatus("507", "参加状态修改失败");
    // }
    // return ResponsePackUtil.getResponseStatus("200", "恢复并签到成功");
    // } catch (Exception e) {
    // logger.error("恢复并签到失败", e.getMessage());
    // return ResponsePackUtil.getResponseStatus("400", "恢复并签到失败");
    // }
    // }

    // 签到
    //
    // public String signInMeeting(String user_id, String requestBody) {
    // logger.debug("签到,requestBody:{},userKey:{}", requestBody, user_id);
    // JSONObject requestJson = JSONObject.parseObject(requestBody);
    // Map<String, Object> model = new HashMap<String, Object>();
    // Map<String, Object> conditions = new HashMap<String, Object>();
    // String meetingId = requestJson.getString("meetingId");
    // String userId = requestJson.getString("userId");
    // if (!StringUtils.stringIsNotNull(userId)) {
    // return ResponsePackUtil.getResponseStatus("500", "用户名为空");
    // }
    // if (!StringUtils.stringIsNotNull(meetingId)) {
    // return ResponsePackUtil.getResponseStatus("501", "会议ID为空");
    // }
    // conditions.put("meetingId", meetingId);
    // // 根据会议ID查询会议信息
    // Map<String, Object> map = meetingService.getMeetingByPk(meetingId);
    // int result = 200;
    // String resultMsg = "签到成功";
    // // 判断签到时间是否到了
    // if (!meetingService.canSign(conditions)) {
    // return ResponsePackUtil.getResponseStatus("300", "签到时间还未到");
    // }
    // conditions.put("userId", userId);
    // if (!meetingService.signInMeeting(conditions) > 0) {
    // return ResponsePackUtil.getResponseStatus("400", "签到失败");
    // }
    // new Thread() {
    // public void run() {
    // // 根据会议ID查询会议参与人员
    // List<String> list = meetingService.getMeetingParticipantUserId(con);
    // ImSendInfo info = new ImSendInfo();
    // info.setId(Integer.parseInt(con.remove("meetingId").toString()));
    // info.setSendDate(System.currentTimeMillis() + "");
    // info.setFromUser(userId);
    // info.setTitle((String) map.get("subject"));
    // info.setReserve4("meeting_sign");
    // for (String userId : list) {
    // // 推送
    // PushOpenfireMessage.PushOpenfireMsg(userId, 24, 200, info);
    // }
    // }
    // }.start();
    // return ResponsePackUtil.getResponseStatus("200", "签到成功");
    // }

    // 获取签到状态
    // public String meetingMorePersonal(String user_id, String requestBody) {
    // logger.debug("获取签到状态,requestBody:{},userKey:{}", requestBody, user_id);
    // JSONObject requestJson = JSONObject.parseObject(requestBody);
    // Map<String, Object> model = new HashMap<String, Object>();
    // Map<String, Object> conditions = new HashMap<String, Object>();
    // String meetingId = requestJson.getString("meetingId");
    // if (!StringUtils.stringIsNotNull(meetingId)) {
    // return ResponsePackUtil.getResponseStatus("501", "会议ID为空");
    // }
    // try {
    // // 查询会议参与人信息
    // List<Map<String, Object>> list = meetingService.getMeetingParticipant(conditions);
    // conditions.clear();
    // conditions.put("result", 200);
    // conditions.put("resultMsg", "获取签到成功");
    // conditions.put("resultData", list);
    // } catch (Exception e) {
    // conditions.put("result", 300);
    // conditions.put("resultMsg", "获取签到失败");
    // conditions.put("resultData", null);
    // }
    // model.put("jsonObject", conditions);
    // return model.toString();
    // }

    // 修改讨论状态

    public String setTalkStatus(String user_id, String requestBody) {
        logger.debug("修改讨论状态,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String meetingId = requestJson.getString("meetingId");
        String talkStatus = requestJson.getString("talkStatus");
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map.put("pk_meeting", meetingId);
            map.put("talkStatus", talkStatus);
            updateMeeting(map);
        } catch (Exception e) {
            logger.error("会议更新异常", e.getMessage());
            return ResponsePackUtil.getResponseStatus("400", "会议更新异常");
        }
        return ResponsePackUtil.getResponseStatus("200", "会议更新成功");
    }

    /**
     * 发起会议聊天
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    // public String sendImMeet(String user_id, String requestBody) {
    // logger.debug("发起会议聊天,requestBody:{},userKey:{}", requestBody, user_id);
    // JSONObject requestJson = JSONObject.parseObject(requestBody);
    // Map<String, Object> mapResult = new HashMap<String, Object>();
    // Map<String, Object> jsonObject = new HashMap<String, Object>();
    // int result = 200;
    // String resultMsg = "发起会议聊天成功";
    // // 通过userId获取姓名
    // String userId = requestJson.getString("userId");
    // String meet_Id = requestJson.getString("meet_Id");
    // String content = requestJson.getString("content");
    // String sendTime = requestJson.getString("sendTime");
    // String type = requestJson.getString("type");
    // // content = EmojiFilter.filterEmoji(content);
    //
    // // int send_Type = Integer.parseInt(request.getParameter("type"));
    // // 通过会议id返回详情
    // try {
    // Map<String, Object> condition = new HashMap<String, Object>();
    // condition.put("EQ_pkMeeting", meet_Id);
    // condition.put("EQ_issignin", 1);
    // // 获取已签到人数
    // List<ImMeetingUserVo> list = meetingInterface.findMeetingUserByCondition(condition, null);
    // // 通过会议ID查询会议主表信息
    // ImMeetingVo meet_map = meetingInterface.findMeetingInfoById(Long.parseLong(meet_Id));
    // ImSendInfo imSendInfo = new ImSendInfo();
    // imSendInfo.setContent(content);
    // imSendInfo.setFromUser(userId);
    // imSendInfo.setId(Integer.parseInt(meet_Id));
    // imSendInfo.setTitle((String) meet_map.getSubject());
    // imSendInfo.setFilePath((String) meet_map.getAttach());
    // imSendInfo.setReserve1((String) meet_map.getSummary());
    // imSendInfo.setSendDate(GetCalendarUtils.getDateTime(sendTime).getTime());
    //
    // imSendInfo.setReserve2(System.currentTimeMillis() + "");
    // String status = String.valueOf(meet_map.getStatus());
    // String talkStatus = String.valueOf(meet_map.getTalkStatus());
    //
    // String userIds = "";
    // for (ImMeetingUserVo map : list) {
    // if (!userId.equals(map.getUserid())) {
    // userIds = map.getUserid() + ";" + userIds;
    // }
    // }
    // imSendInfo.setUsers(userIds);
    // imSendInfo.setReserve3(type);
    //
    // if (null != status && status.equals("0")) {
    // if (null != talkStatus && !talkStatus.equals("1")) {
    // PushOpenfireMessage.PushOpenfireMsg(userIds, 26, 200, imSendInfo);
    // jsonObject.clear();
    // resultMsg = "会话成功!";
    // jsonObject.put("createTime", System.currentTimeMillis() + "");
    // } else {
    // jsonObject.clear();
    // result = 500;
    // resultMsg = "暂未开始讨论";
    // jsonObject.put("createTime", System.currentTimeMillis() + "");
    // }
    // } else {
    // jsonObject.clear();
    // result = 500;
    // resultMsg = "会议已经结束";
    // jsonObject.put("createTime", System.currentTimeMillis() + "");
    // }
    //
    // } catch (Exception e) {
    // jsonObject.clear();
    // result = 400;
    // resultMsg = "会话失败!";
    // e.printStackTrace();
    // } finally {
    // jsonObject.put("result", result);
    // jsonObject.put("resultMsg", resultMsg);
    // mapResult.put("jsonObject", jsonObject);
    // }
    // return mapResult.toString();
    // }

    /**
     * 删除会议
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String deleteMeeting(String user_id, String requestBody) {
        logger.debug("删除会议,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String fromUserId = requestJson.getString("userId");// 发起人id

        String pk_meeting = requestJson.getString("meetingId");// 会议id

        if (!StringUtils.stringIsNotNull(fromUserId)) {

            return ResponsePackUtil.getResponseStatus("501", "用户手机号为空!");
        }

        if (!StringUtils.stringIsNotNull(pk_meeting)) {

            return ResponsePackUtil.getResponseStatus("500", "会议id为空!");
        }
        long pkMeeting = -1;
        try {
            pkMeeting = Long.parseLong(pk_meeting);
        } catch (NumberFormatException e1) {
            logger.debug("", e1.getMessage());
            return ResponsePackUtil.getResponseStatus("300", "会议id类型转换异常!");
        }
        try {
            // 获取所有参入人ID，逗号分隔
            String userIds = getAllMeetingUsers(pkMeeting);
            // 根据会议ID获取所有参与人信息
            List<ImMeetingUserVo> listImMeetingUserVo = meetingInterface.findMeetingUserById(pkMeeting);
            // //根据会议ID获取会议信息
            // ImMeetingVo imMeetingVo = meetingInterface.findMeetingInfoById(pkMeeting);
            // 根据会议ID及会议是否删除查询会议内容
            Map<String, Object> mapDelete = new HashMap<String, Object>();
            mapDelete.put("EQ_pkMeeting", pkMeeting);
            mapDelete.put("EQ_isdelete", 0);
            List<ImMeetingVo> imMeetingVoList = meetingInterface.findMeetingInfoByCondition(mapDelete, null);
            logger.debug("根据会议ID及会议是否删除查询会议内容,imMeetingVoList{}", imMeetingVoList.toString());
            if (null == imMeetingVoList || imMeetingVoList.isEmpty() || null == imMeetingVoList.get(0)) {
                return ResponsePackUtil.getResponseStatus("502", "会议内容为空!");
            }
            ImMeetingVo immtVo = imMeetingVoList.get(0); // 获取会议对象
            if (!fromUserId.equals(immtVo.getFromuserid())) {
                return ResponsePackUtil.getResponseStatus("504", "用户不是会议发起人!");
            }
            if ("0".equals(immtVo.getStatus() + "")) {
                return ResponsePackUtil.getResponseStatus("507", "会议正在进行中,不允许删除!");
            }
            immtVo.setIsdelete(1L);
            ImMeetingVo resImmtVo = meetingInterface.save(immtVo);
            if (null == resImmtVo) {
                return ResponsePackUtil.getResponseStatus("505", "会议信息删除失败!");
            }
            // 推送
            // SimpleDateFormat dateType = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // ImSendInfo info = new ImSendInfo();
            // info.setId(Integer.valueOf(pk_meeting));
            // info.setSendDate(System.currentTimeMillis() + "");
            // info.setFromUser(fromUserId);
            // info.setUsers(userIds);
            // info.setTitle(map2.get("subject").toString());
            // info.setContent(map2.get("content").toString());
            // info.setFilePath(map2.get("attach").toString());
            // info.setReserve1(dateType.format(new Date()));
            // if (!list.isEmpty()) {
            // info.setReserve4("meeting_delete");
            // OpPushMessage(info, list);
            // }
            return ResponsePackUtil.getResponseStatus("200", "删除会议成功!");
        } catch (Exception e) {
            logger.error("删除会议异常!", e.getMessage());
            return ResponsePackUtil.getResponseStatus("300", "删除会议异常!");
        }

    }

    /**
     * 编辑会议
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String editMeeting(String user_id, String requestBody) {
        logger.debug("删除会议,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        String pk_meeting = requestJson.getString("pk_meeting");// 会议id
        String fromUserId = requestJson.getString("userId");// 编辑人id
        String subject = requestJson.getString("meetingTheme");// 会议主题
        String content = requestJson.getString("meetingContent");// 会议内容
        String startTime = requestJson.getString("meetingTime");// 会议开始时间
        String userId = requestJson.getString("aboutId");// 相关人员ID
        String realName = requestJson.getString("aboutUsername");// 相关人员姓名
        String remainFile = requestJson.getString("remainFile");
        String deleteFile = requestJson.getString("deleteFile");
        if (!StringUtils.stringIsNotNull(pk_meeting)) {
            return ResponsePackUtil.getResponseStatus("300", "会议Id为空");
        }
        if (!StringUtils.stringIsNotNull(fromUserId)) {
            return ResponsePackUtil.getResponseStatus("300", "编辑者Id为空");
        }
        long pkMeeting = -1;
        try {
            pkMeeting = Long.parseLong(pk_meeting);
        } catch (NumberFormatException e1) {
            logger.debug("", e1.getMessage());
            return ResponsePackUtil.getResponseStatus("300", "会议id类型转换异常!");
        }
        ImMeetingVo imMeetingVo = meetingInterface.findMeetingInfoById(pkMeeting);
        if (null == imMeetingVo) {
            return ResponsePackUtil.getResponseStatus("300", "不存在该会议");
        }
        Date nowDate = new Date();
        if (!(imMeetingVo.getStarttime().getTime() - nowDate.getTime() > 1200000)) {
            return ResponsePackUtil.getResponseStatus("300", "会议即将开始，不允许修改!");
        }
        // 获取会议创建人
        String trueFrom = (String) imMeetingVo.getFromuserid();
        if (!trueFrom.equals(fromUserId)) {
            return ResponsePackUtil.getResponseStatus("304", "该用户不是会议创建人!");
        }
        if (!(nowDate.getTime() < imMeetingVo.getStarttime().getTime())) {
            return ResponsePackUtil.getResponseStatus("305", "开始时间不能小于当前时间，请重新选择!");
        }
        try {
            // 封装会议信息
            imMeetingVo.setSubject(subject);
            imMeetingVo.setContent(content);
            imMeetingVo.setFromuserid(fromUserId);
            imMeetingVo.setStarttime(GetCalendarUtils.getDateTime(startTime));
            imMeetingVo.setTouserid(StringUtils.moveSplit(userId, ",") + "," + fromUserId);
            imMeetingVo.setTousername(StringUtils.moveSplit(realName, ",") + "," + imMeetingVo.getFromusername());
            String attach = "";
            if (StringUtils.stringIsNotNull(remainFile)) {
                attach = StringUtils.moveSplit(attach, ";");
                attach = StringUtils.moveSplit(attach, ",");
                attach = StringUtils.moveSplit(remainFile, ";") + ";" + attach;
            }
            imMeetingVo.setAttach(attach);

            if (StringUtils.stringIsNotNull(deleteFile)) {
                deleteFile = StringUtils.moveSplit(deleteFile, ";");
                String[] deteleFiles = deleteFile.split(";");
                for (String string : deteleFiles) {
                    String ap = string.split(",")[0];
                    String filePathDelete = "系统路径" + ap;
                    File file = new File(filePathDelete);
                    if (file.exists()) {
                        file.delete();
                    }
                }

            }
            // 保存会议信息
            ImMeetingVo resImmtVo = meetingInterface.save(imMeetingVo);
            if (null == resImmtVo) {
                return ResponsePackUtil.getResponseStatus("300", "会议编辑失败!");
            }
            // 获取编辑前后的人员差异
            Map<String, List<String>> userMap = getAllUsers(getAllMeetingUsers(pkMeeting), fromUserId + "," + userId);

            // ImSendInfo info = new ImSendInfo();
            // info.setId(Integer.valueOf(pk_meeting));
            // info.setSendDate(System.currentTimeMillis() + "");
            // info.setFromUser(fromUserId);
            // info.setUsers(userId + "," + fromUserId);
            // info.setTitle(subject);
            // info.setContent(content);
            // info.setFilePath(attach);
            // info.setReserve1(dateType.format(new Date()));

            List<String> listD = userMap.get("listD");
            List<String> listS = userMap.get("listS");
            List<String> listI = userMap.get("listI");

            if (!listD.isEmpty()) {
                // System.out.println("listD====" + listD.size());
                // info.setReserve4("edit_delete");
                // OpPushMessage(info, listD);
                for (String string : listD) {
                    meetingInterface.deleteMeetingUserByIdAndUserId(pkMeeting, Long.parseLong(string));
                }

            }
            if (!listS.isEmpty()) {
                // System.out.println("listS====" + listS.size());
                // //info.setReserve4("edit_same");
                // OpPushMessage(info, listS);
            }
            if (!listI.isEmpty()) {
                // System.out.println("listI====" + listI.size());
                // info.setReserve4("edit_insert");
                // OpPushMessage(info, listI);
                for (String string : listI) {
                    ImMeetingUserVo imMeetingUserVo = new ImMeetingUserVo();
                    imMeetingUserVo.setPkMeeting(pkMeeting);
                    imMeetingUserVo.setUserid(Long.parseLong(string));
                    String menberName = getMenberNameByPhone(string);
                    imMeetingUserVo.setRealname(menberName);
                    meetingInterface.save(imMeetingUserVo);
                }
            }

        } catch (Exception e) {
            logger.error("会议编辑异常!", e.getMessage());
            return ResponsePackUtil.getResponseStatus("300", "会议编辑异常!");
        }
        jsonObject.put("meetingId", pkMeeting);
        jsonObject.put("result", 200);
        jsonObject.put("resultMsg", "会议编辑成功！");
        mapResult.put("jsonObject", jsonObject);
        return mapResult.toString();
    }

    /**
     * 获取编辑前后的人员差异
     * 
     * @param before
     * @param after
     * @return
     */
    public Map<String, List<String>> getAllUsers(String before, String after) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> listD = new ArrayList<String>();
        List<String> listS = new ArrayList<String>();
        List<String> listI = new ArrayList<String>();
        before = StringUtils.moveSplit(before, ",");
        after = StringUtils.moveSplit(after, ",");
        String[] befores = before.split(",");
        String[] afters = after.split(",");
        for (String string : afters) {
            listI.add(string);
        }
        for (String string : befores) {
            listD.add(string);
        }
        for (String bf : befores) {
            for (String af : afters) {
                if (bf.equals(af)) {
                    listS.add(bf);
                    listD.remove(bf);
                    listI.remove(bf);
                }
            }
        }
        map.put("listD", listD);
        map.put("listS", listS);
        map.put("listI", listI);

        return map;
    }

    /**
     * 更新会议公用处理方法
     * 
     * @param map
     */
    private void updateMeeting(Map<String, Object> map) {
        String act = String.valueOf(map.get("act"));
        if ("upload".equals(act)) {
            String pkMeeting = String.valueOf(map.get("pk_meeting"));
            if (null != pkMeeting && !"".equals(pkMeeting.trim())) {
                // 查询会议内容
                Map<String, Object> maprst = null;
                String attach = String.valueOf(maprst.get("attach"));
                String newAttach = "";
                if (null != attach && !"".equals(attach.trim())) {
                    if (null != map.get("attach")) {
                        newAttach = attach + ";" + map.get("attach").toString();
                    }
                } else {
                    if (null != map.get("attach")) {
                        newAttach = map.get("attach").toString();
                    }
                }
                map.put("attach", newAttach);
            }
        }
        // 更新数据
        // meetingDao.updateMeeting(map);
    }

    /**
     * 根据会议id获取所有参与者
     * 
     * @param meetingId
     * @return
     */
    private String getAllMeetingUsers(long meetingId) {
        String allUsers = "";
        // 根据会议id获取所有参与者
        List<ImMeetingUserVo> list = meetingInterface.findMeetingUserById(meetingId);
        logger.debug("根据会议id获取所有参与者,list{}", list.toString());
        if (!list.isEmpty()) {
            for (ImMeetingUserVo imMeetingUserVo : list) {
                allUsers = allUsers + imMeetingUserVo.getUserid() + ",";
            }
        }
        if (StringUtils.stringIsNotNull(allUsers)) {
            StringUtils.moveSplit(allUsers, ",");
        }
        return allUsers;
    }

    /**
     * 根据用户ID查询用户姓名
     * 
     * @param phone
     * @return
     */
    private String getMenberNameByPhone(String phone) {
        if (!StringUtils.stringIsNotNull(phone)) {
            return "";
        }
        // 根据用户ID查询用户姓名
        //TODO
        MemberInfoVO menberInfo = memberInfoInterface.findById(phone);
        logger.debug("查询对应用户信息，senderMenberInfo", menberInfo);
        if (null != menberInfo) {
            return (String) menberInfo.getMemberName();
        }
        return "";
    }

    /**
     * 封装会议对象 返回ImMeetingVo
     * 
     * @param pkMeeting
     * @param subject
     * @param content
     * @param attach
     * @param summary
     * @param fromuserid
     * @param fromusername
     * @param starttime
     * @param endtime
     * @param status
     * @param talkStatus
     * @param touserid
     * @param tousername
     * @param createtime
     * @param isdelete
     * @param reserve2
     * @param reserve3
     * @return
     */
    private ImMeetingVo packageImMeetingVo(long pkMeeting, String subject, String content, String attach, Long summary, String fromuserid, String fromusername, Date starttime, Date endtime,
            Long status, Long talkStatus, String touserid, String tousername, String createtime, Long isdelete, String reserve2, String reserve3) {
        ImMeetingVo imMeetingVo = new ImMeetingVo();
        imMeetingVo.setPkMeeting(pkMeeting);
        imMeetingVo.setSubject(subject);
        imMeetingVo.setContent(content);
        imMeetingVo.setAttach(attach);
        imMeetingVo.setSummary(summary);
        imMeetingVo.setFromuserid(fromuserid);
        imMeetingVo.setFromusername(fromusername);
        imMeetingVo.setStarttime(starttime);
        imMeetingVo.setEndtime(endtime);
        imMeetingVo.setStatus(talkStatus);
        imMeetingVo.setTalkStatus(talkStatus);
        imMeetingVo.setTouserid(touserid);
        imMeetingVo.setTousername(tousername);
        imMeetingVo.setCreatetime(createtime);
        imMeetingVo.setIsdelete(isdelete);
        imMeetingVo.setReserve2(reserve2);
        imMeetingVo.setReserve3(reserve3);
        return imMeetingVo;
    }

}
