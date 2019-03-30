/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ParaUtil;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.GetCalendarUtils;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.PackageReturnTypeUtils;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.mq.ActionRecordUtil;
import com.royasoft.vwt.cag.util.mq.ImTaskAction;
import com.royasoft.vwt.cag.util.mq.MQProvideUtil;
import com.royasoft.vwt.cag.util.mq.RedisAction;
import com.royasoft.vwt.cag.util.mq.RocketMqUtil;
import com.royasoft.vwt.cag.util.upload.FileUploadUtil;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.business.sendtask.api.interfaces.WorkTaskInterface;
import com.royasoft.vwt.soa.business.sendtask.api.vo.SendTaskReceiptVo;
import com.royasoft.vwt.soa.business.sendtask.api.vo.SendTaskVo;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 
 * @author ZHOUKQ
 *
 */
@Scope("prototype")
@Service
public class SendTaskServices implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SendTaskServices.class);

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;
    /** 日志保存接口 */
    @Autowired
    private OperationLogService operationLogService;
    /** 任务服务接口 */
    @Autowired
    private WorkTaskInterface workTaskInterface;
    /** 数据服务接口 */
    @Autowired
    private DatabaseInterface databaseInterface;

    /** 获取人员信息接口 */
    @Autowired
    private MemberInfoInterface memberInfoInterface;

    @Autowired
    private ActionRecordUtil actionRecordUtil;

    @Autowired
    private MemberInfoUtil memberInfoUtil;

    @Autowired
    private PackageReturnTypeUtils packageReturnTypeUtils;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.sendTask_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID

                    String user_id = queue_packet.getUser_id(); // 获取用户ID

                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    String tel_number = queue_packet.getTel_number();
                    /***************************** 业务逻辑处理 *********************************************/

                    String res = "";// 响应结果
                    if (function_id == null || function_id.length() <= 0 || user_id == null || user_id.length() <= 0 || request_body == null || request_body.length() <= 0) {
                        ResponsePackUtil.CalibrationParametersFailure(channel, "任务业务请求参数校验失败！");
                    } else {
                        res = sendTaskBusinessLayer(function_id, user_id, request_body, msg);
                    }
                    ResponsePackUtil.responseStatusOK(channel, res); // 响应成功
                    // String responseStatus = ResponsePackUtil.getResCode(res);
                    // if (null != responseStatus && !"".equals(responseStatus))
                    operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", "0000");

                }
            } catch (Exception e) {
                logger.error("任务业务逻辑处理异常", e);
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
     * 创建任务
     * 
     * @param user_id
     * @param request_body
     * @return
     */
    public String createSendTask(String user_id, String requestBody, Object request) {
        logger.debug("创建任务,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }

        JSONObject jsonObject = new JSONObject();// 拼接响应结果

        String fromUserId = requestJson.getString("fromUserId"); // 发送人ID

        String fromUsername = requestJson.getString("fromUsername");// 发送人姓名

        String type = requestJson.getString("type"); // 类型

        String title = requestJson.getString("title"); // 标题

        String content = requestJson.getString("content");// 内容

        String userGroup = requestJson.getString("userGroup"); // 用户组

        String usernameGroup = requestJson.getString("usernameGroup");// 用户姓名组
        
        String finishtime = requestJson.getString("finishtime");// 完成时间

        // 校验任务发送人ID
        if (!StringUtils.stringIsNotNull(fromUserId)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_501, ParaUtil.REGE_ERROY_MSG_302);
        }
        // 校验任务发送人姓名
        if (!StringUtils.stringIsNotNull(fromUsername)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_502, ParaUtil.SENDTASK_USERNAME_ISNULL);
        }
        // 校验任务类型
        if (!StringUtils.stringIsNotNull(type)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_503, ParaUtil.SENDTASK_TYPE_ISNULL);
        }
        // 校验任务内容
        if (!StringUtils.stringIsNotNull(content)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_504, ParaUtil.SENDTASK_CONTENT_ISNULL);
        }
        long longType = 1;// 普通任务
        try {
            longType = Long.parseLong(type);
        } catch (Exception e) {
            logger.debug("任务类型转换异常,type:{}", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
        }
        // 如果接收人手机号以逗号开头
        if (null != userGroup && userGroup.indexOf(",") == 0) {
            userGroup = userGroup.substring(userGroup.indexOf(",") + 1);
        }
        // 如果接收人名字以逗号开头
        if (null != usernameGroup && usernameGroup.indexOf(",") == 0) {
            usernameGroup = usernameGroup.substring(usernameGroup.indexOf(",") + 1);
        }
        // 要从配置文件中获取限制条数
        int limit = 100;
        try {
            limit = Integer.parseInt(ParamConfig.number_limit);// 获取配置文件信息
        } catch (Exception e) {
            logger.error(ParaUtil.SENDTASK_NUMBER_ERROR, e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SENDTASK_NUMBER_ERROR);
        }
        String[] userGroups = userGroup.split(",");
        if ((userGroups.length + 1) > limit) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_301, ParaUtil.SENDTASK_NUMBER_MORELIMIT);
        }
        String attachmentName = "";
        String attachMentUrl = "";
        try {
            // 保存附件，成功之后设置任务附件名称和下载地址
            JSONObject json = FileUploadUtil.uploadFileForSendTask(request);
            logger.debug("获取附件上传信息，json{}", null == json ? "" : json.toJSONString());
            if (null != json && !json.isEmpty()) {
                if ((Integer) json.get("length") > 1048576 * 5) {
                    return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_506, ParaUtil.SENDTASK_FILELENGTH_MORE5M);
                }
                attachmentName = json.getString("fileName");
                attachMentUrl = json.getString("pathUrl");
            }
        } catch (Exception e) {
            logger.error(ParaUtil.SENDTASK_SAVEFILE_ERROR, e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SENDTASK_SAVEFILE_ERROR);
        }
        try {
            // 保存创建任务信息
            SendTaskVo sendTaskId = workTaskInterface.save(packageSendTask(fromUserId, fromUsername, title, content, userGroup, usernameGroup, longType, attachmentName, attachMentUrl, new Date(),
                    null, "", "", 0, finishtime));
            
            logger.debug("保存创建任务信息,sendTaskId{}", null == sendTaskId ? "" : JSON.toJSONString(sendTaskId));
            if (null == sendTaskId) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_201, ParaUtil.CREATE_TASK_EROY_MSG);
            }

            // // 根据接收人列表添加默认回执并向接收人推送消息
            newMessageNotice(ParaUtil.IMTASK_TYPE_CREATE, sendTaskId);

            jsonObject.put("result", ParaUtil.SUCC_CODE);
            jsonObject.put("resultMsg", ParaUtil.CREATE_TASK_MSG);
            jsonObject.put("sendTaskId", PackageReturnTypeUtils.packageSendTaskMap(sendTaskId));
            jsonObject.put("createTime", GetCalendarUtils.getDate(sendTaskId.getCreatetime()));
            logger.debug("响应创建任务：mapResult{}", jsonObject.toJSONString());
            return jsonObject.toJSONString();
        } catch (Exception e) {
            logger.error("创建任务异常:{}", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_201, ParaUtil.CREATE_TASK_EROY_MSG);
        }
    }

    /**
     * 获取任务列表
     * 
     * @param user_id
     * @param request_body
     * @return
     */
    public String getSendTaskList(String user_id, String requestBody) {
        logger.debug("获取任务列表,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String username = requestJson.getString("username");
        String fromUserId = requestJson.getString("fromUserId");// 发起任务的人ID
        String toUserId = requestJson.getString("toUserId");// 参与任务的人ID
        String status = requestJson.getString("status");// 任务是否结束:0.未结束1.已结束
        String hasRead = requestJson.getString("hasRead");// 任务是否已读0.未读1.已读2.已完成
        String pageSize = requestJson.getString("pageSize");// 分页大小
        String pageIndex = requestJson.getString("pageIndex");// 分页起始位
        int indexOfPage = 1; // 分页起始位
        int sizeOfPage = 15;// 分页容量
        long longhasRead = -1;
        long longStatus = -1;
        // long longUserName = -1;
        if (StringUtils.stringIsNotNull(hasRead)) {
            try {
                longhasRead = Long.parseLong(hasRead);
            } catch (NumberFormatException e) {
                logger.error("获取任务列表类型转换异常！", e);
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
            }
        }
        if (StringUtils.stringIsNotNull(status)) {
            try {
                longStatus = Long.parseLong(status);
            } catch (NumberFormatException e) {
                logger.error("获取任务列表类型转换异常！", e);
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
            }
        }
        if (StringUtils.stringIsNotNull(username)) {
            try {
                // longUserName = Long.parseLong(username);
            } catch (NumberFormatException e) {
                logger.error("获取任务列表类型转换异常！", e);
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
            }
        }
        // 校验分页相关参数
        if (!StringUtils.stringIsNotNull(pageSize) || !StringUtils.stringIsNotNull(pageIndex)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.PARM_VALIDATION_ERROR);
        }
        try {
            sizeOfPage = Integer.parseInt(pageSize);
            indexOfPage = Integer.parseInt(pageIndex);
        } catch (NumberFormatException e) {
            logger.error("获取任务列表类型转换异常！", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
        }
        // 查询条件参数
        Map<String, Object> condition = new HashMap<String, Object>();
        // 排序条件
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        Map<String, Object> sendTaskMap = new HashMap<String, Object>();
        try {
            // 判断是我发起的任务，还是和我相关或者我参与的任务
            if (StringUtils.stringIsNotNull(fromUserId)) {
                condition.put("EQ_fromuserid", fromUserId);
                condition.put("EQ_status", status);
                sortMap.put("createtime", true);// 按照时间倒序
                sendTaskMap = workTaskInterface.findAllByPage(indexOfPage, sizeOfPage, condition, sortMap);
                logger.debug("获取发起任务列表：sendTaskMap{}", null == sendTaskMap ? "" : JSON.toJSONString(sendTaskMap));
            }
            if (StringUtils.stringIsNotNull(toUserId)) {// 判断是接收的任务。
                // 根据条件分页查询任务列表（我收到的任务和与我有关的任务）
                sendTaskMap = workTaskInterface.findSelfSendTaskOfPage(indexOfPage, sizeOfPage, toUserId, longhasRead, longStatus);
                logger.debug("获取与我相关任务列表：sendTaskMap{}", null == sendTaskMap ? "" : JSON.toJSONString(sendTaskMap));
            }
            if (StringUtils.stringIsNotNull(username)) {// 已归档
                sendTaskMap = workTaskInterface.findOverSendTaskOfPage(indexOfPage, sizeOfPage, username, longStatus);
                logger.debug("获取归档任务列表：sendTaskMap{}", null == sendTaskMap ? "" : JSON.toJSONString(sendTaskMap));
            }
            JSONObject jsonObject = new JSONObject();// 返回结果集
            if (!StringUtils.mapSOIsNotNull(sendTaskMap)) {
                jsonObject.put("result", ParaUtil.SUCC_CODE);
                jsonObject.put("resultMsg", ParaUtil.GET_TASKLIST_SUCC);
                jsonObject.put("sendTaskList", new ArrayList<SendTaskVo>());// 返回空数据给客户端
                jsonObject.put("listType", "");
                logger.debug("响应获取任务列表：mapResult{}", jsonObject.toString());
                return jsonObject.toJSONString();// ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.GET_TASKLIST_ERROR);
            }
            
            jsonObject.put("result", ParaUtil.SUCC_CODE);
            jsonObject.put("resultMsg", ParaUtil.GET_TASKLIST_SUCC);
            // 获取发送任务列表
            jsonObject.put("sendTaskList", PackageReturnTypeUtils.packageSendTaskInfo(sendTaskMap.get("content"), toUserId));
            // 供客户端使用,用于标识我发起、我参与、已结束
            jsonObject.put("listType", "");
            logger.debug("响应获取任务列表：mapResult{}", jsonObject.toString());
            return jsonObject.toString();
        } catch (Exception e) {
            logger.error("获取任务列表异常!", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.GET_TASKLIST_ERROR);
        }

    }

    /**
     * 编辑任务
     * 
     * @param user_id
     * @param request_body
     * @return
     */
    public String editSendTask(String user_id, String requestBody, Object request) {
        logger.debug("编辑任务,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }

        String id = requestJson.getString("id");// 任务ID

        String fromUserId = requestJson.getString("fromUserId");// 任务发起人

        String type = requestJson.getString("type");// 任务类型

        String title = requestJson.getString("title");// 任务标题

        String content = requestJson.getString("content");// 任务内容

        String userGroup = requestJson.getString("userGroup"); // 任务用户ID组

        String usernameGroup = requestJson.getString("usernameGroup"); // 任务用户姓名组

        String attachementUrl = requestJson.getString("attachementUrl");// 附件地址URL

        String finishtime = requestJson.getString("finishtime");// 完成时间
        
        if (!StringUtils.stringIsNotNull(id)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.TASKIDISNULL);
        }
        if (!StringUtils.stringIsNotNull(fromUserId)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_501, ParaUtil.TASK_FROMUSERID);
        }
        if (!StringUtils.stringIsNotNull(type)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_503, ParaUtil.SENDTASK_TYPE_ISNULL);
        }
        long longId = -1;
        long longType = -1;
        try {
            longId = Long.parseLong(id);
            longType = Long.parseLong(type);
        } catch (NumberFormatException e) {
            logger.error("参数类型转换异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
        }
        // 根据任务ID查询任务信息
        SendTaskVo sendTask = workTaskInterface.findSendTaskById(longId);
        logger.debug("根据任务ID查询任务信息,sendTask{}", null == sendTask ? "" : JSON.toJSONString(sendTask));
        if (null == sendTask) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.GET_TASKBYID_ERROR);
        }

        // 判断请求者是不是任务发起人
        if (!fromUserId.equals(sendTask.getFromuserid() + "")) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_502, ParaUtil.END_TALK_ERR_MSG2);
        }
        // 判断是否是已归档的任务
        if (sendTask.getStatus() == 1) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_301, ParaUtil.ALREADY_ARCHIVED_TASK_NOEDIT);
        }
        // 判断任务类型是不是为空
        if (!StringUtils.stringIsNotNull(type)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_503, ParaUtil.SENDTASK_TYPE_ISNULL);
        }
        try {
            // 保存附件，成功之后设置任务附件名称和下载地址
            JSONObject json = FileUploadUtil.uploadFileForSendTask(request);
            logger.error("获取附件上传信息，json{}", null == json ? "" : json.toJSONString());
            if (null != json && !json.isEmpty()) {

                if ((Integer) json.get("length") > 1048576 * 5) {
                    return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_506, ParaUtil.SENDTASK_FILELENGTH_MORE5M);
                }
                sendTask.setAttachmentname(json.getString("fileName"));
                sendTask.setAttachmenturl(json.getString("pathUrl"));
            }// 没有附件信息
            else {
                // 如果附件URL为空则取消附件信息
                if (!StringUtils.stringIsNotNull(attachementUrl)) {
                    sendTask.setAttachmentname("");
                    sendTask.setAttachmenturl("");
                }
            }
        } catch (Exception e) {
            logger.error(ParaUtil.SENDTASK_SAVEFILE_ERROR, e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.SENDTASK_SAVEFILE_ERROR);
        }
        sendTask.setType(longType);
        sendTask.setTitle(title);
        sendTask.setContent(content);
        sendTask.setUsergroup(userGroup);
        sendTask.setUsernamegroup(usernameGroup);
        sendTask.setSendTaskReceipts(null);
        if (StringUtils.stringIsNotNull(finishtime)) {
        	try {
        		if (":".indexOf(finishtime) < 0) {
        			finishtime = finishtime + " 23:59:59";
        		}
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date finishdate = sdf.parse(finishtime);
                sendTask.setFinishtime(finishdate);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        } else {
            sendTask.setFinishtime(null);
        }
        
        // 更新任务信息
        SendTaskVo sendTaskId = null;
        try {
            sendTaskId = workTaskInterface.save(sendTask);
            logger.debug("更新任务信息,sendTaskId{}", null == sendTaskId ? "" : JSON.toJSONString(sendTaskId));
        } catch (Exception e) {
            logger.error("更新任务信息异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_201, ParaUtil.CREATE_TASK_EROY_MSG);
        }
        if (null == sendTaskId) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_201, ParaUtil.CREATE_TASK_EROY_MSG);
        }

        newMessageNotice(ParaUtil.IMTASK_TYPE_UPDATE, sendTaskId);

        return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.CREATE_EDITTASK_MSG);

    }

    /**
     * 结束任务
     * 
     * @param user_id
     * @param request_body
     * @return
     */
    public String endSendTask(String user_id, String requestBody) {
        logger.debug(" 结束任务,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String strId = requestJson.getString("id");// 任务ID
        String fromUserId = requestJson.getString("fromUserId");// 发起人ID
        if (!StringUtils.stringIsNotNull(strId)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.TASKIDISNULL);
        }
        if (!StringUtils.stringIsNotNull(fromUserId)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_501, ParaUtil.TASK_FROMUSERID);
        }
        long longId = -1;
        try {
            longId = Long.parseLong(strId);
        } catch (NumberFormatException e) {
            logger.error("参数类型转换异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
        }

        // 根据任务ID查询任务
        SendTaskVo sendtask = workTaskInterface.findSendTaskById(longId);
        logger.debug("根据任务ID查询任务,sendTask{}", null == sendtask ? "" : JSON.toJSONString(sendtask));
        if (null == sendtask) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TASK_ISNOTEXIST_MSG);
        }
        // 获取发起人ID
        String longFromUserId = sendtask.getFromuserid();
        if (!fromUserId.equals(longFromUserId + "")) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.END_TALK_ERR_MSG2);
        }
        sendtask.setSendTaskReceipts(null);
        sendtask.setStatus(1L);
        //by wdw 归档时应填写endtime.   sendtask.setCreatetime(new Date());
        sendtask.setEndtime(new Date());
        try {
            SendTaskVo sendTaskVo = workTaskInterface.save(sendtask);
            logger.debug(" 结束任务返回结果,sendTaskVo:{}", null == sendtask ? "" : JSON.toJSONString(sendtask));
            if (null == sendTaskVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.END_TASK_ERROR);
            }
            return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.END_TASK_SUCC);

        } catch (Exception e) {
            logger.error(" 结束任务异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.END_TASK_ERROR);
        }

    }

    /**
     * 删除任务
     * 
     * @param user_id
     * @param request_body
     * @return
     */
    public String cancelSendTask(String user_id, String requestBody) {
        logger.debug("删除任务,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String id = requestJson.getString("id");// 任务ID
        String fromUserId = requestJson.getString("fromUserId");// 发起人ID

        if (!StringUtils.stringIsNotNull(id)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.TASKIDISNULL);
        }
        if (!StringUtils.stringIsNotNull(fromUserId)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_501, ParaUtil.TASK_FROMUSERID);
        }
        long longId = -1;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            logger.error("参数类型转换异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
        }

        // 根据任务ID查询任务
        SendTaskVo sendtask = workTaskInterface.findSendTaskById(longId);
        logger.debug("根据任务ID查询任务,sendTask{}", null == sendtask ? "" : JSON.toJSONString(sendtask));
        if (null == sendtask) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_502, ParaUtil.TASK_ISNOTEXIST_MSG);
        }
        // 获取发起人ID
        String longFromUserId = sendtask.getFromuserid();
        if (!fromUserId.equals(longFromUserId + "")) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_503, ParaUtil.END_TALK_ERR_MSG2);
        }
        // 判断任务是否已归档
        if (sendtask.getStatus() == 1) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_505, ParaUtil.ALREADY_ARCHIVED_TASK_NOEDIT);
        }
        try {
            // 根据任务ID删除任务
            boolean sendTaskFalg = workTaskInterface.deleteById(longId);
            logger.debug("根据任务ID删除任务返回结果，sendTaskFalg{}", sendTaskFalg);
            if (false == sendTaskFalg) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_504, ParaUtil.CANCLETASK_ERROR);
            }
            SendTaskReceiptVo sendTaskReceiptVo = workTaskInterface.findSendTaskReceiptById(longId);
            logger.debug("根据任务ID查询对应的回执返回结果，sendTaskReceiptVo{}", null == sendTaskReceiptVo ? "" : JSON.toJSON(sendTaskReceiptVo));
            if (null != sendTaskReceiptVo) {
                // 根据任务ID删除对应的回执列表
                boolean sendTaskReceiptFalg = workTaskInterface.deleteReceiptById(longId);
                logger.debug("根据任务ID删除对应的回执返回结果，sendTaskReceiptFalg{}", sendTaskReceiptFalg);
            }
            // 推送

            newMessageNotice(ParaUtil.IMTASK_TYPE_CANCLE, sendtask);

            return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.CANCLETASK_SUCC);
        } catch (Exception e) {
            logger.error("任务取消失败", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.CANCLETASK_ERROR);
        }

    }

    /**
     * 发送文本回执
     * 
     * @param userId
     * @param requestBody
     * @return
     */
    public String sendTaskReceipt(String userId, String requestBody) {
        logger.debug("发送文本回执,requestBody:{},userKey:{}", requestBody, userId);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.error("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }

        String fromUserId = requestJson.getString("fromUserId");// 发送人ID

        String sendTask = requestJson.getString("sendTaskId"); // 任务ID

        String content = requestJson.getString("content");// 回执文本内容
        if (!StringUtils.stringIsNotNull(sendTask)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.RECEIPT_TASKIDISNULL);
        }
        long sendTaskId = -1;
        try {
            sendTaskId = Long.parseLong(sendTask);
        } catch (Exception e) {
            logger.error(ParaUtil.TPYECHANGE_ERROR, e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
        }

        try {
            // 根据任务ID查询任务是否存在
            SendTaskVo sendTaskVo = workTaskInterface.findSendTaskById(sendTaskId);
            logger.debug("根据任务ID查询任务,sendTaskReceiptVo:{}", null == sendTaskVo ? "" : JSON.toJSONString(sendTaskVo));
            if (null == sendTaskVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_501, ParaUtil.END_TALK_ERR_MSG1);
            }
            // 判断任务是否结束
            if (sendTaskVo.getStatus() == 1) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_504, ParaUtil.END_TALK_MSG);
            }
            // 查询回执列表
            Map<String, Object> condition = new HashMap<String, Object>();
            condition.put("EQ_sendtaskid", sendTaskId);
            condition.put("EQ_fromuserid", fromUserId);
            List<SendTaskReceiptVo> listsendTaskReceiptVo = workTaskInterface.findSendTaskReceiptByCondition(condition, null);
            logger.debug("查询文本回执列表,listsendTaskReceiptVo:{}", null == listsendTaskReceiptVo ? "" : JSON.toJSONString(listsendTaskReceiptVo));
            SendTaskReceiptVo newSendTaskReceiptVo = null;
            if (null != listsendTaskReceiptVo && !listsendTaskReceiptVo.isEmpty()) {
                newSendTaskReceiptVo = listsendTaskReceiptVo.get(0);
            }
            if (null == newSendTaskReceiptVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_502, ParaUtil.NOEXEXIT_TASK);
            }
            // 将指定回执任务状态设为已回复
            newSendTaskReceiptVo.setStatus(2L);
            // 将指定回执任务回复内容放入任务回执对象中
            newSendTaskReceiptVo.setContent(content);
            newSendTaskReceiptVo.setReceivetime(new Date());

            // 更新任务回执
            SendTaskReceiptVo resSendTaskReceiptVo = workTaskInterface.save(newSendTaskReceiptVo);
            logger.debug("更新文本任务回执,resSendTaskReceiptVo:{}", null == resSendTaskReceiptVo ? "" : JSON.toJSONString(resSendTaskReceiptVo));
            if (null == resSendTaskReceiptVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_503, ParaUtil.RECEIPT_TASK_ERROR);
            }
            return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.RECEIPT_TASK_SUCC);
        } catch (Exception e) {
            logger.error("发送文本回执异常:{}", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.RECEIPT_TASK_ERROR);
        }

    }

    /**
     * 发送阅读回执
     * 
     * @param userId
     * @param requestBody
     * @return
     */
    public String sendReadTaskReceipt(String userId, String requestBody) {
        logger.debug("发送阅读回执,requestBody:{},userKey:{}", requestBody, userId);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }

        String fromUserId = requestJson.getString("fromUserId");// 发送人ID

        String sendTask = requestJson.getString("sendTaskId"); // 任务ID
        if (!StringUtils.stringIsNotNull(sendTask)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.RECEIPT_TASKIDISNULL);
        }
        if (!StringUtils.stringIsNotNull(fromUserId)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TASK_FROMUSERID);
        }
        try {
            // 查询回执列表
            Map<String, Object> condition = new HashMap<String, Object>();
            condition.put("EQ_sendtaskid", sendTask);
            condition.put("EQ_fromuserid", fromUserId);
            List<SendTaskReceiptVo> listsendTaskReceiptVo = workTaskInterface.findSendTaskReceiptByCondition(condition, null);
            logger.debug("查询回执列表,listsendTaskReceiptVo{}", null == listsendTaskReceiptVo ? "" : JSON.toJSONString(listsendTaskReceiptVo));
            SendTaskReceiptVo newSendTaskReceiptVo = null;
            if (null != listsendTaskReceiptVo && !listsendTaskReceiptVo.isEmpty()) {

                newSendTaskReceiptVo = listsendTaskReceiptVo.get(0);
            }
            if (null == newSendTaskReceiptVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_502, ParaUtil.NOEXEXIT_TASK);
            }
            // 判断指定回执状态
            if (0 != (newSendTaskReceiptVo.getStatus() == null ? 0 : newSendTaskReceiptVo.getStatus())) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.RECEIPT_TASK_STATIUS_IS0);
            }
            newSendTaskReceiptVo.setStatus(1L);
            // 更新任务回执
            SendTaskReceiptVo resSendTaskReceiptVo = workTaskInterface.save(newSendTaskReceiptVo);
            logger.debug("更新任务回执,resSendTaskReceiptVo{}", null == resSendTaskReceiptVo ? "" : JSON.toJSONString(resSendTaskReceiptVo));
            if (null == resSendTaskReceiptVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_503, ParaUtil.RECEIPT_READ_TASK_ERROR);
            }
            return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.RECEIPT_READ_TASK_SUCC);
        } catch (Exception e) {
            logger.error("发送阅读回执异常:{}", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.RECEIPT_READ_TASK_ERROR);
        }

    }

    /**
     * 查询任务回执列表集合
     * 
     * @param userId
     * @param requestBody
     * @return
     */
    @SuppressWarnings({ "unchecked", "unused" })
    public String getSendTaskReceiptList(String userId, String requestBody) {
        logger.debug("发送文本回执,requestBody:{},userKey:{}", requestBody, userId);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        int result = 0;
        String sendTaskId = requestJson.getString("sendTaskId");// 任务ID
        String pageSize = requestJson.getString("pageSize");// 分页容量
        String pageIndex = requestJson.getString("pageIndex");// 分页起始位

        if (!StringUtils.stringIsNotNull(pageSize)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.PARM_VALIDATION_ERROR);
        }
        if (!StringUtils.stringIsNotNull(pageIndex)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.PARM_VALIDATION_ERROR);
        }
        int intPageSize = -1;
        int intPageIndex = -1;
        try {
            intPageSize = Integer.valueOf(pageSize);
            intPageIndex = Integer.valueOf(pageIndex);
        } catch (Exception e) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
        }

        try {
            // 结果集合
            List<Map<String, Object>> listRes = new ArrayList<Map<String, Object>>();
            // 回执消息列表
            List<SendTaskReceiptVo> listSendTaskReceiptVo = null;
            Map<String, Object> condition = new HashMap<String, Object>();// 查询条件
            condition.put("EQ_sendtaskid", sendTaskId);
            // 查询回执列表数据
            Map<String, Object> mapSendTaskList = workTaskInterface.findAllReceiptByPage(intPageIndex, intPageSize, condition, null);
            logger.debug("查询回执列表数据,mapSendTaskList{}", null == mapSendTaskList ? "" : JSON.toJSONString(mapSendTaskList));
            if (null != mapSendTaskList) {
                listSendTaskReceiptVo = (List<SendTaskReceiptVo>) mapSendTaskList.get("content");
                if (null != listSendTaskReceiptVo) {
                    for (SendTaskReceiptVo sendTaskReceiptVo : listSendTaskReceiptVo) {
                        Map<String, Object> resMap = packageReturnTypeUtils.packageSendTaskReceiptInfo(sendTaskReceiptVo);
                        resMap.put("receiverName", getMenberNameByPhone(sendTaskReceiptVo.getFromuserid() + ""));
                        // 回执时间（特定格式:年月日时分）
                        resMap.put("receiveTime1", "");
                        listRes.add(resMap);
                    }
                }
            }
            jsonObject.put("sendTaskReceiptList", listRes);
            jsonObject.put("result", ParaUtil.SUCC_CODE);
            jsonObject.put("resultMsg", ParaUtil.GET_RECEIPT_TASKLIST_SUCC);
            logger.debug("查询任务回执列表返回结果：mapResult：{}", jsonObject.toString());
            return jsonObject.toString();
        } catch (Exception e) {
            logger.error("查询回执列表数据异常！", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.GET_RECEIPT_TASKLIST_ERROR);
        }

    }

    /**
     * 任务分模块请求
     * 
     * @param function_id
     * @param user_id
     * @param request_body
     * @param msg
     * @return
     */
    private String sendTaskBusinessLayer(String function_id, String user_id, String request_body, Object request) {

        String res = "";
        switch (function_id) {

            case FunctionIdConstant.createSendTask:// 创建任务
                res = createSendTask(user_id, request_body, request);
                break;

            case FunctionIdConstant.getSendTaskList:// 获取任务列表
                res = getSendTaskList(user_id, request_body);
                break;

            case FunctionIdConstant.editSendTask:// 编辑任务
                res = editSendTask(user_id, request_body, request);
                break;

            case FunctionIdConstant.endSendTask: // 结束任务
                res = endSendTask(user_id, request_body);
                break;

            case FunctionIdConstant.cancelSendTask:// 删除任务
                res = cancelSendTask(user_id, request_body);
                break;

            case FunctionIdConstant.sendTaskReceipt:// 发送文本回执
                res = sendTaskReceipt(user_id, request_body);
                break;

            case FunctionIdConstant.sendReadTaskReceipt: // 发送阅读回执
                res = sendReadTaskReceipt(user_id, request_body);
                break;

            case FunctionIdConstant.getSendTaskReceiptList:// 查询回执列表
                res = getSendTaskReceiptList(user_id, request_body);
                break;
            default:
                res = ResponsePackUtil.returnFaileInfo(); // 未知请求
        }
        return res;
    }

    /**
     * 封装任务对象
     * 
     * @param fromUserId
     * @param fromUsername
     * @param title
     * @param content
     * @param userGroup
     * @param usernameGroup
     * @param longType
     * @param attachmentname
     * @param attachmenturl
     * @param createtime
     * @param endtime
     * @param reserve4
     * @param reserve5
     * @param status
     * @return SendTaskVo
     */

    private SendTaskVo packageSendTask(String fromUserId, String fromUsername, String title, String content, String userGroup, String usernameGroup, long longType, String attachmentname,
            String attachmenturl, Date createtime, Date endtime, String reserve4, String reserve5, long status, String finishtime) {
        SendTaskVo sendTaskVo = new SendTaskVo();
        sendTaskVo.setFromuserid(fromUserId);
        sendTaskVo.setFromusername(fromUsername);
        sendTaskVo.setTitle(title);
        sendTaskVo.setContent(content);
        sendTaskVo.setUsergroup(userGroup);
        sendTaskVo.setUsernamegroup(usernameGroup);
        sendTaskVo.setId(databaseInterface.generateId("send_task", "id"));
        sendTaskVo.setType(longType);
        sendTaskVo.setAttachmentname(attachmentname);
        sendTaskVo.setAttachmenturl(attachmenturl);
        sendTaskVo.setCreatetime(createtime);
        sendTaskVo.setEndtime(endtime);
        sendTaskVo.setReserve4(reserve4);
        sendTaskVo.setReserve5(reserve5);
        sendTaskVo.setStatus(status);
        logger.debug("保存创建任务信息,packageSendTask finishtime{}", finishtime);
        
        if (StringUtils.stringIsNotNull(finishtime)) {
            logger.debug("保存创建任务信息,packageSendTask11 finishtime{}", finishtime);
    		if (":".indexOf(finishtime) < 0) {
    			finishtime = finishtime + " 23:59:59";
    		}
        	try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date finishdate = sdf.parse(finishtime);
                logger.debug("保存创建任务信息,packageSendTask11 finishdate{}", finishdate);
                
                sendTaskVo.setFinishtime(finishdate);
        	} catch (Exception e) {
                logger.debug("保存创建任务信息,packageSendTask22 finishtime{}", finishtime);
        		
        		e.printStackTrace();
        	}
        } else {
            logger.debug("保存创建任务信息,packageSendTask33 finishtime{}", finishtime);
        	
            sendTaskVo.setFinishtime(null);
        }
        return sendTaskVo;
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
        // TODO
        // MemberInfoVO menberInfo = memberInfoInterface.findById(phone);
        MemberInfoVO menberInfo = memberInfoUtil.findMemberInfoById(phone);
        logger.debug("查询对应用户信息，senderMenberInfo", null == menberInfo ? "" : JSON.toJSONString(menberInfo));
        if (null != menberInfo) {
            return (String) menberInfo.getMemberName();
        }
        return "";
    }

    /**
     * 推送并新增回执
     * 
     * @param operations
     * @param sendTasks
     */
    private void newMessageNotice(int operations, SendTaskVo sendTasks) {
        final SendTaskVo sendTask = sendTasks;
        final int operation = operations;

        String[] toUsers = sendTask.getUsergroup().split(",");
        // 获取任务回执ID
        long sendTaskReceiptId = databaseInterface.generateId("send_task_receipt", "id");
        // 针对创建
        if (ParaUtil.IMTASK_TYPE_CREATE == operation) {
            for (int i = 0; i < toUsers.length; i++) {
                if (null == toUsers[i] || toUsers[i].isEmpty()) {
                    continue;
                }
                // 保存回执消息
                SendTaskReceiptVo sendTaskReceiptVo = savesendTaskReceiptVo(sendTask.getId(), toUsers[i], sendTaskReceiptId);
                logger.debug("保存回执消息,sendTaskReceiptVo{}", null == sendTaskReceiptVo ? "" : JSON.toJSONString(sendTaskReceiptVo));
                if (null == sendTaskReceiptVo) {
                    continue;
                }
            }
        }
        if (ParaUtil.IMTASK_TYPE_UPDATE == operation) {
            for (int i = 0; i < toUsers.length; i++) {
                Map<String, Object> receiptCondition = new HashMap<String, Object>();
                receiptCondition.put("EQ_sendtaskid", sendTask.getId());
                receiptCondition.put("EQ_fromuserid", toUsers[i]);
                List<SendTaskReceiptVo> sendTaskReceiptList = workTaskInterface.findSendTaskReceiptByCondition(receiptCondition, null);
                logger.debug("分页查询任务回执列表,sendTaskReceiptVo{}", null == sendTaskReceiptList ? "" : JSON.toJSONString(sendTaskReceiptList));
                // 如果编辑时任务接收人之前不在接收人列表中
                if (null == sendTaskReceiptList || sendTaskReceiptList.isEmpty()) {
                    SendTaskReceiptVo sendTaskReceiptVo = savesendTaskReceiptVo(sendTask.getId(), toUsers[i], sendTaskReceiptId);
                    logger.debug("保存回执消息,sendTaskReceiptVo{}", null == sendTaskReceiptVo ? "" : JSON.toJSONString(sendTaskReceiptVo));
                    if (null == sendTaskReceiptVo) {
                        continue;
                    }
                }
            }
        }
        ImTaskAction ita = new ImTaskAction();
        Date now = new Date();
        ita.setSendDate(now.getTime() + "");
        ita.setFromUsername(sendTask.getFromusername());
        ita.setTitle(sendTask.getTitle());
        ita.setType(operation);

        RedisAction ra = new RedisAction();
        ra.setCreateTime(format.format(now));
        ra.setHead(MQProvideUtil.IMTASK_HEAD);
        ra.setSource(MQProvideUtil.SOURCE);
        ra.setMessage(ita);
        try {
            long msgId = actionRecordUtil.save(ra);

            ita.setMsg_id(msgId);
            String receivers = StringUtils.moveSplit(sendTask.getUsergroup(), ",");
            String[] receiverArr = receivers.split(",");
            for (int i = 0; i < receiverArr.length; i++) {
                ita.setTo_role_id(receiverArr[i]);
                RocketMqUtil.send(RocketMqUtil.ImTaskQueue, JSON.toJSONString(ita));
            }
        } catch (Exception e) {
            logger.error("任务推送异常:{}", e);
        }

    }

    /**
     * 保存回执信息
     * 
     * @param sendTaskId
     * @param fromuserid
     * @param sendTaskReceiptId
     * @return
     */
    private SendTaskReceiptVo savesendTaskReceiptVo(long sendTaskId, String fromuserid, long sendTaskReceiptId) {
        SendTaskReceiptVo sendTaskReceipt = new SendTaskReceiptVo();
        sendTaskReceipt.setSendtaskid(sendTaskId);
        sendTaskReceipt.setFromuserid(fromuserid);
        sendTaskReceipt.setCreatetime(new Date());
        sendTaskReceipt.setId(databaseInterface.generateId("send_task_receipt", "id"));
        SendTaskReceiptVo sendTaskReceiptVO = workTaskInterface.save(sendTaskReceipt);
        return sendTaskReceiptVO;
    }

}
