package com.royasoft.vwt.cag.util.mq;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.vo.ImSendInfo;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamReplyInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamUserInterface;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamMessageVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamReplyVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamUserVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamVo;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

@Component
public class WorkTeamPushUtils {
    @Autowired
    private MemberInfoInterface memberInfoInterface;
    @Autowired
    private WorkTeamUserInterface workTeamUserInterface;
    @Autowired
    private WorkTeamReplyInterface workTeamReplyInterface;

    /**
     * 根据map中的说说ID获取说说并向相关用户推送消息
     * 
     * @param map
     * @param imSendInfo
     * @throws SQLException
     */
    public void pushNoticeByPkMessage(WorkTeamVo workTeamVo, final ImSendInfo imSendInfo) throws SQLException {
        WorkTeamMessageVo workTeamMessageVo = workTeamVo.getWorkTeamMessageVo();
        Long messageId = workTeamMessageVo.getPkMessage();
        imSendInfo.setId(messageId);
        String scope = String.valueOf(workTeamMessageVo.getScope());

        if (null == imSendInfo.getFromUser() || "".equals(imSendInfo.getFromUser()))
            return;
        if ("1".equals(scope)) {// 公开说说
            final MemberInfoVO senderMenberInfo = memberInfoInterface.findById(imSendInfo.getFromUser());
            if (null == senderMenberInfo || null == senderMenberInfo.getCorpId())
                return;
            new Thread() {
                public void run() {
                    PushCorpMsg(senderMenberInfo.getCorpId(), 34, 200, imSendInfo);
                };
            }.start();

        } else if ("2".equals(scope)) {// 私密说说
            List<WorkTeamUserVo> workTeamUserVos = workTeamUserInterface.findWkUserById(messageId);
            for (int i = 0; i < workTeamUserVos.size(); i++) {
                WorkTeamUserVo messageUser = workTeamUserVos.get(i);
                String receiver = messageUser.getReceiver();
                if (null == receiver || imSendInfo.getFromUser().equals(receiver))
                    continue;
                PushOpenfireMsg(String.valueOf(receiver), 34, 200, imSendInfo);
            }
        }
    }

    public void pushNoticeByPkMessageRelate(WorkTeamVo workTeamVo, final ImSendInfo imSendInfo) throws SQLException {
        WorkTeamMessageVo workTeamMessageVo = workTeamVo.getWorkTeamMessageVo();
        Long messageId = workTeamMessageVo.getPkMessage();
        imSendInfo.setId(messageId);
        String scope = String.valueOf(workTeamMessageVo.getScope());
        if (null == imSendInfo.getFromUser() || "".equals(imSendInfo.getFromUser()))
            return;

        if ("1".equals(scope)) {// 公开说说
            final MemberInfoVO senderMenberInfo = memberInfoInterface.findById(imSendInfo.getFromUser());
            if (null != senderMenberInfo && !"".equals(senderMenberInfo.getCorpId())) {
                List<WorkTeamReplyVo> allReplys = workTeamReplyInterface.findWkReplyByPkMessage(messageId);
                List<String> sendPersons = new ArrayList<String>();
                String msgPublish = workTeamMessageVo.getSenderclientid();
                sendPersons.add(msgPublish);
                if (null != allReplys && !allReplys.isEmpty()) {
                    for (WorkTeamReplyVo replyMap : allReplys) {
                        Long sendType = replyMap.getSendtype();
                        if (sendType == 1) {
                            String replyTel = String.valueOf(replyMap.getSenderclientid());
                            sendPersons.add(replyTel);
                        } else if (sendType == 2) {
                            String replyTel = replyMap.getMemo();
                            String[] replyTels = replyTel.split(",");
                            for (String string : replyTels) {
                                if (StringUtils.stringIsNotNull(string)) {
                                    sendPersons.add(string);
                                }
                            }
                        }
                    }
                }

                List<String> sendPersonsAfter = StringUtils.stringFilterRepeat(sendPersons);

                sendPersonsAfter.remove(imSendInfo.getFromUser());

                String sendStr = "";
                for (String string2 : sendPersonsAfter) {
                    sendStr = sendStr + string2 + ";";
                }
                sendStr = StringUtils.moveSplit(sendStr, ";");
                final String sendPersonsAfterFinal = sendStr;
                new Thread() {
                    public void run() {
                        PushOpenfireMsg(sendPersonsAfterFinal, 34, 200, imSendInfo);
                    };
                }.start();

            }
        } else if ("2".equals(scope)) {// 私密说说
            List<WorkTeamUserVo> workTeamUserVos = workTeamUserInterface.findWkUserById(messageId);
            for (int i = 0; i < workTeamUserVos.size(); i++) {
                WorkTeamUserVo messageUser = workTeamUserVos.get(i);
                String receiver = messageUser.getReceiver();
                if (null == receiver || imSendInfo.getFromUser().equals(receiver))
                    continue;
                PushOpenfireMsg(String.valueOf(receiver), 34, 200, imSendInfo);
            }
        }
    }

    public void wtNotice(WorkTeamVo workTeamVo, final ImSendInfo imSendInfo) throws SQLException {
        WorkTeamMessageVo workTeamMessageVo = workTeamVo.getWorkTeamMessageVo();
        Long messageId = workTeamMessageVo.getPkMessage();

        imSendInfo.setId(messageId);
        if (null != imSendInfo.getFromUser() && !"".equals(imSendInfo.getFromUser())) {
            List<WorkTeamReplyVo> allReplys = workTeamReplyInterface.findWkReplyByPkMessage(messageId);
            List<String> sendPersons = new ArrayList<String>();
            String msgPublish = workTeamMessageVo.getSenderclientid();
            sendPersons.add(msgPublish);
            if (null != allReplys && !allReplys.isEmpty()) {
                for (WorkTeamReplyVo replyMap : allReplys) {
                    Long sendType = replyMap.getSendtype();
                    if (sendType == 1) {
                        String replyTel = String.valueOf(replyMap.getSenderclientid());
                        sendPersons.add(replyTel);
                    } else if (sendType == 2) {
                        String replyTel = replyMap.getMemo();
                        String[] replyTels = replyTel.split(",");
                        for (String string : replyTels) {
                            if (StringUtils.stringIsNotNull(string)) {
                                sendPersons.add(string);
                            }
                        }
                    }
                }
            }

            List<String> sendPersonsAfter = StringUtils.stringFilterRepeat(sendPersons);

            sendPersonsAfter.remove(imSendInfo.getFromUser());

            MemberInfoVO memberInfoVO = memberInfoInterface.findById(imSendInfo.getFromUser());
            if (null != imSendInfo.getFromUser()) {
                if (null != memberInfoVO.getAvatar() && !"".equals(memberInfoVO.getAvatar())) {
                    imSendInfo.setTitle(memberInfoVO.getAvatar());
                } else {
                    imSendInfo.setTitle("");
                }
            } else {
                imSendInfo.setTitle("");
            }

            imSendInfo.setId(messageId);
            imSendInfo.setSendDate(String.valueOf(System.currentTimeMillis()));
            imSendInfo.setReserve1(String.valueOf(workTeamMessageVo.getSendtype()));
            if (workTeamMessageVo.getSendtype() == 3) {
                imSendInfo.setContent(workTeamMessageVo.getReserve1());
            } else {
                imSendInfo.setContent(workTeamMessageVo.getContent());
            }

            String image = workTeamMessageVo.getImage();
            if (null != image && !"".equals(image)) {
                image = StringUtils.moveSplit(image, ",");
                String[] images = image.split(",");
                if (StringUtils.stringIsNotNull(images[0])) {
                    imSendInfo.setFilePath(images[0]);
                } else {
                    imSendInfo.setFilePath("");
                }
            } else {
                imSendInfo.setFilePath("");
            }

            final List<String> sendPersonsAfterFinal = sendPersonsAfter;
            new Thread() {
                public void run() {
                    PushOpenfireMsg1(sendPersonsAfterFinal, 35, 200, imSendInfo);
                };
            }.start();
        }

    }

    /**
     * 用于有标志位消息推送方法
     * 
     * @param toUserId
     * @param position
     * @param result
     * @param imSendInfo
     */
    public static void PushOpenfireMsg1(List<String> toUserId, int position, int result, ImSendInfo imSendInfo) {
        ImSendInfo imSendInfoNew = base64ImSendInfo(imSendInfo);
        if (null != toUserId && !toUserId.isEmpty()) {
            for (String mobile : toUserId) {
                JSONObject json = new JSONObject();
                String uuid = java.util.UUID.randomUUID().toString();
                try {
                    String clientType = "";
                    String imsi = "";
                    json.put("result", result);
                    json.put("position", position);
                    json.put("uuid", uuid);
                    json.put("ImSendInfo", imSendInfoNew);
                    MsgPushUtil.sendSingleMsg("IM", "1", json.toString(), mobile, MQProvideUtil.FirstLvQueue, String.valueOf(position), uuid, clientType, imsi, "");

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    public static void PushCorpMsg(String corpId, int position, int result, ImSendInfo imSendInfo) {

        JSONObject json = new JSONObject();
        if (null != corpId && !"".equals(corpId)) {

            json.put("result", result);
            json.put("position", position);
            json.put("ImSendInfo", base64ImSendInfo(imSendInfo));
            if (position == 34) {
                MsgPushUtil.sendCorpMsgUUID("COMMON", MQProvideUtil.SecLvQueue, json, String.valueOf(corpId), String.valueOf(position), "");
            } else {
                MsgPushUtil.sendCorpMsg("COMMON", MQProvideUtil.SecLvQueue, json, String.valueOf(corpId), String.valueOf(position), "");
            }

        }

    }

    /**
     * 用于有标志位消息推送方法
     * 
     * @param toUserId
     * @param position
     * @param result
     * @param imSendInfo
     */
    public void PushOpenfireMsg(String toUserId, int position, int result, ImSendInfo imSendInfo) {
        Map<String, Object> map = new HashMap<String, Object>();
        ImSendInfo imSendInfoNew = base64ImSendInfo(imSendInfo);
        if (null != toUserId && !"".equals(toUserId)) {
            String[] toUserIds = toUserId.split(";");

            for (String mobile : toUserIds) {
                JSONObject json = new JSONObject();
                String uuid = java.util.UUID.randomUUID().toString();
                MemberInfoVO memberInfoVo = memberInfoInterface.findById(mobile);
                try {
                    json.put("result", result);
                    json.put("position", position);
                    json.put("uuid", uuid);

                    json.put("ImSendInfo", imSendInfoNew);

                    map.clear();
                    map.put("telNum", imSendInfoNew.getFromUser());
                    String clientType = "";
                    String imsi = "";
                    if (null != memberInfoVo) {
                        if (null != memberInfoVo.getAvatar() && !"".equals(memberInfoVo.getAvatar())) {
                            json.put("avatarUrl", memberInfoVo.getAvatar());
                        } else {
                            json.put("avatarUrl", "");
                        }
                    } else {
                        json.put("avatarUrl", "");
                    }
                    MsgPushUtil.sendSingleMsg("COMMON", "1", json.toString(), mobile, MQProvideUtil.SecLvQueue, String.valueOf(position), uuid, clientType, imsi, "");
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }

    private static ImSendInfo base64ImSendInfo(ImSendInfo imSendInfo) {
        ImSendInfo imSendInfo2 = new ImSendInfo();
        if (StringUtils.stringIsNotNull(imSendInfo.getContent())) {
            imSendInfo2.setContent(Base64.encodeBytes((imSendInfo.getContent().getBytes())));
        } else {
            imSendInfo2.setContent("");
        }
        if (StringUtils.stringIsNotNull(imSendInfo.getTitle())) {
            imSendInfo2.setTitle(Base64.encodeBytes((imSendInfo.getTitle().getBytes())));
        } else {
            imSendInfo2.setTitle("");
        }
        imSendInfo2.setFilePath(imSendInfo.getFilePath());
        imSendInfo2.setFromUser(imSendInfo.getFromUser());
        imSendInfo2.setId(imSendInfo.getId());
        imSendInfo2.setReserve1(imSendInfo.getReserve1());
        imSendInfo2.setReserve2(imSendInfo.getReserve2());
        imSendInfo2.setReserve3(imSendInfo.getReserve3());
        imSendInfo2.setReserve4(imSendInfo.getReserve4());
        imSendInfo2.setSendDate(imSendInfo.getSendDate());
        imSendInfo2.setUsers(imSendInfo.getUsers());
        imSendInfo2.setUuid(imSendInfo2.getUuid());
        return imSendInfo2;
    }

}
