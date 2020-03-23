package com.ruoyi.project.weixin.schedule.task;

import com.ruoyi.project.weixin.service.impl.HelpActivityServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author VingKing
 */
@Component("helpActivityTask")
@Slf4j
@AllArgsConstructor
public class HelpActivityTask {

    private final HelpActivityServiceImpl helpActivityService;

    public void sendInviteMessage(String appId){
        log.info("开始执行任务，三人助力活动，已关注且任务未完成的消息推送，appId:{}",appId);
        helpActivityService.sendInviteMessage(appId);
    }
}
