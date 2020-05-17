package com.ruoyi.common.utils.qrcode;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.common.utils.qrcode
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月17日 10:17
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public class QRCodeUtil {

    private static final String CHARSET = "utf-8";
    private static final String FORMAT_NAME = "JPG";

//    // LOGO宽度
//    private static final int WIDTH = 60;
//    // LOGO高度
//    private static final int HEIGHT = 60;
//    // 二维码尺寸
//    private static final int QRCODE_SIZE = 300;

    private static BufferedImage createImage(String content, String avatarImgPath, boolean needCompress, int avatarWidth, int avatarHeight, int qrcodeSize) throws Exception {
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, qrcodeSize, qrcodeSize, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        if (avatarImgPath == null || "".equals(avatarImgPath)) {
            return image;
        }
        // 插入图片
        QRCodeUtil.insertImage(image, avatarImgPath, needCompress, avatarWidth, avatarHeight, qrcodeSize);
        return image;
    }

    private static void insertImage(BufferedImage source, String avatarImgPath, boolean needCompress, int avatarWidth, int avatarHeight, int qrcodeSize) throws Exception {
        File file = new File(avatarImgPath);
        if (!file.exists()) {
            System.err.println("" + avatarImgPath + "   该文件不存在！");
            return;
        }
        Image src = ImageIO.read(new File(avatarImgPath));
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        // 压缩LOGO
        if (needCompress) {
            if (width > avatarWidth) {
                width = avatarWidth;
            }
            if (height > avatarHeight) {
                height = avatarHeight;
            }
            Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            // 绘制缩小后的图
            g.drawImage(image, 0, 0, null);
            g.dispose();
            src = image;
        }
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = (qrcodeSize - width) / 2;
        int y = (qrcodeSize - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    public static void encode(String content, String avatarImgPath, String destPath, boolean needCompress, int avatarWidth, int avatarHeight, int qrcodeSize) throws Exception {
        BufferedImage image = QRCodeUtil.createImage(content, avatarImgPath, needCompress, avatarWidth, avatarHeight, qrcodeSize);
        mkdirs(destPath);
        // String file = new Random().nextInt(99999999)+".jpg";
        // ImageIO.write(image, FORMAT_NAME, new File(destPath+"/"+file));
        ImageIO.write(image, FORMAT_NAME, new File(destPath));
    }

    public static BufferedImage encode(String content, String avatarImgPath, boolean needCompress, int avatarWidth, int avatarHeight, int qrcodeSize) throws Exception {
        BufferedImage image = QRCodeUtil.createImage(content, avatarImgPath, needCompress, avatarWidth, avatarHeight, qrcodeSize);
        return image;
    }

    public static void mkdirs(String destPath) {
        File file = new File(destPath);
        // 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    public static void encode(String content, String avatarImgPath, String destPath, int avatarWidth, int avatarHeight, int qrcodeSize) throws Exception {
        QRCodeUtil.encode(content, avatarImgPath, destPath, false, avatarWidth, avatarHeight, qrcodeSize);
    }
    // 被注释的方法
    /*
     * public static void encode(String content, String destPath, boolean
     * needCompress) throws Exception { QRCodeUtil.encode(content, null, destPath,
     * needCompress); }
     */

    public static void encode(String content, String destPath, int avatarWidth, int avatarHeight, int qrcodeSize) throws Exception {
        QRCodeUtil.encode(content, null, destPath, false, avatarWidth, avatarHeight, qrcodeSize);
    }

    public static void encode(String content, String avatarImgPath, OutputStream output, boolean needCompress, int avatarWidth, int avatarHeight, int qrcodeSize)
            throws Exception {
        BufferedImage image = QRCodeUtil.createImage(content, avatarImgPath, needCompress, avatarWidth, avatarHeight, qrcodeSize);
        ImageIO.write(image, FORMAT_NAME, output);
    }

    public static void encode(String content, OutputStream output, int avatarWidth, int avatarHeight, int qrcodeSize) throws Exception {
        QRCodeUtil.encode(content, null, output, false, avatarWidth, avatarHeight, qrcodeSize);
    }

    public static String decode(File file) throws Exception {
        BufferedImage image;
        image = ImageIO.read(file);
        if (image == null) {
            return null;
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        Hashtable hints = new Hashtable();
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
        result = new MultiFormatReader().decode(bitmap, hints);
        String resultStr = result.getText();
        return resultStr;
    }

    public static String decode(String path) throws Exception {
        return QRCodeUtil.decode(new File(path));
    }

}
