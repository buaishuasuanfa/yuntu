package com.ljw.yuntubackend.utils;

import com.luciad.imageio.webp.WebPReadParam;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author 刘佳伟
 * @date 2025/1/25 12:11
 * @Description
 */
public class ImgUtil {

    /**
     * 	批量将文件夹跟目录webp格式图片转成JPG格式
     * @return
     */
    public static void ListFileWebp2jpg(String folderPath) {

        File file = new File(folderPath);
        File[] files = file.listFiles();

        if (files != null) {
            for (File file0 : files) {
                if (file0.isFile() && file0.getName().endsWith(".webp")) {
                    System.out.println(file0.getName());
                    String oldfile = file0.getAbsolutePath();
                    String newfile = file0.getAbsolutePath().replace(".webp", ".jpg");
                    webp2jpg(oldfile,newfile);

                }
            }
        }


    }

    /**
     * 	webp格式图片转成JPG格式
     * @param oldfile	c:/1.test.webp
     * @param newfile	c:/1.test.jpg
     * @return
     */
    public static void webp2jpg(String oldfile, String newfile){
        // 创建WebP ImageReader实例
        ImageReader reader = ImageIO.getImageReadersByMIMEType("image/webp").next();
        // 配置解码参数
        WebPReadParam readParam = new WebPReadParam();
        readParam.setBypassFiltering(true);
        // 在ImageReader设置读取的原文件
        try {
            reader.setInput(new FileImageInputStream(new File(oldfile)));
            // 解码图像
            BufferedImage image = reader.read(0, readParam);
            // 设置输入文件的格式和文件名
            ImageIO.write(image, "jpg", new File(newfile)); // 这里也可以使用其他图片格式，但是格式和文件名后缀要保持一致
            System.out.println("webp文件转成png格式成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * JPG格式图片转成webp格式(也可以是其他格式图片)
     * @return
     */
    public static void toWebpFile(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            ImageIO.write(bufferedImage, "webp", file);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
