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
import java.util.UUID;

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
import com.royasoft.vwt.cag.util.PushUtil;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.mq.ActionRecordUtil;
import com.royasoft.vwt.cag.util.mq.MsgPushUtil;
import com.royasoft.vwt.cag.util.mq.RedisAction;
import com.royasoft.vwt.cag.util.mq.RocketMqUtil;
import com.royasoft.vwt.cag.vo.NewsItem;
import com.royasoft.vwt.cag.vo.ServicePush;
import com.royasoft.vwt.cag.vo.SquareInfoMemberVo;
import com.royasoft.vwt.cag.vo.SquareInfoSelfVo;
import com.royasoft.vwt.cag.vo.WorkBenchAction;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.ImRedisInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.serviceCallBack.api.ServiceCallBackInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.LabelInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareWelMsgInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.TwoDimensionalCodeInterface;
import com.royasoft.vwt.soa.business.square.api.vo.GraphicSourceVo;
import com.royasoft.vwt.soa.business.square.api.vo.LabelVo;
import com.royasoft.vwt.soa.business.square.api.vo.SquareDownloadRecordVo;
import com.royasoft.vwt.soa.business.square.api.vo.SquareMenuVo;
import com.royasoft.vwt.soa.business.square.api.vo.SquareParametersVo;
import com.royasoft.vwt.soa.business.square.api.vo.SquareRelationVo;
import com.royasoft.vwt.soa.business.square.api.vo.SquareVo;
import com.royasoft.vwt.soa.business.square.api.vo.SquareWelMsgVo;
import com.royasoft.vwt.soa.business.square.api.vo.TwoDimensionalCodeVo;
import com.royasoft.vwt.soa.sundry.clientversion.api.interfaces.ClientVersionInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.oaaccount.api.Vo.OAaccountInfoVo;
import com.royasoft.vwt.soa.uic.oaaccount.api.interfaces.OAaccountInfoInterface;

/**
 * 多角色工作台业务处理
 *
 * @Author:MB
 * @Since:2016年4月11日
 */
@Scope("prototype")
@Service
public class WorkBenchService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkBenchService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private DatabaseInterface databaseInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Autowired
    private CorpInterface corpInterface;
    @Autowired
    private DepartMentInterface departMentInterface;
    @Autowired
    private SquareInterface squareInterface;

    @Autowired
    private MsgPushUtil msgPushUtil;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private DictionaryInterface dictionaryInterface;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private LabelInterface labelInterface;

    @Autowired
    private ActionRecordUtil actionRecordUtil;

    @Autowired
    private ServiceCallBackInterface serviceCallBackInterface;

    @Autowired
    private ImRedisInterface imRedisInterface;

    @Autowired
    private OAaccountInfoInterface oAaccountInfoInterface;// OA账号服务接口
    
    @Autowired
    private SquareWelMsgInterface squareWelMsgInterface;  //服务号欢迎语服务

    @Autowired
    private TwoDimensionalCodeInterface twoDimensionalCodeInterface;  // 获取URL生成二维码扫描
    
    @Autowired
    private PushUtil pushUtil;
    
    @Autowired
    private ClientVersionInterface clientVersionInterface;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.workBench_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("多角色工作台业务处理(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /***************************** 业务逻辑处理 *********************************************/

                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        case FunctionIdConstant.WB_DOATTENTION:
                            resInfo = doAttention(request_body, user_id);
                            break;
                        case FunctionIdConstant.WB_DOCANCELATTENTION:
                            resInfo = doAttention(request_body, user_id);
                            break;
                        case FunctionIdConstant.WB_GETALLAPP:
                            resInfo = getAllAppList(request_body, user_id);
                            break;
                        case FunctionIdConstant.WB_GETAPPDETAIL:
                            resInfo = getAppDetail(request_body, user_id);
                            break;
                        case FunctionIdConstant.WB_GETLABEL:
                            resInfo = getLabelOfMember(request_body, user_id);
                            break;
                        case FunctionIdConstant.WB_GETSELFAPP:
                            resInfo = getAppOfSelf(request_body, user_id);
                            break;
                        case FunctionIdConstant.GETPERSONALIZEAPP:
                            resInfo = getAppOfSelfTailor(request_body, user_id);
                            break;
                        case FunctionIdConstant.WB_GRAPHICPSUH:
                            // resInfo = prefectInternetInfo(request_body);
                            break;
                        case FunctionIdConstant.WB_MENUPUSHMSG:
                            resInfo = menuPushMessage(request_body, user_id);
                            break;
                        case FunctionIdConstant.WB_GETMENU:
                            resInfo = getMenuOfService(request_body, user_id);
                            break;
                        case FunctionIdConstant.WB_DOWNLOAD:
                            resInfo = doDownloadOr(request_body, user_id);
                            break;
                        case FunctionIdConstant.WB_TWODIMENSIONALCODE:
                            resInfo = getTwoDimensionalCode(request_body, user_id);
                            break;                            
                        default:
                            break;
                    }
                    logger.debug("多角色工作台业务处理(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("多角色工作台业务处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }
        }
    }

    /**
     * 获取应用列表 1901
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    public String getAllAppList(String requestBody, String aesKey) {
        logger.debug("获取应用列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 用户id */
        String userId = requestJson.getString("userId");
        /** 页码 （从1开始） */
        String pageIndex = requestJson.getString("pageIndex");
        /** 每页显示大小 */
        String pageSize = requestJson.getString("pageSize");
        /** 搜索应用名称 */
        String appName = requestJson.getString("appName");
        /** 请求客户端类型(Android、IOS) */
        String appType = requestJson.getString("appType");
        /** 应用标签id */
        String appLabelId = requestJson.getString("appLabelId");

        logger.debug("获取应用列表(解析body),userId:{},pageIndex:{},pageSize:{},appName:{},appType:{},appLabelId:{}", userId, pageIndex, pageSize, appName, appType, appLabelId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, pageIndex, pageSize, appType) || (!appType.equalsIgnoreCase("ios") && !appType.equalsIgnoreCase("android")))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        /** 校验该用户是否存在 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("获取应用列表(校验该用户是否存在),userId:{},clientUserVO:{}", userId, clientUserVO);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");

        /** 分页查询与该用户相关应用--调用服务化 */
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("EQ_userId", userId);
        conditions.put("EQ_appType", appType);
        if (null != appLabelId && !"".equals(appLabelId))
            conditions.put("EQ_labelid", appLabelId);
        if (null != appName && !"".equals(appName))
            conditions.put("LIKE_name", appName);
        Map<String, Object> resMap = squareInterface.findSquareByUserId(Integer.valueOf(pageIndex), Integer.valueOf(pageSize), conditions, null);
        logger.debug("获取应用列表(调用服务化分页查询),userId:{},resMap:{}", userId, JSONObject.toJSONString(resMap));
        List<SquareVo> squareVos = new ArrayList<SquareVo>();
        if (null != resMap && null != resMap.get("squareVos"))
            squareVos = (List<SquareVo>) resMap.get("squareVos");

        logger.debug("获取应用列表(squareVos),userId:{},squareVos:{}", userId, squareVos.size());
        List<SquareInfoMemberVo> squareInfoMemberVos = getSquareInfoMemberInfo(squareVos, userId, true);
        JSONObject resJson = new JSONObject();
        resJson.put("squareInfoList", squareInfoMemberVos);
        resJson.put("ftpIp", ParamConfig.ftp_ip);
        resJson.put("ftpPort", ParamConfig.ftp_port);
        resJson.put("ftpUsername", ParamConfig.ftp_username);
        resJson.put("ftpPassword", ParamConfig.ftp_pwd);
        logger.debug("获取应用列表(返回json),userId:{},resJson:{}", userId, JSONObject.toJSONString(resJson));

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), aesKey));
    }

    /**
     * 应用列表VO封装
     * 
     * @param squareVos
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    private List<SquareInfoMemberVo> getSquareInfoMemberInfo(List<SquareVo> squareVos, String userId, boolean boo) {
        List<SquareInfoMemberVo> squareInfoMemberVos = new ArrayList<SquareInfoMemberVo>();
        if (null != squareVos && !squareVos.isEmpty()) {
            for (SquareVo squareVo : squareVos) {
                SquareInfoMemberVo squareInfoMemberVo = new SquareInfoMemberVo();
                squareInfoMemberVo = transSquareInfoMemberVo(squareVo);
                if (null == squareInfoMemberVo)
                    continue;
                /** 调用服务化查询是否关注 */
                if (boo) {
                    Map<String, Object> conditions = new HashMap<String, Object>();
                    conditions.put("userId", userId);
                    conditions.put("squareIds", squareVo.getId());
                    Map<String, Object> squareRelationMap = squareInterface.findUsersSquareStatus(conditions);
                    int attend = 0;
                    if (null != squareRelationMap && !squareRelationMap.isEmpty()) {
                        List<SquareRelationVo> squareRelationVos = (List<SquareRelationVo>) squareRelationMap.get("squareRelationVos");
                        if (null != squareRelationVos && !squareRelationVos.isEmpty())
                            attend = squareRelationVos.get(0).getIsTrue();
                    }
                    squareInfoMemberVo.setIsAttend(attend);
                } else {
                    squareInfoMemberVo.setIsAttend(1);
                }

                List<String> paramList = new ArrayList<String>();
                if (squareVo.getType() == 3) {
                    // 回调模式是否传参：0-否，1-是
                    // 需要传参列表
                    if (squareVo.getTelNum() == 1)
                        paramList.add("FromUserTelNum");
                    if (squareVo.getUserId() == 1)
                        paramList.add("FromUserId");
                    if (squareVo.getChannelNum() == 1)
                        paramList.add("src");
                } else {// APK、H5
                    logger.debug("H5或者APK");
                    if (2 == squareVo.getIsFreeLogin()) {
                        paramList.add("FromUserTelNum");
                        paramList.add("FromUserId");
                        paramList.add("src");
                        paramList.add("token");
                    }
                    logger.debug("paramList{}", "参数" + paramList.toString());
                }
                try {
                    // 客户端是否传参：0-否，1-是
                    int flag = 0;
                    List<SquareParametersVo> list = squareInterface.findBySquareId(squareVo.getId());
                    if (null != list && !list.isEmpty()) {
                        for (SquareParametersVo v : list) {
                            if ("OAaccount".equals(v.getParameValue())) {
                                OAaccountInfoVo oaVo = oAaccountInfoInterface.findByMemId(userId);
                                logger.debug("获取OA账号信息，oaVo{}", null == oaVo ? "null" : JSON.toJSONString(oaVo));
                                paramList.add(v.getParameValue() + "=" + (null == oaVo ? "" : oaVo.getOaaccount()));
                            } else
                                paramList.add(v.getParameValue());
                        }
                    }
                    if (!paramList.isEmpty()) {
                        flag = 1;
                        squareInfoMemberVo.setParamList(JSON.toJSONString(paramList));
                    }
                    squareInfoMemberVo.setParamFlag(flag);

                } catch (Exception e) {
                    logger.error("获取H5或APK自定义参数异常", e);
                    continue;
                }

                squareInfoMemberVos.add(squareInfoMemberVo);
            }
        }
        return squareInfoMemberVos;
    }

    /**
     * 收藏关注 1902
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String doAttention(String requestBody, String aesKey) {
        logger.debug("收藏关注,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 用户id */
        String userId = requestJson.getString("userId");
        /** 操作类型：1为关注；0为取消 */
        String optType = requestJson.getString("optType");
        /** 应用id */
        String appId = requestJson.getString("appId");

        logger.debug("收藏关注(解析body),userId:{},optType:{},appId:{}", userId, optType, appId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, optType, appId) || (!optType.equalsIgnoreCase("1") && !optType.equalsIgnoreCase("0")))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        /** 校验该用户是否存在 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("收藏关注(校验该用户是否存在),userId:{},clientUserVO:{}", userId, clientUserVO);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");

        /** 校验应用是否存在 */
        SquareVo squareVo = squareInterface.findSquareById(appId);
        logger.debug("收藏关注(校验该应用是否存在),appId:{},squareVo:{}", appId, squareVo);
        if (null == squareVo)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1056, "");

        boolean isAttend = checkAppIsAttend(appId, userId);
        logger.debug("收藏关注(该用户是否已关注或者尚未关注),appId:{},isAttend:{},optType:{}", appId, isAttend, optType);

        /** 判断该用户是否已关注或者尚未关注 */
        if (optType.equals("1")) {// 关注
            /** 若查询该用户已关注该应用则返回 */
            if (isAttend)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1057, "");
        } else {// 取消关注
            /** 若查询该用户尚未关注该应用则返回 */
            if (!isAttend)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1058, "");
        }

        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("userId", userId);
        conditions.put("optType", optType);
        conditions.put("squareId", appId);
        int resStatus = squareInterface.saveSquareRelation(conditions);
        logger.debug("收藏关注(关注或取消关注返回结果),appId:{},resStatus:{},optType:{}", appId, resStatus, optType);
        if (resStatus == 0)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1060, "");

        if (appId.equals(ParamConfig.surf_no)) {
            if (optType.equals("1")) // 关注
                imRedisInterface.removeSet("ROYASOFT:VWT:VIC_CANCELLIST", userId);
            else
                imRedisInterface.addSet("ROYASOFT:VWT:VIC_CANCELLIST", userId);
        }
        try{
            if(optType.equals("1")){
                String welMsg="欢迎关注"+squareVo.getName();
                
                SquareWelMsgVo squareWelMsgVo=squareWelMsgInterface.findBysquareId(appId);
                if(null!=squareWelMsgVo&&null!=squareWelMsgVo.getWelMsg()&&!"".equals(squareWelMsgVo.getWelMsg()))
                    welMsg=squareWelMsgVo.getWelMsg();
                    
                    logger.info("服务号欢迎语======"+welMsg+"========="+userId);
                    pushUtil.pushTextNewIm(appId, userId,welMsg, "1");
            }
         }catch(Exception e){
             logger.error("推送服务号欢迎语异常",e);
         }

        if (optType.equals("1") && squareVo.getType() == 3) {
            List<SquareMenuVo> squareMenuVos = getSquareMenuVos(appId, clientUserVO.getTelNum(), userId);
            JSONObject resJson = new JSONObject();
            resJson.put("squareMenuInfo", squareMenuVos);
            return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), aesKey));
        }
        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 预置应用取消收藏和关注 1903
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String doCancelAttention(String requestBody, String aesKey) {
        logger.debug("预置应用取消收藏和关注,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 用户id */
        String userId = requestJson.getString("userId");
        /** 操作类型：1为关注；0为取消 */
        String optType = requestJson.getString("optType");
        /** 应用id */
        String appId = requestJson.getString("appId");

        logger.debug("预置应用取消收藏和关注(解析body),userId:{},optType:{},appId:{}", userId, optType, appId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, optType, appId) || (!optType.equalsIgnoreCase("1") && !optType.equalsIgnoreCase("0")))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        /** 校验该用户是否存在 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("预置应用取消收藏和关注(校验该用户是否存在),userId:{},clientUserVO:{}", userId, clientUserVO);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");

        /** 校验应用是否存在 */
        SquareVo squareVo = squareInterface.findSquareById(appId);
        logger.debug("预置应用取消收藏和关注(校验该应用是否存在),appId:{},squareVo:{}", appId, squareVo);
        if (null == squareVo)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1056, "");

        boolean isAttend = checkAppIsAttend(appId, userId);
        logger.debug("预置应用取消收藏和关注(该用户是否已关注或者尚未关注),appId:{},isAttend:{},optType:{}", appId, isAttend, optType);

        /** 判断该用户是否已关注或者尚未关注 */
        if (optType.equals("1")) {// 关注
            /** 若查询该用户已关注该应用则返回 */
            if (isAttend)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1057, "");
        } else {// 取消关注
            /** 若查询该用户尚未关注该应用则返回 */
            if (!isAttend)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1058, "");
        }

        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("userId", userId);
        conditions.put("optType", optType);
        conditions.put("squareId", appId);
        int resStatus = squareInterface.saveSquareRelationCancel(conditions);
        logger.debug("预置应用取消收藏和关注(关注或取消关注返回结果),appId:{},resStatus:{},optType:{}", appId, resStatus, optType);
        if (resStatus == 0)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1060, "");

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", "");
    }

    @SuppressWarnings("unchecked")
    private List<SquareMenuVo> getSquareMenuVos(String appId, String telNum, String userId) {
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("serviceno", appId);
        Map<String, Object> squareMap = squareInterface.findMenuRebackByServiceNo(conditions);
        if (null == squareMap || squareMap.isEmpty())
            return new ArrayList<SquareMenuVo>();
        List<SquareMenuVo> menuList = (List<SquareMenuVo>) squareMap.get("item");
        /** 针对面登陆的菜单，处理访问地址 */
        for (SquareMenuVo squareMenuVo : menuList) {
            if (squareMenuVo.getLogin() == 1 && !org.springframework.util.StringUtils.isEmpty(squareMenuVo.getUrladdress())) {
                String urlAddress = squareMenuVo.getUrladdress();
                if (urlAddress.indexOf("?") != -1) {
                    urlAddress += "&src=v&" + "FromUserTelNum=" + telNum + "&FromUserId=" + telNum;
                } else {
                    urlAddress += "?src=v&" + "FromUserTelNum=" + telNum + "&FromUserId=" + telNum;
                }
                squareMenuVo.setUrladdress(urlAddress);
            }
        }
        return transSquareMenuVo(menuList, telNum, userId);
    }

    /**
     * 登录获取应用信息 1904
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    public String getAppOfSelf(String requestBody, String aesKey) {
        logger.debug("登录获取应用信息,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 用户id */
        String userId = requestJson.getString("userId");
        /** 请求客户端类型(Android、IOS) */
        String appType = requestJson.getString("appType");
        
        String username = requestJson.getString("username");// 用户名
        String clientVersion = requestJson.getString("clientVersion");// 客户端版本
        String clientModel = requestJson.getString("clientModel");// 客户端类型
        
        logger.debug("登录获取应用信息(解析body),userId:{},appType:{}", userId, appType);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, appType) || (!appType.equalsIgnoreCase("ios") && !appType.equalsIgnoreCase("android")))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        /** 校验该用户是否存在 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("登录获取应用信息(校验该用户是否存在),userId:{},clientUserVO:{}", userId, clientUserVO);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");

        /** 分页查询与该用户有关应用--调用服务化 */
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("EQ_userId", userId);
        conditions.put("EQ_appType", appType);
        Map<String, Object> squareMap = squareInterface.findSquareByUserRelation(1, 10000, conditions, null);
        logger.debug("登录获取应用信息(查询相关应用),userId:{},squareMap:{}", userId, JSON.toJSONString(squareMap));
        List<SquareVo> squareVos = new ArrayList<SquareVo>();
        if (null != squareMap && !squareMap.isEmpty())
            squareVos = (List<SquareVo>) squareMap.get("squareVos");

        /** 还需要遍历上一个集合，获取服务号的权限和服务号的菜单列表 */
        List<SquareInfoMemberVo> squareInfoMemberVos = getSquareInfoMemberInfo(squareVos, userId, false);
        logger.debug("登录获取应用信息(squareInfoMemberVos),squareInfoMemberVos:{}", JSON.toJSONString(squareInfoMemberVos));
        List<SquareInfoSelfVo> squareInfoSelfVos = getSquareInfoSelfVos(squareInfoMemberVos, clientUserVO.getTelNum(), userId);
        logger.debug("登录获取应用信息(获取服务号的权限和服务号的菜单列表),userId:{},squareInfoSelfVos:{}", userId, JSON.toJSONString(squareInfoSelfVos));
        
        /** 记录登陆客户端设备信息 */
        logger.info("wdw 记录登陆客户端设备信息 start  appType:{},clientVersion:{},clientModel:{},username:{}",appType, clientVersion, clientModel, username);
        //saveClientInfo(clientType, clientVersion, clientModel, username);
        saveClientInfo(appType, clientVersion, clientModel, username);
        logger.info("wdw 记录登陆客户端设备信息 end  appType:{},clientVersion:{},clientModel:{},username:{}",appType, clientVersion, clientModel, username);

        JSONObject resJson = new JSONObject();
        resJson.put("squareInfoList", squareInfoSelfVos);
        resJson.put("ftpIp", ParamConfig.ftp_ip);
        resJson.put("ftpPort", ParamConfig.ftp_port);
        resJson.put("ftpUsername", ParamConfig.ftp_username);
        resJson.put("ftpPassword", ParamConfig.ftp_pwd);
        resJson.put("importAppList", getImportantServiceNo());
        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), aesKey));
    }
    
	/**
	 * 登录获取定制应用信息 【1912】
	 * 
	 * @param requestBody
	 * @param aesKey
	 * @return
	 */
	public String getAppOfSelfTailor(String requestBody, String aesKey) {
		logger.debug("登录获取应用信息,requestBody:{}", requestBody);
		logger.info("登录获取应用信息,requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		/** 用户id */
		String userId = requestJson.getString("userId");
		/** 请求客户端类型(Android、IOS) */
		String appType = requestJson.getString("appType");
		String username = requestJson.getString("username");// 用户名
		String clientVersion = requestJson.getString("clientVersion");// 客户端版本
		String clientModel = requestJson.getString("clientModel");// 客户端类型
		//JSONArray personalize = requestJson.getJSONArray("personalize");// 客户定制标志,必须传值
		logger.debug("登录获取应用信息(解析body),userId:{},appType:{}", userId, appType);
		/** 校验参数 */
		if (!StringUtils.checkParamNull(userId, appType)
				|| (!appType.equalsIgnoreCase("ios") && !appType.equalsIgnoreCase("android")))
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		/** 校验该用户是否存在 */
		ClientUserVO clientUserVO = clientUserInterface.findById(userId);
		logger.debug("登录获取应用信息(校验该用户是否存在),userId:{},clientUserVO:{}", userId, clientUserVO);
		if (null == clientUserVO)
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");
//		if (null == personalize)
//			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, "");
		/** 分页查询与该用户有关应用--调用服务化 */
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("EQ_userId", userId);
		conditions.put("EQ_appType", appType);
		Map<String, Object> squareMap = squareInterface.findSquareByUserRelation(1, 10000, conditions, null);
		logger.debug("登录获取应用信息(查询相关应用),userId:{},squareMap:{}", userId, JSON.toJSONString(squareMap));
		List<SquareVo> squareVos = new ArrayList<SquareVo>();
		if (null != squareMap && !squareMap.isEmpty())
			squareVos = (List<SquareVo>) squareMap.get("squareVos");

		/** 还需要遍历上一个集合，获取服务号的权限和服务号的菜单列表 */
		List<SquareInfoMemberVo> squareInfoMemberVos = getSquareInfoMemberInfo(squareVos, userId, false);
		logger.debug("登录获取应用信息(squareInfoMemberVos),squareInfoMemberVos:{}", JSON.toJSONString(squareInfoMemberVos));
		List<SquareInfoSelfVo> squareInfoSelfVos = getSquareInfoSelfVos(squareInfoMemberVos, clientUserVO.getTelNum(),
				userId);
		logger.debug("登录获取应用信息(获取服务号的权限和服务号的菜单列表),userId:{},squareInfoSelfVos:{}", userId,
				JSON.toJSONString(squareInfoSelfVos));

		/** 记录登陆客户端设备信息 */
		logger.info("wdw 记录登陆客户端设备信息 start  appType:{},clientVersion:{},clientModel:{},username:{}", appType,
				clientVersion, clientModel, username);
		// saveClientInfo(clientType, clientVersion, clientModel, username);
		saveClientInfo(appType, clientVersion, clientModel, username);
		logger.info("wdw 记录登陆客户端设备信息 end  appType:{},clientVersion:{},clientModel:{},username:{}", appType,
				clientVersion, clientModel, username);

		JSONObject resJson = new JSONObject();
		resJson.put("squareInfoList", squareInfoSelfVos);
		resJson.put("ftpIp", ParamConfig.ftp_ip);
		resJson.put("ftpPort", ParamConfig.ftp_port);
		resJson.put("ftpUsername", ParamConfig.ftp_username);
		resJson.put("ftpPassword", ParamConfig.ftp_pwd); 
		resJson.put("importAppList", getImportantServiceNo());//squareVos
		logger.debug("定制个性菜单返回信息，resJson{}",resJson.toJSONString());
		/** 加密返回body */
		return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), aesKey));
	

	}
    
    /**
     * 获取URL生成二维码扫描 1911
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    public String getTwoDimensionalCode(String requestBody, String aesKey) {
        logger.debug("获取URL生成二维码扫描,requestBody:{}", requestBody);
        //JSONObject requestJson = JSONObject.parseObject(requestBody);
        
        TwoDimensionalCodeVo twoDimensionalCodeVo = twoDimensionalCodeInterface.findByTwoDimensionalCodeId("20170410170101001");
                                                    
        logger.debug("获取URL生成二维码扫描,twoDimensionalCodeVo.getUrlData():{}", twoDimensionalCodeVo.getUrlData());
        
        JSONObject resJson = new JSONObject();
        resJson.put("twoDimensionalCode", twoDimensionalCodeVo.getUrlData());
       
        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), aesKey));
    }    
    /**
     * 记录登录客户端设备信息
     * 
     * @param clientType
     * @param clientVersion
     * @param clientModel
     * @param userName
     * @Description:
     */
    private void saveClientInfo(String clientType, String clientVersion, String clientModel, String userName) {
        clientVersionInterface.addLogonLog(userName, clientType, clientVersion, clientModel);
    }

    /**
     * 获取我的客户经理和冲浪新闻
     * 
     * @return
     * @Description:
     */
    private String getImportantServiceNo() {
        try {
            if (!ParaUtil.IMPORTANT_SERVICE_NO.equals(""))
                return ParaUtil.IMPORTANT_SERVICE_NO;
            JSONArray jsonArray = new JSONArray();
            logger.debug("获取我的客户经理和冲浪新闻(两个id),customerNo:{},surfNo:{}", ParamConfig.customer_no, ParamConfig.surf_no);
            SquareVo squareVoCus = squareInterface.findSquareById(ParamConfig.customer_no);
            logger.debug("获取我的客户经理和冲浪新闻(客户经理),customerNo:{},squareVoCus:{}", ParamConfig.customer_no, JSON.toJSONString(squareVoCus));
            if (null != squareVoCus) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appSign", "KHJL");
                jsonObject.put("appId", squareVoCus.getId());
                jsonObject.put("appName", squareVoCus.getName());
                jsonObject.put("appIcon", squareVoCus.getLogo());
                jsonArray.add(jsonObject);
            }
            SquareVo squareVoSur = squareInterface.findSquareById(ParamConfig.surf_no);
            logger.debug("获取我的客户经理和冲浪新闻(冲浪新闻),surfNo:{},squareVoSur:{}", ParamConfig.surf_no, JSON.toJSONString(squareVoSur));
            if (null != squareVoSur) {
                JSONObject jsonObjectSur = new JSONObject();
                jsonObjectSur.put("appSign", "CLXW");
                jsonObjectSur.put("appId", squareVoSur.getId());
                jsonObjectSur.put("appName", squareVoSur.getName());
                jsonObjectSur.put("appIcon", squareVoSur.getLogo());
                
                jsonObjectSur.put("reconfirm", squareVoSur.getReconfirm());//二次确认
                
                jsonArray.add(jsonObjectSur);
            }
            if (null != squareVoCus && null != squareVoSur)
                ParaUtil.IMPORTANT_SERVICE_NO = jsonArray.toJSONString();
            return jsonArray.toJSONString();
        } catch (Exception e) {
            logger.error("获取我的客户经理和冲浪新闻异常", e);
            return "";
        }

    }

    /**
     * 获取与己相关应用
     * 
     * @param squareVos
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    private List<SquareInfoSelfVo> getSquareInfoSelfVos(List<SquareInfoMemberVo> squareVos, String telNum, String userId) {
        List<SquareInfoSelfVo> squareInfoSelfVos = new ArrayList<SquareInfoSelfVo>();
        if (null != squareVos && !squareVos.isEmpty()) {
            for (SquareInfoMemberVo squareVo : squareVos) {
                SquareInfoSelfVo squareInfoSelfVo = transSquareInfoSelfVo(squareVo);
                if (null == squareInfoSelfVo)
                    continue;
                // 需要传参列表
                List<String> paramList = new ArrayList<String>();
                logger.debug("squareVo.getIsFreeLogin(){},squareVo.getType()", squareVo.getIsFreeLogin(), squareVo.getType());
                squareInfoSelfVo.setPersonalize(squareVo.getPersonalize());
                if (squareVo.getType() == 3) {// 服务号
                    Map<String, Object> conditions = new HashMap<String, Object>();
                    conditions.put("serviceno", squareVo.getId());
                    Map<String, Object> squareMap = squareInterface.findMenuRebackByServiceNo(conditions);
                    if (null != squareMap && !squareMap.isEmpty()) {
                        squareInfoSelfVo.setSquareMenuVos(transSquareMenuVo((List<SquareMenuVo>) squareMap.get("item"), telNum, userId));
                    }
                    if (null != squareVo.getTelNum() && squareVo.getTelNum() == 1)
                        paramList.add("FromUserTelNum");
                    if (null != squareVo.getUserId() && squareVo.getUserId() == 1)
                        paramList.add("FromUserId");
                    if (null != squareVo.getChannelNum() && squareVo.getChannelNum() == 1)
                        paramList.add("src");
                } else {// APK、H5
                    logger.debug("H5或者APK");
                    if (2 == squareVo.getIsFreeLogin()) {
                        paramList.add("FromUserTelNum");
                        paramList.add("FromUserId");
                        paramList.add("src");
                        paramList.add("token");
                    }
                    logger.debug("paramList{}", "参数" + paramList.toString());
                }
                try {
                    // 客户端是否传参：0-否，1-是
                    int flag = 0;
                    List<SquareParametersVo> list = squareInterface.findBySquareId(squareVo.getId());
                    //list.get(0).get
                    if (null != list && !list.isEmpty()) {
                        for (SquareParametersVo v : list) {
                        	
                            if ("OAaccount".equals(v.getParameValue())) {
                                OAaccountInfoVo oaVo = oAaccountInfoInterface.findByMemId(userId);
                                logger.debug("获取OA账号信息，oaVo{}", null == oaVo ? "null" : JSON.toJSONString(oaVo));
                                paramList.add(v.getParameValue() + "=" + (null == oaVo ? "" : oaVo.getOaaccount()));
                            } else
                                paramList.add(v.getParameValue());
                        }
                    }
                    if (!paramList.isEmpty()) {
                        flag = 1;
                        squareInfoSelfVo.setParamList(JSON.toJSONString(paramList));
                    }
                    squareInfoSelfVo.setParamFlag(flag);
                } catch (Exception e) {
                    logger.error("获取H5或APK自定义参数异常", e);
                    continue;
                }
                squareInfoSelfVos.add(squareInfoSelfVo);
            }
        }
        return squareInfoSelfVos;
    }

    private SquareInfoSelfVo transSquareInfoSelfVo(SquareInfoMemberVo squareVo) {
        try {
            SquareInfoSelfVo squareInfoMemberVo = new SquareInfoSelfVo();
            squareInfoMemberVo.setCreateTime(squareVo.getCreateTime());
            squareInfoMemberVo.setDescription(squareVo.getDescription());
            squareInfoMemberVo.setFtpUrl(squareVo.getFtpUrl());
            squareInfoMemberVo.setId(squareVo.getId());
            squareInfoMemberVo.setIsCancelAttention(squareVo.getIsCancelAttention());
            squareInfoMemberVo.setIsSystemApp(squareVo.getIsSystemApp());
            squareInfoMemberVo.setLogo(squareVo.getLogo());
            squareInfoMemberVo.setName(squareVo.getName());
            squareInfoMemberVo.setStartParameter(squareVo.getStartParameter());
            squareInfoMemberVo.setPackageName(squareVo.getPackageName());
            squareInfoMemberVo.setPreset(squareVo.getPreset());
            squareInfoMemberVo.setPublicImage1(squareVo.getPublicImage1());
            squareInfoMemberVo.setPublicImage2(squareVo.getPublicImage2());
            squareInfoMemberVo.setPublicImage3(squareVo.getPublicImage3());
            squareInfoMemberVo.setPublicImage4(squareVo.getPublicImage4());
            squareInfoMemberVo.setSize(squareVo.getSize());
            squareInfoMemberVo.setType(squareVo.getType());
            squareInfoMemberVo.setVersion(squareVo.getVersion());
            squareInfoMemberVo.setVersionCode(squareVo.getVersionCode());
            squareInfoMemberVo.setTelNum(squareVo.getTelNum());
            squareInfoMemberVo.setUserId(squareVo.getUserId());
            squareInfoMemberVo.setChannelNum(squareVo.getChannelNum());
            squareInfoMemberVo.setSort(squareVo.getSort());
            squareInfoMemberVo.setIsFreeLogin(squareVo.getIsFreeLogin());
            squareInfoMemberVo.setToken(squareVo.getToken());
            squareInfoMemberVo.setTokenUrl(squareVo.getTokenUrl());
            squareInfoMemberVo.setSecurityKey(squareVo.getSecurityKey());
            
            squareInfoMemberVo.setReconfirm(squareVo.getReconfirm());
            logger.info("二次确认reconfirm:{}",squareInfoMemberVo.getReconfirm());
            return squareInfoMemberVo;
        } catch (Exception e) {
            logger.error("transSquareInfoSelfVo转化异常", e);
            return null;
        }

    }

    /**
     * 获取应用详情 1905
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String getAppDetail(String requestBody, String aesKey) {
        logger.debug("获取应用详情,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 用户id */
        String userId = requestJson.getString("userId");
        /** 应用id */
        String appId = requestJson.getString("appId");

        logger.debug("获取应用详情(解析body),userId:{},appId:{}", userId, appId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, appId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        /** 校验该用户是否存在 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("获取应用详情(校验该用户是否存在),userId:{},clientUserVO:{}", userId, clientUserVO);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");

        /** 校验应用是否存在 */
        SquareVo squareVo = squareInterface.findSquareById(appId);
        logger.debug("获取应用详情(校验该应用是否存在),appId:{},squareVo:{}", appId, squareVo);
        if (null == squareVo)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1056, "");

        /** 根据squareVo查询该用户是否已关注以及是否允许取消关注等等信息 */
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("userId", userId);
        conditions.put("squareId", appId);
        Map<String, Object> appDetailMap = squareInterface.findSquareBySquareIdAndUserId(conditions);
        if (null == appDetailMap || appDetailMap.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1061, "");

        SquareVo squareVoRes = (SquareVo) appDetailMap.get("item");
        SquareInfoMemberVo squareInfoMemberVo = getSquareInfoMemberInfoDetail(squareVoRes, userId);
        JSONObject resJson = new JSONObject();
        resJson.put("appDetailInfo", squareInfoMemberVo);
        resJson.put("appIndustryInfo", appDetailMap.get("industry"));
        resJson.put("appLabelInfo", appDetailMap.get("label"));
        resJson.put("ftpIp", ParamConfig.ftp_ip);
        resJson.put("ftpPort", ParamConfig.ftp_port);
        resJson.put("ftpUsername", ParamConfig.ftp_username);
        resJson.put("ftpPassword", ParamConfig.ftp_pwd);
        logger.debug("获取应用详情(返回信息),appId:{},resJson:{}", appId, resJson.toJSONString());

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), aesKey));
    }

    /**
     * 应用列表VO封装详情
     * 
     * @param squareVos
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    private SquareInfoMemberVo getSquareInfoMemberInfoDetail(SquareVo squareVo, String userId) {
        SquareInfoMemberVo squareInfoMemberVo = new SquareInfoMemberVo();
        if (null != squareVo) {
            squareInfoMemberVo = transSquareInfoMemberVo(squareVo);
            /** 调用服务化查询是否关注 */
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("userId", userId);
            conditions.put("squareIds", squareVo.getId());
            Map<String, Object> squareRelationMap = squareInterface.findUsersSquareStatus(conditions);
            int attend = 0;
            if (null != squareRelationMap && !squareRelationMap.isEmpty()) {
                List<SquareRelationVo> squareRelationVos = (List<SquareRelationVo>) squareRelationMap.get("squareRelationVos");
                if (null != squareRelationVos && !squareRelationVos.isEmpty())
                    attend = squareRelationVos.get(0).getIsTrue();
            }
            squareInfoMemberVo.setIsAttend(attend);
            List<String> paramList = new ArrayList<String>();
            if (squareVo.getType() == 3) {
                // 回调模式是否传参：0-否，1-是
                // 需要传参列表

                if (null != squareVo.getTelNum() && squareVo.getTelNum() == 1)
                    paramList.add("FromUserTelNum");
                if (null != squareVo.getUserId() && squareVo.getUserId() == 1)
                    paramList.add("FromUserId");
                if (null != squareVo.getChannelNum() && squareVo.getChannelNum() == 1)
                    paramList.add("src");

            } else {// APK、H5
                logger.debug("H5或者APK");
                if (null != squareVo.getIsFreeLogin() && 2 == squareVo.getIsFreeLogin()) {
                    paramList.add("FromUserTelNum");
                    paramList.add("FromUserId");
                    paramList.add("src");
                    paramList.add("token");
                }
                logger.debug("paramList{}", "参数" + paramList.toString());
            }
            try {
                // 客户端是否传参：0-否，1-是
                int flag = 0;
                List<SquareParametersVo> list = squareInterface.findBySquareId(squareVo.getId());
                if (null != list && !list.isEmpty()) {
                    for (SquareParametersVo v : list) {
                        if ("OAaccount".equals(v.getParameValue())) {
                            OAaccountInfoVo oaVo = oAaccountInfoInterface.findByMemId(userId);
                            logger.debug("获取OA账号信息，oaVo{}", null == oaVo ? "null" : JSON.toJSONString(oaVo));
                            paramList.add(v.getParameValue() + "=" + (null == oaVo ? "" : oaVo.getOaaccount()));
                        } else
                            paramList.add(v.getParameValue());
                    }
                }
                if (!paramList.isEmpty()) {
                    flag = 1;
                    squareInfoMemberVo.setParamList(JSON.toJSONString(paramList));
                }
                squareInfoMemberVo.setParamFlag(flag);

            } catch (Exception e) {
                logger.error("获取H5或APK自定义参数异常", e);
                return squareInfoMemberVo;
            }
        }
        return squareInfoMemberVo;
    }

    private SquareInfoMemberVo transSquareInfoMemberVo(SquareVo squareVo) {
        try {
            SquareInfoMemberVo squareInfoMemberVo = new SquareInfoMemberVo();
            squareInfoMemberVo.setCreateTime(squareVo.getCreateTime());
            squareInfoMemberVo.setDescription(squareVo.getDescription());
            squareInfoMemberVo.setFtpUrl(squareVo.getFtpUrl());
            squareInfoMemberVo.setId(squareVo.getId());
            squareInfoMemberVo.setIsCancelAttention(null == squareVo.getIsCancelAttention() ? 0 : squareVo.getIsCancelAttention());
            squareInfoMemberVo.setIsSystemApp(null == squareVo.getIsSystemApp() ? 2 : squareVo.getIsSystemApp());
            squareInfoMemberVo.setLogo(squareVo.getLogo());
            squareInfoMemberVo.setName(squareVo.getName());
            squareInfoMemberVo.setStartParameter(squareVo.getStartParameter());
            squareInfoMemberVo.setPackageName(squareVo.getPackageName());
            squareInfoMemberVo.setPreset(squareVo.getPreset());
            squareInfoMemberVo.setPublicImage1(squareVo.getPublicImage1());
            squareInfoMemberVo.setPublicImage2(squareVo.getPublicImage2());
            squareInfoMemberVo.setPublicImage3(squareVo.getPublicImage3());
            squareInfoMemberVo.setPublicImage4(squareVo.getPublicImage4());
            squareInfoMemberVo.setSize(squareVo.getSize());
            squareInfoMemberVo.setType(squareVo.getType());
            squareInfoMemberVo.setVersion(squareVo.getVersion());
            squareInfoMemberVo.setVersionCode(squareVo.getVersionCode());
            Integer a = squareVo.getTelNum();
            squareInfoMemberVo.setTelNum(a);
            squareInfoMemberVo.setUserId(squareVo.getUserId());
            squareInfoMemberVo.setChannelNum(squareVo.getChannelNum());
            squareInfoMemberVo.setSort(squareVo.getSort());
            squareInfoMemberVo.setIsFreeLogin(squareVo.getIsFreeLogin());
            squareInfoMemberVo.setToken(squareVo.getToken());
            squareInfoMemberVo.setTokenUrl(squareVo.getTokenUrl());
            squareInfoMemberVo.setSecurityKey(null == squareVo.getSecurityKey() ? "" : squareVo.getSecurityKey());
            squareInfoMemberVo.setPersonalize(squareVo.getPersonalize());
            
            squareInfoMemberVo.setReconfirm(squareVo.getReconfirm());
            logger.info("二次确认第一1，reconfirm:{}",squareInfoMemberVo.getReconfirm());
            return squareInfoMemberVo;
        } catch (Exception e) {
            logger.error("transSquareInfoMemberVo转化异常,squareVo:{}", JSON.toJSONString(squareVo), e);
            return null;
        }
    }

    /**
     * 点击菜单推送消息 1906
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String menuPushMessage(String requestBody, String aesKey) {
        logger.debug("点击菜单推送消息,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 用户id */
        String userId = requestJson.getString("userId");
        /** 应用id */
        String appId = requestJson.getString("appId");
        /** 菜单id（当clickType为0时不为空） */
        String menuId = requestJson.getString("menuId");
        /** 触发类型：0为点击菜单；1为服务号输入框发送文本 */
        String clickType = requestJson.getString("clickType");
        /** 发送文本内容（当clickType为1时不为空） */
        String sendMessage = requestJson.getString("sendMessage");

        logger.debug("点击菜单推送消息(解析body),userId:{},appId:{},menuId:{},clickType:{},sendMessage:{}", userId, appId, menuId, clickType, sendMessage);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, appId, clickType) || (!"0".equals(clickType) && !"1".equals(clickType)) || ("0".equals(clickType) && "".equals(menuId))
                || ("1".equals(clickType) && "".equals(sendMessage)))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        /** 校验该用户是否存在 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("点击菜单推送消息(校验该用户是否存在),userId:{},clientUserVO:{}", userId, clientUserVO);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");

        /** 校验应用是否存在 */
        SquareVo squareVo = squareInterface.findSquareById(appId);
        logger.debug("点击菜单推送消息(校验该应用是否存在),appId:{},squareVo:{}", appId, squareVo);
        if (null == squareVo)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1056, "");

        if (clickType.equals("1")) {
            /** 推送消息“感谢您的留言”，入rocketMQ */
            final String toUserId = userId;
            final String serviceId = appId;
            new Thread() {
                public void run() {
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pushTextNewIm(serviceId, toUserId, "感谢您的留言！");
                }
            }.start();

        } else {
            /** 根据菜单id查询菜单 */
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("menuid", menuId);
            Map<String, Object> menuMap = squareInterface.findMenuRebackByMenuId(conditions);
            logger.debug("点击菜单推送消息(获取菜单信息),appId:{},menuMap:{}", appId, JSON.toJSONString(menuMap));
            if (null == menuMap || menuMap.isEmpty())
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1059, "");

            SquareMenuVo squareMenuVo = (SquareMenuVo) menuMap.get("item");
            /** 末级菜单标志位 1为是；2为否 */
            long lastMenu = squareMenuVo.getLastmenutype();
            if (lastMenu == 2L) {
                /** 获取菜单消息，并调用rocketMQ进行推送 */
                /** 获取菜单消息，并调用rocketMQ进行推送 */
                String menuMessage = squareMenuVo.getFixed_messages();
                /** rocketMQ推送 */
                final String toUserId = userId;
                final String serviceId = appId;
                final String content = menuMessage;
                new Thread() {
                    public void run() {
                        pushTextNewIm(serviceId, toUserId, content);
                    }
                }.start();

            } else {
                /** 菜单模式。1:普通模式，2，回调模式 */
                long serviceNoMenu = squareMenuVo.getServicenomenu();
                /** 菜单类型 */
                String menuType = squareMenuVo.getMenutype();
                /** 消息推送1:固定消息;2:图文消息 */
                String menuConn = squareMenuVo.getMenuconn();
                if (serviceNoMenu == 1L) {
                    if ("1".equals(menuType)) {
                        /** 获取菜单消息，并调用rocketMQ进行推送 */
                        String menuMessage = squareMenuVo.getFixed_messages();
                        /** rocketMQ推送 */
                        final String toUserId = userId;
                        final String serviceId = appId;
                        final String content = menuMessage;
                        new Thread() {
                            public void run() {
                                pushTextNewIm(serviceId, toUserId, content);
                            }
                        }.start();
                    } else {
                        if ("1".equals(menuConn)) {
                            /** 获取菜单消息，并调用rocketMQ进行推送 */
                            String menuMessage = squareMenuVo.getFixed_messages();
                            /** rocketMQ推送 */
                            final String toUserId = userId;
                            final String serviceId = appId;
                            final String content = menuMessage;
                            new Thread() {
                                public void run() {
                                    pushTextNewIm(serviceId, toUserId, content);
                                }
                            }.start();
                        } else {// 图文推送
                            /** rocketMQ推送 */
                            final String toUserId = userId;
                            final String serviceId = appId;
                            final String menu_id = menuId;
                            new Thread() {
                                public void run() {
                                    pushGraphicNewIm(serviceId, menu_id, toUserId);
                                }
                            }.start();
                        }
                    }
                } else {
                    if ("4".equals(menuType)) {// 链接
                        String urlAddress = squareMenuVo.getUrladdress();
                        serviceCallBackInterface.serviceCall(appId, clientUserVO.getTelNum(), userId, appId, "event", urlAddress, "VIEW");
                    } else {// 菜单KEY
                        String keyValue = squareMenuVo.getKeyvalue();
                        serviceCallBackInterface.serviceCall(appId, clientUserVO.getTelNum(), userId, appId, "event", keyValue, "CLICK");
                    }
                }

            }

        }

        JSONObject resJson = new JSONObject();
        resJson.put("sendDate", System.currentTimeMillis());

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), userId));
    }

    /**
     * 获取标签 1908
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    public String getLabelOfMember(String requestBody, String aesKey) {
        logger.debug("获取标签,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 用户id */
        String userId = requestJson.getString("userId");

        logger.debug("获取标签(解析body),userId:{}", userId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        /** 校验该用户是否存在 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("获取标签(校验该用户是否存在),userId:{},clientUserVO:{}", userId, clientUserVO);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");

        /** 全表查询label */
        List<LabelVo> labelVos = new ArrayList<LabelVo>();
        Map<String, Object> labelMap = labelInterface.findAllLabel();
        if (null != labelMap && !labelMap.isEmpty())
            labelVos = (List<LabelVo>) labelMap.get("item");

        JSONObject resJson = new JSONObject();
        resJson.put("labelInfoList", labelVos);
        logger.debug("获取标签(返回信息),userId:{},resJson:{}", userId, resJson.toJSONString());
        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), aesKey));
    }

    /**
     * 获取服务号菜单 1909
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    public String getMenuOfService(String requestBody, String aesKey) {
        logger.debug("获取服务号菜单,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 应用id */
        String appId = requestJson.getString("appId");

        String telNum = requestJson.getString("telNum");

        logger.debug("获取服务号菜单(解析body),appId:{}", appId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(appId, telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        SquareVo squareVo = squareInterface.findSquareById(appId);
        logger.debug("获取服务号菜单(查询服务号信息),appId:{},squareVo:{}", appId, squareVo);
        if (null == squareVo)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1056, "");

        if (null == squareVo.getType() || "".equals(squareVo.getType()) || squareVo.getType() != 3)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1062, "");

        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("serviceno", squareVo.getId());
        Map<String, Object> squareMap = squareInterface.findMenuRebackByServiceNo(conditions);
        logger.debug("获取服务号菜单(查询菜单信息),appId:{},squareVo:{},squareMap:{}", appId, squareVo, JSON.toJSONString(squareMap));
        List<SquareMenuVo> squareMenuVos = new ArrayList<SquareMenuVo>();
        if (null != squareMap && !squareMap.isEmpty())
            squareMenuVos = (List<SquareMenuVo>) squareMap.get("item");

        JSONObject resJson = new JSONObject();
        resJson.put("squareMenuInfo", transSquareMenuVo(squareMenuVos, telNum, ""));
        logger.debug("获取服务号菜单(返回信息),appId:{},resJson:{}", appId, resJson.toJSONString());
        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(resJson.toJSONString(), aesKey));
    }

    /**
     * 下载与卸载接口 1910
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String doDownloadOr(String requestBody, String aesKey) {
        logger.debug("下载与卸载接口,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 用户id */
        String userId = requestJson.getString("userId");
        /** 应用id */
        String appId = requestJson.getString("appId");

        /** 0为卸载，1为下载 */
        String optType = requestJson.getString("optType");

        logger.debug("下载与卸载接口(解析body),userId:{}", userId);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, appId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

        /** 校验该用户是否存在 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        logger.debug("下载与卸载接口(校验该用户是否存在),userId:{},clientUserVO:{}", userId, clientUserVO);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1055, "");

        SquareVo squareVo = squareInterface.findSquareById(appId);
        logger.debug("获取服务号菜单(查询服务号信息),appId:{},squareVo:{}", appId, squareVo);
        if (null == squareVo)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1056, "");

        if (squareVo.getType() != 1 && squareVo.getType() != 4)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1056, "");

        List<SquareDownloadRecordVo> downloadRecordVos = squareInterface.getSquareDownloadRecordListBySquareId(appId, userId);
        logger.debug("下载与卸载接口(查询下载记录),downloadRecordVos:{}", JSON.toJSONString(downloadRecordVos));

        if (optType.equals("0")) {
            if (null != downloadRecordVos && downloadRecordVos.size() == 1)
                squareInterface.deleteSquareDownloadRecord(downloadRecordVos.get(0).getId());
        } else {
            if (null == downloadRecordVos || downloadRecordVos.isEmpty()) {
                SquareDownloadRecordVo downloadRecordVo = new SquareDownloadRecordVo();
                downloadRecordVo.setId(UUID.randomUUID().toString());
                downloadRecordVo.setInsertTime(new Date());
                downloadRecordVo.setMemId(userId);
                downloadRecordVo.setSquareId(appId);
                squareInterface.saveSquareDownloadRecord(downloadRecordVo);

            }
        }

        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("userId", userId);
        conditions.put("optType", optType);
        conditions.put("squareId", appId);
        int resStatus = squareInterface.saveSquareRelation(conditions);
        logger.debug("下载与卸载接口[收藏关注](关注或取消关注返回结果),appId:{},resStatus:{},optType:{}", appId, resStatus, optType);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", "");
    }
    
    private List<SquareMenuVo> transSquareMenuVo(List<SquareMenuVo> squareMenuVos, String telNum, String userId) {
        List<SquareMenuVo> squareMenuVosRes = new ArrayList<SquareMenuVo>();
        if (null == squareMenuVos || squareMenuVos.isEmpty())
            return squareMenuVosRes;
        for (SquareMenuVo squareMenuVo : squareMenuVos) {
            try {
                SquareMenuVo squareMenuVoRes = new SquareMenuVo();
                squareMenuVoRes.setCorpid(squareMenuVo.getCorpid());

                squareMenuVoRes.setFixed_messages(null == squareMenuVo.getFixed_messages() ? "" : squareMenuVo.getFixed_messages());
                squareMenuVoRes.setGraphic_message(null == squareMenuVo.getGraphic_message() ? "" : squareMenuVo.getGraphic_message());
                squareMenuVoRes.setKeyvalue(null == squareMenuVo.getKeyvalue() ? "" : squareMenuVo.getKeyvalue());
                squareMenuVoRes.setLastmenutype(squareMenuVo.getLastmenutype());
                squareMenuVoRes.setLogin(squareMenuVo.getLogin());
                squareMenuVoRes.setMain_id(null == squareMenuVo.getMain_id() ? "" : squareMenuVo.getMain_id());
                squareMenuVoRes.setMenuconn(null == squareMenuVo.getMenuconn() ? "" : squareMenuVo.getMenuconn());
                squareMenuVoRes.setMenuid(null == squareMenuVo.getMenuid() ? "" : squareMenuVo.getMenuid());
                squareMenuVoRes.setMenuname(null == squareMenuVo.getMenuname() ? "" : squareMenuVo.getMenuname());
                squareMenuVoRes.setMenusortone(squareMenuVo.getMenusortone());
                squareMenuVoRes.setMenusortthree(squareMenuVo.getMenusortthree());
                squareMenuVoRes.setMenusorttwo(squareMenuVo.getMenusorttwo());
                squareMenuVoRes.setMenutype(null == squareMenuVo.getMenutype() ? "" : squareMenuVo.getMenutype());
                squareMenuVoRes.setParentid(null == squareMenuVo.getParentid() ? "" : squareMenuVo.getParentid());
                squareMenuVoRes.setServicenoid(null == squareMenuVo.getServicenoid() ? "" : squareMenuVo.getServicenoid());
                squareMenuVoRes.setServicenomenu(squareMenuVo.getServicenomenu());
                String urlAddress = null == squareMenuVo.getUrladdress() ? "" : squareMenuVo.getUrladdress();
                if ("1".equals(squareMenuVo.getLogin()) || 1 == squareMenuVo.getLogin()) {
                    if (urlAddress.indexOf("?") != -1) {
                        urlAddress += "&src=v&" + "FromUserTelNum=" + telNum + "&FromUserId=" + userId;
                    } else {
                        urlAddress += "?src=v&" + "FromUserTelNum=" + telNum + "&FromUserId=" + userId;
                    }
                }
                squareMenuVoRes.setUrladdress(urlAddress);
                squareMenuVosRes.add(squareMenuVoRes);
            } catch (Exception e) {
                continue;
            }

        }
        return squareMenuVosRes;
    }

    /**
     * 判断该用户是否关注该应用
     * 
     * @param appId
     * @param userId
     * @return true为关注，false为未关注
     * @Description:
     */
    @SuppressWarnings("unchecked")
    private boolean checkAppIsAttend(String appId, String userId) {
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("userId", userId);
        conditions.put("squareIds", appId);
        Map<String, Object> squareRelationMap = squareInterface.findUsersSquareStatus(conditions);
        if (null != squareRelationMap && !squareRelationMap.isEmpty()) {
            List<SquareRelationVo> squareRelationVos = (List<SquareRelationVo>) squareRelationMap.get("squareRelationVos");
            if (null != squareRelationVos && !squareRelationVos.isEmpty())
                if (squareRelationVos.get(0).getIsTrue() == 1)
                    return true;
        }
        return false;
    }

    public void pushTextNewIm(String serviceId, String telNum, String content) {
        SquareVo squareVo = getPushObject(serviceId, telNum);
        if (null != squareVo) {
            String serviceName = squareVo.getName();
            String serviceLogo = squareVo.getLogo();
            pushMenuTextMsg(serviceName, serviceLogo, serviceId, telNum, content, squareVo.getIsSystemApp());
        }
    }

    public void pushGraphicNewIm(String serviceId, String menuId, String cellPhone) {
        SquareVo squareVo = getPushObject(serviceId, cellPhone);
        if (null != squareVo) {
            String serviceName = squareVo.getName();
            String serviceLogo = squareVo.getLogo();
            pushMenuGraphicMsg(menuId, serviceName, serviceLogo, serviceId, cellPhone);
        }
    }

    public void pushMenuTextMsg(String serviceName, String serviceLogo, String serviceId, String cellPhone, String content, int isSystem) {
        // 入redis
        WorkBenchAction message = servicePushTextNew(serviceName, serviceLogo, serviceId, content, isSystem);
        RedisAction ra = new RedisAction();
        ra.setMessage(message);
        ra.setSource(RocketMqUtil.SOURCE);
        ra.setHead(RocketMqUtil.SQUARE_HEAD);
        ra.setCreateTime(dateFormat.format(new Date()));
        try {
            long msgId = actionRecordUtil.save(ra);
            MsgPushUtil.sendMenuMsg(serviceId, msgId, cellPhone, message, 2, "", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void pushMenuGraphicMsg(String menuId, String serviceName, String serviceLogo, String serviceId, String cellPhone) {
        WorkBenchAction message = servicePushGraphicNew(menuId, serviceName, serviceLogo, serviceId);
        RedisAction ra = new RedisAction();
        ra.setMessage(message);
        ra.setSource(RocketMqUtil.SOURCE);
        ra.setHead(RocketMqUtil.SQUARE_HEAD);
        ra.setCreateTime(dateFormat.format(new Date()));
        try {
            long msgId = actionRecordUtil.save(ra);
            MsgPushUtil.sendMenuMsg(serviceId, msgId, cellPhone, message, 2, "", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 调用服务推送文本
     * 
     * @param mainId
     * @param serviceName
     * @param serviceLogo
     * @param serviceId
     * @param target_type
     * @param target
     */
    public WorkBenchAction servicePushTextNew(String serviceName, String serviceLogo, String serviceId, String content, int isSystem) {
        WorkBenchAction action = new WorkBenchAction();
        ServicePush push = new ServicePush();

        push.setMsgUUID(UUID.randomUUID().toString());
        push.setServiceName(serviceName);
        push.setContent(content);
        push.setMsgType("text");
        push.setServiceId(serviceId);
        push.setCreateTime(System.currentTimeMillis());
        push.setServiceType("");
        push.setLogoAddr(serviceLogo);
        push.setArticleCount(0);
        push.setIsSystemApp(isSystem);
        action.setContent(JSON.toJSONString(push));
        action.setType(1);

        return action;
    }

    /**
     * 调用服务推送图文
     * 
     * @param mainId
     * @param serviceName
     * @param serviceLogo
     * @param serviceId
     * @param target_type
     * @param target
     */
    public WorkBenchAction servicePushGraphicNew(String menuId, String serviceName, String serviceLogo, String serviceId) {
        WorkBenchAction action = new WorkBenchAction();
        ServicePush push = new ServicePush();
        List<NewsItem> newsItemList = new ArrayList<NewsItem>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("menuid", menuId);
        Map<String, Object> menuMap = squareInterface.findMenuRebackByMenuId(map);
        SquareMenuVo squareMenuVo = (SquareMenuVo) menuMap.get("item");
        List<GraphicSourceVo> sourceVos = squareInterface.findgraphicByMainId(squareMenuVo.getMain_id());
        for (int i = 0; i < sourceVos.size(); i++) {
            NewsItem newsItem = new NewsItem();
            String graphic_source_type = sourceVos.get(i).getGraphicSourceType();
            String htmlUrl = "";
            if (graphic_source_type.equals("1")) {
                htmlUrl = sourceVos.get(i).getConnectUrl();
            } else {
                String url = ParamConfig.GRAPHIC_SOURCE_URL;
                if (org.apache.commons.lang3.StringUtils.isEmpty(url)) {
                    logger.debug("获取GRAPHIC_SOURCE_URL值为空,图文推送无法进行");
                    return null;
                }
                htmlUrl = url + "?gid=" + sourceVos.get(i).getId() + "&serviceid=" + serviceId;
            }
            String titlePicUrl = "";
            String picurl = sourceVos.get(i).getGraphicPic() == null ? "" : sourceVos.get(i).getGraphicPic();
            if (picurl.startsWith("/group")) {
                titlePicUrl = ParamConfig.file_server_url + "/" + picurl;
            } else {
                titlePicUrl = ParamConfig.nginx_address + "/" + picurl;
            }
            String titleDesc = sourceVos.get(i).getGraphicTitle();
            if ("1".equals(sourceVos.get(i).getIsMain())) {
                push.setMainTitle(titleDesc);

            }
            newsItem.setDescription("");
            newsItem.setId(String.valueOf(sourceVos.get(i).getId()));
            newsItem.setPicUrl(titlePicUrl);
            newsItem.setTitle(titleDesc);
            newsItem.setUrl(htmlUrl);
            newsItemList.add(newsItem);
        }

        push.setMsgUUID(UUID.randomUUID().toString());
        push.setServiceName(serviceName);
        push.setContent(newsItemList);
        push.setMsgType("news");
        push.setServiceId(serviceId);
        push.setCreateTime(System.currentTimeMillis());
        push.setServiceType("");
        push.setLogoAddr(serviceLogo);
        push.setArticleCount(sourceVos.size());
        action.setContent(JSON.toJSONString(push));
        action.setType(1);

        return action;
    }

    /**
     * 获取图文推送接收者
     * 
     * @param serviceId
     * @param corpId
     * @return
     */
    public SquareVo getPushObject(String serviceId, String corpId) {
        return squareInterface.findSquareById(serviceId);
    }
}
