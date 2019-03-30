package com.royasoft.vwt.cag.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:ZIP 工具类，提供压缩和解压缩方法
 *
 * @Author:MB
 * @Since:2016年3月8日
 */
public class ZipUtil {
	
	 private final static Logger logger = LoggerFactory.getLogger(ZipUtil.class);

    /**
     * 解压文件
     * 
     * @param zipFileName ZIP文件全路径
     * @param outputDirectory 目标文件夹
     * @return 返回ZIP文件中包含子文件的全路径
     * @throws IOException
     */
    public static List<String> unzip(String zipFileName, String outputDirectory) throws IOException {
        List<String> filePathList = new ArrayList<String>();
        File directory = new File(zipFileName);
        if (!directory.getParentFile().exists()) {
            directory.getParentFile().mkdirs();
        }
        ZipFile zipFile = new ZipFile(directory);

        try {
            Enumeration<?> enumeration = zipFile.getEntries();
            ZipEntry zipEntry = null;
            // 循环输出文件
            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                String fileName = zipEntry.getName();
                File destFile = new File(outputDirectory + File.separator + fileName);
                File parent = new File(destFile.getParent());
                if (!parent.exists()) { // 判断目录是否存在，不存在则创建
                    parent.mkdirs();
                }
                InputStream is = null;
                FileOutputStream os = null;
                try {
                    is = zipFile.getInputStream(zipEntry);
                    os = new FileOutputStream(destFile);
                    int len = 0;
                    byte[] buf = new byte[1024];
                    // 写文件
                    while ((len = is.read(buf)) != -1) {
                        os.write(buf, 0, len);
                        os.flush();
                    }
                    filePathList.add(destFile.getPath());
                } finally {
                    // 关闭输入输出流
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                    if (os != null) {
                        os.close();
                        os = null;
                    }
                }
            }
        } finally {
            zipFile.close();
        }
        return filePathList;
    }

    /**
     * 解压文件
     * 
     * @param zipFileName ZIP文件全路径
     * @param outputDirectory 目标文件夹
     * @return 返回附件和图片的MAP
     * @throws IOException
     */
    public static Map<String, List<String>> unFilezip(String zipFileName, String outputDirectory) throws IOException {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> picPathList = new ArrayList<String>();
        List<String> filePathList = new ArrayList<String>();
        File directory = new File(zipFileName);
        if (!directory.getParentFile().exists()) {
            directory.getParentFile().mkdirs();
        }
        ZipFile zipFile = new ZipFile(directory);

        try {
            Enumeration<?> enumeration = zipFile.getEntries();
            ZipEntry zipEntry = null;
            // 循环输出文件
            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                String fileName = zipEntry.getName();
              //兼容pc ,c#
                if ("/".equals(fileName)) {
                    logger.info("丢弃" +zipEntry.getName());
                    continue;
                }
                String oldName = "";
                if (fileName.contains("attachment/")) {
                    oldName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
                    fileName = "attachment/" + System.currentTimeMillis() + fileName.substring(fileName.indexOf("."), fileName.length());
                }
                File destFile = new File(outputDirectory + File.separator + fileName);
                File parent = new File(destFile.getParent());
                if (!parent.exists()) { // 判断目录是否存在，不存在则创建
                    parent.mkdirs();
                }
                InputStream is = null;
                FileOutputStream os = null;
                try {
                    is = zipFile.getInputStream(zipEntry);
                    os = new FileOutputStream(destFile);
                    int len = 0;
                    byte[] buf = new byte[1024];
                    // 写文件
                    while ((len = is.read(buf)) != -1) {
                        os.write(buf, 0, len);
                        os.flush();
                    }
                    if (fileName.contains("attachment/")) {
                        filePathList.add(destFile.getPath() + "*" + oldName);
                    } else {
                        picPathList.add(destFile.getPath());
                    }

                } finally {
                    // 关闭输入输出流
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                    if (os != null) {
                        os.close();
                        os = null;
                    }
                }
            }
        } finally {
            zipFile.close();
            map.put("picPathList", picPathList);
            map.put("filePathList", filePathList);
        }
        return map;
    }

    /**
     * 将N个文件压缩为一个ZIP文件
     * 
     * @param zipFile 目标ZIP全路径
     * @param filePathList 需要压缩的文件的全路径
     * @throws IOException
     */

    public static void zip(File zipFile, List<String> filePathList) throws IOException {
        FileOutputStream fos = null;
        CheckedOutputStream cos = null;
        ZipOutputStream zos = null;
        try {
            // zipFile.createNewFile();
            // 创建ZIP文件输出流
            fos = new FileOutputStream(zipFile);
            cos = new CheckedOutputStream(fos, new CRC32());
            zos = new ZipOutputStream(cos);
            BufferedInputStream bis = null;
            // 循环压缩文件
            for (String filePath : filePathList) {
                try {
                    bis = new BufferedInputStream(new FileInputStream(filePath));
                    ZipEntry entry = new ZipEntry(new File(filePath).getName());
                    zos.putNextEntry(entry);
                    int count;
                    byte data[] = new byte[1024];
                    while ((count = bis.read(data, 0, 1024)) != -1) {
                        zos.write(data, 0, count);
                        zos.flush();
                    }
                } finally {
                    if (bis != null) {
                        bis.close();
                        bis = null;
                    }
                }
            }

        } finally {
            // 关闭ZIP输出流
            if (zos != null) {
                try {
                    zos.close();
                } catch (Exception e) {
                }
                zos = null;
            }
            if (cos != null) {
                try {
                    cos.close();
                } catch (Exception e) {
                }
                cos = null;
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
                fos = null;
            }
        }
    }

}
