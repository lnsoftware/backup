/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.utils;

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

    /** 请求失败-参数值为空 */
    public static final String FAIL1001 = "-1001";

    /** 请求失败-参数有误 */
    public static final String FAIL1002 = "-1002";

    /** 请求失败-数据不存在 */
    public static final String FAIL1003 = "-1003";
    
    /** 请求失败-用户不存在 */
    public static final String FAIL1004 = "-1004";
    
    /** 请求失败-可选类型重复 */
    public static final String FAIL1005 = "-1005";
    
    /** 请求失败-该商品已生成订单 */
    public static final String FAIL1006 = "-1006";
    
    /** 请求失败-超过最大预定数量 */
    public static final String FAIL1007 = "-1007";
    
    /** 请求失败-已经预定过 */
    public static final String FAIL1008 = "-1008";
    
    /** 请求失败-已经预定过 */
    public static final String FAIL1009 = "-1009";
    
    /** 请求失败-单个商品超过最大预定数量 */
    public static final String FAIL1010 = "-1010";

	/** 内购-活动结束 */
    public static final String FAIL1011 = "-1011";

	/** 内购-后台-存在客户订单 */
	public static final String FAIL1012 = "-1012";


    /** 应答码 **/
    public static final Map<String, String> responseMap = new HashMap<String, String>();
    static {
        responseMap.put(SUCC, "请求成功");
        responseMap.put(FAIL1001, "请求失败-参数值为空");
        responseMap.put(FAIL1002, "请求失败-参数有误");
        responseMap.put(FAIL1003, "请求失败-数据不存在");
        responseMap.put(FAIL1004, "请求失败-用户不存在");
        responseMap.put(FAIL1005, "请求失败-可选类型重复");
        responseMap.put(FAIL1007, "请求失败-超过最大预定数量");
        responseMap.put(FAIL1009, "请求失败-内购活动已结束");
        responseMap.put(FAIL1010, "请求失败-单个商品超过最大预定数量");
        responseMap.put(FAIL1011, "活动结束");
		responseMap.put(FAIL1012, "存在客户订单");
    }
}