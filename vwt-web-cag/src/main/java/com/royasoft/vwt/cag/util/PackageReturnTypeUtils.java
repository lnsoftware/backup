/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.soa.business.meeting.api.vo.ImMeetingVo;
import com.royasoft.vwt.soa.business.sendtask.api.vo.SendTaskReceiptVo;
import com.royasoft.vwt.soa.business.sendtask.api.vo.SendTaskVo;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.CircleInterface;
import com.royasoft.vwt.soa.business.workteam.api.vo.CircleInfoVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamFileVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamReplyVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamVo;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 
 * 
 * 封装响应给客户端的结果集
 * 
 * @author ZHOUKQ
 * @Since:2016年03月1日
 */
@Component
public class PackageReturnTypeUtils {

    private static final Logger logger = LoggerFactory.getLogger(PackageReturnTypeUtils.class);

    @Autowired
    private MemberInfoUtil memberInfoUtil;

    /**
     * 封装工作圈消息对象
     * 
     * @param workTeamVo
     * @return Map<String, Object>
     */
    public static Map<String, Object> packageWorkTeamMsgVO(WorkTeamVo workTeamVo) {
        logger.debug("封装工作圈消息对象,workTeamVo{}", workTeamVo);
        if (null == workTeamVo) {
            return null;
        }
        Map<String, Object> workTeamMap = new HashMap<String, Object>();

        workTeamMap.put("pk_message", workTeamVo.getWorkTeamMessageVo().getPkMessage());
        workTeamMap.put("content", workTeamVo.getWorkTeamMessageVo().getContent());
        workTeamMap.put("image", workTeamVo.getWorkTeamMessageVo().getImage());
        workTeamMap.put("urlLink", workTeamVo.getWorkTeamMessageVo().getUrllink());
        workTeamMap.put("sendtype", workTeamVo.getWorkTeamMessageVo().getSendtype());
        workTeamMap.put("senderPhoneNumber", workTeamVo.getWorkTeamMessageVo().getSenderclientid());
        workTeamMap.put("sendtime", workTeamVo.getWorkTeamMessageVo().getSendtime());
        workTeamMap.put("address", workTeamVo.getWorkTeamMessageVo().getAddress());
        workTeamMap.put("memo", workTeamVo.getWorkTeamMessageVo().getMemo());
        workTeamMap.put("scope", workTeamVo.getWorkTeamMessageVo().getScope());
        workTeamMap.put("reserve1", workTeamVo.getWorkTeamMessageVo().getReserve1());
        workTeamMap.put("reserve2", workTeamVo.getWorkTeamMessageVo().getReserve2());
        workTeamMap.put("reserve3", workTeamVo.getWorkTeamMessageVo().getReserve3());
        workTeamMap.put("sender", workTeamVo.getWorkTeamMessageVo().getSender());
        workTeamMap.put("circleName", getCircleName(workTeamVo.getWorkTeamMessageVo().getReserve3()));
        workTeamMap.put("avatar", "");
        workTeamMap.put("attachment", PackageReturnTypeUtils.packageWorkTeamFileList(workTeamVo.getWorkTeamFileVos()));
        logger.debug("封装工作圈消息对象返回Map,workTeamMap{}", workTeamMap.toString());
        return workTeamMap;
    }
    /**
     * 获取圈子名称
     * @param circleId
     * @return
     */
    private static String getCircleName(String circleId){
        
        CircleInterface  circleInterface=SpringContext.getBean(CircleInterface.class);
        CircleInfoVo circleInfoVo=null;
        try {
            circleInfoVo = circleInterface.findByCircleId(circleId);
        } catch (Exception e) {
            logger.error("获取圈子ID名称异常");
            return "";
        }
        if(null==circleInfoVo){
            return "";
        }
        return circleInfoVo.getCircleName();
    }

    /**
     * 封装附件集合信息
     * 
     * @param listWorkTeamFiles
     * @return List<Map<String, Object>>
     */
    public static List<Map<String, Object>> packageWorkTeamFileList(List<WorkTeamFileVo> listWorkTeamFiles) {
        logger.debug("封装附件集合信息,listWorkTeamFiles{}", JSON.toJSONString(listWorkTeamFiles));
        if (null == listWorkTeamFiles || listWorkTeamFiles.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
        for (WorkTeamFileVo wtfile : listWorkTeamFiles) {
            Map<String, Object> wtFileMap = new HashMap<String, Object>();
            wtFileMap.put("pk_message", wtfile.getPkMessage());
            wtFileMap.put("fileName", wtfile.getFilename());
            wtFileMap.put("fileLength", wtfile.getFilelength());
            wtFileMap.put("filePaths", wtfile.getFilepaths());
            resList.add(wtFileMap);
        }
        logger.debug("封装附件集合信息返回结果集,resList{}", JSON.toJSONString(resList));
        return resList;
    }

    /**
     * 封装评论集合信息
     * 
     * @param listWorkTeamReplys
     * @returnList<Map<String, Object>>
     */
    public static List<Map<String, Object>> packageWorkTeamReplyList(List<WorkTeamReplyVo> listWorkTeamReplys) {
        logger.debug("封装评论集合信息,listWorkTeamReplys{}", JSON.toJSONString(listWorkTeamReplys));
        if (null == listWorkTeamReplys || listWorkTeamReplys.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
        for (WorkTeamReplyVo wtReply : listWorkTeamReplys) {
            Map<String, Object> wtReplyMap = new HashMap<String, Object>();
            wtReplyMap.put("pk_reply", wtReply.getPkReply());
            wtReplyMap.put("content", wtReply.getContent());
            wtReplyMap.put("sendType", wtReply.getSendtype());
            wtReplyMap.put("sender", wtReply.getSender());
            wtReplyMap.put("sendTime", wtReply.getSendtime());
            wtReplyMap.put("memo", wtReply.getMemo());
            wtReplyMap.put("memberName", wtReply.getMemoname());
            wtReplyMap.put("senderPhoneNumber", wtReply.getSenderclientid());
            wtReplyMap.put("receiver", wtReply.getReceiver());
            wtReplyMap.put("receiverPhoneNumber", wtReply.getReceiverclientid());
            wtReplyMap.put("pk_message", wtReply.getPkMessage());
            resList.add(wtReplyMap);
        }
        logger.debug("封装评论集合返回结果集,resList{}", JSON.toJSONString(resList));
        return resList;
    }

    /**
     * 封装工作圈消息集合
     * 
     * @param workTeamVo
     * @return List<Map<String, Object>>
     */
    public static List<Map<String, Object>> packageWorkTeamMessageList(List<WorkTeamVo> workTeamLists) {
        logger.debug("封装工作圈消息集合,resList{}", JSON.toJSONString(workTeamLists));
        List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
        if (null == workTeamLists || workTeamLists.isEmpty()) {
            return resList;
        }
        for (WorkTeamVo workTeamVo : workTeamLists) {
            resList.add(PackageReturnTypeUtils.packageWorkTeamMsgVO(workTeamVo));
        }
        logger.debug("封装工作圈消息返回结果集,resList{}", JSON.toJSONString(resList));
        return resList;
    }

    /**
     * 封装会议对象
     * 
     * @param imMeetingVo
     * @return Map<String,Object>
     */
    public static Map<String, Object> packageMeetingInfo(ImMeetingVo imMeetingVo) {
        logger.debug("封装会议对象,imMeetingVo{}", JSON.toJSONString(imMeetingVo));
        Map<String, Object> res = new HashMap<String, Object>();
        if (null == imMeetingVo) {
            return null;
        }
        res.put("pk_meeting", imMeetingVo.getPkMeeting());
        res.put("subject", imMeetingVo.getSubject());
        res.put("content", imMeetingVo.getContent());
        res.put("attach", imMeetingVo.getAttach());
        res.put("summary", imMeetingVo.getSummary());
        res.put("fromUserId", imMeetingVo.getFromuserid());
        res.put("fromUserName", imMeetingVo.getFromusername());
        res.put("startTime", imMeetingVo.getStarttime());
        res.put("endTime", imMeetingVo.getEndtime());
        res.put("status", imMeetingVo.getStatus());
        res.put("talkStatus", imMeetingVo.getTalkStatus());
        res.put("toUserId", imMeetingVo.getTouserid());
        res.put("toUserName", imMeetingVo.getTousername());
        res.put("createTime", imMeetingVo.getCreatetime());
        res.put("isDelete", imMeetingVo.getIsdelete());
        res.put("reserve2", imMeetingVo.getReserve2());
        res.put("reserve3", imMeetingVo.getReserve3());
        logger.debug("封装会议对象返回结果集,res{}", res.toString());
        return res;
    }

    /**
     * 将工作任务转成对应的List Map集合
     * 
     * @param listSendTaskVo
     * @return List<Map<String,Object>>
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> packageSendTaskInfo(Object listSendTaskVo, String toUserId) {
        if (null == listSendTaskVo) {
            return null;
        }
        
        List<SendTaskVo> listRes = (List<SendTaskVo>) listSendTaskVo;
        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
        for (SendTaskVo sendTask : listRes) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", sendTask.getId());
            map.put("fromUserId", sendTask.getFromuserid());
            map.put("fromUsername", sendTask.getFromusername());
            map.put("type", sendTask.getType());
            map.put("status", sendTask.getStatus());
            map.put("createTime", sendTask.getCreatetime());
            map.put("endTime", sendTask.getEndtime());
            String finishtime = "";
            DateFormat finish_format = new SimpleDateFormat("yyyy-MM-dd"); 
            if (null == sendTask.getFinishtime()) {
            	finishtime = "";
            } else {
            	finishtime = finish_format.format(sendTask.getFinishtime());
            }
            map.put("finishTime",finishtime);
            logger.debug("wdw packageSendTaskInfo id{}", sendTask.getId());
            logger.debug("wdw packageSendTaskInfo finishTime{}", sendTask.getFinishtime());
            map.put("title", sendTask.getTitle());
            map.put("userGroup", sendTask.getUsergroup());
            map.put("usernameGroup", sendTask.getUsernamegroup());
            map.put("content", sendTask.getContent());
            map.put("attachmentName", sendTask.getAttachmentname());
            map.put("attachementUrl", sendTask.getAttachmenturl());
            map.put("reserve4", sendTask.getReserve4());
            map.put("reserve5", sendTask.getReserve5());
            map.put("createTime1", "");
            map.put("subject", "");
            map.put("flag", "");
            
            Set<SendTaskReceiptVo> set = sendTask.getSendTaskReceipts();
            if (!StringUtils.isEmpty(toUserId) && null != set && set.size() > 0) {
                String str = JSON.toJSONString(set);
                JSONArray jsonArr = JSONArray.parseArray(str);
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject json = jsonArr.getJSONObject(i);
                    if (toUserId.equals(json.getString("fromuserid")))
                        map.put("status", StringUtils.isEmpty(json.getString("status")) ? "0" : json.getString("status"));
                }
            }
            res.add(map);
        }
        return res;
    }

    /**
     * 将工作任务转成对应的 Map集合
     * 
     * @param sendTask
     * @returnMap<String, Object>
     */
    public static Map<String, Object> packageSendTaskMap(SendTaskVo sendTask) {
        if (null == sendTask) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", sendTask.getId());
        map.put("fromUserId", sendTask.getFromuserid());
        map.put("fromUsername", sendTask.getFromusername());
        map.put("type", sendTask.getType());
        map.put("status", sendTask.getStatus());
        map.put("createTime", sendTask.getCreatetime());
        map.put("endTime", sendTask.getEndtime());
        map.put("finishTime", sendTask.getFinishtime());
        map.put("title", sendTask.getTitle());
        map.put("userGroup", sendTask.getUsergroup());
        map.put("usernameGroup", sendTask.getUsernamegroup());
        map.put("content", sendTask.getContent());
        map.put("attachmentName", sendTask.getAttachmentname());
        map.put("attachementUrl", sendTask.getAttachmenturl());
        map.put("reserve4", sendTask.getReserve4());
        map.put("reserve5", sendTask.getReserve5());
        map.put("createTime1", "");
        map.put("subject", "");
        map.put("flag", "");
        return map;
    }

    /**
     * 任务回执转成对应的Map<String,Object>
     * 
     * @param sendTaskReceipt
     * @return
     */
    public Map<String, Object> packageSendTaskReceiptInfo(SendTaskReceiptVo sendTaskReceipt) {
        if (null == sendTaskReceipt) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", sendTaskReceipt.getId());
        map.put("sendTaskId", sendTaskReceipt.getSendtaskid());
        map.put("content", sendTaskReceipt.getContent());
        map.put("fromUserId", sendTaskReceipt.getFromuserid());
        MemberInfoVO mivo = memberInfoUtil.findMemberInfoById(sendTaskReceipt.getFromuserid());
        map.put("fromUserMobile", mivo == null ? "" : mivo.getTelNum());// 客户端需要使用手机号进行拨打电话
        map.put("status", sendTaskReceipt.getStatus());
        map.put("receiveTime", sendTaskReceipt.getReceivetime());
        map.put("createTime", sendTaskReceipt.getCreatetime());
        return map;
    }

}
