package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class HelpActivityServiceImpl implements ActivityService {

    private final IWxMpTemplateMessageService wxMpTemplateMessageService;

    private final IWxTaskHelpRecordService wxTaskHelpRecordService;

    private final IWxTaskHelpService wxTaskHelpService;

    private final WxUserService wxUserService;

    private final WxMpService wxMpService;

    @Override
    public void execute(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId) {
        // 先判断是不是对应的扫码带参进入
        log.info("成功执行助力活动方法");
        String eventKey = inMessage.getEventKey();
        String appId = wxMp.getAppId();
        String templateId = template.getId();
        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("template_id", templateId);
        List<WxMpTemplateMessage> list = wxMpTemplateMessageService.list(queryWrapper);
        WxUser wxUser = wxUserService.getByOpenId(openId);
        String wxUserId = wxUser.getId();
        // 首先判断是不是扫活动码进入的
        if (StringUtils.isNotBlank(eventKey) && eventKey.contains(HelpActivityConstant.SCENE_EVENT_KEY)) {
            //查找助力记录
            List<WxTaskHelpRecord> records = wxTaskHelpRecordService.list(Wrappers.<WxTaskHelpRecord>lambdaQuery()
                    .eq(WxTaskHelpRecord::getHelpWxUserId, wxUserId));
            if (records.isEmpty()) {
                // 未助力过，开始执行助力
                // 获取推荐人的openId
                String inviterOpenId = eventKey.substring(eventKey.lastIndexOf(":") + 1);
                WxUser inviter = wxUserService.getByOpenId(inviterOpenId);
                WxTaskHelp wxTaskHelp = wxTaskHelpService.getOne(Wrappers.<WxTaskHelp>lambdaQuery()
                        .eq(WxTaskHelp::getWxUserId, wxUserId)) ;
                if (wxTaskHelp == null) {
                    wxTaskHelp = new WxTaskHelp();
                    wxTaskHelp.setHelpNum(0);
                    wxTaskHelp.setTaskStatus(ConfigConstant.TASK_DOING);
                    wxTaskHelp.setWxUserId(inviter.getId());
                    wxTaskHelpService.save(wxTaskHelp);
                }
                // 邀请人完成人数+1
                wxTaskHelp.setHelpNum(wxTaskHelp.getHelpNum() + 1);
                if (wxTaskHelp.getHelpNum() >= HelpActivityConstant.TASK_COMPLETE_NEED_NUM) {
                    wxTaskHelp.setTaskStatus(ConfigConstant.TASK_COMPLETE);
                }
                // 存储助力记录
                WxTaskHelpRecord wxTaskHelpRecord = new WxTaskHelpRecord();
                wxTaskHelpRecord.setHelpWxUserId(wxUserId);
                wxTaskHelpRecord.setInviteWxUserId(inviter.getId());
                wxTaskHelpRecordService.save(wxTaskHelpRecord);
                // 推送助力成功消息
                WxMpTemplateMessage message = list.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_HELP_SUCCESS)).findFirst().orElse(null);
                boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
                if (hasAvailableMessage) {
                    String content = message.getRepContent();
                    if (content.contains(HelpActivityConstant.PLACEHOLDER_INVITER_NICKNAME)) {
                        content = content.replace(HelpActivityConstant.PLACEHOLDER_INVITER_NICKNAME,inviter.getNickName());
                    }
                    sendTextMessage(openId, content);
                }
            }
        }
    }

    private void sendTextMessage(String openId, String content) {
        try {
            WxMpKefuMessage wxMpKefuMessage = WxMpKefuMessage
                    .TEXT()
                    .toUser(openId)
                    .content(content)
                    .build();
            wxMpService.getKefuService().sendKefuMessage(wxMpKefuMessage);
        } catch (Exception e) {
            log.error("发送客服消息失败，openId：{}",openId);
        }

    }
}
