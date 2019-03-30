/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.IMSUtil;
import com.royasoft.vwt.cag.util.IntegralUtil;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.PinyinTool;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.VoiceCodeUtil;
import com.royasoft.vwt.cag.util.mq.LogRocketMqUtil;
import com.royasoft.vwt.cag.util.mq.MsgPushUtil;
import com.royasoft.vwt.cag.util.mq.RocketMqUtil;
import com.royasoft.vwt.cag.util.mq.UserAndCorpRocketMqUtil;
import com.royasoft.vwt.cag.vo.CorpLogVO;
import com.royasoft.vwt.cag.vo.UserLogVO;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.dictionary.api.vo.DictionaryVo;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendSmsInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.VerifyCodeInterface;
import com.royasoft.vwt.soa.business.invite.api.interfaces.InviteShareInterface;
import com.royasoft.vwt.soa.business.invite.api.vo.InviteShareVo;
import com.royasoft.vwt.soa.business.meeting.api.interfaces.MeetingInterface;
import com.royasoft.vwt.soa.business.meeting.api.vo.ImsErrorVO;
import com.royasoft.vwt.soa.integral.api.interfaces.IntegralInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.SignInterface;
import com.royasoft.vwt.soa.sundry.memberactive.api.interfaces.MemberActiveInterface;
import com.royasoft.vwt.soa.systemsettings.gatedlaunch.api.interfaces.GatedlaunchInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.clique.api.interfaces.CliqueInfoInterface;
import com.royasoft.vwt.soa.uic.clique.api.vo.CliqueInfoVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.customer.api.interfaces.CustomerInterface;
import com.royasoft.vwt.soa.uic.customer.api.vo.CustomerVo;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.HLWMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 2.1版本设置业务处理
 *
 * @Author:MB
 * @Since:2016年5月25日
 */
@Scope("prototype")
@Service
public class SettingNewService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SettingNewService.class);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private InviteShareInterface inviteShareInterface;

    @Autowired
    private DatabaseInterface databaseInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Autowired
    private CorpInterface corpInterface;
    @Autowired
    private DepartMentInterface departMentInterface;
    @Autowired
    private MemberInfoUtil memberInfoUtil;

    @Autowired
    private CliqueInfoInterface cliqueInfoInterface;

    @Autowired
    private SignInterface signInterface;

    @Autowired
    private MsgPushUtil msgPushUtil;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private IntegralInterface integralInterface;

    @Autowired
    private DictionaryInterface dictionaryInterface;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private GatedlaunchInterface gatedlaunchInterface;

    @Autowired
    private SendSmsInterface sendSmsInterface;

    @Autowired
    private VerifyCodeInterface verifyCodeInterface;

    @Autowired
    private MemberActiveInterface memberActiveInterface;

    @Autowired
    private HLWMemberInfoInterface hlwMemberInfoInterface;

    @Autowired
    private MeetingInterface meetingInterface;

    @Autowired
    private CustomerInterface customerInterface;

    @Autowired
    private IntegralUtil integralUtil;

    @Autowired
    private VoiceCodeUtil voiceCodeUtil;

    @Autowired
    private CommonService commonService;

    @Autowired
    private IMSUtil IMSUtil;

    private static final String INTERNET_USERINFO_TEL = "INTERNET:USERINFO:TEL:";

    private static final String INTERNET_USERINFO_TEL_OUTTIME = "INTERNET:OUTTIME:USERINFO:TEL:";

    private static final String COMMON_USERINFO_TEL = "COMMON:USERINFO:TEL:";

    private static final String VERIFY_CODE_TEL = "VERIFY:CODE:TEL:";
    
    private static final String SEND_INTERVAL_TEL = "SEND:INTERVAL:TEL:";
    
    /** 发送短信验证码间隔*/
    public static final int  SendSmsInterval = 60;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.settingNew_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String tel_number = queue_packet.getTel_number();
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    logger.debug("2.1版本设置业务处理(入口),function_id:{},user_id:{},tel_number:{},request_body:{}", function_id, user_id, tel_number, request_body);
                    /***************************** 业务逻辑处理 *********************************************/

                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果
                    switch (function_id) {
                        case FunctionIdConstant.ST_SENDSMSCODE:
                            resInfo = sendVerifyCodeSMS(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.ST_SENDVOICECODE:
                            resInfo = sendVerifyCodeVoice(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.ST_FORGETSMS:
                            resInfo = sendForgetSmsVerifyCode(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.ST_FORGETPWD:
                            resInfo = checkForgetAndSetting(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.ST_VALICATECODE:
                            resInfo = valicateSmsVerifyCode(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.ST_INTERNETPWD:
                            resInfo = internetSetPwd(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.ST_INTERNETPREFECT:
                            resInfo = prefectInternetInfo(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.ST_OLDPWD:
                            resInfo = setPwdAsOld(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        case FunctionIdConstant.ST_TOKEN:
                            resInfo = setPwdAsToken(request_body);
                            user_id = ResponsePackUtil.getResBody(resInfo);
                            tel_number = ResponsePackUtil.getResBody(resInfo);
                            break;
                        default:
                            break;
                    }
                    logger.debug("2.1版本设置业务处理(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("2.1版本设置业务处理异常", e);
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
     * 下发短信验证码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String sendVerifyCodeSMS(String requestBody) {
        logger.debug("下发短信验证码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        logger.debug("下发短信验证码(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);
        
        
        if (!commonService.checkVerifyCodeIsExist(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2707, telNum);
        
        if(!commonService.saveSmsCount(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2712, telNum);
        
        /** 生成短信验证码 */
        String verifyCode = getRandomString();
        String smsContent = getSmsContent("您好，移动社区动态密码是：", verifyCode, "，有效期为10分钟。安全提醒：切勿向他人泄露，以防上当受骗。","");
        /** 校验号码是否存在 */
        List<MemberInfoVO> list=memberInfoUtil.findMemberInfosByTelNum(telNum);
        if (null == list || list.isEmpty())
            smsContent = "您好，您还不是移动社区用户。";
        
        logger.debug("下发短信验证码(下发短信),telNum:{},smsContent:{}", telNum, smsContent);

        if (org.apache.commons.lang3.StringUtils.isNotEmpty(redisInterface.getString(SEND_INTERVAL_TEL+telNum)) || !saveVerifyCode(telNum, verifyCode, smsContent)){
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, telNum);
        }
            
        //设置验证码发送间隔
        redisInterface.setString(SEND_INTERVAL_TEL + telNum, verifyCode, SendSmsInterval);
        
        redisInterface.setString(VERIFY_CODE_TEL + telNum, verifyCode,
                null == ParamConfig.verify_code_timeout || "".equals(ParamConfig.verify_code_timeout) ? 120 : Integer.valueOf(ParamConfig.verify_code_timeout));

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * 下发语音验证码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String sendVerifyCodeVoice(String requestBody) {
        logger.debug("下发语音验证码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        logger.debug("下发语音验证码(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        if (!commonService.checkVerifyCodeIsExist(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2707, telNum);

        /** 生成短信验证码 */
        String verifyCode = getRandomString();
        logger.debug("下发语音验证码(生成验证码),telNum:{},verifyCode:{}", telNum, verifyCode);

        if (!voiceIntoMq(telNum, verifyCode))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2706, telNum);

        if (!saveVerifyCodeOnly(telNum, verifyCode))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, telNum);

        redisInterface.setString(VERIFY_CODE_TEL + telNum, verifyCode,
                null == ParamConfig.verify_code_timeout || "".equals(ParamConfig.verify_code_timeout) ? 120 : Integer.valueOf(ParamConfig.verify_code_timeout));

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * 校验短信验证码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String valicateSmsVerifyCode(String requestBody) {
        logger.debug("校验短信验证码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        String verifyCode = requestJson.getString("verifyCode");
        String imei = requestJson.getString("imei");

        logger.debug("校验短信验证码(解析body),telNum:{},verifyCode:{},imei:{}", telNum, verifyCode, imei);

        /** 校验参数 */
        if (!valicateParams(telNum, verifyCode))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        String codeReal = commonService.valicateVerifyCode(telNum);
        if (null == codeReal || "".equals(codeReal))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1020, getTipInfo("该验证码已失效"));

        if (!codeReal.equals(verifyCode))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1016, getTipInfo("验证码错误"));

        /** 移除redis验证码信息 */
        commonService.removeVerifyInfo(telNum);

        /** 之后流程类似sessionId */
        String valicateCode = UUID.randomUUID().toString();

        /** 校验该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = checkUserExist(telNum);
        logger.debug("校验短信验证码(校验用户),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        if (null == memberInfoVOs || memberInfoVOs.isEmpty()) {
            redisInterface.setString(INTERNET_USERINFO_TEL + telNum, valicateCode,
                    null == ParamConfig.internet_user_timeout || "".equals(ParamConfig.internet_user_timeout) ? 120 : Integer.valueOf(ParamConfig.internet_user_timeout));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sessionId", valicateCode);
            List<String> regionNames = commonService.getAllRegionName();
            jsonObject.put("regionInfo", regionNames);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2701, jsonObject.toJSONString());
        }

        /** 校验该用户是否注册 */
        List<ClientUserVO> clientUserVOs = checkUserActive(telNum);
        logger.debug("校验短信验证码(校验该用户是否注册),telNum:{},clientUserVOs:{}", telNum, JSON.toJSONString(clientUserVOs));
        if (null != clientUserVOs && !clientUserVOs.isEmpty()) {
            redisInterface.setString(COMMON_USERINFO_TEL + telNum, valicateCode,
                    null == ParamConfig.common_user_timeout || "".equals(ParamConfig.common_user_timeout) ? 120 : Integer.valueOf(ParamConfig.common_user_timeout));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sessionId", valicateCode);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2702, jsonObject.toJSONString());
        } else {
            /** 激活用户后保存至vwt_client_user表 */
            if (!saveClientUser(memberInfoVOs, "", imei))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, telNum);
            /** 用户注册成功，获得积分 ，10注册新用户 */
            integralUtil.integralSigns(telNum, "10");

            /** 处理激活营销信息 */
            dealActiveInfo(telNum);

            openIms(memberInfoVOs.get(0).getMemId(), telNum);

            redisInterface.setString(COMMON_USERINFO_TEL + telNum, valicateCode,
                    null == ParamConfig.common_user_timeout || "".equals(ParamConfig.common_user_timeout) ? 120 : Integer.valueOf(ParamConfig.common_user_timeout));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sessionId", valicateCode);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2703, jsonObject.toJSONString());
        }
    }

    /**
     * 互联网个人注册设置密码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String internetSetPwd(String requestBody) {
        logger.debug("互联网个人注册设置密码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        String sessionId = requestJson.getString("sessionId");
        String password = requestJson.getString("password");

        logger.debug("互联网个人注册设置密码(解析body),telNum:{},sessionId:{},password:{}", telNum, sessionId, password);

        /** 校验参数 */
        if (!valicateParams(telNum, sessionId, password))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 校验该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = checkUserExist(telNum);
        logger.debug("互联网个人注册设置密码(校验用户),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        if (null != memberInfoVOs && !memberInfoVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2705, telNum);

        String sessionIdReal = redisInterface.getString(INTERNET_USERINFO_TEL + telNum);
        if (null == sessionIdReal || "".equals(sessionIdReal) || !sessionId.equals(sessionIdReal))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2704, telNum);

        JSONObject redisJson = new JSONObject();
        redisJson.put("telNum", telNum);
        redisJson.put("password", password);
        redisInterface.setString(INTERNET_USERINFO_TEL_OUTTIME + telNum, redisJson.toJSONString(), null == ParamConfig.internet_user_timeout || "".equals(ParamConfig.internet_user_timeout) ? 600
                : Integer.valueOf(ParamConfig.internet_user_timeout));

        List<String> regionNames = commonService.getAllRegionName();
        JSONObject returnJson = new JSONObject();
        returnJson.put("regionInfo", regionNames);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", returnJson.toJSONString());
    }

    /**
     * 互联网个人注册完善资料
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String prefectInternetInfo(String requestBody) {
        logger.debug("互联网个人注册完善资料,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        String memberName = requestJson.getString("memberName");
        String corpName = requestJson.getString("corpName");
        String regionName = requestJson.getString("regionName");
        String sessionId = requestJson.getString("sessionId");

        logger.debug("互联网个人注册完善资料(解析body),telNum:{},memberName:{},corpName:{},regionName:{},sessionId:{}", telNum, memberName, corpName, regionName, sessionId);

        /** 校验参数 */
        if (!valicateParams(telNum, memberName, corpName, regionName, sessionId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 判断是否超时 */
        String sessionIdReal = redisInterface.getString(INTERNET_USERINFO_TEL + telNum);
        if (null == sessionIdReal || "".equals(sessionIdReal) || !sessionId.equals(sessionIdReal))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2704, telNum);

        String registAccountInfo = redisInterface.getString(INTERNET_USERINFO_TEL_OUTTIME + telNum);
        JSONObject registAccountJson = JSON.parseObject(registAccountInfo);

        List<CorpVO> corpVOs = corpInterface.findCorpByCorpName(corpName);
        if (null != corpVOs && !corpVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1047, telNum);

        /** 体验集团开户 */
        DepartMentVO corpCreateRes = doCreateCorp(telNum, corpName, memberName, regionName);
        if (null == corpCreateRes)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1048, telNum);

        DictionaryVo dictionaryVoRegion = dictionaryInterface.findDictionaryByIdKeyDesc("地市", regionName);
        String regionCode = null == dictionaryVoRegion ? "" : dictionaryVoRegion.getDictKey();
        MemberInfoVO memberInfoVORes = doCreateMemberInfo(telNum, memberName, "", corpCreateRes, regionCode, regionCode);
        if (null == memberInfoVORes)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1049, telNum);

        boolean clientUserRes = doCreateClientUser(telNum, memberName, null == registAccountJson ? "" : registAccountJson.getString("password"), corpCreateRes, memberInfoVORes, "", regionCode,
                regionCode, corpName);
        if (!clientUserRes)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1050, telNum);
        /** 处理激活营销信息 */
        dealActiveInfo(telNum);
        /** 用户注册成功，获得积分 ，10注册新用户 */
        integralUtil.integralSigns(telNum, "10");

        /** IMS 开户 */
        openIms(memberInfoVORes.getMemId(), telNum);

        /** 对新注册的用户进行欢迎推送 */

        /** 开户完成清除redis */
        redisInterface.del(INTERNET_USERINFO_TEL_OUTTIME + telNum);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * 忘记密码用户校验并下发短信验证码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String sendForgetSmsVerifyCode(String requestBody) {
        logger.debug("忘记密码用户校验并下发短信验证码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");

        logger.debug("忘记密码用户校验并下发短信验证码(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (null == telNum || "".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 校验该用户是否注册 */
        List<ClientUserVO> clientUserVOs = checkUserActive(telNum);
        logger.debug("忘记密码用户校验并下发短信验证码(校验该用户是否注册),telNum:{},clientUserVOs:{}", telNum, JSON.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1015, getTipInfo("用户账号尚未注册，请先注册账号再登录"));

        return sendVerifyCodeSMS(requestBody);
    }

    /**
     * 忘记密码校验验证码并设置密码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String checkForgetAndSetting(String requestBody) {
        logger.debug("忘记密码校验验证码并设置密码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");
        String verifyCode = requestJson.getString("verifyCode");
        String password = requestJson.getString("password");

        logger.debug("忘记密码校验验证码并设置密码(解析body),telNum:{},verifyCode:{},password:{}", telNum, verifyCode, password);

        /** 校验参数 */
        if (!valicateParams(telNum, verifyCode, password))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, telNum);

        /** 校验该用户是否注册 */
        List<ClientUserVO> clientUserVOs = checkUserActive(telNum);
        logger.debug("忘记密码校验验证码并设置密码(校验该用户是否注册),telNum:{},clientUserVOs:{}", telNum, JSON.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1015, telNum);

        /** 校验短信验证码并编辑密码 */
        String codeReal = commonService.valicateVerifyCode(telNum);
        if (null == codeReal || "".equals(codeReal))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1020, getTipInfo("该验证码已失效"));

        if (!codeReal.equals(verifyCode))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1016, getTipInfo("验证码错误"));

        /** 移除redis验证码信息 */
        commonService.removeVerifyInfo(telNum);

        /** 修改vwt_client_user表用户密码 */
        if (!editClientUserPwd(clientUserVOs, password))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, telNum);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", telNum);
    }

    /**
     * 根据原密码修改密码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String setPwdAsOld(String requestBody) {
        logger.debug("根据原密码修改密码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String userId = requestJson.getString("userId");
        String telNum = requestJson.getString("telNum");
        String oldPwd = requestJson.getString("oldPwd");
        String newPwd = requestJson.getString("newPwd");

        logger.debug("根据原密码修改密码(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, telNum, oldPwd, newPwd))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, userId);

        /** 校验该用户是否注册 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1015, getTipInfo("用户账号尚未注册，请先注册账号再登录"));

        if (null == clientUserVO.getPwd() || "".equals(clientUserVO.getPwd()) || !oldPwd.equals(clientUserVO.getPwd()))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2709, userId);

        savePwd(telNum, newPwd);

        return ResponsePackUtil.buildPack("0000", userId);
    }

    /**
     * 根据token修改密码
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String setPwdAsToken(String requestBody) {
        logger.debug("根据token修改密码,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String userId = requestJson.getString("userId");
        String telNum = requestJson.getString("telNum");
        String token = requestJson.getString("token");
        String newPwd = requestJson.getString("newPwd");

        logger.debug("根据token修改密码(解析body),telNum:{}", telNum);

        /** 校验参数 */
        if (!StringUtils.checkParamNull(userId, telNum, token, newPwd))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1018, userId);

        /** 校验该用户是否注册 */
        ClientUserVO clientUserVO = clientUserInterface.findById(userId);
        if (null == clientUserVO)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1015, getTipInfo("用户账号尚未注册，请先注册账号再登录"));

        if (!commonService.valicateToken(token, telNum, userId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2711, userId);
        savePwd(telNum, newPwd);

        return ResponsePackUtil.buildPack("0000", userId);
    }

    /**
     * 修改密码
     * 
     * @param telNum
     * @param pwd
     * @Description:
     */
    private void savePwd(String telNum, String pwd) {
        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
        for (ClientUserVO clientUserVO : clientUserVOs) {
            try {
                clientUserVO.setPwd(pwd);
                clientUserInterface.saveUser(clientUserVO);
            } catch (Exception e) {
                logger.error("修改密码异常", e);
            }
        }
    }

    /**
     * IMS开户
     * 
     * @param userid
     * @param telnum
     */
    public void openIms(String userid, String telnum) {
        try {
            String result = IMSUtil.registeOrcancelIMS(telnum, ParamConfig.ims_cmd_open);
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
        } catch (Exception e) {
            logger.error("记录IMS开户失败信息 异常,telnum:{},e:{}", telnum, e);
        }
    }

    /**
     * 激活后修改邀请记录表
     * 
     * @param memberInfoVOs
     */
    public void updateShareInfo(String telNum) {
        try {
            List<InviteShareVo> list = inviteShareInterface.findInviteShareListByTelnum(telNum);
            if (list == null || list.isEmpty())
                return;
            InviteShareVo isv = list.get(0);
            isv.setActivetime(sdf.format(new Date()));
            isv.setType(1);
            inviteShareInterface.saveInviteRecord(isv);
        } catch (Exception e) {
            logger.error("激活后修改邀请记录表异常,telnum:{},e:{}", telNum, e);
        }
    }

    /**
     * 获取灰度发布开关
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String findGrayReleaseByCorpID(String requestBody, String userId) {
        logger.debug("获取灰度发布开关,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String corpid = requestJson.getString("corpid");

        logger.debug("获取灰度发布开关(解析body),corpid:{}", corpid);
        /** 校验参数 */
        if (!StringUtils.checkParamNull(corpid))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1034, "");

        /** 灰度发布开关 */
        boolean grayRelease = gatedlaunchInterface.findGrayReleaseByCorpID(corpid);

        logger.debug("获取灰度发布开关,grayRelease:{}", grayRelease);

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("grayRelease", grayRelease);
        /** 加密返回body */
        // String userKey = getUserKeyByTelNum(userId);
        // if (null == userKey)
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
        String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userId);
        return ResponsePackUtil.buildPack("0000", resBody);
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
     * 语音验证码入MQ
     * 
     * @param telNum
     * @Description:
     */
    private boolean voiceIntoMq(String telNum, String verifyCode) {
        logger.debug("语音验证码入MQ，telNum:{},verifyCode:{}", telNum, verifyCode);
        try {
            if (null == telNum || "".equals(telNum) || null == verifyCode || "".equals(verifyCode))
                return false;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("telNum", telNum);
            jsonObject.put("verifyCode", verifyCode);
            LogRocketMqUtil.send(RocketMqUtil.VoicePushMessageTopic, jsonObject.toJSONString());
            return true;
        } catch (Exception e) {
            logger.error("语音验证码入MQ异常，telNum:{},verifyCode:{}", telNum, verifyCode, e);
            return false;
        }

    }

    /**
     * 获取提示信息
     * 
     * @param tip
     * @return
     * @Description:
     */
    private String getTipInfo(String tip) {
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("tipInfo", tip);
        return JSONObject.toJSONString(bodyJson);
    }

    /**
     * 获取注册url
     * 
     * @param telNum
     * @return
     * @Description:
     */
    // private String getRegistUrl(String telNum) {
    // JSONObject bodyJson = new JSONObject();
    // String registUrl = "/internet.do?act=toRegiste&telNum=" + telNum + "&type=0";
    // bodyJson.put("registUrl", registUrl);
    // return JSONObject.toJSONString(bodyJson);
    // }

    /**
     * 修改vwt_client_user表用户密码
     * 
     * @param clientUserVO
     * @param password
     * @return
     * @Description:
     */
    private boolean editClientUserPwd(List<ClientUserVO> clientUserVOs, String password) {
        for (ClientUserVO clientUserVO : clientUserVOs) {
            clientUserVO.setPwd(password);
            if (null == clientUserInterface.saveUser(clientUserVO))
                return false;
        }
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

                CorpVO corp = corpInterface.findById(memberInfoVO.getCorpId());

                /** 激活成功后保存信息到mq，提供给经分数据 */
                UserLogVO userLogVo = new UserLogVO();
                userLogVo.setAreaCode(corp == null ? "" : corp.getCorpArea());
                userLogVo.setCityCode(corp == null ? "" : corp.getCorpRegion());
                userLogVo.setCorpId(memberInfoVO.getCorpId());
                userLogVo.setDealFlag(1);
                userLogVo.setDealTime(sdf.format(new Date()));
                userLogVo.setIMEI(imei);
                userLogVo.setMobile(memberInfoVO.getTelNum());
                userLogVo.setSeq(System.nanoTime());
                userLogVo.setUserId(memberInfoVO.getMemId());
                userLogVo.setUuid(UUID.randomUUID().toString());
                UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.userRecordQueue, JSON.toJSONString(userLogVo));

                /** 推送用户欢迎语 */
                pushToNewUser(userVO, corp.getCorpName(), memberInfoVO.getPartName());
            }
        } catch (Exception e) {
            logger.error("激活用户后保存至vwt_client_user表异常,memberInfoVOs:{},password:{}", JSON.toJSONString(memberInfoVOs), password, e);
            return false;
        }
        return true;
    }

    /**
     * 保存短信验证码并发送
     * 
     * @param telNum
     * @param verifyCode
     * @return
     * @Description:
     */
    private boolean saveVerifyCode(String telNum, String verifyCode, String content) {
        return sendSmsInterface.sendVerifyCodeSms(telNum, "1", content, verifyCode, 600000L);
    }

    /**
     * 保存短信验证码
     * 
     * @param telNum
     * @param verifyCode
     * @return
     * @Description:
     */
    private boolean saveVerifyCodeOnly(String telNum, String verifyCode) {
        return verifyCodeInterface.saveVerifyCode(telNum, "1", "", verifyCode, 600000L);
    }

    /**
     * 组装短信内容
     * 
     * @param content
     * @param verifyCode
     * @param sign
     * @return
     * @Description:
     */
    private String getSmsContent(String content, String verifyCode, String period, String sign) {
        return content + verifyCode + period + sign;
    }

    /**
     * 返回四位随机数字
     * 
     * @return
     * @Description:
     */
    private String getRandomString() {
        Random random = new Random();
        String verifyCode = "";
        for (int i = 0; i < 6; i++) {
            String rand = String.valueOf(random.nextInt(10));
            verifyCode += rand;
        }
        return verifyCode;
    }

    /**
     * 检查用户是否存在
     * 
     * @param telNum
     * @return
     * @Description:
     */
    private List<MemberInfoVO> checkUserExist(String telNum) {
        return memberInfoUtil.findMemberInfosByTelNum(telNum);
    }

    /**
     * 根据手机号码检查用户是否激活
     * 
     * @param telNum
     * @return
     * @Description:
     */
    private List<ClientUserVO> checkUserActive(String telNum) {
        return clientUserInterface.findByTelNum(telNum);
    }

    /**
     * 校验获取列表请求参数
     * 
     * @param pageIndex
     * @param pageSize
     * @param userName
     * @return
     * @Description:
     */
    private boolean valicateParams(String... strs) {
        for (String string : strs) {
            if (null == string || "".equals(string))
                return false;
        }
        return true;
    }

    /**
     * 保存激活信息
     * 
     * @param memberId
     * @param corpId
     * @Description:
     */
    private boolean saveMemberActive(List<MemberInfoVO> memberInfoVOs) {
        logger.debug("保存激活信息,memberInfoVOs:{}", JSON.toJSONString(memberInfoVOs));
        try {
            for (MemberInfoVO memberInfoVO : memberInfoVOs) {
                if (!memberActiveInterface.saveMemberActive(memberInfoVO.getMemId(), memberInfoVO.getCorpId()))
                    return false;
            }
        } catch (Exception e) {
            logger.error("保存激活信息异常,memberInfoVOs:{}", JSON.toJSONString(memberInfoVOs), e);
            return false;
        }
        return true;
    }

    /**
     * 新建企业
     * 
     * @param telNum
     * @param corpName
     * @param memberName
     * @param regionName
     * @return
     * @Description:
     */
    public DepartMentVO doCreateCorp(String telNum, String corpName, String memberName, String regionName) {
        logger.debug("新建企业,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName);

        try {
            DictionaryVo dictionaryVo = dictionaryInterface.findDictionaryByIdKeyDesc("行业", "电脑互联网");
            DictionaryVo dictionaryVoRegion = dictionaryInterface.findDictionaryByIdKeyDesc("地市", regionName);
            // 调oracle服务维护oracle
            CorpVO corpVo = new CorpVO();
            corpVo.setCorpName(corpName);
            corpVo.setCorpIndustry(null == dictionaryVo ? "" : dictionaryVo.getDictKey());
            corpVo.setCorpPersonname(memberName);
            corpVo.setCorpMobilephone(telNum);
            corpVo.setFromchannel(7L);
            corpVo.setPkState(1L);
            corpVo.setCorpStarttime(new Date());
            corpVo.setCorpArea(null == dictionaryVoRegion ? "" : dictionaryVoRegion.getDictKey());
            corpVo.setCorpRegion(null == dictionaryVoRegion ? "" : dictionaryVoRegion.getDictKey());
            logger.debug("新建企业(企业信息)，corpVo{}", JSON.toJSON(corpVo));
            CorpVO corpVoRes = corpInterface.saveCorp(corpVo);
            logger.debug("新建企业(新建后返回企业信息),corpVoRes:{}", JSON.toJSON(corpVoRes));
            if (corpVoRes != null) {
                DepartMentVO enterpriseVOCorp = new DepartMentVO();
                enterpriseVOCorp.setParentDeptNum("1");
                enterpriseVOCorp.setCorpId(corpVoRes.getCorpId());
                enterpriseVOCorp.setCorpStatus("1");
                enterpriseVOCorp.setFromChannel("7");
                enterpriseVOCorp.setPartFullName(corpName);
                enterpriseVOCorp.setPartName(corpName);
                enterpriseVOCorp.setSort(1L);
                enterpriseVOCorp.setActTime(new Date());

                logger.debug("新建企业(企业部门信息),enterpriseVOCorp{}", JSON.toJSON(enterpriseVOCorp));

                DepartMentVO enterpriseVOCorpRes = departMentInterface.save(enterpriseVOCorp);

                if (enterpriseVOCorpRes == null) {
                    logger.error("新建企业异常,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName);
                    return null;
                }

                DepartMentVO enterpriseVO = new DepartMentVO();
                enterpriseVO.setParentDeptNum(enterpriseVOCorpRes.getDeptId());
                enterpriseVO.setCorpId(corpVoRes.getCorpId());
                enterpriseVO.setCorpStatus("1");
                enterpriseVO.setFromChannel("7");
                enterpriseVO.setPartFullName(corpName + "/" + memberName + "的体验部门");
                enterpriseVO.setPartName(memberName + "的体验部门");
                enterpriseVO.setSort(2L);
                enterpriseVO.setActTime(new Date());

                logger.debug("新建企业(部门信息),enterpriseVO{}", JSON.toJSON(enterpriseVO));

                DepartMentVO enterprise = departMentInterface.save(enterpriseVO);

                if (enterprise == null) {
                    logger.error("新建企业异常,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName);
                    return null;
                }
                CliqueInfoVO cliqueInfoVO = new CliqueInfoVO();
                cliqueInfoVO.setId(databaseInterface.generateId("vwt_clique", "id"));
                cliqueInfoVO.setCliqueId(1L);
                cliqueInfoVO.setCliqueName("集团1");
                cliqueInfoVO.setCorpId(corpVoRes.getCorpId());
                cliqueInfoInterface.saveCliqueInfo(cliqueInfoVO);

                // 企业开户成功后，信息保存到mq，提供给经分数据
                CorpLogVO corpLogVo = new CorpLogVO();
                corpLogVo.setBossid("");
                corpLogVo.setChangeCustomer(0);
                corpLogVo.setCorpArea(corpVoRes.getCorpArea() == null ? "" : corpVoRes.getCorpArea());
                corpLogVo.setCorpRegion(corpVoRes.getCorpRegion() == null ? "" : corpVoRes.getCorpRegion());
                corpLogVo.setCorpId(corpVoRes.getCorpId());
                corpLogVo.setCorpName(corpVoRes.getCorpName());
                CustomerVo customerVo = customerInterface.findCustomerById(corpVoRes.getCustomerId());
                corpLogVo.setCustManagerMobile(customerVo == null ? "" : customerVo.getTelNum());
                corpLogVo.setCustManagerName(customerVo == null ? "" : customerVo.getName());
                corpLogVo.setCustomerId(corpVoRes.getCustomerId());
                corpLogVo.setDealFlag(0);
                corpLogVo.setDealTime(sdf.format(new Date()));
                corpLogVo.setFROMCHANNEL(7);
                corpLogVo.setOther("");
                corpLogVo.setSeq(System.nanoTime());
                corpLogVo.setUuid(UUID.randomUUID().toString());
                UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.corpRecordQueue, JSON.toJSONString(corpLogVo));
                return enterprise;
            } else {
                logger.error("新建企业异常,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName);
                return null;
            }
        } catch (Exception e) {
            logger.error("新建企业异常,telNum:{},corpName:{},memberName:{},regionName:{}", telNum, corpName, memberName, regionName, e);
            return null;
        }
    }

    /**
     * 创建人员(互联网注册)
     * 
     * @param telNum
     * @param memberName
     * @param password
     * @param duty
     * @param enterpriseVO
     * @return
     * @Description:
     */
    private MemberInfoVO doCreateMemberInfo(String telNum, String memberName, String duty, DepartMentVO enterpriseVO, String regionCode, String areaCode) {
        logger.debug("创建人员(互联网注册),telNum:{},memberName:{},enterpriseVO:{}", telNum, memberName, JSON.toJSONString(enterpriseVO));
        try {
            Long sortMax = hlwMemberInfoInterface.findMaxSortByCorpId(enterpriseVO.getCorpId());
            MemberInfoVO memberInfoVO = new MemberInfoVO();
            memberInfoVO.setUserCreateTime(new Date());
            memberInfoVO.setCorpId(enterpriseVO.getCorpId());
            memberInfoVO.setCreatTime(new Date());
            memberInfoVO.setDeptId(enterpriseVO.getDeptId());
            memberInfoVO.setFromChannel(7L);
            memberInfoVO.setMemberName(memberName);
            memberInfoVO.setMemStatus("1");
            memberInfoVO.setOperationTime(new Date());
            memberInfoVO.setPartName(enterpriseVO.getPartName());
            memberInfoVO.setTelNum(telNum);
            memberInfoVO.setSpell(PinyinTool.getFullSpell(memberName));
            memberInfoVO.setFirstSpell(PinyinTool.getFirstSpell(memberName));
            memberInfoVO.setClique("1");
            memberInfoVO.setVisitAuth(0L);
            memberInfoVO.setRoleAuth(0L);
            memberInfoVO.setSort(null == sortMax ? 1L : sortMax + 1);
            if (null != duty && !"".equals(duty))
                memberInfoVO.setDuty(duty);
            MemberInfoVO memberInfoVORes = hlwMemberInfoInterface.save(memberInfoVO);
            if (null == memberInfoVORes)
                return null;

            /** 导入成功保存信息，提供给经分数据 */
            UserLogVO userLogVo = new UserLogVO();
            userLogVo.setAreaCode(areaCode);
            userLogVo.setCityCode(regionCode);
            userLogVo.setCorpId(enterpriseVO.getCorpId());
            userLogVo.setDealFlag(0);
            userLogVo.setDealTime(sdf.format(new Date()));
            userLogVo.setIMEI("");
            userLogVo.setMobile(telNum);
            userLogVo.setSeq(System.nanoTime());
            userLogVo.setUserId(memberInfoVORes.getMemId());
            userLogVo.setUuid(UUID.randomUUID().toString());
            UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.userRecordQueue, JSON.toJSONString(userLogVo));

            return memberInfoVORes;
        } catch (Exception e) {
            logger.error("创建人员(互联网注册)异常,telNum:{},memberName:{},enterpriseVO:{}", telNum, memberName, JSON.toJSONString(enterpriseVO), e);
            return null;
        }
    }

    /**
     * 激活人员(互联网注册)
     * 
     * @param telNum
     * @param memberName
     * @param password
     * @param enterpriseVO
     * @param memberInfoVORes
     * @return
     * @Description:
     */
    private boolean doCreateClientUser(String telNum, String memberName, String password, DepartMentVO enterpriseVO, MemberInfoVO memberInfoVORes, String privateKey, String regionCode,
            String areaCode, String corpName) {
        logger.debug("激活人员(互联网注册),telNum:{},memberName:{},password:{},memberInfoVORes:{},enterpriseVO:{},privateKey:{}", telNum, memberName, password, JSON.toJSONString(memberInfoVORes),
                JSON.toJSONString(enterpriseVO), privateKey);
        try {
            ClientUserVO clientUserVO = new ClientUserVO();
            clientUserVO.setCorpId(enterpriseVO.getCorpId());
            clientUserVO.setCreateTime(new Date());
            clientUserVO.setDeptId(enterpriseVO.getDeptId());
            clientUserVO.setFromChannel(7L);
            clientUserVO.setPwd(password);
            clientUserVO.setTelNum(telNum);
            clientUserVO.setUserId(memberInfoVORes.getMemId());
            clientUserVO.setUserName(memberName);
            clientUserVO.setUserState("Y");
            clientUserVO.setClique("1");
            if (null != privateKey && !"".equals(privateKey))
                clientUserVO.setPrivateKey(privateKey);
            ClientUserVO clientUserVORes = clientUserInterface.saveUser(clientUserVO);
            if (null == clientUserVORes)
                return false;

            memberInfoVORes.setMemStatus("1");
            memberInfoUtil.saveMemberInfo(memberInfoVORes, memberInfoVORes.getFromChannel());

            pushToNewUser(clientUserVORes, corpName, enterpriseVO.getPartName());

            /** 激活成功后保存信息到mq，提供给经分数据 */
            UserLogVO userLogVo = new UserLogVO();
            userLogVo.setAreaCode(areaCode);
            userLogVo.setCityCode(regionCode);
            userLogVo.setCorpId(enterpriseVO.getCorpId());
            userLogVo.setDealFlag(1);
            userLogVo.setDealTime(sdf.format(new Date()));
            userLogVo.setIMEI("");
            userLogVo.setMobile(telNum);
            userLogVo.setSeq(System.nanoTime());
            userLogVo.setUserId(clientUserVORes.getUserId());
            userLogVo.setUuid(UUID.randomUUID().toString());
            UserAndCorpRocketMqUtil.send(UserAndCorpRocketMqUtil.userRecordQueue, JSON.toJSONString(userLogVo));
        } catch (Exception e) {
            logger.error("激活人员(互联网注册),telNum:{},memberName:{},password:{},memberInfoVORes:{},enterpriseVO:{}", telNum, memberName, password, JSON.toJSONString(memberInfoVORes),
                    JSON.toJSONString(enterpriseVO), e);
            return false;
        }
        return true;
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
     * 用户激活时，进行欢迎提醒
     * 
     * @param clientUserVO
     * @param departMentVO
     * @param content
     */
    private void pushToNewUser(ClientUserVO clientUserVO, String corpName, String departName) {
        /** 推送服务号消息 */
        msgPushUtil.pushVwtHelpToNewUser(clientUserVO.getUserId());
        /** 推送文字提示 */
        // XXX（企业名称），欢迎XX（用户）加入
        String content = "欢迎加入" + corpName;
        msgPushUtil.pushHlwNewUser(clientUserVO.getUserId(), content, clientUserVO.getCorpId(), clientUserVO.getDeptId(), departName);
    }
}
