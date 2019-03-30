package com.royasoft.vwt.cag.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.util.mq.Base64;
import com.royasoft.vwt.common.security.MD5;

@SuppressWarnings("deprecation")
@Component
public class VoiceCodeUtil {

    private static final Logger logger = LoggerFactory.getLogger(VoiceCodeUtil.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 封装请求体并请求语音验证码
     * 
     * @return
     * @Description:
     */
    public boolean requestVoice(String verifyCode, String telNum) {
        logger.debug("封装请求体并请求语音验证码,verifyCode:{},telNum:{}", verifyCode, telNum);
        String accountId = ParamConfig.voice_account_id;
        String sign = createSign();
        String appId = ParamConfig.voice_app_id;
        String callId = getRandomStr();
        String displayNum = ParamConfig.voice_display_num;
        String url = ParamConfig.voice_url;
        logger.debug("封装请求体并请求语音验证码,accountId:{},sign:{},appId:{},callId:{},displayNum:{},url:{}", accountId, sign, appId, callId, displayNum, url);
        if (!StringUtils.checkParamNull(accountId, sign, appId, callId, displayNum, url)) {
            logger.error("封装请求体并请求语音验证码,参数有误,accountId:{},sign:{},appId:{},callId:{},displayNum:{},url:{}", accountId, sign, appId, callId, displayNum, url);
            return false;
        }
        JSONObject json = new JSONObject();
        json.put("accountId", accountId);
        json.put("sign", sign);
        json.put("appId", appId);
        json.put("callId", callId);
        json.put("captchaCode", verifyCode);
        json.put("playTimes", "2");
        json.put("to", telNum);
        json.put("displayNum", displayNum);
        json.put("respUrl", "");
        try {
            String res = postToVoice(url, json);
            logger.debug("封装请求体并请求语音验证码(返回值)", res);
            if (null == res || "".equals(res))
                return false;
            JSONObject resJson = JSONObject.parseObject(res);
            String respCode = resJson.getString("respCode");
            if (null == respCode || "".equals(respCode) || !respCode.equals("00000"))
                return false;
            return true;
        } catch (Exception e) {
            logger.error("封装请求体并请求语音验证码异常", e);
            return false;
        }
    }

    /**
     * 语音验证码http请求
     * 
     * @param url
     * @param json
     * @return
     * @throws Exception
     * @Description:
     */
    public String postToVoice(String url, JSONObject json) throws Exception {
        logger.debug("语音验证码http请求,url:{},json:{}", url, null == json ? "为空" : json.toJSONString());
        String Authorization = createAuthorization();
        if (null == Authorization || "".equals(Authorization)) {
            logger.error("语音验证码http请求,Authorization有误,Authorization:{}", Authorization);
            return "-10001";
        }
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json;charset=utf-8");
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Authorization", Authorization);
        StringEntity se = new StringEntity(json.toString());
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(se);
        // 返回服务器响应
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String responseString = null;
        try {
            if (response.getEntity() != null) {
                responseString = EntityUtils.toString(response.getEntity()); // 返回服务器响应的HTML代码
            }
        } finally {
            if (entity != null)
                entity.consumeContent(); // release connection gracefully
            if (null != httpClient)
                httpClient.close();
        }
        return responseString;
    }

    /**
     * 获取16为随机数字字符串
     * 
     * @return
     * @Description:
     */
    private String getRandomStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(1 + Math.random() * 10);
        return sb.toString();
    }

    /**
     * 生成用户验证参数
     * 
     * @return
     * @Description:
     */
    private String createSign() {
        String str = ParamConfig.voice_account_id + ParamConfig.voice_token + DATE_FORMAT.format(new Date());
        try {
            return MD5.encodeMD5(str).toUpperCase();
        } catch (Exception e) {
            logger.error("生成用户验证参数失败", e);
            return null;
        }
    }

    /**
     * 生成Authorization验证信息
     * 
     * @return
     * @Description:
     */
    private String createAuthorization() {
        String str = ParamConfig.voice_account_id + ":" + DATE_FORMAT.format(new Date());
        return Base64.encodeBytes(str.getBytes());
    }
}
