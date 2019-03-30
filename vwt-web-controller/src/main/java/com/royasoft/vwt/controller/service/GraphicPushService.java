/************************************************
 * Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.AESUtil;
import com.royasoft.vwt.controller.util.BaseConstant;
import com.royasoft.vwt.controller.util.PropertiesUtils;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.StringUtils;
import com.royasoft.vwt.controller.util.upload.FastDFSUtil;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.dictionary.api.vo.DictionaryVo;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.core.tools.net.HttpUtil;
import com.royasoft.vwt.soa.business.materialRole.api.interfaces.MemberInfoInterfaces;
import com.royasoft.vwt.soa.business.materialRole.api.interfaces.RoleInterface;
import com.royasoft.vwt.soa.business.materialRole.api.vo.DeptAndMemberVO;
import com.royasoft.vwt.soa.business.materialRole.api.vo.GroupRole;
import com.royasoft.vwt.soa.business.materialRole.api.vo.GroupRoleVO;
import com.royasoft.vwt.soa.business.materialRole.api.vo.MemberInfoPageVO;
import com.royasoft.vwt.soa.business.materialRole.api.vo.MemberInfoVOs;
import com.royasoft.vwt.soa.business.materialRole.api.vo.Role;
import com.royasoft.vwt.soa.business.materialRole.api.vo.RoleVO;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareInterface;
import com.royasoft.vwt.soa.graphicpush.api.interfaces.GraphicPushInfoInterface;
import com.royasoft.vwt.soa.graphicpush.api.interfaces.GraphicSourceInterface;
import com.royasoft.vwt.soa.graphicpush.api.vo.GraphicSourceVo;
import com.royasoft.vwt.soa.graphicpush.api.vo.PushRequestVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.customer.api.interfaces.CustomerInterface;
import com.royasoft.vwt.soa.uic.customer.api.vo.CustomerVo;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 图文推送业务处理类
 *
 * @Author:MB
 * @Since:2015年8月26日
 */
@Scope("prototype")
@Service
public class GraphicPushService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(GraphicPushService.class);
    

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private CorpInterface corpInterface;

    @Autowired
    private MemberInfoInterfaces memberInfoInterfaces;
    
    @Autowired
    private MemberInfoInterface memberInfoInterface;

    @Autowired
    private GraphicPushInfoInterface graphicPushInfoInterface;

    @Autowired
    private CustomerInterface customerInterface;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private DictionaryInterface dictionaryInterface;
    
    @Autowired
    private SquareInterface squareInterface;

    @Autowired
    private DepartMentInterface departMentInterface;
    
    @Autowired
    private RoleInterface roleInterface;
    
     @Autowired
    private GraphicSourceInterface graphicSourceInterface;
    
    
     @Autowired
    private ZkUtil zkUtil;

    private Map<String, String> roleMap = new HashMap<String, String>() {
        /**
         * 
         */
        private static final long serialVersionUID = -396930875141644641L;
        {
            put("1", "系统管理员");
            put("2", "平台管理员");
            put("3", "企业管理员");
            put("4", "省公司管理员");
            put("5", "地市公司管理员");
            put("6", "区县管理员");
            put("7", "客户经理");
            put("8", "省直管理员");
        }
    };

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.materialCenter_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String tel_number = queue_packet.getTel_number();
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    /***************************** 业务逻辑处理 *********************************************/
                    String res = "";// 响应结果
                    if (function_id == null || function_id.length() <= 0 || request_body == null || request_body.length() <= 0) {
                        ResponsePackUtil.CalibrationParametersFailure(channel, "图文推送业务请求参数校验失败！");
                    } else {
                        // 素材中心具体业务分层跳转
                        res = GraphicMaterialBusinessLayer(channel, request, function_id, user_id, request_body, msg);
                    }
                    ResponsePackUtil.cagHttpResponse(channel, res);
                    String responseStatus = ResponsePackUtil.getResCode(res);
                    if (null != responseStatus && !StringUtils.isEmpty(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                    // 响应成功
                }
            } catch (Exception e) {
                logger.error("图文推送业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
                // channel.close();
            }
        }
    }

    /**
     * 图文推送功能分块请求
     * 
     * @param function_id
     * @param user_id
     * @param request_body
     * @param msg
     * @return
     */
    private String GraphicMaterialBusinessLayer(Channel channel, HttpRequest request, String function_id, String user_id, String request_body, Object msg) {
        String res = "";
        switch (function_id) {
            case FunctionIdConstant.FILEUPLOAD:// 上传文件
                res = uploadFile(request_body);
                break;
            case FunctionIdConstant.GETGRAPHICPUSHLIST:
                res = getGraphicPushList(request_body);
                break;
            case FunctionIdConstant.EXPORTGRAPHICPUSHLIST:
                res = exportGraphicPushList(request_body);
                break;
            case FunctionIdConstant.GRAPHICPUSHAGAIN:
                res = graphicPushAgain(request_body);
                break;
            case FunctionIdConstant.GETGRAPHICPUSHSERVICENOLIST:
                res = getGraphicPushServicenoList(request_body);
                break;
            case FunctionIdConstant.GETGRAPHICPUSHCORPLIST:
                res = getGraphicPushCorpList(request_body);
                break;
            case FunctionIdConstant.INSERTGRAPHICPUSHINFO:
                res = insertGraphicPushInfo(request_body);
                break;
            case FunctionIdConstant.GETROLEBYSESSIONID:
                res = getRoleBySessionId(request_body);
                break;
            case FunctionIdConstant.GETGRAPHICPUSHDETAIL:
                res = getGraphicPushDetail(request_body);
                break;
            case FunctionIdConstant.INSERTCORPGRAPHICPUSHINFO:
                res = insertCorpGraphicPushInfo(request_body);
                break;
            case FunctionIdConstant.CANCELCORPGRAPHICPUSHINFO:
                res = cancelCorpGraphicPushInfo(request_body);
                break;
            case FunctionIdConstant.LISTCORPGRAPHICPUSHINFO:
                res = listCorpGraphicPushInfo(request_body);
                break;
            case FunctionIdConstant.GRAPHICPUSHTEST:
                res = graphicPushTest(request_body);
                break;
            case FunctionIdConstant.GETCLIENTUSERBYDEPART:
                res = getClientUserByDepart(request_body);
                break;
            case FunctionIdConstant.GRAPHICPUSHWECHAT:
                res = insertGraphicPushInfoToWeChat(request_body);
                break;
             // 角色-获取左侧群组-角色列表（树）
            case FunctionIdConstant.ROLE__LIST:
            	res = getRoleList(request_body);
                break;
            // 角色-角色组-新增
            case FunctionIdConstant.ROLE_GROUP_ADD:
            	res = groupRoleAdd(request_body);
                break;
            // 角色-角色组-修改
            case FunctionIdConstant.ROLE_GROUP_UPDATE:
            	res = groupRoleUpdate(request_body);
                break;
            // 角色-角色组-删除
            case FunctionIdConstant.ROLE_GROUP_DELETE:
            	res = groupRoleDelete(request_body);
                break;
            // 角色-角色组-详情
            case FunctionIdConstant.ROLE_GROUP_DETAIL:
            	res = groupRoleDetail(request_body);
                break;
             // 角色-人员列表
            case FunctionIdConstant.ROLE_MEMBER_LIST:
            	res = memberList(request_body);
                break;
             // 角色-人员列表
            case FunctionIdConstant.MEMBER_LIST:
            	res = memberListNoPage(request_body);
                break;
            // 角色-新增
            case FunctionIdConstant.ROLE_ADD:
            	res = roleAdd(request_body);
                break;
            // 角色-修改
            case FunctionIdConstant.ROLE_UPDATE:
            	res = roleUpdate(request_body);
                break;
            // 角色-删除
            case FunctionIdConstant.ROLE_DELETE:
            	res = roleDelete(request_body);
                break;
            // 角色-详情
            case FunctionIdConstant.ROLE_DETAIL:
            	res = roleDetail(request_body);
                break;
            // 角色-角色列表
            case FunctionIdConstant.ROLE_ROLE_LIST:
            	res = roleList(request_body);
                break;
            // 角色-增加人员
            case FunctionIdConstant.ROLE_MEMBER_ADD:
            	res = memberAdd(request_body);
                break;
            // 角色-批量删除人员
            case FunctionIdConstant.ROLE_MEMBER_BATCH_DELETE:
            	res = memberBatchDelete(request_body);
                break;
            // 角色-新增修改时获取的群组列表
            case FunctionIdConstant.ROLE_GROUP_FOR_EDIT_ROLE:
            	res = roleEditGroupList(request_body);
                break;
             // 获取部门
            case FunctionIdConstant.ADDRESS_DEPART:
            	res = getDeparts(request_body);
                break;
            // 获取人员
            case FunctionIdConstant.ADDRESS_MEMBER:
            	res = getMembers(request_body);
                break;
            // 根据关键字搜索人员列表
            case FunctionIdConstant.ADDRESS_MEMBER_SEARCH:
            	res = searchMembers(request_body);
                break;
            // 根据部门id获取下级部门以及人员
            case FunctionIdConstant.ADDRESS_GET_DEPART_AND_MEMBERS_BYDEPTID:
            	res = getDeptAndMemByDeptId(request_body);
                break;
            default:
                res = ResponsePackUtil.returnFaileInfo(); // 未知请求
        }
        return res;
    }

    /**
     * 上传文件
     * 
     * @param requestBody
     * @return
     */
    public String uploadFile(String requestBody) {
        logger.debug("上传文件,requestBody:{}", requestBody);
        JSONObject requestJson = StringUtils.strisJsonStr(requestBody);// 将参数字符串转换为JSON对象
        JSONObject json = new JSONObject();// 返回参数JSON串
        if (null == requestJson) {
            logger.debug("JSON格式异常,requestBody{}", requestBody);
            json.put("result", 400);
            json.put("resultMsg", "上传文件失败");
            return json.toJSONString();
        }
        String filePath;
        try {
            String file = requestJson.getString("data");
            byte[] fileByte = Base64.decodeBase64(file);
            filePath = FastDFSUtil.uploadFile(fileByte, "jpg");
            if (filePath != null && !StringUtils.isEmpty(filePath)) {
                json.put("filePath", filePath.replace("\\", "/"));
                logger.debug("上传文件,requestBody:{},filePath:{}", requestBody, filePath);
            }
        } catch (Exception e) {
            logger.error("上传文件异常,requestBody:{}", requestBody);
            json.put("result", 400);
            json.put("resultMsg", "上传文件失败");
            return json.toJSONString();
        }
        json.put("result", 200);
        json.put("resultMsg", "上传文件成功");
        logger.debug("上传文件,json:{}", json.toJSONString());
        return json.toString();
    }

    /**
     * 获取图文推送列表接口
     * 
     * @return
     */
    public String getGraphicPushList(String requestBody) {
        logger.debug("获取图文推送列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String userId = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            userId = sessionJson.getString("userId");
            logger.debug("获取图文推送列表获取操作用户信息,userId:{}", userId);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        String name = requestJson.getString("name");// 活动名称模糊查询
        String page = requestJson.getString("page");// 页数
        String row = requestJson.getString("row");// 行数
        String status = requestJson.getString("status");// 推送状态
                                                        // 1未提交、2未审核、3审核中、4不通过、5待推送、6已推送、7推送失败、8推送中
        if ("0".equals(status))
            status = null;
        int pageIndex = 1;
        int pageSize = 10;
        try {
            if (!StringUtils.isEmpty(page))
                pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        }
        try {
            if (!StringUtils.isEmpty(row))
                pageSize = Integer.parseInt(row);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        }
        try {
            logger.debug("获取图文推送列表,name:{},status:{},pageIndex:{},pageSize:{},userId:{}", name, status, pageIndex, pageSize, userId);
            Object o = graphicPushInfoInterface.findGraphicPushInfoOfPage(pageIndex, pageSize, Integer.parseInt(userId), name, status);
            logger.debug("获取图文推送列表查询调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1303, "");
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return JSON.toJSONString(o, ser);
        } catch (Exception e) {
            logger.error("获取图文推送列表查询调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 图文推送列表导出接口
     * 
     * @return
     */
    public String exportGraphicPushList(String requestBody) {
        logger.debug("图文推送列表导出,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String userId = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            userId = sessionJson.getString("userId");
            logger.debug("图文推送列表导出获取操作用户信息,userId:{}", userId);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        String name = requestJson.getString("name");// 活动名称模糊查询
        String status = requestJson.getString("status");// 推送状态
                                                        // 1未提交、2未审核、3审核中、4不通过、5待推送、6已推送、7推送失败、8推送中
        if ("0".equals(status))
            status = null;
        try {
            logger.debug("图文推送列表导出,name:{},status:{},userId:{}", name, status, userId);
            Object o = graphicPushInfoInterface.getGraphicPushInfoExcel(Integer.parseInt(userId), name, status);
            logger.debug("图文推送列表导出调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1304, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("图文推送列表导出调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 图文推送(推送失败)重新推送接口
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String graphicPushAgain(String requestBody) {
        logger.debug("图文推送(推送失败)重新推送,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String id = requestJson.getString("id");// 图文推送id
        String type = requestJson.getString("type");// 图文推送id
        String pushTime = requestJson.getString("pushTime");// 图文推送计划推送时间
        String excelPath = requestJson.getString("excelPath");// excel推送文件路径
        if (!"6".equals(type) && !"7".equals(type))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        try {
            date = sdf.parse(pushTime);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        }
        if (!date.after(new Date()))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1312, "");
        if (StringUtils.isEmpty(pushTime))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        if ("6".equals(type) && (StringUtils.isEmpty(excelPath))) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        } else if ("6".equals(type)) {
            String fileType = excelPath.substring(excelPath.lastIndexOf(".") + 1);
            if (!"xls".equals(fileType) && !"xlsx".equals(fileType) && !"XLS".equals(fileType) && !"XLSX".equals(fileType))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
            if (excelPath.indexOf("/group") < 0)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
            excelPath = excelPath.substring(excelPath.indexOf("/group"));
        }
        try {
            logger.debug("图文推送(推送失败)重新推送,id:{},date:{},excelPath:{}", id, date, excelPath);
            Object o = graphicPushInfoInterface.pushAgain(id, date, excelPath);
            logger.debug("图文推送(推送失败)重新推送调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1305, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("图文推送(推送失败)重新推送请求服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 图文推送查询应用列表
     * 
     * @return
     */
    public String getGraphicPushServicenoList(String requestBody) {
        logger.debug("图文推送查询应用列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String userId = "";
        String roleId = "";
        String corpId = "";
        String userCityArea = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            roleId = sessionJson.getString("roleId");
            userId = sessionJson.getString("userId");
            corpId = sessionJson.getString("corpId");
            userCityArea = sessionJson.getString("userCityArea");
            logger.debug("图文推送查询应用列表获取操作用户信息,userId:{},roleId:{},corpId:{},userCityArea:{}", userId, roleId, userCityArea);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        String name = requestJson.getString("name");// 应用名称模糊查询
        String type = requestJson.getString("type");// 应用类型 apk:1;HTML5:2;服务号:3;ipa:4 不选传0
        String page = requestJson.getString("page");// 页数
        String row = requestJson.getString("row");// 行数
        int pageIndex = 1;
        int pageSize = 10;
        try {
            if (!StringUtils.isEmpty(page))
                pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        }
        try {
            if (!StringUtils.isEmpty(row))
                pageSize = Integer.parseInt(row);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        }
        try {
            logger.debug("图文推送查询应用列表,name:{},type:{},pageIndex:{},pageSize:{},roleId:{},corpId:{},userCityArea:{},userId:{}", name, type, pageIndex, pageSize,
                    roleId, corpId, userCityArea, userId);
            Object o = graphicPushInfoInterface.getSquareList(pageIndex, pageSize, Integer.parseInt(type), name, Integer.parseInt(roleId), corpId, userCityArea,
                    userId);
            logger.debug("图文推送查询应用列表调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1306, "");
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return JSON.toJSONString(o, ser);
        } catch (Exception e) {
            logger.error("图文推送查询应用列表调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 图文推送客户经理查询企业列表接口
     * 
     * @return
     */
    public String getGraphicPushCorpList(String requestBody) {
        logger.debug("图文推送客户经理查询企业列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String telNum = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            telNum = sessionJson.getString("telNum");
            logger.debug("图文推送客户经理查询企业列表获取操作用户信息,telNum:{}", telNum);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        try {
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_telNum", telNum);
            List<CustomerVo> list = customerInterface.findCustomerByCondition(conditions, null);
            logger.debug("根据手机号查询客户经理调取服务返回结果,list:{}", JSON.toJSONString(list));
            String customerId = list.get(0).getId();
            logger.debug("图文推送客户经理查询企业列表,customerId:{},", customerId);
            Object o = graphicPushInfoInterface.getCorpListByCustomer(customerId);
            logger.debug("图文推送客户经理查询企业列表调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1307, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("图文推送客户经理查询企业列表调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 新建图文推送活动接口
     * 
     * @return
     */
    public String insertGraphicPushInfo(String requestBody) {
        logger.debug("新建图文推送活动,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String userId = "";
        String userName = "";
        String roleId = "";
        String telNum = "";
        String userCityArea = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            userId = sessionJson.getString("userId");
            userName = sessionJson.getString("userName");
            roleId = sessionJson.getString("roleId");
            telNum = sessionJson.getString("telNum");
            userCityArea = sessionJson.getString("userCityArea");
            logger.debug("新建图文推送活动获取操作用户信息,userId:{},userName:{},roleId:{},telNum:{},userCityArea:{}", userId, userName, roleId, telNum, userCityArea);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        String activeName = requestJson.getString("activeName");// 推送活动名称
        String targetNums = requestJson.getString("targetNums");// 推送人数
        String activeType = requestJson.getString("activeType");// 活动类型 1:营销活动推广2:客户关怀提醒3:宣传服务告知
        String isNeedService = requestJson.getString("isNeedService");// 是否需要客服系统:0不需要、1需要
        String activeStartTime = requestJson.getString("activeStartTime");// 活动开始时间
        String activeEndTime = requestJson.getString("activeEndTime");// 活动结束时间
        String planPushTime = requestJson.getString("planPushTime");// 计划推送时间
        String activeDetail = requestJson.getString("activeDetail");// 活动描述
        String graphicId = requestJson.getString("mainId");// 图文mainid
        String squareId = requestJson.getString("squareId");// 应用id
        String attachmentFileName = requestJson.getString("attachmentFileName");// 附件名称
        String attachmentFilePath = requestJson.getString("attachmentFilePath");// 附件路径
        String excelFileName = requestJson.getString("excelFileName");// excel名称
        String excelFilePath = requestJson.getString("excelFilePath");// excel路径
        String corpIds = requestJson.getString("corpIds");// 企业id列表
        String isPushAll = requestJson.getString("isPushAll");// 目标用户是否是全体推送 1-是，0否(excel)
        PushRequestVO pv = new PushRequestVO();
        try {
            pv.setActiveName(activeName);
            if (!StringUtils.isEmpty(targetNums) && org.apache.commons.lang3.StringUtils.isNumeric(targetNums))
                pv.setTargetNums(Integer.parseInt(targetNums));
            pv.setActiveType(Integer.parseInt(activeType));
            pv.setIsNeedService(Integer.parseInt(isNeedService));
            pv.setActiveStartTime(activeStartTime);
            pv.setActiveEndTime(activeEndTime);
            pv.setPlanPushTime(planPushTime);
            pv.setActiveDetail(activeDetail);
            pv.setGraphicId(graphicId);
            pv.setSquareId(squareId);
            pv.setAttachmentFileName(attachmentFileName);
            pv.setAttachmentFilePath(attachmentFilePath);
            pv.setExcelFileName(excelFileName);
            pv.setExcelFilePath(excelFilePath);
            pv.setCorpIds(corpIds);
            pv.setIsPushAll(Integer.parseInt(isPushAll));
        } catch (Exception e) {
            logger.error("新建图文推送活动转换参数异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        }
        try {
            logger.debug("新建图文推送活动,roleId:{},userId:{},userName:{},telNum:{},userCityArea:{},PushRequestVO:{}", roleId, userId, userName, telNum, userCityArea,
                    JSON.toJSONString(pv));
            Object o = graphicPushInfoInterface.saveGraphicPush(Integer.parseInt(roleId), userId, userName, telNum, userCityArea, pv);
            logger.debug("新建图文推送活动调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1308, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("新建图文推送活动调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 根据sessionid获取角色接口
     * 
     * @return
     */
    public String getRoleBySessionId(String requestBody) {
        logger.debug("根据sessionid获取角色,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String userId = "";
        String userName = "";
        String roleId = "";
        String telNum = "";
        String userCityArea = "";
        DictionaryVo dv = null;
        try {
            sessionJson = JSONObject.parseObject(session);
            roleId = sessionJson.getString("roleId");// 1
                                                     // 系统管理员,2平台管理员,3企业管理员,4省公司管理员,5地市公司管理员,6区县管理员,7客户经理,8省直管理员
            userId = sessionJson.getString("userId");
            userName = sessionJson.getString("userName");
            telNum = sessionJson.getString("telNum");
            userCityArea = sessionJson.getString("userCityArea");
            dv = dictionaryInterface.findDictionaryByDictIdAndKey(51L, userCityArea);
            logger.debug("根据sessionid获取角色,userId:{},userName:{},roleId:{},telNum:{},userCityArea:{}", userId, userName, roleId, telNum, userCityArea);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("roleId", roleId);
        model.put("userId", userId);
        model.put("userName", userName);
        model.put("telNum", telNum);
        model.put("roleName", roleMap.get(roleId));
        model.put("userCityName", dv.getDictKeyDesc());
        model.put("userCityArea", userCityArea);
        return ResponsePackUtil.buildPack("0000", model);
    }

    /**
     * 获取图文推送详情
     * 
     * @return
     */
    public String getGraphicPushDetail(String requestBody) {
        logger.debug("获取图文推送详情,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String id = requestJson.getString("id");// 行数
        if (StringUtils.isEmpty(id))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        try {
            logger.debug("获取图文推送详情,id:{}", id);
            Object o = graphicPushInfoInterface.findGraphicPushInfoResponseById(id);
            logger.debug("获取图文推送详情调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1309, "");
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            return ResponsePackUtil.buildPack(o, ser);
        } catch (Exception e) {
            logger.error("获取图文推送详情调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 新建企业图文推送活动接口
     * 
     * @return
     */
    public String insertCorpGraphicPushInfo(String requestBody) {
        logger.debug("新建企业图文推送活动,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpId = "";
        String userId = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            corpId = sessionJson.getString("corpId");
            userId = sessionJson.getString("userId");
            logger.debug("新建企业图文推送活动获取操作用户信息,corpId:{},userId:{}", corpId, userId);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        String serviceId = requestJson.getString("serviceId");// 应用id
        String mainId = requestJson.getString("mainId");// 素材mainId
        JSONArray departIds = requestJson.getJSONArray("departIds");// 部门id集合
        JSONArray memberIds = requestJson.getJSONArray("memberIds");// 激活人员id集合
        String isAll = requestJson.getString("isAll");// 是否选择了全企业 1：是
        try {
            if (null != departIds && 1 == departIds.size()) {
                // 只传了一个部门id，判断是否为根节点部门，是则全企业推送
                DepartMentVO dv = departMentInterface.findById(departIds.getString(0));
                if (null == dv) {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
                } else if ("1".equals(dv.getParentDeptNum())) {
                    logger.debug("新建企业图文推送活动,corpId:{},serviceId:{},mainId:{}", corpId, serviceId, mainId);
                    Object o = graphicPushInfoInterface.pushGraphicForCorpManager(serviceId, mainId, corpId, userId);
                    logger.debug("新建企业图文推送活动调取服务返回结果,response:{}", JSON.toJSONString(o));
                    if (null == o)
                        return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1310, "");
                    return ResponsePackUtil.buildPack(o);
                }
            }
            logger.debug("新建企业部分人员图文推送活动,corpId:{},serviceId:{},mainId:{},departIds:{},memberIds:{}", corpId, serviceId, mainId, JSON.toJSONString(departIds),
                    JSON.toJSONString(memberIds));
            Object o = graphicPushInfoInterface.pushGraphicForCorpManager(serviceId, mainId, corpId, userId, departIds, memberIds,isAll);
            logger.debug("新建企业部分人员图文推送活动调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1310, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("新建企业图文推送活动调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 撤销企业图文推送活动接口
     * 
     * @return
     */
    public String cancelCorpGraphicPushInfo(String requestBody) {
        logger.debug("撤销企业图文推送活动,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String userId = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            userId = sessionJson.getString("userId");
            logger.debug("撤销企业图文推送活动获取操作用户信息,corpId:{}", userId);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        String pushId = requestJson.getString("pushId");
        try {
            logger.debug("撤销企业图文推送活动,pushId:{}", pushId);
            Object o = graphicPushInfoInterface.cancelGraphicForCorpManager(pushId, userId);
            logger.debug("撤销企业图文推送活动调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1310, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("撤销企业图文推送活动调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }
    
    /**
     * 获取企业图文推送接口
     * 
     * @return
     */
    public String listCorpGraphicPushInfo(String requestBody) {
        logger.debug("撤销企业图文推送活动,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String userId = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            userId = sessionJson.getString("userId");
            logger.debug("撤销企业图文推送活动获取操作用户信息,userId:{}", userId);
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        String serviceName = requestJson.getString("serviceName");// 应用名称
        String title = requestJson.getString("title");// 素材主标题
        String pushStatus = requestJson.getString("pushStatus");// 推送状态
        String page = requestJson.getString("page");// 页数
        String row = requestJson.getString("row");// 行数
        int pageIndex = 1;
        int pageSize = 10;
        try {
            if (!StringUtils.isEmpty(page)) pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        }
        try {
            if (!StringUtils.isEmpty(row))pageSize = Integer.parseInt(row);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        }
        
        try {
            Object o = squareInterface.findPushPage(serviceName, title, pushStatus, userId, pageIndex, pageSize);
            return ResponsePackUtil.buildPack("0000", o);
        } catch (Exception e) {
            logger.error("撤销企业图文推送活动调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 图文推送测试
     * 
     * @return
     */
    public String graphicPushTest(String requestBody) {
        logger.debug("图文推送测试,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String activeName = requestJson.getString("activeName");// 推送活动名称
        String telNum = requestJson.getString("telNum");// 推送手机号
        String serviceId = requestJson.getString("serviceId");// 应用id
        String mainId = requestJson.getString("mainId");// 素材mainId
        if (StringUtils.isEmpty(telNum) || StringUtils.isEmpty(mainId) || StringUtils.isEmpty(serviceId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1301, "");
        }
        try {
            logger.debug("图文推送测试,activeName:{},serviceId:{},mainId:{},telNum:{}", activeName, serviceId, mainId, telNum);
            Object o = graphicPushInfoInterface.testPush(telNum, activeName, serviceId, mainId);
            logger.debug("图文推送测试调取服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1311, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("图文推送测试调取服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1302, "");
        }
    }

    /**
     * 根据部门id获取激活人员
     * 
     * @param requestBody
     * @return
     * @author wuyf 2016年12月5日
     */
    public String getClientUserByDepart(String requestBody) {
        logger.debug("企业图文推送-获取激活人员,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionid = requestJson.getString("sessionid");
            String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
            JSONObject sessionJson = null;
            String corpId = "";
            String deptid = requestJson.getString("deptid");
            /** 校验参数完整性 */
            if (!StringUtils.checkParam(sessionid, true, 50) || !StringUtils.checkParam(deptid, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            // 获取企业id
            try {
                sessionJson = JSONObject.parseObject(session);
                corpId = sessionJson.getString("corpId");
                logger.debug("新建企业图文推送活动获取操作用户信息,corpId:{}", corpId);
            } catch (Exception e) {
                logger.error("获取session报错,session:{}", session, e);
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
            }

            /** 校验该企业是否存在 */
            CorpVO corpVO = corpInterface.findById(corpId);
            if (corpVO == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2052, "");

            /** 根据企业渠道，查询对应通讯录表 */
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_deptId", deptid);
            conditions.put("EQ_corpId", corpId);
            List<MemberInfoVOs> list = memberInfoInterfaces.findMemberInfoByCondition(conditions, null);
            List<Map<String, Object>> returnList =new ArrayList<Map<String, Object>>();
            if(CollectionUtils.isNotEmpty(list)){
                for(MemberInfoVOs mv:list){
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("userId", mv.getMemId());
                    map.put("userName", mv.getMemberName());
                    map.put("deptId", mv.getDeptId());
                    returnList.add(map);
                }
            }
            return ResponsePackUtil.buildPack("0000", returnList);
        } catch (Exception e) {
            logger.error("企业图文推送-获取激活人员异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }

    }
    
    /**
     * 查询角色列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String getRoleList(String requestBody) {
    	JSONObject requestJson = JSONObject.parseObject(requestBody);
    	
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpId ="";
        try {
        	sessionJson = JSONObject.parseObject(session);
            corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        
        // 校验参数
        if (null == corpId.trim()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, roleInterface.roleTree(corpId));
    }

    /**
     * 新增角色组
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String groupRoleAdd(String requestBody) {
    	JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpId = "";
        String optId = "";
        String groupName = requestJson.getString("groupName");
        try {
			sessionJson = JSONObject.parseObject(session);
		    corpId = sessionJson.getString("corpId");
		    optId = sessionJson.getString("userId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        // 校验参数
        if (null == corpId.trim() || null == optId.trim() || null == groupName.trim()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验组名是否重复
        if (null != roleInterface.findByGroupName(groupName, corpId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1104,"");
        }
        // 新增群组
        roleInterface.groupAdd(groupName, corpId, optId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,"");
    }

    /**
     * 删除角色组
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String groupRoleDelete(String requestBody) {
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        Long groupId = requestJson.getLong("groupId");
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        String corpId = "";
        JSONObject sessionJson = null;
        try {
			sessionJson = JSONObject.parseObject(session);
			corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        if (groupId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");

        }
        GroupRole group = roleInterface.findGroupRoleById(groupId);
        if (group == null) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        // 校验该群组是否属于该企业
        if (!corpId.equals(group.getCorpId())) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011,"");
        }
        // 默认分组不能操作
        if (group.getIsDefault() == 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1103,"");
        }
        List<RoleVO> roleList = roleInterface.findByGroupId(groupId);
        // 如果群组下有角色，不能删除
        if (!CollectionUtils.isEmpty(roleList)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1101,"");
        }
        roleInterface.groupDelete(group);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,"");
    }

    /**
     * 修改角色组
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String groupRoleUpdate(String requestBody) {
        String corpId = "";
        String optId = "";      
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String groupName = requestJson.getString("groupName");
        long groupId = requestJson.getLongValue("groupId");
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
            optId = sessionJson.getString("userId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        // 校验参数
        if (null == corpId.trim() || null == optId.trim() || null ==groupName.trim() || groupId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验数据是否存在
        GroupRole oldGroup = roleInterface.findGroupRoleById(groupId);
        if (null == oldGroup) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        // 默认分组不能操作
        if (oldGroup.getIsDefault() == 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1103,"");
        }
        // 校验该群组是否属于该企业
        if (!corpId.equals(oldGroup.getCorpId())) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011,"");
        }
        // 校验组名是否重复
        GroupRole group = roleInterface.findByGroupName(groupName, corpId);
        if (null != group && !group.getGroupId().equals(groupId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1100,"");
        }
        roleInterface.groupUpdate(oldGroup, groupName, optId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,"");
    }

    /**
     * 查询角色组详情
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String groupRoleDetail(String requestBody) {
        String corpId = "";
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        long groupId = requestJson.getLongValue("groupId");
        // 校验参数
        if (null == corpId.trim() || groupId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验数据是否存在
        GroupRoleVO groupVO = roleInterface.groupDetail(groupId);
        if (null == groupVO) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, groupVO);
    }

    /**
     * 新增/修改角色时 获取的群组列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String roleEditGroupList(String requestBody) {
        String corpId = "";
        // 校验参数
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
//        String deptId = org.apache.commons.lang3.StringUtils.isEmpty(requestJson.getString("deptId"))==true?"":requestJson.getString("deptId");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        if (null ==corpId.trim()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        List<GroupRoleVO> groupListVO = roleInterface.getGroupList(corpId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, groupListVO);
    }

    /**
     * 新增角色
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String roleAdd(String requestBody) {
        String corpId = "";
        String optId = "";
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        long groupId = requestJson.getLongValue("groupId");
        String roleName = requestJson.getString("roleName");   
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
        	optId = sessionJson.getString("userId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        // 校验参数
        if (null == corpId.trim() || null == roleName.trim() || groupId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验群组是否存在
        GroupRole group = roleInterface.findGroupRoleById(groupId);
        if (null == group) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        // 校验该群组是否属于该企业
        if (!corpId.equals(group.getCorpId())) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011,"");
        }
        // 校验角色名在当前群组是否已存在
        Role role = roleInterface.findByGroupIdAndRoleName(groupId, roleName);
        if (role != null) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1102,"");
        }
        // 只能操作非默认的角色
        if (group.getIsDefault() == 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011,"");
        }
        roleInterface.roleAdd(roleName, groupId, optId, corpId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,"");
    }

    /**
     * 删除角色
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String roleDelete(String requestBody) {
        String corpId = "";
        
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        long roleId = requestJson.getLongValue("roleId");        
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        
        // 校验参数
        if (null == corpId.trim() || roleId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验角色是否存在
        Role role = roleInterface.findRoleById(roleId);
        if (null == role) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        // 校验角色组是否存在
        GroupRole group = roleInterface.findGroupRoleById(role.getGroupId());
        if (null == group) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        // 校验该群组是否属于该企业
        if (!corpId.equals(group.getCorpId())) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011,"");
        }
        // 校验角色是否有对应人员
        if (!CollectionUtils.isEmpty(roleInterface.checkHasMember(corpId, roleId, role.getRoleType()))) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1101,"");
        }
        // 只能操作非默认的角色
        if (role.getRoleType() != 0) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1103,"");

        }
        roleInterface.roleDelete(role);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,"");
    }

    /**
     * 修改角色
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String roleUpdate(String requestBody) {
        String corpId = "";
        String optId = "";
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        long roleId = requestJson.getLongValue("roleId");
        long targetGroupId = requestJson.getLongValue("targetGroupId");
        String roleName = requestJson.getString("roleName");
        
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
        	optId = sessionJson.getString("userId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        
        // 校验参数
        if (null == corpId.trim() || null == roleName.trim() || null == optId.trim() || roleId < 1 || targetGroupId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验角色是否存在
        Role role = roleInterface.findRoleById(roleId);
        if (null == role) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        // 校验目标角色组是否存在
        GroupRole targetGroup = roleInterface.findGroupRoleById(targetGroupId);
        if (null == targetGroup) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        // 校验角色名是否重复
        Role newRole = roleInterface.findByGroupIdAndRoleName(targetGroup.getGroupId(), roleName);
        if (newRole != null && !newRole.getRoleId().equals(roleId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1102,"");
        }
        // 校验该群组是否属于该企业
        if (!corpId.equals(role.getCorpId()) || !corpId.equals(targetGroup.getCorpId())) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011,"");
        }
        // 只能操作非默认的角色
        if (role.getRoleType() != 0) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1103,"");
        }
        roleInterface.roleUpdate(role, roleName, targetGroupId, optId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,"");
    }

    /**
     * 查询角色详情
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String roleDetail(String requestBody) {
        String corpId = "";
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        long roleId = requestJson.getLongValue("roleId");
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        // 校验参数
        if (null == corpId.trim() || roleId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验角色是否存在
        RoleVO vo = roleInterface.roleDetail(roleId);
        if (null == vo) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, vo);
    }

    /**
     * 根据角色查询角色人员列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String memberList(String requestBody) {
        String corpId = "";        
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        long roleId = requestJson.getLongValue("roleId");
        int page = requestJson.getIntValue("page");
        int rows = requestJson.getIntValue("rows");       
        
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        
        // 校验参数
        if (null == corpId.trim() || roleId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验角色是否存在
        RoleVO vo = roleInterface.roleDetail(roleId);
        if (null == vo) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        Set<String> memIds = roleInterface.checkHasMember(corpId, roleId, vo.getRoleType());
        MemberInfoPageVO pageVo = memberInfoInterfaces.getListByMemIds(page, rows, memIds);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, pageVo);
    }
    
    /**
     * 根据角色查询角色人员列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String memberListNoPage(String requestBody) {
        String corpId = "";
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        long roleId = requestJson.getLongValue("roleId");     
        
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        
        // 校验参数
        if (null == corpId.trim() || roleId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验角色是否存在
        RoleVO vo = roleInterface.roleDetail(roleId);
        if (null == vo) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        List<MemberInfoVOs> list = roleInterface.findByRole(roleId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, list);
    }
    
    /**
     * 根据角色查询角色人员列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String roleList(String requestBody) {
     
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        long groupId = requestJson.getLongValue("groupId");     
        
      
        // 校验角色是否存在
        GroupRole group = roleInterface.findGroupRoleById(groupId);
        if (null == group) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        List<RoleVO> list = roleInterface.findByGroupId(groupId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, list);
    }

    /**
     * 根据角色新增角色人员
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String memberAdd(String requestBody) {
        String corpId = "";
        String optId = "";
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        JSONArray array = requestJson.getJSONArray("memIds");
        long roleId = requestJson.getLongValue("roleId");
        String isAll = requestJson.getString("isAll");// 是否选择了全企业 1：是
        
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
        	optId = sessionJson.getString("userId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        
        // 校验参数
        if (null == corpId.trim() || roleId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验角色是否存在
        Role role = roleInterface.findRoleById(roleId);
        if (null == role) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        GroupRole group = roleInterface.findGroupRoleById(role.getGroupId());
        if (null == group) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        // 校验操作目标是否属于当前企业
        if (!corpId.equals(group.getCorpId())) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011,"");
        }
        roleInterface.memberAdd(array, roleId, optId,isAll,corpId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,"");
    }

    /**
     * 根据角色批量删除角色人员列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String memberBatchDelete(String requestBody) {
        String corpId = "";
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        JSONArray array = requestJson.getJSONArray("memIds");
        long roleId = requestJson.getLongValue("roleId");
        
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        
        // 校验参数
        if (null == corpId.trim() || null == array || array.isEmpty() || roleId < 1) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001,"");
        }
        // 校验角色是否存在
        Role role = roleInterface.findRoleById(roleId);
        if (null == role) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        GroupRole group = roleInterface.findGroupRoleById(role.getGroupId());
        if (null == group) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010,"");
        }
        // 校验操作目标是否属于当前企业
        if (!corpId.equals(group.getCorpId())) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011,"");
        }
        roleInterface.memberBatchDelete(array, roleId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC,"");
    }
    
    /**
     * 查询部门列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String getDeparts(String requestBody) {
    	
    	JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpId ="";
        String deptId ="";
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	deptId = sessionJson.getString("deptId");
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        if (StringUtils.isEmpty(corpId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        List<DepartMentVO> deptList = null;
        if (StringUtils.isEmpty(deptId)) {
            deptList = departMentInterface.findByCorpId(corpId);
        } else {
            deptList = departMentInterface.findDeptAndSubDept(deptId);
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, deptList);
    }
    
    /**
     * 根据部门id查询人员列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String getMembers(String requestBody) {
    	JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpId ="";
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        if (StringUtils.isEmpty(corpId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }

        if (StringUtils.isEmpty(requestBody)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        JSONObject bodyJson = JSON.parseObject(requestBody);
        String deptId = bodyJson.getString("deptId");
        int page = bodyJson.getIntValue("page");
        int rows = bodyJson.getIntValue("rows");
        if (StringUtils.isEmpty(deptId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        MemberInfoPageVO pageVo = memberInfoInterfaces.getListByDepartId(page, rows, deptId, corpId, sessionJson.getString("deptId"));
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, pageVo);
    }

    /**
     * 根据关键字搜索人员列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String searchMembers(String requestBody) {
    	JSONObject requestJson = JSON.parseObject(requestBody);
        String corpId = "";
        String deptId = org.apache.commons.lang3.StringUtils.isEmpty(requestJson.getString("deptId"))==true?"-1":requestJson.getString("deptId");
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        if (StringUtils.isEmpty(corpId) || StringUtils.isEmpty(deptId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }

        if (StringUtils.isEmpty(requestBody)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        
        String param = requestJson.getString("param");
        if (null == StringUtils.trimToNull(param)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        List<MemberInfoVOs> voList = memberInfoInterfaces.getMemberListBySearch(param, corpId, deptId);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, voList);
    }
    
    /**
     * 根据部门id查询人员以及下级部门列表
     * 
     * @param currentSysUser
     * @param requestBody
     * @return
     */
    private String getDeptAndMemByDeptId(String requestBody) {
    	JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpId = "";
        String deptId = "";
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        try {
        	sessionJson = JSONObject.parseObject(session);
        	corpId = sessionJson.getString("corpId");
        	deptId = sessionJson.getString("deptId");
		} catch (Exception e) {
			logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
		}
        if (StringUtils.isEmpty(corpId)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }

        if (StringUtils.isEmpty(requestBody)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        JSONObject bodyJson = JSON.parseObject(requestBody);
        String deptIdParam = bodyJson.getString("deptId");

        if (StringUtils.isEmpty(deptIdParam) || deptIdParam.equals("null")) {
            if (StringUtils.isEmpty(deptId) || deptId.equals("-1")) {
                deptIdParam = null;
            } else {
                deptIdParam = deptId;
            }
        }

        logger.debug("corpId:{},deptIdParam:{},deptId:{}", corpId, deptIdParam, deptId);
        DeptAndMemberVO vo = memberInfoInterfaces.getDeptAndMemsByDeptId(corpId, deptIdParam);
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, vo);
    }
    
    /**
     * 新建微信图文推送活动接口
     * 
     * @return
     * @throws Exception 
     * @throws NumberFormatException 
     */
    public String insertGraphicPushInfoToWeChat(String requestBody) {
        logger.debug("新建图文推送至微信,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = JSONObject.parseObject(session);
        String sourceId = requestJson.getString("sourceId");// 素材主键
        String serviceId = requestJson.getString("serviceId");// 服务id
        String serviceName = requestJson.getString("serviceName");// 服务名称
        String isAll = requestJson.getString("isAll");// 是否选择了全企业 1：是
        
        GraphicSourceVo vo = new GraphicSourceVo();
        try {
        	vo = graphicSourceInterface.getGraphicsourceContentToService(Long.valueOf(sourceId));
		} catch (Exception e) {
			logger.error("转换类型报错",e);
		}

        
        String userId = "";
        String userName = "";
        String telNum = "";
        String corpId = "";
        try {
            userId = sessionJson.getString("userId");
            userName = sessionJson.getString("userName");
            telNum = sessionJson.getString("telNum");
            corpId = sessionJson.getString("corpId");
            logger.debug("新建图文推送至微信活动获取操作用户信息,userId:{},userName:{},telNum:{},corpId:{}", userId, userName, telNum,corpId);
            
        } catch (Exception e) {
            logger.error("获取session报错,session:{}", session, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "");
        }
        List<String> memberIds = new ArrayList<>();
        if(!"1".equals(isAll)){
        	memberIds = JSON.parseArray(requestJson.getString("memberIds"), String.class);
        }else{
        	List<MemberInfoVO> voList = memberInfoInterface.findByCorpId(corpId);
        	for (MemberInfoVO memberInfoVO : voList) {
        		memberIds.add(memberInfoVO.getMemId());
			}
        }
                
       return pushToEsip(vo,memberIds,userId,userName,telNum,serviceName,serviceId);
    }  
        

    private String pushToEsip(GraphicSourceVo vo,List<String> userIds, String userId, String userName,String telNum,String serviceName,String serviceId) {
    	String result = "";
        try {
            if (CollectionUtils.isEmpty(userIds)) {
                return null;
            }
            JSONObject json = new JSONObject();
            String eventId = UUID.randomUUID().toString();
            json.put("EventId", eventId);
            json.put("EventType", 1);
            String operTypeStr = "新增";
            json.put("OperType", operTypeStr);
            // String eventTitle = "临时会议";
            json.put("EventTitle", vo.getGraphicTitle());
            json.put("EventContent", serviceName);
            json.put("EventTime", DateFormatUtils.format(vo.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            json.put("ReleaseTime", DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
            json.put("EventPublisher", userId == null ? "1" : userId);
            json.put("EventPublisherMobile", telNum == null ? "1" : telNum);
            json.put("EventPublishName", userName == null ? "1" : userName);
            json.put("MsgType", "news");
            json.put("URL", vo.getHtmlUrl());
            // fastdfs zk中节点
            String path = zkUtil.findData(Constants.Param.ZK_FASTDFS_NGINX);
            if (!org.apache.commons.lang3.StringUtils.isNotEmpty(path)) {
                path = PropertiesUtils.findPropertiesKey("fileServerurl", null);
            }
            json.put("CoverUrl",path+vo.getGraphicPic());
            String htmlUrl = zkUtil.findData(Constants.Param.GRAPHIC_SOURCE_URL_WECHAT) + "?gid=" + vo.getId() + "&amp;serviceid=" + serviceId;
//            String htmlUrl = Constants.GRAPHIC_SOURCE_URL + "?gid=" + graphicSourceVo.getId() + "&serviceid=" + serviceId;
            if(vo.getGraphicSourceType().equals("0")){
            	json.put("URL",htmlUrl);
            }else{
            	json.put("URL",vo.getConnectUrl());
            }
            json.put("type", 1);

            JSONArray sendUsers = new JSONArray();
            for(String str : userIds){
            	JSONObject sendUser = new JSONObject();
            	MemberInfoVO memvo = memberInfoInterface.findById(str);
            	sendUser.put("UumId", memvo.getMemNum());
                sendUser.put("Mobile", memvo.getTelNum());
                sendUsers.add(sendUser);
            }
            json.put("SendUsers", sendUsers);
//            String data = "EventId=" + eventId + "&EventType=" + 1 + "&OperType=" + operTypeStr+"&EventTitle=" + "素材中心" + "&EventContent=" + "素材中心图文推送" + "&EventTime=" +DateFormatUtils.format(vo.getCreateTime(), "yyyy-MM-dd HH:mm:ss")+ "&ReleaseTime=" + DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss") + "&EventPublisher=" + userId+"&EventPublisherMobile=" + telNum + "&EventPublisherName=" + userName + "&type=" +1+ "&SendUsers=" + sendUsers;
            
            String url = zkUtil.findData(Constants.Param.ZK_WECHAT_URL);
//            String url = "http://192.168.1.3:14008/esip/";
            String key = zkUtil.findData(Constants.Param.ZK_WECHAT_AES_KEY);
            String aesText = AESUtil.encode128(key,JSON.toJSONString(json));
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("function_id", "20180122");
            paramMap.put("request_body", aesText);
            result = HttpUtil.post(url, paramMap);
            logger.debug("调用esip推送微信消息响应,eventId:{},request_body:{},result:{}", eventId, json.toJSONString(), result);
            
        } catch (Exception e) {
            logger.error("调用esip推送微信消息异常,e:{}", e);
        }
        return result;
    }
}
