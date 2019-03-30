package com.royasoft.vwt.cag.util;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImgCompress {
	private File file = null; // 文件对象
	private String inputDir; // 输入图路径
	private String outputDir; // 输出图路径
	private int outputWidth = 200; // 默认输出图片宽
	private int outputHeight = 200; // 默认输出图片高
	private boolean proportion = true; // 是否等比缩放标记(默认为等比缩放)

	public ImgCompress() { // 初始化变量
		inputDir = "";
		outputDir = "";

		outputWidth = 200;
		outputHeight = 200;
	}

	public void setInputDir(String inputDir) {
		this.inputDir = inputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public void setOutputWidth(int outputWidth) {
		this.outputWidth = outputWidth;
	}

	public void setOutputHeight(int outputHeight) {
		this.outputHeight = outputHeight;
	}

	public void setWidthAndHeight(int width, int height) {
		this.outputWidth = width;
		this.outputHeight = height;
	}

	/*
	 * 获得图片大小 传入参数 String path ：图片路径
	 */
	public long getPicSize(String path) {
		file = new File(path);
		return file.length();
	}

	// 图片处理
	public boolean compressPic() {
		boolean boo = true;
		try {
			// 获得源文件
			file = new File(inputDir);
			if (!file.exists()) {
				boo = false;
			}
			Image img = ImageIO.read(file);
			// 判断图片格式是否正确
			if (img.getWidth(null) == -1) {
				System.out.println(" can't read,retry!" + "<BR>");
				boo = false;
			} else {
				if (img.getWidth(null) < 200 || img.getHeight(null) < 200) {
					FileManageTool fileManageTool = new FileManageTool();
					fileManageTool.copyFile(inputDir, outputDir);
				} else {
					int newWidth;
					int newHeight;
					// 判断是否是等比缩放
					if (this.proportion == true) {
						// 为等比缩放计算输出的图片宽度及高度
						double rate1 = ((double) img.getWidth(null))
								/ (double) outputWidth;// + 0.1;
						double rate2 = ((double) img.getHeight(null))
								/ (double) outputHeight;// + 0.1;
						// 根据缩放比率大的进行缩放控制
						// double rate = rate1 > rate2 ? rate1 : rate2;
						double rate;
						if (img.getWidth(null) > img.getHeight(null)) {
							rate = rate2;
						} else {
							rate = rate1;
						}
						newWidth = (int) (((double) img.getWidth(null)) / rate);
						newHeight = (int) (((double) img.getHeight(null)) / rate);
					} else {
						newWidth = img.getWidth(null); // 输出的图片宽度
						newHeight = img.getHeight(null); // 输出的图片高度
					}
					BufferedImage tag = new BufferedImage((int) newWidth,
							(int) newHeight, BufferedImage.TYPE_INT_RGB);

					/*
					 * Image.SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的 优先级比速度高 生成的图片质量比较好
					 * 但速度慢
					 */
					tag.getGraphics().drawImage(
							img.getScaledInstance(newWidth, newHeight,
									Image.SCALE_SMOOTH), 0, 0, null);
					FileOutputStream out = new FileOutputStream(outputDir);
					// JPEGImageEncoder可适用于其他图片类型的转换
					JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
					encoder.encode(tag);
					out.close();
					cutCenterImage(outputDir, outputDir, 200, 200);
					boo = true;
				}

			}
		} catch (IOException ex) {
			boo = false;
			ex.printStackTrace();
		}
		return boo;
	}

	public boolean compressPicOnly(String inDir, String outDir, int outWidth,
			int outHeight) {
		boolean boo = true;
		try {
			// 获得源文件
			file = new File(inDir);
			if (!file.exists()) {
				boo = false;
			}
			Image img = ImageIO.read(file);
			// 判断图片格式是否正确
			if (img.getWidth(null) == -1) {
				System.out.println(" can't read,retry!" + "<BR>");
				boo = false;
			} else {
				int newWidth;
				int newHeight;
				// 判断是否是等比缩放
				if (this.proportion == true) {
					// 为等比缩放计算输出的图片宽度及高度
					double rate1 = ((double) img.getWidth(null))
							/ (double) outWidth;// + 0.1;
					double rate2 = ((double) img.getHeight(null))
							/ (double) outHeight;// + 0.1;
					// 根据缩放比率大的进行缩放控制
					// double rate = rate1 > rate2 ? rate1 : rate2;
					double rate;
					if (rate2 > rate1) {
						rate = rate2;
					} else {
						rate = rate1;
					}
					newWidth = (int) (((double) img.getWidth(null)) / rate);
					newHeight = (int) (((double) img.getHeight(null)) / rate);
				} else {
					newWidth = img.getWidth(null); // 输出的图片宽度
					newHeight = img.getHeight(null); // 输出的图片高度
				}
				BufferedImage tag = new BufferedImage((int) newWidth,
						(int) newHeight, BufferedImage.TYPE_INT_RGB);

				/*
				 * Image.SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的 优先级比速度高 生成的图片质量比较好 但速度慢
				 */
				tag.getGraphics().drawImage(
						img.getScaledInstance(newWidth, newHeight,
								Image.SCALE_SMOOTH), 0, 0, null);
				FileOutputStream out = new FileOutputStream(outDir);
				// JPEGImageEncoder可适用于其他图片类型的转换
				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
				encoder.encode(tag);
				out.close();

				// ImageIO.write(tag, "jpg", new File(outDir));
				boo = true;

			}
		} catch (IOException ex) {
			boo = false;
			ex.printStackTrace();
		}
		return boo;
	}

	public boolean compressPic(String inputDir, String outputDir) {
		// 输入图路径
		this.inputDir = inputDir;
		// 输出图路径
		this.outputDir = outputDir;

		return compressPic();
	}

	public boolean compressPic(String inputDir, String outputDir, int width,
			int height) {
		// 输入图路径
		this.inputDir = inputDir;
		// 输出图路径
		this.outputDir = outputDir;
		// 设置图片长宽
		setWidthAndHeight(width, height);
		// 是否是等比缩放 标记
		this.proportion = true;
		return compressPic();
	}

	/*
	 * 根据尺寸图片居中裁剪
	 */
	public static void cutCenterImage(String src, String dest, int w, int h)
			throws IOException {
		Iterator iterator = ImageIO.getImageReadersByFormatName("jpg");
		ImageReader reader = (ImageReader) iterator.next();
		InputStream in = new FileInputStream(src);
		ImageInputStream iis = ImageIO.createImageInputStream(in);
		reader.setInput(iis, true);
		ImageReadParam param = reader.getDefaultReadParam();
		int imageIndex = 0;
		Rectangle rect = new Rectangle((reader.getWidth(imageIndex) - w) / 2,
				(reader.getHeight(imageIndex) - h) / 2, w, h);
		param.setSourceRegion(rect);
		BufferedImage bi = reader.read(0, param);
		ImageIO.write(bi, "jpg", new File(dest));

	}

}