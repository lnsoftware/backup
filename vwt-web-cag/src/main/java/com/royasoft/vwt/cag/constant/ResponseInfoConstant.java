/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应编码及描述常量类
 *
 * @Author:MB
 * @Since:2016年3月23日
 */
public class ResponseInfoConstant {

    public static final String SUCC = "0000";

    /** 请求失败-参数有误 */
    public static final String FAIL1001 = "-1001";

    /** 请求失败-参数值为空 */
    public static final String FAIL1002 = "-1002";

    /** 请求失败-获取密钥失败 */
    public static final String FAIL1003 = "-1003";

    /** 请求失败-其他错误 */
    public static final String FAIL1004 = "-1004";

    /** 请求失败-参数解密失败 */
    public static final String FAIL1005 = "-1005";

    /** 获取公告列表失败-参数有误 */
    public static final String FAIL1006 = "-1006";

    /** 获取公告详情失败-参数有误 */
    public static final String FAIL1007 = "-1007";

    /** 请求失败-功能号有误 */
    public static final String FAIL1008 = "-1008";

    /** 获取公告详情阅读次数失败-参数有误 */
    public static final String FAIL1009 = "-1009";

    /** Sqlite增量更新模块-参数有误 */
    public static final String FAIL1010 = "-1010";

    /** Sqlite增量更新模块-内部数据错误 */
    public static final String FAIL1011 = "-1011";

    /** IM文件处理-文件信息有误 */
    public static final String FAIL1012 = "-1012";

    /** 激活&修改密码-用户不存在 */
    public static final String FAIL1013 = "-1013";

    /** 激活&修改密码-该用户已激活 */
    public static final String FAIL1014 = "-1014";

    /** 激活&修改密码-该用户尚未激活 */
    public static final String FAIL1015 = "-1015";

    /** 激活&修改密码-验证码校验失败或修改密码失败 */
    public static final String FAIL1016 = "-1016";

    /** 请求失败-加密方式有误 */
    public static final String FAIL1017 = "-1017";

    /** 激活&修改密码-参数有误 */
    public static final String FAIL1018 = "-1018";

    /** 激活&修改密码-手机号码正在审核 */
    public static final String FAIL1019 = "-1019";

    /** 激活&修改密码-验证码失效 */
    public static final String FAIL1020 = "-1020";

    /** 获取图文次数失败-参数有误 */
    public static final String FAIL1021 = "-1021";

    /** 获取图文次数失败-数据库操作异常 */
    public static final String FAIL1022 = "-1022";

    /** 登陆鉴权-参数有误 */
    public static final String FAIL1023 = "-1023";

    /** 登陆鉴权-不存在该用户 */
    public static final String FAIL1024 = "-1024";

    /** 登陆鉴权-该用户尚未激活 */
    public static final String FAIL1025 = "-1025";

    /** 登陆鉴权-保存AESKEY失败 */
    public static final String FAIL1026 = "-1026";

    /** 登陆鉴权-检测并修改安卓版本信息失败 */
    public static final String FAIL1027 = "-1027";

    /** 登陆鉴权-密码错误 */
    public static final String FAIL1028 = "-1028";

    /** 登陆鉴权-保存imsi和clientType失败 */
    public static final String FAIL1029 = "-1029";

    /** 登陆鉴权-发送强制下线消息失败 */
    public static final String FAIL1030 = "-1030";

    /** 版本更新-参数有误 */
    public static final String FAIL1031 = "-1031";

    /** 版本更新-版本信息为空 */
    public static final String FAIL1032 = "-1032";

    /** 登录获取参数-参数有误 */
    public static final String FAIL1033 = "-1033";

    /** 获取灰度发布开关-企业id为空 */
    public static final String FAIL1034 = "-1034";

    /** 签到-参数有误 */
    public static final String FAIL1035 = "-1035";

    /** 签到-用户不存在 */
    public static final String FAIL1036 = "-1036";

    /** 签到-该用户当天已签到 */
    public static final String FAIL1037 = "-1037";

    /** 签到-其它错误 */
    public static final String FAIL1038 = "-1038";

    /** 获取当月签到信息-参数有误 */
    public static final String FAIL1039 = "-1039";
    /** 获取当月签到信息-用户不存在 */
    public static final String FAIL1040 = "-1040";
    /** 获取当月签到信息-其它错误 */
    public static final String FAIL1041 = "-1041";

    /** 获取用户总积分-参数有误 */
    public static final String FAIL1042 = "-1042";

    /** 获取用户总积分-用户不存在 */
    public static final String FAIL1043 = "-1043";
    /** 获取用户总积分-其它错误 */
    public static final String FAIL1044 = "-1044";
    /** 重连新消息服务器异常 */
    public static final String FAIL1045 = "-1045";
    /** 互联网个人注册完善资料-该用户未进行互联网注册 */
    public static final String FAIL1046 = "-1046";

    /** 互联网个人注册完善资料-企业名称已存在 */
    public static final String FAIL1047 = "-1047";

    /** 互联网个人注册完善资料-企业开户失败 */
    public static final String FAIL1048 = "-1048";

    /** 互联网个人注册完善资料-人员表添加失败 */
    public static final String FAIL1049 = "-1049";

    /** 互联网个人注册完善资料-注册用户表添加失败 */
    public static final String FAIL1050 = "-1050";

    /** 互联网添加人员-该企业不存在或信息有误 */
    public static final String FAIL1051 = "-1051";

    /** 互联网添加人员-当前用户不存在 */
    public static final String FAIL1063 = "-1063";

    /** 互联网添加人员-该企业部门不存在或信息有误 */
    public static final String FAIL1052 = "-1052";

    /** 互联网添加人员-被添加人员已存在 */
    public static final String FAIL1053 = "-1053";

    /** 互联网添加人员-人员表添加失败 */
    public static final String FAIL1054 = "-1054";

    /** 请求失败-该用户不存在 */
    public static final String FAIL1055 = "-1055";

    /** 多角色工作台-该应用不存在 */
    public static final String FAIL1056 = "-1056";

    /** 多角色工作台-该用户已关注(收藏)该应用 */
    public static final String FAIL1057 = "-1057";

    /** 多角色工作台-该用户尚未关注(收藏)该应用 */
    public static final String FAIL1058 = "-1058";

    /** 多角色工作台-该菜单不存在 */
    public static final String FAIL1059 = "-1059";

    /** 多角色工作台-关注或取消关注失败 */
    public static final String FAIL1060 = "-1060";

    /** 多角色工作台-获取应用详情失败 */
    public static final String FAIL1061 = "-1061";

    /** 多角色工作台-数据异常 */
    public static final String FAIL1062 = "-1062";

    /** 常用通讯录获取参数-参数有误 */
    public static final String FAIL1064 = "-1064";
    /** 常用通讯录-该常用通讯不存在 */
    public static final String FAIL1065 = "-1065";
    /** 常用通讯录-常用通讯录删除失败 */
    public static final String FAIL1073 = "-1073";
    /** 常用通讯录-该联系人已存在 */
    public static final String FAIL1074 = "-1074";

    /** 邀请体系-缺少参数 */
    public static final String FAIL1066 = "-1066";

    /** 邀请体系-参数有误 */
    public static final String FAIL1067 = "-1067";

    /** 邀请体系-处理异常 */
    public static final String FAIL1068 = "-1068";

    /** 邀请体系-文件上传失败 */
    public static final String FAIL1069 = "-1069";

    /** 邀请体系-数据保存失败 */
    public static final String FAIL1070 = "-1070";

    /** 邮箱域名列表内部数据出错 */
    public static final String FAIL1071 = "-1071";
    /** 邮箱域名列表参数异常 */
    public static final String FAIL1072 = "-1072";
    /** 企业或部门logo参数异常 */
    public static final String FAIL1081 = "-1081";
    /** 企业或部门logo内部数据出错 */
    public static final String FAIL1082 = "-1082";
    /** 企业或部门logo标志位参数异常 */
    public static final String FAIL1083 = "-1083";
    /** 企业或部门logo标志位内部数据出错 */
    public static final String FAIL1084 = "-1084";

    /** 获取投票主题列表参数异常 */
    public static final String FAIL1085 = "-1085";
    /** 投票内部数据出错 */
    public static final String FAIL1086 = "-1086";
    /** 提交投票参数异常 */
    public static final String FAIL1087 = "-1087";
    /** 提交投票查询相关信息异常 */
    public static final String FAIL1088 = "-1088";
    /** 获取投票选项列表参数异常 */
    public static final String FAIL1089 = "-1089";
    /** 查询投票记录异常 */
    public static final String FAIL1090 = "-1090";
    /** 获取投票选项详情参数异常 */
    public static final String FAIL1091 = "-1091";
    /** 投票功能激活用户id不存在 */
    public static final String FAIL1092 = "-1092";
    /** 投票功能获取激活用户信息异常 */
    public static final String FAIL1093 = "-1093";
    /** 重复提交投票 */
    public static final String FAIL1094 = "-1094";
    
    /** 投票已过期 */
    public static final String FAIL1095 = "-1095";
    
    /** 你今天已参加过投票,请明天再来! */
    public static final String FAIL1096 = "-1096";

    /** 美丽江苏功能请求参数错误 */
    public static final String FAIL1101 = "-1101";
    /** 美丽江苏功能请求服务化异常 */
    public static final String FAIL1102 = "-1102";
    
    /** 获取图文内容参数错误 */
    public static final String FAIL1116 = "-1116";
    /** 获取图文内容调取服务失败 */
    public static final String FAIL1117 = "-1117";
    /** 获取图文内容调取服务异常 */
    public static final String FAIL1118 = "-1118";
    
    /** 积分-处理异常 */
    public static final String FAIL1200 = "-1200";
    /** 积分-商品不存在 */
    public static final String FAIL1201 = "-1201";
    /** 积分-兑换码不存在 */
    public static final String FAIL1202 = "-1202";

    /** 固定密码登陆鉴权-不存在该用户 */
    public static final String FAIL1098 = "-1098";

    /** 固定密码登陆鉴权-该用户尚未激活 */
    public static final String FAIL1099 = "-1099";

    /** 互联网体验企业-企业认证-该用户不存在 */
    public static final String Fail2501 = "-2501";

    /** 互联网体验企业-企业认证-该企业不存在 */
    public static final String Fail2502 = "-2502";

    /** 互联网体验企业-企业认证-该用户不是该企业管理员 */
    public static final String Fail2503 = "-2503";

    /** 互联网体验企业-企业认证-完善信息失败 */
    public static final String Fail2504 = "-2504";

    /** 互联网体验企业-企业认证-该企业已存在 */
    public static final String Fail2505 = "-2505";

    /** 互联网体验企业-企业认证-上传认证公函失败 */
    public static final String Fail2506 = "-2506";

    /** 互联网体验企业-企业认证-企业认证信息已提交并正在审核 */
    public static final String Fail2507 = "-2507";

    /** 互联网体验企业-企业认证-获取企业认证信息失败 */
    public static final String Fail2508 = "-2508";

    /** 互联网体验企业-企业认证-尚未填写企业信息 */
    public static final String Fail2509 = "-2509";

    /** 互联网体验企业-企业认证-尚未上传认证公函 */
    public static final String Fail2510 = "-2510";

    /** 互联网体验企业-企业认证-提交企业认证失败 */
    public static final String Fail2511 = "-2511";

    /** 互联网体验企业通讯录管理-管理员用户不存在 */
    public static final String FAIL2601 = "-2601";// ：
    /** 互联网体验企业通讯录管理-企业不存在 */
    public static final String FAIL2602 = "-2602";// ：
    /** 互联网体验企业通讯录管理-该用户不是该企业管理员 */
    public static final String FAIL2603 = "-2603";
    /** 互联网体验企业通讯录管理-添加部门-上级部门不存在 */
    public static final String FAIL2604 = "-2604";
    /** 互联网体验企业通讯录管理-添加部门-添加部门已存在 */
    public static final String FAIL2605 = "-2605";
    /** 互联网体验企业通讯录管理-添加部门-添加部门失败 */
    public static final String FAIL2606 = "-2606";
    /** 互联网体验企业通讯录管理-该部门不存在 */
    public static final String FAIL2607 = "-2607";
    /** 互联网体验企业通讯录管理-添加部门-修改部门名称已存在 */
    public static final String FAIL2608 = "-2608";
    /** 互联网体验企业通讯录管理-添加部门-修改部门失败 */
    public static final String FAIL2609 = "-2609";
    /** 互联网体验企业通讯录管理-删除部门-该部门下有子部门或员工 */
    public static final String FAIL2610 = "-2610";
    /** 互联网体验企业通讯录管理-删除部门-删除部门失败 */
    public static final String FAIL2611 = "-2611";
    /** 互联网体验企业通讯录管理-添加人员-添加人员在该部门下已存在 */
    public static final String FAIL2612 = "-2612";
    /** 互联网体验企业通讯录管理-添加人员-添加人员失败 */
    public static final String FAIL2613 = "-2613";
    /** 互联网体验企业通讯录管理-修改人员-被修改用户不存在 */
    public static final String FAIL2614 = "-2614";
    /** 互联网体验企业通讯录管理-修改人员-修改人员失败 */
    public static final String FAIL2615 = "-2615";
    /** 互联网体验企业通讯录管理-删除人员-被删除用户不存在 */
    public static final String FAIL2616 = "-2616";
    /** 互联网体验企业通讯录管理-删除人员-删除人员失败 */
    public static final String FAIL2617 = "-2617";
    /** 互联网体验企业通讯录管理-修改人员-被修改的用户已存在于该部门下 */
    public static final String FAIL2618 = "-2618";
    /** 互联网体验企业通讯录管理-删除人员-管理员不能删除自己 */
    public static final String FAIL2619 = "-2619";

    /** 设置-该用户进行互联网体验用户流程 */
    public static final String FAIL2701 = "-2701";// ：
    /** 设置-该用户进行正常登陆流程 */
    public static final String FAIL2702 = "-2702";// ：
    /** 设置-该用户进行用户注册流程 */
    public static final String FAIL2703 = "-2703";
    /** 设置-操作已超时 */
    public static final String FAIL2704 = "-2704";
    /** 设置-该用户已存在 */
    public static final String FAIL2705 = "-2705";
    /** 设置-下发语音验证码失败 */
    public static final String FAIL2706 = "-2706";
    /** 设置-该用户已下发过验证码 */
    public static final String FAIL2707 = "-2707";
    /** 设置-校验超时唯一标识失败 */
    public static final String FAIL2708 = "-2708";
    /** 设置-原密码错误 */
    public static final String FAIL2709 = "-2709";
    /** 设置-无法进行token改密 */
    public static final String FAIL2710 = "-2710";
    /** 设置-校验token失败 */
    public static final String FAIL2711 = "-2711";
    /** 设置-您获取验证码次数已超过上限！ */
    public static final String FAIL2712 = "-2712";
    /** IMS通讯录集团列表内部数据出错 */
    public static final String FAIL2801 = "-2801";
    /** IMS通讯录部门列表参数错误 */
    public static final String FAIL2802 = "-2802";
    /** IMS通讯录人员列表参数错误 */
    public static final String FAIL2803 = "-2803";
    /** IMS通讯录人员查询参数错误 */
    public static final String FAIL2804 = "-2804";
    /** 二学一做-参数错误 */
    public static final String FAIL2901 = "-2901";

    /** 黑名单-用户无权限 */
    public static final String FAIL1999 = "-1999";
    /** 获取OA账号信息查询参数错误 **/
    public static final String FAIL4001 = "-4001";
    /** OA账号信息查询异常 **/
    public static final String FAIL4002 = "-4002";

    /** 获取敏感词异常 **/
    public static final String FAIL4003 = "-4003";

    /** 消息队列排序异常 */
    public static final String FAIL4004 = "-4004";

    /** 同步免打扰状态异常 */
    public static final String FAIL4005 = "-4005";

    /** 单聊消息异常 */
    public static final String FAIL4006 = "-4006";

    /** 群聊消息异常 */
    public static final String FAIL4007 = "-4007";
    /** 当前页为空 */
    public static final String FAIL92011 = "-92011";
    /** 获取新闻列表异常 */
    public static final String FAIL92012 = "-92012";
    /** 新闻ID为空 */
    public static final String FAIL92021 = "-92021";
    /** 获取新闻详情失败 */
    public static final String FAIL92022 = "-92022";
    /** 获取新闻详情异常 */
    public static final String FAIL92023 = "-92023";
    /** 服务号反馈异常 */
    public static final String FAIL92024 = "-92024";

	/** 内购-二期-产品限购 */
	public static final String FAIL92025 = "-92025";
	/** 内购-二期-库存不足 */
	public static final String FAIL92026 = "-92026";
	/** 内购-二期-活动限购 */
	public static final String FAIL92027 = "-92027";

    /** 应答码 **/
    public static final Map<String, String> responseMap = new HashMap<String, String>();
    static {
        responseMap.put(SUCC, "请求成功");
        responseMap.put(FAIL1001, "请求失败-参数缺失");
        responseMap.put(FAIL1002, "请求失败-function_id为空");
        responseMap.put(FAIL1003, "请求失败-获取密钥失败");
        responseMap.put(FAIL1004, "请求失败-其他错误");
        responseMap.put(FAIL1005, "请求失败-参数解密失败");
        responseMap.put(FAIL1006, "获取公告列表失败-参数有误");
        responseMap.put(FAIL1007, "获取公告详情失败-参数有误");
        responseMap.put(FAIL1008, "请求失败-功能号有误");
        responseMap.put(FAIL1009, "获取公告详情阅读次数失败-参数有误");
        responseMap.put(FAIL1010, "Sqlite增量更新模块-参数有误");
        responseMap.put(FAIL1011, "Sqlite增量更新模块-内部数据错误");
        responseMap.put(FAIL1012, "IM文件处理-文件信息有误");
        responseMap.put(FAIL1013, "激活&修改密码-用户不存在");
        responseMap.put(FAIL1014, "激活&修改密码-该用户已激活");
        responseMap.put(FAIL1015, "激活&修改密码-该用户尚未激活");
        responseMap.put(FAIL1016, "激活&修改密码-验证码校验失败或修改密码失败");
        responseMap.put(FAIL1017, "请求失败-加密方式有误");
        responseMap.put(FAIL1018, "激活&修改密码-参数有误");
        responseMap.put(FAIL1019, "激活&修改密码-手机号码正在审核");
        responseMap.put(FAIL1020, "激活&修改密码-验证码失效");
        responseMap.put(FAIL1021, "获取图文阅读次数失败-参数有误");
        responseMap.put(FAIL1023, "登陆鉴权-参数有误");
        responseMap.put(FAIL1024, "登陆鉴权-不存在该用户");
        responseMap.put(FAIL1025, "登陆鉴权-该用户尚未激活");
        responseMap.put(FAIL1026, "登陆鉴权-保存AESKEY失败");
        responseMap.put(FAIL1027, "登陆鉴权-参数有误");
        responseMap.put(FAIL1028, "登陆鉴权-密码错误");
        responseMap.put(FAIL1029, "登陆鉴权-保存imsi和clientType失败");
        responseMap.put(FAIL1030, "登陆鉴权-发送强制下线消息失败");
        responseMap.put(FAIL1031, "版本更新-参数有误");
        responseMap.put(FAIL1032, "版本更新-版本信息为空");
        responseMap.put(FAIL1033, "登录获取参数-参数有误");
        responseMap.put(FAIL1034, "获取灰度发布开关-企业id为空");
        responseMap.put(FAIL1035, "签到-参数有误");
        responseMap.put(FAIL1036, "签到-用户不存在");
        responseMap.put(FAIL1037, "签到-该用户当天已签到");
        responseMap.put(FAIL1038, "签到-其它错误");
        responseMap.put(FAIL1039, "获取当月签到信息-参数有误");
        responseMap.put(FAIL1040, "获取当月签到信息-用户不存在");
        responseMap.put(FAIL1041, "获取当月签到信息-其它错误");
        responseMap.put(FAIL1042, "获取用户总积分-参数有误");
        responseMap.put(FAIL1043, "获取用户总积分-用户不存在");
        responseMap.put(FAIL1044, "获取用户总积分-其它错误");
        responseMap.put(FAIL1045, "重连新消息服务器异常");
        responseMap.put(FAIL1046, "互联网个人注册完善资料-该用户未进行互联网注册");
        responseMap.put(FAIL1047, "互联网个人注册完善资料-企业名称已存在");
        responseMap.put(FAIL1048, "互联网个人注册完善资料-企业开户失败");
        responseMap.put(FAIL1049, "互联网个人注册完善资料-人员表添加失败");
        responseMap.put(FAIL1050, "互联网个人注册完善资料-注册用户表添加失败 ");
        responseMap.put(FAIL1051, "互联网添加人员-该企业不存在或信息有误");
        responseMap.put(FAIL1052, "互联网添加人员-该企业部门不存在或信息有误");
        responseMap.put(FAIL1053, "互联网添加人员-被添加人员已存在");
        responseMap.put(FAIL1054, "互联网添加人员-人员表添加失败");
        responseMap.put(FAIL1055, "请求失败-该用户不存在");
        responseMap.put(FAIL1056, "多角色工作台-该应用不存在");
        responseMap.put(FAIL1057, "多角色工作台-该用户已关注(收藏)该应用");
        responseMap.put(FAIL1058, "多角色工作台-该用户尚未关注(收藏)该应用");
        responseMap.put(FAIL1059, "多角色工作台-该菜单不存在");
        responseMap.put(FAIL1060, "多角色工作台-关注或取消关注失败");
        responseMap.put(FAIL1061, "多角色工作台-获取应用详情失败");
        responseMap.put(FAIL1062, "多角色工作台-数据异常");
        responseMap.put(FAIL1063, "互联网添加人员-当前用户不存在");

        responseMap.put(FAIL1064, "常用联系人-参数有误");
        responseMap.put(FAIL1065, "常用联系人-该常用联系人不存在");
        responseMap.put(FAIL1073, "常用联系人-删除失败");
        responseMap.put(FAIL1074, "常用联系人-该联系人已存在");

        responseMap.put(FAIL1066, "邀请体系-缺少参数");
        responseMap.put(FAIL1067, "邀请体系-参数有误");
        responseMap.put(FAIL1068, "邀请体系-处理异常");
        responseMap.put(FAIL1069, "邀请体系-文件上传失败");
        responseMap.put(FAIL1070, "邀请体系-数据保存失败");

        responseMap.put(FAIL2601, "互联网体验企业添加部门-管理员用户不存在");
        responseMap.put(FAIL2602, "互联网体验企业添加部门-企业不存在");
        responseMap.put(FAIL2603, "互联网体验企业添加部门-用户不是该企业管理员");
        responseMap.put(FAIL2604, "互联网体验企业添加部门-部门上级不存在");
        responseMap.put(FAIL2605, "互联网体验企业添加部门-部门已存在");
        responseMap.put(FAIL2606, "互联网体验企业添加部门-部门添加失败");
        responseMap.put(FAIL2607, "互联网体验企业修改部门-部门不存在");
        responseMap.put(FAIL2608, "互联网体验企业修改部门-部门名称已存在");
        responseMap.put(FAIL2609, "互联网体验企业修改部门-部门修改失败");
        responseMap.put(FAIL2610, "互联网体验企业删除部门-部门下有子部门或员工");
        responseMap.put(FAIL2611, "互联网体验企业修改部门-部门删除失败");

        responseMap.put(FAIL1071, "邮箱配置无相关配置信息");
        responseMap.put(FAIL1072, "邮箱配置参数异常");
        responseMap.put(FAIL1081, "企业或部门logo参数异常");
        responseMap.put(FAIL1082, "企业或部门logo内部数据出错");
        responseMap.put(FAIL1083, "企业或部门logo标志位参数异常");
        responseMap.put(FAIL1084, "企业或部门logo标志位内部数据出错");

        responseMap.put(FAIL1085, "获取投票主题列表参数异常");
        responseMap.put(FAIL1086, "投票内部数据出错");
        responseMap.put(FAIL1087, "提交投票参数异常");
        responseMap.put(FAIL1088, "提交投票查询相关信息异常 ");
        responseMap.put(FAIL1089, "获取投票选项列表参数异常");
        responseMap.put(FAIL1090, "查询投票记录异常");
        responseMap.put(FAIL1091, "获取投票选项详情参数异常");
        responseMap.put(FAIL1092, "投票功能激活用户id不存在 ");
        responseMap.put(FAIL1093, "投票功能获取激活用户信息异常 ");
        responseMap.put(FAIL1094, "重复提交投票");
        responseMap.put(FAIL1095, "投票已过期");
        responseMap.put(FAIL1096, "你今天已参加过投票,请明天再来!");

        responseMap.put(FAIL2612, "互联网体验企业通讯录管理-添加人员-添加人员在该部门下已存在");
        responseMap.put(FAIL2613, "互联网体验企业通讯录管理-添加人员-添加人员失败");
        responseMap.put(FAIL2614, "互联网体验企业通讯录管理-修改人员-被修改用户不存在");
        responseMap.put(FAIL2615, "互联网体验企业通讯录管理-修改人员-修改人员失败");
        responseMap.put(FAIL2616, "互联网体验企业通讯录管理-删除人员-被删除用户不存在");
        responseMap.put(FAIL2617, "互联网体验企业通讯录管理-删除人员-删除人员失败");
        responseMap.put(FAIL2618, "互联网体验企业通讯录管理-修改人员-被修改的用户已存在于该部门下");
        responseMap.put(FAIL2619, "互联网体验企业通讯录管理-删除人员-管理员不能删除自己");

        responseMap.put(FAIL2701, "设置-该用户进行互联网体验用户流程");
        responseMap.put(FAIL2702, "设置-该用户进行正常登陆流程");
        responseMap.put(FAIL2703, "设置-该用户进行用户注册流程");
        responseMap.put(FAIL2704, "设置-操作已超时");
        responseMap.put(FAIL2705, "设置-该用户已存在");
        responseMap.put(FAIL2706, "设置-下发语音验证码失败");
        responseMap.put(FAIL2707, "验证码已下发，请耐心等待");
        responseMap.put(FAIL2708, "设置-校验超时唯一标识失败");
        responseMap.put(FAIL2709, "设置-原密码错误");
        responseMap.put(FAIL2710, "设置-无法进行token改密");
        responseMap.put(FAIL2711, "设置-校验token失败");
        responseMap.put(FAIL2712, "您获取验证码次数已超过上限！");

        responseMap.put(FAIL2801, "IMS通讯录集团列表内部数据出错");
        responseMap.put(FAIL2802, "IMS通讯录部门列表参数错误");
        responseMap.put(FAIL2803, "IMS通讯录人员列表查询参数错误");
        responseMap.put(FAIL2804, "IMS通讯录人员查询参数错误");

        responseMap.put(FAIL2901, "二学一做-参数有误");

        responseMap.put(FAIL1999, "黑名单-用户无权限");

        responseMap.put(FAIL4001, "获取OA账号信息查询参数错误");
        responseMap.put(FAIL4002, "OA账号信息查询异常");

        responseMap.put(FAIL1101, "美丽江苏功能请求参数错误");
        responseMap.put(FAIL1102, "美丽江苏功能请求服务化异常");
        responseMap.put(FAIL4003, "获取敏感词异常");
        responseMap.put(FAIL4004, "消息队列排序异常");
        responseMap.put(FAIL4005, "同步免打扰状态异常");
        responseMap.put(FAIL4006, "单聊消息查询异常");
        responseMap.put(FAIL4007, "群聊消息查询异常");

        responseMap.put(FAIL92011, "当前页为空");
        responseMap.put(FAIL92012, "获取新闻列表异常");
        responseMap.put(FAIL92021, "新闻ID为空");
        responseMap.put(FAIL92022, "获取新闻详情失败");
        responseMap.put(FAIL92023, "获取新闻详情异常");
        
        responseMap.put(FAIL1116, "获取图文内容参数错误");
        responseMap.put(FAIL1117, "获取图文内容调取服务失败");
        responseMap.put(FAIL1118, "获取图文内容调取服务异常");
        responseMap.put(FAIL92024, "服务号反馈异常 ");

		responseMap.put(FAIL92025, "产品限购");
		responseMap.put(FAIL92026, "库存不足 ");
		responseMap.put(FAIL92027, "活动限购 ");
    }
}