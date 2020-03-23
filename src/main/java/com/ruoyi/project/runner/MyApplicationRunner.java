package com.ruoyi.project.runner;

import com.ruoyi.project.weixin.service.IWxMpTemplateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author VingKing
 */
@Component
public class MyApplicationRunner implements ApplicationRunner {

    @Autowired
    private IWxMpTemplateMessageService wxMpTemplateMessageService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        wxMpTemplateMessageService.pushAllScheduleMessageTask();
    }
}
