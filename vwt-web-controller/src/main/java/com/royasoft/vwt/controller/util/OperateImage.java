/************************************************
 *  Copyright © 2002-2016 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.util;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * 
 * 裁剪图片工具
 *
 * @Author:wuyf
 * @Date 2016-4-12
 */
public class OperateImage {

    // ===源图片路径名称如：c:\1.jpg
    private String srcpath;

    // ===剪切图片存放路径名称。如：c:\2.jpg
    private String subpath;

    public void setSrcpath(String srcpath) {
        this.srcpath = srcpath;
    }

    public void setSubpath(String subpath) {
        this.subpath = subpath;
    }

    // ===剪切点x坐标
    private int x;

    private int y;

    // ===剪切点宽度
    private int width;
    private int height;

    // 图片类型
    private String type;

    public OperateImage() {
    }

    public OperateImage(int x, int y, int width, int height, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    /**
     * 
     * 对图片裁剪，并把裁剪完蛋新图片保存 。
     */

    public void cut() throws IOException {
        FileInputStream is = null;
        ImageInputStream iis = null;
        try {
            // 读取图片文件
            is = new FileInputStream(srcpath);

            /**
             * 
             * 返回包含所有当前已注册 ImageReader 的 Iterator，这些 ImageReader
             * 
             * 声称能够解码指定格式。 参数：formatName - 包含非正式格式名称 .
             * 
             * (例如 "jpeg" 或 "tiff")等 。
             */
            Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(type);

            ImageReader reader = it.next();

            // 获取图片流
            iis = ImageIO.createImageInputStream(is);

            /**
             * 
             * <p>
             * iis:读取源。true:只向前搜索
             * </p>
             * .将它标记为 ‘只向前搜索’。
             * 
             * 此设置意味着包含在输入源中的图像将只按顺序读取，可能允许 reader
             * 
             * 避免缓存包含与以前已经读取的图像关联的数据的那些输入部分。
             */
            reader.setInput(iis, true);

            /**
             * 
             * <p>
             * 描述如何对流进行解码的类
             * <p>
             * .用于指定如何在输入时从 Java Image I/O
             * 
             * 框架的上下文中的流转换一幅图像或一组图像。用于特定图像格式的插件
             * 
             * 将从其 ImageReader 实现的 getDefaultReadParam 方法中返回
             * 
             * ImageReadParam 的实例。
             */
            ImageReadParam param = reader.getDefaultReadParam();

            /**
             * 
             * 图片裁剪区域。Rectangle 指定了坐标空间中的一个区域，通过 Rectangle 对象
             * 
             * 的左上顶点的坐标(x，y)、宽度和高度可以定义这个区域。
             */
            Rectangle rect = new Rectangle(x, y, width, height);

            // 提供一个 BufferedImage，将其用作解码像素数据的目标。
            param.setSourceRegion(rect);

            /**
             * 
             * 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象，并将
             * 
             * 它作为一个完整的 BufferedImage 返回。
             */
            BufferedImage bi = reader.read(0, param);

            // 保存新图片
            ImageIO.write(bi, type, new File(subpath));
        } finally {
            if (is != null)
                is.close();
            if (iis != null)
                iis.close();
        }

    }

    /*
     * 根据尺寸图片居中裁剪
     */
    @SuppressWarnings("rawtypes")
    public static void cutCenterImage(String src, int w, int h, String type) throws IOException {
        Iterator iterator = ImageIO.getImageReadersByFormatName(type);
        ImageReader reader = (ImageReader) iterator.next();
        InputStream in = new FileInputStream(src);
        ImageInputStream iis = ImageIO.createImageInputStream(in);
        reader.setInput(iis, true);
        ImageReadParam param = reader.getDefaultReadParam();
        int imageIndex = 0;
        Rectangle rect = new Rectangle((reader.getWidth(imageIndex) - w) / 2, (reader.getHeight(imageIndex) - h) / 2, w, h);
        param.setSourceRegion(rect);
        BufferedImage bi = reader.read(0, param);
        ImageIO.write(bi, type, new File(src));

    }

    /*
     * 图片缩放
     */
    @SuppressWarnings("static-access")
    public static void zoomImage(String src, int w, int h) throws Exception {
        double wr = 0, hr = 0;
        File srcFile = new File(src);
        BufferedImage bufImg = ImageIO.read(srcFile);
        Image Itemp = bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);
        wr = w * 1.0 / bufImg.getWidth();
        hr = h * 1.0 / bufImg.getHeight();
        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
        Itemp = ato.filter(bufImg, null);
        File destFile = new File(src);
        try {
            ImageIO.write((BufferedImage) Itemp, src.substring(src.lastIndexOf(".") + 1), destFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}