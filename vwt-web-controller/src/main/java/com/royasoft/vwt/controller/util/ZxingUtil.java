package com.royasoft.vwt.controller.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * ZxingUtil 的二维码常用类
 * 
 * @author meiguiyang
 */
public final class ZxingUtil {
    private static final String CHARSET = "UTF-8";

    private ZxingUtil() {
    }

    /**
     * 生成带logo的二维码图片 BufferedImage
     * 
     * @param contents 内容
     * @param logoFile logo对象
     * @return BufferedImage
     * @throws Exception
     */
    public static BufferedImage createImage(String contents, File logoFile) throws Exception {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        int qrcodeSize = 200;
        BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, qrcodeSize, qrcodeSize, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        // 插入图片
        if (logoFile != null && logoFile.exists()) {
            insertImage(image, logoFile);
        }
        return image;
    }

    /**
     * 插入LOGO
     * 
     * @param source 二维码图片
     * @param logoFile logofile
     * @throws Exception
     */
    private static void insertImage(BufferedImage source, File logoFile) throws Exception {
        Image src = ImageIO.read(logoFile);
        int width = 100;
        int height = 100;
        // 压缩LOGO
        Image image = src.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        BufferedImage tag = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(image, 0, 0, null); // 绘制缩小后的图
        g.dispose();
        src = image;
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = (500 - width) / 2;
        int y = (500 - height) / 2;
        graph.setBackground(Color.WHITE);
        graph.drawImage(src, x, y, 100, 100, null);
        Shape shape = new RoundRectangle2D.Float(x, y, 0, 0, 0, 0);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    /**
     * 生成二维码 并保存指定路径
     * 
     * @param contents 内容
     * @param logoFile logofile
     * @param file 目标文件对象
     * @throws Exception
     */
    public static void saveimage(String contents, File logoFile, File file) throws Exception {
        BufferedImage bufferedImage = createImage(contents, logoFile);
        String formatName = "JPG";
        mkdirs(file);
        ImageIO.write(bufferedImage, formatName, file);
    }

    private static void mkdirs(File file) {
        if (!file.exists() && !file.isDirectory()) {
            file.getParentFile().mkdirs();
        }
    }
}
