package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpTemplateMessage;
import com.ruoyi.project.weixin.mapper.WxMpTemplateMessageMapper;
import com.ruoyi.project.weixin.schedule.SchedulingRunnable;
import com.ruoyi.project.weixin.schedule.config.CronTaskRegistrar;
import com.ruoyi.project.weixin.service.IWxMpService;
import com.ruoyi.project.weixin.service.IWxMpTemplateMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Service
public class WxMpTemplateMessageServiceImpl extends ServiceImpl<WxMpTemplateMessageMapper, WxMpTemplateMessage> implements IWxMpTemplateMessageService {

    @Autowired
    private IWxMpService wxMpService;

    @Autowired
    private IWxMpTemplateMessageService wxMpTemplateMessageService;

    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;

    @Override
    public void pushAllScheduleMessageTask() {
        List<WxMp> list = wxMpService.list();
        List<WxMpTemplateMessage> needPublishSchedule = new ArrayList<>();
        for (WxMp wxMp : list) {
            String templateId = wxMp.getTemplateId();
            List<WxMpTemplateMessage> scheduleMessages = wxMpTemplateMessageService.list(Wrappers.<WxMpTemplateMessage>lambdaQuery()
                    .eq(WxMpTemplateMessage::getTemplateId, templateId)
                    .eq(WxMpTemplateMessage::getAppId, wxMp.getAppId())
                    .eq(WxMpTemplateMessage::getRepType, ConfigConstant.MESSAGE_REP_TYPE_SCHEDULE));
            needPublishSchedule.addAll(scheduleMessages);
        }
        for (WxMpTemplateMessage wxMpTemplateMessage : needPublishSchedule) {
            SchedulingRunnable task = new SchedulingRunnable(wxMpTemplateMessage.getScheduleClass(), wxMpTemplateMessage.getScheduleMethod(), wxMpTemplateMessage.getAppId());
            cronTaskRegistrar.addCronTask(task, wxMpTemplateMessage.getScheduleCron(),wxMpTemplateMessage.getId());
        }
    }
}
