package com.ruoyi.project.weixin.server;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.qrcode.QRCodeUtil;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxMsg;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateService;
import com.ruoyi.project.weixin.service.WxMsgService;
import com.ruoyi.project.weixin.utils.ImgUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.server
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月15日 18:45
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
@Slf4j
@Service
public class WxSendMsgServer {

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WxMsgService wxMsgService;

    @Autowired
    private IWxActivityTemplateService wxActivityTemplateService;


    /**
     * 发送文本消息
     * @param content
     * @param wxUser
     */
    public void sendTextMessage(String content, WxUser wxUser) {
        try {
            WxMpKefuMessage wxMpKefuMessage = WxMpKefuMessage
                    .TEXT()
                    .toUser(wxUser.getOpenId())
                    .content(content)
                    .build();
            wxMpService.switchoverTo(wxUser.getAppId()).getKefuService().sendKefuMessage(wxMpKefuMessage);
        } catch (Exception e) {
            log.error("发送客服消息失败，appId:{} openId：{}", wxUser.getAppId(), wxUser.getOpenId());
        }
        // 记录数据库
        WxMsg wxMsg = new WxMsg();
        wxMsg.setAppId(wxUser.getAppId());
        wxMsg.setNickName(wxUser.getNickName());
        wxMsg.setHeadimgUrl(wxUser.getHeadimgUrl());
        wxMsg.setType(ConfigConstant.WX_MSG_TYPE_2);
        wxMsg.setRepContent(content);
        wxMsg.setWxUserId(wxUser.getId());
        wxMsg.setRepType(ConfigConstant.MESSAGE_REP_TYPE_TEXT);
        wxMsgService.save(wxMsg);
    }


    /**
     * 发送海报消息
     * @param message
     * @param wxUser
     */
    public void sendPosterMessage(WxMpActivityTemplateMessage message, WxUser wxUser){
        log.info("【sendPosterMessage】,message:{},wxUser:{}",message,wxUser);
        String openId = wxUser.getOpenId();
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent()) && StringUtils.isNotBlank(message.getRepMediaId());
        if (hasAvailableMessage) {
//            String qrCodeUrl = message.getRepUrl();
            //qrCodeUrl=null时，生成的是公众号的二维码，不为null时生成的qrCodeUrl里面带的链接地址的二维码
            File poster = getPosterFile(openId, message, wxUser.getAppId());
            try {
                // 将海报上传到临时素材库
                WxMediaUploadResult uploadResult = wxMpService.switchoverTo(wxUser.getAppId()).getMaterialService().mediaUpload(ConfigConstant.MESSAGE_REP_TYPE_IMAGE, poster);
                log.info("【sendPosterMessage】上传海报到临时素材库，上传结果:{}",uploadResult);
                sendImageMessage(uploadResult,wxUser);
            } catch (WxErrorException e) {
                log.error("【sendPosterMessage】发送活动海报消息异常，消息模板id:{},openId:{}",message.getId(),openId,e);
            } finally {
                if (poster.exists()) {
                    poster.delete();
                }
            }
        }
    }


    private void sendImageMessage(WxMediaUploadResult result, WxUser wxUser) {
        try {
            WxMpKefuMessage wxMpKefuMessage = WxMpKefuMessage
                    .IMAGE()
                    .toUser(wxUser.getOpenId())
                    .mediaId(result.getMediaId())
                    .build();
            wxMpService.switchoverTo(wxUser.getAppId()).getKefuService().sendKefuMessage(wxMpKefuMessage);
        } catch (Exception e) {
            log.error("sendImageMessage发送客服消息失败，openId：{}",wxUser.getOpenId());
        }
        // 记录数据库
        WxMsg wxMsg = new WxMsg();
        wxMsg.setAppId(wxUser.getAppId());
        wxMsg.setNickName(wxUser.getNickName());
        wxMsg.setHeadimgUrl(wxUser.getHeadimgUrl());
        wxMsg.setType(ConfigConstant.WX_MSG_TYPE_2);
        wxMsg.setRepMediaId(result.getMediaId());
        wxMsg.setRepUrl(result.getUrl());
        wxMsg.setWxUserId(wxUser.getId());
        wxMsg.setRepType(ConfigConstant.MESSAGE_REP_TYPE_IMAGE);
        wxMsgService.save(wxMsg);
    }


    /**
     * qrCodeUrl为null时，生成的是公众号的二维码，
     * qrCodeUrl不为null时生成的qrCodeUrl里面带的链接地址的二维码
     * @param openId
     * @param message
     * @param appId
     * @return
     *
     * 二维码参数格式：alias:helpActivity@openid
     */
    public File getPosterFile(String openId, WxMpActivityTemplateMessage message, String appId) {
        String templateId = message.getTemplateId();
        WxActivityTemplate wxActivityTemplate = wxActivityTemplateService.getById(templateId);
        String wxMpQrParams = "alias:"+wxActivityTemplate.getAlias()+"@"+openId;
        String messageId = message.getId();
        // 先获取海报图片
        String repMediaId = message.getRepMediaId();
        InputStream inputStream = null;
        try {
            inputStream = wxMpService.switchoverTo(appId).getMaterialService().materialImageOrVoiceDownload(repMediaId);
        } catch (WxErrorException e) {
            log.error("从素材库获取海报图片异常，消息模板id:{},openId:{}",messageId,openId,e);
        }
        // 获取用户头像
        String headImgUrl = null;
        try {
            //语言
            String lang = "zh_CN";
            WxMpUser user = wxMpService.switchoverTo(appId).getUserService().userInfo(openId,lang);
            headImgUrl = user.getHeadImgUrl();
        } catch (WxErrorException e) {
            log.error("获取用户头像信息异常，消息模板id:{},openId:{}",messageId,openId,e);
        }
        // 开始处理图片,生成海报
        File poster = null;
        try {
            poster = File.createTempFile("temp",".jpg");
            // 先处理二维码 设置长宽
            BufferedImage qrCodeBuffer = null;

            String qrCodeUrl = message.getRepUrl();

            //如果指定的二维码路径为空，则使用appId自动生成二维码，否则使用指定的路径生成二维码
            if(StringUtils.isEmpty(qrCodeUrl)){
                // 获取邀请二维码
                File qrCode = null;
                try {
                    WxMpQrCodeTicket ticket = wxMpService.switchoverTo(appId).getQrcodeService().qrCodeCreateLastTicket(wxMpQrParams);
                    qrCode = wxMpService.switchoverTo(appId).getQrcodeService().qrCodePicture(ticket);
                } catch (Exception e) {
                    log.error("生成助力活动带参二维码异常，消息模板id:{},openId:{}",messageId,openId,e);
                }

                qrCodeBuffer = Thumbnails.of(qrCode).size(message.getQrcodeSize(), message.getQrcodeSize()).asBufferedImage();
            }else{
//                //将分享着的openId带到二维码中
//                qrCodeUrl = String.format(qrCodeUrl,openId); 这个动作应该在调用方法的外面做 可能还需要appid等其他场景变量，不应该在平台层面做定制化动作
                //通过Java生成二维码
                qrCodeBuffer = QRCodeUtil.encode(qrCodeUrl,null,true,0,0,message.getQrcodeSize());
            }
            // 处理头像
            URL url = new URL(headImgUrl);
            // 获取圆形头像
            BufferedImage roundHead = ImgUtils.getRoundHead(url);
            roundHead = Thumbnails.of(roundHead).size(message.getAvatarSize(), message.getAvatarSize()).asBufferedImage();
            generatorPoster(message, inputStream, poster, qrCodeBuffer, roundHead);
        } catch (Exception e) {
            log.error("生成海报图片异常，消息模板id:{},openId:{}",messageId,openId,e);
        }
        return poster;
    }

    public void generatorPoster(WxMpActivityTemplateMessage message, InputStream inputStream, File poster, BufferedImage qrCodeBuffer, BufferedImage roundHead) throws IOException {
        // 处理海报
        Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(inputStream).scale(1.0);
        // 拼接头像
        String[] avatarCoordinate = message.getAvatarCoordinate().split(",");
        builder.watermark(new Coordinate(Integer.parseInt(avatarCoordinate[0]),Integer.parseInt(avatarCoordinate[1])), roundHead,1.0f);
        // 拼接二维码
        String[] qrcodeCoordinate = message.getQrcodeCoordinate().split(",");
        log.info("【sendPosterMessage】generatorPoster:{}",qrcodeCoordinate);
        builder.watermark(new Coordinate(Integer.parseInt(qrcodeCoordinate[0]),Integer.parseInt(qrcodeCoordinate[1])), qrCodeBuffer,1.0f);
        builder.toFile(poster);
        if (poster.length() > HelpActivityConstant.POSTER_SIZE ) {
            Thumbnails.of(poster).scale(1.0).outputQuality((float) HelpActivityConstant.POSTER_SIZE / poster.length()).outputFormat("jpg").toFile(poster);
        }
    }


    /**
     * 生成指定appid的公众号二维码
     * @param appId
     * @param wxMpQrParams
     * @return
     */
    public String generatorPosterMpQrcode(String appId,String wxMpQrParams){
        String qrCodePictureUrl = null;
        try {
            //2592000秒=30天
            WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.switchoverTo(appId).getQrcodeService().qrCodeCreateTmpTicket(wxMpQrParams,2592000);
            String ticket = wxMpQrCodeTicket.getTicket();
            File qrCode = wxMpService.switchoverTo(appId).getQrcodeService().qrCodePicture(wxMpQrCodeTicket);
            String path = qrCode.getPath();
            log.info("【generatorPosterMpQrcode】,path:{}",path);
            qrCodePictureUrl = wxMpService.switchoverTo(appId).getQrcodeService().qrCodePictureUrl(ticket);

            log.info("【generatorPosterMpQrcode】,qrCodePictureUrl:{}",qrCodePictureUrl);
        } catch (Exception e) {
            log.error("生成助力活动带参二维码异常，消息模板appId:"+appId,e);
        }

        return qrCodePictureUrl;
    }


}
