/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util.upload;

import java.io.File;
import java.net.InetSocketAddress;

import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerGroup;
import org.csource.fastdfs.TrackerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 分布式文件系统接口
 * 
 * @author qinp
 */
@Component
public class FastDFSUtil {

    private static final Logger logger = LoggerFactory.getLogger(FastDFSUtil.class);

    public static TrackerServer trackerServer = null;

    public static FastDFSConnectionPoolFactory factory = null;
    
    
    public static void init(String trackerAddr) {
        try {
            ClientGlobal.g_connect_timeout = 40000;
            ClientGlobal.g_network_timeout = 60000;
            ClientGlobal.g_charset = "UTF-8";
            String[] parts = trackerAddr.split(":");
            InetSocketAddress[] tracker_servers = new InetSocketAddress[1];
            tracker_servers[0] = new InetSocketAddress(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            ClientGlobal.g_tracker_group = new TrackerGroup(tracker_servers);
            TrackerClient tracker = new TrackerClient();
            Config config = new Config();
            config.maxActive = Runtime.getRuntime().availableProcessors();
            config.maxWait = 30000;
            config.testOnBorrow = false;
            config.testOnReturn = false;
            factory = new FastDFSConnectionPoolFactory(config, tracker);
            logger.debug("初始FastDFS成功....");
        } catch (Exception e) {
            logger.error("初始化FastDFS异常", e);
        }
    }
    
    public static byte[] getFileBytesFromDFS(String fileAddr) {
        StorageClient storageClient=null;
        try {
            storageClient = factory.getConnection();
            String[] strs = fileAddr.split(",");
            logger.debug("从fastDFS获取文件信息,组名为{},uuid{}", strs[0], strs[1]);
            byte[] b = storageClient.download_file(strs[0], strs[1]);
            return b;
        } catch (Exception e) {
            logger.error("从fastDFS获取文件信息异常", e);
            return null;
        }
        finally{
            if(storageClient!=null)
                factory.releaseConnection(storageClient);
        }
    }

    /**
     * 上传文件至文件服务器
     * 
     * @param localFile
     * @return
     */
    public static String uploadFile(byte[] localFile, String fileName) {
        logger.debug("上传文件至文件服务器,fileName:{},localFile:{}", fileName, localFile.length);
        String fileServerPath = "";
        StorageClient storageClient=null;
        try {
            storageClient = factory.getConnection();
            NameValuePair nvp[] = new NameValuePair[] {};
            String fileIds[] = storageClient.upload_file(localFile, fileName, nvp);
            logger.debug("上传文件至文件服务器,group:{},path:{}", fileIds[0], fileIds[1]);
            fileServerPath = File.separator + fileIds[0] + File.separator + fileIds[1];
            logger.debug("上传文件至文件服务器,fileServerPath:{}", fileServerPath);
            return  fileServerPath;
        } catch (Exception e) {
            logger.error("上传文件至文件服务器异常,fileName:{},localFile:{}", fileName, localFile.length, e);
            return "";
        }finally{
            if(storageClient!=null)
                factory.releaseConnection(storageClient);
        }
    }

    /**
     * 上传文件至文件服务器
     * 
     * @param localFile
     * @return
     */
    public static String uploadFile(String localPath) {
        logger.debug("上传文件至文件服务器,localPath:{}", localPath);
        String fileServerPath = "";
        StorageClient storageClient=null;
        try {
            NameValuePair nvp[] = new NameValuePair[] {};
            storageClient = factory.getConnection();
            String fileIds[] = storageClient.upload_file(localPath, localPath.substring(localPath.lastIndexOf(".") + 1), nvp);
            logger.debug("上传文件至文件服务器,group:{},path:{}", fileIds[0], fileIds[1]);
            fileServerPath = "/" + fileIds[0] +"/"+ fileIds[1];
            logger.debug("上传文件至文件服务器,fileServerPath:{}", fileServerPath);
            return fileServerPath;
        } catch (Exception e) {
            logger.error("上传文件至文件服务器异常,localPath:{}", localPath, e);
            return "";
        }finally{
            if(storageClient!=null)
                factory.releaseConnection(storageClient);
        }
    }
}
