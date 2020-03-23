package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.mapper.WxUserMapper;
import com.ruoyi.project.weixin.service.*;
import com.ruoyi.project.weixin.utils.ImgUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * @author VingKing
 */
@Service
@Slf4j
@AllArgsConstructor
public class HelpActivityServiceImpl implements ActivityService {

    private final IWxMpTemplateMessageService wxMpTemplateMessageService;

    private final IWxTaskHelpRecordService wxTaskHelpRecordService;

    private final IWxActivityTaskService wxActivityTaskService;

    private final WxUserService wxUserService;

    private final WxMpService wxMpService;

    private final WxMsgService wxMsgService;

    private final WxUserMapper wxUserMapper;

    private final IWxMpService iWxMpService;

    @Override
    @Async
    public void execute(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId) {
        String eventKey = inMessage.getEventKey();
        String appId = wxMp.getAppId();
        String templateId = template.getId();
        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WxMpTemplateMessage::getAppId, appId).eq(WxMpTemplateMessage::getTemplateId,templateId);
        List<WxMpTemplateMessage> messages = wxMpTemplateMessageService.list(queryWrapper);
        WxUser wxUser = wxUserService.getByOpenId(openId);
        String wxUserId = wxUser.getId();
        Integer needNum = template.getNeedNum();
        log.info("event key:[{}],openId:[{}],appId[{}]",eventKey,openId,appId);
        // 首先判断是不是扫活动码进入的
        if (StringUtils.isNotBlank(eventKey) && eventKey.contains(HelpActivityConstant.SCENE_EVENT_KEY)) {
            String inviterOpenId = eventKey.substring(eventKey.lastIndexOf(":") + 1);
            WxUser inviter = wxUserService.getByOpenId(inviterOpenId);
            String inviterId = inviter.getId();
            // 不是自己扫自己的码进入的
            if (!inviterId.equals(wxUserId)) {
                WxActivityTask wxActivityTask = wxActivityTaskService.getOne(Wrappers.<WxActivityTask>lambdaQuery()
                        .eq(WxActivityTask::getWxUserId, inviterId)
                        .eq(WxActivityTask::getTemplateId,templateId)
                        .eq(WxActivityTask::getAppId,appId));
                if (wxActivityTask == null) {
                    wxActivityTask = new WxActivityTask();
                    wxActivityTask.setCompleteNum(0);
                    wxActivityTask.setTaskStatus(ConfigConstant.TASK_DOING);
                    wxActivityTask.setWxUserId(inviterId);
                    wxActivityTask.setTemplateId(templateId);
                    wxActivityTask.setAppId(appId);
                    wxActivityTaskService.save(wxActivityTask);
                }
                if (wxActivityTask.getCompleteNum() < needNum ){
                    //查找助力记录,一个人只能助力一次
                    List<WxTaskHelpRecord> records = wxTaskHelpRecordService.list(Wrappers.<WxTaskHelpRecord>lambdaQuery()
                            .eq(WxTaskHelpRecord::getHelpWxUserId, wxUserId));
                    if (records.isEmpty()) {
                        // 未助力过，可以执行助力流程
                        executeHelpSuccess(messages, wxUser, inviter, wxActivityTask,needNum);
                        // 为邀请人推送助力成功
                        executeBeHelped(messages,wxUser,inviter, wxActivityTask,needNum);
                    } else {
                        // 已经助力过了
                        executeHasHelp(messages,wxUser,inviter);
                    }
                }
            }
        }
        // 推送活动规则消息
        executeActivityRule(messages,wxUser,templateId,appId);
        // 推送活动海报
        executeActivityPoster(messages,wxUser);
    }

    private void executeActivityPoster(List<WxMpTemplateMessage> messages, WxUser wxUser) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_ACTIVITY_POSTER);
        String openId = wxUser.getOpenId();
        WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_ACTIVITY_POSTER)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent()) && StringUtils.isNotBlank(message.getRepMediaId());
        if (hasAvailableMessage) {
            File poster = getPosterFile(openId, message);
            try {
                // 将海报上传到临时素材库
                WxMediaUploadResult uploadResult = wxMpService.getMaterialService().mediaUpload(ConfigConstant.MESSAGE_REP_TYPE_IMAGE, poster);
                log.info("上传海报到临时素材库，上传结果:{}",uploadResult);
                sendImageMessage(uploadResult,wxUser);
            } catch (WxErrorException e) {
                log.error("发送活动海报消息异常，消息模板id:{},openId:{}",message.getId(),openId,e);
            } finally {
                if (poster.exists()) {
                    poster.delete();
                }
            }
        }
    }

    public File getPosterFile(String openId, WxMpTemplateMessage message) {
        StopWatch stopWatch = new StopWatch();
        String messageId = message.getId();
        // 先获取海报图片
        String repMediaId = message.getRepMediaId();
        InputStream inputStream = null;
        stopWatch.start("get poster img");
        try {
            inputStream = wxMpService.getMaterialService().materialImageOrVoiceDownload(repMediaId);
        } catch (WxErrorException e) {
            log.error("从素材库获取海报图片异常，消息模板id:{},openId:{}",messageId,openId,e);
        }
        stopWatch.stop();
        // 获取邀请二维码
        stopWatch.start("get qrcode img");
        File qrCode = null;
        try {
            WxMpQrCodeTicket ticket = wxMpService.getQrcodeService().qrCodeCreateLastTicket("helpActivity:"+ openId);
            qrCode = wxMpService.getQrcodeService().qrCodePicture(ticket);
        } catch (Exception e) {
            log.error("生成助力活动带参二维码异常，消息模板id:{},openId:{}",messageId,openId,e);
        }
        stopWatch.stop();
        // 获取用户头像
        stopWatch.start("get avatar img");
        String headImgUrl = null;
        try {
            //语言
            String lang = "zh_CN";
            WxMpUser user = wxMpService.getUserService().userInfo(openId,lang);
            headImgUrl = user.getHeadImgUrl();
        } catch (WxErrorException e) {
            log.error("获取用户头像信息异常，消息模板id:{},openId:{}",messageId,openId,e);
        }
        stopWatch.stop();
        // 开始处理图片,生成海报
        stopWatch.start("Join poster img");
        File poster = null;
        try {
            poster = File.createTempFile("temp",".jpg");
            // 先处理二维码 设置长宽
            BufferedImage qrCodeBuffer = Thumbnails.of(qrCode).size(message.getQrcodeSize(), message.getQrcodeSize()).asBufferedImage();
            // 处理头像
            URL url = new URL(headImgUrl);
            // 获取圆形头像
            BufferedImage roundHead = ImgUtils.getRoundHead(url);
            roundHead = Thumbnails.of(roundHead).size(message.getAvatarSize(), message.getAvatarSize()).asBufferedImage();
            generatorPoster(message, inputStream, poster, qrCodeBuffer, roundHead);
        } catch (Exception e) {
            log.error("生成海报图片异常，消息模板id:{},openId:{}",messageId,openId,e);
        }
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        return poster;
    }

    public void generatorPoster(WxMpTemplateMessage message, InputStream inputStream, File poster, BufferedImage qrCodeBuffer, BufferedImage roundHead) throws IOException {
        // 处理海报
        Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(inputStream).scale(1.0);
        // 拼接头像
        String[] avatarCoordinate = message.getAvatarCoordinate().split(",");
        builder.watermark(new Coordinate(Integer.parseInt(avatarCoordinate[0]),Integer.parseInt(avatarCoordinate[1])), roundHead,1.0f);
        // 拼接二维码
        String[] qrcodeCoordinate = message.getQrcodeCoordinate().split(",");
        builder.watermark(new Coordinate(Integer.parseInt(qrcodeCoordinate[0]),Integer.parseInt(qrcodeCoordinate[1])), qrCodeBuffer,1.0f);
        builder.toFile(poster);
        if (poster.length() > HelpActivityConstant.POSTER_SIZE ) {
            Thumbnails.of(poster).scale(1.0).outputQuality((float)HelpActivityConstant.POSTER_SIZE / poster.length()).outputFormat("jpg").toFile(poster);
        }
    }

    private void sendImageMessage(WxMediaUploadResult result, WxUser wxUser) {
        try {
            WxMpKefuMessage wxMpKefuMessage = WxMpKefuMessage
                    .IMAGE()
                    .toUser(wxUser.getOpenId())
                    .mediaId(result.getMediaId())
                    .build();
            wxMpService.getKefuService().sendKefuMessage(wxMpKefuMessage);
        } catch (Exception e) {
            log.error("发送客服消息失败，openId：{}",wxUser.getOpenId());
        }
        // 记录数据库
        WxMsg wxMsg = new WxMsg();
        wxMsg.setNickName(wxUser.getNickName());
        wxMsg.setHeadimgUrl(wxUser.getHeadimgUrl());
        wxMsg.setType(ConfigConstant.WX_MSG_TYPE_2);
        wxMsg.setRepMediaId(result.getMediaId());
        wxMsg.setRepUrl(result.getUrl());
        wxMsg.setWxUserId(wxUser.getId());
        wxMsg.setRepType(ConfigConstant.MESSAGE_REP_TYPE_IMAGE);
        wxMsgService.save(wxMsg);
    }

    private void executeActivityRule(List<WxMpTemplateMessage> messages, WxUser wxUser, String templateId, String appId) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_ACTIVITY_RULE);
        WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_ACTIVITY_RULE)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_SUBSCRIBE_NICKNAME,wxUser.getNickName());
            sendTextMessage(content,wxUser);
        }
        // 生成助力任务信息
        String wxUserId = wxUser.getId();
        WxActivityTask wxActivityTask = wxActivityTaskService.getOne(Wrappers.<WxActivityTask>lambdaQuery()
                .eq(WxActivityTask::getWxUserId, wxUserId)
                .eq(WxActivityTask::getTemplateId,templateId)
                .eq(WxActivityTask::getAppId,appId));
        if (wxActivityTask == null) {
            wxActivityTask = new WxActivityTask();
            wxActivityTask.setCompleteNum(0);
            wxActivityTask.setTaskStatus(ConfigConstant.TASK_DOING);
            wxActivityTask.setWxUserId(wxUserId);
            wxActivityTask.setTemplateId(templateId);
            wxActivityTask.setAppId(appId);
            wxActivityTaskService.save(wxActivityTask);
        }
    }

    private void executeHasHelp(List<WxMpTemplateMessage> messages, WxUser wxUser, WxUser inviter) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_HAS_HELP);
        WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_HAS_HELP)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_INVITER_NICKNAME,inviter.getNickName());
            sendTextMessage(content,wxUser);
        }
    }

    private void executeBeHelped(List<WxMpTemplateMessage> messages, WxUser wxUser, WxUser inviter, WxActivityTask wxActivityTask, Integer needNum) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_BE_HELPED);
        if (wxActivityTask.getCompleteNum() < needNum) {
            WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_BE_HELPED)).findFirst().orElse(null);
            boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
            if (hasAvailableMessage) {
                String content = message.getRepContent();
                content = content.replace(HelpActivityConstant.PLACEHOLDER_BE_RECOMMEND_NICKNAME,wxUser.getNickName()).replace(HelpActivityConstant.PLACEHOLDER_LACK_NUM,needNum- wxActivityTask.getCompleteNum()+"");
                sendTextMessage(content,inviter);
            }
        } else {
            WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_TASK_COMPLETE)).findFirst().orElse(null);
            boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
            if (hasAvailableMessage) {
                String content = message.getRepContent();
                sendTextMessage(content,inviter);
            }
        }
    }

    private void executeHelpSuccess(List<WxMpTemplateMessage> list, WxUser wxUser, WxUser inviter, WxActivityTask wxActivityTask, Integer needNum) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_HELP_SUCCESS);
        String wxUserId = wxUser.getId();
        String inviterId = inviter.getId();
        // 邀请人完成人数+1
        wxActivityTask.setCompleteNum(wxActivityTask.getCompleteNum() + 1);
        if (wxActivityTask.getCompleteNum() >= needNum) {
            wxActivityTask.setTaskStatus(ConfigConstant.TASK_COMPLETE);
        }
        wxActivityTaskService.updateById(wxActivityTask);
        // 存储助力记录
        WxTaskHelpRecord wxTaskHelpRecord = new WxTaskHelpRecord();
        wxTaskHelpRecord.setHelpWxUserId(wxUserId);
        wxTaskHelpRecord.setInviteWxUserId(inviterId);
        wxTaskHelpRecordService.save(wxTaskHelpRecord);
        // 推送助力成功消息
        WxMpTemplateMessage message = list.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_HELP_SUCCESS)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_INVITER_NICKNAME,inviter.getNickName());
            sendTextMessage(content,wxUser);
        }
    }

    private void sendTextMessage(String content,WxUser wxUser) {
        try {
            WxMpKefuMessage wxMpKefuMessage = WxMpKefuMessage
                    .TEXT()
                    .toUser(wxUser.getOpenId())
                    .content(content)
                    .build();
            wxMpService.getKefuService().sendKefuMessage(wxMpKefuMessage);
        } catch (Exception e) {
            log.error("发送客服消息失败，openId：{}",wxUser.getOpenId());
        }
        // 记录数据库
        WxMsg wxMsg = new WxMsg();
        wxMsg.setNickName(wxUser.getNickName());
        wxMsg.setHeadimgUrl(wxUser.getHeadimgUrl());
        wxMsg.setType(ConfigConstant.WX_MSG_TYPE_2);
        wxMsg.setRepContent(content);
        wxMsg.setWxUserId(wxUser.getId());
        wxMsg.setRepType(ConfigConstant.MESSAGE_REP_TYPE_TEXT);
        wxMsgService.save(wxMsg);
    }

    public void sendInviteMessage(String appId) {
        WxMp wxMp = iWxMpService.getByAppId(appId);
        if (!wxMp.isActivityEnable()) {
            log.info("appId:[{}]已暂停活动，流程结束",appId);
            return;
        }
        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WxMpTemplateMessage::getAppId, appId).eq(WxMpTemplateMessage::getTemplateId,HelpActivityConstant.ACTIVITY_TEMPLATE_ID);
        List<WxMpTemplateMessage> messages = wxMpTemplateMessageService.list(queryWrapper);
        WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_SCHEDULE_INVITER)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            List<WxUser> users =  wxUserMapper.getNotCompleteUser(appId,HelpActivityConstant.ACTIVITY_TEMPLATE_ID);
            log.info("共查询到：{}个需要发送消息的用户",users.size());
            String content = message.getRepContent();
            for (WxUser wxUser : users) {
                sendTextMessage(content,wxUser);
            }
        }
    }
}
