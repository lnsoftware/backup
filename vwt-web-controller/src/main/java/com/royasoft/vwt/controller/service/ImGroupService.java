/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import com.royasoft.vwt.soa.business.im.api.interfaces.ImGroupInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.corpcustom.api.interfaces.CorpCustomInterface;
import com.royasoft.vwt.soa.uic.corpcustom.api.vo.CorpCustomVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.CWTMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.HLWMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.XXTMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 群聊业务处理类
 *
 * @Author:huangs
 * @Since:2016年8月23日
 */
@Scope("prototype")
@Service
public class ImGroupService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ImGroupService.class);

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
    private ImGroupInterface imGroupInterface;
    
    @Autowired
    private MemberInfoInterface memberInfoInterface;
    
    @Autowired
    private CWTMemberInfoInterface cwtMemberInfoInterface;
    
    @Autowired
    private XXTMemberInfoInterface xxtMemberInfoInterface;
    
    @Autowired
    private HLWMemberInfoInterface hlwMemberInfoInterface;
    
    @Autowired
    private CorpInterface corpInterface;
    
    @Autowired
    private ClientUserInterface clientUserInterface;
    
    @Autowired
    private CorpCustomInterface corpCustomInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.imGroup_queue.take();// 获取队列处理数据
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
                        // 查询人员的职位（一人多职）
                        case FunctionIdConstant.MEMBER_DUTY_QUERY:
                            resInfo = findMemberDuty(request_body);
                            break;
                        // 查询人员的群聊
                        case FunctionIdConstant.IMGROUP_QUERY:
                            resInfo = findImGroup(request_body);
                            break;
                        // 查询群聊的人员
                        case FunctionIdConstant.IMGROUP_MEMBER_QUERY:
                            resInfo = findImGroupMember(request_body);
                            break;
                         // 删除群聊人员
                        case FunctionIdConstant.IMGROUP_MEMBER_DELETE:
                            resInfo = deleteGroupMember(request_body);
                            break;
                            //删除群聊
                        case FunctionIdConstant.IMGROUP_DELETE:
                            resInfo = deleteGroup(request_body);
                            break;
                            //根据消息查询人员群组
                        case FunctionIdConstant.QUERYGROUPIDBYMESSAGE:
                            resInfo = queryGroupId(request_body);
                            break;
                            //添加人员到群组
                        case FunctionIdConstant.ADDMEMBERTOGROUP:
                            resInfo = addMemberToGroup(request_body);
                            break;
                            //修改群主
                        case FunctionIdConstant.CHANGETASKMASTER:
                            resInfo = chageTaskMaster(request_body);
                            break;
                            //添加群管理员
                        case FunctionIdConstant.ADDMANAGER:
                            resInfo = addManager(request_body);
                            break;
                            //删除群管理员
                        case FunctionIdConstant.DELMANAGER:
                            resInfo = delManager(request_body);
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
                logger.error("群聊业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
                channel.close();
            }
        }
    }
    
    /**
     * 查询人员的职位（一人多职）
     * 
     * @return
     */
    public String findMemberDuty(String requestBody) {
        logger.debug("查询人员职位,requestBody:{}", requestBody);
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
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
         }
        String telNum = trim(requestJson.getString("telNum"));
        if(null==telNum||"".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
            
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,this.getMemberInfos(telNum));
        } catch (Exception e) {
            logger.error("查询人员职位调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
        }
    }
    
    /**
     * 获取一人多职信息
     * 
     * @param memberInfoVOs
     * @return
     * @Description:
     */
    private JSONArray getMemberInfos(String telNum) {
        List<MemberInfoVO> memberInfoVOs = this.findMemberInfosByTelNum(telNum);// memberInfoInterface.findByTelNum(username);
        logger.debug("获取一人多职信息,memberInfoVOs:{}", memberInfoVOs.size());
        JSONArray jsonArray = new JSONArray();
        try {
            for (MemberInfoVO memberInfoVO : memberInfoVOs) {
                String corpId = memberInfoVO.getCorpId();
                CorpVO corpVO = corpInterface.findById(corpId);
                ClientUserVO clientUserVO = clientUserInterface.findById(memberInfoVO.getMemId());
                CorpCustomVO customVO = corpCustomInterface.findCorpCustomById(corpId);
                JSONObject jsonObject = new JSONObject();
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
                if (null != corpVO.getFromchannel() && !"".equals(corpVO.getFromchannel()) && corpVO.getFromchannel() == 7 && corpVO.getCorpMobilephone().equals(memberInfoVO.getTelNum()))
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
    
    /**
     * 根据手机号查询用户信息
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public List<MemberInfoVO> findMemberInfosByTelNum(String telNum) {
        logger.debug("根据手机号查询用户信息,telNum:{}", telNum);
        if (null == telNum || "".equals(telNum))
            return null;
        List<MemberInfoVO> memberInfoVOs = new ArrayList<MemberInfoVO>();
        List<MemberInfoVO> vwtList = memberInfoInterface.findByTelNum(telNum);
        if (null != vwtList && !vwtList.isEmpty())
            memberInfoVOs.addAll(vwtList);
        List<MemberInfoVO> cwtList = cwtMemberInfoInterface.findCWTMemberByTelNum(telNum);
        if (null != cwtList && !cwtList.isEmpty())
            memberInfoVOs.addAll(cwtList);
        List<MemberInfoVO> xxtList = xxtMemberInfoInterface.findXXTMemberByTelNum(telNum);
        if (null != xxtList && !xxtList.isEmpty())
            memberInfoVOs.addAll(xxtList);
        List<MemberInfoVO> hlwList = hlwMemberInfoInterface.findHLWMemberByTelNum(telNum);
        if (null != hlwList && !hlwList.isEmpty())
            memberInfoVOs.addAll(hlwList);
        logger.debug("根据手机号查询用户信息(返回),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        return memberInfoVOs;
    }
    
    /**
     * 查询所有群聊
     * 
     * @return
     */
    public String findImGroup(String requestBody) {
        logger.debug("查询所有的群聊,requestBody:{}", requestBody);
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
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
         }
        String memId = trim(requestJson.getString("memId"));
        if(null==memId||"".equals(memId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
            
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,imGroupInterface.getGroupInfosByUserId(memId));
        } catch (Exception e) {
            logger.error("查询人员群聊调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
        }
    }
    /**
     * 查询群聊的人员
     * 
     * @return
     */
    public String findImGroupMember(String requestBody) {
        logger.debug("查询群聊的人员,requestBody:{}", requestBody);
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
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
         }
        String groupId = trim(requestJson.getString("groupId"));
        if(null==groupId||"".equals(groupId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        if ("".equals(groupId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        try {
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, imGroupInterface.getGroupMembersInfoByGroupId(groupId));
        } catch (Exception e) {
            logger.error("查询群聊的人员调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
        }
    }

    /**
     * 查询群聊的人员
     * 
     * @return
     */
    public String deleteGroupMember(String requestBody) {
        logger.debug("删除群聊人员,requestBody:{}", requestBody);
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
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
         }
        String groupId = trim(requestJson.getString("groupId"));
        String memId = trim(requestJson.getString("memId"));
        
        if(null==groupId||"".equals(groupId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        if ("".equals(groupId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        try {
            boolean flag= imGroupInterface.deleteTaskMember(groupId, memId);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, flag);
        } catch (Exception e) {
            logger.error("查询群聊的人员调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
        }
    }
    
    
    /**
     * 查询群聊的人员
     * 
     * @return
     */
    public String deleteGroup(String requestBody) {
        logger.debug("解散群聊,requestBody:{}", requestBody);
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
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
         }
        String groupId = trim(requestJson.getString("groupId"));
        
        if(null==groupId||"".equals(groupId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        if ("".equals(groupId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        try {
            boolean flag= imGroupInterface.deleteGroup(groupId);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, flag);
        } catch (Exception e) {
            logger.error("解散群聊调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
        }
    }

    /**
     * 根据群聊消息获取群Id
     * 
     * @return
     */
    public String queryGroupId(String requestBody) {
        logger.debug("根据群聊消息获取群Id,requestBody:{}", requestBody);
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
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
         }
        String memId = trim(requestJson.getString("memId"));
        
        if(null==memId||"".equals(memId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        try {
            Set<String> msg= imGroupInterface.findTaskIdByMessage(memId);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, msg);
        } catch (Exception e) {
            logger.error("据群聊消息获取群Id服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
        }
    }
    
    
    /**
     * 添加人员进群
     * 
     * @return
     */
    public String addMemberToGroup(String requestBody) {
        logger.debug("添加人员进群,requestBody:{}", requestBody);
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
         return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
         }
        String groupId = trim(requestJson.getString("groupId"));
        String memId = trim(requestJson.getString("memId"));
        if(null==groupId||"".equals(groupId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        if(null==memId||"".equals(memId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        
        try {
            boolean flag= imGroupInterface.addTaskMember(groupId,memId);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, flag);
        } catch (Exception e) {
            logger.error("添加人员进群调取服务化异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
        }
    }
    
    
    /**
     * 更换群主
     * 
     * @return
     */
    public String chageTaskMaster(String requestBody) {
        logger.debug("更换群主信息,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
       
        String groupId = trim(requestJson.getString("groupId"));
        String memId = trim(requestJson.getString("memId"));
        if(StringUtils.isEmpty(groupId)||StringUtils.isEmpty(memId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        
        try {
            boolean flag= imGroupInterface.changeTaskMaster (groupId,memId);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, flag);
        } catch (Exception e) {
            logger.error("调用更换群主服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
        }
    }
    
    
    /**
     * 新增管理员
     * 
     * @return
     */
    public String addManager(String requestBody) {
        logger.debug("添加群管理员,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
       
        String groupId = trim(requestJson.getString("groupId"));
        String managers = trim(requestJson.getString("managers"));
        if(StringUtils.isEmpty(groupId)||StringUtils.isEmpty(managers))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        try {
            boolean flag= imGroupInterface.addTaskManager (groupId,managers);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, flag);
        } catch (Exception e) {
            logger.error("调用添加群管理员服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
        }
    }
    
    /**
     * 新增管理员
     * 
     * @return
     */
    public String delManager(String requestBody) {
        logger.debug("添加群管理员,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
       
        String groupId = trim(requestJson.getString("groupId"));
        String managers = trim(requestJson.getString("managers"));
        if(StringUtils.isEmpty(groupId)||StringUtils.isEmpty(managers))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9301, "");
        try {
            boolean flag= imGroupInterface.deleteTaskManager(groupId,managers);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, flag);
        } catch (Exception e) {
            logger.error("调用添加群管理员服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL9302, "");
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
