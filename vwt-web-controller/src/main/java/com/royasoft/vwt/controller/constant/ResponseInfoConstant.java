/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.constant;

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

    /** 处理异常 */
    public static final String FAIL1006 = "-1006";

    /** 文件上传失败 */
    public static final String FAIL1007 = "-1007";

    /** 请求失败-功能号有误 */
    public static final String FAIL1008 = "-1008";
    
    /** 图文推送功能请求参数错误 */
    public static final String FAIL1301 = "-1301";
    /** 图文推送功能请求服务异常 */
    public static final String FAIL1302 = "-1302";
    /** 图文推送列表查询请求服务失败 */
    public static final String FAIL1303 = "-1303";
    /** 导出图文推送列表请求服务失败 */
    public static final String FAIL1304 = "-1304";
    /** 图文推送(推送失败)重新推送请求服务失败 */
    public static final String FAIL1305 = "-1305";
    /** 图文推送查询应用列表请求服务失败 */
    public static final String FAIL1306 = "-1306";
    /** 图文推送客户经理查询企业列表请求服务失败 */
    public static final String FAIL1307 = "-1307";
    /** 新建图文推送活动请求服务失败 */
    public static final String FAIL1308 = "-1308";
    /** 获取图文推送详情请求服务失败 */
    public static final String FAIL1309 = "-1309";
    /** 新建企业图文推送活动请求服务失败 */
    public static final String FAIL1310 = "-1310";
    /** 图文推送测试请求服务失败 */
    public static final String FAIL1311 = "-1311";
    /** 重新推送计划时间小于服务器当前时间 */
    public static final String FAIL1312 = "-1312";

    /** 投票请求参数错误 */
    public static final String FAIL2001 = "-2001";
    /** 投票主题查询请求没有数据 */
    public static final String FAIL2002 = "-2002";
    /** 投票主题新增修改错误 */
    public static final String FAIL2003 = "-2003";
    /** 投票主题删除失败 */
    public static final String FAIL2004 = "-2004";
    /** 主题发布异常 */
    public static final String FAIL2010 = "-2010";
    /** 投票上传文件失败 */
    public static final String FAIL2005 = "-2005";

    /** 投票选项查询请求参数错误 */
    public static final String FAIL2006 = "-2006";
    /** 投票选项查询请求没有数据 */
    public static final String FAIL2007 = "-2007";
    /** 投票选项新增修改错误 */
    public static final String FAIL2008 = "-2008";
    /** 投票选项删除失败 */
    public static final String FAIL2009 = "-2009";
    /** 单个投票选项为空 */
    public static final String FAIL2011 = "-2011";

    /** 投票结果列表请求参数错误 */
    public static final String FAIL2012 = "-2012";
    /** 投票权限保存失败 */
    public static final String FAIL2013 = "-2013";

    /** 投票部门树没有数据 */
    public static final String FAIL2014 = "-2014";
    /** 投票部门无人员 */
    public static final String FAIL2015 = "-2015";

    /** 人员数右侧列表无数据 */
    public static final String FAIL2016 = "-2016";

    /** 投票权限删除失败 */
    public static final String FAIL2017 = "-2017";

    /** 导出失败 */
    public static final String FAIL2018 = "-2018";

    /** 主题保存，参数有误 */
    public static final String FAIL2019 = "-2019";

    /** 投票截止日期需大于等于当前日期 */
    public static final String FAIL2020 = "-2020";

    /** 保存其他选项异常 */
    public static final String FAIL2021 = "-2021";
    /** 投票主题列表查询异常 */
    public static final String FAIL2022 = "-2022";

    /** 根据sessionid获取信息有误 */
    public static final String FAIL2051 = "-2051";
    /** 企业不存在 */
    public static final String FAIL2052 = "-2052";
    /** 未激活人员提醒-该企业当月以推送过 */
    public static final String FAIL2053 = "-2053";

    /** 公告不存在 */
    public static final String FAIL2061 = "-2061";
    /** 公告保存失败 */
    public static final String FAIL2062 = "-2062";
    /** 公告验证码校验失败 */
    public static final String FAIL2063 = "-2063";

    /** 互联网认证获取区县为空 */
    public static final String FAIL3001 = "-3001";

    /** 互联网认证获取区县客户经理为空 */
    public static final String FAIL3002 = "-3002";

    /** 互联网认证信息删除失败 */
    public static final String FAIL3003 = "-3003";

    /** 互联网认证信息角色为空 */
    public static final String FAIL3004 = "-3004";

    /** 互联网认证信息开户检查 */
    public static final String FAIL3005 = "-3005";

    /** 互联网认证信息导出失败 */
    public static final String FAIL3006 = "-3006";

    /** 素材中心获取session异常 */
    public static final String FAIL3051 = "-3051";

    /** 素材中心保存参数错误 */
    public static final String FAIL3052 = "-3052";

    /** 素材中心保存异常 */
    public static final String FAIL3053 = "-3053";

    /** 素材中心查询详情参数错误 */
    public static final String FAIL3054 = "-3054";

    /** 查询素材不存在 */
    public static final String FAIL3055 = "-3055";

    /** 查询素材详情异常 */
    public static final String FAIL3056 = "-3056";

    /** 裁剪素材图片参数错误 */
    public static final String FAIL3057 = "-3057";
    
    /** 裁剪素材图片不存在 */
    public static final String FAIL3058 = "-3058";

    /** 裁剪素材图片异常 */
    public static final String FAIL3059 = "-3059";

    /** 获取素材列表参数错误 */
    public static final String FAIL3060 = "-3060";

    /** 获取素材列表异常 */
    public static final String FAIL3061 = "-3061";
    
    /** 获取素材列表调取服务失败 */
    public static final String FAIL3062 = "-3062";
    /** 素材中心功能请求参数错误*/
    public static final String FAIL3063 = "-3063";
    /** 删除素材调取服务失败 */
    public static final String FAIL3064 = "-3064";
    /** 保存素材调取服务失败 */
    public static final String FAIL3065 = "-3065";
    /** 查询素材调取服务失败 */
    public static final String FAIL3066 = "-3066";
    /** 素材调取服务异常 */
    public static final String FAIL3067 = "-3067";
    /** 根据图文预览id获取预览内容请求服务失败 */
    public static final String FAIL3068 = "-3068";
    /** 根据素材id获取素材内容请求服务失败 */
    public static final String FAIL3069 = "-3069";

    /** 工作圈管理-说说不存在 */
    public static final String FAIL3071 = "-3071";

    /** 黑名单-保存失败 */
    public static final String FAIL3081 = "-3081";
    /** 黑名单-删除失败 */
    public static final String FAIL3082 = "-3082";

    /** 两学一做功能请求参数错误 */
    public static final String FAIL3101 = "-3101";

    /** 两学一做功能请求服务化异常 */
    public static final String FAIL3102 = "-3102";

    /** 两学一做导出统计结果失败 */
    public static final String FAIL3103 = "-3103";

    /** 两学一做上传视频失败 */
    public static final String FAIL3104 = "-3104";

    /** 两学一做上传视频超过1.5G */
    public static final String FAIL3105 = "-3105";

    /** 圈子管理请求参数错误 */
    public static final String FAIL9101 = "-9101";

    /** 圈子管理请求服务化异常 */
    public static final String FAIL9102 = "-9102";

    /** 圈子管理圈子已不存在败 */
    public static final String FAIL9103 = "-9103";

    /** 关键词请求参数错误 */
    public static final String FAIL9201 = "-9201";

    /** 关键词请求服务化异常 */
    public static final String FAIL9202 = "-9202";

    /** 群聊请求参数错误 */
    public static final String FAIL9301 = "-9301";

    /** 群聊请求服务化异常 */
    public static final String FAIL9302 = "-9302";

    /** 设置敏感词请求参数错误 */
    public static final String FAIL9501 = "-9501";

    /** 设置敏感词请求服务化异常 */
    public static final String FAIL9502 = "-9502";

    /** 查询包含敏感词错误信息服务化异常 */
    public static final String FAIL9601 = "-9601";

    /** 查询敏感词请求服务化异常 */
    public static final String FAIL9701 = "-9701";
    
    /** 查询欢迎图请求参数错误异常 */
    public static final String FAIL9901 = "-9901";
    
    /** 欢迎图请求服务化异常 */
    public static final String FAIL9902 = "-9902";
    
    /** 欢迎图请求时间重复 */
    public static final String FAIL9903 = "-9903";
    
    /** 保存二维码url请求参数错误 */
    public static final String FAIL9904 = "-9904";
    
    /** 数据已存在 */
    public static final String FAIL1100 = "-1100";
    
    /** 数据不存在 */
    public static final String FAIL1010 = "-1010";
    
    /** 不属于本企业，无权限操作 */
    public static final String FAIL1011 = "-1011";
    
    /**默认分组不能操作 */
    public static final String FAIL1103 = "-1103";
    
    /**群组下有角色，不能删除 */
    public static final String FAIL1101 = "-1101";
    
    /**校验角色名在当前群组是否已存在 */
    public static final String FAIL1102 = "-1102";
    
    /**该标签组已存在 */
    public static final String FAIL1104 = "-1104";
    
    
    
    
    
    /** 应答码 **/
    public static final Map<String, String> responseMap = new HashMap<String, String>();

    static {
        responseMap.put(SUCC, "请求成功");
        responseMap.put(FAIL1001, "请求失败-参数缺失");
        responseMap.put(FAIL1002, "请求失败-function_id为空");
        responseMap.put(FAIL1003, "请求失败-获取密钥失败");
        responseMap.put(FAIL1004, "请求失败-其他错误");
        responseMap.put(FAIL1005, "请求失败-参数解密失败");
        responseMap.put(FAIL1006, "处理异常");
        responseMap.put(FAIL1007, "文件上传失败");

        responseMap.put(FAIL2001, "投票请求参数错误");
        responseMap.put(FAIL2002, "投票主题查询请求没有数据 ");
        responseMap.put(FAIL2003, "投票主题新增修改报错");
        responseMap.put(FAIL2004, "投票主题删除失败");
        responseMap.put(FAIL2005, "投票上传文件失败");

        responseMap.put(FAIL2006, "投票选项查询请求参数错误");
        responseMap.put(FAIL2007, "投票选项查询请求没有数据 ");
        responseMap.put(FAIL2008, "投票选项新增修改报错");
        responseMap.put(FAIL2009, "投票选项删除失败");
        responseMap.put(FAIL2010, "单个投票主题为空");
        responseMap.put(FAIL2011, "单个投票选项为空");
        responseMap.put(FAIL2012, "投票结果列表请求参数错误");
        responseMap.put(FAIL2013, "投票结果列表没有数据");
        responseMap.put(FAIL2015, "投票部门无人员");
        responseMap.put(FAIL2016, "人员数右侧列表无数据");
        responseMap.put(FAIL2017, "投票权限删除失败");
        responseMap.put(FAIL2018, "导出失败");
        responseMap.put(FAIL2019, "主题保存，参数有误");
        responseMap.put(FAIL2020, "投票截止日期需大于等于当前日期");

        responseMap.put(FAIL2051, "根据sessionid获取信息有误");
        responseMap.put(FAIL2052, "企业不存在");
        responseMap.put(FAIL2053, "未激活人员提醒-该企业当月已推送过");

        /** 互联网认证 */
        responseMap.put(FAIL3001, "获取区县为空");
        responseMap.put(FAIL3002, "获取客户经理为空");
        responseMap.put(FAIL3003, "互联网认证删除失败");

        responseMap.put(FAIL3004, "登录已失效，请重新登录");
        responseMap.put(FAIL3005, "开户账户已存在，请重新开户");
        responseMap.put(FAIL3006, "登录已失效，导出失败");

        /** 素材中心 */
        responseMap.put(FAIL3051, "素材中心获取session异常");
        responseMap.put(FAIL3052, "素材中心保存参数错误");
        responseMap.put(FAIL3053, "素材中心保存异常 ");
        responseMap.put(FAIL3054, "素材中心查询详情参数错误");
        responseMap.put(FAIL3055, "查询素材不存在");
        responseMap.put(FAIL3056, "查询素材详情异常");
        responseMap.put(FAIL3057, "裁剪素材图片参数错误");
        responseMap.put(FAIL3058, "裁剪素材图片不存在");
        responseMap.put(FAIL3059, "裁剪素材图片异常");
        responseMap.put(FAIL3060, "获取素材列表参数错误 ");
        responseMap.put(FAIL3061, "获取素材列表异常");
        responseMap.put(FAIL3062, "获取素材列表调取服务失败");
        responseMap.put(FAIL3063, "素材中心功能请求参数错误");
        responseMap.put(FAIL3064, "删除素材调取服务失败 ");
        responseMap.put(FAIL3065, "保存素材调取服务失败");
        responseMap.put(FAIL3066, "查询素材调取服务失败");
        responseMap.put(FAIL3067, "素材调取服务异常");
        responseMap.put(FAIL3068, "根据图文预览id获取预览内容请求服务失败");
        responseMap.put(FAIL3069, "根据素材id获取素材内容请求服务失败");

        /** 公告 */
        responseMap.put(FAIL2061, "公告不存在");
        responseMap.put(FAIL2062, "公告保存失败");
        responseMap.put(FAIL2063, "验证码校验失败");

        /** 两学一做 */
        responseMap.put(FAIL3101, "两学一做功能请求参数错误");
        responseMap.put(FAIL3102, "两学一做功能请求服务化异常");
        responseMap.put(FAIL3103, "两学一做导出统计结果失败");
        responseMap.put(FAIL3104, "两学一做上传视频失败");

        /** 圈子管理 */
        responseMap.put(FAIL9101, "圈子管理请求参数错误");
        responseMap.put(FAIL9102, "圈子管理请求服务化异常");
        responseMap.put(FAIL9103, "圈子管理圈子已不存在败");

        /** 关键词 */
        responseMap.put(FAIL9201, "关键词请求参数错误");
        responseMap.put(FAIL9202, "关键词请求服务化异常");

        /** 关键词 */
        responseMap.put(FAIL9301, "群聊请求参数错误");
        responseMap.put(FAIL9302, "群聊请求服务化异常");

        responseMap.put(FAIL9501, "设置敏感词请求参数错误");
        responseMap.put(FAIL9502, "设置敏感词请求服务化异常");
        responseMap.put(FAIL9601, "查询包含敏感词错误信息服务化异常");
        responseMap.put(FAIL9701, "查询敏感词请求服务化异常");
        
        responseMap.put(FAIL9901, "欢迎图请求参数错误异常");
        responseMap.put(FAIL9902, "欢迎图请求服务化异常");
        responseMap.put(FAIL9903, "添加欢迎图时间重复");
        
        /**图文推送**/
        responseMap.put(FAIL1301, "图文推送功能请求参数错误");
        responseMap.put(FAIL1302, "图文推送功能请求服务异常");
        responseMap.put(FAIL1303, "图文推送列表查询请求服务失败");
        responseMap.put(FAIL1304, "导出图文推送列表请求服务失败");
        responseMap.put(FAIL1305, "图文推送(推送失败)重新推送请求服务失败 ");
        responseMap.put(FAIL1306, "图文推送查询应用列表请求服务失败");
        responseMap.put(FAIL1307, "图文推送客户经理查询企业列表请求服务失败");
        responseMap.put(FAIL1308, "新建图文推送活动请求服务失败");
        responseMap.put(FAIL1309, "获取图文推送详情请求服务失败");
        responseMap.put(FAIL1310, "新建企业图文推送活动请求服务失败 ");
        responseMap.put(FAIL1311, "图文推送测试请求服务失败");
        responseMap.put(FAIL1312, "重新推送计划时间小于服务器当前时间");
        
        responseMap.put(FAIL9904, "保存二维码url请求参数错误");
        
        
        responseMap.put(FAIL1101, "当前标签组下有标签,无法删除");
        responseMap.put(FAIL1102, "同一个标签组不能出现相同标签");
        responseMap.put(FAIL1103, "默认分组无法编辑");
        responseMap.put(FAIL1104, "该标签组已存在");
        responseMap.put(FAIL1010, "数据不存在");
        responseMap.put(FAIL1011, "不属于本企业，无权限操作");
        
        

    }
}