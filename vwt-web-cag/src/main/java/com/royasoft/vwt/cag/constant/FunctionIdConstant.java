/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 请求方法id
 *
 * @Author:ZHOUKQ
 * @Since:2016年3月1日
 */
public class FunctionIdConstant {

	public static final Logger logger = LoggerFactory.getLogger(FunctionIdConstant.class);

	/** RSA解密方式 */
	public static final String rsaEncode = "RSA";
	/** AES解密方式 */
	public static final String aesEncode = "AES";
	/** 明文方式 */
	public static final String clearEncode = "CLEAR";

	/** 工作圈业务请求功能号集合 */
	public static List<String> workTeamUrlFunctionIdList = new ArrayList<String>();
	/** 任务业务请求功能号集合 */
	public static List<String> taskUrlFunctionIdList = new ArrayList<String>();
	/** 设置业务请求功能号集合 */
	public static List<String> settingUrlFunctionIdList = new ArrayList<String>();
	/** 公告业务请求功能号集合 */
	public static List<String> announceUrlFunctionIdList = new ArrayList<String>();
	/** 通讯录业务请求功能号集合 */
	public static List<String> addressUrlFunctionIdList = new ArrayList<String>();
	/** 登陆业务请求功能号集合 */
	public static List<String> loginAuthUrlFunctionIdList = new ArrayList<String>();
	/** 版本更新业务请求功能号集合 */
	public static List<String> versionUrlFunctionIdList = new ArrayList<String>();
	/** 签到业务请求功能号集合 */
	public static List<String> signInUrlFunctionIdList = new ArrayList<String>();
	/** 多角色工作台业务请求功能号集合 */
	public static List<String> workBenchUrlFunctionIdList = new ArrayList<String>();
	/** 邀请体系请求功能号集合 */
	public static List<String> inviteSystemFunctionIdList = new ArrayList<String>();
	/** 互联网企业认证业务请求功能号集合 */
	public static List<String> hlwAuthUrlFunctionIdList = new ArrayList<String>();

	/** 企业邮箱设置请求功能号集合 */
	public static List<String> mailConfigUrlFunctionIdList = new ArrayList<String>();
	/** 企业或部门logo请求功能号集合 */
	public static List<String> corpCustomUrlFunctionIdList = new ArrayList<String>();

	/** 投票请求功能号集合 */
	public static List<String> voteFunctionIdList = new ArrayList<String>();

	/** 2.1版本设置业务请求功能号集合 */
	public static List<String> settingNewUrlFunctionIdList = new ArrayList<String>();

	/** o了IMS通讯录请求功能号集合 */
	public static List<String> oleIMSFunctionIdList = new ArrayList<String>();

	/** OA账号请求功能号集合 **/
	public static List<String> OAaccountInfoList = new ArrayList<String>();

	/** 二学一做请求功能号集合 **/
	public static List<String> twoLearnFunctioinIdList = new ArrayList<String>();

	/** 二学一做请求功能号集合 **/
	public static List<String> beautyJSFunctionIdList = new ArrayList<String>();

	/** 敏感词请求集合 **/
	public static List<String> sensitivewordList = new ArrayList<String>();

	/** 同步免打扰状态请求功能号集合 */
	public static List<String> noDisturbList = new ArrayList<>();

	/** 服务号反馈集合 */
	public static List<String> squeareFeedbackList = new ArrayList<>();

	/** 收藏请求请求集合 **/
	public static List<String> collectionList = new ArrayList<String>();

	/** pc版本更新 **/
	public static List<String> pcversionlist = new ArrayList<String>();

	/** 山东ＯＡ接口集合 **/
	public static List<String> shandongOAList = new ArrayList<String>();

	/** 二维码url接口集合 **/
	public static List<String> urlmanageFunctionIdList = new ArrayList<String>();

	public static List<String> integralH5FunctionIdList = new ArrayList<String>();

	public static List<String> insidePurchFunctionIdList = new ArrayList<String>();

	public static final String VERSIONUPDATE = "1101";

	/** 新的版本更新 */
	public static final String VERSIONUPDATENEW = "1102";

	/** pc版本更新获取 */
	public static final String VERSIONPCUPDATE = "1103";

	/** 获取灰度发布开关 **/
	public static final String GRAYRELEASE = "1201";

	/** 签到 */
	public static final String SIGNIN = "1301";

	/** 登录查询签到日期参数 */
	public static final String UQERYSIGNDATE = "1302";

	/** 积分功能——查询当天是否签到参数 */
	public static final String UQERYISSIGN = "1303";

	/** 获取用户当月总积分 */
	public static final String MONTHINTEGRAL = "1304";

	/** 获取获取积分规则 */
	public static final String RULEINTEGRAL = "1305";

	/** 获取前三月收入出积分 */
	public static final String INTAKEINTEGRAL = "1306";

	/** 获取指定月份详细收入积分(分页方式) */
	public static final String INTAKEMOREINTEGRAL = "1307";

	/** 获取用户当月总积分（加密） */
	public static final String MONTHINTEGRALAES = "1308";

	/** 加载V特权 */
	public static final String LOADVPRIVILEGE = "1309";

	/** 记录V特权点击次数 */
	public static final String VPRIVILEGECLICK = "1310";

	/** 查询该用户是否已领取火龙果奖券 */
	public static final String CHECKDRAGONFRUIT = "1311";

	/** 获取所有未领取的奖券 */
	public static final String DRAGONFRUITNOTGET = "1312";

	/** 领取火龙果奖券 */
	public static final String GETDRAGONFRUIT = "1313";

	/** 分享下载页面获取下载验证码 */
	public static final String DOWNLOADCODE = "1314";

	/** 分享下载页面记录下载记录 */
	public static final String DOWNLOADRECORD = "1315";

	/** 分享页面查询分享列表 */
	public static final String INVITELIST = "1316";

	/** 分享页面查询分享详情 */
	public static final String INVITEDETAIL = "1317";

	/** 抢红包 */
	public static final String REDPACKET_LOTTERY = "1318";

	/** 用户跳转到红包首页初始化信息 */
	public static final String REDPACKET_SHOWINFO = "1319";

	/** 邀请体系，未登陆的情况下获取帮助首页 */
	public static final String INVITATIONSYSTEM_HOTFAQLIST = "1320";
	/** 邀请体系，根据功能号加载常见问题 */
	public static final String INVITATIONSYSTEM_FAQLIST = "1321";
	/** 邀请体系，根据功能号加载问题场景 */
	public static final String INVITATIONSYSTEM_PROBLEMSCENE = "1322";
	/** 邀请体系，问题反馈 */
	public static final String INVITATIONSYSTEM_FEEDBACK = "1323";
	/** 邀请体系，登录后 加载首页信息(获取模块，功能) */
	public static final String INVITATIONSYSTEM_MODULES = "1324";

	/** 邀请体系，查询自己问题的反馈 */
	public static final String INVITATIONSYSTEM_MYPEROBLEM = "1325";

	public static final String pay_validataPoint = "1330";
	public static final String pay_getIntegralcommodityList = "1331";
	public static final String pay_getIntegralcommodityDateil = "1332";
	public static final String pay_getRedeemCodeCommodity = "1333";
	public static final String pay_getExchange = "1334";
	public static final String pay_getDateFormatOrderList = "1335";
	public static final String pay_getOrderDetailList = "1336";
	public static final String pay_getRedeemCodeOrder = "1337";
	public static final String pay_getAddPointDateList = "1338";
	public static final String pay_getPayPointDateList = "1339";
	public static final String pay_getPayPointDetailList = "1340";
	public static final String getMonthList = "1341";

	/*********************** 工作圈模块请求功能号 ********************************************************/
	/** 获取工作圈消息列表 */
	public static final String getWorkTeamMsgList = "1401";

	/** 获取工作圈个人说说消息列表 */
	public static final String getSomeOneWorkTeamMsgList = "1402";
	/** 发表工作圈说说 */
	public static final String appendWorkTeamMessage = "1403";
	/** 删除工作圈说说 */
	public static final String deleteWorkTeamMessage = "1404";
	/** 删除我的工作圈说说 */
	public static final String deleteMyWorkTeamMessage = "1405";
	/** 评论工作圈消息 */
	public static final String appendWorkTeamReply = "1406";

	/** 赞工作圈消息 */
	public static final String appendWorkTeamPraise = "1407";

	/** 取消赞工作圈消息 */
	public static final String cancelWorkTeamPraise = "1408";
	/** 取消工作圈回复 */
	public static final String cancelWorkTeamReply = "1411";

	/** 获取工作圈消息信息 */
	public static final String getWorkTeamMessageDetail = "1409";

	public static final String getListCirleInfo = "1410";

	/** 获取工作圈分页特别关注列表 */
	public static final String getSpecialUserList = "1421";

	/** 批量新增特别关注 */
	public static final String addBanchSpecialUsers = "1422";

	/** 删除特别关注 */
	public static final String delSpecialUser = "1423";

	/** 获取工作圈分页企业列表 */
	public static final String getCorpList = "1424";

	/** 获取工作圈企业下部门列表 */
	public static final String getDeptList = "1425";

	/*************************** 任务模块请求功能号 ****************************************************/
	/** 创建任务 */
	public static final String createSendTask = "1501";

	/** 获取任务列表 */
	public static final String getSendTaskList = "1502";

	/** 编辑任务 */
	public static final String editSendTask = "1503";

	/** 结束任务 */
	public static final String endSendTask = "1504";

	/** 删除任务 */
	public static final String cancelSendTask = "1505";

	/** 发送文本回执 */
	public static final String sendTaskReceipt = "1506";

	/** 发送阅读回执 */
	public static final String sendReadTaskReceipt = "1507";

	/** 查询回执列表 */
	public static final String getSendTaskReceiptList = "1508";

	/** 修改头像 */
	public static final String changeAvatar = "1601";

	/** 获取自定义字段 */
	public static final String reserveField = "1701";

	/** 美丽江苏点赞保存 */
	public static final String BEAUTYJSPRAISE = "1801";

	/** 评论保存 */
	public static final String BEAUTYJSDISCUSS = "1802";

	/** 展示评论和点赞 */
	public static final String GETDISCUSSANDPRAISE = "1803";

	/** 取消点赞 */
	public static final String CANCELBEAUTYJSPRAISE = "1804";

	/** 美丽江苏列表展示 */
	public static final String GETBEAUTYJSLIST = "1805";

	/** 美丽江苏——进入详情界面(增加一次观看次数+获取点赞状态) */
	public static final String BEAUTYJSENTERDETAIL = "1806";

	/** 二学一做-保存观看视频时间 */
	public static final String twoLearn_saveVideoTime = "1810";
	/** 二学一做-视频-评论保存 */
	public static final String twoLearn_saveComment = "1811";
	/** 二学一做-视频-评论列表 */
	public static final String twoLearn_commentList = "1812";
	/** 二学一做-视频-详情 */
	public static final String twoLearn_videoDetail = "1813";
	/** 二学一做-视频列表 */
	public static final String twoLearn_videoList = "1814";
	/** 二学一做-获取转码状态 */
	public static final String twoLearn_transcodingStatus = "1815";
	/** 二学一做-修改转码状态 */
	public static final String twoLearn_updateTranscodingStatus = "1816";

	/** 获取应用列表 */
	public static final String WB_GETALLAPP = "1901";
	/** 收藏关注 */
	public static final String WB_DOATTENTION = "1902";
	/** 预置应用取消收藏和关注 */
	public static final String WB_DOCANCELATTENTION = "1903";
	/** 登录获取应用信息接口 */
	public static final String WB_GETSELFAPP = "1904";
	/** 获取详情接口 */
	public static final String WB_GETAPPDETAIL = "1905";
	/** 点击菜单推送消息 */
	public static final String WB_MENUPUSHMSG = "1906";
	/** 推送系统级推送信息(封装推送内容) */
	public static final String WB_GRAPHICPSUH = "1907";
	/** 获取标签接口 */
	public static final String WB_GETLABEL = "1908";
	/** 获取服务号菜单接口 */
	public static final String WB_GETMENU = "1909";
	/** 应用下载与卸载接口 */
	public static final String WB_DOWNLOAD = "1910";
	/** 获取URL生成二维码扫描接口 */
	public static final String WB_TWODIMENSIONALCODE = "1911";
	/** 登录获取应用定制信息接口 */
	public static final String GETPERSONALIZEAPP = "1912";

	/** 获取公告列表 */
	public static final String ANNOUNCELIST = "2001";

	/** 获取公告详情 */
	public static final String ANNOUNCEDETAIL = "2002";

	/** 获取公告阅读次数，手机客户端使用 */
	public static final String ANNOUNCERECORD = "2003";

	/** 获取公告详情H5接口 */
	public static final String ANNOUNCEDETAILFORH5 = "2004";

	/** 获取公告阅读次数，H5客户端使用 */
	public static final String ANNOUNCERECORDH5 = "2005";

	/** 素材中心-根据图文预览id获取预览内容 */
	public static final String GETGRAPHICSOURCEPREVIEW = "2006";

	/** 素材中心-根据素材id单个素材 */
	public static final String GETGRAPHICSOURCECONTENT = "2007";
	/** 素材中心-根据id获取素材图文阅读数 */
	public static final String GETGRAPHICSOURCECOUNTBYID = "2008";

	/** 公告弹屏 */
	public static final String ANNOUNCEWINDOWSLIST = "2009";

	/** 获取推荐邮箱域名列表接口 */
	public static final String MAILRECOMMEND = "2101";

	/** 获取邮箱配置信息接口 */
	public static final String MAILCONFIG = "2102";

	/** 获取企业和部门logo图片 */
	public static final String CORPCUSTOM = "2201";

	/** 获取企业和部门logo标志位 */
	public static final String CORPCUSTOMFLAG = "2202";

	/** 完善企业信息 */
	public static final String HLW_AUTH_PREFECTCORP = "2501";
	/** 上传认证公函 */
	public static final String HLW_AUTH_UPLOADOFFICIAL = "2502";
	/** 获取企业认证信息 */
	public static final String HLW_AUTH_GETPREFECTINFO = "2503";
	/** 提交企业认证信息 */
	public static final String HLW_AUTH_SUBMITPREFECT = "2504";

	/** 用户互联网体验-通讯录管理-部门添加 */
	public static final String ADDRESS_DEPARTMENT_ADD = "2601";

	/** 用户互联网体验-通讯录管理-部门修改 */
	public static final String ADDRESS_DEPARTMENT_UPDATE = "2602";

	/** 用户互联网体验-通讯录管理-部门删除 */
	public static final String ADDRESS_DEPARTMENT_DELETE = "2603";

	/** 互联网管理员增加人员 */
	public static final String HLW_MEMBER_ADD = "2604";

	/** 互联网管理员修改人员 */
	public static final String HLW_MEMBER_UPDATE = "2605";

	/** 互联网管理员删除人员 */
	public static final String HLW_MEMBER_DELETE = "2606";

	/** 分页获取投票主题和选项 */
	public static final String GETVOTESUBJECT = "2901";

	/** 提交投票信息 */
	public static final String SUBMITVOTE = "2902";

	/** 获取投票选项 */
	public static final String GETVOTEOPTIONS = "2903";

	/** 获取当前主题投票结果 */
	public static final String GETVOTERESULT = "2904";

	/** 获取当前主题投票结果 */
	public static final String GETVOTEOPTIONSDETAILL = "2905";

	/** 获取主题总票数 */
	public static final String GETVOTESUBJECTCOUNT = "2906";

	/** 获取其他选项输入信息 */
	public static final String GETOTHERVOTEOPTIONSDETAILL = "2907";

	/** 获取IMS通讯录集团列表 */
	public static final String GETAFFILIATION = "3001";

	/** 获取IMS通讯录部门列表 */
	public static final String GETDEPARTMENTS = "3002";

	/** 获取IMS通讯录人员列表 */
	public static final String GETPERSONINFOLEADERSEARCHLIST = "3003";

	/** 获取IMS通讯录人员信息 */
	public static final String GETPERSONINFOLEADERSEARCH = "3004";

	/** 获取OA账号信息 **/
	public static final String GETOAACCOUNTINFO = "4001";

	/** 获取敏感词 **/
	public static final String GETSENSITIVEWORD = "4002";

	/** 群聊关注 **/
	public static final String IM_ATTEND = "4101";

	/** 取消关注 **/
	public static final String IM_CANCEL_ATTEND = "4102";

	public static final String IM_ATTEND_BATCH = "4104";

	/** 获取关注列表 **/
	public static final String IM_GET_ATTENTION = "4103";

	/** 获取人员、部门更新数量 */
	public static final String ADDRESSCOUNT = "5001";

	/** 分页获取部门更新 */
	public static final String ORGPAGE = "5002";

	/** 分页获取人员更新 */
	public static final String MEMBERPAGE = "5003";

	/** 全量获取部门以及人员更新 */
	public static final String ADDRESSALL = "5004";

	/** 历史问题数据处理 */
	public static final String GARBAGEDEAL = "5005";

	/** 个人常用联系人列表 */
	public static final String MY_CONTACT = "5006";

	/** 某个常用联系人分组列表 */
	public static final String MY_CONTACT_GROUP = "50061";

	/** 个人常用联系人添加 */
	public static final String MY_CONTACT_ADD = "5007";

	/** 个人常用联系人分组添加 */
	public static final String MY_CONTACT_GROUP_ADD = "50071";

	/** 个人常用联系人删除 */
	public static final String MY_CONTACT_DELETE = "5008";

	/** 个人常用联系人分组删除 */
	public static final String MY_CONTACT_GROUP_DELETE = "50081";

	/** 个人常用联系人分组修改 */
	public static final String MY_CONTACT_GROUP_MODIFY = "50091";

	/** 个人常用联系人或分组更新 */
	public static final String MY_CONTACT_GROUP_UPDATE = "5010";

	/** 激活用户校验 */
	public static final String TOREGIST = "6001";

	/** 忘记密码用户校验 */
	public static final String TOFORGET = "6002";

	/** 激活校验及设置密码 */
	public static final String DOREGIST = "6003";

	/** 忘记密码校验及设置密码 */
	public static final String DOFORGET = "6004";

	/** 下发短信验证码 */
	public static final String SENDSMS = "6005";

	/** 互联网人员注册 */
	public static final String DOINTERNETREGIST = "6006";

	/** 完善资料 */
	public static final String PREFECTINFO = "6007";

	/** 短信邀请 */
	public static final String SMSINVIT = "6008";// 废弃

	/** 体验集团添加人员 */
	public static final String INTERNETADD = "6009";

	/** 获取自定义字段 */
	public static final String GETRESERVEFIELD = "6010";

	/** 获取系统参数 */
	public static final String GETSYSTEMPARAM = "6011";

	/** “我”重置密码 */
	public static final String DORESERPASSWORD = "6012";

	/** 通讯录纠错 */
	public static final String ADDRESSCORRECTION = "6013";

	/** 客户端日志上传 */
	public static final String CLIENTLOGUPLOAD = "6014";

	/** pc端日志上传 */
	public static final String PCLOGUPLOAD = "6016";

	/** pc发表说说 */
	public static final String PC_APPEND_WORK_TEAM = "6017";

	/** 客户端日志 */
	public static final String CLIENTOPERATION = "6015";

	/** 分享链接 */
	public static final String SHAREURL = "6018";

	/** 2.1版本下发短信验证码 */
	public static final String ST_SENDSMSCODE = "6030";
	/** 2.1版本下发语音验证码 */
	public static final String ST_SENDVOICECODE = "6031";
	/** 2.1版本校验验证码 */
	public static final String ST_VALICATECODE = "6032";
	/** 2.1版本互联网个人注册设置密码 */
	public static final String ST_INTERNETPWD = "6033";
	/** 2.1版本互联网个人注册完善资料 */
	public static final String ST_INTERNETPREFECT = "6034";
	/** 2.1版本忘记密码用户校验并下发短信验证码 */
	public static final String ST_FORGETSMS = "6035";
	/** 2.1版本忘记密码校验验证码并设置密码 */
	public static final String ST_FORGETPWD = "6036";
	/** 2.1版本通过原密码设置密码 */
	public static final String ST_OLDPWD = "6037";
	/** 2.1版本通过token设置密码 */
	public static final String ST_TOKEN = "6038";

	/** IM文件下载 */
	public static final String IMFILE = "7001";// 废弃

	/** 图文阅读数 */
	public static final String GRAPHICRECORD = "8001";

	/** oa图文推送服务号列表 */
	public static final String OASQUARELIST = "8002";

	public static final String USERLOGIN = "9001";

	/** 2.1版本验证码登陆 */
	public static final String USERLOGIN_VERIFYCODE = "9005";

	/** 2.1版本SESSIONID登陆 */
	public static final String USERLOGIN_SESSIONID = "9006";

	/** 登录获取参数 */
	public static final String LOGINPARAMETER = "9002";

	/** 重连新消息服务器 */
	public static final String RECONNECTIMSERVER = "9003";

	/** 重连新消息服务器 */
	public static final String RECONNECTIMSERVERNEW = "9007";

	/** 消息列表置顶排序 */
	public static final String NEWSLISTSORT = "9009";

	/** 同步免打扰状态 */
	public static final String NODISTURB = "9101";

	/** 单聊消息查询 */
	public static final String SINGLECHAT = "9102";

	/** 群组消息查询 */
	public static final String GROUPCHAT = "9103";

	/** PC端单聊消息查询 */
	public static final String PCSINGLECHAT = "9152";

	/** PC端群组消息查询 */
	public static final String PCGROUPCHAT = "9153";

	/** 服务号历史消息 */
	public static final String FWHHISTORY = "9104";

	/** 圈子人员查询 */
	public static final String CIRCLEMEMBERLIST = "9106";

	/** 服务号免打扰信息查询 */
	public static final String NODISTURB_SERVICE = "9107";

	/** 解密参数 */
	public static final String DECRYPT = "9105";

	/** 获取新闻列表 */
	public static final String GETNEWSLIST = "9201";

	/** 获取新闻详情 */
	public static final String GETNEWSINFO = "9202";

	/** 掌厅下载计数接口 */
	public static final String ZT_DOWNLOAD = "9999";

	/** 查询节日欢迎图 */
	public static final String FINDFESTIVAL = "9910";

	/** 社区助手推送 */
	public static final String SHEQUPUSH = "10010";

	/** 获取加密key */
	public static final String SECURITYKEY = "10011";

	/** 山东收藏消息内容，工作圈内容保存 */
	public static final String SAVECOLLECTION = "10020";

	/** 山东收藏消息内容，工作圈内容删除收藏 */
	public static final String DELETECOLLECTION = "10021";

	/** 山东查询收藏 */
	public static final String QUERYCOLLECTION = "10022";

	/** 山东扫码登录，pc生成token入缓存 */
	public static final String SAVELOGINTOKEN = "10031";

	/** 山东扫码登录，客户端扫码根据token取信息 */
	public static final String FINDMSGBYTOKEN = "10032";

	/** 客户端允许登录，传入token和登录信息到缓存 */
	public static final String ALLOWLOGIN = "10033";

	/** 山东扫码登录，pc定时根据token取登录信息 */
	public static final String FINDLOGINBYTOKEN = "10034";

	/** 服务号反馈信息 */
	public static final String SQUEAREFEEDBACKSAVE = "10035";

	/*************************** 山东ＯＡ接口 ****************************************************/
	/** ＯＡ登录接口，静态密码登录 */
	public static final String OASTATICLOGIN = "12001";

	/** ＯＡ首页九宫格接口 */
	public static final String OAJIUGONG = "12002";

	/** ＯＡ待办列表接口 */
	public static final String OADOLIST = "12003";

	/** ＯＡ待办详情接口 */
	public static final String OADOFORM = "12004";

	/** ＯＡ意见填写接口 */
	public static final String OAWRITESUG = "12005";

	/** ＯＡ意见保存接口 */
	public static final String OASAVESUG = "12006";

	/** ＯＡ人员选择接口 */
	public static final String OANEXTITEM = "12007";

	/** ＯＡ待办提交接口 */
	public static final String OASAVENEXTITEM = "12008";

	/** ＯＡ已办列表接口 */
	public static final String OADONELIST = "12009";

	/** ＯＡ已办详情接口 */
	public static final String OADONEFORM = "12010";

	/** ＯＡ流程跟踪接口 */
	public static final String OATRACINGSUG = "12011";

	/** ＯＡ附件接口接口 */
	public static final String OAATTACH = "12012";

	/** 查询二维码url */
	public static final String GETURLMANAGE = "12020";

	/** 查询秘钥 */
	public static final String SEARCHSECURITYKEY = "12021";

	/** 山东内购需求-商品-列表 */
	public static final String INSIDE_PURCH_GOODS_PAGE = "12031";
	/** 山东内购需求-商品-订购 */
	public static final String INSIDE_PURCH_GOODS_ORDER = "12032";
	/** 山东内购需求-订购-列表 */
	public static final String INSIDE_PURCH_ORDERS_PAGE = "12033";
	/** 山东内购需求-订购-取消 */
	public static final String INSIDE_PURCH_ORDERS_DELETE = "12034";
	
	/** 山东内购需求二期-商品-列表 */
	public static final String INSIDE_BUY_GOODS_PAGE = "12035";
    /** 山东内购需求二期-商品-详情 */
    public static final String INSIDE_BUY_GOODS_DETAIL = "12036";
    /** 山东内购需求二期-购物车-列表 */
    public static final String INSIDE_BUY_CART_PAGE = "12037";
	/** 山东内购需求二期-购物车-添加 */
	public static final String INSIDE_BUY_CART_SAVE = "12038";
    /** 山东内购需求二期-购物车-更新 */
    public static final String INSIDE_BUY_CART_UPDATE = "12039";
    /** 山东内购需求二期-购物车-删除 */
    public static final String INSIDE_BUY_CART_DELETE = "12040";
    /** 山东内购需求二期-预约 */
    public static final String INSIDE_BUY_ORDER_SAVE = "12041";
	/** 山东内购需求二期-订购-列表 */
    public static final String INSIDE_BUY_ORDER_PAGE = "12042";
	/** 山东内购需求二期-订购-取消 */
    public static final String INSIDE_BUY_ORDER_DELETE = "12043";

	static {

		shandongOAList.add(OASTATICLOGIN);
		shandongOAList.add(OAJIUGONG);
		shandongOAList.add(OADOLIST);
		shandongOAList.add(OADOFORM);
		shandongOAList.add(OAWRITESUG);
		shandongOAList.add(OASAVESUG);
		shandongOAList.add(OANEXTITEM);
		shandongOAList.add(OASAVENEXTITEM);
		shandongOAList.add(OADONELIST);
		shandongOAList.add(OADONEFORM);
		shandongOAList.add(OATRACINGSUG);
		shandongOAList.add(OAATTACH);

		collectionList.add(SAVECOLLECTION);
		collectionList.add(DELETECOLLECTION);
		collectionList.add(QUERYCOLLECTION);
		collectionList.add(SAVELOGINTOKEN);
		collectionList.add(FINDMSGBYTOKEN);
		collectionList.add(ALLOWLOGIN);
		collectionList.add(FINDLOGINBYTOKEN);

		workTeamUrlFunctionIdList.add(getWorkTeamMsgList);
		workTeamUrlFunctionIdList.add(getSomeOneWorkTeamMsgList);
		workTeamUrlFunctionIdList.add(appendWorkTeamMessage);
		workTeamUrlFunctionIdList.add(deleteWorkTeamMessage);
		workTeamUrlFunctionIdList.add(deleteMyWorkTeamMessage);
		workTeamUrlFunctionIdList.add(appendWorkTeamReply);
		workTeamUrlFunctionIdList.add(appendWorkTeamPraise);
		workTeamUrlFunctionIdList.add(cancelWorkTeamPraise);
		workTeamUrlFunctionIdList.add(getWorkTeamMessageDetail);
		workTeamUrlFunctionIdList.add(getListCirleInfo);
		workTeamUrlFunctionIdList.add(changeAvatar);
		workTeamUrlFunctionIdList.add(GETRESERVEFIELD);
		workTeamUrlFunctionIdList.add(GETSYSTEMPARAM);
		workTeamUrlFunctionIdList.add(DORESERPASSWORD);
		workTeamUrlFunctionIdList.add(ADDRESSCORRECTION);
		workTeamUrlFunctionIdList.add(CLIENTLOGUPLOAD);
		workTeamUrlFunctionIdList.add(PCLOGUPLOAD);
		workTeamUrlFunctionIdList.add(CLIENTOPERATION);
		workTeamUrlFunctionIdList.add(FINDFESTIVAL);
		workTeamUrlFunctionIdList.add(cancelWorkTeamReply);
		workTeamUrlFunctionIdList.add(PCSINGLECHAT);
		workTeamUrlFunctionIdList.add(PCGROUPCHAT);
		workTeamUrlFunctionIdList.add(PC_APPEND_WORK_TEAM);
		workTeamUrlFunctionIdList.add(getSpecialUserList);
		workTeamUrlFunctionIdList.add(addBanchSpecialUsers);
		workTeamUrlFunctionIdList.add(delSpecialUser);
		workTeamUrlFunctionIdList.add(getCorpList);
		workTeamUrlFunctionIdList.add(getDeptList);

		taskUrlFunctionIdList.add(createSendTask);
		taskUrlFunctionIdList.add(getSendTaskList);
		taskUrlFunctionIdList.add(editSendTask);
		taskUrlFunctionIdList.add(endSendTask);
		taskUrlFunctionIdList.add(cancelSendTask);
		taskUrlFunctionIdList.add(sendTaskReceipt);
		taskUrlFunctionIdList.add(sendReadTaskReceipt);
		taskUrlFunctionIdList.add(getSendTaskReceiptList);

		settingUrlFunctionIdList.add(GRAYRELEASE);
		settingUrlFunctionIdList.add(TOFORGET);
		settingUrlFunctionIdList.add(TOREGIST);
		settingUrlFunctionIdList.add(DOFORGET);
		settingUrlFunctionIdList.add(DOREGIST);
		settingUrlFunctionIdList.add(DOINTERNETREGIST);
		settingUrlFunctionIdList.add(SMSINVIT);
		settingUrlFunctionIdList.add(INTERNETADD);
		settingUrlFunctionIdList.add(SENDSMS);
		settingUrlFunctionIdList.add(PREFECTINFO);
		settingUrlFunctionIdList.add(SHAREURL);

		announceUrlFunctionIdList.add(ANNOUNCEDETAIL);
		announceUrlFunctionIdList.add(ANNOUNCELIST);
		announceUrlFunctionIdList.add(ANNOUNCEWINDOWSLIST);
		announceUrlFunctionIdList.add(ANNOUNCERECORD);
		announceUrlFunctionIdList.add(GRAPHICRECORD);
		announceUrlFunctionIdList.add(OASQUARELIST);

		addressUrlFunctionIdList.add(ADDRESSALL);
		addressUrlFunctionIdList.add(ADDRESSCOUNT);
		addressUrlFunctionIdList.add(ORGPAGE);
		addressUrlFunctionIdList.add(MEMBERPAGE);
		addressUrlFunctionIdList.add(GARBAGEDEAL);
		addressUrlFunctionIdList.add(MY_CONTACT);
		addressUrlFunctionIdList.add(MY_CONTACT_ADD);
		addressUrlFunctionIdList.add(MY_CONTACT_DELETE);
		addressUrlFunctionIdList.add(MY_CONTACT_GROUP);
		addressUrlFunctionIdList.add(MY_CONTACT_GROUP_ADD);
		addressUrlFunctionIdList.add(MY_CONTACT_GROUP_DELETE);
		addressUrlFunctionIdList.add(MY_CONTACT_GROUP_MODIFY);
		addressUrlFunctionIdList.add(MY_CONTACT_GROUP_UPDATE);

		loginAuthUrlFunctionIdList.add(USERLOGIN);
		loginAuthUrlFunctionIdList.add(RECONNECTIMSERVER);
		loginAuthUrlFunctionIdList.add(RECONNECTIMSERVERNEW);
		loginAuthUrlFunctionIdList.add(LOGINPARAMETER);
		loginAuthUrlFunctionIdList.add(USERLOGIN_VERIFYCODE);
		loginAuthUrlFunctionIdList.add(USERLOGIN_SESSIONID);
		loginAuthUrlFunctionIdList.add(NEWSLISTSORT);
		loginAuthUrlFunctionIdList.add(SHEQUPUSH);
		loginAuthUrlFunctionIdList.add(SECURITYKEY);

		versionUrlFunctionIdList.add(VERSIONUPDATE);
		versionUrlFunctionIdList.add(VERSIONUPDATENEW);
		versionUrlFunctionIdList.add(ZT_DOWNLOAD);

		pcversionlist.add(VERSIONPCUPDATE);

		signInUrlFunctionIdList.add(SIGNIN);
		signInUrlFunctionIdList.add(UQERYSIGNDATE);
		signInUrlFunctionIdList.add(UQERYISSIGN);
		signInUrlFunctionIdList.add(RULEINTEGRAL);
		signInUrlFunctionIdList.add(MONTHINTEGRALAES);
		signInUrlFunctionIdList.add(LOADVPRIVILEGE);
		signInUrlFunctionIdList.add(VPRIVILEGECLICK);
		signInUrlFunctionIdList.add(CHECKDRAGONFRUIT);
		signInUrlFunctionIdList.add(DRAGONFRUITNOTGET);
		signInUrlFunctionIdList.add(GETDRAGONFRUIT);
		signInUrlFunctionIdList.add(DOWNLOADCODE);
		signInUrlFunctionIdList.add(DOWNLOADRECORD);
		signInUrlFunctionIdList.add(INVITELIST);

		integralH5FunctionIdList.add(MONTHINTEGRAL);
		integralH5FunctionIdList.add(pay_getAddPointDateList);
		integralH5FunctionIdList.add(pay_getDateFormatOrderList);
		integralH5FunctionIdList.add(pay_getExchange);
		integralH5FunctionIdList.add(pay_getIntegralcommodityDateil);
		integralH5FunctionIdList.add(pay_getIntegralcommodityList);
		integralH5FunctionIdList.add(pay_getOrderDetailList);
		integralH5FunctionIdList.add(pay_getPayPointDateList);
		integralH5FunctionIdList.add(pay_getPayPointDetailList);
		integralH5FunctionIdList.add(pay_getRedeemCodeCommodity);
		integralH5FunctionIdList.add(pay_getRedeemCodeOrder);
		integralH5FunctionIdList.add(pay_validataPoint);
		integralH5FunctionIdList.add(getMonthList);
		integralH5FunctionIdList.add(INTAKEINTEGRAL);
		integralH5FunctionIdList.add(INTAKEMOREINTEGRAL);

		workBenchUrlFunctionIdList.add(WB_DOATTENTION);
		workBenchUrlFunctionIdList.add(WB_DOCANCELATTENTION);
		workBenchUrlFunctionIdList.add(WB_GETALLAPP);
		workBenchUrlFunctionIdList.add(WB_GETAPPDETAIL);
		workBenchUrlFunctionIdList.add(WB_GETLABEL);
		workBenchUrlFunctionIdList.add(WB_GETSELFAPP);
		workBenchUrlFunctionIdList.add(GETPERSONALIZEAPP);
		workBenchUrlFunctionIdList.add(WB_GRAPHICPSUH);
		workBenchUrlFunctionIdList.add(WB_MENUPUSHMSG);
		workBenchUrlFunctionIdList.add(WB_GETMENU);
		workBenchUrlFunctionIdList.add(WB_DOWNLOAD);
		workBenchUrlFunctionIdList.add(WB_TWODIMENSIONALCODE);

		inviteSystemFunctionIdList.add(INVITATIONSYSTEM_MODULES);
		inviteSystemFunctionIdList.add(INVITATIONSYSTEM_FAQLIST);
		inviteSystemFunctionIdList.add(INVITATIONSYSTEM_FEEDBACK);
		inviteSystemFunctionIdList.add(INVITATIONSYSTEM_PROBLEMSCENE);
		inviteSystemFunctionIdList.add(INVITATIONSYSTEM_HOTFAQLIST);
		inviteSystemFunctionIdList.add(ANNOUNCEDETAILFORH5);
		inviteSystemFunctionIdList.add(ANNOUNCERECORDH5);
		inviteSystemFunctionIdList.add(GETGRAPHICSOURCEPREVIEW);
		inviteSystemFunctionIdList.add(GETGRAPHICSOURCECONTENT);
		inviteSystemFunctionIdList.add(GETGRAPHICSOURCECOUNTBYID);
		inviteSystemFunctionIdList.add(INVITATIONSYSTEM_MYPEROBLEM);

		hlwAuthUrlFunctionIdList.add(HLW_AUTH_GETPREFECTINFO);
		hlwAuthUrlFunctionIdList.add(HLW_AUTH_PREFECTCORP);
		hlwAuthUrlFunctionIdList.add(HLW_AUTH_SUBMITPREFECT);
		hlwAuthUrlFunctionIdList.add(HLW_AUTH_UPLOADOFFICIAL);
		hlwAuthUrlFunctionIdList.add(ADDRESS_DEPARTMENT_ADD);
		hlwAuthUrlFunctionIdList.add(ADDRESS_DEPARTMENT_UPDATE);
		hlwAuthUrlFunctionIdList.add(ADDRESS_DEPARTMENT_DELETE);
		hlwAuthUrlFunctionIdList.add(HLW_MEMBER_ADD);
		hlwAuthUrlFunctionIdList.add(HLW_MEMBER_UPDATE);
		hlwAuthUrlFunctionIdList.add(HLW_MEMBER_DELETE);

		hlwAuthUrlFunctionIdList.add(IM_ATTEND);
		hlwAuthUrlFunctionIdList.add(IM_CANCEL_ATTEND);
		hlwAuthUrlFunctionIdList.add(IM_GET_ATTENTION);
		hlwAuthUrlFunctionIdList.add(IM_ATTEND_BATCH);

		mailConfigUrlFunctionIdList.add(MAILRECOMMEND);
		mailConfigUrlFunctionIdList.add(MAILCONFIG);
		corpCustomUrlFunctionIdList.add(CORPCUSTOM);
		corpCustomUrlFunctionIdList.add(CORPCUSTOMFLAG);

		voteFunctionIdList.add(GETVOTESUBJECT);
		voteFunctionIdList.add(SUBMITVOTE);
		voteFunctionIdList.add(GETVOTEOPTIONS);
		voteFunctionIdList.add(GETVOTERESULT);
		voteFunctionIdList.add(GETVOTEOPTIONSDETAILL);
		voteFunctionIdList.add(GETVOTESUBJECTCOUNT);
		voteFunctionIdList.add(GETOTHERVOTEOPTIONSDETAILL);

		settingNewUrlFunctionIdList.add(ST_FORGETPWD);
		settingNewUrlFunctionIdList.add(ST_FORGETSMS);
		settingNewUrlFunctionIdList.add(ST_INTERNETPREFECT);
		settingNewUrlFunctionIdList.add(ST_INTERNETPWD);
		settingNewUrlFunctionIdList.add(ST_SENDSMSCODE);
		settingNewUrlFunctionIdList.add(ST_SENDVOICECODE);
		settingNewUrlFunctionIdList.add(ST_VALICATECODE);
		settingNewUrlFunctionIdList.add(ST_OLDPWD);
		settingNewUrlFunctionIdList.add(ST_TOKEN);

		oleIMSFunctionIdList.add(GETAFFILIATION);
		oleIMSFunctionIdList.add(GETDEPARTMENTS);
		oleIMSFunctionIdList.add(GETPERSONINFOLEADERSEARCHLIST);
		oleIMSFunctionIdList.add(GETPERSONINFOLEADERSEARCH);

		OAaccountInfoList.add(GETOAACCOUNTINFO);

		twoLearnFunctioinIdList.add(twoLearn_saveVideoTime);
		twoLearnFunctioinIdList.add(twoLearn_commentList);
		twoLearnFunctioinIdList.add(twoLearn_saveComment);
		twoLearnFunctioinIdList.add(twoLearn_transcodingStatus);
		twoLearnFunctioinIdList.add(twoLearn_updateTranscodingStatus);
		twoLearnFunctioinIdList.add(twoLearn_videoDetail);
		twoLearnFunctioinIdList.add(twoLearn_videoList);

		beautyJSFunctionIdList.add(BEAUTYJSPRAISE);
		beautyJSFunctionIdList.add(BEAUTYJSDISCUSS);
		beautyJSFunctionIdList.add(GETDISCUSSANDPRAISE);
		beautyJSFunctionIdList.add(CANCELBEAUTYJSPRAISE);
		beautyJSFunctionIdList.add(GETBEAUTYJSLIST);
		beautyJSFunctionIdList.add(BEAUTYJSENTERDETAIL);

		sensitivewordList.add(GETSENSITIVEWORD);

		noDisturbList.add(NODISTURB);
		noDisturbList.add(SINGLECHAT);
		noDisturbList.add(GROUPCHAT);
		noDisturbList.add(FWHHISTORY);
		noDisturbList.add(DECRYPT);
		noDisturbList.add(GETNEWSLIST);
		noDisturbList.add(GETNEWSINFO);
		noDisturbList.add(CIRCLEMEMBERLIST);
		noDisturbList.add(NODISTURB_SERVICE);

		squeareFeedbackList.add(SQUEAREFEEDBACKSAVE);

		urlmanageFunctionIdList.add(GETURLMANAGE);
		
		insidePurchFunctionIdList.add(INSIDE_PURCH_GOODS_ORDER);
		insidePurchFunctionIdList.add(INSIDE_PURCH_GOODS_PAGE);
		insidePurchFunctionIdList.add(INSIDE_PURCH_ORDERS_DELETE);
		insidePurchFunctionIdList.add(INSIDE_PURCH_ORDERS_PAGE);
		
		insidePurchFunctionIdList.add(INSIDE_BUY_GOODS_PAGE);
        insidePurchFunctionIdList.add(INSIDE_BUY_GOODS_DETAIL);
        insidePurchFunctionIdList.add(INSIDE_BUY_CART_PAGE);
		insidePurchFunctionIdList.add(INSIDE_BUY_CART_SAVE);
        insidePurchFunctionIdList.add(INSIDE_BUY_CART_UPDATE);
        insidePurchFunctionIdList.add(INSIDE_BUY_CART_DELETE);
        insidePurchFunctionIdList.add(INSIDE_BUY_ORDER_SAVE);
        insidePurchFunctionIdList.add(INSIDE_BUY_ORDER_PAGE);
        insidePurchFunctionIdList.add(INSIDE_BUY_ORDER_DELETE);

		
	}

	public static final Map<String, String> encodeTypeMap = new HashMap<String, String>();

	static {
		encodeTypeMap.put(SAVECOLLECTION, aesEncode);
		encodeTypeMap.put(DELETECOLLECTION, aesEncode);
		encodeTypeMap.put(QUERYCOLLECTION, aesEncode);
		encodeTypeMap.put(SAVELOGINTOKEN, aesEncode);
		encodeTypeMap.put(FINDMSGBYTOKEN, aesEncode);
		encodeTypeMap.put(ALLOWLOGIN, aesEncode);
		encodeTypeMap.put(FINDLOGINBYTOKEN, aesEncode);

		encodeTypeMap.put(ANNOUNCELIST, aesEncode);
		encodeTypeMap.put(ANNOUNCEWINDOWSLIST, aesEncode);
		encodeTypeMap.put(ANNOUNCEDETAIL, aesEncode);
		encodeTypeMap.put(ANNOUNCERECORD, aesEncode);
		encodeTypeMap.put(ADDRESSCOUNT, aesEncode);
		encodeTypeMap.put(ORGPAGE, aesEncode);
		encodeTypeMap.put(MEMBERPAGE, aesEncode);
		encodeTypeMap.put(ADDRESSALL, aesEncode);
		encodeTypeMap.put(TOREGIST, rsaEncode);
		encodeTypeMap.put(TOFORGET, rsaEncode);
		encodeTypeMap.put(DOREGIST, rsaEncode);
		encodeTypeMap.put(DOFORGET, rsaEncode);
		encodeTypeMap.put(SENDSMS, rsaEncode);
		encodeTypeMap.put(IMFILE, aesEncode);
		encodeTypeMap.put(GRAPHICRECORD, aesEncode);
		encodeTypeMap.put(OASQUARELIST, aesEncode);
		encodeTypeMap.put(USERLOGIN, rsaEncode);
		encodeTypeMap.put(LOGINPARAMETER, aesEncode);
		encodeTypeMap.put(VERSIONUPDATE, rsaEncode);
		encodeTypeMap.put(VERSIONPCUPDATE, aesEncode);
		encodeTypeMap.put(GARBAGEDEAL, aesEncode);
		encodeTypeMap.put(UQERYSIGNDATE, aesEncode);
		encodeTypeMap.put(UQERYISSIGN, aesEncode);
		encodeTypeMap.put(SIGNIN, aesEncode);
		encodeTypeMap.put(MONTHINTEGRAL, clearEncode);
		encodeTypeMap.put(RULEINTEGRAL, clearEncode);
		encodeTypeMap.put(INTAKEINTEGRAL, clearEncode);
		encodeTypeMap.put(INTAKEMOREINTEGRAL, clearEncode);
		encodeTypeMap.put(MONTHINTEGRALAES, aesEncode);
		encodeTypeMap.put(VERSIONUPDATENEW, rsaEncode);
		encodeTypeMap.put(RECONNECTIMSERVER, rsaEncode);
		encodeTypeMap.put(RECONNECTIMSERVERNEW, rsaEncode);
		encodeTypeMap.put(DOINTERNETREGIST, rsaEncode);
		encodeTypeMap.put(PREFECTINFO, rsaEncode);
		encodeTypeMap.put(SMSINVIT, rsaEncode);
		encodeTypeMap.put(INTERNETADD, aesEncode);
		encodeTypeMap.put(GETRESERVEFIELD, clearEncode);
		encodeTypeMap.put(LOADVPRIVILEGE, clearEncode);
		encodeTypeMap.put(VPRIVILEGECLICK, clearEncode);
		encodeTypeMap.put(SHAREURL, clearEncode);
		encodeTypeMap.put(CHECKDRAGONFRUIT, clearEncode);
		encodeTypeMap.put(DRAGONFRUITNOTGET, clearEncode);
		encodeTypeMap.put(GETDRAGONFRUIT, clearEncode);
		encodeTypeMap.put(GETSYSTEMPARAM, clearEncode);

		encodeTypeMap.put(WB_DOATTENTION, aesEncode);
		encodeTypeMap.put(WB_DOCANCELATTENTION, aesEncode);
		encodeTypeMap.put(WB_GETALLAPP, aesEncode);
		encodeTypeMap.put(WB_GETAPPDETAIL, aesEncode);
		encodeTypeMap.put(WB_GETLABEL, aesEncode);
		encodeTypeMap.put(WB_GETSELFAPP, aesEncode);
		encodeTypeMap.put(GETPERSONALIZEAPP, aesEncode);
		encodeTypeMap.put(WB_GRAPHICPSUH, aesEncode);
		encodeTypeMap.put(WB_MENUPUSHMSG, aesEncode);
		encodeTypeMap.put(WB_GETMENU, aesEncode);
		encodeTypeMap.put(WB_DOWNLOAD, aesEncode);
		encodeTypeMap.put(WB_TWODIMENSIONALCODE, aesEncode);
		encodeTypeMap.put(ZT_DOWNLOAD, clearEncode);

		encodeTypeMap.put(ADDRESS_DEPARTMENT_ADD, aesEncode);
		encodeTypeMap.put(ADDRESS_DEPARTMENT_UPDATE, aesEncode);
		encodeTypeMap.put(ADDRESS_DEPARTMENT_DELETE, aesEncode);
		encodeTypeMap.put(MY_CONTACT, aesEncode);
		encodeTypeMap.put(MY_CONTACT_ADD, aesEncode);
		encodeTypeMap.put(MY_CONTACT_DELETE, aesEncode);
		encodeTypeMap.put(MY_CONTACT_GROUP, aesEncode);
		encodeTypeMap.put(MY_CONTACT_GROUP_ADD, aesEncode);
		encodeTypeMap.put(MY_CONTACT_GROUP_DELETE, aesEncode);
		encodeTypeMap.put(MY_CONTACT_GROUP_MODIFY, aesEncode);
		encodeTypeMap.put(MY_CONTACT_GROUP_UPDATE, aesEncode);

		encodeTypeMap.put(HLW_AUTH_GETPREFECTINFO, aesEncode);
		encodeTypeMap.put(HLW_AUTH_PREFECTCORP, aesEncode);
		encodeTypeMap.put(HLW_AUTH_SUBMITPREFECT, aesEncode);
		encodeTypeMap.put(HLW_AUTH_UPLOADOFFICIAL, aesEncode);

		encodeTypeMap.put(HLW_MEMBER_ADD, aesEncode);
		encodeTypeMap.put(HLW_MEMBER_UPDATE, aesEncode);
		encodeTypeMap.put(HLW_MEMBER_DELETE, aesEncode);

		encodeTypeMap.put(MAILRECOMMEND, aesEncode);
		encodeTypeMap.put(MAILCONFIG, aesEncode);
		encodeTypeMap.put(CORPCUSTOM, aesEncode);
		encodeTypeMap.put(CORPCUSTOMFLAG, aesEncode);

		encodeTypeMap.put(GETVOTESUBJECT, clearEncode);
		encodeTypeMap.put(SUBMITVOTE, clearEncode);
		encodeTypeMap.put(GETVOTEOPTIONS, clearEncode);
		encodeTypeMap.put(GETVOTERESULT, clearEncode);
		encodeTypeMap.put(GETVOTEOPTIONSDETAILL, clearEncode);
		encodeTypeMap.put(GETVOTESUBJECTCOUNT, clearEncode);
		encodeTypeMap.put(GETOTHERVOTEOPTIONSDETAILL, clearEncode);

		encodeTypeMap.put(ST_FORGETPWD, rsaEncode);
		encodeTypeMap.put(ST_FORGETSMS, rsaEncode);
		encodeTypeMap.put(ST_INTERNETPREFECT, rsaEncode);
		encodeTypeMap.put(ST_INTERNETPWD, rsaEncode);
		encodeTypeMap.put(ST_SENDSMSCODE, rsaEncode);
		encodeTypeMap.put(ST_SENDVOICECODE, rsaEncode);
		encodeTypeMap.put(ST_VALICATECODE, rsaEncode);
		encodeTypeMap.put(ST_OLDPWD, aesEncode);
		encodeTypeMap.put(ST_TOKEN, aesEncode);

		encodeTypeMap.put(USERLOGIN_SESSIONID, rsaEncode);
		encodeTypeMap.put(USERLOGIN_VERIFYCODE, rsaEncode);

		encodeTypeMap.put(GETAFFILIATION, clearEncode);
		encodeTypeMap.put(GETDEPARTMENTS, clearEncode);
		encodeTypeMap.put(GETPERSONINFOLEADERSEARCH, clearEncode);
		encodeTypeMap.put(GETPERSONINFOLEADERSEARCHLIST, clearEncode);

		encodeTypeMap.put(pay_getAddPointDateList, clearEncode);
		encodeTypeMap.put(pay_getDateFormatOrderList, clearEncode);
		encodeTypeMap.put(pay_getExchange, clearEncode);
		encodeTypeMap.put(pay_getIntegralcommodityDateil, clearEncode);
		encodeTypeMap.put(pay_getIntegralcommodityList, clearEncode);
		encodeTypeMap.put(pay_getOrderDetailList, clearEncode);
		encodeTypeMap.put(pay_getPayPointDateList, clearEncode);
		encodeTypeMap.put(pay_getPayPointDetailList, clearEncode);
		encodeTypeMap.put(pay_getRedeemCodeCommodity, clearEncode);
		encodeTypeMap.put(pay_getRedeemCodeOrder, clearEncode);
		encodeTypeMap.put(pay_validataPoint, clearEncode);

		encodeTypeMap.put(GETOAACCOUNTINFO, aesEncode);

		encodeTypeMap.put(BEAUTYJSPRAISE, clearEncode);
		encodeTypeMap.put(BEAUTYJSDISCUSS, clearEncode);
		encodeTypeMap.put(GETDISCUSSANDPRAISE, clearEncode);
		encodeTypeMap.put(CANCELBEAUTYJSPRAISE, clearEncode);
		encodeTypeMap.put(GETBEAUTYJSLIST, clearEncode);
		encodeTypeMap.put(BEAUTYJSENTERDETAIL, clearEncode);

		encodeTypeMap.put(GETSENSITIVEWORD, clearEncode);

		encodeTypeMap.put(NEWSLISTSORT, aesEncode);
		encodeTypeMap.put(SHEQUPUSH, aesEncode);

		encodeTypeMap.put(NODISTURB, aesEncode);
		encodeTypeMap.put(NODISTURB_SERVICE, aesEncode);

		encodeTypeMap.put(IM_ATTEND, aesEncode);
		encodeTypeMap.put(IM_CANCEL_ATTEND, aesEncode);
		encodeTypeMap.put(IM_GET_ATTENTION, aesEncode);
		encodeTypeMap.put(IM_ATTEND_BATCH, aesEncode);
		encodeTypeMap.put(SINGLECHAT, clearEncode);
		encodeTypeMap.put(GROUPCHAT, clearEncode);
		encodeTypeMap.put(FWHHISTORY, clearEncode);
		encodeTypeMap.put(DECRYPT, aesEncode);
		encodeTypeMap.put(GETNEWSLIST, clearEncode);
		encodeTypeMap.put(GETNEWSINFO, clearEncode);
		encodeTypeMap.put(CIRCLEMEMBERLIST, clearEncode);
		encodeTypeMap.put(FINDFESTIVAL, clearEncode);
		encodeTypeMap.put(PCSINGLECHAT, aesEncode);
		encodeTypeMap.put(PCGROUPCHAT, aesEncode);

		encodeTypeMap.put(SQUEAREFEEDBACKSAVE, aesEncode);

	}

	public static final Map<String, String> functionOptCodeMap = new HashMap<String, String>();

	static {
		functionOptCodeMap.put(ANNOUNCELIST, "C0002006");
		functionOptCodeMap.put(ANNOUNCEDETAIL, "C0002007");
		functionOptCodeMap.put(ANNOUNCERECORD, "C0002008");
		functionOptCodeMap.put(ADDRESSCOUNT, "C0012004");
		functionOptCodeMap.put(ANNOUNCEDETAILFORH5, "C0002007");
		functionOptCodeMap.put(ANNOUNCERECORDH5, "C0002008");
		functionOptCodeMap.put(ORGPAGE, "C0012005");
		functionOptCodeMap.put(MEMBERPAGE, "C0012006");
		functionOptCodeMap.put(ADDRESSALL, "C0012007");
		functionOptCodeMap.put(TOREGIST, "C0015010");
		functionOptCodeMap.put(TOFORGET, "C0015011");
		functionOptCodeMap.put(DOREGIST, "C0015012");
		functionOptCodeMap.put(DOFORGET, "C0015013");
		functionOptCodeMap.put(SENDSMS, "C0015014");
		functionOptCodeMap.put(IMFILE, "C0016003");
		functionOptCodeMap.put(USERLOGIN, "C0014003");
		functionOptCodeMap.put(LOGINPARAMETER, "C0014004");
		functionOptCodeMap.put(VERSIONUPDATE, "C0036001");
		functionOptCodeMap.put(VERSIONUPDATENEW, "C0036002");
		// functionOptCodeMap.put(VERSIONPCUPDATE, "C0036003");
		functionOptCodeMap.put(GARBAGEDEAL, "C0012008");
		functionOptCodeMap.put(UQERYSIGNDATE, "C0022002");
		functionOptCodeMap.put(SIGNIN, "C0022001");
		functionOptCodeMap.put(MONTHINTEGRAL, "C0022003");
		functionOptCodeMap.put(RULEINTEGRAL, "C0022004");
		functionOptCodeMap.put(INTAKEINTEGRAL, "C0022005");
		functionOptCodeMap.put(INTAKEMOREINTEGRAL, "C0022006");
		functionOptCodeMap.put(VERSIONUPDATENEW, "C0012002");
		functionOptCodeMap.put(DOINTERNETREGIST, "C0023001");
		functionOptCodeMap.put(PREFECTINFO, "C0023002");
		functionOptCodeMap.put(INTERNETADD, "C0023003");
		functionOptCodeMap.put(GETRESERVEFIELD, "C0001001");
		functionOptCodeMap.put(LOADVPRIVILEGE, "C0020001");
		functionOptCodeMap.put(VPRIVILEGECLICK, "C0020005");

		functionOptCodeMap.put(GETSYSTEMPARAM, "C0015015");

		functionOptCodeMap.put(WB_DOATTENTION, "C0019024");
		functionOptCodeMap.put(WB_DOCANCELATTENTION, "C0019026");
		functionOptCodeMap.put(WB_GETALLAPP, "C0019023");
		functionOptCodeMap.put(WB_GETAPPDETAIL, "C0019025");
		functionOptCodeMap.put(WB_GETLABEL, "C0019027");
		functionOptCodeMap.put(WB_GETSELFAPP, "C0019028");
		functionOptCodeMap.put(WB_MENUPUSHMSG, "C0019029");
		functionOptCodeMap.put(WB_GETMENU, "C0019030");
		functionOptCodeMap.put(WB_DOWNLOAD, "C0019033");
		functionOptCodeMap.put(WB_TWODIMENSIONALCODE, "C0019034");

		functionOptCodeMap.put(getWorkTeamMsgList, "C0017001");
		functionOptCodeMap.put(getSomeOneWorkTeamMsgList, "C0017002");
		functionOptCodeMap.put(appendWorkTeamMessage, "C0017003");
		functionOptCodeMap.put(deleteWorkTeamMessage, "C0017004");
		functionOptCodeMap.put(deleteMyWorkTeamMessage, "C0017004");
		functionOptCodeMap.put(appendWorkTeamReply, "C0017005");
		functionOptCodeMap.put(appendWorkTeamPraise, "C0017006");
		functionOptCodeMap.put(cancelWorkTeamPraise, "C0017007");
		functionOptCodeMap.put(changeAvatar, "C0015003");
		functionOptCodeMap.put(GETRESERVEFIELD, "C0001001");
		functionOptCodeMap.put(GETSYSTEMPARAM, "C0015015");
		functionOptCodeMap.put(DORESERPASSWORD, "C0015002");
		functionOptCodeMap.put(ADDRESSCORRECTION, "C0012009");
		functionOptCodeMap.put(CLIENTLOGUPLOAD, "C0015016");
		functionOptCodeMap.put(createSendTask, "C0011001");
		functionOptCodeMap.put(getSendTaskList, "C0011002");
		functionOptCodeMap.put(editSendTask, "C0011003");
		functionOptCodeMap.put(endSendTask, "C0011004");
		functionOptCodeMap.put(cancelSendTask, "C0011005");
		functionOptCodeMap.put(sendTaskReceipt, "C0011006");
		functionOptCodeMap.put(sendReadTaskReceipt, "C0011007");
		functionOptCodeMap.put(getSendTaskReceiptList, "C0011008");

		functionOptCodeMap.put(HLW_AUTH_GETPREFECTINFO, "C0035001");
		functionOptCodeMap.put(HLW_AUTH_PREFECTCORP, "C0035002");
		functionOptCodeMap.put(HLW_AUTH_SUBMITPREFECT, "C0035004");
		functionOptCodeMap.put(HLW_AUTH_UPLOADOFFICIAL, "C0035003");

		functionOptCodeMap.put(GETVOTESUBJECT, "C0032001");
		functionOptCodeMap.put(SUBMITVOTE, "C0032002");
		functionOptCodeMap.put(GETVOTEOPTIONS, "C0032003");
		functionOptCodeMap.put(GETVOTERESULT, "C0032004");
		functionOptCodeMap.put(GETVOTEOPTIONSDETAILL, "C0032005");
		functionOptCodeMap.put(GETVOTESUBJECTCOUNT, "C0032006");
		functionOptCodeMap.put(GETOTHERVOTEOPTIONSDETAILL, "C0032007");

		functionOptCodeMap.put(ADDRESS_DEPARTMENT_ADD, "C0037001");
		functionOptCodeMap.put(ADDRESS_DEPARTMENT_DELETE, "C0037003");
		functionOptCodeMap.put(ADDRESS_DEPARTMENT_UPDATE, "C0037002");
		functionOptCodeMap.put(HLW_MEMBER_ADD, "C0037004");
		functionOptCodeMap.put(HLW_MEMBER_DELETE, "C0037006");
		functionOptCodeMap.put(HLW_MEMBER_UPDATE, "C0037005");

		functionOptCodeMap.put(MAILCONFIG, "C0031002");
		functionOptCodeMap.put(MAILRECOMMEND, "C0031001");
		functionOptCodeMap.put(INVITATIONSYSTEM_HOTFAQLIST, "C0034001");
		functionOptCodeMap.put(INVITATIONSYSTEM_MODULES, "C0034002");
		functionOptCodeMap.put(INVITATIONSYSTEM_FEEDBACK, "C0034003");

		functionOptCodeMap.put(USERLOGIN_VERIFYCODE, "C0014005");
		functionOptCodeMap.put(USERLOGIN_SESSIONID, "C0014006");

		functionOptCodeMap.put(ST_FORGETPWD, "C0015001");
		functionOptCodeMap.put(ST_FORGETSMS, "C0015018");
		functionOptCodeMap.put(ST_INTERNETPREFECT, "C0023002");
		functionOptCodeMap.put(ST_INTERNETPWD, "C0023001");
		functionOptCodeMap.put(ST_OLDPWD, "C0015002");
		functionOptCodeMap.put(ST_SENDSMSCODE, "C0015014");
		functionOptCodeMap.put(ST_SENDVOICECODE, "C0015017");
		functionOptCodeMap.put(ST_TOKEN, "C0015002");
		functionOptCodeMap.put(ST_VALICATECODE, "C0015019");

		functionOptCodeMap.put(GETOAACCOUNTINFO, "C0002006");

		functionOptCodeMap.put(GETSENSITIVEWORD, "C0002010");

		// TODO
		functionOptCodeMap.put(NEWSLISTSORT, "C0014007");
	}
}
