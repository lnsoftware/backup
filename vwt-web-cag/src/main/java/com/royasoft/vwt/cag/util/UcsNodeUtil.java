/************************************************
 *  Copyright © 2002-2016 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * nodeId生成工具
 *
 * @Author:daizl
 */
public class UcsNodeUtil {

    private static volatile Long serverObjectId = -1l;

    public static short objectSequenceId = 1;
    private static long orderNum = 0l;
    private static String date;

    public static long getNodeId() {
        if (serverObjectId == -1)
            serverObjectId = 0l;

        long sid = serverObjectId << 56;
        long t = (System.currentTimeMillis() << 16);
        long id = (sid | t | (objectSequenceId++ & 0xffff));
        return id;
    }

    /**
     * 生成订单编号
     * 
     * @return
     */
    public static synchronized String getOrderNo() {
        String str = new SimpleDateFormat("yyMMddHHmm").format(new Date());
        if (date == null || !date.equals(str)) {
            date = str;
            orderNum = 0l;
        }
        orderNum++;
        long orderNo = Long.parseLong((date)) * 10000;
        orderNo += orderNum;
        ;
        return orderNo + "";
    }
}
