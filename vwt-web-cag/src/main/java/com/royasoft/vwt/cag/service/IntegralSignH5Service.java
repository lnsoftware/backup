/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.serialize.support.json.JsonObjectInput;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.IntegralUtil;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.invite.api.interfaces.InviteShareInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.IntegralInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.IntegralSpendInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.SignInterface;
import com.royasoft.vwt.soa.integral.api.vo.IntegralActionVo;
import com.royasoft.vwt.soa.integral.api.vo.IntegralLogVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfIntegralcommodityVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfOrderItemVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfOrderVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfPayPointsLogVo;
import com.royasoft.vwt.soa.integral.api.vo.VwtJfRedeemCodeVo;
import com.royasoft.vwt.soa.integral.utils.Response;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;

/**
 * 签到积分处理类
 *
 * @Author:daizl
 * @Since:2016年7月05日
 */
@Scope("prototype")
@Service
public class IntegralSignH5Service implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(IntegralSignH5Service.class);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat sdf_day = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf_month = new SimpleDateFormat("yyyy-MM");
    SimpleDateFormat sdf_month_cn = new SimpleDateFormat("yyyy年MM月");

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
    private OperationLogService operationLogService;

    @Autowired
    private CorpInterface corpInterface;

    @Autowired
    private IntegralUtil integralUtil;

    @Autowired
    private InviteShareInterface inviteShareInterface;

    @Autowired
    private IntegralSpendInterface integralSpendInterface;

    // 2017-10-09 注释
    // @Autowired
    // private CitiesNumberInterface citiesNumberInterface;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");

    private String telNumTmp = "";

    private String userIdTmp = "";

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.integralH5_queue.take();// 获取队列处理数据
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
                    /** 获取用户总积分 1304 */
                        case FunctionIdConstant.MONTHINTEGRAL:
                            resInfo = getMonthIntegralForH5(request_body);
                            break;
                        /** 积分收入列表 1306 */
                        case FunctionIdConstant.INTAKEINTEGRAL:
                            resInfo = getThreeMonthTotalIntegralForH5(request_body);
                            break;
                        /** 积分收入详情列表1307 */
                        case FunctionIdConstant.INTAKEMOREINTEGRAL:
                            resInfo = getMonthIntegralPageForH5(request_body);
                            break;
                        // /** 1338 */
                        // case FunctionIdConstant.pay_getAddPointDateList:
                        // resInfo = getAddPointDateList(request_body);
                        // break;
                        // /** 分页查询订单 1335 */
                        // case FunctionIdConstant.pay_getDateFormatOrderList:
                        // resInfo = getDateFormatOrderList(request_body);
                        // break;
                        // /** 1334 */
                        // case FunctionIdConstant.pay_getExchange:
                        // resInfo = getExchange(request_body);
                        // break;
                        /** 查询商品详情 1332 */
                        case FunctionIdConstant.pay_getIntegralcommodityDateil:
                            resInfo = getIntegralcommodityDateil(request_body);
                            break;
                        /** 查询商品列表 1331 */
                        case FunctionIdConstant.pay_getIntegralcommodityList:
                            resInfo = getIntegralcommodityList(request_body);
                            break;
                        /** 根据日期获取订单详情 1336 */
                        case FunctionIdConstant.pay_getOrderDetailList:
                            resInfo = getOrderDetailList(request_body);
                            break;
                        /** 获取支出积分列表 1339 */
                        case FunctionIdConstant.pay_getPayPointDateList:
                            resInfo = getPayPointDateList(request_body);
                            break;
                        /** 获取支出积分明细 1340 */
                        case FunctionIdConstant.pay_getPayPointDetailList:
                            resInfo = getPayPointDetailList(request_body);
                            break;
                        // /** 购买兑换码商品 1333*/
                        // case FunctionIdConstant.pay_getRedeemCodeCommodity:
                        // resInfo = getRedeemCodeCommodity(request_body);
                        // break;
                        /** 查询兑换码 1337 */
                        case FunctionIdConstant.pay_getRedeemCodeOrder:
                            resInfo = getRedeemCodeOrder(request_body);
                            break;
                        /** 兑换商品1330 */
                        case FunctionIdConstant.pay_validataPoint:
                            resInfo = purchaseGoods(request_body);
                            break;
                        /** 获取订单明细的月份 1341 */
                        case FunctionIdConstant.getMonthList:
                            resInfo = getMonthList(request_body);
                            break;
                        default:
                            break;
                    }
                    logger.debug("签到积分处理类(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponseH5(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        if (org.apache.commons.lang3.StringUtils.isEmpty(user_id) && org.apache.commons.lang3.StringUtils.isEmpty(tel_number)) {
                            operationLogService.saveOperationLogNew(channel, request, userIdTmp, telNumTmp, function_id, request_body, "", responseStatus);
                        } else {
                            operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                        }
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Throwable e) {
                logger.error("签到积分业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {
                // channel.close();
                // 2017/01/08 增加netty主动释放内存方法
                while (!ReferenceCountUtil.release(msg)) {
                    // 自动释放netty计数器
                }
            }
        }
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
        logger.debug("获取用户总积分H5,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String telNum = requestJson.getString("telNum");
            /** 校验参数 */
            if (!StringUtils.checkParam(telNum, true, 11))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1042, "");

            telNumTmp = telNum;

            List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
            logger.debug("获取当月总积分HTML5(校验用户是否存在),telNum:{},clientUserVOs:{}", telNum, JSONObject.toJSONString(clientUserVOs));
            if (null == clientUserVOs || clientUserVOs.isEmpty())
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1043, "");

            int monthIntegral = integralSpendInterface.getUserCurrPoints(telNum);// integralInterface.findMonthTotalIntegral(currentDate, telNum);
            logger.debug("获取当月总积分HTML5(获取总积分),telNum:{},monthIntegral:{}", telNum, monthIntegral);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("totalIntegral", monthIntegral);

            return ResponsePackUtil.buildPack("0000", bodyJson);
        } catch (Exception e) {
            logger.error("获取用户总积分H5异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
    }

    /**
     * 获取三个月积分 1306 HTML5
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String getThreeMonthTotalIntegralForH5(String requestBody) {
        try {
            logger.debug("获取三个月积分HTML5,requestBody:{}", requestBody);
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String telNum = requestJson.getString("telNum");
            logger.debug("获取三个月积分HTML5(解析body),telNum:{}", telNum);
            /** 校验参数 */
            if (!StringUtils.checkParam(telNum, true, 11))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1042, "");

            telNumTmp = telNum;
            List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
            logger.debug("获取三个月积分HTML5(校验用户是否存在),telNum:{},clientUserVOs:{}", telNum, JSONObject.toJSONString(clientUserVOs));
            if (null == clientUserVOs || clientUserVOs.isEmpty())
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1043, "");
            /** 前三个月积分记录 */
            List<Map<String, Object>> list = integralInterface.findThreeMonthTotalIntegral(telNum);
            // /** 当前积分总数 */
            // int integralSum = integralSpendInterface.getUserCurrPoints(telNum);
            //
            // resInfo.put("integralSum", integralSum);

            // resInfo.put("Takes", Takes);
            logger.debug("获取三个月积分HTML5(返回),telNum:{},resInfo:{}", telNum, JSON.toJSONString(list));
            return ResponsePackUtil.buildPack("0000", list);
        } catch (Exception e) {
            logger.error("获取三个月积分H5异常,requestBody:{},e:{}", requestBody, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
    }

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

    @SuppressWarnings("deprecation")
    public List<String> queryShowTime(String starttime, String endtime) throws Exception {
        List<String> shareTime = new ArrayList<String>();
        List<String> shTime = new ArrayList<String>();
        Date d1 = sdf.parse(starttime);// 定义起始日期
        Date d2 = sdf.parse(endtime);
        Calendar dd = Calendar.getInstance();// 定义日期实例
        dd.setTime(d1);// 设置日期起始时间
        while (dd.getTime().before(d2)) {// 判断是否到结束日期
            String str = sdf_month.format(dd.getTime());
            shareTime.add(str);
            shTime.add(str);
            dd.add(Calendar.MONTH, 1);// 进行当前日期月份加1
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
     * 兑换商品
     * 
     * @param request_body
     * @return
     */
    public String purchaseGoods(String request_body) {
        logger.debug("兑换商品requestbody:{}", request_body);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String userId = requestJson.getString("userId");
            String telNum = requestJson.getString("telNum");
            String code = requestJson.getString("code");
            String effectType = requestJson.getString("effectType");
            if (!StringUtils.checkParam(userId, true, 50) || !StringUtils.checkParam(telNum, true, 11) || !StringUtils.checkParam(code, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            telNumTmp = telNum;
            userIdTmp = userId;

            Response response = integralSpendInterface.purchaseGoods(code, userId, telNum, effectType);
            String result = JSON.toJSONString(response);
            logger.debug("兑换商品,requstbody:{},result:{}", request_body, result);
            return result;
        } catch (Exception e) {
            logger.error("兑换商品异常,requestbody:{},e:{}", request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
    }

    // public String validataPoint(String request_body) {
    // JSONObject requestJson = JSONObject.parseObject(request_body);
    // JSONObject jo = new JSONObject();
    // try {
    // // 获取手机号码
    // String telNum = requestJson.getString("telNum");
    // // 现有积分
    // int point = integralSpendInterface.getUserCurrPoints(telNum);
    // // 商品积分
    // int InPoint = 0;
    // // 类型
    // String commodityType = "";
    // //
    // String commodityCode = "";
    // // 是否可兑换多份
    // String isMultiple = "";
    // // 兑换多份上线
    // int multipleLimit = 0;
    // // 商品个数
    // int commodityCount = 0;
    // String exchangePeriod = null;// 兑换期，1为每月，2为任意时间
    // String id = requestJson.getString("id");
    // List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(id);
    // if (vwtJfInteList != null && vwtJfInteList.size() > 0) {
    // VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);
    // InPoint = v.getPoint();
    // commodityCode = v.getCommodityCode();
    // commodityType = v.getCommodityType();
    // isMultiple = v.getIsMultiple();
    // multipleLimit = v.getMultipleLimit();
    // commodityCount = v.getAmount();
    // exchangePeriod = v.getExchangePeriod();
    // // 判断当前时间是否在兑现时间范围内：
    // // 2为任意时间
    // Calendar currentTime = Calendar.getInstance();
    // Date date = currentTime.getTime();
    // if (exchangePeriod.equals("2")) {
    // // 判断兑换时间是否在当前时间范围
    // if (v.getEndDate().compareTo(date) < 0 || v.getStartDate().compareTo(date) > 0) {
    // jo.put("msg", "对不起，不在规定的兑换时间范围内");
    // return ResponsePackUtil.buildPack("0000", jo.toJSONString());
    // }
    // // 当月 判断开始结束日期
    // } else if (exchangePeriod.equals("1")) {
    // int currentDay = currentTime.getTime().getDate();
    // if (currentDay < v.getPeriodStart() || currentDay > v.getPeriodEnd()) {
    // jo.put("msg", "对不起，不在规定的兑换时间范围内");
    // return ResponsePackUtil.buildPack("0000", jo.toJSONString());
    // }
    // }
    // }
    // if (InPoint > point) {
    // jo.put("msg", "对不起，您的积分不足不能兑换");
    // return ResponsePackUtil.buildPack("0000", jo.toJSONString());
    // }
    // List<VwtJfOrderVo> list = integralSpendInterface.getByCtyIdCurrentMonth(id, telNum);
    // // 任意时间
    // if (null != exchangePeriod && exchangePeriod.equals("2")) {
    // integralSpendInterface.getByCtyId(id, telNum);
    // }
    // // 已兑换
    // List<VwtJfRedeemCodeVo> isList = null;
    // if (!"".equals(commodityType) && "30".equals(commodityType)) {
    // // 已兑换
    // isList = integralSpendInterface.getVwtJfRedeemIsCodeList(commodityCode);
    // }
    // // 判断订单历史数据
    // if (list != null && list.size() > 0) {
    // int size = list.size();
    // // 只可兑换一份
    // if ("0".equals(isMultiple)) {
    // if (size > 0) {
    // jo.put("msg", "对不起，您已兑换过该商品不能再次兑换");
    // }
    // // 兑换码判断
    // else if (isList != null && isList.size() > 0) {
    // jo.put("msg", "对不起，您已兑换过该商品不能再次兑换");
    // } else {
    // jo.put("msg", true);
    // }
    // // 可以兑换多份
    // } else if ("1".equals(isMultiple)) {
    // if (size >= multipleLimit) {
    // jo.put("msg", "对不起，该商品到达兑换上限，不能再次兑换");
    // }
    // // 兑换码判断
    // else if (isList != null && isList.size() >= multipleLimit) {
    // jo.put("msg", "对不起，该商品到达兑换上限，不能再次兑换");
    // } else {
    // jo.put("msg", true);
    // }
    // } else {
    // jo.put("msg", "服务器内部错误，请联系管理员!");
    // }
    // return ResponsePackUtil.buildPack("0000", jo.toJSONString());
    // }
    // jo.put("msg", true);
    // return ResponsePackUtil.buildPack("0000", jo.toJSONString());
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // return ResponsePackUtil.buildPack("0000", jo.toJSONString());
    // }

    /**
     * 查询商品列表
     * 
     * @param request
     * @param response
     * @return
     */
    public String getIntegralcommodityList(String request_body) {
        logger.debug("获取商品列表:{}", request_body);
        JSONObject resJson = new JSONObject();
        JSONArray ja = new JSONArray();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            // VwtJfIntegralcommodityVo vjp = new VwtJfIntegralcommodityVo();
            // 手机号码
            String telNum = requestJson.getString("telNum");
            // 用户ID
            String userId = requestJson.getString("userId");
            if (!StringUtils.checkParam(telNum, true, 11) || !StringUtils.checkParam(userId, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            telNumTmp = telNum;
            userIdTmp = userId;

            ClientUserVO clientUserVO = clientUserInterface.findById(userId);
            CorpVO corpVO = corpInterface.findById(clientUserVO.getCorpId());
            String userCity = corpVO.getCorpRegion();
            if ("99".equals(userCity) || "98".equals(userCity))
                userCity = "14";
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_delFlag", 0);
            conditions.put("EQ_isonsale", 1);
            Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
            sortMap.put("sort", false);
            List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityListByCondition(conditions, sortMap);

            for (int i = 0; i < vwtJfInteList.size(); i++) {
                JSONObject jo = new JSONObject();
                VwtJfIntegralcommodityVo v = vwtJfInteList.get(i);
                /* //判断兑换范围是否是部分地市 */
                if ("2".equals(v.getCityRange())) {
                    String detail = v.getCityDetail();
                    // 判断用户所在地是否在部分地市中存在
                    if (detail.indexOf(userCity) == -1) {
                        continue;
                    }
                }
                String userSource = v.getUserSource();
                String fromChannel = v.getFromChannel();
                /** 校验用户来源范围 */
                if (!"1".equals(userSource)) {
                    if (!fromChannel.contains(clientUserVO.getFromChannel() + ""))
                        continue;
                }

                /** 校验用户号码范围 */
                String userRange = v.getUserRange();// 用户范围，1全部用户，2江苏省移动用户
                String userMobile = clientUserVO.getTelNum().substring(0, 7);
                // 2017-10-09 注释
                // if (!"1".equals(userRange) && false == citiesNumberInterface.checkIsExist(userMobile))
                // continue;

                /** 校验是否在可兑换时间 */
                String time = sdf.format(new Date());
                Timestamp nowTime = Timestamp.valueOf(time);
                Timestamp startDate = v.getStartDate();// 开始兑换时间
                Timestamp endDate = v.getEndDate();// 结束兑换时间
                // String exchangePeriod = v.getExchangePeriod();// 兑换期，1为每月，2为任意时间
                // int periodStart = v.getPeriodStart();// 兑换期，每月开始日期，如3
                // int periodEnd = v.getPeriodEnd();// 兑换期，每月结束日期，如10
                if (nowTime.getTime() < startDate.getTime() || nowTime.getTime() > endDate.getTime())
                    continue;
                // if (!"2".equals(exchangePeriod) && (nowTime.getDate() < periodStart || nowTime.getDate() > periodEnd))
                // continue;

                jo.put("exPer", v.getExchangePeriod()); // 兑换期，1为每月，2为任意时间
                // jo.put("pSt", v.getPeriodStart()); // 兑换期，每月开始日期，如3
                // jo.put("pEd", v.getPeriodEnd()); // 兑换期，每月结束日期，如10
                jo.put("goodsId", v.getGoodsId());
                jo.put("commodityCode", v.getCommodityCode());// 商品编码
                jo.put("commodityName", v.getCommodityName());// 商品名称
                jo.put("commodityType", v.getCommodityType());// 商品分类，10兑换流量，20兑换话费
                jo.put("point", v.getPoint());// 兑换积分
                jo.put("imgSmall", ParamConfig.file_server_url + v.getImgSmall());// 图片地址
                ja.add(jo);
            }
            resJson.put("goodList", ja);
            return ResponsePackUtil.buildPack("0000", resJson);
        } catch (Exception e) {
            logger.error("查询商品列表异常,requestBody:{},e:{}", request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
    }

    /**
     * 查询商品详情
     * 
     * @param request
     * @param response
     * @return
     */
    public String getIntegralcommodityDateil(String request_body) {
        logger.debug("查询商品详情:{}", request_body);
        JSONObject jo = new JSONObject();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String code = requestJson.getString("code");
            if (!StringUtils.checkParam(code, true, 50))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            // String telNum = requestJson.getString("telNum");
            // 查询商品
            List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(code);
            if (vwtJfInteList != null && vwtJfInteList.size() > 0) {
                VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);
                jo.put("goodsId", v.getGoodsId());
                jo.put("commodityCode", v.getCommodityCode());
                jo.put("commodityName", v.getCommodityName());
                jo.put("redeemExplain", v.getExplain());
                jo.put("explain", v.getExplain());
                jo.put("commodityType", v.getCommodityType());
                jo.put("point", v.getPoint());
                jo.put("imgSmall", ParamConfig.file_server_url + v.getImgLarge());
                jo.put("res", 1);
                resJson.put("resInfo", jo);
                // 判断商品总数 是否小于
                if (v.getAmount() - v.getRedeemAmount() <= 0) {
                    jo.put("res", 0);
                    resJson.put("resInfo", jo);
                    return ResponsePackUtil.buildPack("0000", resJson);
                }
                //
                if ("30".equals(v.getCommodityType())) {
                    // 未兑换
                    List<VwtJfRedeemCodeVo> noList = integralSpendInterface.getVwtJfRedeemNoCodeList(v.getCommodityCode());
                    if (noList != null && noList.size() > 0) {
                        jo.put("res", 1);
                    } else {
                        jo.put("res", 0);
                    }
                }
            }
            resJson.put("resInfo", jo);
            return ResponsePackUtil.buildPack("0000", resJson);
        } catch (Exception e) {
            logger.error("获取商品详情异常,requestBody:{},e:{}", request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
    }

    // /**
    // * 查询兑换码（创建订单及扣除积分）
    // *
    // * @param request
    // * @param response
    // * @return
    // */
    // public String getRedeemCodeCommodity(String request_body) {
    // JSONObject jo = new JSONObject();
    // JSONObject resJson = new JSONObject();
    // try {
    // JSONObject requestJson = JSONObject.parseObject(request_body);
    // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // String dateTime = sdf.format(new Date());
    // String commodityCode = requestJson.getString("id");
    // String telNum = requestJson.getString("telNum");
    // String userId = requestJson.getString("userId");
    // // 查询未兑换商品兑换码
    // List<VwtJfRedeemCodeVo> listCode = integralSpendInterface.getVwtJfRedeemNoCodeList(commodityCode);
    // if (listCode == null || listCode.isEmpty()) {
    // jo.put("success", false);
    // jo.put("res", "对不起，没有查询到兑换码，请联系管理员!");
    // return null;
    // }
    // VwtJfRedeemCodeVo redCode = listCode.get(0);
    // // 查询商品
    // List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(commodityCode);
    // if (vwtJfInteList != null && vwtJfInteList.size() > 0) {
    // VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);
    //
    // VwtJfOrderVo vwtJfOrder = new VwtJfOrderVo();
    // String orderNo = UcsNodeUtil.getOrderNo();
    //
    // redCode.setRedeemDate(Timestamp.valueOf(dateTime));
    // redCode.setRedeemFlag("1");
    // redCode.setTelNum(telNum);
    // redCode.setRemark(orderNo);
    // // 更新该兑换码已被兑换
    // integralSpendInterface.saveVwtJfRedeemCode(redCode);
    //
    // vwtJfOrder.setPointorderID(UcsNodeUtil.getNodeId() + "");
    // vwtJfOrder.setOrderid(orderNo);
    // vwtJfOrder.setCommodityType(v.getCommodityType());
    // vwtJfOrder.setOrderTitle(v.getCommodityName());
    // vwtJfOrder.setOrderRemark(v.getExplain());
    // // vwtJfOrder.setUserid("");
    // vwtJfOrder.setTelNum(telNum);
    // // vwtJfOrder.setMenberName("");
    // vwtJfOrder.setOrderDate(dateTime);
    // vwtJfOrder.setGoodsAmount(v.getAmount());
    // vwtJfOrder.setPointsAmount(v.getPoint());
    // vwtJfOrder.setStatus("10");
    // // vwtJfOrder.setRemark("");
    // // vwtJfOrder.setCreateId("");
    // vwtJfOrder.setCreateTime(Timestamp.valueOf(dateTime));
    // // vwtJfOrder.setUpdateId();
    // // vwtJfOrder.setUpdateTime("");
    // vwtJfOrder.setDelFlag("0");
    //
    // VwtJfOrderItemVo vwtJfOrderItemVo = new VwtJfOrderItemVo();
    // vwtJfOrderItemVo.setOrderItemId(UcsNodeUtil.getNodeId() + "");
    // vwtJfOrderItemVo.setOrderid(orderNo);
    // vwtJfOrderItemVo.setCommoditycode(v.getCommodityCode());
    // vwtJfOrderItemVo.setCommodityType(v.getCommodityType());
    // vwtJfOrderItemVo.setOrderTitle(v.getCommodityName());
    // vwtJfOrderItemVo.setCount(v.getAmount());
    // vwtJfOrderItemVo.setPoints(v.getPoint());
    // vwtJfOrderItemVo.setItemStatus("10");
    // // vwtJfOrderItem.setRemark("");
    // // vwtJfOrderItem.setCreateId("");
    // vwtJfOrderItemVo.setCreateTime(dateTime);
    // // vwtJfOrderItem.setUpdateId("");
    // // vwtJfOrderItem.setUpdateTime("");
    // vwtJfOrderItemVo.setDelFlag("0");
    //
    // // vwtJfOrder.setVwtJfOrderItem(vwtJfOrderItem);
    // // 插入订单详情数据
    // integralSpendInterface.saveOrderItem(vwtJfOrderItemVo);
    // // 插入订单数据
    // integralSpendInterface.saveOrder(vwtJfOrder);
    //
    // VwtJfPayPointsLogVo vwtJfPayPointsLog = new VwtJfPayPointsLogVo();
    // vwtJfPayPointsLog.setPaylogId(UcsNodeUtil.getNodeId() + "");
    // vwtJfPayPointsLog.setUserId(0);
    // vwtJfPayPointsLog.setTelNum(telNum);
    // vwtJfPayPointsLog.setPointsDate(Timestamp.valueOf(dateTime));
    // vwtJfPayPointsLog.setCount(v.getPoint());
    // vwtJfPayPointsLog.setOrderid(orderNo);
    // vwtJfPayPointsLog.setRemark("");
    // // 插入支出积分日志数据
    // integralSpendInterface.savePayPointsLog(vwtJfPayPointsLog);
    // }
    // jo.put("success", true);
    // jo.put("res", redCode.getRedeemCode());
    // } catch (Exception e) {
    // logger.error("处理异常:{}", e);
    // return null;
    // }
    // resJson.put("resInfo", jo);
    // return ResponsePackUtil.buildPack("0000", resJson);
    // }

    // /**
    // * 非兑换码商品 创建订单及扣除积分
    // *
    // * @param request
    // * @param response
    // * @return
    // */
    // public String getExchange(String request_body) {
    // JSONObject jo = new JSONObject();
    // JSONObject resJson = new JSONObject();
    // try {
    // JSONObject requestJson = JSONObject.parseObject(request_body);
    // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // String dateTime = sdf.format(new Date());
    // // 商品编号
    // String commodityCode = requestJson.getString("code");
    // // 手机号
    // String telNum = requestJson.getString("telNum");
    // String userId = requestJson.getString("userId");
    //
    // // 查询商品
    // List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(commodityCode);
    // if (vwtJfInteList == null || vwtJfInteList.size() == 0)
    // return null;
    // VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);
    // VwtJfOrderVo vwtJfOrder = new VwtJfOrderVo();
    // // 订单编号
    // String orderNo = UcsNodeUtil.getOrderNo();
    // vwtJfOrder.setPointorderID(UcsNodeUtil.getNodeId() + "");
    // vwtJfOrder.setOrderid(orderNo);
    // vwtJfOrder.setCommodityType(v.getCommodityType());
    // vwtJfOrder.setOrderTitle(v.getCommodityName());
    // vwtJfOrder.setOrderRemark(v.getExplain());
    // // vwtJfOrder.setUserid("");
    // vwtJfOrder.setTelNum(telNum);
    // // vwtJfOrder.setMenberName("");
    // vwtJfOrder.setOrderDate(dateTime);
    // vwtJfOrder.setGoodsAmount(v.getAmount());
    // vwtJfOrder.setPointsAmount(v.getPoint());
    // vwtJfOrder.setStatus("10");
    // // vwtJfOrder.setRemark("");
    // // vwtJfOrder.setCreateId("");
    // vwtJfOrder.setCreateTime(Timestamp.valueOf(dateTime));
    // // vwtJfOrder.setUpdateId();
    // // vwtJfOrder.setUpdateTime("");
    // vwtJfOrder.setDelFlag("0");
    // VwtJfOrderItemVo vwtJfOrderItemVo = new VwtJfOrderItemVo();
    // vwtJfOrderItemVo.setOrderItemId(UcsNodeUtil.getNodeId() + "");
    // vwtJfOrderItemVo.setOrderid(orderNo);
    // vwtJfOrderItemVo.setCommoditycode(v.getCommodityCode());
    // vwtJfOrderItemVo.setCommodityType(v.getCommodityType());
    // vwtJfOrderItemVo.setOrderTitle(v.getCommodityName());
    // vwtJfOrderItemVo.setCount(v.getAmount());
    // vwtJfOrderItemVo.setPoints(v.getPoint());
    // vwtJfOrderItemVo.setItemStatus("10");
    // // vwtJfOrderItem.setRemark("");
    // // vwtJfOrderItem.setCreateId("");
    // vwtJfOrderItemVo.setCreateTime(dateTime);
    // // vwtJfOrderItem.setUpdateId("");
    // // vwtJfOrderItem.setUpdateTime("");
    // vwtJfOrderItemVo.setDelFlag("0");
    //
    // // vwtJfOrder.setVwtJfOrderItem(vwtJfOrderItem);
    // // 插入订单详情数据
    // integralSpendInterface.saveOrderItem(vwtJfOrderItemVo);
    // // 插入订单数据
    // integralSpendInterface.saveOrder(vwtJfOrder);
    //
    // VwtJfPayPointsLogVo vwtJfPayPointsLog = new VwtJfPayPointsLogVo();
    // vwtJfPayPointsLog.setPaylogId(UcsNodeUtil.getNodeId() + "");
    // // ####================
    // vwtJfPayPointsLog.setUserId(123);
    // vwtJfPayPointsLog.setTelNum(telNum);
    // vwtJfPayPointsLog.setPointsDate(Timestamp.valueOf(dateTime));
    // vwtJfPayPointsLog.setCount(v.getPoint());
    // vwtJfPayPointsLog.setOrderid(orderNo);
    // vwtJfPayPointsLog.setRemark("");
    // // 对非兑换商品总数进行更新
    // v.setAmount(v.getAmount() - 1);
    // integralSpendInterface.saveVwtJfIntegralcommodity(v);
    // // 插入支出积分日志数据
    // integralSpendInterface.savePayPointsLog(vwtJfPayPointsLog);
    // jo.put("success", true);
    // } catch (Exception e) {
    // jo.put("success", false);
    // jo.put("res", "对不起，插入订单或支出积分日志错误，请联系管理员!");
    // e.printStackTrace();
    // }
    // resJson.put("resInfo", jo);
    // return ResponsePackUtil.buildPack("0000", resJson);
    // }

    // /**
    // * 分页查询订单
    // *
    // * @param request
    // * @param response
    // * @return
    // */
    // public String getDateFormatOrderList(String request_body) {
    // JSONArray ja = new JSONArray();
    // JSONObject resJson = new JSONObject();
    // try {
    // JSONObject requestJson = JSONObject.parseObject(request_body);
    // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
    // SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月");
    // List<String> orderList = integralSpendInterface.getOrderList();
    // for (int i = 0; i < orderList.size(); i++) {
    // if (orderList.get(i) == null && !"".equals(orderList.get(i))) {
    // continue;
    // }
    // JSONObject jo = new JSONObject();
    // jo.put("orderDate", sdf1.format(sdf.parse(orderList.get(i))));
    // ja.add(jo);
    // }
    // } catch (Exception e) {
    // logger.debug("分页查询订单数据错误：" + e.getMessage());
    // e.printStackTrace();
    // }
    // resJson.put("resInfo", ja);
    // return ResponsePackUtil.buildPack("0000", resJson);
    // }

    public String getOrderDetailList(String request_body) {
        logger.debug("根据日期获取订单详情:{}", request_body);
        JSONArray ja = new JSONArray();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String orderDate = requestJson.getString("date");
            String telNum = requestJson.getString("telNum");
            if (!StringUtils.checkParam(orderDate, true, 50) || !StringUtils.checkParam(telNum, true, 11))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");

            telNumTmp = telNum;

            orderDate = orderDate.replaceAll("年", "-").replaceAll("月", "");
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
                jo.put("orderItemId", detailItem.getOrderItemId());
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
            logger.debug("分页查询订单数据错误：", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
        resJson.put("resInfo", ja);
        return ResponsePackUtil.buildPack("0000", resJson);
    }

    /**
     * 查询兑换码（创建订单及扣除积分）
     * 
     * @param request
     * @param response
     * @return
     */
    public String getRedeemCodeOrder(String request_body) {
        logger.debug("查询兑换码:{}", request_body);
        JSONObject jo = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String OrderItemID = requestJson.getString("orderItemID");
            String commodityCode = requestJson.getString("commodityCode");
            String telNum = requestJson.getString("telNum");
            if (!StringUtils.checkParam(OrderItemID, true, 50) || !StringUtils.checkParam(commodityCode, true, 50) || !StringUtils.checkParam(telNum, true, 11)) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            }

            telNumTmp = telNum;

            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_commodityCode", commodityCode);
            // 查询商品
            List<VwtJfIntegralcommodityVo> vwtJfInteList = integralSpendInterface.getIntegralcommodityList(commodityCode);
            if (vwtJfInteList == null || vwtJfInteList.size() == 0) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1201, "");
            }
            VwtJfIntegralcommodityVo v = vwtJfInteList.get(0);
            conditions.put("EQ_telNum", telNum);
            conditions.put("EQ_remark", OrderItemID);
            if ("30".equals(v.getCommodityType())) {
                List<VwtJfRedeemCodeVo> jfRedeemCodes = integralSpendInterface.getRedeemCodeByCondition(conditions, null);
                if (jfRedeemCodes == null || jfRedeemCodes.isEmpty())
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1202, "");

                VwtJfRedeemCodeVo codeDetail = jfRedeemCodes.get(0);
                jo.put("goodsId", v.getGoodsId());
                jo.put("commodityCode", v.getCommodityCode());
                jo.put("commodityName", v.getCommodityName());
                jo.put("redeemExplain", v.getRedeemExplain());
                jo.put("point", v.getPoint());
                jo.put("imgSmall", ParamConfig.file_server_url + v.getImgLarge());
                jo.put("redeemCode", codeDetail.getRedeemCode());
                jo.put("commodityType", v.getCommodityType());
            } else {
                jo.put("goodsId", v.getGoodsId());
                jo.put("commodityCode", v.getCommodityCode());
                jo.put("commodityName", v.getCommodityName());
                jo.put("redeemExplain", v.getRedeemExplain());
                jo.put("point", v.getPoint());
                jo.put("imgSmall", v.getImgLarge());
                jo.put("commodityType", v.getCommodityType());
            }
            return ResponsePackUtil.buildPack("0000", jo);
        } catch (Exception e) {
            logger.error("查询兑换码异常,requestbody:{},e:{}", request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
    }

    /**
     * 按照月份统计获得积分并按照日期倒叙展示
     * 
     * @return
     */
    public String getAddPointDateList(String request_body) {
        JSONObject resJson = new JSONObject();
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String telNum = requestJson.getString("telNum");
            JSONObject jo = integralSpendInterface.getThreeMonthAddPointTotal(telNum);

            telNumTmp = telNum;
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
            return ResponsePackUtil.buildPack("0000", resJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        resJson.put("resInfo", "");
        return ResponsePackUtil.buildPack("0000", resJson);
    }

    /**
     * 按照月份统计支出积分并按照日期倒叙展示
     * 
     * @return
     */
    public String getPayPointDateList(String request_body) {
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String telNum = requestJson.getString("telNum");

            telNumTmp = telNum;
            List<Map<String, Object>> list = integralSpendInterface.getThreeMonthPayPointTotal(telNum);// hbaseService.getThreeMonthPayPointTotal(telNum);//vwtJfPayPointsLogService.getPayPointDateList();
            logger.debug("获取积分支出列表:{}", JSONObject.toJSONString(list));
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
    public String getPayPointDetailList(String request_body) {
        logger.debug("获取支出积分明细,requestbody:{}", request_body);
        JSONObject joObj = new JSONObject();
        JSONArray ja = new JSONArray();
        JSONObject resJson = new JSONObject();
        try {
            JSONObject myJsonObject = JSONObject.parseObject(request_body);
            String telNum = myJsonObject.getString("telNum");
            byte[] last_rowkey = myJsonObject.getString("page").getBytes();
            Integer pageSize = myJsonObject.getString("rows") != null && !"".equals(myJsonObject.getString("rows")) ? Integer.parseInt(myJsonObject.getString("rows")) : 0;
            Date thisMonth = myJsonObject.getString("Month") != null && !"".equals(myJsonObject.getString("Month")) ? sdf_month_cn.parse(myJsonObject.getString("Month")) : null;
            JSONObject joList = integralSpendInterface.getMonthPayPointsLogOfPage(telNum, thisMonth, last_rowkey, pageSize);// vwtJfPayPointsLogService.getPayPointDetailList(date);
            logger.debug("获取支出积分明细,json:{}", joList);
            if (null == joList)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");

            telNumTmp = telNum;

            JSONArray jaList = joList.getJSONArray("payPointsLogList");
            String page = joList.getString("lastRowId");
            for (int i = 0; i < jaList.size(); i++) {
                VwtJfPayPointsLogVo obj = jaList.getObject(i, VwtJfPayPointsLogVo.class);
                VwtJfOrderVo order = integralSpendInterface.getOrderData(obj.getOrderid());
                String title = order == null ? "" : order.getOrderTitle();
                JSONObject jo = new JSONObject();
                jo.put("date", sdf.format(obj.getPointsDate()));
                jo.put("count", obj.getCount());
                jo.put("orderName", title);
                ja.add(jo);
            }
            joObj.put("list", ja);
            joObj.put("page", page);
        } catch (Exception e) {
            logger.error("根据月份查询支出详情异常requestbody:{},e:{}", request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
        resJson.put("resInfo", joObj);
        return ResponsePackUtil.buildPack("0000", resJson);
    }

    /**
     * 获取订单明细的月份
     * 
     * @return
     */
    public String getMonthList(String request_body) {
        logger.debug("获取订单明细的月份:{}", request_body);
        try {
            String starttime = "2017-11-01 00:00:00";
            Date now = new Date();
            String endtime = sdf.format(now);
            List<String> showList = new ArrayList<String>();
            List<String> list = queryShowTime(starttime, endtime);
            if (list != null && !list.isEmpty()) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    String time = list.get(i);
                    time = time.substring(0, time.indexOf("-")) + "年" + time.substring(time.indexOf("-") + 1) + "月";
                    showList.add(time);
                }
            }
            return ResponsePackUtil.buildPack("0000", showList);
        } catch (Exception e) {
            logger.error("积分-获取订单明细月份异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1200, "");
        }
    }

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
        Month = Month.replaceAll("年", "").replaceAll("月", "");
        logger.debug("分页获取指定月份详细收入积分HTML5(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(telNum, Month))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1042, "");

        telNumTmp = telNum;

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
        SerializeConfig ser = new SerializeConfig();
        ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        return ResponsePackUtil.buildPack("0000", resInfo, ser);
    }
}
