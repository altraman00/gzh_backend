package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxMpTemplateMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
public interface IWxMpTemplateMessageService extends IService<WxMpTemplateMessage> {

    /**
     * 发布所有公众号所绑定的定时任务消息
     */
    void pushAllScheduleMessageTask();
}
