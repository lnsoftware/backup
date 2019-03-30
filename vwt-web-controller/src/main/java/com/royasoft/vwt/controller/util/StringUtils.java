/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.alibaba.fastjson.JSONObject;

/**
 * 常用字符串处理工具类
 * 
 * @author ZHOUKQ
 *
 */
public class StringUtils {
    /**
     * 对手机号码排序
     * 
     * @param strs
     * @return
     */
    public static String sortNum(String strs) {
        String r1 = strs.replaceAll(";;", ";");
        String[] r2 = r1.split(";");
        List<String> lx = new ArrayList<String>();
        for (String string : r2) {
            lx.add(string);
        }
        Collections.sort(lx);
        String r3 = "";
        for (String string : lx) {
            r3 = r3 + string + ";";
        }
        return r3;
    }

    /**
     * 数组去掉空值成员
     * 
     * @param str
     * @return
     */
    public static String[] filterSpace(String[] str) {
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < str.length && str.length > 0; i++) {
            if (str[i] == null || "".equals(str[i].trim().toString())) {
                continue;
            } else {
                list.add(str[i]);
            }
        }
        String[] newArray = new String[list.size()];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = list.get(i);
        }
        return newArray;
    }

    /**
     * 处理字符串前后含有特殊字符的处理（截取）
     * 
     * @param str 字符串
     * @param split 分隔符
     * @return
     */
    public static String moveSplit(String str, String split) {
        if (str.startsWith(split)) {
            str = str.substring(1, str.length());
        }
        if (str.endsWith(split)) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * 判断List是否为空
     * 
     * @param list
     * @return 不为空-true
     */
    public static Boolean arrayIsNotNull(List<Object> list) {
        Boolean boo = false;
        if (null != list && !list.isEmpty()) {
            boo = true;
        }
        return boo;
    }

    /**
     * 判断字符串是否为空
     * 
     * @param string
     * @return 不为空-true，为空-false
     * 
     */
    public static Boolean stringIsNotNull(String string) {
        Boolean boo = false;
        if (null != string && !"".equals(string)) {
            boo = true;
        }
        return boo;
    }

    /**
     * 判断Map<String,Object>是否为空
     * 
     * @param string
     * @return 不为空-true，为空-false
     * 
     */
    public static Boolean mapSOIsNotNull(Map<String, Object> map) {
        Boolean boo = false;
        if (null != map && !map.isEmpty()) {
            boo = true;
        }
        return boo;
    }

    /**
     * 转换JsonStr
     * 
     * @param jsonStr
     * @return JSONObject
     */
    public static JSONObject strisJsonStr(String jsonStr) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            return jsonObject;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断字符串是否为空并赋予默认值
     * 
     * @param string
     * @return 为空则赋予值
     * 
     */
    public static String strIsNullAndGetValue(String str, String value) {
        if (null == str || "".equals(str)) {
            str = value;
        }
        return str;
    }

    /**
     * 字符串过滤其他字符串
     * 
     * @param beforeString
     * @param removeString
     * @return
     */
    public static String stringRemove(String beforeString, String removeString) {
        String[] beforeStrings = beforeString.split(";");
        String[] removeStrings = removeString.split(";");
        List<String> strings = new ArrayList<String>();
        String afterString = "";
        for (String string : beforeStrings) {
            strings.add(string);
        }
        for (String string : removeStrings) {
            if (strings.contains(string)) {
                strings.remove(string);
            }
        }
        for (String string : strings) {
            afterString = afterString + ";" + string;
        }

        return StringUtils.moveSplit(afterString, ";");
    }

    /**
     * 判断手机号码是否是某一号段
     * 
     * @param telNum
     * @return true 是某一号段；false 不是某一号段
     */
    public static boolean checkMobileNumber(String telNum, String patternStr) {
        Pattern pattern = Pattern.compile(patternStr);
        // "^1(3[4-9]|4[7]|5[012789]|[08]|8[23478])\\d{8}$"
        Matcher matcher = pattern.matcher(telNum);
        boolean b = matcher.matches();
        // 当条件满足时，将返回true，否则返回false
        return b;
    }

    /**
     * 过滤List重复元素
     * 
     * @return
     */
    public static List<String> stringFilterRepeat(List<String> listBefore) {
        List<String> list2 = new ArrayList<String>();
        if (null != listBefore && !listBefore.isEmpty()) {
            for (int i = 0; i < listBefore.size(); i++) {
                if (!list2.contains(listBefore.get(i)))
                    list2.add(listBefore.get(i));
            }
        }
        return list2;
    }

    /**
     * 将字符串转为List，按照指定分隔符
     * 
     * @param str
     * @param split 分隔符
     * @return
     */
    public static List<String> transObjectToList(String str, String split) {
        List<String> strings = new ArrayList<String>();
        if (stringIsNotNull(str)) {
            String[] strArray = str.split(split);
            for (String string : strArray) {
                if (stringIsNotNull(string)) {
                    strings.add(string);
                }
            }
        }
        return strings;
    }

    /**
     * 替换字符串
     * 
     * @param oldStr
     * @param content
     * @return
     */
    public static String getRemoveStr(String str, String oldStr, String newStr) {
        if (str.equals(oldStr)) {
            return str.replace(oldStr, newStr);
        } else if (str.endsWith(oldStr)) {
            return str.replace("," + oldStr, newStr);
        } else {
            return str.replace(oldStr + ",", newStr);
        }
    }

    /**
     * 校验参数是否为空
     * 
     * @param params
     * @return
     * @Description:
     */
    public static boolean checkParamNull(String... params) {
        for (String str : params) {
            if (null == str || "".equals(str))
                return false;
        }
        return true;
    }

    /**
     * 压缩
     * 
     * @param param
     * @return
     * @throws IOException
     * @Description:
     */
    public static byte[] compressByte(String param) throws IOException {
        if (param == null || param.length() == 0) {
            return null;
        }
        ByteArrayOutputStream byteOut = null;
        GZIPOutputStream gzipOut = null;
        byte[] outPut = null;
        try {
            // 开启数据输出流,关闭无效
            byteOut = new ByteArrayOutputStream();
            // 开启数据压缩流
            gzipOut = new GZIPOutputStream(byteOut);
            // 将字串转换成字节，然后按照ＵＴＦ－８的形式压缩
            gzipOut.write(param.getBytes("UTF-8"));
            // 压缩完毕
            gzipOut.finish();
            gzipOut.close();
            // 将压缩好的流转换到byte数组中去
            outPut = byteOut.toByteArray();
            byteOut.flush();
            byteOut.close();
        } finally {
            if (byteOut != null) {
                byteOut.close();
            }
        }
        return outPut;
    }

    /**
     * 解压
     * 
     * @param param
     * @return
     * @throws IOException
     * @Description:
     */
    public static String uncompressByte(byte[] param) throws IOException {
        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        GZIPInputStream gzip = null;
        byte[] b = null;
        try {
            // 创建输出流
            out = new ByteArrayOutputStream();
            // 创建输入流,并把传入的字串参数转码成ISO-8895-1
            in = new ByteArrayInputStream(param);
            // 创建压缩输入流，将大小默认为参数输入流大小
            gzip = new GZIPInputStream(in);
            // 创建byte数组用于接收解压后的流转化成byte数组
            byte[] byteArry = new byte[256];
            int n = -1;
            while ((n = gzip.read(byteArry)) != -1) {
                out.write(byteArry, 0, n);
            }
            // 转换数据
            b = out.toByteArray();
            out.flush();
        } finally {
            // 关闭压缩流资源
            if (out != null)
                out.close();
            if (gzip != null)
                gzip.close();
            if (in != null)
                in.close();
        }
        return new String(b, "UTF-8");
    }

    /**
     * 校验参数
     * 
     * @param param 参数
     * @param required 是否必选 true||false
     * @param maxlength 最大长度 -1时未没有长度限制
     * @return
     */
    public static boolean checkParam(String param, boolean required, int maxlength) {
        /** 必选 */
        if (required) {
            /** 实际参数为空 */
            if (org.springframework.util.StringUtils.isEmpty(param))
                return false;

            /** 长度超过限制 */
            if (-1 != maxlength && param.length() > maxlength)
                return false;
        } else {
            /** 长度超过限制 */
            if (!org.springframework.util.StringUtils.isEmpty(param) && -1 != maxlength && param.length() > maxlength)
                return false;
        }
        return true;
    }

    /**
     * 验证参数是否空
     * 
     * @param str
     * @return 空 true
     */
    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equalsIgnoreCase(str)) {
            return true;
        }
        return false;
    }
    
    public static String trimToNull(String str) {
        String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }
    
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }
}
