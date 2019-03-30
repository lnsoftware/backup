package com.royasoft.vwt.controller.util.upload;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.controller.server.HttpServer;

/**
 * 文件上传
 *
 * @Author:MB
 * @Since:2016年3月5日
 */
@Component
public class FileUploadUtil {

    private final static Logger logger = LoggerFactory.getLogger(FileUploadUtil.class);

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private static HttpPostRequestDecoder decoder;

    /**
     * 上传文件
     * 
     * @param msg
     * @return
     * @throws Exception
     * @Description:
     */
    public static String uploadFile(Object msg) throws Exception {
        logger.debug("上传文件,msg:{}", msg);
        HttpRequest request = (HttpRequest) msg;

        decoder = new HttpPostRequestDecoder(factory, request);

        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            if (request.getMethod() == HttpMethod.POST) {
                if (decoder != null) {
                    decoder.cleanFiles();
                    decoder = null;
                }
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (Exception e) {
                    logger.error("上传文件,msg:{}", msg, e);
                    return null;
                }
            }
        }

        Map<String, Object> resMap = null;
        if (decoder != null && msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;
            try {
                decoder.offer(chunk);
            } catch (Exception e) {
                logger.error("读取字节数组异常", e);
                return null;
            }
            resMap = readHttpDataChunkByChunk();
        }
        if (null == resMap)
            return "";
        logger.debug("上传文件,msg:{},resByte:{},fileName:{}", msg, ((byte[]) resMap.get("fileBytes")).length, (String) resMap.get("fileName"));
        if (null == (String)resMap.get("fileName") || "".equals((String)resMap.get("fileName")))
            return "";
        String extName = ((String)resMap.get("fileName")).substring(((String)resMap.get("fileName")).lastIndexOf(".") + 1);
        return FastDFSUtil.uploadFile(((byte[]) resMap.get("fileBytes")), extName);
    }
    
    
    /**
     * 上传文件
     * 
     * @param msg
     * @return
     * @throws Exception
     * @Description:
     */
    public static Map<String,Object> uploadFile1(Object msg) throws Exception {
        logger.debug("上传文件,msg:{}", msg);
        HttpRequest request = (HttpRequest) msg;

        decoder = new HttpPostRequestDecoder(factory, request);

        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            if (request.getMethod() == HttpMethod.POST) {
                if (decoder != null) {
                    decoder.cleanFiles();
                    decoder = null;
                }
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (Exception e) {
                    logger.error("上传文件,msg:{}", msg, e);
                    return null;
                }
            }
        }

        Map<String, Object> resMap = null;
        if (decoder != null && msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;
            try {
                decoder.offer(chunk);
            } catch (Exception e) {
                logger.error("读取字节数组异常", e);
                return null;
            }
            resMap = readHttpDataChunkByChunk();
        }
        Map<String,Object>map = new HashMap<>();
        if (null == resMap)
            return map;
        logger.debug("上传文件,msg:{},resByte:{},fileName:{}", msg, ((byte[]) resMap.get("fileBytes")).length, (String) resMap.get("fileName"));
        if (null == (String)resMap.get("fileName") || "".equals((String)resMap.get("fileName")))
            return map;
        String extName = ((String)resMap.get("fileName")).substring(((String)resMap.get("fileName")).lastIndexOf(".") + 1);

        map.put("path", FastDFSUtil.uploadFile(((byte[]) resMap.get("fileBytes")), extName));
        map.put("size", ((byte[])resMap.get("fileBytes")).length);
        return map;
    }    

    /**
     * 上传文件(提供给任务)
     * 
     * @param msg
     * @return JSONObject {fileName:.., pathUrl:.., length:..}
     * @throws Exception
     * @Description:
     */
    public static JSONObject uploadFileForSendTask(Object msg) throws Exception {
        logger.debug("上传文件,msg:{}", msg);
        HttpRequest request = (HttpRequest) msg;
        JSONObject json = new JSONObject();
        decoder = new HttpPostRequestDecoder(factory, request);

        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            if (request.getMethod() == HttpMethod.POST) {
                if (decoder != null) {
                    decoder.cleanFiles();
                    decoder = null;
                }
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (Exception e) {
                    logger.error("上传文件,msg:{}", msg, e);
                    return null;
                }
            }
        }
        String fileName="";
        byte[] fileBytes=null;
        Map<String, Object> resMap = null;
        if (decoder != null && msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;
            try {
                decoder.offer(chunk);
            } catch (Exception e) {
                logger.error("读取字节数组异常", e);
                return null;
            }
            resMap = readHttpDataChunkByChunk();
            if (null == resMap) {
                return null;
            }
            fileName=(String) resMap.get("fileName");
            fileBytes=(byte[]) resMap.get("fileBytes");
            if (fileBytes.length > 1048576 * 5) {
                json.put("length", fileBytes.length);
                return json;
            }
        }
        logger.debug("上传文件,msg:{},resByte:{},fileName:{}", msg, fileBytes.length, fileName);
        if (null == fileName || "".equals(fileName))
            return null;
        String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
        json.put("fileName", fileName);
        json.put("pathUrl", FastDFSUtil.uploadFile(fileBytes, extName));
        json.put("length", fileBytes.length);
        return json;
    }

    /**
     * 上传文件至本地
     * 
     * @param msg
     * @return
     * @throws Exception
     * @Description:
     */
    public static JSONObject uploadFileToLocal(Object msg) throws Exception {
        logger.debug("上传文件至本地,msg:{}", msg);
        HttpRequest request = (HttpRequest) msg;

        decoder = new HttpPostRequestDecoder(factory, request);
        logger.debug("上传文件至本地,decoder:{}", decoder);
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            if (request.getMethod() == HttpMethod.POST) {
                if (decoder != null) {
                    decoder.cleanFiles();
                    decoder = null;
                }
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (Exception e) {
                    logger.error("上传文件至本地,msg:{}", msg, e);
                    return null;
                }
            }
        }
        logger.debug("上传文件至本地读取");
        Map<String, Object> resMap = null;
        if (decoder != null && msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;
            try {
                decoder.offer(chunk);
            } catch (Exception e) {
                logger.error("读取字节数组异常", e);
                return null;
            }
            resMap = readHttpDataChunkByChunk();
            logger.debug("上传文件至本地读取");
            if (null == resMap) {
                return null;
            }
        }
        String fileName=(String) resMap.get("fileName");
        byte[] fileBytes=(byte[]) resMap.get("fileBytes");
        logger.debug("上传文件至本地,msg:{},resByte:{},fileName:{}", msg, fileBytes == null ? 0 : fileBytes.length, fileName);
        if (null == fileName || "".equals(fileName))
            return null;
        String extName = fileName.substring(fileName.lastIndexOf("."));
        String launchPath = getLaunchPath();
        String uuid = UUID.randomUUID().toString();
        String filePath = launchPath + File.separator + uuid;
        getFile(fileBytes, filePath, uuid + extName);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("path", filePath);
        jsonObject.put("name", uuid + extName);
        return jsonObject;
    }

    /**
     * 根据byte数组，生成文件
     */
    public static void getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            logger.error("写文件异常", e.getMessage());
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    logger.error("Buffer流关闭异常", e1.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    logger.error("文件流关闭异常", e1.getMessage());
                }
            }
        }
    }

    /**
     * 读取http请求数据
     * 
     * @return
     * @throws Exception
     * @Description:
     */
    private static Map<String, Object> readHttpDataChunkByChunk() throws Exception {
        Map<String, Object> resMap = null;
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    try {
                        resMap = writeHttpData(data);
                    } finally {
                        data.release();
                    }
                }
            }
        } catch (Exception e) {
        }
        logger.debug("读取http请求数据,resByte:{}", resMap == null);
        return resMap;
    }

    /**
     * 从http请求获取文件数据
     * 
     * @param data
     * @return
     * @throws IOException
     * @Description:
     */
    private static Map<String, Object> writeHttpData(InterfaceHttpData data) throws IOException {
        if (data.getHttpDataType() == HttpDataType.FileUpload) {
            FileUpload fileUpload = (FileUpload) data;
            if (fileUpload.isCompleted()) {
                String fileName = fileUpload.getFilename();
                Map<String, Object> resMap = new HashMap<String, Object>();
                resMap.put("fileName", fileName);
                resMap.put("fileBytes", fileUpload.get());
                return resMap;
            }
        }
        return null;
    }

    /**
     * 获取程序运行路径
     * 
     * @return 程序当前绝对路径
     * @throws UnsupportedEncodingException
     */
    public static String getLaunchPath() throws Exception {
        String filePath = HttpServer.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        File file = new File(filePath);
        String parentPath = URLDecoder.decode(file.getParentFile().getAbsolutePath(), "utf-8");
        return parentPath;
    }

}
