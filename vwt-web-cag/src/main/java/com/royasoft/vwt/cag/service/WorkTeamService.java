/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
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
import com.royasoft.vwt.cag.util.IntegralUtil;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.PackageReturnTypeUtils;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.SensitivewordFilter;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.SystemUtils;
import com.royasoft.vwt.cag.util.mq.ActionRecordUtil;
import com.royasoft.vwt.cag.util.mq.MQProvideUtil;
import com.royasoft.vwt.cag.util.mq.RedisAction;
import com.royasoft.vwt.cag.util.mq.RocketMqUtil;
import com.royasoft.vwt.cag.util.mq.WorkCircleAction;
import com.royasoft.vwt.cag.util.mq.WorkCircleReply;
import com.royasoft.vwt.cag.util.upload.FastDFSUtil;
import com.royasoft.vwt.cag.util.upload.FileUploadUtil;
import com.royasoft.vwt.cag.util.upload.WorkTeamFileUtil;
import com.royasoft.vwt.common.security.AESUtil;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.business.festival.api.interfaces.FestivalInterface;
import com.royasoft.vwt.soa.business.festival.api.vo.FestivalVo;
import com.royasoft.vwt.soa.business.im.api.interfaces.ImMessageInterface;
import com.royasoft.vwt.soa.business.sensitivewords.api.interfaces.SensitiveWordInterface;
import com.royasoft.vwt.soa.business.sensitivewords.api.vo.ErrorMsgVO;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareInterface;
import com.royasoft.vwt.soa.business.square.api.vo.SquareVo;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.CircleInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.SpecialUserInfoInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamFileInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamMessageInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamReplyInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamUserInterface;
import com.royasoft.vwt.soa.business.workteam.api.vo.CircleInfoVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.SpecialUserInfoVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamFileVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamMessageVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamReplyVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamUserVo;
import com.royasoft.vwt.soa.business.workteam.api.vo.WorkTeamVo;
import com.royasoft.vwt.soa.sundry.logmanager.api.interfaces.LogManagerInterface;
import com.royasoft.vwt.soa.sundry.logmanager.api.vo.LogManagerVo;
import com.royasoft.vwt.soa.sundry.sysparam.api.interfaces.SystemParamInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.infofeedback.api.interfaces.InfoFeedbackInterface;
import com.royasoft.vwt.soa.uic.infofeedback.api.vo.InfoFeedbackVo;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;
import com.royasoft.vwt.soa.uic.reservefield.api.interfaces.ReserveFieldInterface;
import com.royasoft.vwt.soa.uic.reservefield.api.vo.ReserveFieldVO;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

/**
 * 工作圈业务处理
 * 
 * @author ZHOUKQ
 * @Since:2016年03月1日
 */
@Scope("prototype")
@Service
public class WorkTeamService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkTeamService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;
    /** 工作圈消息接口 */
    @Autowired
    private WorkTeamMessageInterface workTeamMessageInterface;
    /** 工作圈消息评论接口 */
    @Autowired
    private WorkTeamReplyInterface workTeamReplyInterface;
    /** 工作圈接口 */
    @Autowired
    private WorkTeamInterface workTeamInterface;
    /** 获取人员信息接口 */
    @Autowired
    private CorpInterface corpInterface;
    @Autowired
    private SystemParamInterface systemParamInterface;
    @Autowired
    private ReserveFieldInterface reserveFieldInterface;
    @Autowired
    private DepartMentInterface departMentInterface;
    /** 工作圈文件接口 */
    @Autowired
    private WorkTeamFileInterface workTeamFileInterface;
    /** 工作圈相关人员接口 */
    @Autowired
    private WorkTeamUserInterface workTeamUserInterface;
    /** 数据调用接口 */
    @Autowired
    private DatabaseInterface databaseInterface;
    /** 工作圈日志接口 */
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ClientUserInterface clientUserInterface;
    @Autowired
    private InfoFeedbackInterface infoFeedbackInterface;
    @Autowired
    private LogManagerInterface logManagerInterface;
    @Autowired
    private SquareInterface squareInterface;
    @Autowired
    private ActionRecordUtil actionRecordUtil;

    @Autowired
    private IntegralUtil integralUtil;

    @Autowired
    private MemberInfoUtil memberInfoUtil;

    @Autowired
    private CircleInterface circleInterface;

    @Autowired
    private SensitiveWordInterface sensitiveWordInterface;
    
    @Autowired
    private FestivalInterface festivalInterface;

    @Autowired
    private ImMessageInterface imMessageInterface;

    @Autowired
    private SpecialUserInfoInterface specialUserInfoInterface;
    
    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.WorkTeam_queue.take();// 获取队列处理数据
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

                    //if (function_id == null || function_id.length() <= 0 || user_id == null || user_id.length() <= 0 || request_body == null || request_body.length() <= 0) {
                    if (function_id == null || function_id.length() <= 0){   
                        ResponsePackUtil.CalibrationParametersFailure(channel, "工作圈业务请求参数校验失败！");
                    } else {
                        // 工作圈具体业务分层跳转
                        res = WorkTeamBusinessLayer(channel, request, function_id, user_id, request_body, msg);
                    }
                    // 响应成功
                    ResponsePackUtil.responseStatusOK(channel, res);
                    // String responseStatus = ResponsePackUtil.getResCode(res);
                    // if (null != responseStatus && !"".equals(responseStatus))
                    operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", "0000");
                }
            } catch (Exception e) {
                logger.error("工作圈业务逻辑处理异常", e);
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
     * 获取工作圈列表消息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String getWorkTeamMsgList(String user_id, String requestBody) {
        logger.debug("获取工作圈列表消息,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        JSONObject json = new JSONObject();// 返回参数JSON串
        // Map<String, Object> mapResult = new HashMap<String, Object>();// 响应Map集合

        String pageSize = StringUtils.strIsNullAndGetValue(requestJson.getString("pageSize"), "10");// 获取分页大,并判断是否为空并赋值

        String identId = requestJson.getString("identId");// ?

        String refreshFlag = StringUtils.strIsNullAndGetValue(requestJson.getString("refreshFlag"), "0"); // 是否刷新,并判断是否为空并赋值

        String userName = requestJson.getString("userName");// 用户id

        String corpId = requestJson.getString("corpId");// 企业Id

        String circleId = requestJson.getString("circleId");// 圈子ID
        
        String startTime = requestJson.getString("startTime");// 开始时间
        String endTime = requestJson.getString("endTime");// 结束时间
//        String corpIdList = requestJson.getString("corpIdList");// 逗号拼接选中corpId，corpFlag为1时必传
//        String corpFlag = requestJson.getString("corpFlag");// 0表示全部1表示有选中企业
        String deptList = requestJson.getString("deptList");// 逗号拼接部门id，deptFlag为1时必传
        String deptFlag = requestJson.getString("deptFlag");// 0表示全部，1表示有选中部门
        String status = requestJson.getString("status");// 0表示选中企业and部门，1表示选中特别关注
        String specialFlag = requestJson.getString("specialFlag");// 0表示全部特别关注，1表示选中特别关注
        String specialList = requestJson.getString("specialList");// 逗号拼接特别关注人员id;sepcialFlag为1时必传

        String param = requestJson.getString("param");// 参数（通过说说内容，标题 模糊搜索）
        
        
        if (!StringUtils.stringIsNotNull(circleId)) {
            logger.debug("获取工作圈列表消息,circleId参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_CIRCLEID_ERROY);
        }
        // 判断用户名是否为空！
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("获取工作圈列表消息,userName参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_USERNAME_ERROY);
        }
        if (!StringUtils.stringIsNotNull(identId)) {
            refreshFlag = "0";
        }
        //TODO
        if (!StringUtils.stringIsNotNull(corpId)) {
            try {
                // 获取用户所对应的企业ID
                corpId = getMenberCorpIdById(userName);
                logger.debug("获取用户所对应的企业ID,corpId:{}", corpId);
            } catch (Exception e) {
                logger.error("获取用户所对应的企业ID异常", e);
                return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_PKCORPID_ERROY);
            }

        }
        ClientUserVO clientUserVO = clientUserInterface.findById(userName);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");
        json.put("isBlack", false);
        if (memberInfoUtil.checkIsInBlackList(corpId, clientUserVO.getTelNum()))
            json.put("isBlack", true);
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();// 消息视图包含附件信息排序条件Map
        Map<String, Object> condition = new HashMap<String, Object>();// 消息视图包含附件信息查询条件Map
        // 赋予查询条件值
        // if ("-1".equals(circleId)) {
        // try {
        // String circleIds = getCircleIds(user_id);
        // condition.put("IN_reserve3", circleIds);
        // } catch (Exception e) {
        // return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_GETCIRCLEID_ERROY);
        // }
        // } else {
        // }
        if (StringUtils.stringIsNotNull(startTime)) {
			
        	condition.put("start_time_sendtime", startTime);
		}
        if (StringUtils.stringIsNotNull(startTime)) {
        	
        	condition.put("end_time_sendtime", endTime);
        }
        if (StringUtils.stringIsNotNull(param)) {
        	
        	condition.put("ORLIKE_content", param);
        	condition.put("ORLIKE_reserve1",  param );
        }
        
        // 0表示选中企业and部门，1表示选中特别关注
        if (StringUtils.stringIsNotNull(status)) {
        	if ("0".equals(status)) {
            	
        		// 0表示全部，1表示有选中部门
        		if ("0".equals(deptFlag)) {
        			//
        			condition.put("EQ_pkCorp", corpId);

				}else{
					// 查询部门下所有人员id
					List<DepartMentVO> lists = departMentInterface.findByCorpId(corpId);
					List<String> nodes = getAllChildNodes(lists, deptList, new ArrayList<String>());
					nodes.add(deptList);
					String deptIds = "";
					if (CollectionUtils.isNotEmpty(nodes)) {
						for (String string : nodes) {
							deptIds = deptIds + string + ",";
						}
						deptIds = deptIds.substring(0,deptIds.length() - 1);
						condition.put("IN_deptId", deptIds);
					}
					
				}
			
		}else{
			// 0表示全部特别关注，1表示选中特别关注
			if ("0".equals(specialFlag)) {
				try {
					List<SpecialUserInfoVo> ids = specialUserInfoInterface.queryUserIds(userName);
					String userList = "";
					if (CollectionUtils.isNotEmpty(ids)) {
						for (SpecialUserInfoVo specialUserInfoVo : ids) {
							userList = userList + specialUserInfoVo.getSpecialUserId() + ",";
						}
						userList = userList.substring(0,userList.length() - 1);
						condition.put("IN_sendclientid", userList);
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				
				condition.put("EQ_sendclientid", specialList);
			}
		}
		}else{
			condition.put("EQ_pkCorp", corpId);
		}
        
        if ("0".equals(circleId.trim())) {
            condition.put("EQ_reserve3", circleId);
        } else {
            condition.put("IN_reserve3", circleId);
        }
        //TODO
//        condition.put("EQ_pkCorp", corpId);
        condition.put("IN_receiver", "0," + userName);
        if (refreshFlag.equals("2")) {
            condition.put("LT_pkMessage", identId);
        }
        sortMap.put("pkMessage", false);
        List<Map<String, Object>> wtMsglist = null;// 消息列表
        List<Map<String, Object>> wtReplylist = null;// 评论集合
        try {
            int IntpageSize = Integer.valueOf(pageSize);
            wtMsglist = getWtMsgList(IntpageSize, condition, sortMap);// 获取消息列表

            wtReplylist = getWtMsgReplyList(wtMsglist);// 获取评论集合

        } catch (Exception e) {
            logger.error("获取工作圈列表消息异常", e);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MSGLIST_ERROR);
        }
        // 将结果放入JSON中
        json.put("content", wtMsglist);
        json.put("reply", wtReplylist);
        json.put("code", ParaUtil.SUCC_CODE);
        json.put("msg", ParaUtil.WORKTEAM_MSGLIST_SUCCESS);
        logger.debug("返回工作圈列表消息,mapResult{}:", json.toString());
        return json.toString();

    }

    /**
     * 获取工作圈个人说说消息详情
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String getSomeOneWorkTeamMsgList(String user_id, String requestBody) {
        logger.debug("获取工作圈消息详情,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        JSONObject json = new JSONObject();// 返回参数JSON串
        // Map<String, Object> mapResult = new HashMap<String, Object>();// 响应Map集合

        String pageSize = StringUtils.strIsNullAndGetValue(requestJson.getString("pageSize"), "10");// 获取分页大,并判断是否为空并赋值

        String identId = requestJson.getString("identId");// ?

        String refreshFlag = StringUtils.strIsNullAndGetValue(requestJson.getString("refreshFlag"), "0"); // 是否刷新,并判断是否为空并赋值

        String userName = requestJson.getString("userName");// 用户名

        String corpId = requestJson.getString("corpId");// 企业Id

        String senderPhoneNumber = requestJson.getString("senderPhoneNumber");// 发送人手机号码

        String circleId = requestJson.getString("circleId");// 圈子ID

        if (!StringUtils.stringIsNotNull(circleId)) {
            logger.debug("获取工作圈列表消息,circleId参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_CIRCLEID_ERROY);
        }

        // 判断用户名是否为空！
        if (!StringUtils.stringIsNotNull(userName)) {
            logger.debug("获取工作圈列表消息,userName参数为空");
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_USERNAME_ERROY);
        }
        if (!StringUtils.stringIsNotNull(identId)) {
            refreshFlag = "0";
        }
        if (!StringUtils.stringIsNotNull(corpId)) {
            try {
                // 获取用户所对应的企业ID
                corpId = getMenberCorpIdById(userName);
                logger.debug("获取用户所对应的企业ID,corpId:{}", corpId);
            } catch (Exception e) {
                logger.error("获取用户所对应的企业ID异常", e);
                return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_PKCORPID_ERROY);
            }

        }
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();// 消息视图包含附件信息排序条件Map
        Map<String, Object> condition = new HashMap<String, Object>();// 消息视图包含附件信息查询条件Map
        if ("0".equals(circleId.trim())) {
            condition.put("EQ_reserve3", circleId);
        } else {
            condition.put("IN_reserve3", circleId);
        }
        // 赋予查询条件值
        condition.put("EQ_pkCorp", corpId);
        condition.put("IN_receiver", "0," + userName);
        condition.put("EQ_sendclientid", senderPhoneNumber);
        if (refreshFlag.equals("2")) {
            condition.put("LT_pkMessage", identId);
        }
        // 排序
        sortMap.put("pkMessage", false);
        List<Map<String, Object>> wtMsglist = null;// 消息列表
        List<Map<String, Object>> wtReplylist = null;// 评论集合
        try {
            int IntpageSize = Integer.valueOf(pageSize);
            wtMsglist = getWtMsgList(IntpageSize, condition, sortMap);// 获取消息列表

            wtReplylist = getWtMsgReplyList(wtMsglist);// 获取评论集合

            // for (WorkTeamReplyVo workTeamReplyVo : wtReplylist) {
            // if (null != workTeamReplyVo) {
            // if (null != workTeamReplyVo.getMemo()) {
            // String memo = workTeamReplyVo.getMemo();
            // if (null != memo && "".equals(memo.trim())) {
            // memo = MbTools.moveSplit(memo, ",");
            // String[] memoArray = memo.split(",");
            // String memberName = "";
            // for (int i = 0; i < memoArray.length; i++) {
            // String name = "";// 查询用户名menberInfoService.getNameByTels(memoArray[i]);
            // memberName = memberName + name + ",";
            // }
            // workTeamReplyVo.setMemoname(MbTools.moveSplit(memberName, ","));
            // }
            // }
            // }
            // }
        } catch (Exception e) {
            logger.error("获取工作圈个人说说异常", e);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MYMSGLIST_ERROR);
        }
        // 将结果放入JSON中
        json.put("content", wtMsglist);
        json.put("reply", wtReplylist);
        json.put("code", ParaUtil.SUCC_CODE);
        json.put("msg", ParaUtil.WORKTEAM_MYMSGLIST_SUCC);
        // mapResult.put("jsonObject", json);
        logger.debug("返回获取工作圈个人说说結果集,mapResult{}:", json.toString());
        return json.toString();

    }

    /**
     * 发表个人说说
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String appendWorkTeamMessage(String user_id, String requestBody, Object msg) {
        logger.debug("获取工作圈消息详情,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }

        String sendType = requestJson.getString("sendType");// 发送类型

        String content = requestJson.getString("content"); // 发送内容

        String urlLink = requestJson.getString("urlLink");// 发送URL链接

        String senderPhoneNumber = requestJson.getString("senderPhoneNumber"); // 发送手机号码

        String memo = requestJson.getString("memo");// 备注

        String address = requestJson.getString("address"); // 发送地址

        String scope = requestJson.getString("scope");// 可见范围

        String reminder = requestJson.getString("remind");// 私有人

        String receiver = requestJson.getString("receiver");// 指定人员

        String reserve1 = requestJson.getString("reserve1");//

        String circleId = requestJson.getString("circleId");// 圈子ID

        if (!StringUtils.stringIsNotNull(circleId)) {
            circleId = "0";
        }
        if (!"0".equals(circleId)) {// 判断用户是否存在该标签
            try {
                boolean falg = circleInterface.exitUserForCircle(circleId, user_id);
                logger.debug("判断用户是否存在该标签状态，falg{}", falg);
                if (!falg) {
                    return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, "用户不存在该标签");
                }
            } catch (Exception e) {
                return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, "判断用户是否存在该标签异常");
            }
        }

        String corpId = requestJson.getString("pk_corp");// 企业ID

        final Map<String, Object> messageMap = new HashMap<String, Object>();
        messageMap.put("corpId", corpId);
        messageMap.put("receivers", receiver);
        messageMap.put("senderId", senderPhoneNumber);
        // 判断发送者手机号是否为空
        if (!StringUtils.stringIsNotNull(senderPhoneNumber)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_301, ParaUtil.WORKTEAM_SENDPHONEISNULL);
        }

        ClientUserVO clientUserVO = clientUserInterface.findById(senderPhoneNumber);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");
        if (memberInfoUtil.checkIsInBlackList(corpId, clientUserVO.getTelNum()))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1999, "");

        try {

            // 查询对应用户是否存在
            ClientUserVO cv = clientUserInterface.findById(user_id);
            logger.debug("查询对应用户信息，menberInfoVo{}", null == cv ? "" : JSON.toJSONString(cv));
            if (null == cv) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_304, ParaUtil.WORKTEAM_MENBERINFOISNULL);
            }
            // 获取消息表主键值
            long pkMessage = databaseInterface.generateId("im_wt_message", "pk_message");
            // 如果有文件信息存储文件信息
            JSONObject jsonFile = new WorkTeamFileUtil().uploadZIP(msg);

            // 判断发送内容是否为空
            if (jsonFile == null && !StringUtils.stringIsNotNull(content)) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_302, ParaUtil.WORKTEAM_SENDCONTENTISNULL);
            }
            logger.debug("文件信息：jsonFile:{}", null == jsonFile ? "" : jsonFile.toJSONString());
            logger.debug("敏感词，{}", ParamConfig.sensitivewords);
            if (StringUtils.stringIsNotNull(content) && "1".equals(ParamConfig.isFilterSensitivewords)) {
                SensitivewordFilter filter = SensitivewordFilter.getInstance();
                boolean hou = filter.replaceSensitiveWord(content, 1);
                if (hou) {
                    ErrorMsgVO errorMsgVO = sensitiveWordInterface.saveErrorMsgVO(getErrorMsgVO(user_id, circleId, content, "2"));
                    logger.debug("保存错误信息，errorMsgVO{}", JSON.toJSONString(errorMsgVO));
                    return ResponsePackUtil.getResponseStatus("-200", "您的此次发言含有敏感信息，请重新输入");
                }
            }
            String images = "";// 图片路径
            Object files = null;// 文件信息
            if (null != jsonFile && !jsonFile.isEmpty()) {
                images = (String) jsonFile.get("image");
                files = jsonFile.get("files");
            }
            // 保存说说消息
            boolean saveMsgFlag = saveWtMessageInfo(cv.getUserName(), urlLink, senderPhoneNumber, sendType, address, memo, scope, corpId, content, pkMessage, reserve1, images, circleId);
            if (false == saveMsgFlag) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_303, ParaUtil.WORKTEAM_MSGSAVE_ERROR);
            }
            // 保存文件信息
            if (null != files) {
                boolean flag = saveWtFileInfo(files, pkMessage);
                logger.debug("说说附件数据保存状态：flag:{}", flag);
                if (false == flag) {
                    ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_303, ParaUtil.WORKTEAM_MSGFILESAVE_ERROR);
                }
            }
            // 保存 私有，指定接收人信息
            saveReceiverOrReminderInfo(scope, receiver, reminder, pkMessage, corpId);

            messageMap.put("circleId", circleId);

            // 向同一企业推送
            new Thread() {
                public void run() {
                    noticeNewMessage(messageMap);
                }

            }.start();
            integralUtil.integralSigns(cv.getTelNum(), "401");
        } catch (Exception e) {
            logger.error("说说数据保存失败", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_303, ParaUtil.WORKTEAM_MSGSAVE_ERROR);
        }

        return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.WORKTEAM_MSGSAVE_SUCC);
    }

    /**
     * 发表个人说说
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String pcAppendWorkTeamMessage(String user_id, String requestBody, Object msg) {
        logger.debug("获取工作圈消息详情,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        
        String sendType = requestJson.getString("sendType");// 发送类型
        
        String content = requestJson.getString("content"); // 发送内容
        
        String urlLink = requestJson.getString("urlLink");// 发送URL链接
        
        String senderPhoneNumber = requestJson.getString("senderPhoneNumber"); // 发送手机号码
        
        String memo = requestJson.getString("memo");// 备注
        
        String address = requestJson.getString("address"); // 发送地址
        
        String scope = requestJson.getString("scope");// 可见范围
        
        String reminder = requestJson.getString("remind");// 私有人
        
        String receiver = requestJson.getString("receiver");// 指定人员
        
        String reserve1 = requestJson.getString("reserve1");//
        
        String circleId = requestJson.getString("circleId");// 圈子ID
        
        
        JSONArray jsonArray = requestJson.getJSONArray("imagesZip");
        String zipFileName=requestJson.getString("zipFileName");
        byte[] buffer =null;
        if(jsonArray!=null){
            Object[] imageszip= jsonArray.toArray();
            buffer = new byte[imageszip.length];
            for(int i=0;i<buffer.length;i++){
                buffer[i]=(byte) imageszip[i].hashCode();
            }
        }
        
        
        
        if (!StringUtils.stringIsNotNull(circleId)) {
            circleId = "0";
        }
        if (!"0".equals(circleId)) {// 判断用户是否存在该标签
            try {
                boolean falg = circleInterface.exitUserForCircle(circleId, user_id);
                logger.debug("判断用户是否存在该标签状态，falg{}", falg);
                if (!falg) {
                    return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, "用户不存在该标签");
                }
            } catch (Exception e) {
                return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, "判断用户是否存在该标签异常");
            }
        }
        
        String corpId = requestJson.getString("pk_corp");// 企业ID
        
        final Map<String, Object> messageMap = new HashMap<String, Object>();
        messageMap.put("corpId", corpId);
        messageMap.put("receivers", receiver);
        messageMap.put("senderId", senderPhoneNumber);
        // 判断发送者手机号是否为空
        if (!StringUtils.stringIsNotNull(senderPhoneNumber)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_301, ParaUtil.WORKTEAM_SENDPHONEISNULL);
        }
        
        ClientUserVO clientUserVO = clientUserInterface.findById(senderPhoneNumber);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");
        if (memberInfoUtil.checkIsInBlackList(corpId, clientUserVO.getTelNum()))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1999, "");
        
        try {
            
            // 查询对应用户是否存在
            ClientUserVO cv = clientUserInterface.findById(user_id);
            logger.debug("查询对应用户信息，menberInfoVo{}", null == cv ? "" : JSON.toJSONString(cv));
            if (null == cv) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_304, ParaUtil.WORKTEAM_MENBERINFOISNULL);
            }
            // 获取消息表主键值
            long pkMessage = databaseInterface.generateId("im_wt_message", "pk_message");
            // 如果有文件信息存储文件信息 
            JSONObject jsonFile =null;
            if(buffer!=null){
                jsonFile= new WorkTeamFileUtil().uploadZipByBytes(buffer, zipFileName);
            }
            
            // 判断发送内容是否为空
            if (jsonFile == null && !StringUtils.stringIsNotNull(content)) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_302, ParaUtil.WORKTEAM_SENDCONTENTISNULL);
            }
            logger.debug("文件信息：jsonFile:{}", null == jsonFile ? "" : jsonFile.toJSONString());
            logger.debug("敏感词，{}", ParamConfig.sensitivewords);
            if (StringUtils.stringIsNotNull(content) && "1".equals(ParamConfig.isFilterSensitivewords)) {
                SensitivewordFilter filter = SensitivewordFilter.getInstance();
                boolean hou = filter.replaceSensitiveWord(content, 1);
                if (hou) {
                    ErrorMsgVO errorMsgVO = sensitiveWordInterface.saveErrorMsgVO(getErrorMsgVO(user_id, circleId, content, "2"));
                    logger.debug("保存错误信息，errorMsgVO{}", JSON.toJSONString(errorMsgVO));
                    return ResponsePackUtil.getResponseStatus("-200", "您的此次发言含有敏感信息，请重新输入");
                }
            }
            String images = "";// 图片路径
            Object files = null;// 文件信息
            if (null != jsonFile && !jsonFile.isEmpty()) {
                images = (String) jsonFile.get("image");
                files = jsonFile.get("files");
            }
            // 保存说说消息
            boolean saveMsgFlag = saveWtMessageInfo(cv.getUserName(), urlLink, senderPhoneNumber, sendType, address, memo, scope, corpId, content, pkMessage, reserve1, images, circleId);
            if (false == saveMsgFlag) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_303, ParaUtil.WORKTEAM_MSGSAVE_ERROR);
            }
            // 保存文件信息
            if (null != files) {
                boolean flag = saveWtFileInfo(files, pkMessage);
                logger.debug("说说附件数据保存状态：flag:{}", flag);
                if (false == flag) {
                    ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_303, ParaUtil.WORKTEAM_MSGFILESAVE_ERROR);
                }
            }
            // 保存 私有，指定接收人信息
            saveReceiverOrReminderInfo(scope, receiver, reminder, pkMessage, corpId);
            
            messageMap.put("circleId", circleId);
            
            // 向同一企业推送
            new Thread() {
                public void run() {
                    noticeNewMessage(messageMap);
                }
                
            }.start();
            integralUtil.integralSigns(cv.getTelNum(), "401");
        } catch (Exception e) {
            logger.error("说说数据保存失败", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_303, ParaUtil.WORKTEAM_MSGSAVE_ERROR);
        }
        
        return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.WORKTEAM_MSGSAVE_SUCC);
    }
    /**
     * 封装错误消息
     * 
     * @param userId
     * @param circleId
     * @param content
     * @return
     */
    private ErrorMsgVO getErrorMsgVO(String userId, String circleId, String content, String type) {
        ErrorMsgVO errorMsgVO = new ErrorMsgVO();
        errorMsgVO.setContent(content);
        errorMsgVO.setFromUser(userId);
        errorMsgVO.setReceiveUser(circleId);
        errorMsgVO.setSendTime(new Date());
        errorMsgVO.setType(type);
        return errorMsgVO;
    }

    /**
     * 删除工作圈消息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String deleteWorkTeamMessage(String user_id, String requestBody) {
        logger.debug("删除工作圈消息,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String ids = requestJson.getString("ids");// 消息ID字符串

        if (!StringUtils.stringIsNotNull(ids)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MSGDEL_USERIDISNULL);
        }
        try {
            // 从数据库删除
            boolean res = workTeamInterface.deleteById(Long.parseLong(ids));
            logger.debug("删除工作圈消息返回状态，res{}", res);
            if (false == res) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MSGDEL_ERROR);
            }
            return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.WORKTEAM_MSGDEL_SUCC);
        } catch (Exception e) {
            logger.error("删除工作圈消息异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MSGDEL_ERROR);
        }

    }

    /**
     * 删除我的工作圈说说
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String deleteMyWorkTeamMessage(String user_id, String requestBody) {
        logger.debug("删除我的工作圈消息,requestBody:{},userKey:{}", requestBody, user_id);
        try {
            requestBody = AESUtil.decode(user_id, requestBody);
        } catch (Exception e) {
            logger.error("删除我的工作圈说说AES解密异常,user_id:{},requestBody:{},key:{}", user_id, requestBody, user_id, e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MSGDEL_ERROR);
        }
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String pk_message = requestJson.getString("pk_message");// 消息ID

        String userName = requestJson.getString("userName");// 用户名

        // String corpId = requestJson.getString("pk_corp");//企业ID

        if (!StringUtils.stringIsNotNull(pk_message) || !StringUtils.stringIsNotNull(userName)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.PARM_VALIDATION_ERROR);
        }
        try {

            // 根据消息ID查询工作圈消息
            WorkTeamMessageVo workTeamMessageVo = workTeamMessageInterface.findMessageById(Long.parseLong(pk_message));
            logger.debug("根据消息ID查询工作圈消息，workTeamMessageVo{}", workTeamMessageVo);
            if (null == workTeamMessageVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_503, ParaUtil.WORKTEAM_MSGISNULL);
            }
            // 判断是否是自己发表的说说
            if (!userName.equals(workTeamMessageVo.getSenderclientid() + "")) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_504, ParaUtil.WORKTEAM_MSGISNOTSENDER);
            }
            // 删除说说
            boolean res = workTeamInterface.deleteById(Long.parseLong(pk_message));
            logger.debug("删除我的个人说说返回状态，res{}", res);
            if (false == res) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MSGDEL_ERROR);
            }
            return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.WORKTEAM_MSGDEL_SUCC);

        } catch (Exception e) {
            logger.error("说说删除异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MSGDEL_ERROR);
        }
    }

    /**
     * 发表工作圈消息评论
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String appendWorkTeamReply(String user_id, String requestBody) {
        logger.debug("发表工作圈消息评论,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        // Map<String, Object> mapResult = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();

        String sendType = requestJson.getString("sendType");// 消息评论类型

        String content = requestJson.getString("content");// 消息评论内容

        String senderPhoneNumber = requestJson.getString("senderPhoneNumber");// 消息评论人ID

        String receiverPhoneNumber = requestJson.getString("receiverPhoneNumber"); // 被评论人ID

        String pk_message = requestJson.getString("pk_message");// 被评论消息ID
        if (!StringUtils.stringIsNotNull(pk_message) || !StringUtils.stringIsNotNull(sendType)) {
            logger.debug("参数值：pk_message{}，sendType{}", pk_message, sendType);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.PARM_VALIDATION_ERROR);
        }
        long pkMessage = -1;
        long longSendType = -1;
        try {
            pkMessage = Long.parseLong(pk_message);

            longSendType = Long.parseLong(sendType);
        } catch (NumberFormatException e1) {
            logger.error("类型转换异常", e1);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
        }
        try {
            if (StringUtils.stringIsNotNull(content) && "1".equals(ParamConfig.isFilterSensitivewords)) {
                SensitivewordFilter filter = SensitivewordFilter.getInstance();
                boolean hou = filter.replaceSensitiveWord(content, 1);
                if (hou) {
                    ErrorMsgVO errorMsgVO = sensitiveWordInterface.saveErrorMsgVO(getErrorMsgVO(user_id, receiverPhoneNumber, content, "3"));
                    logger.debug("保存错误信息，errorMsgVO{}", JSON.toJSONString(errorMsgVO));
                    return ResponsePackUtil.getResponseStatus("-200", "您的此次发言含有敏感信息，请重新输入");
                }
            }
            // 根据消息ID查询工作圈消息
            WorkTeamMessageVo workTeamMessageVo = workTeamMessageInterface.findMessageById(pkMessage);
            logger.debug("根据消息ID查询工作圈消息，workTeamMessageVo{}", null == workTeamMessageVo ? "" : JSON.toJSONString(workTeamMessageVo));
            if (null == workTeamMessageVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.WORKTEAM_MSGISNULL);
            }
            // 存储工作圈消息评论
            WorkTeamReplyVo workTeamReplyVo = new WorkTeamReplyVo();
            // 根据发送者ID查询发送者姓名
            workTeamReplyVo.setSender(getMenberNameByPhone(senderPhoneNumber));
            // 根据接收者ID查询接收者姓名
            if(null==receiverPhoneNumber||"".equals(receiverPhoneNumber))
                receiverPhoneNumber=workTeamMessageVo.getSenderclientid();
            
            workTeamReplyVo.setReceiver(getMenberNameByPhone(receiverPhoneNumber));
            workTeamReplyVo.setReceiverclientid(receiverPhoneNumber);
            workTeamReplyVo.setSenderclientid(senderPhoneNumber);
            workTeamReplyVo.setSendtime(new Date());
            workTeamReplyVo.setContent(content);
            workTeamReplyVo.setPkMessage(pkMessage);
            workTeamReplyVo.setSendtype(longSendType);
            workTeamReplyVo.setPkReply(databaseInterface.generateId("im_wt_reply", "pk_reply"));
            // 保存评论信息
            WorkTeamReplyVo reswtrvo = workTeamReplyInterface.save(workTeamReplyVo);
            logger.debug("该用户保存评论信息，reswtrvo{}", null == reswtrvo ? "" : JSON.toJSONString(reswtrvo));
            if (null == reswtrvo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_10001, ParaUtil.WORKTEAM_REPLY_ERROR);
            }
            // 推送
            noticeRelatedPeople(user_id, pk_message, workTeamMessageVo, reswtrvo, MQProvideUtil.WORK_TYPE_NOTICE, 1);
         
            jsonObject.put("pk_reply", reswtrvo.getPkReply());
            jsonObject.put("result", ParaUtil.SUCC_CODE);
            jsonObject.put("resultMsg", ParaUtil.WORKTEAM_REPLY_SUCC);
            // mapResult.put("jsonObject", jsonObject);
            ClientUserVO clientUserVO = clientUserInterface.findById(senderPhoneNumber);
            integralUtil.integralSigns(clientUserVO.getTelNum(), "402");
            logger.debug("评论返回结果，mapResult{}", jsonObject.toString());
            return jsonObject.toString();
        } catch (Exception e) {
            logger.error("评论出现异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_REPLY_ERROR);
        }
    }

    /**
     * 发表工作圈消息赞
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String appendWorkTeamPraise(String user_id, String requestBody) {
        logger.debug("发表工作圈消息赞,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            logger.debug("wdw pc00");
            
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String sendType = requestJson.getString("sendType");// 发送类型

        String pk_message = requestJson.getString("pk_message");// 消息ID

        String senderPhoneNumber = requestJson.getString("senderPhoneNumber");// 赞用户手机号码
        
        // 参数校验
        if (!StringUtils.stringIsNotNull(pk_message) || !StringUtils.stringIsNotNull(sendType) || !StringUtils.stringIsNotNull(senderPhoneNumber)) {
            logger.debug("参数值：pk_message{}，sendType{}，senderPhoneNumber{}", pk_message, sendType, senderPhoneNumber);
            logger.debug("wdw pc01");

            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.PARM_VALIDATION_ERROR);
        }
        try {
            long pkMessage = Long.parseLong(pk_message);
            long longSendType = Long.parseLong(sendType);
            // 查询说说是否存在
            WorkTeamMessageVo workTeamMessageVo = workTeamMessageInterface.findMessageById(pkMessage);
            logger.debug("根据消息ID查询工作圈消息，workTeamMessageVo{}", null == workTeamMessageVo ? "" : JSON.toJSONString(workTeamMessageVo));
            if (null == workTeamMessageVo) {
                logger.debug("wdw pc02");
            	
                return ResponsePackUtil.getResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.WORKTEAM_MSGISNULL);
            }
            // 查询赞信息
            // 查询条件
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("EQ_sendtype", sendType);
            map.put("EQ_pkMessage", pk_message);
            // 查询该消息是否被赞过
            List<WorkTeamReplyVo> listWtReply = workTeamReplyInterface.findWkReplyByCondition(map, null);
            logger.debug("查询该消息赞信息，listWtReply{}", null == listWtReply ? "" : JSON.toJSONString(listWtReply));
            // 获取该用户的姓名
            String memoName = getMenberNameByPhone(senderPhoneNumber);
            // 获取已赞人员ID
            String memo = senderPhoneNumber + "";
            logger.debug("查询该用户姓名信息，memoName{}", memoName);
            // 获取评论ID
            long pkReply = databaseInterface.generateId("im_wt_reply", "pk_reply");
            // 判断是否存在该用户赞信息
            // 没有赞记录就入库　
            WorkTeamReplyVo saveWtPriseFlag = null;
            if (null == listWtReply || listWtReply.isEmpty()) {
                // 保存赞信息
                saveWtPriseFlag = saveWtPriseInfo(senderPhoneNumber, memoName, pkMessage, longSendType, pkReply,workTeamMessageVo.getSenderclientid());
                
                if (null == saveWtPriseFlag) {
                    logger.debug("wdw pc03");
                	
                    return ResponsePackUtil.wTMsgPriseResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_PRISE_ERROR, memoName, memo);
                }
            } else {
                // 获取赞对象
                WorkTeamReplyVo workTeamReplyVo = listWtReply.get(0);
                // 获取已赞人员ID
                memo = workTeamReplyVo.getMemo();
                if (null != memo && memo.contains(senderPhoneNumber + "")) {
                    // 该用户已赞过
                    logger.debug("wdw pc04");
                	
                    return ResponsePackUtil.wTMsgPriseResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_PRISE_ERROR, memoName, memo);
                }
                memo = (workTeamReplyVo.getMemo() == null ? "" : workTeamReplyVo.getMemo() + ",") + senderPhoneNumber;
                memoName = (workTeamReplyVo.getMemoname() == null ? "" : workTeamReplyVo.getMemoname() + ",") + memoName;
                workTeamReplyVo.setMemo(memo);
                workTeamReplyVo.setMemoname(memoName);
                // 保存赞信息
                saveWtPriseFlag = workTeamReplyInterface.save(workTeamReplyVo);
                if (null == saveWtPriseFlag) {
                    logger.debug("wdw pc05");
                	
                    return ResponsePackUtil.wTMsgPriseResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_PRISE_ERROR, memoName, memo);
                }
            }
            // 推送
            noticeRelatedPeople(user_id, pk_message, workTeamMessageVo, saveWtPriseFlag, MQProvideUtil.WORK_TYPE_NOTICE, 2);
            ClientUserVO clientUserVO = clientUserInterface.findById(senderPhoneNumber);
            integralUtil.integralSigns(clientUserVO.getTelNum(), "403");
            return ResponsePackUtil.wTMsgPriseResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.WORKTEAM_PRISE_SUCC, memoName, memo);
        } catch (Exception e) {
            logger.error("赞出现异常", e);
            logger.debug("wdw pc06");
            
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_PRISE_ERROR);
        }

    }

    /**
     * 取消工作圈消息赞
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String cancelWorkTeamPraise(String user_id, String requestBody) {
        logger.debug("取消工作圈消息赞,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String sendType = requestJson.getString("sendType");// 发送类型

        String pk_message = requestJson.getString("pk_message");// 消息ID

        String senderPhoneNumber = requestJson.getString("senderPhoneNumber");// 赞用户手机号码
        // 参数校验
        if (!StringUtils.stringIsNotNull(pk_message) || !StringUtils.stringIsNotNull(sendType) || !StringUtils.stringIsNotNull(senderPhoneNumber)) {
            logger.debug("参数值：pk_message{}，sendType{}，senderPhoneNumber{}", pk_message, sendType, senderPhoneNumber);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.PARM_VALIDATION_ERROR);
        }
        try {
            long pkMessage = Long.parseLong(pk_message);
            // long longSendType = Long.parseLong(sendType);
            // 查询说说是否存在
            WorkTeamMessageVo workTeamMessageVo = workTeamMessageInterface.findMessageById(pkMessage);
            logger.debug("根据消息ID查询工作圈消息，workTeamMessageVo{}", null == workTeamMessageVo ? "" : JSON.toJSONString(workTeamMessageVo));
            if (null == workTeamMessageVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.WORKTEAM_MSGISNULL);
            }
            // 查询赞消息
            // 查询条件
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("EQ_sendtype", sendType);
            map.put("EQ_pkMessage", pk_message);
            // 查询该消息是否被赞过
            List<WorkTeamReplyVo> listWtReply = workTeamReplyInterface.findWkReplyByCondition(map, null);
            logger.debug("查询该消息赞信息，listWtReply{}", null == listWtReply ? "" : JSON.toJSONString(listWtReply));
            // 获取该用户的姓名
            String memoName = getMenberNameByPhone(senderPhoneNumber);
            // 获取已赞人员ID
            String memo = senderPhoneNumber + "";
            logger.debug("查询该用户姓名信息，memoName{}", memoName);
            // 判断是否存在该用户赞信息
            if (null == listWtReply) {
                return ResponsePackUtil.wTMsgPriseResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.WORKTEAM_MSGISNULL, memoName, memo);
            }
            // 已存在的赞信息对象
            WorkTeamReplyVo workTeamReplyVo = listWtReply.get(0);
            memo = StringUtils.getRemoveStr(workTeamReplyVo.getMemo(), senderPhoneNumber + "", "");
            memoName = StringUtils.getRemoveStr(workTeamReplyVo.getMemoname(), memoName, "");
            workTeamReplyVo.setMemo(memo);
            workTeamReplyVo.setMemoname(memoName);
            // 保存赞信息
            WorkTeamReplyVo savePriseFlag = workTeamReplyInterface.save(workTeamReplyVo);

            if (null == savePriseFlag) {
                return ResponsePackUtil.wTMsgPriseResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_CANCLEPRISE_ERROR, memoName, memo);
            }
            String afterStr = savePriseFlag.getMemo() == null ? "" : savePriseFlag.getMemo();
            String after[] = afterStr.split(",");
            if (after == null || after.length < 1)
                workTeamReplyInterface.deleteById(savePriseFlag.getPkReply(), pkMessage);

            // 推送
            noticeRelatedPeople(user_id, pk_message, workTeamMessageVo, savePriseFlag, MQProvideUtil.WORK_TYPE_NOTICE, 3);
            return ResponsePackUtil.wTMsgPriseResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.WORKTEAM_CANCLEPRISE_SUCC, memoName, memo);
        } catch (Exception e) {
            logger.error("取消赞出现异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_CANCLEPRISE_ERROR);
        }

    }
    
    /**
     * 取消工作圈回复
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String cancelWorkTeamReply(String user_id, String requestBody) {
        logger.debug("取消工作圈回复,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
          
          String pk_message = requestJson.getString("pk_message");// 消息ID
          
          String pkReply= requestJson.getString("pkReply");// 回复id
          
          String userId = requestJson.getString("userId");// 赞用户手机号码
        // 参数校验
        if (!StringUtils.stringIsNotNull(pk_message) || !StringUtils.stringIsNotNull(pkReply)) {
            logger.debug("参数值：pk_message{}，pkReply{}", pk_message, pkReply);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.PARM_VALIDATION_ERROR);
        }
        try {
            long pkMessage = Long.parseLong(pk_message);
            // 查询说说是否存在
            WorkTeamMessageVo workTeamMessageVo = workTeamMessageInterface.findMessageById(pkMessage);
            logger.debug("根据消息ID查询工作圈消息，workTeamMessageVo{}", null == workTeamMessageVo ? "" : JSON.toJSONString(workTeamMessageVo));
            if (null == workTeamMessageVo) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.WORKTEAM_MSGISNULL);
            }
            // 查询条件
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("EQ_sendtype", 1);
            map.put("EQ_pkMessage", pk_message);
            map.put("EQ_pkReply", pkReply);
            List<WorkTeamReplyVo> listWtReply = workTeamReplyInterface.findWkReplyByCondition(map, null);
            //防止用户所删除回复已经被删除
            if(null==listWtReply){
                return ResponsePackUtil.getResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.WORKTEAM_REPLYISNULL);
            }
            //防止用户删除他人回复
            if(!userId.equals(listWtReply.get(0).getSenderclientid())){
                return ResponsePackUtil.getResponseStatus(ParaUtil.GROUP_ERROR_CODE, ParaUtil.WORKTEAM_MSGISNOTREPLY);
            }
            logger.debug("查询该回复信息，listWtReply{}", null == listWtReply ? "" : JSON.toJSONString(listWtReply));
            //删除回复
            boolean deleteReplyFlag = workTeamReplyInterface.deleteById(Long.parseLong(pkReply), pkMessage);
            logger.debug("删除回复结果：{}",deleteReplyFlag);
            if (!deleteReplyFlag) {
                return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_CANCLEREPLY_ERROR);
            }
            // 推送
            noticeRelatedPeople(user_id, pk_message, workTeamMessageVo, listWtReply.get(0), MQProvideUtil.WORK_TYPE_NOTICE, 3);
            return ResponsePackUtil.getResponseStatus(ParaUtil.SUCC_CODE, ParaUtil.WORKTEAM_CANCLEREPLY_SUCC);
        } catch (Exception e) {
            logger.error("取消赞出现异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_CANCLEREPLY_ERROR);
        }

    }
    
    

    /**
     * 获取工作圈消息信息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String getWorkTeamMessageDetail(String user_id, String requestBody) {
        logger.debug("获取工作圈消息信息,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        JSONObject jsonObject = new JSONObject();
        // Map<String, Object> mapResult = new HashMap<String, Object>();

        String pk_message = requestJson.getString("pk_message"); // 消息ID

        String userName = requestJson.getString("userName");// 用户ID
        if (!StringUtils.stringIsNotNull(userName)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE_302, ParaUtil.WORKTEAM_USERNAME_ERROY);
        }
        if (!StringUtils.stringIsNotNull(pk_message)) {
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MSGDEL_USERIDISNULL);
        }
        long pkMessage = -1;
        try {
            pkMessage = Long.parseLong(pk_message);
        } catch (NumberFormatException e) {
            logger.error("类型转换异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.TPYECHANGE_ERROR);
        }

        try {
            // 根据消息Id查询说说详情包括附件信息 /
            WorkTeamVo workTeamVo = workTeamInterface.findMessageById(pkMessage);
            logger.debug("根据消息Id查询说说详情包括附件信息返回结果，workTeamVo{}", null == workTeamVo ? "" : JSON.toJSONString(workTeamVo));
            // 根据消息Id查询说说评论以及赞信息
            List<WorkTeamReplyVo> listWtReply = workTeamReplyInterface.findWkReplyByPkMessage(pkMessage);
            logger.debug("根据消息Id查询说说评论以及赞信息，listWtReply{}", null == listWtReply ? "" : JSON.toJSONString(listWtReply));

            jsonObject.put("result", ParaUtil.SUCC_CODE);
            jsonObject.put("resultMsg", ParaUtil.WORKTEAM_MSGINFO_SUCC);
            jsonObject.put("content", PackageReturnTypeUtils.packageWorkTeamMsgVO(workTeamVo));
            jsonObject.put("reply", PackageReturnTypeUtils.packageWorkTeamReplyList(listWtReply));
            // mapResult.put("jsonObject", jsonObject);
            logger.debug("消息详情返回结果，mapResult{}", jsonObject.toString());
            return jsonObject.toString();
        } catch (Exception e) {
            logger.error("获取工作圈消息信息异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.WORKTEAM_MSGINFO_ERROR);
        }

    }

    /**
     * 获取工作圈信息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getListCircle(String user_id, String requestBody) {
        logger.debug("获取圈子信息,user_id{},requestBody{}", user_id, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String pageSize = StringUtils.strIsNullAndGetValue(requestJson.getString("pageSize"), "10");// 获取分页大小,并判断是否为空并赋值

        String page = StringUtils.strIsNullAndGetValue(requestJson.getString("pageNow"), "1");// 获取当前页,并判断是否为空并赋值

        Map<String, Object> conditions = new HashMap<String, Object>();

        conditions.put("EQ_userId", user_id);
        List<CircleInfoVo> listCircleInfoVo = new ArrayList<CircleInfoVo>();
        Long total = 0L;
        try {
            Map<String, Object> map = circleInterface.findCircleInfoVoByPage(Integer.valueOf(page), Integer.valueOf(pageSize), conditions, null);
            logger.debug("获取圈子信息结果，map{}", JSON.toJSONString(map));
            if (null != map) {
                listCircleInfoVo = (List<CircleInfoVo>) map.get("content");
                total = (Long) map.get("total");
            }
        } catch (Exception e) {
            logger.error("获取圈子信息异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.GETCIRCLE_ERROR);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.GETCIRCLE_SUCC);
        jsonObject.put("total", total);
        jsonObject.put("content", listCircleInfoVo);
        return jsonObject.toString();
    }

    /**
     * 根据用户ID获取用户所有圈子的ID
     * 
     * @param user_id
     * @return
     * @throws Exception
     */
    public String getCircleIds(String user_id) throws Exception {
        logger.debug("获取圈子信息,user_id{}", user_id);
        List<CircleInfoVo> listCircleInfoVo = circleInterface.findByUserId(user_id);
        logger.debug("获取圈子信息结果，listCircleInfoVo{}", JSON.toJSONString(listCircleInfoVo));
        String circleIds = "";
        if (null == listCircleInfoVo || listCircleInfoVo.isEmpty()) {
            return circleIds;
        }
        for (CircleInfoVo circleInfoVo : listCircleInfoVo) {
            circleIds = circleIds + circleInfoVo.getCircleId() + ",";
        }
        logger.debug("获取用户拥有圈子ID，circleIds{}", circleIds);
        return circleIds.substring(0, circleIds.length());
    }
    
    
    
    /**
     * 工作圈功能分块请求
     * 
     * @param function_id
     * @param user_id
     * @param request_body
     * @param msg
     * @return
     */
    private String WorkTeamBusinessLayer(Channel channel, HttpRequest request, String function_id, String user_id, String request_body, Object msg) {
        String res = "";
        switch (function_id) {
            case FunctionIdConstant.getWorkTeamMsgList:// 获取工作圈消息列表
                res = getWorkTeamMsgList(user_id, request_body);
                break;
            case FunctionIdConstant.getSomeOneWorkTeamMsgList: // 获取工作圈个人说说消息列表
                res = getSomeOneWorkTeamMsgList(user_id, request_body);
                break;
            case FunctionIdConstant.appendWorkTeamMessage: // 发表工作圈消息
                res = appendWorkTeamMessage(user_id, request_body, msg);
                break;
            case FunctionIdConstant.PC_APPEND_WORK_TEAM: // 发表工作圈消息
                res = pcAppendWorkTeamMessage(user_id, request_body, msg);
                break;
            case FunctionIdConstant.deleteWorkTeamMessage:// 删除工作圈消息
                res = deleteWorkTeamMessage(user_id, request_body);
                break;
            case FunctionIdConstant.deleteMyWorkTeamMessage: // 删除我的工作圈消息
                res = deleteMyWorkTeamMessage(user_id, request_body);
                break;
            case FunctionIdConstant.appendWorkTeamReply:// 发表工作圈消息评论
                res = appendWorkTeamReply(user_id, request_body);
                break;
            case FunctionIdConstant.appendWorkTeamPraise:// 发表工作圈消息赞
                res = appendWorkTeamPraise(user_id, request_body);
                break;
            case FunctionIdConstant.cancelWorkTeamPraise:
                res = cancelWorkTeamPraise(user_id, request_body); // 取消工作圈消息赞
                break;
            case FunctionIdConstant.cancelWorkTeamReply:
                res = cancelWorkTeamReply(user_id, request_body); // 取消工作圈回复
                break;
            case FunctionIdConstant.getWorkTeamMessageDetail:// 获取工作圈消息信息
                res = getWorkTeamMessageDetail(user_id, request_body);
                break;
            case FunctionIdConstant.changeAvatar:// 修改头像
                res = changeAvatar(user_id, request_body);
                break;
            case FunctionIdConstant.GETRESERVEFIELD:// 获取自定义字段
                res = getReserveFieldInfo(user_id, request_body);
                break;
            case FunctionIdConstant.GETSYSTEMPARAM:// 获取系统参数
                res = getSystemParameterInfo(request_body);
                break;
            case FunctionIdConstant.DORESERPASSWORD:// “我”重置密码
                res = doResetPassword(user_id, request_body);
                break;
            case FunctionIdConstant.ADDRESSCORRECTION:// 通讯录纠错
                res = addressCorrection(user_id, request_body);
                break;
            case FunctionIdConstant.CLIENTLOGUPLOAD:// 客户端日志上传
                res = clientLogUpload(user_id, request_body, msg);
                break;
            case FunctionIdConstant.PCLOGUPLOAD:// pc端日志上传
                res = pcLogUpload(user_id, request_body, msg);
                break;
            case FunctionIdConstant.CLIENTOPERATION:// 客户端操作
                res = clientOperation(user_id, request_body, channel, request);
                break;
            case FunctionIdConstant.getListCirleInfo:// 客户端操作
                res = getListCircle(user_id, request_body);
                break;
            case FunctionIdConstant.FINDFESTIVAL:// 客户端获取节日欢迎图
                res = getListFestival(user_id,request_body);
                break;
            case FunctionIdConstant.PCSINGLECHAT:// 获取pc端单聊消息
                res = getSingleChat(user_id,request_body);
                break;
            case FunctionIdConstant.PCGROUPCHAT:// 获取pc端群聊个人消息
                res = getGroupChat(user_id,request_body);
                break;
            case FunctionIdConstant.getCorpList:// 分页获取企业列表
            	res = getCorpList(request_body);
            	break;
            case FunctionIdConstant.getDeptList:// 分页获取企业的二级部门列表
            	res = getDeptList(request_body);
            	break;
            case FunctionIdConstant.getSpecialUserList:// 获取特别关注列表
            	res = getSpecialUserList(request_body);
            	break;
            case FunctionIdConstant.addBanchSpecialUsers:// 批量新增特别关注
            	res = addBanchSpecialUsers(request_body);
            	break;
            case FunctionIdConstant.delSpecialUser:// 删除特别关注
            	res = delSpecialUser(request_body);
            	break;
            default:
                res = ResponsePackUtil.returnFaileInfo(); // 未知请求
        }
        return res;
    }

    /**
     * 获取消息列表
     * 
     * @param pageSize
     * @param condition
     * @param sortMap
     * @return List<Map<String, Object>>
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getWtMsgList(int pageSize, Map<String, Object> condition, Map<String, Boolean> sortMap) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>(); // 消息视图包含附件信息map集合
        List<WorkTeamVo> wtMsglist = new ArrayList<WorkTeamVo>();// 消息列表
        // 获取消息视图包含附件信息map集合
        resultMap = workTeamInterface.findMessageOfPage(1, pageSize, condition, sortMap);
        logger.debug("获取消息视图包含附件信息,resultMap:{}", null == resultMap ? "" : JSON.toJSONString(resultMap));
        // 判断获取消息视图包含附件信息map集合是否为空
        if (!StringUtils.mapSOIsNotNull(resultMap)) {
            throw new Exception("查询消息视图包含附件信息map集合为空");
        }
        wtMsglist = (List<WorkTeamVo>) resultMap.get("content");// 获取消息列表
        logger.debug("获取消息列表集合,wtReplylist:{}", null == wtMsglist ? "" : JSON.toJSONString(wtMsglist));
        return PackageReturnTypeUtils.packageWorkTeamMessageList(wtMsglist);
    }

    /**
     * 获取评论列表
     * 
     * @param wtMsglist
     * @param condition
     * @return List<Map<String, Object>>
     */
    private List<Map<String, Object>> getWtMsgReplyList(List<Map<String, Object>> wtMsglist) {
        List<Map<String, Object>> wtReplylistMap = new ArrayList<Map<String, Object>>();// 评论集合
        Map<String, Object> condition = new HashMap<String, Object>();// 查询条件
        Map<String, Object> map = StringUtils.getPkMessages(wtMsglist);// 获取评论Id字符串
        logger.debug("获取评论Id字符串,pks:{}", null == map ? "" : JSON.toJSONString(map));
        if (null != map && map.get("pks") != null) {
            condition.clear();// 清空查询条件从新赋值
            condition.put("IN_pkMessage", map.get("pks"));
            List<WorkTeamReplyVo> wtReplylist = workTeamReplyInterface.findWkReplyByCondition(condition, null); // 获取评论列表
            logger.debug("获取评论列表集合,wtReplylist:{}", null == wtReplylist ? "" : JSON.toJSONString(wtReplylist));
            wtReplylistMap = PackageReturnTypeUtils.packageWorkTeamReplyList(wtReplylist);
        }
        return wtReplylistMap;
    }

    
    /**
     * 获取欢迎图信息
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String getListFestival( String userId,String requestBody)  {
        logger.debug("获取欢迎图信息,user_id{},requestBody{}", userId, requestBody);
      //  JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        Map<String, Object> conditions = new HashMap<String, Object>();
        
        conditions.put("LTE_beginTime", new Date());
        conditions.put("GTE_endTime", new Date());
        List<FestivalVo> listFestivalVo = new ArrayList<FestivalVo>();
        try {
            listFestivalVo = festivalInterface.findFestivalByConditions(conditions);
            logger.debug("获取欢迎图信息结果，map{}", JSON.toJSONString(listFestivalVo));
        } catch (Exception e) {
            logger.error("获取欢迎图信息异常", e);
            return ResponsePackUtil.getResponseStatus(ParaUtil.ERROY_CODE, ParaUtil.GETCIRCLE_ERROR);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", ParaUtil.SUCC_CODE);
        jsonObject.put("resultMsg", ParaUtil.GETCIRCLE_SUCC);
        jsonObject.put("content", listFestivalVo);
        return jsonObject.toString();
    }
    
    /**
     * 单聊消息查询模块
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String getSingleChat( String userId,String requestBody)  {
        logger.debug("获取单聊信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String sendRoleId = requestJson.getString("sendRoleId");
        String memId = requestJson.getString("memId");
        String startKey = requestJson.getString("startKey");
        /** 校验参数 */
        if ("".equals(memId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        if ("".equals(sendRoleId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        try {
            Map<String, Object> map = imMessageInterface.getSingleMessageSendHistroy(memId, sendRoleId, startKey);
            return ResponsePackUtil.buildPack("0000", map);
        } catch (Exception e) {
            logger.error("单聊消息查询服务异常：e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4006, "");
        }
    }
    
    /**
     * 群聊消息查询模块
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getGroupChat( String userId,String requestBody) {
        logger.debug("获取群聊信息,user_id{},requestBody{}", userId, requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            return ResponsePackUtil.getWTMsgLisetResponseFaile(ParaUtil.ERROY_CODE, ParaUtil.JSONFORMAT_ERROR);
        }
        String groupId = requestJson.getString("groupId");
        String sendRoleId = requestJson.getString("sendRoleId");
        String startKey = requestJson.getString("startKey");
        /** 校验参数 */
        if ("".equals(groupId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        if ("".equals(sendRoleId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        try {
            Map<String, Object> map = imMessageInterface.getGroupMessageSendHistroy(groupId, sendRoleId, startKey);
            return ResponsePackUtil.buildPack("0000", map);
        } catch (Exception e) {
            logger.error("群聊消息查询服务异常：e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4007, "");
        }
    }
    
    
    /**
     * 保存文件信息
     * 
     * @param files
     * @param pkMessage
     */
    @SuppressWarnings("unchecked")
    private boolean saveWtFileInfo(Object files, long pkMessage) {
        if (null == files) {
            return false;
        }
        List<Map<String, Object>> filesList = (List<Map<String, Object>>) files;
        if (filesList.isEmpty()) {
            return false;
        }
        try {
            for (Map<String, Object> file : filesList) {
                WorkTeamFileVo workTeamFileVo = new WorkTeamFileVo();
                workTeamFileVo.setFilelength((Long) file.get("fileLength") + "");
                workTeamFileVo.setFilename((String) file.get("fileName"));
                workTeamFileVo.setFilepaths((String) file.get("filePaths"));
                workTeamFileVo.setPkMessage(pkMessage);
                workTeamFileInterface.save(workTeamFileVo);
            }
            return true;
        } catch (Exception e) {
            logger.error("工作圈附件保存异常", e);
            return false;
        }

    }

    /**
     * 保存消息信息
     * 
     * @param urlLink
     * @param senderPhoneNumber
     * @param sendType
     * @param address
     * @param memo
     * @param scope
     * @param corpId
     * @param content
     * @param pkMessage
     * @param reserve1
     * @param images
     * @return
     */
    private boolean saveWtMessageInfo(String menberName, String urlLink, String senderPhoneNumber, String sendType, String address, String memo, String scope, String corpId, String content,
            long pkMessage, String reserve1, String images, String circleId) {
        /** 说说消息对象 */
        WorkTeamMessageVo workTeamMessageVo = new WorkTeamMessageVo();
        try {
            workTeamMessageVo.setSender(menberName);
            workTeamMessageVo.setUrllink(urlLink);
            workTeamMessageVo.setSenderclientid(senderPhoneNumber);
            workTeamMessageVo.setSendtype(Long.parseLong(sendType));
            workTeamMessageVo.setAddress(address);
            workTeamMessageVo.setMemo(memo);
            workTeamMessageVo.setScope(Long.parseLong(scope));
            workTeamMessageVo.setPkCorp(corpId);
            workTeamMessageVo.setSendtime(new Date());
            workTeamMessageVo.setContent(content);
            workTeamMessageVo.setPkMessage(pkMessage);
            if ("3".equals(sendType)) {
                workTeamMessageVo.setReserve1(reserve1);
            }
            if (StringUtils.stringIsNotNull(images)) {
                workTeamMessageVo.setImage(images);
            }
            workTeamMessageVo.setReserve3(circleId);
            // 插入说说消息
            WorkTeamMessageVo reswtvo = workTeamMessageInterface.save(workTeamMessageVo);
            if (null == reswtvo) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            logger.error("工作圈消息保存异常", e);
            return false;
        }

    }

    /**
     * 保存工作圈私有人或者指定接收人信息
     * 
     * @param scope
     * @param receiver
     * @param reminder
     * @param pkMessage
     * @param corpId
     */
    private void saveReceiverOrReminderInfo(String scope, String receiver, String reminder, long pkMessage, String corpId) {
        if (!"2".equals(scope)) {
            return;
        }// 私有，指定接收人
         // 插入指定接收人信息
        if (StringUtils.stringIsNotNull(receiver)) {
            String[] arrReceiver = receiver.split(";");
            for (String phoneNum : arrReceiver) {
                try {
                    WorkTeamUserVo workTeamUserVo = new WorkTeamUserVo();
                    workTeamUserVo.setPkMessage(pkMessage);
                    workTeamUserVo.setReceiver(phoneNum);
                    workTeamUserVo.setPkCorp(corpId);
                    workTeamUserInterface.save(workTeamUserVo);
                } catch (Exception e) {
                    logger.error("指定接收人数据保存异常", e);
                }
                // 如果插入信息出错 是否回滚
            }
        }
        // 插入指定私有人信息
        if (StringUtils.stringIsNotNull(reminder)) {
            String[] arrReminder = reminder.split(";");
            for (String phoneNum : arrReminder) {
                try {
                    WorkTeamUserVo workTeamUserVo = new WorkTeamUserVo();
                    workTeamUserVo.setPkMessage(pkMessage);
                    workTeamUserVo.setReminder(phoneNum);
                    workTeamUserVo.setPkCorp(corpId);
                    workTeamUserInterface.save(workTeamUserVo);
                } catch (Exception e) {
                    logger.error("指定私有人数据保存异常", e);
                }
                // 如果插入信息出错 是否回滚
            }
        }

    }

    /**
     * 保存赞信息
     * 
     * @param senderPhoneNumber
     * @param memoName
     * @param pkMessage
     * @param longSendType
     * @param pkReply
     * @return
     */
    private WorkTeamReplyVo saveWtPriseInfo(String senderPhoneNumber, String memoName, long pkMessage, long longSendType, long pkReply,String receiverPhoneNumber) {
        WorkTeamReplyVo wtReplyVo = new WorkTeamReplyVo();
        wtReplyVo.setMemo(senderPhoneNumber);
        wtReplyVo.setMemoname(memoName);
        wtReplyVo.setPkMessage(pkMessage);
        wtReplyVo.setSendtype(longSendType);
        // 获取主键
        wtReplyVo.setPkReply(pkReply);
        wtReplyVo.setReceiverclientid(receiverPhoneNumber);
        // 保存赞信息 \
        WorkTeamReplyVo reswtrvo = workTeamReplyInterface.save(wtReplyVo);
        logger.debug("该用户赞信息，reswtrvo{}", null == reswtrvo ? "" : JSON.toJSONString(reswtrvo));
        return reswtrvo;
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
        MemberInfoVO menberInfo = memberInfoUtil.findMemberInfoById(phone);
        // MemberInfoVO menberInfo = memberInfoInterface.findById(phone);
        logger.debug("查询对应用户信息，senderMenberInfo", null == menberInfo ? "" : JSON.toJSONString(menberInfo));
        if (null != menberInfo) {
            return (String) menberInfo.getMemberName();
        }
        return "";
    }

    /**
     * 根据用户ID查询用户企业ID
     * 
     * @param userName
     * @return
     */
    private String getMenberCorpIdById(String userName) {
        if (!StringUtils.stringIsNotNull(userName)) {
            return "";
        }
        MemberInfoVO menberInfoVo = null;
        try {
            // menberInfoVo = memberInfoInterface.findById(userName);
            menberInfoVo = memberInfoUtil.findMemberInfoById(userName);
            logger.debug("查询对应用户信息，senderMenberInfo", null == menberInfoVo ? "" : JSON.toJSONString(menberInfoVo));
        } catch (Exception e) {
            logger.error("查询对应用户信息异常", e);
            return "";
        }
        return menberInfoVo.getCorpId() + "";
    }

    /**
     * 修改用户头像
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String changeAvatar(String user_id, String requestBody) {
        logger.debug("修改用户头像,requestBody:{},userKey:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        /** 用户id */
        String userId = requestJson.getString("userId");

        /** 返回信息 */
        JSONObject jsonRes = new JSONObject();

        if (null == userId || "".equals(userId)) {
            jsonRes.put("result", 301);
            jsonRes.put("resultMsg", "用户名不能为空");
            return jsonRes.toJSONString();
        }

        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("修改用户头像,requestBody:{},userKey:{},clientUserVO:{}", requestBody, user_id, JSON.toJSONString(clientUserVO));
        if (null == clientUserVO) {
            jsonRes.put("result", 300);
            jsonRes.put("resultMsg", "该用户不存在");
            return jsonRes.toJSONString();
        }

        try {
            String filePath = FileUploadUtil.uploadFile(msg);
            logger.debug("修改用户头像,requestBody:{},userKey:{},filePath:{}", requestBody, user_id, filePath);
            clientUserVO.setAvatar(filePath);
            jsonRes.put("avatar", filePath);
            clientUserInterface.saveUser(clientUserVO);

        } catch (Exception e) {
            logger.error("修改用户头像,requestBody:{},userKey:{}", requestBody, user_id, e);
            jsonRes.put("result", 400);
            jsonRes.put("resultMsg", "头像更换失败");
            return jsonRes.toJSONString();
        }
        jsonRes.put("result", 200);
        jsonRes.put("resultMsg", "头像更换成功");
        integralUtil.integralSigns(clientUserVO.getTelNum(), "201");

        logger.debug("修改用户头像,jsonRes:", jsonRes.toJSONString());
        return jsonRes.toJSONString();
    }

    /**
     * 获取自定义字段
     * 
     * @param user_id
     * @param requestBody
     * @return
     */
    public String getReserveFieldInfo(String user_id, String requestBody) {
        logger.debug("获取自定义字段,requestBody:{},userId:{}", requestBody, user_id);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        /** 用户id */
        String userId = requestJson.getString("userId");

        /** 返回信息 */
        JSONObject jsonRes = new JSONObject();

        if (null == userId || "".equals(userId)) {
            jsonRes.put("result", 301);
            jsonRes.put("resultMsg", "用户名不能为空");
            return jsonRes.toJSONString();
        }

        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("获取自定义字段,requestBody:{},userKey:{},clientUserVO:{}", requestBody, user_id, JSON.toJSONString(clientUserVO));
        if (null == clientUserVO) {
            jsonRes.put("result", 300);
            jsonRes.put("resultMsg", "该用户不存在");
            return jsonRes.toJSONString();
        }
        // MemberInfoVO memberInfoVO = memberInfoInterface.findById(userId);
        MemberInfoVO memberInfoVO = memberInfoUtil.findMemberInfoById(userId);
        try {
            List<ReserveFieldVO> reserveFieldVOs = reserveFieldInterface.findByCorpId(memberInfoVO.getCorpId());
            logger.debug("获取自定义字段,requestBody:{},userKey:{},reserveFieldVOs:{}", requestBody, user_id, JSON.toJSONString(reserveFieldVOs));

            jsonRes.put("reserveField", reserveFieldVOs);
        } catch (Exception e) {
            logger.error("获取自定义字段,requestBody:{},userKey:{}", requestBody, user_id, e);
        }
        jsonRes.put("result", 200);
        jsonRes.put("resultMsg", "获取自定义字段成功");
        logger.debug("获取自定义字段,jsonRes:", jsonRes.toJSONString());
        return jsonRes.toJSONString();
    }

    /**
     * 获取系统参数信息
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String getSystemParameterInfo(String requestBody) {
        logger.debug("激活用户校验,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpId = requestJson.getString("corpId");

        List<Map<String, Object>> resultParams = new ArrayList<Map<String, Object>>();
        int result = 200;
        String resultMsg = "获取成功";
        if (StringUtils.checkParamNull(corpId)) {
            List<Map<String, Object>> allParam = systemParamInterface.getAllSysParamter();

            for (Map<String, Object> paramMap : allParam) {
                Object object = paramMap.get("parameterValue");
                if (null != object) {
                    String pv = (String) object;
                    if (StringUtils.checkParamNull(pv)) {
                        Map<String, Object> resultParam = new HashMap<String, Object>();
                        resultParam.put("parameterCode", (String) paramMap.get("parameterCode"));
                        resultParam.put("parameterValue", pv);
                        resultParam.put("parameterReserve", paramMap.get("times") == null ? 0 : paramMap.get("times"));
                        if (pv.equals("2")) {
                            List<Map<String, Object>> sysParams = systemParamInterface.getSysCorpParamter(corpId, (String) paramMap.get("parameterCode"));
                            if (null != sysParams && !sysParams.isEmpty()) {
                                resultParam.put("isOpen", "1");
                                resultParam.put("parameterReserve", sysParams.get(0).get("times") == null ? 0 : sysParams.get(0).get("times"));
                            } else {
                                resultParam.put("isOpen", "0");
                            }
                        }
                        resultParams.add(resultParam);
                    }

                }
            }
        } else {
            result = 300;
            resultMsg = "请求参数corpId为空";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resultPosition", resultParams);
        jsonObject.put("result", result);
        jsonObject.put("resultMsg", resultMsg);
        return jsonObject.toString();
    }

    /**
     * 重置密码密码
     * 
     * @param user_id
     * @param request_body
     */
    private String doResetPassword(String user_id, String request_body) {
        logger.debug("重置密码,requestBody:{},userId:{}", request_body, user_id);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String ciphertext = requestJson.getString("ciphertext");// 如果客户端传入的密码是密文传入
        String oldPassword = requestJson.getString("oldPassword");
        String newPassword = requestJson.getString("newPassword");
        String username = requestJson.getString("username");

        // 原密码为空
        if (org.springframework.util.StringUtils.isEmpty(oldPassword))
            return ResponsePackUtil.getResponseStatus("302", "原密码为空");

        // 新密码为空
        if (org.springframework.util.StringUtils.isEmpty(newPassword))
            return ResponsePackUtil.getResponseStatus("303", "新密码为空");

        // 用户id为空
        if (org.springframework.util.StringUtils.isEmpty(username))
            return ResponsePackUtil.getResponseStatus("304", "用户id为空");

        if (org.springframework.util.StringUtils.isEmpty(ciphertext)) {
            oldPassword = SystemUtils.getMD5Str(oldPassword);
            newPassword = SystemUtils.getMD5Str(newPassword);
        }

        ClientUserVO cv = null;
        try {
            cv = clientUserInterface.findById(username);
            if (cv == null)
                return ResponsePackUtil.getResponseStatus("305", "用户不存在");

            // 原密码不匹配
            if (!oldPassword.equals(cv.getPwd()))
                return ResponsePackUtil.getResponseStatus("301", "原密码错误");

            List<ClientUserVO> list = clientUserInterface.findByTelNum(cv.getTelNum());
            for (ClientUserVO clientUserVO : list) {
                clientUserVO.setPwd(newPassword);
                cv = clientUserInterface.saveUser(clientUserVO);
                // 修改失败
                if (null == cv)
                    return ResponsePackUtil.getResponseStatus("306", "重置密码失败");
            }
        } catch (Exception e) {
            logger.error("重置密码异常{}", e);
            return ResponsePackUtil.getResponseStatus("307", "重置密码异常");

        }

        return ResponsePackUtil.getResponseStatus("200", "重置密码成功");
    }

    /**
     * 通讯录纠错
     * 
     * @param user_id
     * @param request_body
     * @return
     */
    private String addressCorrection(String user_id, String request_body) {
        logger.debug("通讯录纠错,requestBody:{},userId:{}", request_body, user_id);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String sendTelNum = requestJson.getString("sendTelNum");
            String updateTelNum = requestJson.getString("updateTelNum");
            String sendContent = requestJson.getString("sendContent");

            logger.debug("通讯录纠错,sendTelNum{},updateTelNum{},sendContent{}", sendTelNum, updateTelNum, sendContent);

            if (org.springframework.util.StringUtils.isEmpty(sendTelNum))
                return ResponsePackUtil.getResponseStatus("301", "发起纠错人员手机号码为空");

            if (org.springframework.util.StringUtils.isEmpty(updateTelNum))
                return ResponsePackUtil.getResponseStatus("304", "被纠错人员手机号码为空");

            if (org.springframework.util.StringUtils.isEmpty(sendContent))
                return ResponsePackUtil.getResponseStatus("307", "纠错信息为空");

            ClientUserVO sendCV = clientUserInterface.findById(sendTelNum);// 发起人
            logger.debug("纠错发起人信息{}", sendCV);

            if (sendCV == null)
                return ResponsePackUtil.getResponseStatus("302", "不存在此发起纠错人员");

            String sendName = sendCV.getUserName();

            DepartMentVO sendDv = departMentInterface.findById(sendCV.getDeptId());
            logger.debug("纠错发起人部门信息{}", sendDv);

            if (sendDv == null)
                return ResponsePackUtil.getResponseStatus("303", "发起纠错人员无对应部门");

            String sendPart = sendDv.getPartName();

            // ClientUserVO updateCV = clientUserInterface.findById(updateTelNum);// 被纠错人
            MemberInfoVO updateCV = memberInfoUtil.findMemberInfoById(updateTelNum);
            logger.debug("被纠错人信息{}", updateCV);

            if (updateCV == null)
                return ResponsePackUtil.getResponseStatus("305", "不存在此被纠错人员");

            String updateName = updateCV.getMemberName();
            DepartMentVO updateDV = departMentInterface.findById(updateCV.getDeptId());
            logger.debug("被纠错人部门信息{}", updateDV);

            if (updateDV == null)
                return ResponsePackUtil.getResponseStatus("306", "被纠错人员无对应部门");

            String updatePart = updateDV.getPartName();
            String pk_corp = updateCV.getCorpId();

            InfoFeedbackVo infoVO = new InfoFeedbackVo();
            infoVO.setId(UUID.randomUUID().toString());
            infoVO.setSendName(sendName);
            infoVO.setSendPart(sendPart);
            infoVO.setSendTime(new Date());
            infoVO.setStatus("0");
            infoVO.setUpdateContent(sendContent);
            infoVO.setUpdateName(updateName);
            infoVO.setUpdatePart(updatePart);
            infoVO.setUpdateMemid(updateTelNum);
            infoVO.setUpdateTelnum(updateCV.getTelNum());
            infoVO.setCorpId(pk_corp);
            infoVO = infoFeedbackInterface.saveInfoFeedback(infoVO);
            logger.debug("纠错信息保存{}", infoVO);

            if (null == infoVO)
                return ResponsePackUtil.getResponseStatus("308", "添加该条信息失败");
        } catch (Exception e) {
            logger.error("通讯录纠错异常{}", e);
            return ResponsePackUtil.getResponseStatus("309", "通讯录纠错异常");

        }

        return ResponsePackUtil.getResponseStatus("200", "纠错信息添加成功");
    }

    /**
     * 客户端日志
     * 
     * @param user_id
     * @param request_body
     * @return
     */
    public String clientLogUpload(String user_id, String request_body, Object msg) {
        logger.debug("客户端上传日志zip,requestBody:{},userId:{}", request_body, user_id);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        // String corpId = requestJson.getString("corpId");
        // String md5Str = requestJson.getString("md5Str");
        String telNum = requestJson.getString("telNum");
        String fileType = requestJson.getString("fileType");

        String fileName = telNum + "-" + user_id;
        JSONObject resJson = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject = FileUploadUtil.uploadFileToLocal(msg);
            logger.debug("客户端上传日志zip,jsonObject:{}", jsonObject);

            if (jsonObject == null) {
                resJson.put("result", 200);
                resJson.put("uploadStatus", false);
                return resJson.toString();
            }
            String path = jsonObject.getString("path");
            String name = jsonObject.getString("name");
            logger.debug("客户端上传日志到本地结果{}", path, name);
            String filePath = FastDFSUtil.uploadFile(path + File.separator + name);

            LogManagerVo lv = new LogManagerVo();
            lv.setFilename(fileName);
            lv.setFilepath(filePath);
            lv.setLogid(UUID.randomUUID().toString());
            lv.setPhonenumber(telNum);
            lv.setUserid(user_id);
            lv.setUpdatetime(new Date());
            lv.setAction(fileType);
            lv = logManagerInterface.saveLogManager(lv);

            logger.debug("客户端上传日志到本地，保存信息{}", JSON.toJSONString(lv));

            clearFiles(path);

        } catch (Exception e) {
            logger.error("客户端上传日志异常{}", e);
            resJson.put("result", 200);
            resJson.put("uploadStatus", false);
            return resJson.toString();
        }
        resJson.put("result", 200);
        resJson.put("uploadStatus", true);
        return resJson.toString();
    }

    
    
    /**
     * pc端日志
     * 
     * @param user_id
     * @param request_body
     * @return
     */
    public String pcLogUpload(String user_id, String request_body, Object msg) {
        logger.debug("客户端上传日志zip,requestBody:{},userId:{}", request_body, user_id);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String telNum = requestJson.getString("telNum");
        String fileType = requestJson.getString("fileType");

        String fileName = telNum + "-" + user_id;
        JSONObject resJson = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
           // jsonObject = FileUploadUtil.uploadFileToLocal(msg);
         //   String   str=requestJson.getString("file");
            
           Object[] files= requestJson.getJSONArray("file").toArray();
           byte[] buffer = new byte[files.length];
           for(int i=0;i<buffer.length;i++){
               buffer[i]=(byte) files[i].hashCode();
           }
            String filePath = FastDFSUtil.uploadFile(buffer, "zip");
            logger.debug("pc端上传日志zip,jsonObject:{}", jsonObject);

            LogManagerVo lv = new LogManagerVo();
            lv.setFilename(fileName);
            lv.setFilepath(filePath);
            lv.setLogid(UUID.randomUUID().toString());
            lv.setPhonenumber(telNum);
            lv.setUserid(user_id);
            lv.setUpdatetime(new Date());
            lv.setAction(fileType);
            lv = logManagerInterface.saveLogManager(lv);

            logger.debug("pc上传日志到本地，保存信息{}", JSON.toJSONString(lv));

        } catch (Exception e) {
            logger.error("客户端上传日志异常{}", e);
            resJson.put("result", 200);
            resJson.put("uploadStatus", false);
            return resJson.toString();
        }
        resJson.put("result", 200);
        resJson.put("uploadStatus", true);
        return resJson.toString();
    }
   
    /**
     * 客户端操作上报
     * 
     * @param user_id
     * @param request_body
     * @return
     */
    public String clientOperation(String user_id, String request_body, Channel channel, HttpRequest request) {
        logger.debug("客户端操作上报,requestBody:{},userId:{}", request_body, user_id);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        JSONObject json = new JSONObject();
        try {
            String telNum = requestJson.getString("telNum");
            String module = requestJson.getString("module");
            String operation = requestJson.getString("operation");
            String remark = requestJson.getString("remark");
            String squareType = requestJson.getString("isSuccess");

            // 若为企业应用则为corp，若为系统则为system，若为我的客户经理则为customer，未知为空

            List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
            if (null == clientUserVOs || clientUserVOs.isEmpty()) {
                json.put("result", 300);
                json.put("resultMsg", "添加失败");
                return json.toJSONString();
            }

            String operationCode = "";
            if (module.equals("通讯录")) {
                operationCode = "C0012003";
            } else if (module.equals("客户端版本") || module.equals("通讯录客户端版本")) {
                operationCode = "C0012002";
            } else if (module.equals("服务号")) {
                if (null == squareType || "".equals(squareType) || (!squareType.equals("corp") && !squareType.equals("system") && !squareType.equals("customer"))
                        || remark.equals("e6ab3432-3d3a-422c-b987-bd18841c6b4e") || remark.equals("c5efeb54-df30-44f5-ac60-17ce6050109a")) {
                    SquareVo squareVo = squareInterface.findSquareById(remark);
                    if (null != squareVo) {
                        String name = squareVo.getName();
                        if (name.equals("我的客户经理")) {
                            squareType = "customer";
                        } else if (name.equals("冲浪新闻")) {
                            squareType = "surf";
                        } else {
                            int isSystemApp = squareVo.getIsSystemApp();
                            if (isSystemApp == 1)
                                squareType = "system";
                            if (isSystemApp == 2)
                                squareType = "corp";
                        }
                    }
                }
                if (null == squareType || "".equals(squareType)) {
                    json.put("result", 300);
                    json.put("resultMsg", "添加失败");
                    return json.toJSONString();
                }
                if (operation.equals("1")) {// 进入服务号
                    if (squareType.equals("customer"))
                        operationCode = "C0019003";
                    else if (squareType.equals("surf"))
                        operationCode = "C0019031";
                    else if (squareType.equals("system"))
                        operationCode = "C0019001";
                    else
                        operationCode = "C0019002";
                } else if (operation.equals("2")) {// 回复信息
                    if (squareType.equals("customer"))
                        operationCode = "C0019009";
                    else if (squareType.equals("system"))
                        operationCode = "C0019007";
                    else
                        operationCode = "C0019008";
                } else if (operation.equals("3")) {// 点击菜单
                    if (squareType.equals("customer"))
                        operationCode = "C0019006";
                    else if (squareType.equals("system"))
                        operationCode = "C0019004";
                    else
                        operationCode = "C0019005";
                } else if (operation.equals("4")) {// 点击图文
                    if (squareType.equals("customer"))
                        operationCode = "C0019019";
                    else if (squareType.equals("surf"))
                        operationCode = "C0019032";
                    else if (squareType.equals("system"))
                        operationCode = "C0019017";
                    else
                        operationCode = "C0019018";
                }
            } else if (module.equals("APK")) {// 点击apk
                if (null == squareType || "".equals(squareType) || (!squareType.equals("corp") && !squareType.equals("system") && !squareType.equals("customer"))) {
                    SquareVo squareVo = squareInterface.findSquareById(remark);
                    if (null != squareVo) {
                        int isSystemApp = squareVo.getIsSystemApp();
                        if (isSystemApp == 1)
                            squareType = "system";
                        if (isSystemApp == 2)
                            squareType = "corp";
                    }
                }
                if (null == squareType || "".equals(squareType)) {
                    json.put("result", 300);
                    json.put("resultMsg", "添加失败");
                    return json.toJSONString();
                }
                if (squareType.equals("system"))
                    operationCode = "C0019015";
                else
                    operationCode = "C0019016";
            } else if (module.equals("H5")) {// 进入H5
                SquareVo squareVo = squareInterface.findSquareById(remark);
                if (null != squareVo) {
                    int isSystemApp = squareVo.getIsSystemApp();
                    if (isSystemApp == 1)
                        squareType = "system";
                    if (isSystemApp == 2)
                        squareType = "corp";
                    if (squareVo.getId().equals(ParamConfig.approval_id))
                        squareType = "approval";
                }
                if (null == squareType || "".equals(squareType)) {
                    json.put("result", 300);
                    json.put("resultMsg", "添加失败");
                    return json.toJSONString();
                }
                if (squareType.equals("system"))
                    operationCode = "C0019010";
                else if (squareType.equals("approval"))
                    operationCode = "C0019035";
                else
                    operationCode = "C0019011";
            } else if (module.equals("V特权")) {
                if (operation.equals("1")) {
                    operationCode = "C0019013";
                } else {
                    operationCode = "C0019014";
                }
            } else if (module.equalsIgnoreCase("IPA")) {
                if (null == squareType || "".equals(squareType) || (!squareType.equals("corp") && !squareType.equals("system") && !squareType.equals("customer"))) {
                    SquareVo squareVo = squareInterface.findSquareById(remark);
                    if (null != squareVo) {
                        int isSystemApp = squareVo.getIsSystemApp();
                        if (isSystemApp == 1)
                            squareType = "system";
                        if (isSystemApp == 2)
                            squareType = "corp";
                    }
                }
                if (null == squareType || "".equals(squareType)) {
                    json.put("result", 300);
                    json.put("resultMsg", "添加失败");
                    return json.toJSONString();
                }
                if (squareType.equals("system"))
                    operationCode = "C0019021";
                else
                    operationCode = "C0019022";
            } else {
                operationCode = "C0018003";
            }
            for (ClientUserVO clientUserVO2 : clientUserVOs) {
                operationLogService.saveClientOperationLog(channel, request, clientUserVO2.getUserId(), clientUserVO2.getTelNum(), operationCode, "", remark, "0000");
            }

        } catch (Exception ex) {
            json.put("result", 400);
            json.put("resultMsg", "添加失败");
            return json.toJSONString();
        }
        json.put("result", 200);
        json.put("resultMsg", "添加成功");
        return json.toJSONString();
    }

    /**
     * 新消息提醒
     * 
     * @param messageMap
     */
    public void noticeNewMessage(Map<String, Object> messageMap) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        WorkCircleAction wca = new WorkCircleAction();
        WorkCircleReply wcr = new WorkCircleReply();

        RedisAction ra = new RedisAction();
        ra.setHead(MQProvideUtil.WORK_HEAD);
        ra.setSource(MQProvideUtil.SOURCE);
        ra.setCreateTime(sdf.format(new Date()));

        wca.setType(1);
        wca.setContent(JSON.toJSONString(wcr));

        ra.setMessage(wca);
        // 入redis
        long msgId = -1;
        try {
            msgId = actionRecordUtil.save(ra);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 入MQ
        wca.setMsg_id(msgId);
        // 创建人id
        String creater = messageMap.get("senderId").toString();
        // 指定接收人
        String receivers = messageMap.get("receivers") == null ? "" : messageMap.get("receivers").toString();
        if (!com.alibaba.dubbo.common.utils.StringUtils.isEmpty(receivers)) {
            String[] arrReceiver = receivers.split(";");
            for (int i = 0; i < arrReceiver.length; i++) {
                // wca.setTelNum(arrReceiver[i]);
                wca.setTo_role_id(arrReceiver[i]);
                RocketMqUtil.send(RocketMqUtil.WorkCircleQueue, JSON.toJSONString(wca));
            }
        }
        // 对全体人员推送
        else {
            String circleId = messageMap.get("circleId").toString();
            if (com.alibaba.dubbo.common.utils.StringUtils.isEmpty(circleId) || "0".equals(circleId)) {
                String corpId = messageMap.get("corpId").toString();
                // 查询该企业所有的激活人员
                List<ClientUserVO> list = clientUserInterface.findByCorpId(corpId);
                if (null != list && list.size() > 0) {
                    for (ClientUserVO cv : list) {
                        String toUserId = cv.getUserId();
                        if (!creater.equals(toUserId)) {
                            // wca.setTelNum(toUserId);
                            wca.setTo_role_id(toUserId);
                            RocketMqUtil.send(RocketMqUtil.WorkCircleQueue, JSON.toJSONString(wca));
                        }
                    }
                }
            } else {
                // 获取圈子成员
                try {
                    List<String> listMemId = circleInterface.findMemIdByCircle(circleId);
                    for (String memId : listMemId) {
                        if (!creater.equals(memId)) {
                            wca.setTo_role_id(memId);
                            RocketMqUtil.send(RocketMqUtil.WorkCircleQueue, JSON.toJSONString(wca));
                        }
                    }
                } catch (Exception e) {
                    logger.error("获取MEMId异常");
                    return;
                }
            }
        }
    }

    /**
     * 通知与本条说说相关的人员 ，相关的定义为（发布人，评论过的，赞过的）
     * 
     * @param messageId 说说id
     * @param createPerson 说说创建人手机号
     * @param sendPerson 操作人手机号
     * @param operationType 操作类型 (1新说说，2提醒)
     * @param replyId 说说评论id (评论操作才需要，其余传"")
     * @param replyedMember 被评论人手机号 (评论操作才需要，其余传"")
     * @param replyContent 评论内容 (评论操作才需要，其余传"")
     * @param replyTime 评论时间 (评论操作才需要，其余传"")
     */
    public void noticeRelatedPeople(final String senderId, final String messageId, final WorkTeamMessageVo workTeamMessageVo, final WorkTeamReplyVo replyVO, final int operationType, final int type) {
        new Thread() {
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String sendPersonName = replyVO.getSender() == null ? "" : replyVO.getSender();// 回复人姓名
                String sendPersonCell = senderId;// 回复人id
                String receiverPersonName = replyVO.getReceiver() == null ? "" : replyVO.getReceiver();// 被回复人姓名
                String receiverPersonCell = replyVO.getReceiverclientid() == null ? "" : replyVO.getReceiverclientid();// 被回复人id
                String replyContent = replyVO.getContent() == null ? "" : replyVO.getContent();// 回复内容
                long replyId = replyVO.getPkReply();// 回复id
                String sendTime = replyVO.getSendtime() == null ? sdf.format(new Date()) : sdf.format(replyVO.getSendtime());// 回复时间
                long times = 0;
                try {
                    times = sdf.parse(sendTime).getTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String content = workTeamMessageVo.getContent() == null ? "" : workTeamMessageVo.getContent();// 说说内容
                long sendType = workTeamMessageVo.getSendtype();// 说说类型 1:文本2:图片 4:图文 其中2、4需要传filePath
                String filePath = workTeamMessageVo.getImage() == null ? "" : workTeamMessageVo.getImage().split(",")[0];// 说说图片
                String createPerson = workTeamMessageVo.getSenderclientid() == null ? "" : workTeamMessageVo.getSenderclientid();// 说说发表人id

                List<WorkTeamReplyVo> replyList = workTeamReplyInterface.findWkReplyByPkMessage(Long.parseLong(messageId));
                if (replyList == null || replyList.size() == 0) {
                    return;
                }
                // 获取相关人员
                String replyTels = "";// 回复人员手机号 , 分割
                // String replyNames = "";// 回复人员姓名 ,分割
                String praiseTels = "";// 赞人员手机号 ,分割
                String praiseNames = "";// 赞人员姓名 ,分割
                for (WorkTeamReplyVo replyMap : replyList) {
                    long sendTypes = replyMap.getSendtype();
                    // 评论
                    if (1 == sendTypes) {
                        String userTel = replyMap.getSenderclientid() == null ? "" : replyMap.getSenderclientid();
                        // String userName = replyMap.getSender() == null ? "" : replyMap.getSender();
                        replyTels += userTel + ",";
                        // replyNames += userName + ",";
                    }
                    // 赞
                    else if (2 == sendTypes) {
                        praiseTels = replyMap.getMemo() == null ? "" : StringUtils.moveSplit(replyMap.getMemo(), ",");
                        praiseNames = replyMap.getMemoname() == null ? "" : StringUtils.moveSplit(replyMap.getMemoname(), ",");
                    }
                }

                // 所有相关人员手机号
                String allTels = createPerson + "," + replyTels + praiseTels;
                String[] allTelsArr = allTels.split(",");
                List<String> allTelLists = Arrays.asList(allTelsArr);
                List<String> allTelList = new ArrayList<String>();
                allTelList.addAll(allTelLists);
                // 去重
                if (allTelList == null || allTelList.size() == 0)
                    return;
                // 根据类型推送
                // 回复
                WorkCircleAction wca = new WorkCircleAction();
                WorkCircleReply wcr = new WorkCircleReply();

                RedisAction ra = new RedisAction();
                ra.setHead(MQProvideUtil.WORK_HEAD);
                ra.setSource(MQProvideUtil.SOURCE);
                ra.setCreateTime(sdf.format(new Date()));

                wcr.setContent(content);
                /** 如果是链接，将reserve1放入content */
                if (workTeamMessageVo.getSendtype() == 3)
                    wcr.setContent(workTeamMessageVo.getReserve1());
                wcr.setFilePath(filePath);
                wcr.setMemberCells(praiseTels);
                wcr.setMemberNames(praiseNames);
                wcr.setMessageId(messageId);
                wcr.setReceiverPersonCell(receiverPersonCell);
                wcr.setReceiverPersonName(receiverPersonName);
                wcr.setReplyContent(replyContent);
                wcr.setReplyId(replyId + "");
                wcr.setSendPersonCell(sendPersonCell);
                wcr.setSendPersonName(sendPersonName);
                wcr.setSendTime(times + "");
                wcr.setSendType((int) sendType);
                wcr.setType(type);

                wca.setType(operationType);
                wca.setContent(JSON.toJSONString(wcr));

                ra.setMessage(wca);
                try {
                    // 入redis
                    long msgId = actionRecordUtil.save(ra);
                    // 入MQ
                    wca.setMsg_id(msgId);
                    List<String> hasPush = new ArrayList<String>();
                    for (String toTel : allTelList) {
                        if (!hasPush.contains(toTel) && !sendPersonCell.equals(toTel)) {
                            wca.setTo_role_id(toTel);
                            RocketMqUtil.send(RocketMqUtil.WorkCircleQueue, JSON.toJSONString(wca));
                            hasPush.add(toTel);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    public String getCorpList(String request){

    	logger.debug("获取企业列表消息,requestBody:{}", request);
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Object> model2 = new HashMap<String, Object>();
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        Map<String, Object> conditions = new HashMap<String, Object>();
        try {
            JSONObject requestJson = StringUtils.strisJsonStr(request);// 将参数字符串转换为JSON对象
            int pageIndex = requestJson.getIntValue("pageIndex");
            int pageSize = requestJson.getIntValue("pageSize");

            // 过滤掉V网通云平台
            conditions.put("NE_corpName", "V网通云平台");
            // 过滤掉来源渠道为村务通、校讯通和互联网的企业
            conditions.put("IN_fromchannel", "1,6");

            sortMap.put("corpArea", true);
            sortMap.put("corpId", true);
            logger.debug("根据区域编码分页查询企业,pageIndex{},pageSize{},conditions{},sortMap{}", pageIndex, pageSize, JSON.toJSON(conditions), JSON.toJSON(sortMap));
            model = corpInterface.findAllByPage(pageIndex, pageSize, conditions, sortMap);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (model == null) {
                model2.put("items", null);
                model2.put("total", 0);
                return JSON.toJSONString(model2);
            }

            @SuppressWarnings("unchecked")
			List<CorpVO> corpList = (List<CorpVO>) model.get("content");
            Map<String, Object> corpMap = null;
            if (corpList != null && !corpList.isEmpty()) {
                try {
                    for (CorpVO corpVO : corpList) {
                        corpMap = new HashMap<String, Object>();
                        corpMap.put("corpId", corpVO.getCorpId());
                        corpMap.put("corpName", corpVO.getCorpName());
//                        corpMap.put("newCorpName", corpVO.getCorpName());
//                        corpMap.put("corpPersonname", corpVO.getCorpPersonname());
//                        corpMap.put("corpMobilephone", corpVO.getCorpMobilephone());
//                        corpMap.put("corpIndustry", corpVO.getCorpIndustry());
//                        corpMap.put("bossCorpNum", corpVO.getBossCorpNum());
//                        corpMap.put("bossCorpNumC", corpVO.getBossCorpNum_C());
                        list.add(corpMap);
                    }
                    model2.put("items", list);
                    model2.put("total", model.get("total"));
                    
                } catch (Exception e) {
                	logger.error("根据区域编码分页查询企业", e);
                	 e.printStackTrace();
                }
            }else{
            	 model2.put("items", null);
                 model2.put("total", 0);
            }
            
        } catch (Exception e) {
            logger.error("获取企业列表消息异常", e);
            e.printStackTrace();
        }
        return JSON.toJSONString(model2);
    }
   
    /**
     * 分页获取企业二级部门
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
	public String getDeptList(String request){
    	logger.debug("获取企业二级部门列表消息,requestBody:{}", request);
    	Map<String, Object> model = new HashMap<String, Object>();
    	Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
    	sortMap.put("sort", true);
    	Map<String, Object> conditions = new HashMap<String, Object>();
    	try {
    		JSONObject requestJson = StringUtils.strisJsonStr(request);// 将参数字符串转换为JSON对象
    		int pageIndex = requestJson.getIntValue("pageIndex");
    		int pageSize = requestJson.getIntValue("pageSize");
    		String corpId = requestJson.getString("corpId");
    		
    		conditions.put("EQ_corpId", corpId);
            conditions.put("EQ_parentDeptNum", 1L);
    		List<DepartMentVO> list = departMentInterface.findAllEnterprise(conditions, sortMap);
    		if (CollectionUtils.isEmpty(list)) {
    			return ResponsePackUtil.getResponseStatus("-1119", "无对应根部门");
			}
    		conditions.clear();
    		conditions.put("EQ_corpId", corpId);
            conditions.put("EQ_parentDeptNum", list.get(0).getDeptId());
            Map<String, Object> page = departMentInterface.findAllByPage(pageIndex, pageSize, conditions, sortMap);
            if (null != page) {
                list = (List<DepartMentVO>) page.get("content");
                int total = Integer.parseInt(page.get("total").toString());
                model.put("items", list);
                model.put("total", total);// 数据总数
            } else {
                model.put("items", "");
                model.put("total", 0);// 数据总数
            }
    	} catch (Exception e) {
    		logger.error("获取企业二级部门列表消息异常", e);
    		e.printStackTrace();
    	}
    	return JSON.toJSONString(model);
    }
    
    /**
     * 获取特别关注人员列表
     * @param request
     * @return
     */
    public String getSpecialUserList(String request){
    	logger.debug("获取特别关注人员列表,requestBody:{}", request);
    	Map<String, Object> model = new HashMap<String, Object>();
//    	Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
//    	sortMap.put("sort", true);
//    	Map<String, Object> conditions = new HashMap<String, Object>();
    	try {
    		JSONObject requestJson = StringUtils.strisJsonStr(request);// 将参数字符串转换为JSON对象
    		String userId = requestJson.getString("memId");
    		List<SpecialUserInfoVo> list = specialUserInfoInterface.queryUserIds(userId);
    		if (CollectionUtils.isNotEmpty(list)) {
				model.put("item", list);
			}
    	} catch (Exception e) {
    		logger.error("获取特别关注人员列表异常", e);
    		e.printStackTrace();
    	}
    	return JSON.toJSONString(model);
    }
    
    /**
     * 批量新增特别关注人员
     * @param request
     * @return
     */
    public String addBanchSpecialUsers(String request){
    	logger.debug("批量新增特别关注人员,requestBody:{}", request);
    	try {
    		JSONObject requestJson = StringUtils.strisJsonStr(request);// 将参数字符串转换为JSON对象
    		String userId = requestJson.getString("memId");
    		JSONArray arry = requestJson.getJSONArray("specialUserList");
    		List<SpecialUserInfoVo> specialUserInfoVoList = new ArrayList<>();
    		for(int i=0;i<arry.size();i++){
    			SpecialUserInfoVo vo =new SpecialUserInfoVo();
    			vo.setId(UUID.randomUUID().toString().replaceAll("-", ""));
    			vo.setUserId(userId);
    			vo.setSpecialUserId(arry.getJSONObject(i).getString("specialUserId"));
    			specialUserInfoVoList.add(vo);
    			
			}
    		specialUserInfoInterface.save(specialUserInfoVoList);
    	} catch (Exception e) {
    		logger.error("批量新增特别关注人员异常", e);
    		e.printStackTrace();
    	}
    	return ResponsePackUtil.getResponseStatus("200", "批量新增成功");
    }
    
    /**
     * 删除特别关注人员
     * @param request
     * @return
     */
    public String delSpecialUser(String request){
    	logger.debug("删除特别关注人员,requestBody:{}", request);
    	try {
    		JSONObject requestJson = StringUtils.strisJsonStr(request);// 将参数字符串转换为JSON对象
    		String id = requestJson.getString("id");
    		boolean flag = specialUserInfoInterface.delete(id);
    		if (!flag) {
				return ResponsePackUtil.getResponseStatus("-1120", "删除特别关注人失败");
			}
    	} catch (Exception e) {
    		logger.error("删除特别关注人员异常", e);
    		e.printStackTrace();
    	}
    	return ResponsePackUtil.getResponseStatus("200", "删除特别关注人成功");
    }
    
    

    // 删除文件和目录
    private void clearFiles(String workspaceRootPath) {
        File file = new File(workspaceRootPath);
        if (file.exists()) {
            deleteFile(file);
        }
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
        }
        file.delete();
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


}
