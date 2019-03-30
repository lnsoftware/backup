package com.royasoft.vwt.cag.util.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.util.ImgCompress;
import com.royasoft.vwt.cag.util.ZipUtil;

/**
 * 工作圈文件上传
 *
 * @Author:MB
 * @Since:2016年3月8日
 */
public class WorkTeamFileUtil {

    private final static Logger logger = LoggerFactory.getLogger(WorkTeamFileUtil.class);

    /**
     * 上传zip
     * 
     * @param msg
     * @Description:
     */
    public JSONObject uploadZipByBytes(byte[] bytes, String fileName) {
        logger.debug("enter  uploadZipByBytes zip 文件");
        if (bytes == null || fileName == null) {
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = FileUploadUtil.wirteFileToLocal(bytes, fileName);
            logger.debug("上传zip,jsonObject:{}", jsonObject);
            if (jsonObject == null) {
                return null;
            }
            String path = jsonObject.getString("path");
            String name = jsonObject.getString("name");
            Map<String, List<String>> map = ZipUtil.unFilezip(path + File.separator + name,  path);
            logger.debug("上传zip,map:{}", JSON.toJSONString(map));
            if (null == map) {
                return null;
            }
            List<String> lstFile = map.get("picPathList");
            Collections.sort(lstFile);

            List<String> listFile = map.get("filePathList");
            Collections.sort(listFile);

            String image = savePIC(lstFile);
            List<Map<String, Object>> list = saveFile(listFile);
            JSONObject objectRes = new JSONObject();
            objectRes.put("image", image);
            objectRes.put("files", list);
            logger.debug("上传zip,objectRes:{}", objectRes.toJSONString());
            clearFiles(path);
            return objectRes;
        } catch (Exception e) {
            logger.error("上传zip异常", e);
        }
        return null;
    }
    /**
     * 上传zip
     * 
     * @param msg
     * @Description:
     */
    public JSONObject uploadZIP(Object msg) {
        logger.debug("上传zip");
        JSONObject jsonObject = null;
        try {
            jsonObject = FileUploadUtil.uploadFileToLocal(msg);
            logger.debug("上传zip,jsonObject:{}", jsonObject);

            if (jsonObject == null)
                return null;
            String path = jsonObject.getString("path");
            String name = jsonObject.getString("name");
            Map<String, List<String>> map = ZipUtil.unFilezip(path + File.separator + name, path);
            logger.debug("上传zip,map:{}", JSON.toJSONString(map));
            if (null == map)
                return null;
            List<String> lstFile = map.get("picPathList");
            Collections.sort(lstFile);

            List<String> listFile = map.get("filePathList");
            Collections.sort(listFile);

            String image = savePIC(lstFile);
            List<Map<String, Object>> list = saveFile(listFile);
            JSONObject objectRes = new JSONObject();
            objectRes.put("image", image);
            objectRes.put("files", list);
            logger.debug("上传zip,objectRes:{}", objectRes.toJSONString());
            clearFiles(path);
            return objectRes;
        } catch (Exception e) {
            logger.error("上传zip异常", e);
        }
        return null;
    }

    private String savePIC(List<String> listFile) {
        String allPath = "";
        if (null == listFile || listFile.isEmpty())
            return null;
        for (String string : listFile) {
            int lastPoint = string.lastIndexOf(".");
            String smallPath = string.substring(0, lastPoint) + "small" + string.substring(lastPoint);
            String serverSmallPath = "";
            ImgCompress compress = new ImgCompress();
            if (compress.compressPic(string, smallPath))
                serverSmallPath = FastDFSUtil.uploadFile(smallPath);
            String serverPath = FastDFSUtil.uploadFile(string);
            if (!com.alibaba.dubbo.common.utils.StringUtils.isEmpty(serverSmallPath) && !com.alibaba.dubbo.common.utils.StringUtils.isEmpty(serverPath)) {
                String fullPath = serverSmallPath + "," + serverPath;
                allPath += fullPath + ";";
            }
        }
        logger.debug("保存图片,allPath:{}", allPath);
        if(!com.alibaba.dubbo.common.utils.StringUtils.isEmpty(allPath))
            allPath = allPath.substring(0, allPath.length() - 1);
        return allPath;
    }

    private List<Map<String, Object>> saveFile(List<String> listFile) {
        if (null == listFile || listFile.isEmpty())
            return null;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (String obj : listFile) {
            Map<String, Object> map = new HashMap<String, Object>();
            String filePath = String.valueOf(obj).substring(0, String.valueOf(obj).indexOf("*"));
            String oldName = String.valueOf(obj).substring(String.valueOf(obj).indexOf("*") + 1, String.valueOf(obj).length());
            File file = new File(filePath);
            map.put("fileLength", file.length());
            String srcFilePath = FastDFSUtil.uploadFile(filePath);
            map.put("filePaths", srcFilePath);
            map.put("fileName", oldName);
            list.add(map);
        }
        logger.debug("保存文件,list:{}", JSON.toJSONString(list));
        return list;
    }

    // 删除文件和目录
    private void clearFiles(String workspaceRootPath) {
        File file = new File(workspaceRootPath);
        if (file.exists()) {
            deleteFile(file);
        }
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
        }
        file.delete();
    }

}
