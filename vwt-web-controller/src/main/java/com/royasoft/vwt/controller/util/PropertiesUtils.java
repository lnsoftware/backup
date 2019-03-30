package com.royasoft.vwt.controller.util;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 读取properties文件类
 */
public class PropertiesUtils {
    private static final Logger Log = LoggerFactory.getLogger(PropertiesUtils.class);

    /**
     * 获取属性文件的数据 根据key获取值
     * 
     * @param key
     * @return
     */
    public static String findPropertiesKey(String key, String defaultValue) {

        try {
            Properties prop = getProperties();
            return prop.getProperty(key, defaultValue);
        } catch (Exception e) {
            Log.error("获取属性失败", e);
            return "";
        }

    }

    /**
     * 返回　Properties
     * 
     * @return
     */
    public static Properties getProperties() {
        Properties prop = new Properties();
        try {
            InputStream in = PropertiesUtils.class.getResourceAsStream("/application.properties");
            prop.load(in);
        } catch (Exception e) {
            Log.error("加载properties文件失败", e);
            return null;
        }
        return prop;
    }
}
