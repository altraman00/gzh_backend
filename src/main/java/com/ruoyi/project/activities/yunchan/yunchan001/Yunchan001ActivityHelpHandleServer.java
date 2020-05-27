package com.ruoyi.project.activities.yunchan.yunchan001;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001HelpUserRecord;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001HelpUserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001UserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001HelpUserRecordService;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001HelpUserStatusService;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001UserStatusService;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.utils.ObjectLockUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.activities.yunchan.yunchan001
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月25日 18:11
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Component
@Service
@Slf4j
@AllArgsConstructor
public class Yunchan001ActivityHelpHandleServer {

    private final IWxMpYunchan001HelpUserRecordService yunchan001HelpUserRecordService;

    private final IWxMpYunchan001HelpUserStatusService yunchan001HelpUserStatusService;

    private final IWxMpYunchan001UserStatusService yunchan001UserStatusService;

    private final WxUserService wxUserService;

    private final WxSendMsgServer wxSendMsgServer;

    /**
     * 孕产001-助力活动
     *
     * @param inviterOpenId
     * @param openId
     * @param appId
     * @param templateId
     * @param messages
     * @param wxUser
     * @param wxUserId
     * @param needNum
     */
    public void activityHelp(String inviterOpenId, String openId, String appId, String templateId, List<WxMpActivityTemplateMessage> messages, WxUser wxUser, String wxUserId, Integer needNum) {
        WxUser inviter = wxUserService.getByOpenIdAndAppId(inviterOpenId, appId);
        if (inviter == null) {
            return;
        }
        String inviterId = inviter.getId();
        // 不是自己扫自己的码进入的
        if (!inviterId.equals(wxUserId)) {
            //根据三个参数组合 得到锁对象 (不支持多节点分布式服务)
            String lockKey = inviterId + "-" + templateId + "-" + appId;
            try {
                synchronized (ObjectLockUtil.lock(lockKey)) {
                    WxMpYunchan001HelpUserStatus wxMpYunchan001HelpUserStatus = yunchan001HelpUserStatusService.getOne(Wrappers.<WxMpYunchan001HelpUserStatus>lambdaQuery()
                            .eq(WxMpYunchan001HelpUserStatus::getWxUserId, inviterId)
                            .eq(WxMpYunchan001HelpUserStatus::getTemplateId, templateId)
                            .eq(WxMpYunchan001HelpUserStatus::getAppId, appId));
                    if (wxMpYunchan001HelpUserStatus == null) {
                        wxMpYunchan001HelpUserStatus = new WxMpYunchan001HelpUserStatus();
                        wxMpYunchan001HelpUserStatus.setCompleteNum(0);
                        wxMpYunchan001HelpUserStatus.setTaskStatus(ConfigConstant.TASK_DOING);
                        wxMpYunchan001HelpUserStatus.setWxUserId(inviterId);
                        wxMpYunchan001HelpUserStatus.setTemplateId(templateId);
                        wxMpYunchan001HelpUserStatus.setAppId(appId);
                        yunchan001HelpUserStatusService.save(wxMpYunchan001HelpUserStatus);
                    }
                    if (wxMpYunchan001HelpUserStatus.getCompleteNum() < needNum) {
                        //查找助力记录,一个人只能助力一次
                        List<WxMpYunchan001HelpUserRecord> records = yunchan001HelpUserRecordService.list(Wrappers.<WxMpYunchan001HelpUserRecord>lambdaQuery()
                                .eq(WxMpYunchan001HelpUserRecord::getHelpWxUserId, wxUserId));
                        if (records.isEmpty()) {
                            // 未助力过，可以执行助力流程
                            executeHelpSuccess(messages, wxUser, inviter, wxMpYunchan001HelpUserStatus, needNum);
                            // 为邀请人推送助力成功消息
                            executeBeHelped(messages, wxUser, inviter, wxMpYunchan001HelpUserStatus, needNum);
                        } else {
                            // 已经助力过了
                            executeHasHelp(messages, wxUser, inviter);
                        }
                    } else {
                        // 邀请者已完成任务
                        executeHasComplete(messages, wxUser);
                    }
                }
            } catch (Exception e) {
                log.error("【yunchan001Subscrib】助力异常 当前用户openId:{} lockKey:{}", openId, lockKey, e);
            } finally {
                ObjectLockUtil.unlock(lockKey);
            }
        }
    }


    /**
     * 给当前助力者发送已完成任务的消息
     *
     * @param messages
     * @param wxUser
     */
    private void executeHasComplete(List<WxMpActivityTemplateMessage> messages, WxUser wxUser) {
        log.info("【yunchan001Subscrib】开始执行助理活动流程：{}", YunChan001Constant.SCENE_HAS_COMPLETE);
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(YunChan001Constant.SCENE_HAS_COMPLETE)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            wxSendMsgServer.sendTextMessage(content, wxUser);
        }
    }


    /**
     * 推送活动规则消息
     *
     * @param messages
     * @param wxUser
     * @param templateId
     * @param appId
     */
    public void executeActivityRule(List<WxMpActivityTemplateMessage> messages, WxUser wxUser, String templateId, String appId) {
        log.info("【yunchan001Subscrib】开始执行助理活动流程：{}", YunChan001Constant.SCENE_ACTIVITY_RULE);
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(YunChan001Constant.SCENE_ACTIVITY_RULE)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(YunChan001Constant.PLACEHOLDER_SUBSCRIBE_NICKNAME, wxUser.getNickName());
            wxSendMsgServer.sendTextMessage(content, wxUser);
        }
        // 生成助力任务信息
        String wxUserId = wxUser.getId();
        WxMpYunchan001HelpUserStatus wxMpYunchan001HelpUserStatus = yunchan001HelpUserStatusService.getOne(Wrappers.<WxMpYunchan001HelpUserStatus>lambdaQuery()
                .eq(WxMpYunchan001HelpUserStatus::getWxUserId, wxUserId)
                .eq(WxMpYunchan001HelpUserStatus::getTemplateId, templateId)
                .eq(WxMpYunchan001HelpUserStatus::getAppId, appId));
        if (wxMpYunchan001HelpUserStatus == null) {
            wxMpYunchan001HelpUserStatus = new WxMpYunchan001HelpUserStatus();
            wxMpYunchan001HelpUserStatus.setCompleteNum(0);
            wxMpYunchan001HelpUserStatus.setTaskStatus(ConfigConstant.TASK_DOING);
            wxMpYunchan001HelpUserStatus.setWxUserId(wxUserId);
            wxMpYunchan001HelpUserStatus.setTemplateId(templateId);
            wxMpYunchan001HelpUserStatus.setAppId(appId);
            yunchan001HelpUserStatusService.save(wxMpYunchan001HelpUserStatus);
        }
    }


    /**
     * 已经助力过，给被推荐者发送完成助力的消息
     *
     * @param messages
     * @param wxUser
     * @param inviter
     */
    private void executeHasHelp(List<WxMpActivityTemplateMessage> messages, WxUser wxUser, WxUser inviter) {
        log.info("【yunchan001Subscrib】开始执行已经助力过的活动流程：{}", YunChan001Constant.SCENE_HAS_HELP);
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(YunChan001Constant.SCENE_HAS_HELP)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(YunChan001Constant.PLACEHOLDER_INVITER_NICKNAME, inviter.getNickName());
            wxSendMsgServer.sendTextMessage(content, wxUser);
        }
    }


    /**
     * 助力成功后给邀请人发送"xxx帮你助力成功"的消息
     *
     * @param messages
     * @param wxUser
     * @param inviter
     * @param wxActivityTask
     * @param needNum
     */
    private void executeBeHelped(List<WxMpActivityTemplateMessage> messages, WxUser wxUser, WxUser inviter, WxMpYunchan001HelpUserStatus wxActivityTask, Integer needNum) {
        log.info("【yunchan001Subscrib】开始执行发送助力成功消息的流程：{}", YunChan001Constant.SCENE_BE_HELPED);
        if (wxActivityTask.getCompleteNum() < needNum) {
            WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(YunChan001Constant.SCENE_BE_HELPED)).findFirst().orElse(null);
            boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
            if (hasAvailableMessage) {
                String content = message.getRepContent();
                content = content.replace(YunChan001Constant.PLACEHOLDER_BE_RECOMMEND_NICKNAME, wxUser.getNickName()).replace(YunChan001Constant.PLACEHOLDER_LACK_NUM, needNum - wxActivityTask.getCompleteNum() + "");
                wxSendMsgServer.sendTextMessage(content, inviter);
            }
        } else {
            WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(YunChan001Constant.SCENE_TASK_COMPLETE)).findFirst().orElse(null);
            boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
            if (hasAvailableMessage) {
                String content = message.getRepContent();
                wxSendMsgServer.sendTextMessage(content, inviter);
            }
        }
    }


    /**
     * 第一次帮助邀请人助力
     *
     * @param list
     * @param wxUser
     * @param inviter
     * @param wxMpYunchan001HelpUserStatus
     * @param needNum
     */
    private void executeHelpSuccess(List<WxMpActivityTemplateMessage> list, WxUser wxUser, WxUser inviter, WxMpYunchan001HelpUserStatus wxMpYunchan001HelpUserStatus, Integer needNum) {
        log.info("【yunchan001Subscrib】开始执行助理活动流程：{}", YunChan001Constant.SCENE_HELP_SUCCESS);
        String wxUserId = wxUser.getId();
        String inviterId = inviter.getId();
        String inviterOpenId = inviter.getOpenId();
        // 邀请人完成人数+1
        wxMpYunchan001HelpUserStatus.setCompleteNum(wxMpYunchan001HelpUserStatus.getCompleteNum() + 1);
        if (wxMpYunchan001HelpUserStatus.getCompleteNum() >= needNum) {
            wxMpYunchan001HelpUserStatus.setTaskStatus(ConfigConstant.TASK_COMPLETE);

            //更新孕产的第二阶段的解锁状态
            yunchan001UserStatusService.lambdaUpdate()
                    .eq(WxMpYunchan001UserStatus::getOpenId,inviterOpenId)
                    .set(WxMpYunchan001UserStatus::getSecondStageStatus,YunChan001Constant.STAGE_STATUS_COMPLETED)
                    .set(WxMpYunchan001UserStatus::getSecondStageUnlockTime, LocalDateTime.now())
                    .update();

        }
        yunchan001HelpUserStatusService.updateById(wxMpYunchan001HelpUserStatus);
        // 存储助力记录
        WxMpYunchan001HelpUserRecord wxMpYunchan001HelpUserRecord = new WxMpYunchan001HelpUserRecord();
        wxMpYunchan001HelpUserRecord.setHelpWxUserId(wxUserId);
        wxMpYunchan001HelpUserRecord.setInviteWxUserId(inviterId);
        wxMpYunchan001HelpUserRecord.setYunchan001HelpUserStatusId(wxMpYunchan001HelpUserStatus.getId());
        yunchan001HelpUserRecordService.save(wxMpYunchan001HelpUserRecord);
        // 推送助力成功消息
        WxMpActivityTemplateMessage message = list.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(YunChan001Constant.SCENE_HELP_SUCCESS)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(YunChan001Constant.PLACEHOLDER_INVITER_NICKNAME, inviter.getNickName());
            wxSendMsgServer.sendTextMessage(content, wxUser);
        }
    }

}
