package com.royasoft.vwt.cag.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;

/**
 * HTTP工具箱
 * 
 * @author
 */
@Component
public final class IMSUtil {
    private static final Logger logger = LoggerFactory.getLogger(IMSUtil.class);

    public String registeOrcancelIMS(String telnum, String cmd) {
        DataOutputStream out = null;
        DataInputStream in = null;
        try {
            String content = "bossCmd&userName=" + telnum + "&passWord=" + ParamConfig.ims_pwd + "&cmd=" + cmd + "&verifyCode=" + ParamConfig.ims_verifycode + "&userAgent=" + ParamConfig.ims_useagent;
            URL url = new URL(ParamConfig.ims_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6 * 1000);
            conn.setReadTimeout(7 * 1000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", "Profile/MIDP-1.0 Configuration/CLDC-1.0");
            conn.setRequestProperty("Content-Language", "utf-8");
            conn.setRequestProperty("Accept", "application/octet-stream");
            conn.setRequestProperty("Connection", "close");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new DataOutputStream(conn.getOutputStream());
            out.writeUTF(content);
            in = new DataInputStream(conn.getInputStream());
            String urlStr = in.readUTF();
            return urlStr;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error("注册/注销 IMS 关闭IO流异常");
            }
        }

        return null;
    }

}