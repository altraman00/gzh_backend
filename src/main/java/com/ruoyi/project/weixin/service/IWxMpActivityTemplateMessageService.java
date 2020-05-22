package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
public interface IWxMpActivityTemplateMessageService extends IService<WxMpActivityTemplateMessage> {

    /**
     * 发布所有公众号所绑定的定时任务消息
     */
    void pushAllScheduleMessageTask();
}
