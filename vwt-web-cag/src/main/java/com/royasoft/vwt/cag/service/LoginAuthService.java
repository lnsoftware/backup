/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.PushUtil;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.ImRedisInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.VerifyCodeInterface;
import com.royasoft.vwt.soa.business.login.api.interfaces.AlreadyLoginInterface;
import com.royasoft.vwt.soa.business.login.api.vo.AlreadyLoginVo;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareInterface;
import com.royasoft.vwt.soa.sundry.clientversion.api.interfaces.ClientVersionInterface;
import com.royasoft.vwt.soa.systemsettings.msglist.api.MesListInterface;
import com.royasoft.vwt.soa.systemsettings.msglist.api.MesListVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.corpcustom.api.interfaces.CorpCustomInterface;
import com.royasoft.vwt.soa.uic.corpcustom.api.vo.CorpCustomVO;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

/**
 * 登陆鉴权类
 *
 * @Author:MB
 * @Since:2015年11月19日
 */
@Scope("prototype")
@Service
public class LoginAuthService implements Runnable {

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    private final Logger logger = LoggerFactory.getLogger(LoginAuthService.class);

    @Autowired
    private ZkUtil zkUtil;
    @Autowired
    private DatabaseInterface databaseInterface;
    @Autowired
    private MemberInfoUtil memberInfoUtil;
    @Autowired
    private ClientUserInterface clientUserInterface;
    @Autowired
    private RedisInterface redisInterface;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private CorpInterface corpInterface;
    @Autowired
    private ImRedisInterface imRedisInterface;
    @Autowired
    private ClientVersionInterface clientVersionInterface;

    @Autowired
    private VerifyCodeInterface verifyCodeInterface;
    @Autowired
    private DictionaryInterface dictionaryInterface;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CorpCustomInterface corpCustomInterface;

    @Autowired
    private MesListInterface mesListInterface;

    @Autowired
    private SquareInterface squareInterface;

    @Autowired
    private ActionRecordService actionRecordService;
    
    @Autowired
    private AlreadyLoginInterface alreadyLoginInterface;

    @Autowired
    private PushUtil pushUtil;

    private static final String redisImServerNameSpace = "REDIS_PHONE_IP";

    private static final String INTERNET_USERINFO_TEL = "INTERNET:USERINFO:TEL:";

    private static final String COMMON_USERINFO_TEL = "COMMON:USERINFO:TEL:";

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.loginAuth_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("登陆鉴权处理类(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);

                    /***************************** 业务逻辑处理 *********************************************/

                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果
                    switch (function_id) {
                        case FunctionIdConstant.USERLOGIN:
                            resInfo = loginAuth(request_body);
                            break;
                        case FunctionIdConstant.USERLOGIN_VERIFYCODE:
                            resInfo = loginAuthVerifyCode(request_body);
                            break;
                        case FunctionIdConstant.USERLOGIN_SESSIONID:
                            resInfo = loginAuthSessionId(request_body);
                            break;
                        case FunctionIdConstant.LOGINPARAMETER:
                            resInfo = getLoginParameter(request_body, user_id);
                            break;
                        case FunctionIdConstant.RECONNECTIMSERVER:
                            resInfo = reconnectImServer(request_body);
                            break;
                        case FunctionIdConstant.RECONNECTIMSERVERNEW:
                            resInfo = reconnectImServerNew(request_body, user_id);
                            break;
                        case FunctionIdConstant.NEWSLISTSORT:
                            resInfo = newsListSort(request_body, user_id);
                            break;
                        case FunctionIdConstant.SHEQUPUSH: // 社区推送
                            resInfo = shequpush(request_body, user_id);
                            break;
                        case FunctionIdConstant.SECURITYKEY: // 获取安全key
                            resInfo = getSecurityKey(user_id);
                            break;
                        default:
                            break;
                    }
                    logger.debug("登陆鉴权处理类(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponse(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);

                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("登陆鉴权业务逻辑处理异常", e);
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
     * 普通密码登陆鉴权
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String loginAuth(String requestBody) {
        logger.debug("普通密码登陆鉴权,requestBody:{}", requestBody);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String username = requestJson.getString("username");// 用户名
        String password = requestJson.getString("password");// 密码
        String clientVersion = requestJson.getString("clientVersion");// 客户端版本
        String clientModel = requestJson.getString("clientModel");// 客户端类型
        String imei = requestJson.getString("imei");// 手机唯一imei
        String clientType = requestJson.getString("clientType");// 客户端类型（android/ios）
        String aesKey = requestJson.getString("aesKey");// AES加密密钥
        logger.debug("普通密码登陆鉴权(解析body),username:{},password:{},clientVersion:{},clientModel:{},imei:{},clientType:{},aesKey:{}", username, password, clientVersion, clientModel, imei, clientType,
                aesKey);

        /** 校验参数合法性 */
        if (!StringUtils.checkParamNull(username, password, clientVersion, clientModel, imei, clientType, aesKey))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");

        /** 判断该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(username);// memberInfoInterface.findByTelNum(username);
        logger.debug("普通密码,判断该用户是否存在,username:{},memberInfoVOs:{}", username, JSONObject.toJSONString(memberInfoVOs));
        if (null == memberInfoVOs || memberInfoVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1098, ResponsePackUtil.encryptData(getTipInfo("您现在还不是移动社区开通用户，请联系管理员！"), aesKey));

        /** 判断该用户是否激活 */
        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(username);
        logger.debug("普通密码,判断该用户是否激活,username:{},clientUserVOs:{}", username, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1099, ResponsePackUtil.encryptData(getTipInfo("尊敬的用户，你是首次登录，请使用短信认证方式登陆！"), aesKey));

        /** 校验用户名密码 */
        if (!authPassword(clientUserVOs, password))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1028, ResponsePackUtil.encryptData(getTipInfo("密码错误，请重新输入"), aesKey));
        logger.debug("普通密码,校验用户名密码成功,username:{},password:{},clientUserVO:{}", username, password, clientUserVOs.size());

        /** 检测并修改安卓版本信息 */
        if (!updateAndroidVersion(clientType, clientVersion, username))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1027, "");
        logger.debug("检测并修改安卓版本信息成功,username:{},password:{},clientUserVO:{}", username, password, clientUserVOs.size());

        /** 保存imei和clientType信息 */
        if (!saveImeiAndClientType(clientUserVOs, imei, clientType, username))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1029, "");
        logger.debug("保存imei和clientType信息成功,username:{},imei:{},clientType:{}", username, imei, clientType);

        /** 记录登陆客户端设备信息 */
        saveClientInfo(clientType, clientVersion, clientModel, username);
        
        String res = getResponseInfo(memberInfoVOs, clientUserVOs, username);
        if (null == res || "".equals(res))
            ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(res, aesKey));
    }

    /**
     * 验证码登陆鉴权
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String loginAuthVerifyCode(String requestBody) {
        logger.debug("验证码登陆鉴权,requestBody:{}", requestBody);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String username = requestJson.getString("username");// 用户名
        String verifyCode = requestJson.getString("verifyCode");// 密码
        String clientVersion = requestJson.getString("clientVersion");// 客户端版本
        String clientModel = requestJson.getString("clientModel");// 客户端类型
        String imei = requestJson.getString("imei");// 手机唯一imei
        String clientType = requestJson.getString("clientType");// 客户端类型（android/ios）
        String aesKey = requestJson.getString("aesKey");// AES加密密钥
        logger.debug("验证码登陆鉴权(解析body),username:{},verifyCode:{},clientVersion:{},clientModel:{},imei:{},clientType:{},aesKey:{}", username, verifyCode, clientVersion, clientModel, imei, clientType,
                aesKey);

        /** 校验参数合法性 */
        if (!StringUtils.checkParamNull(username, verifyCode, clientVersion, clientModel, imei, clientType, aesKey))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");

        /** 校验短信验证码 */
        String codeReal = commonService.valicateVerifyCode(username);
        if (null == codeReal || "".equals(codeReal))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1020, getTipInfo("该验证码已失效"));

        if (!codeReal.equals(verifyCode))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1016, getTipInfo("验证码错误"));

        /** 移除redis验证码信息 */
        commonService.removeVerifyInfo(username);

        String valicateCode = UUID.randomUUID().toString();

        /** 判断该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(username);
        logger.debug("判断该用户是否存在,username:{},memberInfoVOs:{}", username, JSONObject.toJSONString(memberInfoVOs));
        if (null == memberInfoVOs || memberInfoVOs.isEmpty()) {
            redisInterface.setString(INTERNET_USERINFO_TEL + username, valicateCode, null == ParamConfig.internet_user_timeout ? 600 : Integer.valueOf(ParamConfig.internet_user_timeout));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sessionId", valicateCode);
            List<String> regionNames = commonService.getAllRegionName();
            jsonObject.put("regionInfo", regionNames);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1024, ResponsePackUtil.encryptData(jsonObject.toJSONString(), aesKey));
        }

        /** 判断该用户是否激活 */
        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(username);
        logger.debug("判断该用户是否激活,username:{},clientUserVOs:{}", username, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty()) {
            if (!commonService.doActiveUser(memberInfoVOs, imei, username))
                ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
        }

        /** 检测并修改安卓版本信息 */
        if (!updateAndroidVersion(clientType, clientVersion, username))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1027, "");
        logger.debug("检测并修改安卓版本信息成功,username:{},clientUserVO:{}", username, clientUserVOs.size());

        /** 保存imei和clientType信息 */
        if (!saveImeiAndClientType(clientUserVOs, imei, clientType, username))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1029, "");
        logger.debug("保存imei和clientType信息成功,username:{},imei:{},clientType:{}", username, imei, clientType);

        /** 记录登陆客户端设备信息 */
        saveClientInfo(clientType, clientVersion, clientModel, username);

        String res = getResponseInfo(memberInfoVOs, clientUserVOs, username);
        if (null == res || "".equals(res))
            ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(res, aesKey));
    }

    /**
     * 无密码sessionId登陆鉴权
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String loginAuthSessionId(String requestBody) {
        logger.debug("无密码sessionId登陆鉴权,requestBody:{}", requestBody);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String username = requestJson.getString("username");// 用户名
        String sessionId = requestJson.getString("sessionId");// 密码
        String clientVersion = requestJson.getString("clientVersion");// 客户端版本
        String clientModel = requestJson.getString("clientModel");// 客户端类型
        String imei = requestJson.getString("imei");// 手机唯一imei
        String clientType = requestJson.getString("clientType");// 客户端类型（android/ios）
        String aesKey = requestJson.getString("aesKey");// AES加密密钥
        logger.debug("无密码sessionId登陆鉴权(解析body),username:{},sessionId:{},clientVersion:{},clientModel:{},imei:{},clientType:{},aesKey:{}", username, sessionId, clientVersion, clientModel, imei,
                clientType, aesKey);

        /** 校验参数合法性 */
        if (!StringUtils.checkParamNull(username, sessionId, clientVersion, clientModel, imei, clientType, aesKey))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");

        String sessionIdCommon = redisInterface.getString(COMMON_USERINFO_TEL + username);
        String sessionIdInternet = redisInterface.getString(INTERNET_USERINFO_TEL + username);
        logger.debug("无密码sessionId登陆鉴权(校验sessionId),username:{},sessionId:{},sessionIdCommon:{},sessionIdInternet:{}", username, sessionId, sessionIdCommon, sessionIdInternet);
        if ((null == sessionIdCommon || "".equals(sessionIdCommon) || !sessionIdCommon.equals(sessionId))
                && (null == sessionIdInternet || "".equals(sessionIdInternet) || !sessionIdInternet.equals(sessionId)))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2708, "");

        /** 判断该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(username);
        logger.debug("判断该用户是否存在,username:{},memberInfoVOs:{}", username, JSONObject.toJSONString(memberInfoVOs));
        if (null == memberInfoVOs || memberInfoVOs.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1024, "");
        }

        /** 判断该用户是否激活 */
        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(username);
        logger.debug("判断该用户是否激活,username:{},clientUserVOs:{}", username, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1025, "");

        /** 检测并修改安卓版本信息 */
        if (!updateAndroidVersion(clientType, clientVersion, username))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1027, "");
        logger.debug("检测并修改安卓版本信息成功,username:{},clientUserVO:{}", username, clientUserVOs.size());

        /** 保存imei和clientType信息 */
        if (!saveImeiAndClientType(clientUserVOs, imei, clientType, username))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1029, "");
        logger.debug("保存imei和clientType信息成功,username:{},imei:{},clientType:{}", username, imei, clientType);

        /** 记录登陆客户端设备信息 */
        saveClientInfo(clientType, clientVersion, clientModel, username);
        String res = getResponseInfo(memberInfoVOs, clientUserVOs, username);
        if (null == res || "".equals(res))
            ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");

        /** 移除redis验证码信息 */
        commonService.removeVerifyInfo(username);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(res, aesKey));
    }

    /**
     * 重连新消息服务器
     * 
     * @param requestBody
     * @return
     * @Description:
     */
    public String reconnectImServer(String requestBody) {
        logger.debug("重连新消息服务器,requestBody:{}", requestBody);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String username = requestJson.getString("account");// 用户名
        String password = requestJson.getString("password");// 密码
        logger.debug("重连新消息服务器(解析body),username:{},password:{}", username, password);

        /** 校验参数合法性 */
        if (!StringUtils.checkParamNull(username, password))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");

        /** 判断该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(username);// memberInfoInterface.findByTelNum(username);
        logger.debug("重连新消息服务器,判断该用户是否存在,username:{},memberInfoVOs:{}", username, JSONObject.toJSONString(memberInfoVOs));
        if (null == memberInfoVOs || memberInfoVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1024, "");

        /** 判断该用户是否激活 */
        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(username);
        logger.debug("重连新消息服务器,判断该用户是否激活,username:{},clientUserVOs:{}", username, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1025, "");
        /** 校验用户名密码 */
        boolean checkRes = clientUserInterface.checkPassword(username, password, false);
        logger.debug("重连新消息服务器,校验用户名密码,username:{},password:{},clientUserVOs:{},checkRes:{}", username, password, JSONObject.toJSONString(clientUserVOs), checkRes);
        if (!checkRes)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1028, "");

        JSONObject jsonObject = getImServerInfo(username);
        if (null == jsonObject)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1045, "");
        logger.debug("重连新消息服务器,返回Imserver信息,username:{},jsonObject:{}", username, jsonObject.toJSONString());
        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", jsonObject.toJSONString());
    }

    /**
     * 重连新消息服务器2.1
     * 
     * @param requestBody
     * @return
     * @Description:
     */
    public String reconnectImServerNew(String requestBody, String user_id) {
        logger.debug("重连新消息服务器2.1,requestBody:{},user_id:{}", requestBody, user_id);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");// 用户名
        String userId = requestJson.getString("userId");// 用户id
        String token = requestJson.getString("token");// token
        logger.debug("重连新消息服务器2.1(解析body),telNum:{},userId:{},token:{}", telNum, userId, token);

        /** 校验参数合法性 */
        if (!StringUtils.checkParamNull(telNum, userId, token))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1023, "");

        /** 判断该用户是否存在 */
        List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(telNum);// memberInfoInterface.findByTelNum(username);
        logger.debug("重连新消息服务器2.1,判断该用户是否存在,telNum:{},memberInfoVOs:{}", telNum, JSONObject.toJSONString(memberInfoVOs));
        if (null == memberInfoVOs || memberInfoVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1024, "");

        /** 判断该用户是否激活 */
        List<ClientUserVO> clientUserVOs = clientUserInterface.findByTelNum(telNum);
        logger.debug("重连新消息服务器2.1,判断该用户是否激活,telNum:{},clientUserVOs:{}", telNum, JSONObject.toJSONString(clientUserVOs));
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1025, "");

        if (!commonService.valicateToken(token, telNum, userId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1028, userId);

        JSONObject jsonObject = getImServerInfo(telNum);
        if (null == jsonObject)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1045, "");
        logger.debug("重连新消息服务器2.1,返回Imserver信息,telNum:{},jsonObject:{}", telNum, jsonObject.toJSONString());
        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", jsonObject.toJSONString());
    }

    /**
     * 登录后获取参数
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String getLoginParameter(String requestBody, String userId) {
        logger.debug("登录获取参数,requestBody:{},userId:{}", requestBody, userId);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1033, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String username = requestJson.getString("username");// 用户名
        String corpId = requestJson.getString("corpId");// 企业id
        logger.debug("登录获取参数(解析body),username:{},corpId:{}", username, corpId);

        /** 校验参数合法性 */
        if (!StringUtils.checkParamNull(username, corpId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1033, "");

        /** 获取企业log、小集团id参数 */
        String res = getResponseParameter(username, corpId);
        /** 压缩加密返回body */
        String resBody = ResponsePackUtil.encryptData(res, userId);

        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 校验密码
     * 
     * @param clientUserVOs
     * @param pwd
     * @return
     * @Description:
     */
    private boolean authPassword(List<ClientUserVO> clientUserVOs, String pwd) {
        for (ClientUserVO clientUserVO : clientUserVOs) {
            if (null == clientUserVO || null == clientUserVO.getPwd() || "".equals(clientUserVO.getPwd()))
                continue;
            if (clientUserVO.getPwd().equals(pwd))
                return true;
        }
        return false;
    }

    /**
     * 更新对应手机号码的AESKEY
     * 
     * 注释于2016.4.14 马斌 由于登陆不再更新aes密钥
     * 
     * @param telNum
     * @param aesKey
     */
    /**
     * 
     * private boolean updateAesKey(List<ClientUserVO> clientUserVOs, String aesKey) { logger.debug("更新对应手机号码的AESKEY,clientUserVOs:{},aesKey:{}",
     * clientUserVOs, aesKey); for (ClientUserVO clientUserVO : clientUserVOs) { if (null != clientUserVO.getPrivateKey() &&
     * !"".equals(clientUserVO.getPrivateKey()) && clientUserVO.getPrivateKey().equals(aesKey)) continue; clientUserVO.setPrivateKey(aesKey);
     * ClientUserVO clientUserVORes = null; try { clientUserVORes = clientUserInterface.saveUser(clientUserVO); } catch (Exception e) {
     * logger.error("更新对应手机号码的AESKEY异常,clientUserVO:{},aesKey:{}", clientUserVO, aesKey, e); return false; }
     * 
     * logger.debug("更新对应手机号码的AESKEY(更新后),clientUserVORes:{},aesKey:{}", clientUserVORes, aesKey); if (null == clientUserVORes ||
     * !clientUserVORes.getPrivateKey().equals(aesKey)) return false; } return true; }
     */

    /**
     * 检测安卓客户端版本号并入库
     * 
     * @param clientType
     * @param clientVersion
     * @param username
     * @Description:
     */
    private boolean updateAndroidVersion(String clientType, String clientVersion, String username) {
        logger.debug("检测安卓客户端版本号并入库,clientType:{},clientVersion:{},username:{}", clientType, clientVersion, username);
        if (clientType.equals("android")) {
            String versions = clientVersion.substring(clientVersion.lastIndexOf(".") - 1, clientVersion.lastIndexOf(".") + 2);
            int telNumAndVersionCount = clientVersionInterface.selectTelNumAndVersionCount(username);
            logger.debug("检测安卓客户端版本号并入库,clientType:{},clientVersion:{},username:{},versions:{},telNumAndVersionCount:{}", clientType, clientVersion, username, versions, telNumAndVersionCount);
            if (Float.parseFloat(versions) < 1.5 && telNumAndVersionCount <= 0) {
                boolean insRes = clientVersionInterface.insertTelNumAndVersion(username, clientVersion);
                logger.debug("检测安卓客户端版本号并入库(新增),clientType:{},clientVersion:{},username:{},versions:{},telNumAndVersionCount:{},insRes:{}", clientType, clientVersion, username, versions,
                        telNumAndVersionCount, insRes);
                if (!insRes)
                    return false;
            }
            if (telNumAndVersionCount > 0 && Float.parseFloat(versions) >= 1.5) {
                boolean delRes = clientVersionInterface.deleteTelNumAndVersion(username);
                logger.debug("检测安卓客户端版本号并入库(删除),clientType:{},clientVersion:{},username:{},versions:{},telNumAndVersionCount:{},delRes:{}", clientType, clientVersion, username, versions,
                        telNumAndVersionCount, delRes);
                if (!delRes)
                    return false;
            }
        }
        return true;
    }

    /**
     * 保存imei和clientType信息
     * 
     * @param clientUserVORes
     * @param imei
     * @param clientType
     * @return
     * @Description:
     */
    private boolean saveImeiAndClientType(List<ClientUserVO> clientUserVORes, String imei, String clientType, String username) {
        logger.debug("保存imei和clientType信息,clientUserVORes:{},imei:{},clientType:{}", JSONObject.toJSONString(clientUserVORes), imei, clientType);
        // boolean clearStatus = clearSameImsi(imei);
        // logger.debug("保存imei和clientType信息(清空相同imsi),clientUserVORes:{},imei:{},clientType:{},clearStatus:{}",
        // JSONObject.toJSONString(clientUserVORes), imei, clientType, clearStatus);
        // if (!clearStatus)
        // return false;
        try {
            for (ClientUserVO clientUserVO : clientUserVORes) {
                clientUserVO.setImsi(imei);
                clientUserVO.setClientType(clientType);
                clientUserInterface.saveUser(clientUserVO);
            }
            // if (!databaseInterface.saveClientTypeAndImsi(username, imei, clientType))
            // return false;
        } catch (Exception e) {
            logger.error("保存imei和clientType信息异常,clientUserVORes:{},imei:{},clientType:{}", JSONObject.toJSONString(clientUserVORes), imei, clientType, e);
        }
        return true;
    }

    /**
     * 清空相同imsi的账号
     * 
     * @param imei
     * @return
     * @Description:
     */
    /*
     * private boolean clearSameImsi(String imei) { logger.debug("清空相同imsi的账号,imei:{}", imei); List<ClientUserVO> clientUserVOs =
     * clientUserInterface.findByImsi(imei); logger.debug("清空相同imsi的账号,imei:{},clientUserVOs:{}", imei, JSONObject.toJSONString(clientUserVOs)); if
     * (null == clientUserVOs || clientUserVOs.isEmpty()) return true; try { for (ClientUserVO clientUserVO : clientUserVOs) {
     * clientUserVO.setImsi(""); clientUserInterface.saveUser(clientUserVO); } // if (!databaseInterface.clearSameImsi(imei)) // return false; } catch
     * (Exception e) { logger.error("清空相同imsi的账号异常,imei:{}", imei, e); return false; } return true; }
     */

    /**
     * 发送强制下线消息
     * 
     * @param clientUserVO
     * @param username
     * @param imei
     * @return
     * @Description:
     */
    // private boolean sendForceOffLineMsg(List<ClientUserVO> clientUserVOs, String username, String imei) {
    // logger.debug("发送强制下线消息,username:{},imei:{},clientUserVOs:{}", username, imei, JSON.toJSONString(clientUserVOs));
    // for (ClientUserVO clientUserVO : clientUserVOs) {
    // if (null == clientUserVO.getImsi() || "".equals(clientUserVO.getImsi()) || clientUserVO.getImsi().equals(imei))
    // continue;
    // try {
    // JSONObject jsonObject = new JSONObject();
    // jsonObject.put("result", "200");
    // jsonObject.put("position", "9");
    // jsonObject.put("imei", imei);
    // msgPushUtil.sendSingleMsg("IM", "1", jsonObject.toJSONString(), username, MQProvideUtil.FirstLvQueue, "9", "", "", "");
    // logger.debug("发送强制下线消息(消息信息),username:{},imei:{},databaseImei:{},sendInfo:{}", username, imei, clientUserVO.getImsi(),
    // JSONObject.toJSONString(jsonObject));
    // return true;
    // } catch (Exception e) {
    // logger.error("发送强制下线消息异常,username:{},imei:{},databaseImei:{}", username, imei, clientUserVO.getImsi(), e);
    // return false;
    // }
    // }
    // return true;
    // }

    /**
     * 组装返回信息
     * 
     * @param memberInfoVOs
     * @return
     * @Description:
     */
    private String getResponseInfo(List<MemberInfoVO> memberInfoVOs, List<ClientUserVO> clientUserVOs, String username) {
        logger.debug("组装返回信息,memberInfoVOs:{},clientUserVO:{}", JSONObject.toJSONString(memberInfoVOs), JSONObject.toJSONString(clientUserVOs));
        JSONObject jsonObject = getUserInfo(memberInfoVOs, clientUserVOs, username);
        logger.debug("组装返回信息(基础信息),memberInfoVOs:{},clientUserVOs:{},jsonObject:{}", JSONObject.toJSONString(memberInfoVOs), JSONObject.toJSONString(clientUserVOs),
                JSONObject.toJSONString(jsonObject));
        return JSONObject.toJSONString(jsonObject);
    }

    /**
     * 获取企业log、小集团id参数
     * 
     * @param username
     * @param corpId
     * @param userKey
     * @return
     * @Description:
     */
    private String getResponseParameter(String username, String corpId) {
        logger.debug("请求获取企业log、小集团id参数 ,username:{},corpId:{}", JSONObject.toJSONString(username), JSONObject.toJSONString(corpId));
        JSONObject jsonObject = new JSONObject();
        CorpVO corpVO = corpInterface.findById(corpId);
        logger.debug("获取企业logoMap ,corpVO:{}", JSONObject.toJSONString(corpVO));
        String logoPath = "";
        String logoTime = "";
        if (null != corpVO) {
            if (null != corpVO.getCorpLogo())
                logoPath = corpVO.getCorpLogo();
            logger.debug("获取企业logoPath ,logoPath:{}", JSONObject.toJSONString(logoPath));
            if (null != corpVO.getCorpLogoTime())
                logoTime = String.valueOf(corpVO.getCorpLogoTime().getTime());
            logger.debug("获取企业logoTime ,logoTime:{}", JSONObject.toJSONString(logoTime));
        }
        /** 根据username和corpId获取人员信息 **/
        MemberInfoVO memberInfoVO = memberInfoUtil.findMemberInfoById(username);// memberInfoInterface.findById(username);
        jsonObject.put("logoPath", logoPath);
        jsonObject.put("logoTime", logoTime);
        if (memberInfoVO == null || "".equals(memberInfoVO)) {
            logger.error("获取小集团参数异常username{}", username);
            jsonObject.put("CLIQUE_ID", "");

        } else {
            jsonObject.put("CLIQUE_ID", memberInfoVO.getClique());
        }

        logger.debug("企业log、小集团id参数(jsonObject),jsonObject:{}", jsonObject.toJSONString());
        return JSONObject.toJSONString(jsonObject);
    }

    /**
     * 获取基础信息
     * 
     * @param memberInfoVOs
     * @return
     * @Description:
     */
    private JSONObject getUserInfo(List<MemberInfoVO> memberInfoVOs, List<ClientUserVO> clientUserVOs, String username) {
        JSONObject jsonObject = new JSONObject();
        logger.debug("获取基础信息,memberInfoVOs:{}", JSONObject.toJSONString(memberInfoVOs));

        boolean PwdIsNull = true;
        for (ClientUserVO clientUserVO : clientUserVOs) {
            if (null != clientUserVO.getPwd() && !"".equals(clientUserVO.getPwd())) {
                PwdIsNull = false;
                break;
            }
        }
        jsonObject.put("PwdIsNull", PwdIsNull);
        jsonObject.put("telNum", username);
        jsonObject.put("companyList", getMemberInfos(memberInfoVOs));
        jsonObject.put("appStoreUrl", ParamConfig.appstore_url);
        jsonObject.put("numberLimit", ParamConfig.number_limit);
        jsonObject.put("VFCurl", ParamConfig.vfc_url);
        jsonObject.put("SSOURL", ParamConfig.sso_url);
        jsonObject.put("shareUrl", ParamConfig.share_url);
        jsonObject.put("newFileUrl", ParamConfig.file_server_url);
        logger.debug("获取基础信息(jsonObject),jsonObject:{}", jsonObject.toJSONString());
        return jsonObject;
    }

    /**
     * 获取一人多职信息
     * 
     * @param memberInfoVOs
     * @return
     * @Description:
     */
    private JSONArray getMemberInfos(List<MemberInfoVO> memberInfoVOs) {
        logger.debug("获取一人多职信息,memberInfoVOs:{}", memberInfoVOs.size());
        JSONArray jsonArray = new JSONArray();
        try {
            for (MemberInfoVO memberInfoVO : memberInfoVOs) {
                String corpId = memberInfoVO.getCorpId();
                logger.debug("corpid:{}", corpId);
                CorpVO corpVO = corpInterface.findById(corpId);
                JSONObject jsonObject = new JSONObject();
                ClientUserVO clientUserVO = clientUserInterface.findById(memberInfoVO.getMemId());
                CorpCustomVO customVO = corpCustomInterface.findCorpCustomById(corpId);
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
     * 获取im信息并组装返回值
     * 
     * @param telNum
     * @param jsonObject
     * @return
     * @Description:
     */
    private JSONObject getImServerInfo(String telNum) {
        logger.debug("获取im信息并组装返回值,telNum:{}", telNum);
        try {
            JSONObject jsonObject = new JSONObject();
            Map<String, String> imMap = getImServer(telNum);
            String imServerIp = "";
            String imServerPort = "";
            String token = "";
            if (null != imMap) {
                imServerIp = imMap.get("imServerIp");
                imServerPort = imMap.get("imServerPort");
                token = createToken();
            }
            jsonObject.put("imServerIp", imServerIp);
            jsonObject.put("imServerPort", imServerPort);
            jsonObject.put("token", token);
            saveSession(telNum, token);
            logger.debug("获取im信息并组装返回值(添加后),telNum:{},jsonObject:{}", telNum, jsonObject);
            return jsonObject;
        } catch (Exception e) {
            logger.error("获取im信息并组装返回值异常,telNum:{}", telNum, e);
            return null;
        }
    }

    /**
     * 保存token信息
     * 
     * @param tel
     * @param token
     */
    public void saveSession(String tel, String token) {
        imRedisInterface.setString("im_client_" + tel, token, 60);
    }

    /**
     * 获取新im服务器信息
     * 
     * @return
     */
    private Map<String, String> getImServer(String telNum) {
        logger.debug("获取新im服务器信息,telNum:{}", telNum);
        String lastImInfo = getLastImServer(telNum);
        logger.debug("获取新im服务器信息(用户上次登录IM信息lastImInfo),telNum:{},lastImInfo:{}", telNum, lastImInfo);
        try {
            /** 获取提供服务的所有新IM服务器 */
            // List<String> imservers = zkClient.getChildren("/royasoft/vwt/imserver");
            List<String> imservers = zkUtil.findChildren("/royasoft/imserver/runningServer");
            List<String> imserversTmp = new ArrayList<String>();
            imserversTmp.addAll(imservers);
            /** 2月3日去掉筛除156IM服务器 */
            // for (String imInfo : imservers) {
            // Map<String, String> imInfoMap = getIpAndPortByZK(imInfo);
            // logger.debug("筛除156服务器,ip:{}", imInfoMap.get("imInnerIP"));
            // if (imInfoMap.get("imInnerIP").endsWith(".156"))
            // imserversTmp.remove(imInfo);
            // }
            logger.debug("获取新im服务器信息(获取提供服务的所有新IM服务器imservers),telNum:{},lastImInfo:{},imservers:{}", telNum, lastImInfo, JSONObject.toJSONString(imserversTmp));
            return getNowImInfo(imserversTmp, lastImInfo);
        } catch (Exception e) {
            logger.error("获取新im服务器信息异常,telNum:{}", telNum, e);
            return null;
        }
    }

    /**
     * 检查是否使用之前的imserver
     * 
     * @param imservers
     * @param lastIP
     * @return
     */
    private Map<String, String> getNowImInfo(List<String> imservers, String lastImInfo) {
        logger.debug("检查是否使用之前的imserver,imservers:{},lastImInfo:{}", JSONObject.toJSONString(imservers), lastImInfo);
        if (null == imservers || imservers.isEmpty())
            return null;
        if (null == lastImInfo || "".equals(lastImInfo)) {
            return getRandomImServer(imservers);
        } else {
            for (String imInfo : imservers) {
                Map<String, String> imInfoMap = getIpAndPortByZK(imInfo);
                if (imInfoMap.get("imServerIpAndPort").equals(lastImInfo))
                    return imInfoMap;
            }
            return getRandomImServer(imservers);
        }
    }

    /**
     * 随机获取imServer
     * 
     * @param imservers
     * @return
     * @Description:
     */
    private Map<String, String> getRandomImServer(List<String> imservers) {
        logger.debug("随机获取imServer,imservers:{}", JSONObject.toJSONString(imservers));
        Random random = new Random();
        int randomIndex = random.nextInt(imservers.size());
        logger.debug("随机获取imServer(randomIndex),imservers:{},randomIndex:{}", JSONObject.toJSONString(imservers), randomIndex);
        Map<String, String> imServerInfo = getIpAndPortByZK(imservers.get(randomIndex));
        logger.debug("随机获取imServer(imServerInfo),imservers:{},randomIndex:{},imServerInfo:{}", JSONObject.toJSONString(imservers), randomIndex, JSONObject.toJSONString(imServerInfo));
        return imServerInfo;
    }

    /**
     * 根据im节点获取ip和port信息
     * 
     * @param imServerId
     * @return
     * @Description:
     */
    private Map<String, String> getIpAndPortByZK(String imServerId) {
        logger.debug("根据im节点获取ip和port信息,imServerId:{}", imServerId);
        try {
            if (null == imServerId || "".equals(imServerId))
                return null;

            String imServerInfo = zkUtil.findData("/royasoft/imserver/runningServer/" + imServerId);
            logger.debug("根据im节点获取ip和port信息(imServerInfo),imServerId:{},imServerInfo:{}", imServerId, imServerInfo);
            String[] inAndOutInfo = imServerInfo.split(",");
            if (inAndOutInfo.length < 2)
                return null;

            String outInfo = inAndOutInfo[0];
            if (null == outInfo || "".equals(outInfo))
                return null;
            String[] outInfos = outInfo.split(":");

            if (outInfos.length != 2)
                return null;

            Map<String, String> ImInfo = new HashMap<String, String>();
            ImInfo.put("imServerIpAndPort", outInfo);
            ImInfo.put("imServerIp", outInfos[0]);
            ImInfo.put("imServerPort", outInfos[1]);
            String[] innerInfo = inAndOutInfo[1].split(":");
            ImInfo.put("imInnerIP", innerInfo[0]);
            logger.debug("根据im节点获取ip和port信息(ImInfo),imServerId:{},imServerInfo:{},ImInfo:{}", imServerId, imServerInfo, JSONObject.toJSONString(ImInfo));
            return ImInfo;
        } catch (Exception e) {
            logger.error("根据im节点获取ip和port信息异常,imServerId:{}", imServerId, e);
            return null;
        }

    }

    /**
     * 消息列表置顶排序
     * 
     * @param requestBody
     * @param user_id
     * @return
     * @author Jiangft 2016年8月25日
     */
    public String newsListSort(String requestBody, String user_id) {
        logger.debug("消息列表置顶排序（入口）,requestBody:{},user_id:{}", requestBody, user_id);

        try {
            if (null == requestBody || "".equals(requestBody)) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            }
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            // 平台
            String platform = requestJson.getString("platform");
            logger.debug("消息列表置顶排序platform:{}", platform);
            // 默认V网通
            platform = org.springframework.util.StringUtils.isEmpty(platform) ? "VWT" : platform;
            List<MesListVo> list = mesListInterface.findByPlatform(platform);

            logger.debug("信息排列 list.size:{} ", list == null ? null : list.size());
            if (list == null) {
                logger.error("消息列表置顶排序异常");
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4004, "");
            }
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("mesListSort", list);
            logger.debug("信息排列  model :{}", model);
            return ResponsePackUtil.buildPack("0000", ResponsePackUtil.encryptData(JSONObject.toJSONString(model), user_id));

        } catch (Exception e) {
            logger.error("消息列表置顶排序异常e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL4004, "");
        }
    }

    /**
     * 获取该用户上次登录服务器
     * 
     * @return
     */
    private String getLastImServer(String telNum) {
        logger.debug("获取该用户上次登录服务器,telNum:{}", telNum);
        try {
            String lastInfo = imRedisInterface.hashGet(redisImServerNameSpace, telNum);
            logger.debug("获取该用户上次登录服务器,telNum:{},lastInfo:{}", telNum, lastInfo);
            return lastInfo;
        } catch (Exception e) {
            logger.debug("获取该用户上次登录服务器异常,telNum:{}", telNum, e);
            return null;
        }
    }

    /**
     * 保存token信息
     * 
     * @param tel
     * @param token
     */
    public void saveToken(String tel, String token) {
        logger.debug("保存token信息,tel:{},token:{}", tel, token);
        redisInterface.setString("im_client_" + tel, token, 60);
    }

    /**
     * 随机创建token
     * 
     * @return
     * @Description:
     */
    private String createToken() {
        return UUID.randomUUID().toString();
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
     * 获取用户对应的密钥
     * 
     * 注释于2016.4.14 马斌 由于登陆不再更新aes密钥
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
     * 获取社区助手推送
     * 
     * @param requestBody 请求内容
     * @return
     * @Description:
     */
    public String shequpush(String requestBody, String userId) {
        logger.debug("社区推送获取参数,requestBody:{},userId:{}", requestBody, userId);
        if (null == requestBody || "".equals(requestBody))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1033, "");
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String telNum = requestJson.getString("telNum");// 登录号码
        String memId = requestJson.getString("userId");// 登录号码
        String type = requestJson.getString("type");// 登录状态：1，重装或新装 2，切换账号
        logger.debug("登录获取参数(解析body),telNum:{},type:{}", telNum, type);

        /** 校验参数合法性 */
        if (!StringUtils.checkParamNull(telNum, type))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1033, "");

        /** 检测号码有没有登录历史 */
        boolean flag = pushCareInfo(telNum,memId);

        /** 判断type值确认社区助手推送内容 */
        if (type.equals("1")) {

            /**
             * 向社区助手推送内容 1、欢迎使用移动社区 2、欢迎回到移动社区
             * */
            if (flag) {
                // 第一次登陆
                pushVwtFirstLogin(memId);
            } else {
                pushWelcomeBack(memId, "1");
            }
        }
        /** 记录登陆号码和时间 */
        try{
            AlreadyLoginVo alreadyLoginVo=new AlreadyLoginVo();
            alreadyLoginVo.setMemberTel(telNum);
            alreadyLoginVo.setFirstLoginTime(new Date());
            alreadyLoginInterface.saveAlreadyLogin(alreadyLoginVo);
        }catch(Exception e){
        }
        
        /** 加密返回body */
        return ResponsePackUtil.buildPack("0000", "");
    }
    
    
    private String getSecurityKey( String userId) {
        logger.debug("获取SecurityKey,userId{}",userId);
        JSONObject obj = new JSONObject();
        obj.put("key", ResponsePackUtil.encryptData(ParamConfig.SecurityKey, userId));
        return ResponsePackUtil.buildPack("0000", obj.toJSONString());
    }

    /**
     * 判断此人是否登陆过
     * 
     * @param telNum
     * @return
     */
    public boolean pushCareInfo(String telNum,String memId) {

        boolean flag = true;
        logger.debug("推送关怀语(入口),telNum:{}", telNum);

        // 查询该号码的所有信息
        AlreadyLoginVo alreadyLoginVo= alreadyLoginInterface.findAlreadyLoginByTel(telNum);
        if(null!=alreadyLoginVo){
            flag=false;
        }
        return flag;
    }

    /**
     * 首次使用V网通进行推送
     * 
     * @param clientUserVO
     * @return
     * @Description:
     */
    private String pushVwtFirstLogin(String memId) {

        logger.debug("推送移动社区首次登陆消息(memId)", memId+ParamConfig.v_group_service_no);
        // 推送欢迎语
        try {
            pushUtil.pushTextNewIm(ParamConfig.v_group_service_no, memId, ParamConfig.first_login_content, "1");
        } catch (Exception e) {
            logger.error("推送移动社区首次登陆消息", e);
        }
        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 推送欢迎回来关怀语
     * 
     * @Description:
     */
    private void pushWelcomeBack(String memId, String platForm) {
        logger.debug("推送欢迎回来关怀语(入口),memId:{}", memId+ParamConfig.v_group_service_no);

        // 推送欢迎回来关怀语
            try {
                
                pushUtil.pushTextNewIm(ParamConfig.v_group_service_no, memId, ParamConfig.come_back_content, "1");
            } catch (Exception e) {
                logger.error("推送欢迎回来关怀语异常", e);
            }
    }

}
