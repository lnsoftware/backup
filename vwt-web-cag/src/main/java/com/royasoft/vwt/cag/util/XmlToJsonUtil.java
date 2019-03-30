package com.royasoft.vwt.cag.util;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlToJsonUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(XmlToJsonUtil.class);

    public static String  getOAJSON(String url, Map<String, String> map) {
        HttpClient client = new HttpClient();// 创建http
        PostMethod httpMethod = new PostMethod(url);// 创建post
        for (Map.Entry<String, String> entry : map.entrySet()) {
            httpMethod.setParameter(entry.getKey(), entry.getValue());
        }
        try {
            logger.debug("调用接口开始时间",new Date());
            client.executeMethod(httpMethod);// http执行post方法,发送post请求
            String result = httpMethod.getResponseBodyAsString();
            logger.debug("调用接口结束时间",new Date());
            logger.debug("OA返回值，result{}",result);
            return result;
        } catch (IOException e) {
            logger.error("获取OA信息异常");
        }
        return "";
    }

    /**
     * 将xml字符串转换为JSON对象
     * 
     * @param xmlFile xml字符串
     * @return JSON对象
     */
    public static JSON getJSONFromXml(String xmlString) {
        XMLSerializer xmlSerializer = new XMLSerializer();
        JSON json = xmlSerializer.read(xmlString);
        return json;
    }

}
