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

    private final WxMsgService wxMsgService;

    @Override
    public void execute(WxMpXmlMessage inMessage, WxMp wxMp, WxActivityTemplate template, String openId) {
        // 先判断是不是对应的扫码带参进入
        log.info("成功执行助力活动方法");
        String eventKey = inMessage.getEventKey();
        String appId = wxMp.getAppId();
        String templateId = template.getId();
        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WxMpTemplateMessage::getAppId, appId).eq(WxMpTemplateMessage::getTemplateId,templateId);
        List<WxMpTemplateMessage> messages = wxMpTemplateMessageService.list(queryWrapper);
        WxUser wxUser = wxUserService.getByOpenId(openId);
        String wxUserId = wxUser.getId();
        String inviterOpenId = eventKey.substring(eventKey.lastIndexOf(":") + 1);
        WxUser inviter = wxUserService.getByOpenId(inviterOpenId);
        String inviterId = inviter.getId();
        // 首先判断是不是扫活动码进入的
        if (StringUtils.isNotBlank(eventKey) && eventKey.contains(HelpActivityConstant.SCENE_EVENT_KEY)) {
            // 不是自己扫自己的码进入的
            if (!inviterId.equals(wxUserId)) {
                //查找助力记录,一个人可以对多个不同的好友助力一次
                List<WxTaskHelpRecord> records = wxTaskHelpRecordService.list(Wrappers.<WxTaskHelpRecord>lambdaQuery()
                        .eq(WxTaskHelpRecord::getHelpWxUserId, wxUserId).eq(WxTaskHelpRecord::getInviteWxUserId,inviterId));
                if (records.isEmpty()) {
                    // 未助力过，可以执行助力流程
                    WxTaskHelp wxTaskHelp = executeHelpSuccess(messages, wxUser, inviter);
                    // 为邀请人推送助力成功
                    executeBeHelped(messages,wxUser,inviter,wxTaskHelp);
                } else {
                    // 已经助力过了
                    executeHasHelp(messages,wxUser,inviter);
                }
            }
        }
        // 推送后续消息
        executeActivityRule(messages,wxUser);
    }

    private void executeActivityRule(List<WxMpTemplateMessage> messages, WxUser wxUser) {
        WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_ACTIVITY_RULE)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_SUBSCRIBE_NICKNAME,wxUser.getNickName());
            sendTextMessage(wxUser.getOpenId(), content,wxUser);
        }
    }

    private void executeHasHelp(List<WxMpTemplateMessage> messages, WxUser wxUser, WxUser inviter) {
        WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_HAS_HELP)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_INVITER_NICKNAME,inviter.getNickName());
            sendTextMessage(wxUser.getOpenId(), content,wxUser);
        }
    }

    private void executeBeHelped(List<WxMpTemplateMessage> messages, WxUser wxUser, WxUser inviter, WxTaskHelp wxTaskHelp) {
        if (wxTaskHelp.getHelpNum() < HelpActivityConstant.TASK_COMPLETE_NEED_NUM) {
            WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_BE_HELPED)).findFirst().orElse(null);
            boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
            if (hasAvailableMessage) {
                String content = message.getRepContent();
                content = content.replace(HelpActivityConstant.PLACEHOLDER_BE_RECOMMEND_NICKNAME,wxUser.getNickName()).replace(HelpActivityConstant.PLACEHOLDER_LACK_NUM,HelpActivityConstant.TASK_COMPLETE_NEED_NUM-wxTaskHelp.getHelpNum()+"");
                sendTextMessage(inviter.getOpenId(), content,inviter);
            }
        } else {
            WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_TASK_COMPLETE)).findFirst().orElse(null);
            boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
            if (hasAvailableMessage) {
                String content = message.getRepContent();
                sendTextMessage(inviter.getOpenId(), content,inviter);
            }
        }
    }

    private WxTaskHelp executeHelpSuccess(List<WxMpTemplateMessage> list, WxUser wxUser, WxUser inviter) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_HELP_SUCCESS);
        String wxUserId = wxUser.getId();
        String inviterId = inviter.getId();
        String openId = wxUser.getOpenId();
        // 未助力过，开始执行助力
        // 获取推荐人的openId
        WxTaskHelp wxTaskHelp = wxTaskHelpService.getOne(Wrappers.<WxTaskHelp>lambdaQuery()
                .eq(WxTaskHelp::getWxUserId, wxUserId)) ;
        if (wxTaskHelp == null) {
            wxTaskHelp = new WxTaskHelp();
            wxTaskHelp.setHelpNum(0);
            wxTaskHelp.setTaskStatus(ConfigConstant.TASK_DOING);
            wxTaskHelp.setWxUserId(inviterId);
            wxTaskHelpService.save(wxTaskHelp);
        }
        // 邀请人完成人数+1
        wxTaskHelp.setHelpNum(wxTaskHelp.getHelpNum() + 1);
        if (wxTaskHelp.getHelpNum() >= HelpActivityConstant.TASK_COMPLETE_NEED_NUM) {
            wxTaskHelp.setTaskStatus(ConfigConstant.TASK_COMPLETE);
        }
        wxTaskHelpService.updateById(wxTaskHelp);
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
            sendTextMessage(openId, content,wxUser);
        }
        return wxTaskHelp;
    }

    private void sendTextMessage(String openId, String content,WxUser wxUser) {
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
        // 记录数据库
        WxMsg wxMsg = new WxMsg();
        wxMsg.setNickName(wxUser.getNickName());
        wxMsg.setHeadimgUrl(wxUser.getHeadimgUrl());
        wxMsg.setType(ConfigConstant.WX_MSG_TYPE_2);
        wxMsg.setRepContent(wxMsg.getRepContent());
        wxMsg.setWxUserId(wxUser.getId());
        wxMsg.setRepType(ConfigConstant.MESSAGE_REP_TYPE);
        wxMsgService.save(wxMsg);
    }
}
