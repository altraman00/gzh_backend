package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.system.service.ISysDictDataService;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.entity.WxActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpTemplateMessage;
import com.ruoyi.project.weixin.mapper.WxMpTemplateMessageMapper;
import com.ruoyi.project.weixin.schedule.SchedulingRunnable;
import com.ruoyi.project.weixin.schedule.config.CronTaskRegistrar;
import com.ruoyi.project.weixin.service.IWxActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpService;
import com.ruoyi.project.weixin.service.IWxMpTemplateMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.weixin.utils.ImgUtils;
import com.ruoyi.project.weixin.vo.EditWxTemplateVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Service;

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
 *  服务实现类
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Service
@AllArgsConstructor
@Slf4j
public class WxMpTemplateMessageServiceImpl extends ServiceImpl<WxMpTemplateMessageMapper, WxMpTemplateMessage> implements IWxMpTemplateMessageService {

    private final IWxActivityTemplateService wxActivityTemplateService;

    private final IWxActivityTemplateMessageService wxActivityTemplateMessageService;

    private final IWxMpTemplateMessageService wxMpTemplateMessageService;

    private final IWxMpService wxMpService;

    private final WxMpService wxService;

    private final ISysDictDataService sysDictDataService;

    private final HelpActivityServiceImpl helpActivityService;

    private final CronTaskRegistrar cronTaskRegistrar;

    @Override
    public WxMp bindWxActivityTemplate(String templateId, String appId) {
        WxMp wxMp = wxMpService.getByAppId(appId);
        String originalTemplateId = wxMp.getTemplateId();
        // 未做任何改动
        if (originalTemplateId.equals(templateId) && wxMp.isActivityEnable()) {
            return wxMp;
        }
        wxMp.setTemplateId(templateId);
        wxMp.setActivityEnable(true);
        wxMpService.updateById(wxMp);
        // 判定是否已经复制过模板信息
        List<WxMpTemplateMessage> mpTemplateMessages = wxMpTemplateMessageService.list(Wrappers.<WxMpTemplateMessage>lambdaQuery()
                .eq(WxMpTemplateMessage::getTemplateId, templateId)
                .eq(WxMpTemplateMessage::getAppId, appId));
        if (!mpTemplateMessages.isEmpty()) {
            return wxMp;
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
            wxMpTemplateMessage.setRepContent(wxMpTemplateMessage.getRepContent().replace("appid=","appid="+appId));
            wxMpTemplateMessageService.save(wxMpTemplateMessage);
            if (wxMpTemplateMessage.getRepType().equals(ConfigConstant.MESSAGE_REP_TYPE_SCHEDULE)) {
                needPublishSchedule.add(wxMpTemplateMessage);
            }
        }
        // 发布定时任务
        // 先移除原绑定任务
        List<WxMpTemplateMessage> originalScheduleMessages = wxMpTemplateMessageService.list(Wrappers.<WxMpTemplateMessage>lambdaQuery()
                .eq(WxMpTemplateMessage::getTemplateId, originalTemplateId)
                .eq(WxMpTemplateMessage::getAppId, appId)
                .eq(WxMpTemplateMessage::getRepType, ConfigConstant.MESSAGE_REP_TYPE_SCHEDULE));
        for (WxMpTemplateMessage originalScheduleMessage : originalScheduleMessages) {
            SchedulingRunnable task = new SchedulingRunnable(originalScheduleMessage.getScheduleClass(), originalScheduleMessage.getScheduleMethod(), appId);
            CronTask cronTask = new CronTask(task, originalScheduleMessage.getScheduleCron());
            cronTaskRegistrar.removeCronTask(cronTask.getRunnable());
        }
        // 再发布新的定时任务
        for (WxMpTemplateMessage wxMpTemplateMessage : needPublishSchedule) {
            SchedulingRunnable task = new SchedulingRunnable(wxMpTemplateMessage.getScheduleClass(), wxMpTemplateMessage.getScheduleMethod(), appId);
            CronTask cronTask = new CronTask(task, wxMpTemplateMessage.getScheduleCron());
            cronTaskRegistrar.addCronTask(cronTask);
        }
        return wxMp;
    }

    @Override
    public List<WxMpTemplateMessage> getMpTemplateMessageList(String appId) {
        // 查询出公众号绑定的活动消息
        WxMp wxMp = wxMpService.getByAppId(appId);
        String templateId = wxMp.getTemplateId();
        if (StringUtils.isBlank(templateId)) {
            return null;
        }
        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(WxMpTemplateMessage::getAppId,appId)
                .eq(WxMpTemplateMessage::getTemplateId,templateId);
        List<WxMpTemplateMessage> list = wxMpTemplateMessageService.list(queryWrapper);
        return list;
    }

    @Override
    public WxMpTemplateMessage updateMpTemplateMessage(String id, EditWxTemplateVO editWxTemplateVO) {
        WxMpTemplateMessage query = wxMpTemplateMessageService.getById(id);
        String originalCron = query.getScheduleCron();
        BeanUtils.copyProperties(editWxTemplateVO,query);
        wxMpTemplateMessageService.updateById(query);
        if (query.getRepType().equals(ConfigConstant.MESSAGE_REP_TYPE_SCHEDULE) && !originalCron.equals(editWxTemplateVO.getScheduleCron())) {
            // 重新发布定时任务
            SchedulingRunnable task = new SchedulingRunnable(query.getScheduleClass(), query.getScheduleMethod(), query.getAppId());
            CronTask cronTask = new CronTask(task, query.getScheduleCron());
            cronTaskRegistrar.addCronTask(cronTask);
        }
        return query;
    }

    @Override
    public void editActivityStatus(String appId, Boolean status) {
        WxMp wxMp = wxMpService.getByAppId(appId);
        wxMp.setActivityEnable(status);
        wxMpService.updateById(wxMp);
    }

    @Override
    public Map<String, Object> previewPoster(String messageId) {
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
                helpActivityService.generatorPoster(message,inputStream,poster,qrCodeBuffer,roundHead);
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
                return result;
            } catch (Exception e) {
                log.error("预览海报图片，拼接图片出现异常",e);
            }
        }
        return null;
    }
}
