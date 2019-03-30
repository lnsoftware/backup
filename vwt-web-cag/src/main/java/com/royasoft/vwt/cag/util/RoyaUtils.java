/************************************************
 *  Copyright ? 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * royal系统工具类
 * 
 * @author jxue
 * 
 */
public class RoyaUtils {

    private static final Logger logger = LoggerFactory.getLogger(RoyaUtils.class);

    /**
     * 获取程序运行路径
     * 
     * @param <T>
     * 
     * @return 程序当前绝对路径
     * @throws UnsupportedEncodingException
     */
    public static <T> String getLaunchPath(Class<T> clazz) {
        String filePath = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
        File file = new File(filePath);
        String parentPath = "";
        try {
            parentPath = URLDecoder.decode(file.getParentFile().getAbsolutePath(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("获取程序运行路径异常:{}", e);
        }

        return parentPath;
    }

    /**
     * 获取properties资源信息
     * 
     * @param properties_path
     * @param property
     * @return
     */
    public static String getPropertyByProperties(String properties_path, String property) {
        Properties props = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(properties_path);
            props.load(in);
            return props.getProperty(property);
        } catch (IOException e) {
            logger.error("获取properties资源信息异常:{}", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

}
