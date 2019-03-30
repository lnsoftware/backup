/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

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
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.UcsNodeUtil;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendSmsInterface;
import com.royasoft.vwt.soa.business.invite.api.interfaces.InviteShareInterface;
import com.royasoft.vwt.soa.business.invite.api.vo.InviteShareVo;
import com.royasoft.vwt.soa.integral.api.interfaces.IntegralInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.IntegralSpendInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.SignInterface;
import com.royasoft.vwt.soa.integral.api.vo.IntegralActionVo;
import com.royasoft.vwt.soa.integral.api.vo.IntegralLogVo;
import com.royasoft.vwt.soa.integral.api.vo.IntegralRuleVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfIntegralcommodityVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfOrderItemVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfOrderVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfPayPointsLogVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfRedeemCodeVo;
import com.royasoft.vwt.soa.sundry.vprivilege.api.interfaces.VprivilegeInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;

/**
 * 签到积分处理类
 *
 * @Author:MB
 * @Since:2016年3月24日
 */
@Scope("prototype")
@Service
public class IntegralSignService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(IntegralSignService.class);

    private final String nameSpace = "ROYASOFT:VWT:DOWNLOAD:CODE:MOBILE:";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat sdf_day = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf_month = new SimpleDateFormat("yyyy-MM");

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private SignInterface signInterface;

    @Autowired
    private IntegralInterface integralInterface;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Autowired
    private VprivilegeInterface vprivilegeInterface;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private SendSmsInterface sendSmsInterface;

    @Autowired
    private CorpInterface corpInterface;

    @Autowired
    private IntegralUtil integralUtil;

    @Autowired
    private InviteShareInterface inviteShareInterface;

    @Autowired
    private IntegralSpendInterface integralSpendInterface;

    private final SimpleDateFormat dateFormatDetail = new SimpleDateFormat("yyyy-MM-dd");

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.integral_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("签到积分处理类(入口),function_id:{},user_id:{},request_body:{},tel_number:{}", function_id, user_id, request_body, tel_number);
                    /***************************** 业务逻辑处理 *********************************************/

                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果
                    switch (function_id) {
                        case FunctionIdConstant.UQERYSIGNDATE:
                            resInfo = getSignDate(request_body, user_id);
                            break;
                        case FunctionIdConstant.SIGNIN:
                            resInfo = userSignIn(request_body);
                            break;
                        case FunctionIdConstant.MONTHINTEGRALAES:
                            resInfo = getMonthIntegral(request_body, user_id);
                            break;
                        case FunctionIdConstant.MONTHINTEGRAL:
                            resInfo = getMonthIntegralForH5(request_body);
                            break;
//                        case FunctionIdConstant.INTAKEINTEGRAL:
//                            resInfo = getThreeMonthTotalIntegralForH5(request_body);
//                            break;
                        case FunctionIdConstant.INTAKEMOREINTEGRAL:
                            resInfo = getMonthIntegralPageForH5(request_body);
                            break;
                        case FunctionIdConstant.LOADVPRIVILEGE:
                            resInfo = toVPrivilege(request_body);
                            break;
                        case FunctionIdConstant.VPRIVILEGECLICK:
                            resInfo = addVPrivilege(request_body);
                            break;
                        case FunctionIdConstant.DOWNLOADCODE:// 下载页面获取验证码
                            resInfo = getCodeAtDownloadPage(request_body);
                            break;
                        case FunctionIdConstant.DOWNLOADRECORD:// 下载页面记录下载信息
                            resInfo = recordDownload(request_body);
                            break;
                        case FunctionIdConstant.INVITELIST:// 获取邀请好友列表
                            resInfo = getShareList(user_id, tel_number, request_body);
                            break;
                        /** 以下注释由于火龙果活动已经结束 2016.4.14 马斌 */
                        // case FunctionIdConstant.CHECKDRAGONFRUIT:
                        // resInfo = getDragonFruitByTelNum(request_body);
                        // break;
                        // case FunctionIdConstant.DRAGONFRUITNOTGET:
                        // resInfo = getDragonFruitNoDoToView(request_body);
                        // break;
                        // case FunctionIdConstant.GETDRAGONFRUIT:
                        // resInfo = updateDragonFruit(request_body);
                        // break;
                        case FunctionIdConstant.pay_getAddPointDateList:
                            resInfo = getAddPointDateList(request_body);
                            break;
                        case FunctionIdConstant.pay_getDateFormatOrderList:
                            resInfo = getDateFormatOrderList(request_body);
                            break;
                        case FunctionIdConstant.pay_getExchange:
                            resInfo = getExchange(request_body);
                            break;
                        case FunctionIdConstant.pay_getIntegralcommodityDateil:
                            resInfo = getIntegralcommodityDateil(request_body);
                            break;
                        case FunctionIdConstant.pay_getIntegralcommodityList:
                            resInfo = getIntegralcommodityList(request_body);
                            break;
                        case FunctionIdConstant.pay_getOrderDetailList:
                            resInfo = getOrderDetailList(request_body);
                            break;
                        case FunctionIdConstant.pay_getPayPointDateList:
                            resInfo = getPayPointDateList(request_body);
                            break;
                        case FunctionIdConstant.pay_getPayPointDetailList:
                            resInfo = getPayPointDetailList(request_body);
                            break;
                        case FunctionIdConstant.pay_getRedeemCodeCommodity:
                            resInfo = getRedeemCodeCommodity(request_body);
                            break;
                        case FunctionIdConstant.pay_getRedeemCodeOrder:
                            resInfo = getRedeemCodeOrder(request_body);
                            break;
                        case FunctionIdConstant.pay_validataPoint:
                            resInfo = validataPoint(request_body);
                            break;
                        default:
                            break;
                    }
                    logger.debug("签到积分处理类(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    // continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("签到积分业务逻辑处理异常", e);
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
     * 获取当月已签到的日期
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String getSignDate(String requestBody, String userId) {
        logger.debug("获取当月已签到的日期,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        /** 手机号码 */
        String telNum = requestJson.getString("telNum");

        logger.debug("获取当月已签到的日期(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1039, "");

        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
        logger.debug("获取当月已签到的日期(校验用户是否存在),telNum:{},clientUserVOs:{}", telNum, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1040, "");

        /** Calendar时间类 */
        Calendar calendar = Calendar.getInstance();

        /** 获取当前的年份 */
        String currentYear = calendar.get(Calendar.YEAR) + "";

        /** 获取当前的月份数 */
        String currentMonth = (calendar.get(Calendar.MONTH) + 1) + "";

        /** 获取当前的日期数 */
        String currentDay = calendar.get(Calendar.DATE) + "";

        /** 根据月份查询本月当前所有已签到的日期 */
        List<String> signDateList = signInterface.findCurrentMonthSignInfo(telNum, new Date());
        String signInfos = "";
        logger.debug("获取当月已签到的日期,telNum:{},已签到的日期集合:{}", telNum, signDateList);
        if (null != signDateList && !signDateList.isEmpty()) {
            /** 拼接日期的字符串 */
            StringBuffer signInfo = new StringBuffer();
            /** 遍历集合，用#号拼接 */
            for (String day : signDateList) {
                signInfo.append(day + "#");
            }
            if (null != signInfo && !"".equals(signInfo))
                signInfos = signInfo.deleteCharAt(signInfo.length() - 1).toString();
        }
        /** 根据当前日期是否已签到 1 已签到 0未签到 */
        boolean flag = signInterface.checkIsSigned(telNum, new Date());

        int haveSigned = 0;
        /** 判断签到的日期中是否有今天 */
        if (flag)
            haveSigned = 1;

        logger.debug("获取当前日期是否已签到,telNum:{},是否已签到:{}", telNum, haveSigned);

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("signInfo", signInfos);
        bodyJson.put("haveSigned", haveSigned);
        bodyJson.put("currentYear", currentYear);
        bodyJson.put("currentMonth", currentMonth);
        bodyJson.put("currentDay", currentDay);
        bodyJson.put("currentDate", dateFormatDetail.format(new Date()));
        /** 加密返回body */
        // String userKey = getUserKeyByTelNum(userId);
        // if (null == userKey)
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
        String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userId);
        return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 签到
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String userSignIn(String requestBody) {
        logger.debug("签到,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        logger.debug("签到(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1035, "");

        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
        logger.debug("签到(校验用户是否存在),telNum:{},clientUserVOs:{}", telNum, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1036, "");

        Date currentDate = new Date();
        boolean isSigned = signInterface.checkIsSigned(telNum, currentDate);
        logger.debug("签到(校验用户是否已签到),telNum:{},isSigned:{}", telNum, isSigned);
        if (isSigned)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1037, "");

        boolean res = signInterface.saveSignRecord(telNum);
        logger.debug("签到(签到返回结果),telNum:{},res:{}", telNum, res);
        if (!res)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1038, "");

        logger.debug("签到(签到成功),telNum:{}", telNum);

        // Map<String, Object> conditions = new HashMap<String, Object>();
        // Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();

        integralUtil.integralSigns(telNum, "30");

        // conditions.put("EQ_actionType", "30");
        // sortMap.put("actionType", true);
        // List<IntegralActionVo> integralActionVos = integralInterface.findIntegralActionByCondition(conditions, sortMap);
        // logger.debug("签到(获取签到相关积分行为),telNum:{},integralActionVos:{}", telNum, JSONObject.toJSONString(integralActionVos));
        // if (null != integralActionVos && integralActionVos.size() == 1) {
        // IntegralActionVo integralActionVo = integralActionVos.get(0);
        // String ruleRowId = integralActionVo.getRuleRowId();
        //
        // int signCount = Integer.valueOf(String.valueOf(integralActionVo.getPointQuantity()));
        // saveIntegralLog("", signCount, signCount, "", currentDate, "", integralActionVo.getActionType(), telNum, clientUserVO.getId());
        // conditions.clear();
        // sortMap.clear();
        // conditions.put("start_time_startDate", dateFormatDetailSecond.format(new Date()));
        // conditions.put("end_time_endDate", dateFormatDetailSecond.format(new Date()));
        // conditions.put("EQ_rowId", ruleRowId);
        // sortMap.put("rowId", true);
        // List<IntegralRuleVo> integralRuleVos = integralInterface.findIntegralRuleByCondition(conditions, sortMap);
        // logger.debug("签到(获取签到相关积分规则),telNum:{},integralRuleVos:{}", telNum, JSONObject.toJSONString(integralRuleVos));
        // if (null != integralRuleVos && integralRuleVos.size() == 1) {
        // IntegralRuleVo integralRuleVo = integralRuleVos.get(0);
        // if ("1".equals(integralRuleVo.getIslimit())) {// 封顶
        // if (integralRuleVo.getIsOverlimit().equals("0")) {// 赠送
        // if (integralRuleVo.getIsMonth().equals("0")) {// 不按月
        // if (integralRuleVo.getIsContinue().equals("0")) {// 连续
        // List<IntegralLogVo> integralLogVos = integralInterface.findIntegralLogMonthByTypeTelnum(telNum, "30");
        //
        // } else {// 不连续
        //
        // }
        // } else {// 按月
        // int currentMonthDay = getCurrentMonthLastDay();
        // if (checkIsLastOfMonth(currentMonthDay)) {
        // List<String> monthSign = signInterface.findCurrentMonthSignInfo(telNum, currentDate);
        // if (null != monthSign && monthSign.size() == currentMonthDay) {
        // int addCount = Integer.valueOf(String.valueOf(integralRuleVo.getLimitQuantity())) - currentMonthDay
        // * Integer.valueOf(String.valueOf(integralActionVo.getPointQuantity()));
        // saveIntegralLog("", addCount, addCount, "", currentDate, "", "70", telNum, clientUserVO.getId());
        // }
        // }
        // }
        // } else {// 封顶
        // if (integralRuleVo.getIsMonth().equals("0")) {// 不按月
        // if (integralRuleVo.getIsContinue().equals("0")) {// 连续
        //
        // } else {// 不连续
        //
        // }
        // } else {// 按月
        //
        // }
        // }
        // } else {// 不封顶
        //
        // }
        // }
        //
        // }

        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 获取当月总积分 1308
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String getMonthIntegral(String requestBody, String userId) {
        logger.debug("获取当月总积分,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        logger.debug("获取当月总积分(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1042, "");

        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
        logger.debug("获取当月总积分(校验用户是否存在),telNum:{},clientUserVOs:{}", telNum, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1043, "");

        Date currentDate = new Date();
//        int monthIntegral = integralInterface.findMonthTotalIntegral(currentDate, telNum);
        int monthIntegral = integralSpendInterface.getUserCurrPoints(telNum);
        logger.debug("获取当月总积分(获取月积分),telNum:{},monthIntegral:{}", telNum, monthIntegral);

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("totalIntegral", monthIntegral);
        /** 加密返回body */
        // String userKey = getUserKeyByTelNum(userId);
        // if (null == userKey)
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
        String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userId);

        return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 获取当月总积分 1304 HTML5
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String getMonthIntegralForH5(String requestBody) {
        logger.debug("获取当月总积分HTML5,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        logger.debug("获取当月总积分HTML5(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1042, "");

        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
        logger.debug("获取当月总积分HTML5(校验用户是否存在),telNum:{},clientUserVOs:{}", telNum, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1043, "");

        Date currentDate = new Date();
        int monthIntegral = integralSpendInterface.getUserCurrPoints(telNum);// integralInterface.findMonthTotalIntegral(currentDate, telNum);
        logger.debug("获取当月总积分HTML5(获取月积分),telNum:{},monthIntegral:{}", telNum, monthIntegral);

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("totalIntegral", monthIntegral);

        return ResponsePackUtil.buildPack("0000", bodyJson.toJSONString());
    }

    /**
     * 获取三个月积分 1306 HTML5
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
//    public String getThreeMonthTotalIntegralForH5(String requestBody) {
//        logger.debug("获取三个月积分HTML5,requestBody:{}", requestBody);
//        JSONObject requestJson = JSONObject.parseObject(requestBody);
//        String telNum = requestJson.getString("telNum");
//
//        logger.debug("获取三个月积分HTML5(解析body),telNum:{}", telNum);
//
//        /** 校验参数 */
//        if (null == telNum || "".equals(telNum))
//            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1042, "");
//
//        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
//        logger.debug("获取三个月积分HTML5(校验用户是否存在),telNum:{},clientUserVOs:{}", telNum, JSONObject.toJSONString(clientUserVOs));
//        if (null == clientUserVOs || clientUserVOs.isEmpty())
//            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1043, "");
//
//        JSONObject resInfo = new JSONObject();
//
//        /** 前三个月积分记录 */
//        JSONObject Takes = integralInterface.findThreeMonthTotalIntegral(telNum);
//        /** 当前积分总数 */
//        int integralSum = integralSpendInterface.getUserCurrPoints(telNum);
//
//        resInfo.put("integralSum", integralSum);
//
//        resInfo.put("Takes", Takes);
//        logger.debug("获取三个月积分HTML5(返回),telNum:{},resInfo:{}", telNum, resInfo.toJSONString());
//
//        return ResponsePackUtil.buildPack("0000", resInfo.toJSONString());
//    }

    /**
     * 分页获取指定月份详细收入积分 1307 HTML5
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String getMonthIntegralPageForH5(String requestBody) {
        logger.debug("分页获取指定月份详细收入积分HTML5,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        String Month = requestJson.getString("Month");
        String last_rowkey = requestJson.getString("page");
        int rows = requestJson.getInteger("rows");

        logger.debug("分页获取指定月份详细收入积分HTML5(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(telNum, Month))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1042, "");

        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
        logger.debug("分页获取指定月份详细收入积分HTML5(校验用户是否存在),telNum:{},clientUserVOs:{}", telNum, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1043, "");

        Date date = null;
        try {
            date = dateFormat.parse(Month);
        } catch (ParseException e) {
            date = new Date();
        }

        JSONObject Takes = integralInterface.findMonthIntegralOfPage(date, telNum, last_rowkey, rows);

        logger.debug("分页获取指定月份详细收入积分HTML5(Takes),telNum:{},Takes:{}", telNum, Takes.toJSONString());

        @SuppressWarnings("unchecked")
        List<IntegralLogVo> infos = (List<IntegralLogVo>) Takes.get("integralLogVos");

        String lastRowId = Takes.getString("lastRowId");
        List<IntegralLogVo> infosAfter = new ArrayList<IntegralLogVo>();
        if (!lastRowId.equals(last_rowkey)) {
            if (null != infos && !infos.isEmpty()) {
                Map<String, Object> condition = new HashMap<String, Object>();
                Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
                condition.put("EQ_delFlag", "0");
                sortMap.put("delFlag", true);
                List<IntegralActionVo> integralActionVos = integralInterface.findIntegralActionByCondition(condition, sortMap);
                Map<String, String> actionMap = new HashMap<String, String>();
                if (null != integralActionVos && integralActionVos.size() > 0) {
                    for (IntegralActionVo integralActionVo : integralActionVos) {
                        actionMap.put(integralActionVo.getActionType(), integralActionVo.getActionName());
                    }
                }

                for (IntegralLogVo integralLogVo : infos) {
                    integralLogVo.setRuleType(actionMap.get(integralLogVo.getRuleType()));
                    infosAfter.add(integralLogVo);
                }

            }
        }
        JSONObject resInfo = new JSONObject();
        resInfo.put("Takes", infosAfter);
        resInfo.put("page", lastRowId);
        resInfo.put("rows", rows);
        logger.debug("分页获取指定月份详细收入积分HTML5(返回),telNum:{},resInfo:{}", telNum, resInfo.toJSONString());

        return ResponsePackUtil.buildPack("0000", resInfo.toJSONString());
    }

    /**
     * 跳转v特权页面
     * 
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    public String toVPrivilege(String requestBody) {
        JSONObject jsonObject = new JSONObject();
        // 标题列表
        List<Map<String, Object>> titleList = null;
        // 图片列表
        List<Map<String, Object>> pictureList = null;
        // 标题缓存
        String titleListJson = ParaUtil.titleListJson;
        // 列表缓存
        String pictureListJson = ParaUtil.pictureListJson;
        logger.debug("v特权页面请求加载缓存数据,titleListJson:{},pictureListJson:{}", titleListJson, pictureListJson);
        if (null == titleListJson || "".equals(titleListJson) || null == pictureListJson || "".equals(pictureListJson)) {
            try {
                // 获取v特权标题列表
                titleList = vprivilegeInterface.getAllVPrivilegeTitile();
                // 获取v特权图片列表
                pictureList = vprivilegeInterface.getAllVPrivilegePictures();
                if (null != titleList && null != pictureList && titleList.size() > 0 && pictureList.size() > 0) {
                    ParaUtil.titleListJson = JSONObject.toJSONString(titleList);
                    ParaUtil.pictureListJson = JSONObject.toJSONString(pictureList);
                    logger.debug("v特权页面请求加载数据存入缓存,titleListJson:{},pictureListJson:{}", titleListJson, pictureListJson);
                }
            } catch (Exception e) {
                logger.error("v特权页面请求加载数据服务化失败", e);
            }
        } else {
            // 读取缓存的格式
            List<Map<String, Object>> titleList1 = new ArrayList<Map<String, Object>>();
            titleList = JSONObject.parseObject(titleListJson, titleList1.getClass());
            pictureList = JSONObject.parseObject(pictureListJson, titleList1.getClass());
        }
        logger.debug("v特权页面请求加载数据,titleList:{},pictureList:{}", titleList, pictureList);
        if (null != titleList && null != pictureList && titleList.size() > 0 && pictureList.size() > 0) {
            jsonObject.put("result", "200");
            jsonObject.put("titleList", titleList);
            jsonObject.put("pictureList", pictureList);
        } else {
            jsonObject.put("result", "-1");
        }

        return ResponsePackUtil.buildPack("0000", jsonObject.toJSONString());
    }

    /**
     * 记录V特权点击数
     * 
     * @return
     * @Description:
     */
    public String addVPrivilege(String requestBody) {
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String titleId = requestJson.getString("titleId");
        String picId = requestJson.getString("picId");
        String userId = requestJson.getString("userId");
        String telNum = requestJson.getString("telNum");

        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack("0000", "");
        String corpId = clientUserVO.getCorpId();
        logger.debug("获取大标题id,图片id,企业id,手机号,记录V特权点击数,titleId:{},picId:{},corpId:{},telNum:{}", titleId, picId, corpId, telNum);
        if (null != titleId && !"".equals(titleId) && null != picId && !"".equals(picId) && null != corpId && !"".equals(corpId) && null != telNum && !"".equals(telNum)) {
            try {
                vprivilegeInterface.saveVPrivilegeHis(titleId, picId, telNum, corpId);
            } catch (Exception e) {
                logger.error("记录V特权点击数服务化异常", e);
            }
        }
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 查询该用户是否已领取火龙果奖券
     * 
     * @author WZY
     * @Date 2016年3月2日
     * 
     * @param telNum
     * @return
     */
    /*
     * public String getDragonFruitByTelNum(String requestBody) { List<Map<String, Object>> gdklist = new ArrayList<Map<String, Object>>();
     * logger.debug("查询该用户是否已领取火龙果奖券,requestBody:{}", requestBody); JSONObject requestJson = JSONObject.parseObject(requestBody); String telNum =
     * requestJson.getString("telNum"); logger.debug("查询该用户是否已领取火龙果奖券,telNum:{}", telNum);
     *//** 校验参数 */
    /*
     * if (null == telNum || "".equals(telNum)) return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, "");
     *//** 获取该用户兑换劵的GDK和兑换劵编码 */
    /*
     * logger.debug("查询该用户是否已领取火龙果奖券,telNum:{}", telNum); gdklist = vprivilegeInterface.getDragonFruitByTelNum(telNum);
     * logger.debug("查询该用户是否已领取火龙果奖券,list:{}", gdklist); JSONObject resInfo = new JSONObject(); resInfo.put("gdklist", gdklist); return
     * ResponsePackUtil.buildPack("0000", resInfo.toJSONString()); }
     */

    /**
     * 获取所有未领取的奖券
     * 
     * @author WZY
     * @Date 2016年3月2日
     * 
     * @param
     * @return
     */
    /*
     * public String getDragonFruitNoDoToView(String requestBody) { logger.debug("获取所有未领取的奖券,requestBody:{}", requestBody); List<Map<String, Object>>
     * list = databaseInterface.getDragonFruitNoDo(); logger.debug("获取所有未领取的奖券,list:{}", list); JSONObject resInfo = new JSONObject();
     * resInfo.put("list", list); return ResponsePackUtil.buildPack("0000", resInfo.toJSONString()); }
     */

    /**
     * 领取火龙果奖券
     * 
     * @author WZY
     * @Date 2016年3月2日
     * 
     * @param telNum 用户手机号码
     * @param coding 奖券编码
     * @param createtime 创建时间
     * 
     * @return
     */
    /*
     * public String updateDragonFruit(String requestBody) { List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); List<Map<String,
     * Object>> gdklist = new ArrayList<Map<String, Object>>(); logger.debug("领取火龙果奖券,requestBody:{}", requestBody); JSONObject requestJson =
     * JSONObject.parseObject(requestBody); String telNum = requestJson.getString("telNum"); if (null == telNum || "".equals(telNum)) return
     * ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1002, ""); logger.debug("领取火龙果奖券,telNum:{}", telNum); SimpleDateFormat sdf = new
     * SimpleDateFormat("yyyy-MM-dd"); String createtime = sdf.format(new Date());
     *//** 获取剩余火龙果 */
    /*
     * list = databaseInterface.getDragonFruitNoDo(); JSONObject resInfo = new JSONObject();
     *//** 判断火龙果是否剩余 */
    /*
     * if (0 == list.size()) { resInfo.put("gameOver", "0"); } else { for (Map<String, Object> map : list) { String coding = ""; if (map.get("coding")
     * instanceof String) { coding = (String) map.get("coding"); }
     *//** 为用户添加火龙果编号 */
    /*
     * boolean isok = databaseInterface.updateDragonFruit(telNum, coding, createtime); // 添加成功后查出用户GDK gdklist =
     * databaseInterface.getDragonFruitByTelNum(telNum); resInfo.put("gameOver", "1"); resInfo.put("gdklist", gdklist); } } return
     * ResponsePackUtil.buildPack("0000", resInfo.toJSONString()); }
     */

    /**
     * 取得当月天数
     * */
    public int getCurrentMonthLastDay() {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);// 把日期设置为当月第一天
        a.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 判断是否当月最后一天
     * 
     * @param monthDay
     * @return
     * @Description:
     */
    // private boolean checkIsLastOfMonth(int monthDay) {
    // Calendar now = Calendar.getInstance();
    // int day = now.get(Calendar.DAY_OF_MONTH);
    // if (monthDay == day)
    // return true;
    // return false;
    // }

    // private void saveIntegralLog(String moduleCode, int count, int countReal, String operationCode, Date currentDate, String remark, String
    // ruleType, String telNum, String userId) {
    // IntegralLogVo integralLogVo = new IntegralLogVo();
    // integralLogVo.setModuleCode(moduleCode);
    // integralLogVo.setCount(count);
    // integralLogVo.setCountReal(countReal);
    // integralLogVo.setOperationCode(operationCode);
    // integralLogVo.setPointsDate(currentDate);
    // integralLogVo.setRemark(remark);
    // integralLogVo.setRowId(UUID.randomUUID().toString());
    // integralLogVo.setRuleType(ruleType);
    // integralLogVo.setTelNum(telNum);
    // integralLogVo.setUserId(userId);
    // integralInterface.saveIntegralLog(integralLogVo);
    // }

    /**
     * 封装json
     * 
     * @param result
     * @param response
     */
    public void creatJson(HttpServletResponse response, JSONObject json) {
        PrintWriter write = null;
        try {
            write = response.getWriter();
            write.print(json.toString());
        } catch (Exception e) {
            logger.error("返回响应报错", e);
        } finally {
            if (null != write) {
                write.flush();
                write.close();
            }
        }
    }

    /**
     * 获取积分总数
     * 
     * @param telNum(用户号码)
     * @return
     * @Description:
     */
    public String getIntegralSum(String requestBody, HttpServletResponse response) {
        logger.debug("获取用户手机号码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        int integralSum = integralInterface.findMonthTotalIntegral(new Date(), telNum);

        JSONObject bodyJson = new JSONObject();

        bodyJson.put("integralSum", integralSum);
        this.creatJson(response, bodyJson);
        return null;
    }

    /**
     * 获取积分规则
     * 
     * @return
     * @Description:
     */
    public String getIntegralRule(String requestBody, HttpServletResponse response) {
        JSONObject bodyJson = new JSONObject();

        Map<String, Object> conditions = new HashMap<String, Object>();

        Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();

        List<IntegralRuleVo> integralRules = new ArrayList<IntegralRuleVo>();

        integralRules = integralInterface.findIntegralRuleByCondition(conditions, sortMap);

        bodyJson.put("integralRules", integralRules);
        this.creatJson(response, bodyJson);
        return null;
    }

    /**
     * 获取前三月收入出积分
     * 
     * @param Phone(用户号码)
     * @return
     * @Description:
     */
//    public String getIntegralInTake(String requestBody, HttpServletResponse response) {
//        logger.debug("获取用户手机号码,requestBody:{}", requestBody);
//        JSONObject requestJson = JSONObject.parseObject(requestBody);
//        String telNum = requestJson.getString("telNum");
//
//        JSONObject bodyJson = new JSONObject();
//
//        bodyJson = integralInterface.findThreeMonthTotalIntegral(telNum);
//
//        this.creatJson(response, bodyJson);
//        return null;
//    }

    /**
     * 获取指定月份详细收入积分(分页方式)
     * 
     * @param telNum(用户号码)
     * @param month(月份)
     * @return
     * @Description:
     */
    public String getIntegralInTakeMore(String requestBody, HttpServletResponse response) {
        logger.debug("获取用户手机号码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        Date date = requestJson.getDate("date");
        String last_rowkey = requestJson.getString("last_rowkey");
        int rows = requestJson.getIntValue("rows");

        JSONObject bodyJson = new JSONObject();

        bodyJson = integralInterface.findMonthIntegralOfPage(date, telNum, last_rowkey, rows);

        this.creatJson(response, bodyJson);
        return null;
    }

    /**
     * 获取用户对应的密钥
     * 
     * @param telNum
     * @return
     * @Description:
     */
    // private String getUserKeyByTelNum(String userId) {
    // logger.debug("获取用户对应的密钥,userId:{}", userId);
    // try {
    // ClientUserVO clientUserVO = clientUserInterface.findById(userId);
    // logger.debug("获取用户对应的密钥,userId:{},clientUserVO:{}", userId, clientUserVO);
    // if (null == clientUserVO)
    // return null;
    // logger.debug("获取用户对应的密钥,userId:{},privateKey:{}", userId, clientUserVO.getPrivateKey());
    // return clientUserVO.getPrivateKey();
    // } catch (Exception e) {
    // logger.error("获取用户对应的密钥异常,userId:{}", userId, e);
    // return null;
    // }
    //
    // }

    /**
     * 下载页面，根据手机号生成验证码
     * 
     * @param
     * @return
     * @Description:
     */
    public String getCodeAtDownloadPage(String request_body) {
        logger.debug("下载页面——获取验证码,requestBody:{}", request_body);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String mobile = requestJson.getString("receiverMobile");
        // 缺少参数
        if (org.springframework.util.StringUtils.isEmpty(mobile)) {
            return ResponsePackUtil.buildPack("-2", "");
        }
        String verifyStr = redisInterface.getString(nameSpace + mobile);
        // 如果验证码不为空,验证码已发送,距离发送时间未到120秒,请稍等
        if (!org.springframework.util.StringUtils.isEmpty(verifyStr))
            return ResponsePackUtil.buildPack("-1", "");

        // 生成验证码
        verifyStr = createVerifyCode();

        // 保存失败
        if (!redisInterface.setString(nameSpace + mobile, verifyStr, 120)) {
            logger.error("验证码保存到redis失败{}");
            return ResponsePackUtil.buildPack("-2", "");
        }

        logger.debug("用户:" + mobile + "=====获取下载验证码=====:{}", verifyStr);
        logger.info("用户:" + mobile + "=====获取下载验证码=====:{}", verifyStr);
        logger.error("用户:" + mobile + "=====获取下载验证码=====:{}", verifyStr);

        // 发送短信
        if (!sendSmsInterface.sendCommonSms(mobile, verifyStr + "（下载验证码，请勿泄露）")) {
            // return ResponsePackUtil.buildPack("-2", "");
        }

        return ResponsePackUtil.buildPack("200", "");
    }

    /**
     * 下载页面下载记录
     * 
     * @param request_body
     * @return
     */
    public String recordDownload(String request_body) {
        logger.debug("下载页面——记录下载信息,requestBody:{}", request_body);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String receiverMobile = requestJson.getString("receiverMobile");
        String mobile = requestJson.getString("mobile");
        String channel = requestJson.getString("channel");
        String code = requestJson.getString("code");
        // String inviterId = requestJson.getString("inviterId");

        if (org.springframework.util.StringUtils.isEmpty(receiverMobile) || org.springframework.util.StringUtils.isEmpty(channel) || org.springframework.util.StringUtils.isEmpty(mobile)
                || org.springframework.util.StringUtils.isEmpty(code))
            return ResponsePackUtil.buildPack("-4", "");

        if (mobile.equals(receiverMobile))
            return ResponsePackUtil.buildPack("-5", "");

        String verifyStr = redisInterface.getString(nameSpace + receiverMobile);
        if (org.springframework.util.StringUtils.isEmpty(verifyStr))
            return ResponsePackUtil.buildPack("-3", "");

        if (!code.equals(verifyStr))
            return ResponsePackUtil.buildPack("-2", "");

        channel = parseChannel(channel);

        // 根据邀请人手机号查询邀请人归属地
        try {
            List<ClientUserVO> cvList = clientUserInterface.findByTelNum(mobile);
            String inviterCorp = "";
            if (cvList != null && cvList.size() > 0) {
                for (ClientUserVO clientUserVO : cvList) {
                    if (!org.springframework.util.StringUtils.isEmpty(clientUserVO.getCorpId())) {
                        inviterCorp = clientUserVO.getCorpId();
                    }
                }
            }
            String inviterRegion = "";
            if (!org.springframework.util.StringUtils.isEmpty(inviterCorp)) {
                inviterRegion = corpInterface.findById(inviterCorp) == null ? "" : corpInterface.findById(inviterCorp).getCorpRegion();
            }

            InviteShareVo isv = new InviteShareVo();
            isv.setId(UUID.randomUUID().toString());
            isv.setType(0);
            isv.setInviterregion(inviterRegion);
            isv.setInviter(mobile);
            isv.setInserttime(sdf.format(new Date()));
            isv.setChannel(channel);
            isv.setBeinviterregion("");
            isv.setBeinviter(receiverMobile);
            isv.setActivetime("");
            inviteShareInterface.saveInviteRecord(isv);
        } catch (Exception e) {
            logger.error("记录下载信息异常:{}", e);
            return ResponsePackUtil.buildPack("-4", "");
        }

        return ResponsePackUtil.buildPack("200", "");
    }

    /**
     * 生成6位数数字验证码
     * 
     * @return
     */
    public String createVerifyCode() {
        String verifyCode = "";
        for (int i = 0; i < 6; i++) {
            int num = (int) (Math.random() * 10);
            verifyCode += num + "";
        }
        return verifyCode;
    }

    public String parseChannel(String channel) {
        // M20001:二维码,M20002:分享给微信好友,M20003:分享到微信朋友圈,M20004:分享到新浪微博,M20005:短信邀请,M20006:复制链接
        // 1微信好友 2微信朋友圈 3新浪微博 4短信 5复制链接 6二维码
        if ("1".equals(channel)) {// 微信好友
            channel = "M20002";
        }
        if ("2".equals(channel)) {// 2微信朋友圈
            channel = "M20003";
        }
        if ("3".equals(channel)) {// 3新浪微博
            channel = "M20004";
        }
        if ("4".equals(channel)) {// 4短信
            channel = "M20005";
        }
        if ("5".equals(channel)) {// 5复制链接
            channel = "M20006";
        }
        if ("6".equals(channel)) {// 6二维码
            channel = "M20001";
        }
        return channel;
    }

    /**
     * 获取用户分享列表
     * 
     * @param user_id
     * @param telnum
     * @param request_body
     * @return
     */
    public String getShareList(String user_id, String telNum, String request_body) {

        logger.debug("获取用户分享列表,user_id:{},telNum:{},request_body:{}", user_id, telNum, request_body);

        /** 查询需要在前台展示的月份的数据 */
        String starttime = "2015-09-01 00:00:00";
        String endTime = "2017-01-01 00:00:00";
        String resultDate;
        try {
            List<String> shareTime = queryShowTime(starttime, endTime);
            logger.debug("获取用户分享列表——计算需要展示的月份:{}", JSON.toJSONString(shareTime));

            List<InviteShareVo> list = new ArrayList<InviteShareVo>();
            /** 查询该用户所有的分享记录 */
            List<InviteShareVo> sharelist = inviteShareInterface.findInviteShareListByTelnum(telNum);
            logger.debug("获取用户分享列表——查询该用户在vwt_share中的数据:{}", JSON.toJSONString(sharelist));
            List<InviteShareVo> hisList = inviteShareInterface.findInviteShareHisListByTelnum(telNum);
            logger.debug("获取用户分享列表——查询该用户在vwt_share_his中的数据:{}", JSON.toJSONString(hisList));

            if (sharelist != null)
                list.addAll(sharelist);
            if (hisList != null)
                list.addAll(hisList);

            /** 处理数据在前台展示 */
            List<Map<String, Object>> allList = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < shareTime.size(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                List<InviteShareVo> monthDataList = new ArrayList<InviteShareVo>();
                String month = shareTime.get(i);
                int Success_share = 0;
                map.put("month", month);
                map.put("Success_share", 0);
                map.put("insert_time", shareTime.get(i));
                if (null != list && list.size() > 0) {
                    for (InviteShareVo inviteShareVo : list) {
                        if (month.equals(inviteShareVo.getInserttime().substring(0, 7))) {
                            monthDataList.add(inviteShareVo);

                            if (inviteShareVo.getType() == 1)
                                map.put("Success_share", ++Success_share);
                        }
                    }
                }
                map.put("monthDataList", monthDataList);
                allList.add(map);
            }
            resultDate = JSON.toJSONString(allList);
            logger.debug("获取用户分享列表——返回给前台的数据:{}", resultDate);
            return ResponsePackUtil.buildPack("0000", resultDate);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取用户分享列表异常,user_id:{},telNum:{},request_body:{}", user_id, telNum, request_body, e);
        }
        return null;
    }

    public List<String> queryShowTime(String starttime, String endtime) {
        List<String> shareTime = new ArrayList<String>();
        List<String> shTime = new ArrayList<String>();
        try {
            Date d1 = sdf_month.parse(starttime);// 定义起始日期
            Date d2 = sdf_month.parse(endtime);
            Calendar dd = Calendar.getInstance();// 定义日期实例
            dd.setTime(d1);// 设置日期起始时间
            while (dd.getTime().before(d2)) {// 判断是否到结束日期
                String str = sdf_month.format(dd.getTime());
                shareTime.add(str);
                shTime.add(str);
                dd.add(Calendar.MONTH, 1);// 进行当前日期月份加1
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        for (int i = 0; i < shTime.size(); i++) {
            String year = shTime.get(i).substring(0, 4);
            String month = shTime.get(i).substring(5);
            Date d = new Date(Integer.parseInt(year) - 1900, Integer.parseInt(month) - 1, 1, 0, 0, 0);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            // cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date date = cal.getTime();
            if (date.getTime() >= new Date().getTime()) {
                shareTime.remove(shTime.get(i));
            }
        }

        return shareTime;
    }

    /**
     * 获取用户当月分享详情
     * 
     * @param user_id
     * @param telnum
     * @param request_body
     * @return
     */
    public String getShareDetail(String user_id, String telnum, String request_body) {
        return null;
    }

    /**
     * 积分兑换接口 查询现有积分
     */
    // @RequestMapping(value = "/getUsePoint")
    public int getUsePoint() {
        // 获取手机号码
        String telNum = "";// requestJson.getString("telNum");

        // 获取支出积分
        int payTotal = 0;// getAllPayPointsTotal(telNum);
        // 获取获得积分
        int addTotal = 0;// getAllAddPointsTotal(telNum);
        // 判断支出积分是否大于获得积分或相等。是返回0
        if (payTotal == addTotal || payTotal > addTotal) {
            return 0;
        }
        return addTotal - payTotal;
    }

    public String validataPoint(String request_body) {
        JSONObject requestJson = JSONObject.parseObject(request_body);
        JSONObject jo = new JSONObject();
        try {
            // 获取手机号码
            String telNum = requestJson.getString("telNum");
            // 现有积分
            int point = integralSpendInterface.getUserCurrPoints(telNum);
            // 商品积分
            int InPoint = 0;
            // 类型
            String commodityType = "";
            //
            String commodityCode = "";
            // 是否可兑换多份
            String isMultiple = "";
            // 兑换多份上线
            int multipleLimit = 0;
            // 商品个数
            int commodityCount = 0;
            String exchangePeriod = null;// 兑换期，1为每月，2为任意时间
            String id = requestJson.getString("id");
            List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(id);
            if (vwtJfInteList != null && vwtJfInteList.size() > 0) {
                VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);
                InPoint = v.getPoint();
                commodityCode = v.getCommodityCode();
                commodityType = v.getCommodityType();
                isMultiple = v.getIsMultiple();
                multipleLimit = v.getMultipleLimit();
                commodityCount = v.getAmount();
                exchangePeriod = v.getExchangePeriod();
                // 判断当前时间是否在兑现时间范围内：
                // 2为任意时间
                Calendar currentTime = Calendar.getInstance();
                Date date = currentTime.getTime();
                if (exchangePeriod.equals("2")) {
                    // 判断兑换时间是否在当前时间范围
                    if (v.getEndDate().compareTo(date) < 0 || v.getStartDate().compareTo(date) > 0) {
                        jo.put("msg", "对不起，不在规定的兑换时间范围内");
                        return ResponsePackUtil.buildPack("0000", jo.toJSONString());
                    }
                    // 当月 判断开始结束日期
                } else if (exchangePeriod.equals("1")) {
                    int currentDay = currentTime.getTime().getDate();
                    if (currentDay < v.getPeriodStart() || currentDay > v.getPeriodEnd()) {
                        jo.put("msg", "对不起，不在规定的兑换时间范围内");
                        return ResponsePackUtil.buildPack("0000", jo.toJSONString());
                    }
                }
            }
            if (InPoint > point) {
                jo.put("msg", "对不起，您的积分不足不能兑换");
                return ResponsePackUtil.buildPack("0000", jo.toJSONString());
            }
            List<VwtJfOrderVo> list = integralSpendInterface.getByCtyIdCurrentMonth(id, telNum);
            // 任意时间
            if (null != exchangePeriod && exchangePeriod.equals("2")) {
                integralSpendInterface.getByCtyId(id, telNum);
            }
            // 已兑换
            List<VwtJfRedeemCodeVo> isList = null;
            if (!"".equals(commodityType) && "30".equals(commodityType)) {
                // 已兑换
                isList = integralSpendInterface.getVwtJfRedeemIsCodeList(commodityCode);
            }
            // 判断订单历史数据
            if (list != null && list.size() > 0) {
                int size = list.size();
                // 只可兑换一份
                if ("0".equals(isMultiple)) {
                    if (size > 0) {
                        jo.put("msg", "对不起，您已兑换过该商品不能再次兑换");
                    }
                    // 兑换码判断
                    else if (isList != null && isList.size() > 0) {
                        jo.put("msg", "对不起，您已兑换过该商品不能再次兑换");
                    } else {
                        jo.put("msg", true);
                    }
                    // 可以兑换多份
                } else if ("1".equals(isMultiple)) {
                    if (size >= multipleLimit) {
                        jo.put("msg", "对不起，该商品到达兑换上限，不能再次兑换");
                    }
                    // 兑换码判断
                    else if (isList != null && isList.size() >= multipleLimit) {
                        jo.put("msg", "对不起，该商品到达兑换上限，不能再次兑换");
                    } else {
                        jo.put("msg", true);
                    }
                } else {
                    jo.put("msg", "服务器内部错误，请联系管理员!");
                }
                return ResponsePackUtil.buildPack("0000", jo.toJSONString());
            }
            jo.put("msg", true);
            return ResponsePackUtil.buildPack("0000", jo.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponsePackUtil.buildPack("0000", jo.toJSONString());
    }

    /**
     * 查询商品列表
     * 
     * @param request
     * @param response
     * @return
     */
    // @RequestMapping(value = "/getIntegralcommodityList")
    // @ResponseBody
    public String getIntegralcommodityList(String request_body) {
        JSONObject resJson = new JSONObject();
        JSONArray ja = new JSONArray();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            VwtJfIntegralcommodityVo vjp = new VwtJfIntegralcommodityVo();
            // 手机号码
            String telNum = requestJson.getString("telNum");
            // 用户ID
            String memberId = requestJson.getString("memberId");
            // 获取用户所在地
            // String szd = getUserRegion(memberId);
            String szd = "14";
            String fromChanel = "1";
            // 用户地区为空直接返回null 前台判断不展示对应商品信息
            /*
             * if(null==szd || "".equals(szd)){ return null; }
             */
            // 判断该手机号是否是江苏移动用户
            // if("江苏移动用户".equals(szd)){
            // }

            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("LIKE_fromChannel", fromChanel);
            conditions.put("LIKE_cityDetail", szd);
            List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityListByCondition(conditions, null);
            for (int i = 0; i < vwtJfInteList.size(); i++) {
                JSONObject jo = new JSONObject();
                VwtJfIntegralcommodityVo v = vwtJfInteList.get(i);
                // /*//判断兑换范围是否是部分地市*/
                // if("2".equals(v.getCityRange())){
                // String detail = v.getCityDetail();
                // //判断用户所在地是否在部分地市中存在
                // if(detail.indexOf(szd) == -1){
                // continue;
                // }
                // }
                jo.put("exPer", v.getExchangePeriod());
                jo.put("pSt", v.getPeriodStart());
                jo.put("pEd", v.getPeriodEnd());
                jo.put("goodsId", v.getGoodsId());
                jo.put("commodityCode", v.getCommodityCode());
                jo.put("commodityName", v.getCommodityName());
                jo.put("commodityType", v.getCommodityType());
                jo.put("point", v.getPoint());
                jo.put("imgSmall", ParamConfig.file_server_url + v.getImgSmall());
                ja.add(jo);
            }
            resJson.put("goodList", ja);
            return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
        } catch (Exception e) {
            logger.debug("查询所有积分商品表数据错误：" + e.getMessage());
            e.printStackTrace();
        }
        return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
    }

    /**
     * 查询商品详情
     * 
     * @param request
     * @param response
     * @return
     */
    // @RequestMapping(value = "/getIntegralcommodityDateil")
    // @ResponseBody
    public String getIntegralcommodityDateil(String request_body) {
        JSONObject jo = new JSONObject();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String id = requestJson.getString("id");
            // String telNum = requestJson.getString("telNum");
            // 查询商品
            List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(id);
            if (vwtJfInteList != null && vwtJfInteList.size() > 0) {
                VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);
                jo.put("goodsId", v.getGoodsId());
                jo.put("commodityCode", v.getCommodityCode());
                jo.put("commodityName", v.getCommodityName());
                jo.put("redeemExplain", v.getRedeemExplain());
                jo.put("commodityType", v.getCommodityType());
                jo.put("point", v.getPoint());
                jo.put("imgSmall", ParamConfig.file_server_url + v.getImgSmall());

                Calendar now = Calendar.getInstance();
                int day = now.get(Calendar.DAY_OF_MONTH);
                // System.out.println("日: " + now.get(Calendar.DAY_OF_MONTH));

                // 判断商品总数 是否小于
                if (v.getAmount() == 0) {
                    jo.put("res", false);
                    resJson.put("resInfo", jo);
                    return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
                }

                if ("30".equals(v.getCommodityType())) {
                    // 未兑换
                    List<VwtJfRedeemCodeVo> noList = integralSpendInterface.getVwtJfRedeemNoCodeList(v.getCommodityCode());
                    if (noList != null && noList.size() > 0) {
                        jo.put("res", true);
                    } else {
                        jo.put("res", false);
                    }
                } else {
                    jo.put("res", true);
                }
            }
            resJson.put("resInfo", jo);
            return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
        } catch (Exception e) {
            logger.debug("查询所有积分商品表数据错误：" + e.getMessage());
            e.printStackTrace();
        }
        resJson.put("resInfo", "");
        return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
    }

    /**
     * 查询兑换码（创建订单及扣除积分）
     * 
     * @param request
     * @param response
     * @return
     */
    // @RequestMapping(value = "/getRedeemCode")
    // @ResponseBody
    public String getRedeemCodeCommodity(String request_body) {
        JSONObject jo = new JSONObject();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateTime = sdf.format(new Date());
            String commodityCode = requestJson.getString("id");
            String telNum = requestJson.getString("telNum");
            // 查询未兑换商品兑换码
            List<VwtJfRedeemCodeVo> listCode = integralSpendInterface.getVwtJfRedeemNoCodeList(commodityCode);
            if (listCode != null && listCode.size() > 0) {
                VwtJfRedeemCodeVo redCode = listCode.get(0);
                // 查询商品
                List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(commodityCode);
                if (vwtJfInteList != null && vwtJfInteList.size() > 0) {
                    VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);

                    VwtJfOrderVo vwtJfOrder = new VwtJfOrderVo();
                    String orderNo = UcsNodeUtil.getOrderNo();
                    
                    
                    redCode.setRedeemDate(Timestamp.valueOf(dateTime));
                    redCode.setRedeemFlag("1");
                    redCode.setTelNum(telNum);
                    redCode.setRemark(orderNo);
                    // 更新该兑换码已被兑换
                    integralSpendInterface.saveVwtJfRedeemCode(redCode);

                    vwtJfOrder.setPointorderID(UcsNodeUtil.getNodeId() + "");
                    vwtJfOrder.setOrderid(orderNo);
                    vwtJfOrder.setCommodityType(v.getCommodityType());
                    vwtJfOrder.setOrderTitle(v.getCommodityName());
                    vwtJfOrder.setOrderRemark(v.getExplain());
                    // vwtJfOrder.setUserid("");
                    vwtJfOrder.setTelNum(telNum);
                    // vwtJfOrder.setMenberName("");
                    vwtJfOrder.setOrderDate(dateTime);
                    vwtJfOrder.setGoodsAmount(v.getAmount());
                    vwtJfOrder.setPointsAmount(v.getPoint());
                    vwtJfOrder.setStatus("10");
                    // vwtJfOrder.setRemark("");
                    // vwtJfOrder.setCreateId("");
                    vwtJfOrder.setCreateTime(Timestamp.valueOf(dateTime));
                    // vwtJfOrder.setUpdateId();
                    // vwtJfOrder.setUpdateTime("");
                    vwtJfOrder.setDelFlag("0");

                    VwtJfOrderItemVo vwtJfOrderItemVo = new VwtJfOrderItemVo();
                    vwtJfOrderItemVo.setOrderItemId(UcsNodeUtil.getNodeId() + "");
                    vwtJfOrderItemVo.setOrderid(orderNo);
                    vwtJfOrderItemVo.setCommoditycode(v.getCommodityCode());
                    vwtJfOrderItemVo.setCommodityType(v.getCommodityType());
                    vwtJfOrderItemVo.setOrderTitle(v.getCommodityName());
                    vwtJfOrderItemVo.setCount(v.getAmount());
                    vwtJfOrderItemVo.setPoints(v.getPoint());
                    vwtJfOrderItemVo.setItemStatus("10");
                    // vwtJfOrderItem.setRemark("");
                    // vwtJfOrderItem.setCreateId("");
                    vwtJfOrderItemVo.setCreateTime(dateTime);
                    // vwtJfOrderItem.setUpdateId("");
                    // vwtJfOrderItem.setUpdateTime("");
                    vwtJfOrderItemVo.setDelFlag("0");

                    // vwtJfOrder.setVwtJfOrderItem(vwtJfOrderItem);
                    // 插入订单详情数据
                    integralSpendInterface.saveOrderItem(vwtJfOrderItemVo);
                    // 插入订单数据
                    integralSpendInterface.saveOrder(vwtJfOrder);

                    VwtJfPayPointsLogVo vwtJfPayPointsLog = new VwtJfPayPointsLogVo();
                    vwtJfPayPointsLog.setPaylogId(UcsNodeUtil.getNodeId() + "");
//                    vwtJfPayPointsLog.setUserId(123);
                    vwtJfPayPointsLog.setTelNum(telNum);
                    vwtJfPayPointsLog.setPointsDate(Timestamp.valueOf(dateTime));
                    vwtJfPayPointsLog.setCount(v.getPoint());
                    vwtJfPayPointsLog.setOrderid(orderNo);
                    vwtJfPayPointsLog.setRemark("测试");
                    // 插入支出积分日志数据
                    integralSpendInterface.savePayPointsLog(vwtJfPayPointsLog);
                }
                jo.put("success", true);
                jo.put("res", redCode.getRedeemCode());
            } else {
                jo.put("success", false);
                jo.put("res", "对不起，没有查询到兑换码，请联系管理员!");
            }
        } catch (Exception e) {
            jo.put("success", false);
            jo.put("res", "对不起，没有查询到兑换码，请联系管理员!");
            e.printStackTrace();
        }
        resJson.put("resInfo", jo);
        return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
    }

    /**
     * 非兑换码商品 创建订单及扣除积分
     * 
     * @param request
     * @param response
     * @return
     */
    // @RequestMapping(value = "/getExchange")
    // @ResponseBody
    public String getExchange(String request_body) {
        JSONObject jo = new JSONObject();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateTime = sdf.format(new Date());
            // 商品id
            String commodityCode = requestJson.getString("id");
            // 手机号
            String telNum = requestJson.getString("telNum");

            // 查询商品
            List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(commodityCode);
            if (vwtJfInteList != null && vwtJfInteList.size() > 0) {
                VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);

                VwtJfOrderVo vwtJfOrder = new VwtJfOrderVo();
                // 订单编号
                String orderNo = UcsNodeUtil.getOrderNo();

                vwtJfOrder.setPointorderID(UcsNodeUtil.getNodeId() + "");
                vwtJfOrder.setOrderid(orderNo);
                vwtJfOrder.setCommodityType(v.getCommodityType());
                vwtJfOrder.setOrderTitle(v.getCommodityName());
                vwtJfOrder.setOrderRemark(v.getExplain());
                // vwtJfOrder.setUserid("");
                vwtJfOrder.setTelNum(telNum);
                // vwtJfOrder.setMenberName("");
                vwtJfOrder.setOrderDate(dateTime);
                vwtJfOrder.setGoodsAmount(v.getAmount());
                vwtJfOrder.setPointsAmount(v.getPoint());
                vwtJfOrder.setStatus("10");
                // vwtJfOrder.setRemark("");
                // vwtJfOrder.setCreateId("");
                vwtJfOrder.setCreateTime(Timestamp.valueOf(dateTime));
                // vwtJfOrder.setUpdateId();
                // vwtJfOrder.setUpdateTime("");
                vwtJfOrder.setDelFlag("0");

                VwtJfOrderItemVo vwtJfOrderItemVo = new VwtJfOrderItemVo();
                vwtJfOrderItemVo.setOrderItemId(UcsNodeUtil.getNodeId() + "");
                vwtJfOrderItemVo.setOrderid(orderNo);
                vwtJfOrderItemVo.setCommoditycode(v.getCommodityCode());
                vwtJfOrderItemVo.setCommodityType(v.getCommodityType());
                vwtJfOrderItemVo.setOrderTitle(v.getCommodityName());
                vwtJfOrderItemVo.setCount(v.getAmount());
                vwtJfOrderItemVo.setPoints(v.getPoint());
                vwtJfOrderItemVo.setItemStatus("10");
                // vwtJfOrderItem.setRemark("");
                // vwtJfOrderItem.setCreateId("");
                vwtJfOrderItemVo.setCreateTime(dateTime);
                // vwtJfOrderItem.setUpdateId("");
                // vwtJfOrderItem.setUpdateTime("");
                vwtJfOrderItemVo.setDelFlag("0");

                // vwtJfOrder.setVwtJfOrderItem(vwtJfOrderItem);
                // 插入订单详情数据
                integralSpendInterface.saveOrderItem(vwtJfOrderItemVo);
                // 插入订单数据
                integralSpendInterface.saveOrder(vwtJfOrder);

                VwtJfPayPointsLogVo vwtJfPayPointsLog = new VwtJfPayPointsLogVo();
                vwtJfPayPointsLog.setPaylogId(UcsNodeUtil.getNodeId() + "");
                // ####================
//                vwtJfPayPointsLog.setUserId(123);
                vwtJfPayPointsLog.setTelNum(telNum);
                vwtJfPayPointsLog.setPointsDate(Timestamp.valueOf(dateTime));
                vwtJfPayPointsLog.setCount(v.getPoint());
                vwtJfPayPointsLog.setOrderid(orderNo);
                vwtJfPayPointsLog.setRemark("测试");
                // 对非兑换商品总数进行更新
                v.setAmount(v.getAmount() - 1);
                integralSpendInterface.saveVwtJfIntegralcommodity(v);
                // 插入支出积分日志数据
                integralSpendInterface.savePayPointsLog(vwtJfPayPointsLog);
            }
            jo.put("success", true);
        } catch (Exception e) {
            jo.put("success", false);
            jo.put("res", "对不起，插入订单或支出积分日志错误，请联系管理员!");
            e.printStackTrace();
        }
        resJson.put("resInfo", jo);
        return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
    }

    /**
     * 分页查询订单
     * 
     * @param request
     * @param response
     * @return
     */
    // @RequestMapping(value = "/getDateFormatOrderList")
    public String getDateFormatOrderList(String request_body) {
        JSONArray ja = new JSONArray();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月");
            List<String> orderList = integralSpendInterface.getOrderList();
            for (int i = 0; i < orderList.size(); i++) {
                if (orderList.get(i) == null && !"".equals(orderList.get(i))) {
                    continue;
                }
                JSONObject jo = new JSONObject();
                jo.put("orderDate", sdf1.format(sdf.parse(orderList.get(i))));
                ja.add(jo);
            }
        } catch (Exception e) {
            logger.debug("分页查询订单数据错误：" + e.getMessage());
            e.printStackTrace();
        }
        resJson.put("resInfo", ja);
        return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
    }

    // @RequestMapping(value = "/getOrderDetailList")
    public String getOrderDetailList(String request_body) {
        // SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-mm");
        JSONArray ja = new JSONArray();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String orderDate = requestJson.getString("date");// requestJson.getString("date") != null && !"".equals(requestJson.getString("date")) ?
                                                             // URLDecoder.decode(requestJson.getString("date"), "UTF-8") : "";
            String telNum = requestJson.getString("telNum");
            orderDate = orderDate.replaceAll("年", "-").replaceAll("月", "");
            // orderDate=simpleDateFormat.format(new Date(orderDate));
            List<VwtJfOrderVo> orderList = integralSpendInterface.getOrderDetailList(orderDate, telNum);
            for (int i = 0; i < orderList.size(); i++) {
                VwtJfOrderVo vwtJfOrder = orderList.get(i);
                JSONObject jo = new JSONObject();
                // 查询明细
                VwtJfOrderItemVo detailItem = integralSpendInterface.getOrderItemDetail(vwtJfOrder.getOrderid());
                if (detailItem == null) {
                    continue;
                }
                jo.put("commodityCode", detailItem.getCommoditycode());
                jo.put("pointorderID", vwtJfOrder.getPointorderID());
                jo.put("orderid", vwtJfOrder.getOrderid());
                jo.put("commodityType", vwtJfOrder.getCommodityType());
                jo.put("orderTitle", vwtJfOrder.getOrderTitle());
                jo.put("orderRemark", vwtJfOrder.getOrderRemark());
                jo.put("userid", vwtJfOrder.getUserid());
                jo.put("telNum", vwtJfOrder.getTelNum());
                jo.put("menberName", vwtJfOrder.getMenberName());
                jo.put("orderDate", vwtJfOrder.getOrderDate());
                jo.put("goodsAmount", vwtJfOrder.getGoodsAmount());
                jo.put("pointsAmount", vwtJfOrder.getPointsAmount());
                jo.put("status", vwtJfOrder.getStatus());
                jo.put("remark", vwtJfOrder.getRemark());
                jo.put("createId", vwtJfOrder.getCreateId());
                jo.put("createTime", vwtJfOrder.getCreateTime());
                jo.put("updateId", vwtJfOrder.getUpdateId());
                jo.put("updateTime", vwtJfOrder.getUpdateId());
                ja.add(jo);
            }
        } catch (Exception e) {
            logger.debug("分页查询订单数据错误：" + e.getMessage());
            e.printStackTrace();
        }
        resJson.put("resInfo", ja);
        return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
    }

    /**
     * 查询兑换码（创建订单及扣除积分）
     * 
     * @param request
     * @param response
     * @return
     */
    // @RequestMapping(value = "/getRedeemCode")
    // @ResponseBody
    public String getRedeemCodeOrder(String request_body) {
        JSONObject jo = new JSONObject();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String orderId = requestJson.getString("orderId");
            String commodityCode = requestJson.getString("commodityCode");
            String telNum = requestJson.getString("telNum");
            if (orderId == null || commodityCode == null || telNum == null || orderId.trim().length() <= 0 || commodityCode.trim().length() <= 0 || telNum.trim().length() <= 0) {
                jo.put("success", false);
                jo.put("res", "对不起，参数传递错误，请联系管理员!");
                resJson.put("resInfo", jo);
                return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
            }

            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_commodityCode", commodityCode);
            conditions.put("EQ_telNum", telNum);
            conditions.put("EQ_remark", orderId);

            // 查询商品
            List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(commodityCode);
            if (vwtJfInteList != null && vwtJfInteList.size() > 0) {
                VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);
                if ("30".equals(v.getCommodityType())) {
                    List<VwtJfRedeemCodeVo> jfRedeemCodes = integralSpendInterface.getRedeemCodeByCondition(conditions, null);
                    if (jfRedeemCodes != null && jfRedeemCodes.size() == 1) {
                        VwtJfRedeemCodeVo codeDetail = jfRedeemCodes.get(0);
                        jo.put("goodsId", v.getGoodsId());
                        jo.put("commodityCode", v.getCommodityCode());
                        jo.put("commodityName", v.getCommodityName());
                        jo.put("redeemExplain", v.getRedeemExplain());
                        jo.put("point", v.getPoint());
                        jo.put("imgSmall", v.getImgSmall());
                        jo.put("redeemCode", codeDetail.getRedeemCode());
                        jo.put("commodityType", v.getCommodityType());
                        jo.put("success", true);
                    } else {
                        jo.put("success", false);
                        jo.put("res", "对不起，没有查询到兑换码，请联系管理员!");
                    }
                } else {
                    jo.put("goodsId", v.getGoodsId());
                    jo.put("commodityCode", v.getCommodityCode());
                    jo.put("commodityName", v.getCommodityName());
                    jo.put("redeemExplain", v.getRedeemExplain());
                    jo.put("point", v.getPoint());
                    jo.put("imgSmall", v.getImgSmall());
                    jo.put("commodityType", v.getCommodityType());
                    jo.put("success", true);
                }
            } else {
                jo.put("success", false);
                jo.put("res", "对不起，没有查询到该商品，请联系管理员!");
            }
        } catch (Exception e) {
            jo.put("success", false);
            jo.put("res", "对不起，没有查询到兑换码，请联系管理员!");
            e.printStackTrace();
        }
        resJson.put("resInfo", jo);
        return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
    }

    /**
     * 按照月份统计获得积分并按照日期倒叙展示
     * 
     * @return
     */
    // @RequestMapping(value = "/getAddPointDateList")
    // @ResponseBody
    public String getAddPointDateList(String request_body) {
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String telNum = requestJson.getString("telNum");
            JSONObject jo = integralSpendInterface.getThreeMonthAddPointTotal(telNum);
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            if (jo != null) {
                String data = jo.toString().replace("{", "").replace("}", "").replace("\"", "");

                String[] array = data.split(",");

                for (int i = 0; i < array.length; i++) {

                    Map<String, String> itemMap = new TreeMap<String, String>();

                    String[] stringArray = array[i].split(":");

                    itemMap.put("key", stringArray[0]);

                    itemMap.put("value", stringArray[1]);

                    list.add(itemMap);
                }
            }

            resJson.put("resInfo", list);
            return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        resJson.put("resInfo", "");
        return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
    }

    /**
     * 按照月份统计支出积分并按照日期倒叙展示
     * 
     * @return
     */
    // @RequestMapping(value = "/getPayPointDateList")
    // @ResponseBody
    public String getPayPointDateList(String request_body) {
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String telNum = requestJson.getString("telNum");
            List<Map<String, Object>> list = integralSpendInterface.getThreeMonthPayPointTotal(telNum);// hbaseService.getThreeMonthPayPointTotal(telNum);//vwtJfPayPointsLogService.getPayPointDateList();
            return ResponsePackUtil.buildPack("0000", list);
        } catch (Exception e) {
            logger.error("按照月份统计支出积分并按照日期倒叙展示异常,requestbody:{},e:{}", request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
    }

    /**
     * 查询支出积分明细
     * 
     * @return
     */
    // @RequestMapping(value = "/getPayPointDetailList")
    public String getPayPointDetailList(String request_body) {
        SimpleDateFormat chinese = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject joObj = new JSONObject();
        JSONArray ja = new JSONArray();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject myJsonObject = JSONObject.parseObject(request_body);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

            String telNum = myJsonObject.getString("telNum");
            byte[] last_rowkey = myJsonObject.getString("page").getBytes();
            Integer pageSize = myJsonObject.getString("rows") != null && !"".equals(myJsonObject.getString("rows")) ? Integer.parseInt(myJsonObject.getString("rows")) : 0;
            Date thisMonth = myJsonObject.getString("Month") != null && !"".equals(myJsonObject.getString("Month")) ? sdf.parse(myJsonObject.getString("Month")) : null;

            JSONObject joList = integralSpendInterface.getMonthPayPointsLogOfPage(telNum, thisMonth, last_rowkey, pageSize);// vwtJfPayPointsLogService.getPayPointDetailList(date);
            JSONArray jaList = joList.getJSONArray("payPointsLogList");
            String page = joList.getString("lastRowId");
            for (int i = 0; i < jaList.size(); i++) {
                VwtJfPayPointsLogVo obj = jaList.getObject(i, VwtJfPayPointsLogVo.class);
                VwtJfOrderVo order = integralSpendInterface.getOrderData(obj.getOrderid());
                if (order == null) {
                    continue;
                }
                JSONObject jo = new JSONObject();
                jo.put("date", chinese.format(obj.getPointsDate()));
                jo.put("count", obj.getCount());
                jo.put("orderName", order.getOrderTitle());
                ja.add(jo);
            }
            joObj.put("list", ja);
            joObj.put("page", page);
        } catch (Exception e) {
            e.printStackTrace();
        }
        resJson.put("resInfo", joObj);
        return ResponsePackUtil.buildPack("0000", resJson.toJSONString());
    }
}
