/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 常用日期处理工具类
 * 
 * @author ZHOUKQ
 *
 */
public class GetCalendarUtils {

    static String pDateTime = "yyyy-MM-dd HH:mm:ss";

    static String pDate = "yyyy-MM-dd";

    static String pTime = "HH:mm:ss";

    static SimpleDateFormat sdf = new SimpleDateFormat();

    /**
     * 获取当前时间 格式："yyyy-MM-dd HH:mm:ss"
     * 
     * @return
     */
    public static String getDateTime() {
        Date date = (Date) Calendar.getInstance().getTime();
        sdf.applyPattern(pDateTime);
        return sdf.format(date);
    }

    /**
     * 获取当前时间 格式： "yyyy-MM-dd"
     * 
     * @return
     */
    public static String getDate() {
        Date date = (Date) Calendar.getInstance().getTime();
        sdf.applyPattern(pDate);
        return sdf.format(date);
    }

    /**
     * 获取当前时间 格式："HH:mm:ss"
     * 
     * @return
     */
    public static String getTime() {
        Date date = (Date) Calendar.getInstance().getTime();
        sdf.applyPattern(pTime);
        return sdf.format(date);
    }

    /**
     * 将时间转换格式："yyyy-MM-dd HH:mm:ss"
     * 
     * @return
     */
    public static String getDateTime(Date date) {
        if (date == null)
            return "";
        sdf.applyPattern(pDateTime);
        return sdf.format(date);
    }

    /**
     * 将时间转换格式："yyyy-MM-dd"
     * 
     * @return
     */
    public static String getDate(Date date) {
        if (date == null)
            return "";
        sdf.applyPattern(pDate);
        return sdf.format(date);
    }

    /**
     * 将时间转换格式："HH:mm:ss"
     * 
     * @return
     */
    public static String getTime(Date date) {
        if (date == null)
            return "";
        sdf.applyPattern(pTime);
        return sdf.format(date);
    }

    /**
     * 将字符串日期时间"yyyy-MM-dd "转换Timestamp
     * 
     * @return
     */
    public static Timestamp getDateTime(String date) {
        sdf.applyPattern(pDateTime);
        try {
            return (Timestamp) Timestamp.valueOf(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将字符串日期时间"yyyy-MM-dd HH:mm:ss "转换DATE
     * 
     * @return
     */
    public static Date getNowDateTime(String date) {
        sdf.applyPattern(pDateTime);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 将字符串日期时间"yyyy-MM-dd "转换DATE
     * 
     * @return
     */
    public static Date getDate(String date) {
        sdf.applyPattern(pDate);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取自定义格式的当前时间
     * 
     * @param pattern
     * @return
     */
    public static String getCalendar(String pattern) {
        Date date = (Date) Calendar.getInstance().getTime();
        sdf.applyPattern(pattern);
        return sdf.format(date);
    }

    /**
     * 获取自定义格式的时间
     * 
     * @param pattern
     * @return
     */
    public static String getCalendar(Date date, String pattern) {
        sdf.applyPattern(pattern);
        return sdf.format(date);
    }

    public static String getChangeDateFormat(String startTime) {
        if (null == startTime || "".equals(startTime.trim())) {
            return "";
        }
        String year = startTime.split(" ")[0].split("-")[0];
        String month = startTime.split(" ")[0].split("-")[1];
        String day = startTime.split(" ")[0].split("-")[2];
        String hour = startTime.split(" ")[1].split(":")[0];
        String minute = startTime.split(" ")[1].split(":")[1];
        String second = startTime.split(" ")[1].split(":")[2];
        year = year.length() == 1 ? "0" + "" + year : year;
        month = month.length() == 1 ? "0" + "" + month : month;
        day = day.length() == 1 ? "0" + "" + day : day;
        hour = hour.length() == 1 ? "0" + "" + hour : hour;
        minute = minute.length() == 1 ? "0" + "" + minute : minute;
        second = second.length() == 1 ? "0" + "" + second : second;
        return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
    }

}
