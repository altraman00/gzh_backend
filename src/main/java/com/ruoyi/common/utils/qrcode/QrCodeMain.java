package com.ruoyi.common.utils.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.common.utils.qrcode
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月17日 10:37
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public class QrCodeMain {

    private static final String QR_CODE_IMAGE_PATH = "/Users/admin/Desktop/hanlp/MyQRCode.png";

    private static void generateQRCodeImage(String text, int width, int height, String filePath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

//    public static void main(String[] args) {
//        try {
//            generateQRCodeImage("This is my first QR Code", 350, 350, QR_CODE_IMAGE_PATH);
//        } catch (WriterException e) {
//            System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
//        } catch (IOException e) {
//            System.out.println("Could not generate QR Code, IOException :: " + e.getMessage());
//        }
//    }



    public static void main(String[] args) throws Exception {
//        // 存放在二维码中的内容
//        String text = "我是小铭";
//        // 嵌入二维码的图片路径,如果不写或者为null则生成一个没有嵌入图片的纯净的二维码
//        String imgPath = "/Users/admin/Desktop/hanlp/timg.jpeg";
//        // 生成的二维码的路径及名称
//        String destPath = "/Users/admin/Desktop/hanlp/jam.jpg";
//        //生成二维码,needCompress=true 表示将嵌入二维码的图片进行压缩，如果为“false”则表示不压缩
//        QRCodeUtil.encode(text, imgPath, destPath, true,60,60,300);
//        // 解析二维码,destPath：将要解析的二维码的存放路径
//        String str = QRCodeUtil.decode(destPath);
//        // 打印出解析出的内容
//        System.out.println(str);

        //生成二维码缓冲区图像
        String text = "我是小铭";
        BufferedImage bufferedImage = QRCodeUtil.encode(text,null,true,60,60,300);
        System.out.println("bufferedImage--->" + bufferedImage);

    }





}
