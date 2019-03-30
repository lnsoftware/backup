/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.constant;

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

	/** 登陆业务请求功能号集合 */
	public static List<String> loginAuthUrlFunctionIdList = new ArrayList<String>();
	/** 用户反馈业务请求功能号集合 */
	public static List<String> userFeedbackUrlFunctionIdList = new ArrayList<String>();

	/** 素材中心功能号集合 */
	public static List<String> materialCenterFunctionIdList = new ArrayList<String>();

	/** 投票功能号集合 */
	public static List<String> voteFunctionIdList = new ArrayList<String>();

	/** 互联网认证 */
	public static List<String> internetAuthFunctionIdList = new ArrayList<String>();

	/** 通讯录相关功能号集合 */
	public static List<String> addressFunctionIdList = new ArrayList<String>();

	/** 公告相关功能集合 */
	public static List<String> announceFunctionIdList = new ArrayList<String>();

	/** 素材中心相关功能号集合 */
	public static List<String> graphicSourceFunctionIdList = new ArrayList<String>();

	/** Redis管理相关功能号集合 */
	public static List<String> redisManagerFunctionIdList = new ArrayList<String>();

	/** 工作圈黑名单相关功能号集合 */
	public static List<String> workTeamFunctionIdList = new ArrayList<String>();

	/** 两学一做功能号集合 */
	public static List<String> twoLearnFunctionIdList = new ArrayList<String>();

	/** 圈子管理功能号集合 */
	public static List<String> circleFunctionIdList = new ArrayList<String>();

	/** 关键字功能号集合 */
	public static List<String> keyWordFunctionIdList = new ArrayList<String>();

	/** 服务号信息功能号集合 */
	public static List<String> squareMessageFunctionIdList = new ArrayList<>();

	/** 群聊功能号集合 */
	public static List<String> imGroupFunctionIdList = new ArrayList<String>();

	/** 百度富文本编辑器上传图片功能号集合 */
	public static List<String> baiduUploadList = new ArrayList<>();

	/** 节日欢迎图功能集合 */
	public static List<String> festivalList = new ArrayList<>();

	/** 服务号反馈集合 */
	public static List<String> squeareFeedbackList = new ArrayList<>();

	/** 数据导出功能号集合 */
	public static List<String> exportFunctionIdList = new ArrayList<>();

	/** 企业管理平台--部门管理集合 */
	public static List<String> deptManagerFunctionIdList = new ArrayList<String>();

	/** 二维码url集合 */
	public static List<String> urlmanageFunctionIdList = new ArrayList<>();

	/** 内购需求功能集合 */
	public static List<String> insidePurchFunctionIdList = new ArrayList<>();

	/** 投票主题查询 */
	public static final String VOTESUBJECTQUERY = "2101";
	/** 投票主题修改新增 */
	public static final String VOTESUBJECTSAVE = "2102";
	/** 投票主题删除 */
	public static final String VOTESUBJECTDEL = "2103";
	/** 获取单个主题 */
	public static final String VOTESUBJECT = "2104";
	/** 投票上传 */
	public static final String VOTEUPLOAD = "2105";

	/** 投票选项查询 */
	public static final String VOTEOPTIONSQUERY = "2106";
	/** 投票选项修改新增 */
	public static final String VOTEOPTIONSSAVE = "2107";
	/** 投票选项删除 */
	public static final String VOTEOPTIONSDEL = "2108";
	/** 投票结果列表查询 */
	public static final String VOTERSQUERY = "2109";
	/** 权限人员列表 */
	public static final String VOTETREE = "2110";
	/** 权限企业列表 */
	public static final String VOTETREECORP = "2114";
	/** 图文上传 */
	public static final String TWUPLOAD = "2111";
	/** 投票主题发布 */
	public static final String VOTESUBJECTFB = "2112";
	/** 单个投票选项查询 */
	public static final String VOTEOPTIONVIEW = "2113";
	/** 人员树右侧列表 */
	public static final String VOTECONTROLLER = "2115";
	/** 投票结果导出 */
	public static final String VOTERECORDEXPORT = "2116";

	/** 企业权限右侧列表 */
	public static final String VOTECONTROLCORP = "2117";

	/** 分页展示视频统计 */
	public static final String GETVIDEOLEARNDETAIL = "2201";

	/** 分页展示已看视频，已看完，企业所有人员 */
	public static final String GETVIDEOLEARNLIST = "2202";

	/** 导出视频统计 */
	public static final String EXPORTVIDEOSTATISTICS = "2203";

	/** 分页展示视频列表 */
	public static final String GETVIDEOLIST = "2204";

	/** 新增视频信息 */
	public static final String INSERTVIDEO = "2205";

	/** 修改视频信息 */
	public static final String UPDATEVIDEO = "2206";

	/** 获取单个视频信息 */
	public static final String GETVIDEO = "2207";

	/** 刷新视频状态时间 */
	public static final String FRESHENVIDEO = "2208";

	/** 修改视频上传状态 */
	public static final String UPDATEVIDEOCOMPLETESTATE = "2209";

	/** 修改视频是否发布 */
	public static final String UPDATEVIDEOPUBLISHSTATE = "2210";

	/** 查询问题反馈列表 */
	public static final String getUserFeedbackList = "2701";
	/** 获取反馈问题 */
	public static final String getUserFeedbackInfo = "2702";
	/** 反馈用户提出问题 */
	public static final String beginUserFeedback = "2703";
	/** 删除用户提出问题 */
	public static final String deleteUserFeedbackById = "2704";

	/** 互联网认证查询列表 */
	public static final String getInternetAuthList = "2801";
	/** 地市管理员互联网认证信息 */
	public static final String getInterAuthInfoFromCity = "2802";
	/** 区县管理员互联网认证获取信息 */
	public static final String getInterAuthInfoFromArea = "2803";
	/** 区县管理员开户时获取信息 */
	public static final String getInterAuthInfoOpen = "2804";
	/** 客户经理互联网认证获取信息 */
	public static final String getInterAuthInfoFromCustome = "2805";
	/** 审核互联网认证信息 */
	public static final String examineInterAuth = "2806";
	/** 获取客户经理 */
	public static final String getInterCustomer = "2808";
	/** 获取行业 */
	public static final String getIndustryMsg = "2809";
	/** 修改短信 */
	public static final String updateSendMessage = "2810";
	/** 导出excel */
	public static final String hlwAuthListExport = "2811";
	/** 查询是否发送短信 */
	public static final String getIsSendMessage = "2812";

	/** 素材中心-获取素材中心列表(小v团队) */
	public static final String GETGRAPHICSOURCELIST = "3001";
    
    /** 导出用户提出问题 */
    public static final String exportUserFeedback = "2705";

	/** 素材中心-保存素材 */
	public static final String INSERTGRAPHICSOURCE = "3002";

	/** 素材中心-删除素材 */
	public static final String DELETEGRAPHICSOURCE = "3003";

	/** 素材中心-查询单个素材详细素材 */
	public static final String FINDGRAPHIC = "3004";

	/** 素材中心-上传图片 */
	public static final String GRAPHICSOURCEUPLOAD = "3005";

	/** 素材中心-裁剪图片 */
	public static final String CUTGRAPHICPIC = "3006";

	/** 素材中心-获取素材中心列表 */
	public static final String GETNEWGRAPHICSOURCELIST = "3007";

	/** 素材中心-生成图文内容二维码 */
	public static final String CREATEGRAPHICSOURCECODE = "3008";

	/** 素材中心-根据图文预览id获取预览内容 */
	public static final String GETGRAPHICSOURCEPREVIEW = "3009";

	/** 素材中心-根据素材id单个素材 */
	public static final String GETGRAPHICSOURCECONTENT = "3010";

	/** 黑名单-黑名单列表 */
	public static final String BLACKLIST_PAGE = "3051";
	/** 黑名单-新增 */
	public static final String BLACKLIST_ADD = "3052";
	/** 黑名单-删除 */
	public static final String BLACKLIST_DELETE = "3053";
	/** 黑名单-白名单列表 */
	public static final String BLACKLIST_WHITE = "3054";

	/** 工作圈回复列表 */
	public static final String WORKTEAM_REPLY_LIST = "3060";
	/** 工作圈-列表 */
	public static final String WORKTEAM_LIST = "3061";
	/** 说说详情-基础信息 */
	public static final String WORKTEAM_DETAIL_INFO = "3062";
	/** 说说详情-回复列表 */
	public static final String WORKTEAM_DETAIL_REPLY = "3063";
	/** 删除说说主贴 */
	public static final String WORKTEAM_DELETE_INFO = "3064";
	/** 删除回复 */
	public static final String WORKTEAM_DELETE_REPLY = "3065";

	/** 获取图文推送列表接口 */
	public static final String GETGRAPHICPUSHLIST = "3101";
	/** 图文推送列表导出接口 */
	public static final String EXPORTGRAPHICPUSHLIST = "3102";
	/** 图文推送(推送失败)重新推送接口 */
	public static final String GRAPHICPUSHAGAIN = "3103";
	/** 图文推送查询应用列表接口 */
	public static final String GETGRAPHICPUSHSERVICENOLIST = "3104";
	/** 图文推送客户经理查询企业列表接口 */
	public static final String GETGRAPHICPUSHCORPLIST = "3105";
	/** 新建图文推送活动接口 */
	public static final String INSERTGRAPHICPUSHINFO = "3106";
	/** 根据sessionid获取角色接口 */
	public static final String GETROLEBYSESSIONID = "3107";
	/** 获取推送详情 */
	public static final String GETGRAPHICPUSHDETAIL = "3108";
	/** 新建企业图文推送活动接口 */
	public static final String INSERTCORPGRAPHICPUSHINFO = "3109";
	/** 图文推送测试 */
	public static final String GRAPHICPUSHTEST = "3110";
	/** 撤销企业图文推送活动接口 */
	public static final String CANCELCORPGRAPHICPUSHINFO = "3113";
	/** 获取企业图文推送活动接口 */
	public static final String LISTCORPGRAPHICPUSHINFO = "3112";

	/** 图文推送-根据部门id获取激活人员 */
	public static final String GETCLIENTUSERBYDEPART = "3111";

	/** 素材中心-根据部门id获取激活人员 */
	public static final String GRAPHICPUSHWECHAT = "3114";

	/** 部门管理--查询 **/
	public static final String QUARE_DEPTMANAGER = "3401";

	/** 部门管理--删除 **/
	public static final String DEL_DEPTMANAGER = "3402";

	/** 部门管理--菜单查询 **/
	public static final String QUARY_MENU_DEPTMANAGER = "3403";

	/** 部门管理--保存 **/
	public static final String SAVE_DEPTMANAGER = "3404";

	/** 部门管理--修改 **/
	public static final String EDIT_DEPTMANAGER = "3405";

	/** 部门管理--详情 **/
	public static final String DETAIL_DEPTMANAGER = "3406";

	/** 未注册人员-获取部门 */
	public static final String UNREGISTE_DEPART = "6040";
	/** 未注册人员-获取人员 */
	public static final String UNREGISTE_MEMBER = "6041";
	/** 未注册人员-发送短信 */
	public static final String UNREGISTE_SENDMSG = "6042";

	/** 公告-获取部门数 */
	public static final String ANNOUNCE_DEPARTLIST = "6051";
	/** 公告-根据部门获取人员 */
	public static final String ANNOUNCE_USERLIST = "6052";
	/** 公告-获取公告详情 */
	public static final String ANNOUNCE_GETDETAIL = "6053";
	/** 公告-新增公告 */
	public static final String ANNOUNCE_ADD = "6054";
	/** 公告-编辑公告 */
	public static final String ANNOUNCE_EDIT = "6055";
	/** 公告-删除公告 */
	public static final String ANNOUNCE_DELETE = "6056";
	/** 公告-获取公告列表 */
	public static final String ANNOUNCE_GETLIST = "6057";
	/** 公告-置顶/取消置顶 */
	public static final String ANNOUNCE_TOP = "6058";
	/** 公告-获取图片验证码 */
	public static final String ANNOUNCE_GETVERIFYCODE = "6059";
	/** 公告-校验图片验证码 */
	public static final String ANNOUNCE_CHECKVERIFYCODE = "6060";
	/** 公告-发布/取消发布 */
	public static final String ANNOUNCE_PUBLISHANDCANCEL = "6061";

	/** redis-查询key */
	public static final String REDIS_MANAGE_QUERY = "8001";

	/** redis-删除key */
	public static final String REDIS_MANAGE_DELETE = "8002";

	public static final String USERLOGIN = "9001";

	/** 登录获取参数 */
	public static final String LOGINPARAMETER = "9002";

	/** 图文推送上传文件 */
	public static final String FILEUPLOAD = "110120";

	/** 圈子管理-分页获取所有的圈子 */
	public static final String CIRCLE_QUERY = "9101";

	/** 圈子管理-添加圈子 */
	public static final String CIRCLE_SAVE = "9102";

	/** 圈子管理-修改圈子 */
	public static final String CIRCLE_UPDATE = "9103";

	/** 圈子管理-删除圈子 */
	public static final String CIRCLE_DELETE = "9104";

	/** 圈子管理-分页查询圈子人员 */
	public static final String CIRCLE_MEMBER = "9105";

	/** 圈子管理-添加圈子人员 */
	public static final String CIRCLE_MEMBER_SAVE = "9106";

	/** 圈子管理-删除圈子人员 */
	public static final String CIRCLE_MEMBER_DELETE = "9107";

	/** 圈子管理-根据圈子ID删除圈子人员 */
	public static final String CIRCLE_MEMBER_DELETE_CICRLEID = "9108";

	/** 圈子管理-部门树 */
	public static final String CIRCLETREE = "9110";

	/** 圈子管理-部门树到人 */
	public static final String CIRCLETREECORP = "9114";

	/** 关键字管理-分页查询所有关键字 */
	public static final String KEYWORDS_QUERY = "9201";

	/** 关键字管理-添加关键字 */
	public static final String KEYWORDS_SAVE = "9202";

	/** 关键字管理-删除关键字 */
	public static final String KEYWORDS_DELETE = "9203";

	/** 群聊管理-根据手机号码查询群聊 */
	public static final String IMGROUP_QUERY = "9301";

	/** 群聊管理-查询群聊人员 */
	public static final String IMGROUP_MEMBER_QUERY = "9302";

	/** 群聊管理-查询群聊人员职位 */
	public static final String MEMBER_DUTY_QUERY = "9303";

	/** 群聊管理-删除群聊 */
	public static final String IMGROUP_DELETE = "9304";

	/** 群聊管理-删除群聊 人员 */
	public static final String IMGROUP_MEMBER_DELETE = "9305";

	/** 群聊管理-根据消息查询人员群组 */
	public static final String QUERYGROUPIDBYMESSAGE = "9306";

	/** 群聊管理-添加人员进群 */
	public static final String ADDMEMBERTOGROUP = "9307";

	/** 群聊管理-更换群主 */
	public static final String CHANGETASKMASTER = "9308";

	/** 群聊管理-添加群管理员 */
	public static final String ADDMANAGER = "9309";

	/** 群聊管理-删除群管理员 */
	public static final String DELMANAGER = "9310";

	/** 百度富文本编辑器上传图片 */
	public static final String BAIDUUPLOAD = "9311";

	/** 服务号信息 */
	public static final String SQUAREMESSAGE = "9401";

	/** 设置敏感词 */
	public static final String SETKEYWORDS = "9501";

	/** 查询错误信息 */
	public static final String KEYWORDS_ERRORMSG = "9601";

	/** 查询敏感词 */
	public static final String QUERYKEYWORDS = "9701";

	/** 查询节日欢迎图 */
	public static final String FINDFESTIVAL = "9901";

	/** 添加节日欢迎图 */
	public static final String FESTIVALSAVE = "9902";

	/** 删除节日欢迎图 */
	public static final String DELETEFESTIVAL = "9903";

	/** 修改节日欢迎图 */
	public static final String UPDATEFESTIVAL = "9904";

	/** 服务号反馈信息逻辑删除 */
	public static final String SQUEAREFEEDBACKSAVE = "9905";

	/** 服务号反馈信息单个查询 */
	public static final String SQUEAREFEEDBACKFINDONE = "9906";

	/** 服务号反馈信息分页模糊查询 */
	public static final String SQUEAREFEEDBACKFINDALL = "9907";

	/** 导出部门总人数 */
	public static final String PARTCOUNT = "10001";
	/** 导出部门-各部门已激活的人数 */
	public static final String PARTACTIVATIONCOUNT = "10002";
	/** 导出激活用户数 */
	public static final String ACTIVATIONMEMBER = "10003";
	/** 导出日新增用户量 */
	public static final String NEWMEMBER = "10004";
	/** 导出日活用户量/日登陆用户总数 */
	public static final String ALIVEMEMBER = "10005";
	/** 导出服务号总数 */
	public static final String SERVICECOUNT = "10006";
	/** 查询个人登录情况 */
	public static final String QUERYLOGONMSG = "10007";
	/** 查询工作圈每日新增数量 */
	public static final String QUERYWORKCOUNT = "10008";

	/** 保存二维码url */
	public static final String URLMANAGESAVE = "10009";

	/** 山东需求-内购手机-商品-新增 */
	public static final String INSIDE_PURCH_GOODS_ADD = "10051";
	/** 山东需求-内购手机-商品-修改 */
	public static final String INSIDE_PURCH_GOODS_UPDATE = "10052";
	/** 山东需求-内购手机-商品-删除 */
	public static final String INSIDE_PURCH_GOODS_DELETE = "10053";
	/** 山东需求-内购手机-商品-详情 */
	public static final String INSIDE_PURCH_GOODS_DETAIL = "10054";
	/** 山东需求-内购手机-商品-分页 */
	public static final String INSIDE_PURCH_GOODS_PAGE = "10055";
	/** 山东需求-内购手机-订购-列表 */
	public static final String INSIDE_PURCH_ORDERS_PAGE = "10056";
	/** 山东需求-内购手机-订购-导出 */
	public static final String INSIDE_PURCH_ORDERS_EXPORT = "10057";
	/** 山东需求-内购手机-删除商品前确认是否产生订单 */
	public static final String INSIDE_PURCH_CHECK_ORDERS = "10058";

	/** 山东需求二期-内购-商品-列表 */
	public static final String INSIDE_BUY_GOODS_LIST = "10059";
	/** 山东需求二期-内购-商品-新增 */
	public static final String INSIDE_BUY_GOODS_ADD = "10060";
	/** 山东需求二期-内购-商品-修改 */
	public static final String INSIDE_BUY_GOODS_UPDATE = "10061";
	/** 山东需求二期-内购-商品-删除 */
	public static final String INSIDE_BUY_GOODS_DELETE = "10062";
	/** 山东需求二期-内购-商品-详情 */
	public static final String INSIDE_BUY_GOODS_DETAIL = "10063";

	/** 山东需求二期-内购-商品类型-列表 */
	public static final String INSIDE_BUY_TYPE_LIST = "10064";
	/** 山东需求二期-内购-商品类型-新增 */
	public static final String INSIDE_BUY_TYPE_ADD = "10065";
	/** 山东需求二期-内购-商品类型-修改 */
	public static final String INSIDE_BUY_TYPE_UPDATE = "10066";
	/** 山东需求二期-内购-商品类型-删除 */
	public static final String INSIDE_BUY_TYPE_DELETE = "10067";
	/** 山东需求二期-内购-商品类型-详情 */
	public static final String INSIDE_BUY_TYPE_DETAIL = "10068";

	/** 山东需求二期-内购-活动-详情 */
	public static final String INSIDE_BUY_ACTIVITY_DETAIL = "10069";
	/** 山东需求二期-内购-活动-修改 */
	public static final String INSIDE_BUY_ACTIVITY_UPDATE = "10070";

	// 通讯录-部门
	public static final String ADDRESS_DEPART = "1051";
	// 通讯录-根据部门id查找人员
	public static final String ADDRESS_MEMBER = "1052";
	//
	public static final String ADDRESS_MEMBER_SEARCH = "1053";
	//
	public static final String ADDRESS_GET_DEPART_AND_MEMBERS_BYDEPTID = "1054";

	// 角色-获取角色列表
	public static final String ROLE__LIST = "1061";
	// 角色-角色组-新增
	public static final String ROLE_GROUP_ADD = "1062";
	// 角色-角色组-修改
	public static final String ROLE_GROUP_UPDATE = "1063";
	// 角色-角色组-删除
	public static final String ROLE_GROUP_DELETE = "1064";
	// 角色-角色组-详情
	public static final String ROLE_GROUP_DETAIL = "1065";
	// 角色-新增
	public static final String ROLE_ADD = "1066";
	// 角色-修改
	public static final String ROLE_UPDATE = "1067";
	// 角色-删除
	public static final String ROLE_DELETE = "1068";
	// 角色-详情
	public static final String ROLE_DETAIL = "1069";
	// 角色-人员列表
	public static final String ROLE_MEMBER_LIST = "1070";
	// 角色-增加人员
	public static final String ROLE_MEMBER_ADD = "1071";
	// 角色-批量删除人员
	public static final String ROLE_MEMBER_BATCH_DELETE = "1072";
	// 角色-新增修改角色时的 群组列表
	public static final String ROLE_GROUP_FOR_EDIT_ROLE = "1073";
	// 角色-获取角色列表
	public static final String ROLE_ROLE_LIST = "1074";
	// 角色-人员列表
	public static final String MEMBER_LIST = "1075";
	// 角色-主管理员-新增
	public static final String ROLE_MASTER_ADMIN_ADD = "1080";
	// 角色-主管理员-修改
	public static final String ROLE_MASTER_ADMIN_UPDATE = "1081";
	// 角色-主管理员-查询详情
	public static final String ROLE_MASTER_ADMIN_DETAIL = "1083";

	// 功能模块
	static {
		loginAuthUrlFunctionIdList.add(LOGINPARAMETER);

		materialCenterFunctionIdList.add(FILEUPLOAD);
		/** 用户反馈 */
		userFeedbackUrlFunctionIdList.add(getUserFeedbackList);
		userFeedbackUrlFunctionIdList.add(getUserFeedbackInfo);
		userFeedbackUrlFunctionIdList.add(beginUserFeedback);
		userFeedbackUrlFunctionIdList.add(deleteUserFeedbackById);

		/** 互联网认证 */
		internetAuthFunctionIdList.add(getInternetAuthList);
		internetAuthFunctionIdList.add(getInterAuthInfoFromCity);
		internetAuthFunctionIdList.add(getInterAuthInfoFromArea);
		internetAuthFunctionIdList.add(getInterAuthInfoOpen);
		internetAuthFunctionIdList.add(getInterAuthInfoFromCustome);
		internetAuthFunctionIdList.add(examineInterAuth);
		internetAuthFunctionIdList.add(getIsSendMessage);
		internetAuthFunctionIdList.add(getInterCustomer);
		internetAuthFunctionIdList.add(getIndustryMsg);
		internetAuthFunctionIdList.add(updateSendMessage);
		internetAuthFunctionIdList.add(hlwAuthListExport);

		voteFunctionIdList.add(VOTESUBJECTQUERY);
		voteFunctionIdList.add(VOTESUBJECTSAVE);
		voteFunctionIdList.add(VOTESUBJECTDEL);
		voteFunctionIdList.add(VOTESUBJECT);
		voteFunctionIdList.add(VOTEUPLOAD);

		voteFunctionIdList.add(VOTEOPTIONSQUERY);
		voteFunctionIdList.add(VOTEOPTIONSSAVE);
		voteFunctionIdList.add(VOTEOPTIONSDEL);
		voteFunctionIdList.add(VOTERSQUERY);
		voteFunctionIdList.add(VOTETREE);
		voteFunctionIdList.add(TWUPLOAD);
		voteFunctionIdList.add(VOTESUBJECTFB);
		voteFunctionIdList.add(VOTEOPTIONVIEW);
		voteFunctionIdList.add(VOTECONTROLLER);
		voteFunctionIdList.add(VOTERECORDEXPORT);
		voteFunctionIdList.add(VOTECONTROLCORP);

		addressFunctionIdList.add(UNREGISTE_DEPART);
		addressFunctionIdList.add(UNREGISTE_MEMBER);
		addressFunctionIdList.add(UNREGISTE_SENDMSG);

		announceFunctionIdList.add(ANNOUNCE_DEPARTLIST);
		announceFunctionIdList.add(ANNOUNCE_USERLIST);
		announceFunctionIdList.add(ANNOUNCE_GETDETAIL);
		announceFunctionIdList.add(ANNOUNCE_ADD);
		announceFunctionIdList.add(ANNOUNCE_EDIT);
		announceFunctionIdList.add(ANNOUNCE_DELETE);
		announceFunctionIdList.add(ANNOUNCE_GETLIST);
		announceFunctionIdList.add(ANNOUNCE_TOP);
		announceFunctionIdList.add(ANNOUNCE_GETVERIFYCODE);
		announceFunctionIdList.add(ANNOUNCE_CHECKVERIFYCODE);
		announceFunctionIdList.add(ANNOUNCE_PUBLISHANDCANCEL);

		/** 素材中心 */
		graphicSourceFunctionIdList.add(GETGRAPHICSOURCELIST);
		graphicSourceFunctionIdList.add(INSERTGRAPHICSOURCE);
		graphicSourceFunctionIdList.add(DELETEGRAPHICSOURCE);
		graphicSourceFunctionIdList.add(FINDGRAPHIC);
		graphicSourceFunctionIdList.add(GRAPHICSOURCEUPLOAD);
		graphicSourceFunctionIdList.add(CUTGRAPHICPIC);
		graphicSourceFunctionIdList.add(GETNEWGRAPHICSOURCELIST);
		graphicSourceFunctionIdList.add(CREATEGRAPHICSOURCECODE);
		graphicSourceFunctionIdList.add(GETGRAPHICSOURCEPREVIEW);
		graphicSourceFunctionIdList.add(GETGRAPHICSOURCECONTENT);

		redisManagerFunctionIdList.add(REDIS_MANAGE_QUERY);
		redisManagerFunctionIdList.add(REDIS_MANAGE_DELETE);
        userFeedbackUrlFunctionIdList.add(exportUserFeedback);
        

		workTeamFunctionIdList.add(BLACKLIST_PAGE);
		workTeamFunctionIdList.add(BLACKLIST_ADD);
		workTeamFunctionIdList.add(BLACKLIST_DELETE);
		workTeamFunctionIdList.add(BLACKLIST_WHITE);
		workTeamFunctionIdList.add(WORKTEAM_REPLY_LIST);
		workTeamFunctionIdList.add(WORKTEAM_LIST);
		workTeamFunctionIdList.add(WORKTEAM_DETAIL_INFO);
		workTeamFunctionIdList.add(WORKTEAM_DETAIL_REPLY);
		workTeamFunctionIdList.add(WORKTEAM_DELETE_INFO);
		workTeamFunctionIdList.add(WORKTEAM_DELETE_REPLY);

		twoLearnFunctionIdList.add(GETVIDEOLEARNDETAIL);
		twoLearnFunctionIdList.add(GETVIDEOLEARNLIST);
		twoLearnFunctionIdList.add(EXPORTVIDEOSTATISTICS);
		twoLearnFunctionIdList.add(GETVIDEOLIST);
		twoLearnFunctionIdList.add(INSERTVIDEO);
		twoLearnFunctionIdList.add(UPDATEVIDEO);
		twoLearnFunctionIdList.add(GETVIDEO);
		twoLearnFunctionIdList.add(FRESHENVIDEO);
		twoLearnFunctionIdList.add(UPDATEVIDEOCOMPLETESTATE);
		twoLearnFunctionIdList.add(UPDATEVIDEOPUBLISHSTATE);

		/** 关键字 */
		keyWordFunctionIdList.add(KEYWORDS_QUERY);
		keyWordFunctionIdList.add(KEYWORDS_SAVE);
		keyWordFunctionIdList.add(KEYWORDS_DELETE);
		keyWordFunctionIdList.add(KEYWORDS_ERRORMSG);
		keyWordFunctionIdList.add(SETKEYWORDS);
		keyWordFunctionIdList.add(QUERYKEYWORDS);

		/*** 圈子管理 **/
		circleFunctionIdList.add(CIRCLE_QUERY);
		circleFunctionIdList.add(CIRCLE_SAVE);
		circleFunctionIdList.add(CIRCLE_DELETE);
		circleFunctionIdList.add(CIRCLE_MEMBER);
		circleFunctionIdList.add(CIRCLE_UPDATE);
		circleFunctionIdList.add(CIRCLE_MEMBER_DELETE);
		circleFunctionIdList.add(CIRCLE_MEMBER_SAVE);
		circleFunctionIdList.add(CIRCLE_MEMBER_DELETE_CICRLEID);
		circleFunctionIdList.add(CIRCLETREE);
		circleFunctionIdList.add(CIRCLETREECORP);

		festivalList.add(FINDFESTIVAL);
		festivalList.add(FESTIVALSAVE);
		festivalList.add(DELETEFESTIVAL);
		festivalList.add(UPDATEFESTIVAL);

		squareMessageFunctionIdList.add(SQUAREMESSAGE);

		/*** 群聊管理 **/
		imGroupFunctionIdList.add(IMGROUP_QUERY);
		imGroupFunctionIdList.add(IMGROUP_MEMBER_QUERY);
		imGroupFunctionIdList.add(IMGROUP_MEMBER_DELETE);
		imGroupFunctionIdList.add(IMGROUP_DELETE);
		imGroupFunctionIdList.add(QUERYGROUPIDBYMESSAGE);
		imGroupFunctionIdList.add(ADDMEMBERTOGROUP);
		imGroupFunctionIdList.add(CHANGETASKMASTER);
		imGroupFunctionIdList.add(ADDMANAGER);
		imGroupFunctionIdList.add(DELMANAGER);

		baiduUploadList.add(BAIDUUPLOAD);

		imGroupFunctionIdList.add(MEMBER_DUTY_QUERY);

		/** 图文推送 */
		materialCenterFunctionIdList.add(FILEUPLOAD);
		materialCenterFunctionIdList.add(GETGRAPHICPUSHLIST);
		materialCenterFunctionIdList.add(EXPORTGRAPHICPUSHLIST);
		materialCenterFunctionIdList.add(GRAPHICPUSHAGAIN);
		materialCenterFunctionIdList.add(GETGRAPHICPUSHSERVICENOLIST);
		materialCenterFunctionIdList.add(GETGRAPHICPUSHCORPLIST);
		materialCenterFunctionIdList.add(INSERTGRAPHICPUSHINFO);
		materialCenterFunctionIdList.add(GETROLEBYSESSIONID);
		materialCenterFunctionIdList.add(GETGRAPHICPUSHDETAIL);
		materialCenterFunctionIdList.add(INSERTCORPGRAPHICPUSHINFO);
		materialCenterFunctionIdList.add(CANCELCORPGRAPHICPUSHINFO);
		materialCenterFunctionIdList.add(LISTCORPGRAPHICPUSHINFO);
		materialCenterFunctionIdList.add(GRAPHICPUSHTEST);
		materialCenterFunctionIdList.add(GETCLIENTUSERBYDEPART);
		materialCenterFunctionIdList.add(ROLE__LIST);
		materialCenterFunctionIdList.add(ROLE_GROUP_ADD);
		materialCenterFunctionIdList.add(ROLE_GROUP_UPDATE);
		materialCenterFunctionIdList.add(ROLE_GROUP_DELETE);
		materialCenterFunctionIdList.add(ROLE_GROUP_DETAIL);
		materialCenterFunctionIdList.add(ROLE_ADD);
		materialCenterFunctionIdList.add(ROLE_UPDATE);
		materialCenterFunctionIdList.add(ROLE_DELETE);
		materialCenterFunctionIdList.add(ROLE_DETAIL);
		materialCenterFunctionIdList.add(MEMBER_LIST);
		materialCenterFunctionIdList.add(ROLE_ROLE_LIST);
		materialCenterFunctionIdList.add(ROLE_MEMBER_LIST);
		materialCenterFunctionIdList.add(ROLE_MEMBER_ADD);
		materialCenterFunctionIdList.add(ROLE_MEMBER_BATCH_DELETE);
		materialCenterFunctionIdList.add(ROLE_GROUP_FOR_EDIT_ROLE);
		materialCenterFunctionIdList.add(ADDRESS_DEPART);
		materialCenterFunctionIdList.add(ADDRESS_MEMBER);
		materialCenterFunctionIdList.add(ADDRESS_MEMBER_SEARCH);
		materialCenterFunctionIdList.add(ADDRESS_GET_DEPART_AND_MEMBERS_BYDEPTID);
		materialCenterFunctionIdList.add(GRAPHICPUSHWECHAT);

		squeareFeedbackList.add(SQUEAREFEEDBACKSAVE);
		squeareFeedbackList.add(SQUEAREFEEDBACKFINDONE);
		squeareFeedbackList.add(SQUEAREFEEDBACKFINDALL);

		/** 导出数据 */
		exportFunctionIdList.add(PARTCOUNT);
		exportFunctionIdList.add(PARTACTIVATIONCOUNT);
		exportFunctionIdList.add(ACTIVATIONMEMBER);
		exportFunctionIdList.add(NEWMEMBER);
		exportFunctionIdList.add(ALIVEMEMBER);
		exportFunctionIdList.add(SERVICECOUNT);
		exportFunctionIdList.add(QUERYLOGONMSG);
		exportFunctionIdList.add(QUERYWORKCOUNT);

		/** 企业管理平台 --部门操作 **/
		deptManagerFunctionIdList.add(QUARE_DEPTMANAGER);
		deptManagerFunctionIdList.add(DEL_DEPTMANAGER);
		deptManagerFunctionIdList.add(QUARY_MENU_DEPTMANAGER);
		deptManagerFunctionIdList.add(SAVE_DEPTMANAGER);
		deptManagerFunctionIdList.add(EDIT_DEPTMANAGER);
		deptManagerFunctionIdList.add(DETAIL_DEPTMANAGER);

		/** 二维码url */
		urlmanageFunctionIdList.add(URLMANAGESAVE);

		/** 内购需求集合 */
		insidePurchFunctionIdList.add(INSIDE_PURCH_GOODS_ADD);
		insidePurchFunctionIdList.add(INSIDE_PURCH_GOODS_UPDATE);
		insidePurchFunctionIdList.add(INSIDE_PURCH_GOODS_DELETE);
		insidePurchFunctionIdList.add(INSIDE_PURCH_GOODS_DETAIL);
		insidePurchFunctionIdList.add(INSIDE_PURCH_GOODS_PAGE);
		insidePurchFunctionIdList.add(INSIDE_PURCH_ORDERS_PAGE);
		insidePurchFunctionIdList.add(INSIDE_PURCH_ORDERS_EXPORT);
		insidePurchFunctionIdList.add(INSIDE_PURCH_CHECK_ORDERS);
	}

	/**
	 * 模块加密方式
	 */
	public static final Map<String, String> encodeTypeMap = new HashMap<String, String>();

	static {

		encodeTypeMap.put(VOTESUBJECTQUERY, clearEncode);
		encodeTypeMap.put(VOTESUBJECTSAVE, clearEncode);
		encodeTypeMap.put(VOTESUBJECTDEL, clearEncode);
		encodeTypeMap.put(VOTESUBJECT, clearEncode);
		encodeTypeMap.put(VOTEUPLOAD, clearEncode);

		encodeTypeMap.put(VOTERSQUERY, clearEncode);
		encodeTypeMap.put(VOTEOPTIONSSAVE, clearEncode);
		encodeTypeMap.put(VOTEOPTIONSDEL, clearEncode);

		encodeTypeMap.put(VOTEOPTIONSQUERY, clearEncode);
		encodeTypeMap.put(VOTETREE, clearEncode);
		encodeTypeMap.put(TWUPLOAD, clearEncode);
		encodeTypeMap.put(VOTESUBJECTFB, clearEncode);
		encodeTypeMap.put(VOTEOPTIONVIEW, clearEncode);
		encodeTypeMap.put(VOTETREECORP, clearEncode);
		encodeTypeMap.put(VOTECONTROLLER, clearEncode);
		encodeTypeMap.put(VOTECONTROLCORP, clearEncode);

		encodeTypeMap.put(GETGRAPHICSOURCELIST, clearEncode);
		encodeTypeMap.put(INSERTGRAPHICSOURCE, clearEncode);
		encodeTypeMap.put(FINDGRAPHIC, clearEncode);
		encodeTypeMap.put(GRAPHICSOURCEUPLOAD, clearEncode);
		encodeTypeMap.put(CUTGRAPHICPIC, clearEncode);

		encodeTypeMap.put(GETVIDEOLEARNDETAIL, clearEncode);
		encodeTypeMap.put(GETVIDEOLEARNLIST, clearEncode);
		encodeTypeMap.put(EXPORTVIDEOSTATISTICS, clearEncode);
		encodeTypeMap.put(GETVIDEOLIST, clearEncode);
		encodeTypeMap.put(INSERTVIDEO, clearEncode);
		encodeTypeMap.put(UPDATEVIDEO, clearEncode);
		encodeTypeMap.put(GETVIDEO, clearEncode);
		encodeTypeMap.put(FRESHENVIDEO, clearEncode);
		encodeTypeMap.put(UPDATEVIDEOCOMPLETESTATE, clearEncode);
		encodeTypeMap.put(UPDATEVIDEOPUBLISHSTATE, clearEncode);

		encodeTypeMap.put(SQUAREMESSAGE, clearEncode);

		encodeTypeMap.put(BAIDUUPLOAD, clearEncode);

	}

	/**
	 * 操作日志
	 */
	public static final Map<String, String> functionOptCodeMap = new HashMap<String, String>();

	static {
		functionOptCodeMap.put(ANNOUNCE_GETLIST, "S0002001");
		functionOptCodeMap.put(ANNOUNCE_ADD, "S0002002");
		functionOptCodeMap.put(ANNOUNCE_DELETE, "S0002004");
		functionOptCodeMap.put(ANNOUNCE_TOP, "S0002005");
		functionOptCodeMap.put(ANNOUNCE_EDIT, "S0002007");

		functionOptCodeMap.put(getInternetAuthList, "S0035001");
		functionOptCodeMap.put(getInterAuthInfoFromCity, "S0035002");
		functionOptCodeMap.put(getInterAuthInfoFromArea, "S0035003");
		functionOptCodeMap.put(getInterAuthInfoOpen, "S0035004");
		functionOptCodeMap.put(getInterAuthInfoFromCustome, "S0035005");
		functionOptCodeMap.put(examineInterAuth, "S0035006");
		functionOptCodeMap.put(getInterCustomer, "S0035007");
		functionOptCodeMap.put(getIndustryMsg, "S0035008");
		functionOptCodeMap.put(updateSendMessage, "S0035009");
		functionOptCodeMap.put(hlwAuthListExport, "S0035010");
		functionOptCodeMap.put(getIsSendMessage, "S0035011");

		functionOptCodeMap.put(UNREGISTE_DEPART, "S0008025");
		functionOptCodeMap.put(UNREGISTE_MEMBER, "S0008026");
		functionOptCodeMap.put(UNREGISTE_SENDMSG, "S0008027");

		functionOptCodeMap.put(getUserFeedbackList, "S0034001");
		functionOptCodeMap.put(getUserFeedbackInfo, "S0034002");
		functionOptCodeMap.put(beginUserFeedback, "S0034003");
		functionOptCodeMap.put(deleteUserFeedbackById, "S0034004");

		functionOptCodeMap.put(VOTESUBJECTQUERY, "S0032001");
		functionOptCodeMap.put(VOTESUBJECTSAVE, "S0032002");
		functionOptCodeMap.put(VOTESUBJECTDEL, "S0032003");
		functionOptCodeMap.put(VOTESUBJECT, "S0032004");
		functionOptCodeMap.put(VOTEUPLOAD, "S0032005");
		functionOptCodeMap.put(VOTEOPTIONSQUERY, "S0032006");
		functionOptCodeMap.put(VOTEOPTIONSSAVE, "S0032007");
		functionOptCodeMap.put(VOTEOPTIONSDEL, "S0032008");
		functionOptCodeMap.put(VOTERSQUERY, "S0032009");
		functionOptCodeMap.put(VOTETREE, "S0032010");
		functionOptCodeMap.put(VOTETREECORP, "S0032011");
		functionOptCodeMap.put(TWUPLOAD, "S0032012");
		functionOptCodeMap.put(VOTESUBJECTFB, "S0032013");
		functionOptCodeMap.put(VOTEOPTIONVIEW, "S0032014");
		functionOptCodeMap.put(VOTECONTROLLER, "S0032015");
		functionOptCodeMap.put(VOTERECORDEXPORT, "S0032016");
		functionOptCodeMap.put(VOTECONTROLCORP, "S0032017");

	}
}
