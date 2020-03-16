package com.ruoyi.project.weixin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 活动模板表 前端控制器
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Api("公众号相关")
@RestController
@RequestMapping("/open/wxmp")
@AllArgsConstructor
@Slf4j
public class WxMpOpenController extends BaseController {

    private final IWxMpService myWxMpService;

    private final IWxActivityTemplateService wxActivityTemplateService;

    private final WxMpService wxMpService;

    @ApiOperation("获取公众号基本信息")
    @ApiImplicitParam(name = "appIdentify", value = "公众号身份标识，目前单公众号，固定为online_study", dataType = "String",required = true)
    @GetMapping("/info")
    public AjaxResult getMpInfo(@RequestParam(value = "appIdentify") String appIdentify){
        Map<String,Object> map = new HashMap<>(16);
        // 根据定义好的公众号标识查找公众号
        QueryWrapper<WxMp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_identify",appIdentify);
        WxMp wxMp = myWxMpService.getOne(queryWrapper);
        map.put("wxMp", wxMp);
        // 查询当前公众号配置的活动模板
        WxActivityTemplate wxActivityTemplate = null;
        if (StringUtils.isNotEmpty(wxMp.getTemplateId())) {
            wxActivityTemplate = wxActivityTemplateService.getById(wxMp.getTemplateId());
        }
        map.put("template",wxActivityTemplate);
        return AjaxResult.success(map);
    }

    @ApiOperation("移动端微信授权")
    @ApiImplicitParam(name = "code", value = "微信授权code", dataType = "String",required = true)
    @GetMapping("/oauth2")
    public AjaxResult oauth2(@RequestParam(value = "code") String code){
        // 目前只支持单公众号，返回默认公众号,若改造多公众号需根据appId查询
        Map<String,Object> map = new HashMap<>(16);
        try {
            WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
            WxMpUser wxMpUser = wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);
            map.put("accessToken",wxMpOAuth2AccessToken);
            map.put("wxMpUser",wxMpUser);
        } catch (Exception e) {
            log.error("调用微信授权异常",e);
        }
        return AjaxResult.success(map);
    }

    @GetMapping("/qrcode")
    public AjaxResult getWxMpQrCodeTicket(){
       WxMpQrCodeTicket ticket;
        try {
            ticket = wxMpService.getQrcodeService().qrCodeCreateLastTicket("helpActivity:om_6Xszdb4pEGd2aZm3zi72w5NUw");
            File qrCode = wxMpService.getQrcodeService().qrCodePicture(ticket);
            BufferedImage qrCodeBuffer = Thumbnails.of(qrCode).size(320, 320).asBufferedImage();
            URL url = new URL("http://thirdwx.qlogo.cn/mmopen/wjIXhWEwjdY7o8RNHKBnkkWOVRweq5X0JLibYIPZIZl1wF8PiaibeXoQYHBaAGX3auOF7wgGr0SZVAargD3bHRuibeDLvSTrHHSv/132");
            BufferedImage roundHead = getRoundHead(url);
            Thumbnails.Builder<BufferedImage> headBuilder = Thumbnails.of(roundHead);
            headBuilder.size(108,108);
            BufferedImage bufferedImage = headBuilder.asBufferedImage();

            File poster = new File("C:\\Users\\VingKing\\Downloads\\编组 2备份 2_slices\\编组 2备份 2@2x.png");
            Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(new FileInputStream(poster)).scale(1.0);
            builder.watermark(new Coordinate(72,60), bufferedImage,1.0f);
            builder.watermark(new Coordinate(1060,1972), qrCodeBuffer,1.0f).toFile("C:\\Users\\VingKing\\Desktop\\1.png");
        } catch (Exception e) {
            log.error("生成带参二维码异常",e);
        }
        return AjaxResult.success();
    }

    private BufferedImage getRoundHead(URL url) throws IOException {
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
