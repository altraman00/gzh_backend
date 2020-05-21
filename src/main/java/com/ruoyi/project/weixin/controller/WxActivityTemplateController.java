package com.ruoyi.project.weixin.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.common.ResultCode;
import com.ruoyi.project.system.service.ISysDictDataService;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.schedule.SchedulingRunnable;
import com.ruoyi.project.weixin.schedule.config.CronTaskRegistrar;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.*;
import com.ruoyi.project.weixin.utils.ImgUtils;
import com.ruoyi.project.weixin.utils.ThreadLocalUtil;
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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.quartz.CronExpression;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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

@Api(value = "WxActivityTemplateController", tags = "公众号多活动相关接口")
@RestController
@RequestMapping("/wxactivity")
@AllArgsConstructor
@Slf4j
public class WxActivityTemplateController extends BaseController {


    private final IWxActivityTemplateService wxActivityTemplateService;

    private final IWxActivityTemplateMessageService wxActivityTemplateMessageService;

    private final IWxMpTemplateMessageService wxMpTemplateMessageService;

    private final IWxMpService wxMpService;

    private final WxMpService wxService;

    private final ISysDictDataService sysDictDataService;

    private final CronTaskRegistrar cronTaskRegistrar;

    private final WxSendMsgServer wxSendMsgServer;

    private final IWxMpActivityTemplateService IWxMpActivityTemplateService;


    @ApiOperation("查询默认活动模板")
    @GetMapping("/template/list")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult getWxActivityTemplateList(@RequestParam(value = "type") String type){
        //type 表示当前账号主体类型(1. 订阅号 2. 服务号 3.小程序) 根据这个条件 查询支持当前主体的活动模板
        QueryWrapper<WxActivityTemplate> queryWrapper = new QueryWrapper<>();
        //此处可以用like匹配 是因为 supportScene里面现在最多只包含1,2,3 三个数字, 如果后期包含的数字超过9 需要更改匹配方案(因为1可能会匹配到10或者11上面)
        queryWrapper.lambda().like(WxActivityTemplate::getSupportScene, type);
        return AjaxResult.success(wxActivityTemplateService.list(queryWrapper));
    }

    @ApiOperation("绑定活动模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name="templateId",value="活动模板id",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/template/bind")
    public AjaxResult bindWxActivityTemplate(@RequestParam(value = "templateId") String templateId,@RequestParam(value = "appId") String appId){
        WxMp wxMp = wxMpService.getByAppId(appId);

        List<WxMpActivityTemplate> wxMpActivityTemplates = IWxMpActivityTemplateService.getActivityTemplatesByAppId(appId);
        boolean match = wxMpActivityTemplates.stream().allMatch(t -> t.getTemplateId().equals(templateId) && t.isActivityEnable());
        if(match){
            return AjaxResult.success(wxMp);
        }

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
        List<WxMpTemplateMessage> needPublishSchedule = new ArrayList<>();
        for (WxActivityTemplateMessage wxActivityTemplateMessage : list) {
            // 复制到公众号模板信息表
            WxMpTemplateMessage wxMpTemplateMessage = new WxMpTemplateMessage();
            wxMpTemplateMessage.setAppId(appId);
            BeanUtils.copyProperties(wxActivityTemplateMessage,wxMpTemplateMessage,"id","createId","createTime","updateId","updateTime","delFlag");
            wxMpTemplateMessage.setRepContent(wxMpTemplateMessage.getRepContent().replace("appid=","appid="+appId).replace("state=","state="+appId));
            wxMpTemplateMessageService.save(wxMpTemplateMessage);
            if (wxMpTemplateMessage.getRepType().equals(ConfigConstant.MESSAGE_REP_TYPE_SCHEDULE)) {
                needPublishSchedule.add(wxMpTemplateMessage);
            }
        }
        // 发布定时任务
        // 先移除原绑定任务
        List<WxMpTemplateMessage> originalScheduleMessages = wxMpTemplateMessageService.list(Wrappers.<WxMpTemplateMessage>lambdaQuery()
                .eq(WxMpTemplateMessage::getTemplateId, templateId)
                .eq(WxMpTemplateMessage::getAppId, appId)
                .eq(WxMpTemplateMessage::getRepType, ConfigConstant.MESSAGE_REP_TYPE_SCHEDULE));
        for (WxMpTemplateMessage originalScheduleMessage : originalScheduleMessages) {
            cronTaskRegistrar.removeCronTask(originalScheduleMessage.getId());
        }
        // 再发布新的定时任务
        for (WxMpTemplateMessage wxMpTemplateMessage : needPublishSchedule) {
            SchedulingRunnable task = new SchedulingRunnable(wxMpTemplateMessage.getScheduleClass(), wxMpTemplateMessage.getScheduleMethod(), appId);
            cronTaskRegistrar.addCronTask(task,wxMpTemplateMessage.getScheduleCron(), wxMpTemplateMessage.getId());
            log.info("成功发布定时任务:messageId:[{}]",wxMpTemplateMessage.getId());
        }

        return AjaxResult.success(wxMp);
    }

    @ApiOperation("查询公众号绑定的某一个活动的活动详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
            ,@ApiImplicitParam(name="id",value="公众号下绑定的具体某个活动的id",required=true,paramType="String")
    })
    @GetMapping("/template/message/list")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult getMpTemplateMessage(
             @RequestParam(value = "appId") String appId
            ,@RequestParam(value = "id") String id) {

        WxMpActivityTemplate wxMpActivityTemplate =
                IWxMpActivityTemplateService.getOne(Wrappers.<WxMpActivityTemplate>lambdaQuery()
                        .eq(WxMpActivityTemplate::getAppId,appId)
                        .eq(WxMpActivityTemplate::getId,id)
                        .eq(WxMpActivityTemplate::getDelFlag,"0")
                );
        if(wxMpActivityTemplate == null){
            return AjaxResult.success("活动不存在");
        }
        // 查询出公众号绑定的活动消息
        Map<String,List<WxMpTemplateMessage>> map = Maps.newHashMap();
        String templateId = wxMpActivityTemplate.getTemplateId();
        String templateName = wxMpActivityTemplate.getTemplateName();
        if (StringUtils.isBlank(templateId)) {
            return AjaxResult.success();
        }
        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(WxMpTemplateMessage::getAppId,wxMpActivityTemplate.getAppId())
                .eq(WxMpTemplateMessage::getTemplateId,templateId).orderByAsc(WxMpTemplateMessage::getSortNo);
        List<WxMpTemplateMessage> list = wxMpTemplateMessageService.list(queryWrapper);
        map.put(templateName,list);
        return AjaxResult.success(map);
    }

    @ApiOperation("编辑消息内容")
    @PatchMapping("/template/message/{messageId}")
    public AjaxResult updateMpTemplateMessage(@PathVariable("messageId") String id,@RequestBody EditWxTemplateVO editWxTemplateVO){
        String cron = editWxTemplateVO.getScheduleCron();
        WxMpTemplateMessage query = wxMpTemplateMessageService.getById(id);
        if (query.getRepType().equals(ConfigConstant.MESSAGE_REP_TYPE_SCHEDULE)) {
            boolean validExpression = CronExpression.isValidExpression(cron);
            if (!validExpression) {
                return AjaxResult.error(ResultCode.CRON_NOT_CORRECT);
            }
        }
        String originalCron = query.getScheduleCron();
        BeanUtils.copyProperties(editWxTemplateVO,query);
        wxMpTemplateMessageService.updateById(query);
        if (query.getRepType().equals(ConfigConstant.MESSAGE_REP_TYPE_SCHEDULE) && !originalCron.equals(cron)) {
            // 重新发布定时任务
            SchedulingRunnable task = new SchedulingRunnable(query.getScheduleClass(), query.getScheduleMethod(), query.getAppId());
            cronTaskRegistrar.addCronTask(task,query.getScheduleCron(),query.getId());
            log.info("成功发布定时任务:messageId:[{}]",query.getId());
        }
        return AjaxResult.success(query);
    }

    @ApiOperation("活动启动/活动暂停")
    @ApiImplicitParams({
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @PatchMapping("/status/{appId}")
    public AjaxResult editActivityStatus(@PathVariable("appId") String appId,@RequestBody EditWxTemplateVO editWxTemplateVO) {
        // 查询出公众号绑定的活动消息
        Boolean activityEnable = editWxTemplateVO.getActivityEnable();
        WxMp wxMp = wxMpService.getByAppId(appId);
        wxMp.setActivityEnable(activityEnable);
        wxMpService.updateById(wxMp);
        return AjaxResult.success();
    }

    @ApiOperation("预览海报")
    @ApiImplicitParams({
            @ApiImplicitParam(name="messageId",value="消息Id",required=true,paramType="String")
    })
    @GetMapping("/template/{messageId}/poster/preview")
    public AjaxResult previewPoster(@PathVariable("messageId") String messageId) {
        WxMpTemplateMessage message = wxMpTemplateMessageService.getById(messageId);
        String mediaId = message.getRepMediaId();
        String appId = ThreadLocalUtil.getAppId();
        logger.debug("previewPoster 当前操作的APPID:{}", appId);
        if(StringUtils.isEmpty(appId)){
            AjaxResult.success();
        }
        if (StringUtils.isNotBlank(mediaId)) {
            // 取海报图片
            InputStream inputStream = null;
            try {
                inputStream = wxService.switchoverTo(appId).getMaterialService().materialImageOrVoiceDownload(mediaId);
            } catch (WxErrorException e) {
                log.error("从素材库获取海报图片异常，消息模板id:{},openId:{}",messageId,e);
            }
            // 头像,二维码地址
            String avatarUrl = sysDictDataService.selectDictValueByLabel(ISysDictDataService.LABEL_IMG_AVATAR_URL);
            String qrCodeUrl = sysDictDataService.selectDictValueByLabel(ISysDictDataService.LABEL_IMG_QRCODE_URL);
            try {
                // 先处理二维码 设置长宽
                BufferedImage qrCodeBuffer = Thumbnails.of(ImageIO.read(new URL(qrCodeUrl))).size(message.getQrcodeSize(), message.getQrcodeSize()).asBufferedImage();
                // 获取圆形头像
                BufferedImage roundHead = ImgUtils.getRoundHead(new URL(avatarUrl));
                roundHead = Thumbnails.of(roundHead).size(message.getAvatarSize(), message.getAvatarSize()).asBufferedImage();
                // 处理海报
                File poster = File.createTempFile("temp",".jpg");
                wxSendMsgServer.generatorPoster(message,inputStream,poster,qrCodeBuffer,roundHead);
                Map<String,Object> result = new HashMap<>(4);
                String posterBase64 = null;
                try {
                    posterBase64 = Base64.encodeBase64String(FileUtils.readFileToByteArray(poster));
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
                logger.error("预览海报图片，拼接图片出现异常",e);
            }
        }
        return AjaxResult.error();
    }
}
