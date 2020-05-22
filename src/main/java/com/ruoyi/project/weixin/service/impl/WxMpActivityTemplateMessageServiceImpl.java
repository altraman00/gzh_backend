package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.mapper.WxMpActivityTemplateMessageMapper;
import com.ruoyi.project.weixin.schedule.SchedulingRunnable;
import com.ruoyi.project.weixin.schedule.config.CronTaskRegistrar;
import com.ruoyi.project.weixin.service.IWxMpService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class WxMpActivityTemplateMessageServiceImpl extends ServiceImpl<WxMpActivityTemplateMessageMapper, WxMpActivityTemplateMessage> implements IWxMpActivityTemplateMessageService {

    @Autowired
    private IWxMpService wxMpService;

    @Autowired
    private IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;

    @Autowired
    private IWxMpActivityTemplateService IWxMpActivityTemplateService;

    @Override
    public void pushAllScheduleMessageTask() {
        List<WxMp> list = wxMpService.list();
        List<WxMpActivityTemplateMessage> needPublishSchedule = new ArrayList<>();
        for (WxMp wxMp : list) {
            String appId = wxMp.getAppId();
            List<WxMpActivityTemplate> activityTemplatesByAppId = IWxMpActivityTemplateService.getActivityTemplatesByAppId(appId);
            if(activityTemplatesByAppId != null && activityTemplatesByAppId.size()>0){
                for(WxMpActivityTemplate template : activityTemplatesByAppId){
                    String templateId = template.getTemplateId();
                    List<WxMpActivityTemplateMessage> scheduleMessages = wxMpActivityTemplateMessageService.list(Wrappers.<WxMpActivityTemplateMessage>lambdaQuery()
                            .eq(WxMpActivityTemplateMessage::getTemplateId, templateId)
                            .eq(WxMpActivityTemplateMessage::getAppId, wxMp.getAppId())
                            .eq(WxMpActivityTemplateMessage::getActivityEnable,true)
                            .eq(WxMpActivityTemplateMessage::getRepType, ConfigConstant.MESSAGE_REP_TYPE_SCHEDULE));
                    needPublishSchedule.addAll(scheduleMessages);
                }
            }
        }
        for (WxMpActivityTemplateMessage wxMpActivityTemplateMessage : needPublishSchedule) {
            SchedulingRunnable task = new SchedulingRunnable(wxMpActivityTemplateMessage.getScheduleClass(), wxMpActivityTemplateMessage.getScheduleMethod(), wxMpActivityTemplateMessage.getAppId());
            cronTaskRegistrar.addCronTask(task, wxMpActivityTemplateMessage.getScheduleCron(), wxMpActivityTemplateMessage.getId());
            log.info("成功发布定时任务:messageId:[{}]", wxMpActivityTemplateMessage.getId());
        }
    }
}
