package com.royasoft.vwt.cag.service;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.util.IMSUtil;
import com.royasoft.vwt.cag.util.IntegralUtil;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.mq.LogRocketMqUtil;
import com.royasoft.vwt.cag.util.mq.RocketMqUtil;
import com.royasoft.vwt.common.security.RSAUtil;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.dictionary.api.vo.DictionaryVo;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.VerifyCodeInterface;
import com.royasoft.vwt.soa.business.meeting.api.interfaces.MeetingInterface;
import com.royasoft.vwt.soa.business.meeting.api.vo.ImsErrorVO;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

@Component
public class CommonService {

    private final Logger logger = LoggerFactory.getLogger(CommonService.class);

    @Autowired
    private IntegralUtil integralUtil;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Autowired
    private MemberInfoUtil memberInfoUtil;

    @Autowired
    private MeetingInterface meetingInterface;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private DictionaryInterface dictionaryInterface;

    @Autowired
    private VerifyCodeInterface verifyCodeInterface;

    @Autowired
    private IMSUtil imsUtil;

    private static final String VERIFY_CODE_TEL = "VERIFY:CODE:TEL:";

    private static final String SMS_VERIFY_CODE_COUNT = "VWT:SMS:VERIFY:CODE:COUNT:";

    private static final String SMS_VERIFY_CODE = "VWT:SMS:VERIFY:CODE:";

    private static final String SMS_VERIFY_CODE_COUNT_TODAY = "VWT:SMS:VERIFY:CODE:COUNT:TODAY:";

    public boolean doActiveUser(List<MemberInfoVO> memberInfoVOs, String imei, String telNum) {
        /** 激活用户后保存至vwt_client_user表 */
        if (!saveClientUser(memberInfoVOs, "", imei))
            return false;
        /** 用户注册成功，获得积分 ，10注册新用户 */
        integralUtil.integralSigns(telNum, "10");

        /** 处理激活营销信息 */
        dealActiveInfo(telNum);

        if (!openIms(memberInfoVOs.get(0).getMemId(), telNum))
            return false;
        return true;
    }

    /**
     * 激活用户后保存至vwt_client_user表
     * 
     * @param clientUser
     * @param password
     * @return
     * @Description:
     */
    private boolean saveClientUser(List<MemberInfoVO> memberInfoVOs, String password, String imei) {
        logger.debug("激活用户后保存至vwt_client_user表,memberInfoVOs:{},password:{}", JSON.toJSONString(memberInfoVOs), password);
        try {
            for (MemberInfoVO memberInfoVO : memberInfoVOs) {
                ClientUserVO userVO = new ClientUserVO();
                userVO.setCorpId(memberInfoVO.getCorpId());
                userVO.setCreateTime(new Date());
                userVO.setDeptId(memberInfoVO.getDeptId());
                userVO.setFromChannel(memberInfoVO.getFromChannel());
                userVO.setPwd(password);
                userVO.setTelNum(memberInfoVO.getTelNum());
                userVO.setUserId(memberInfoVO.getMemId());
                userVO.setUserName(memberInfoVO.getMemberName());
                userVO.setUserState("Y");
                userVO.setImsi(imei);
                // userVO.setClique("1");
                clientUserInterface.saveUser(userVO);

                memberInfoVO.setMemStatus("1");
                memberInfoUtil.saveMemberInfo(memberInfoVO, memberInfoVO.getFromChannel());

            }
        } catch (Exception e) {
            logger.error("激活用户后保存至vwt_client_user表异常,memberInfoVOs:{},password:{}", JSON.toJSONString(memberInfoVOs), password, e);
            return false;
        }
        return true;
    }

    /**
     * 处理激活营销活动信息
     * 
     * @param clientUser
     * @param telNum
     * @Description:
     */
    private void dealActiveInfo(String telNum) {
        logger.debug("处理激活营销活动信息入MQ，telNum:{}", telNum);
        if (null == telNum || "".equals(telNum))
            return;
        LogRocketMqUtil.send(RocketMqUtil.activeDealQueue, telNum);
    }

    /**
     * IMS开户
     * 
     * @param userid
     * @param telnum
     */
    public boolean openIms(String userid, String telnum) {
        try {
            String result = imsUtil.registeOrcancelIMS(telnum, ParamConfig.ims_cmd_open);
            logger.debug("IMS开户结果:{},telnum:{},cmd:{}", result, telnum, ParamConfig.ims_cmd_open);
            if (!org.springframework.util.StringUtils.isEmpty(result)) {
                JSONObject json = JSONObject.parseObject(result);
                // 调用HTTP接口开户IMS失败
                if (!"200".equals(json.getString("status"))) {
                    ImsErrorVO iev = new ImsErrorVO();
                    iev.setId(UUID.randomUUID().toString());
                    iev.setTelnum(telnum);
                    iev.setTime(new Date());
                    iev.setUserid(userid);
                    meetingInterface.saveImsError(iev);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("记录IMS开户失败信息 异常,telnum:{},e:{}", telnum, e);
            return false;
        }
    }

    /**
     * 校验是否可以下发验证码
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public boolean checkVerifyCodeIsExist(String telNum) {
        if (null == telNum || "".equals(telNum))
            return false;
        String interStr = redisInterface.getString(VERIFY_CODE_TEL + telNum);
        if (null == interStr || "".equals(interStr))
            return true;
        return false;
    }

    /**
     * 获取字典表地市信息
     * 
     * @return
     * @Description:
     */
    public List<String> getAllRegionName() {
        List<DictionaryVo> dictionaryVos = dictionaryInterface.findDictionaryByDictIdAndDictDesc(51L, "地市");
        if (null == dictionaryVos || dictionaryVos.isEmpty())
            return null;
        List<String> regionInfo = new ArrayList<String>();
        for (DictionaryVo dictionaryVo : dictionaryVos) {
            if (!"省直".equals(dictionaryVo.getDictKeyDesc()) && !"省测试".equals(dictionaryVo.getDictKeyDesc()))
                regionInfo.add(dictionaryVo.getDictKeyDesc());
        }
        /** 注意：是根据的汉字的拼音的字母排序的，而不是根据汉字一般的排序方法 */
        Collections.sort(regionInfo, Collator.getInstance(java.util.Locale.CHINA));
        return regionInfo;
    }

    /**
     * 校验验证码
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public String valicateVerifyCode(String telNum) {
        /** 校验短信验证码 */
        String codeReal = redisInterface.getString(SMS_VERIFY_CODE + telNum);
        logger.debug("校验验证码,telNum:{},codeReal:{}", telNum, codeReal);
        if (null == codeReal || "".equals(codeReal))
            return null;

        String codeCount = redisInterface.getString(SMS_VERIFY_CODE_COUNT + telNum);
        if (null == codeCount || "".equals(codeCount))
            return null;
        int codeCountInt = 0;
        try {
            codeCountInt = Integer.valueOf(codeCount);
            if (codeCountInt >= 5)
                return null;
        } catch (Exception e) {
            logger.error("校验验证码验证次数异常", e);
            return null;
        } finally {
            redisInterface.setString(SMS_VERIFY_CODE_COUNT + telNum, String.valueOf(codeCountInt + 1), 300);
        }
        return codeReal;
    }

    /**
     * 移除redis验证码信息
     * 
     * @param telNum
     * @Description:
     */
    public void removeVerifyInfo(String telNum) {
        logger.debug("移除redis验证码信息,telNum:{}", telNum);
        redisInterface.del(SMS_VERIFY_CODE_COUNT + telNum);
        redisInterface.del(SMS_VERIFY_CODE + telNum);
        redisInterface.del(VERIFY_CODE_TEL + telNum);
    }

    /**
     * 校验token
     * 
     * @param token
     * @param telNum
     * @param userId
     * @return
     * @Description:
     */
    public boolean valicateToken(String token, String telNum, String userId) {
        String rsa_private_key = ParamConfig.rsa_private_key;
        try {
            String tokenClear = RSAUtil.decryptPrivateKey(token, rsa_private_key);
            if (null == tokenClear || "".equals(tokenClear))
                return false;
            String[] tokenArray = tokenClear.split(":");
            if (tokenArray.length != 3)
                return false;
            String telNumToken = tokenArray[0];
            String userIdToken = tokenArray[1];
            if (telNumToken.equals(telNum) && userIdToken.equals(userId))
                return true;
            return false;
        } catch (Exception e) {
            logger.error("token校验异常,telNum:{}", telNum, e);
            return false;
        }
    }

    /**
     * 校验当天下发验证码次数并保存验证码下发次数
     * 
     * @param telNum
     * @Description:需求为：ICT_VWT_REQ20160630
     */
    public boolean saveSmsCount(String telNum) {
        logger.debug("校验当天下发验证码次数并保存验证码下发次数,telNum:{}", telNum);
        if (!StringUtils.stringIsNotNull(telNum))
            return false;
        String nowCount = redisInterface.getString(SMS_VERIFY_CODE_COUNT_TODAY + telNum);
        int nowCountInt = 1;
        if (StringUtils.stringIsNotNull(nowCount))
            nowCountInt = Integer.valueOf(nowCount);
        int systemCount = null == ParamConfig.sms_daily_count ? 20 : Integer.valueOf(ParamConfig.sms_daily_count);
        logger.debug("校验当天下发验证码次数并保存验证码下发次数,systemCount:{},nowCountInt:{},telNum:{}", systemCount, nowCountInt, telNum);

        if (nowCountInt > systemCount)
            return false;
        int remainSecond = getDayRemainSecond();
        if (remainSecond == -1)
            return true;
        redisInterface.setString(SMS_VERIFY_CODE_COUNT_TODAY + telNum, String.valueOf(nowCountInt + 1), remainSecond);
        return true;
    }

    /**
     * 获取当天剩下秒值
     * 
     * @return
     * @Description:需求为：ICT_VWT_REQ20160630
     */
    private int getDayRemainSecond() {
        try {
            Calendar curDate = Calendar.getInstance();
            Calendar tommorowDate = new GregorianCalendar(curDate.get(Calendar.YEAR), curDate.get(Calendar.MONTH), curDate.get(Calendar.DATE) + 1, 0, 0, 0);
            // Calendar tommorowDate = new GregorianCalendar(curDate.get(Calendar.YEAR), curDate.get(Calendar.MONTH), curDate.get(Calendar.DATE) + 0,
            // 21, 50, 0);
            return (int) (tommorowDate.getTimeInMillis() - curDate.getTimeInMillis()) / 1000;
        } catch (Exception e) {
            logger.error("获取当天剩下秒值异常", e);
            return -1;
        }

    }

}
