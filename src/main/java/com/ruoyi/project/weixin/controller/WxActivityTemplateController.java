package com.ruoyi.project.weixin.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.system.service.ISysDictDataService;
import com.ruoyi.project.weixin.entity.WxActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpTemplateMessage;
import com.ruoyi.project.weixin.service.IWxActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpService;
import com.ruoyi.project.weixin.service.IWxMpTemplateMessageService;
import com.ruoyi.project.weixin.utils.ImgUtils;
import com.ruoyi.project.weixin.vo.EditWxTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动模板表 前端控制器
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@RestController
@RequestMapping("/wxactivity")
@AllArgsConstructor
@Api("活动模板管理")
@Slf4j
public class WxActivityTemplateController extends BaseController {


    private final IWxActivityTemplateService wxActivityTemplateService;

    private final IWxActivityTemplateMessageService wxActivityTemplateMessageService;

    private final IWxMpTemplateMessageService wxMpTemplateMessageService;

    private final IWxMpService wxMpService;

    private final WxMpService wxService;

    private final ISysDictDataService sysDictDataService;

    @ApiOperation("查询默认活动模板")
    @GetMapping("/template/list")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult getWxActivityTemplateList(){
        return AjaxResult.success(wxActivityTemplateService.list());
    }

    @ApiOperation("绑定活动模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name="templateId",value="活动模板id",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/template/bind")
    public AjaxResult bindWxActivityTemplate(@RequestParam(value = "templateId") String templateId,@RequestParam(value = "appId") String appId){
        WxMp wxMp = wxMpService.getByAppId(appId);
        wxMp.setTemplateId(templateId);
        wxMp.setActivityEnable(true);
        wxMpService.updateById(wxMp);
        // 判定是否已经复制过模板信息
        List<WxMpTemplateMessage> mpTemplateMessages = wxMpTemplateMessageService.list(Wrappers.<WxMpTemplateMessage>lambdaQuery()
                .eq(WxMpTemplateMessage::getTemplateId, templateId)
                .eq(WxMpTemplateMessage::getAppId, appId));
        if (!mpTemplateMessages.isEmpty()) {
            return AjaxResult.success(wxMp);
        }
        // 查询出模板详细信息
        QueryWrapper<WxActivityTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WxActivityTemplateMessage::getTemplateId,templateId);
        List<WxActivityTemplateMessage> list = wxActivityTemplateMessageService.list(queryWrapper);
        for (WxActivityTemplateMessage wxActivityTemplateMessage : list) {
            // 复制到公众号模板信息表
            WxMpTemplateMessage wxMpTemplateMessage = new WxMpTemplateMessage();
            wxMpTemplateMessage.setAppId(appId);
            BeanUtils.copyProperties(wxActivityTemplateMessage,wxMpTemplateMessage,"id","createId","createTime","updateId","updateTime","delFlag");
            wxMpTemplateMessage.setRepContent(wxMpTemplateMessage.getRepContent().replace("appid=","appid="+appId));
            wxMpTemplateMessageService.save(wxMpTemplateMessage);
        }
        return AjaxResult.success(wxMp);
    }

    @ApiOperation("查询公众号绑定的活动消息详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/template/message/list")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult getMpTemplateMessage(@RequestParam(value = "appId") String appId) {
        // 查询出公众号绑定的活动消息
        WxMp wxMp = wxMpService.getByAppId(appId);
        String templateId = wxMp.getTemplateId();
        if (StringUtils.isBlank(templateId)) {
            return AjaxResult.success();
        }
        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(WxMpTemplateMessage::getAppId,appId)
                .eq(WxMpTemplateMessage::getTemplateId,templateId);
        List<WxMpTemplateMessage> list = wxMpTemplateMessageService.list(queryWrapper);
        return AjaxResult.success(list);
    }

    @ApiOperation("编辑消息内容")
    @PatchMapping("/template/message/{messageId}")
    public AjaxResult updateMpTemplateMessage(@PathVariable("messageId") String id,@RequestBody EditWxTemplateVO editWxTemplateVO){
        WxMpTemplateMessage query = wxMpTemplateMessageService.getById(id);
        BeanUtils.copyProperties(editWxTemplateVO,query);
        wxMpTemplateMessageService.updateById(query);
        return AjaxResult.success(query);
    }

    @ApiOperation("活动启动/活动暂停")
    @ApiImplicitParams({
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @PatchMapping("/status/{appId}")
    public AjaxResult editActivityStatus(@PathVariable("appId") String appId,@RequestBody EditWxTemplateVO editWxTemplateVO) {
        // 查询出公众号绑定的活动消息
        WxMp wxMp = wxMpService.getByAppId(appId);
        wxMp.setActivityEnable(editWxTemplateVO.getActivityEnable());
        wxMpService.updateById(wxMp);
        return AjaxResult.success();
    }

    @ApiOperation("预览海报")
    @ApiImplicitParams({
            @ApiImplicitParam(name="messageId",value="消息Id",required=true,paramType="String")
    })
    @PatchMapping("/template/{appId}/poster/preview")
    public AjaxResult previewPoster(@PathVariable("messageId") String messageId) {
        WxMpTemplateMessage message = wxMpTemplateMessageService.getById(messageId);
        String mediaId = message.getRepMediaId();
        if (StringUtils.isNotBlank(mediaId)) {
            // 取海报图片
            InputStream inputStream = null;
            try {
                inputStream = wxService.getMaterialService().materialImageOrVoiceDownload(mediaId);
            } catch (WxErrorException e) {
                log.error("从素材库获取海报图片异常，消息模板id:{},openId:{}",messageId,e);
            }
            File poster;
            // 头像,二维码地址
            String avatarUrl = sysDictDataService.selectDictValueByLabel(ISysDictDataService.LABEL_IMG_AVATAR_URL);
            String qrCodeUrl = sysDictDataService.selectDictValueByLabel(ISysDictDataService.LABEL_IMG_QRCODE_URL);
            try {
                // 先处理二维码 设置长宽
                BufferedImage qrCodeBuffer = Thumbnails.of(ImageIO.read(new URL(avatarUrl))).size(message.getQrcodeSize(), message.getQrcodeSize()).asBufferedImage();
                // 获取圆形头像
                BufferedImage roundHead = ImgUtils.getRoundHead(new URL(qrCodeUrl));
                roundHead = Thumbnails.of(roundHead).size(message.getAvatarSize(), message.getAvatarSize()).asBufferedImage();
                // 处理海报
                Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(inputStream).scale(1.0);
                // 拼接头像
                String[] avatarCoordinate = message.getAvatarCoordinate().split(",");
                builder.watermark(new Coordinate(Integer.parseInt(avatarCoordinate[0]),Integer.parseInt(avatarCoordinate[1])), roundHead,1.0f);
                // 拼接二维码
                String[] qrcodeCoordinate = message.getQrcodeCoordinate().split(",");
                builder.watermark(new Coordinate(Integer.parseInt(qrcodeCoordinate[0]),Integer.parseInt(qrcodeCoordinate[1])), qrCodeBuffer,1.0f);
                poster = File.createTempFile("temp",".png");
                builder.toFile(poster);
                Map<String,Object> result = new HashMap<>(4);
                String posterBase64 = null;
                try {
                    posterBase64 = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(poster));
                } catch (IOException e) {
                    log.info("将海报文件编码成base64异常",e);
                } finally {
                    if (poster.exists()) {
                        poster.delete();
                    }
                }
                result.put("posterBase64",posterBase64);
                String name = poster.getName();
                result.put("suffix", name.substring(name.lastIndexOf(".")+1));
                return AjaxResult.success(result);
            } catch (Exception e) {
                logger.error("预览海报图片，拼接图片出现异常");
            }
        }
        return AjaxResult.error();
    }
}
