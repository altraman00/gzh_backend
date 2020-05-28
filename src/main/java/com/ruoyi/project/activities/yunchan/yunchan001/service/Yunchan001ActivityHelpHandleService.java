package com.ruoyi.project.activities.yunchan.yunchan001.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001HelpUserRecord;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001HelpUserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001UserStatus;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateService;
import com.ruoyi.project.weixin.service.WxUserService;
import com.ruoyi.project.weixin.utils.ObjectLockUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant.SCENE_ACTIVITY_POSTER;
import static com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant.SCENE_ACTIVITY_RULE;

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
public class Yunchan001ActivityHelpHandleService {

    private final IWxMpYunchan001HelpUserRecordService yunchan001HelpUserRecordService;

    private final IWxMpYunchan001HelpUserStatusService yunchan001HelpUserStatusService;

    private final IWxMpYunchan001UserStatusService yunchan001UserStatusService;

    private final WxUserService wxUserService;

    private final WxSendMsgServer wxSendMsgServer;

    private final IWxMpActivityTemplateService wxMpActivityTemplateService;


    /**
     * 孕产001-助力活动
     * @param inMessage
     * @param openId
     * @param appId
     * @param templateId
     * @param messages
     */
    public void activityHelp(WxMpXmlMessage inMessage, String openId, String appId, String templateId, Map<String, WxMpActivityTemplateMessage> messages) {
        log.debug("开始助力活动逻辑:{}",openId);
        WxUser inviterUser = getInviterFromEventKey(inMessage.getEventKey());;
        WxUser currentUser = wxUserService.findWxUserByOpenid(openId);
        if (inviterUser == null) {
            return;
        }
        log.debug("found inviter user : {}-{}",inviterUser.getOpenId(),inviterUser.getNickName());

        // 不是自己扫自己的码进入的
        if (!inviterUser.equals(currentUser.getId())) {
            String inviterId = inviterUser.getId();
            //根据三个参数组合 得到锁对象 (不支持多节点分布式服务)
            String lockKey = inviterId + "-" + templateId + "-" + appId;
            try {
                synchronized (ObjectLockUtil.lock(lockKey)) {

                    WxMpYunchan001HelpUserStatus wxMpYunchan001HelpUserStatus = getWxMpYunchan001HelpUserStatus(appId, templateId, inviterId);
                    WxMpActivityTemplate template = wxMpActivityTemplateService.findActivityTemplateByAppIdAndTemplateId(appId,templateId);
                    Integer needNum = template.getNeedNum();
                    if (wxMpYunchan001HelpUserStatus.getCompleteNum() < template.getNeedNum()) {
                        //查找助力记录,一个人只能助力一次
                        List<WxMpYunchan001HelpUserRecord> records = yunchan001HelpUserRecordService.list(Wrappers.<WxMpYunchan001HelpUserRecord>lambdaQuery()
                                .eq(WxMpYunchan001HelpUserRecord::getHelpWxUserId, currentUser.getId()));

                        if (records.isEmpty()) {
                            // 未助力过，可以执行助力流程
                            executeHelpSuccess(messages, currentUser, inviterUser, wxMpYunchan001HelpUserStatus, needNum);
                            // 为邀请人推送助力成功消息
                            executeBeHelped(messages, currentUser, inviterUser, wxMpYunchan001HelpUserStatus, needNum);
                        } else {
                            // 已经助力过了
                            executeHasHelp(messages, currentUser, inviterUser);
                        }
                    } else {
                        // 邀请者已完成任务
                        executeHasComplete(messages, currentUser);
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
     * 获取助力用户活动状态 助力状态，如果不存在，则创建一个新的
     * @param appId
     * @param templateId
     * @param inviterId
     * @return
     */
    private WxMpYunchan001HelpUserStatus getWxMpYunchan001HelpUserStatus(String appId, String templateId, String inviterId) {
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
        return wxMpYunchan001HelpUserStatus;
    }


    /**
     *
     * @param eventKey
     * @return
     */
    private WxUser getInviterFromEventKey(String eventKey) {
        WxUser inviter = null;

        if(StringUtils.isNotEmpty(eventKey) && eventKey.indexOf("@")>=0){
            String inviterOpenId =  eventKey.substring(eventKey.indexOf("@")+1);
            inviter = wxUserService.getByOpenId(inviterOpenId);
        }
        return inviter;
    }


    /**
     * 给当前助力者发送已完成任务的消息
     *
     * @param messages
     * @param wxUser
     */
    private void executeHasComplete(Map<String,WxMpActivityTemplateMessage> messages, WxUser wxUser) {
        log.info("【yunchan001Subscrib】开始执行助理活动流程：{}", YunChan001Constant.SCENE_HAS_COMPLETE);
        WxMpActivityTemplateMessage message = messages.get(YunChan001Constant.SCENE_HAS_COMPLETE);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            wxSendMsgServer.sendTextMessage(content, wxUser);
        }
    }


    /**
     * 推送活动规则消息
     *
     * @param message
     * @param wxUser
     */
    public void executeActivityRule(WxMpActivityTemplateMessage message, WxUser wxUser) {
        log.info("【yunchan001Subscrib】开始执行助理活动流程：{}", SCENE_ACTIVITY_RULE);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(YunChan001Constant.PLACEHOLDER_SUBSCRIBE_NICKNAME, wxUser.getNickName());
            wxSendMsgServer.sendTextMessage(content, wxUser);
        }
    }


    /**
     * 已经助力过，给被推荐者发送完成助力的消息
     *
     * @param messages
     * @param wxUser
     * @param inviter
     */
    private void executeHasHelp(Map<String,WxMpActivityTemplateMessage> messages, WxUser wxUser, WxUser inviter) {
        log.info("【yunchan001Subscrib】开始执行已经助力过的活动流程：{}", YunChan001Constant.SCENE_HAS_HELP);
        WxMpActivityTemplateMessage message = messages.get(YunChan001Constant.SCENE_HAS_HELP);
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
    private void executeBeHelped(Map<String,WxMpActivityTemplateMessage> messages, WxUser wxUser, WxUser inviter, WxMpYunchan001HelpUserStatus wxActivityTask, Integer needNum) {
        log.info("【yunchan001Subscrib】开始执行发送助力成功消息的流程：{}", YunChan001Constant.SCENE_BE_HELPED);
        if (wxActivityTask.getCompleteNum() < needNum) {
            WxMpActivityTemplateMessage message = messages.get(YunChan001Constant.SCENE_BE_HELPED);
//                    messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(YunChan001Constant.SCENE_BE_HELPED)).findFirst().orElse(null);
            boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
            if (hasAvailableMessage) {
                String content = message.getRepContent();
                content = content.replace(YunChan001Constant.PLACEHOLDER_BE_RECOMMEND_NICKNAME, wxUser.getNickName()).replace(YunChan001Constant.PLACEHOLDER_LACK_NUM, needNum - wxActivityTask.getCompleteNum() + "");
                wxSendMsgServer.sendTextMessage(content, inviter);
            }
        } else {
            WxMpActivityTemplateMessage message =  messages.get(YunChan001Constant.SCENE_TASK_COMPLETE);
//                    messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(YunChan001Constant.SCENE_TASK_COMPLETE)).findFirst().orElse(null);
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
     * @param msgMap
     * @param wxUser
     * @param inviter
     * @param wxMpYunchan001HelpUserStatus
     * @param needNum
     */
    private void executeHelpSuccess(Map<String,WxMpActivityTemplateMessage> msgMap, WxUser wxUser, WxUser inviter, WxMpYunchan001HelpUserStatus wxMpYunchan001HelpUserStatus, Integer needNum) {
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
        WxMpActivityTemplateMessage message = msgMap.get(YunChan001Constant.SCENE_HELP_SUCCESS) ;
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(YunChan001Constant.PLACEHOLDER_INVITER_NICKNAME, inviter.getNickName());
            wxSendMsgServer.sendTextMessage(content, wxUser);
        }

        //帮助别人助力以后，给自己推活动规则消息:孕妈奶爸火爆推出免费一站式孕产知识免费公开课，推荐3人即可解锁该课程
        // 推送活动规则消息
        executeActivityRule(msgMap.get(SCENE_ACTIVITY_RULE),wxUser);

        // 推送活动海报
        wxSendMsgServer.sendPosterMessage(msgMap.get(SCENE_ACTIVITY_POSTER),wxUser);
    }

}
