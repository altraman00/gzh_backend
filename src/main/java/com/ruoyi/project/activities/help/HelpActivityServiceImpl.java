package com.ruoyi.project.activities.help;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.mapper.WxUserMapper;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.*;
import com.ruoyi.project.weixin.utils.ObjectLockUtil;
import com.ruoyi.project.weixin.utils.SpringContextUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author VingKing
 */
@Service
@Slf4j
@AllArgsConstructor
public class HelpActivityServiceImpl implements ActivityService {

    private final IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    private final IWxTaskHelpRecordService wxTaskHelpRecordService;

    private final IWxActivityTaskService wxActivityTaskService;

    private final WxUserService wxUserService;

    private final WxUserMapper wxUserMapper;

    private final IWxMpService iWxMpService;

    private final WxSendMsgServer wxSendMsgServer;

    private IWxMpActivityTemplateService IWxMpActivityTemplateService;


    @Override
    public String getActivityServiceImplClassName() {
        String classFullName = this.getClass().getName();
        return SpringContextUtils.getCurrentClassName(classFullName);
    }

    @Override
    @Async
    public void subscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {
        String eventKey = inMessage.getEventKey();
        String appId = wxMp.getAppId();
        String templateId = template.getTemplateId();
        QueryWrapper<WxMpActivityTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(WxMpActivityTemplateMessage::getAppId, appId)
                .eq(WxMpActivityTemplateMessage::getTemplateId,templateId)
                .eq(WxMpActivityTemplateMessage::getActivityEnable,true);
        List<WxMpActivityTemplateMessage> messages = wxMpActivityTemplateMessageService.list(queryWrapper);
        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                .eq(WxUser::getOpenId,openId).eq(WxUser::getAppId,appId));
        String wxUserId = wxUser.getId();
        Integer needNum = template.getNeedNum();
        log.info("event key:[{}],openId:[{}],appId[{}]",eventKey,openId,appId);
        // 首先判断是不是扫活动码进入的
        if (StringUtils.isNotBlank(eventKey) && eventKey.contains(HelpActivityConstant.SCENE_EVENT_KEY)) {
            String inviterOpenId = eventKey.substring(eventKey.lastIndexOf(":") + 1);
            WxUser inviter = wxUserService.getByOpenIdAndAppId(inviterOpenId,appId);
            String inviterId = inviter.getId();
            // 不是自己扫自己的码进入的
            if (!inviterId.equals(wxUserId)) {
                //根据三个参数组合 得到锁对象 (不支持多节点分布式服务)
                String lockKey = inviterId + "-" + templateId + "-" + appId;
                try {
                    synchronized (ObjectLockUtil.lock(lockKey)){
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

                        log.info("help activity task-> user {} has complete {} , need num is : {}",inviterOpenId,wxActivityTask.getCompleteNum(),needNum);

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
                        } else {
                            // 邀请者已完成任务
                            executeHasComplete(messages,wxUser);
                        }
                    }
                }catch (Exception e){
                    log.error("助力异常 当前用户openId:{} lockKey:{}", openId, lockKey, e);
                }finally {
                    ObjectLockUtil.unlock(lockKey);
                }
            }
        }
        // 推送活动规则消息
        executeActivityRule(messages,wxUser,templateId,appId);
        // 推送活动海报
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_ACTIVITY_POSTER)).findFirst().orElse(null);

        wxSendMsgServer.sendPosterMessage(message,wxUser);
    }

    @Override
    public void unsubscrib(WxMpXmlMessage inMessage, WxMp wxMp, WxMpActivityTemplate template, String openId) {

    }

    private void executeHasComplete(List<WxMpActivityTemplateMessage> messages, WxUser wxUser) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_HAS_COMPLETE);
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_HAS_COMPLETE)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            wxSendMsgServer.sendTextMessage(content,wxUser);
        }
    }


    private void executeActivityRule(List<WxMpActivityTemplateMessage> messages, WxUser wxUser, String templateId, String appId) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_ACTIVITY_RULE);
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_ACTIVITY_RULE)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_SUBSCRIBE_NICKNAME,wxUser.getNickName());
            wxSendMsgServer.sendTextMessage(content,wxUser);
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

    private void executeHasHelp(List<WxMpActivityTemplateMessage> messages, WxUser wxUser, WxUser inviter) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_HAS_HELP);
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_HAS_HELP)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_INVITER_NICKNAME,inviter.getNickName());
            wxSendMsgServer.sendTextMessage(content,wxUser);
        }
    }

    private void executeBeHelped(List<WxMpActivityTemplateMessage> messages, WxUser wxUser, WxUser inviter, WxActivityTask wxActivityTask, Integer needNum) {
        log.info("开始执行助理活动流程：{}",HelpActivityConstant.SCENE_BE_HELPED);
        if (wxActivityTask.getCompleteNum() < needNum) {
            WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_BE_HELPED)).findFirst().orElse(null);
            boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
            if (hasAvailableMessage) {
                String content = message.getRepContent();
                content = content.replace(HelpActivityConstant.PLACEHOLDER_BE_RECOMMEND_NICKNAME,wxUser.getNickName()).replace(HelpActivityConstant.PLACEHOLDER_LACK_NUM,needNum- wxActivityTask.getCompleteNum()+"");
                wxSendMsgServer.sendTextMessage(content,inviter);
            }
        } else {
            WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_TASK_COMPLETE)).findFirst().orElse(null);
            boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
            if (hasAvailableMessage) {
                String content = message.getRepContent();
                wxSendMsgServer.sendTextMessage(content,inviter);
            }
        }
    }

    private void executeHelpSuccess(List<WxMpActivityTemplateMessage> list, WxUser wxUser, WxUser inviter, WxActivityTask wxActivityTask, Integer needNum) {
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
        wxTaskHelpRecord.setWxUserTaskId(wxActivityTask.getId());
        wxTaskHelpRecordService.save(wxTaskHelpRecord);
        // 推送助力成功消息
        WxMpActivityTemplateMessage message = list.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_HELP_SUCCESS)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            String content = message.getRepContent();
            content = content.replace(HelpActivityConstant.PLACEHOLDER_INVITER_NICKNAME,inviter.getNickName());
            wxSendMsgServer.sendTextMessage(content,wxUser);
        }
    }

    public void sendInviteMessage(String appId) {

        WxMpActivityTemplate wxMpActivityTemplate = IWxMpActivityTemplateService.findActivityTemplateByAppIdAndAlias(appId,HelpActivityConstant.SCENE_EVENT_KEY);

        if (!wxMpActivityTemplate.isActivityEnable()) {
            log.info("appId:[{}]已暂停活动，流程结束",appId);
            return;
        }

        QueryWrapper<WxMpActivityTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(WxMpActivityTemplateMessage::getAppId, appId)
                .eq(WxMpActivityTemplateMessage::getActivityEnable,true)
                .eq(WxMpActivityTemplateMessage::getTemplateId, wxMpActivityTemplate.getTemplateId());
        List<WxMpActivityTemplateMessage> messages = wxMpActivityTemplateMessageService.list(queryWrapper);
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_SCHEDULE_INVITER)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent());
        if (hasAvailableMessage) {
            List<WxUser> users =  wxUserMapper.getNotCompleteUser(appId, wxMpActivityTemplate.getTemplateId());
            log.info("共查询到：{}个需要发送消息的用户",users.size());
            String content = message.getRepContent();
            for (WxUser wxUser : users) {
                wxSendMsgServer.sendTextMessage(content,wxUser);
            }
        }
    }
}
