package com.royasoft.vwt.cag.util;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.util.mq.LogRocketMqUtil;
import com.royasoft.vwt.cag.util.mq.RocketMqUtil;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;

@Component
public class IntegralUtil {

    private final static Logger logger = LoggerFactory.getLogger(IntegralUtil.class);

    @Autowired
    private ClientUserInterface clientUserInterface;

    /**
     * 积分行为记录 积分计算
     * 
     * @param telNum 手机号码
     * @param EQ_actionType 积分规则5大类，存码表值，10注册新用户、20完善资料,201修改头像、30签到、40工作圈，401发表说说，402回复，403赞、50邀请用户、60积分返还、70积分补充
     * @return
     */
    public void integralSigns(String telNum, String EQ_actionType) {
        logger.debug("积分行为记录,telNum:{},EQ_actionType:{}", telNum, EQ_actionType);
        try {
            /** 2016-10-28 14:34:09 此段代码修改原因：防止HBASE的问题引起其他业务的正常流转，此处改为直接入MQ */
            JSONObject json = new JSONObject();
            json.put("telNum", telNum);
            json.put("actionType", EQ_actionType);
            LogRocketMqUtil.send(RocketMqUtil.integralDealQueue, json.toJSONString());
            /** 2016-10-28 14:34:09 以下代码注释，mq_consumer，由mq_consumer处理进程来维护积分 */
            // ClientUserVO clientUserVO = clientUserInterface.findByTelNum(telNum).get(0);
            //
            // Map<String, Object> conditions = new HashMap<String, Object>();
            // Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();
            //
            // Date currentDate = new Date();
            // conditions.put("EQ_actionType", EQ_actionType);
            // sortMap.put("actionType", true);
            // List<IntegralActionVo> integralActionVos = integralInterface.findIntegralActionByCondition(conditions, sortMap);
            // logger.debug("签到(获取签到相关积分行为),telNum:{},integralActionVos:{}", telNum, JSONObject.toJSONString(integralActionVos));
            // if (null != integralActionVos && integralActionVos.size() == 1) {
            // IntegralActionVo integralActionVo = integralActionVos.get(0);
            // String ruleRowId = integralActionVo.getRuleRowId();
            // int actionDelFlag = integralActionVo.getDelFlag(); // 查询该行为是否删除 0:未删除 1:删除
            // if (actionDelFlag != 0)
            // return;
            //
            // int signCount = Integer.valueOf(String.valueOf(integralActionVo.getPointQuantity())); // 单次积分数量
            // conditions.clear();
            // sortMap.clear();
            // conditions.put("end_time_startDate", dateFormatDetail.format(new Date()));
            // conditions.put("start_time_endDate", dateFormatDetail.format(new Date()));
            // conditions.put("EQ_rowId", ruleRowId);
            // sortMap.put("rowId", true);
            // List<IntegralRuleVo> integralRuleVos = integralInterface.findIntegralRuleByCondition(conditions, sortMap);
            // logger.debug("签到(获取签到相关积分规则),telNum:{},integralRuleVos:{}", telNum, JSONObject.toJSONString(integralRuleVos));
            // if (null != integralRuleVos && integralRuleVos.size() == 1) {
            // IntegralRuleVo integralRuleVo = integralRuleVos.get(0);
            // int isEnable = integralRuleVo.getIsEnable(); // 查询是否启用 0停用，1启用
            // int delFlag = integralRuleVo.getDelFlag(); // 查询该规则是否删除 0:未删除 1:删除
            // long limitQuantity = integralRuleVo.getLimitQuantity(); // 封顶积分数量
            // if (isEnable != 1)
            // return;
            //
            // if (delFlag != 0)
            // return;
            //
            // if (integralRuleVo.getIslimit() == 1) {// 封顶
            // if (integralRuleVo.getIsOverlimit().equals("0")) {// 赠送
            // if (integralRuleVo.getIsMonth().equals("0")) {// 不按月
            // if (integralRuleVo.getIsContinue().equals("0")) {// 连续
            // // List<IntegralLogVo> integralLogVos = integralInterface.findIntegralLogMonthByTypeTelnum(telNum, "30");
            //
            // } else {// 不连续
            //
            // }
            // } else {// 按月
            // saveIntegralLog("", signCount, signCount, "", currentDate, "", integralActionVo.getActionType(), telNum, clientUserVO.getUserId(),
            // integralInterface,
            // integralActionVo.getRuleRowId());
            // int currentMonthDay = getCurrentMonthLastDay();
            // if (checkIsLastOfMonth(currentMonthDay)) {
            // List<String> monthSign = signInterface.findCurrentMonthSignInfo(telNum, currentDate);
            // if (null != monthSign && monthSign.size() == currentMonthDay) {
            // int addCount = Integer.valueOf(String.valueOf(integralRuleVo.getLimitQuantity())) - currentMonthDay
            // * Integer.valueOf(String.valueOf(integralActionVo.getPointQuantity())); // 赠送的积分
            // saveIntegralLog("", addCount, addCount, "", currentDate, "", "70", telNum, clientUserVO.getUserId(), integralInterface,
            // integralActionVo.getRuleRowId());
            // }
            // }
            // }
            // } else {// 不赠送
            // if (integralRuleVo.getIsMonth().equals("0")) {// 不按月
            // if (integralRuleVo.getIsContinue().equals("0")) {// 连续
            //
            // } else {// 不连续
            //
            // }
            // } else {// 按月
            // /** 查询当月的积分值，判断是否达到上限 */
            // List<IntegralLogVo> logList = integralInterface.findIntegralLogRule(telNum, new Date(), integralRuleVo.getRowId());
            // int count = 0;
            // if (null != logList && !logList.isEmpty()) {
            // for (IntegralLogVo integralLogVo : logList) {
            // count += integralLogVo.getCountReal();
            // }
            //
            // }
            //
            // long distanceNum = limitQuantity - count;
            // if (distanceNum <= 0)
            // distanceNum = 0;
            //
            // if (distanceNum <= signCount) {
            // saveIntegralLog("", signCount, (int) distanceNum, "", currentDate, "", integralActionVo.getActionType(), telNum,
            // clientUserVO.getUserId(), integralInterface,
            // integralActionVo.getRuleRowId());
            // } else {
            // saveIntegralLog("", signCount, signCount, "", currentDate, "", integralActionVo.getActionType(), telNum, clientUserVO.getUserId(),
            // integralInterface,
            // integralActionVo.getRuleRowId());
            // }
            // }
            // }
            // } else {// 不封顶
            // saveIntegralLog("", signCount, signCount, "", currentDate, "", integralActionVo.getActionType(), telNum, clientUserVO.getUserId(),
            // integralInterface,
            // integralActionVo.getRuleRowId());
            // }
            // } else {
            // saveIntegralLog("", signCount, signCount, "", currentDate, "", integralActionVo.getActionType(), telNum, clientUserVO.getUserId(),
            // integralInterface,
            // integralActionVo.getRuleRowId());
            // }
            //
            // }
        } catch (Exception e) {
            logger.error("积分记录错误", e);
        }
    }

    /**
     * 取得当月天数
     * */
    public static int getCurrentMonthLastDay() {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);// 把日期设置为当月第一天
        a.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }
}
