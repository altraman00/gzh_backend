package com.ruoyi.project.weixin.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImgUtils {

    public static BufferedImage getRoundHead(URL url) throws IOException {
        BufferedImage avatarImage = ImageIO.read(url);
        int width = avatarImage.getWidth();
        // 透明底的图片
        BufferedImage formatAvatarImage = new BufferedImage(width, width, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = formatAvatarImage.createGraphics();
        //把图片切成一个圓

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //留一个像素的空白区域，这个很重要，画圆的时候把这个覆盖
        int border = 1;
        //图片是一个圆型
        Ellipse2D.Double shape = new Ellipse2D.Double(border, border, width - border * 2, width - border * 2);
        //需要保留的区域
        graphics.setClip(shape);
        graphics.drawImage(avatarImage, border, border, width - border * 2, width - border * 2, null);
        graphics.dispose();

        //在圆图外面再画一个圆

        //新创建一个graphics，这样画的圆不会有锯齿
        graphics = formatAvatarImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int border1 = 3;
        //画笔是4.5个像素，BasicStroke的使用可以查看下面的参考文档
        //使画笔时基本会像外延伸一定像素，具体可以自己使用的时候测试
        Stroke s = new BasicStroke(4.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        graphics.setStroke(s);
        graphics.setColor(Color.WHITE);
        graphics.drawOval(border1, border1, width - border1 * 2, width - border1 * 2);
        graphics.dispose();
        return formatAvatarImage;
    }
}
