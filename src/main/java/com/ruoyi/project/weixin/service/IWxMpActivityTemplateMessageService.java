package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

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


    /**
     * 根据appid
     * @param appId
     * @param templateId
     * @param strings
     * @return
     */
    Map<String, WxMpActivityTemplateMessage> findEnabledActivityTemplateMessages(String appId, String templateId, String[] strings);


    /**
     * 获取指定app下的指定活动的所有配置
     * @param appId
     * @param templateId
     * @return
     */
    Map<String, WxMpActivityTemplateMessage> findEnabledActivityTemplateMessages(String appId, String templateId);


    /**
     * 根据appid
     * @param appId
     * @param templateAlias
     * @param strings
     * @return
     */
    Map<String, WxMpActivityTemplateMessage> findActivityTemplateMessagesByTemplateAlias(String appId, String templateAlias, String[] strings);


    /**
     * 根据模板别名和配置项的场景值获取对应的配置项
     * @param appId
     * @param activityAliasName
     * @param sceneKey
     * @return
     */
    WxMpActivityTemplateMessage findOneActivityTemplateMessageByTemplateAlias(String appId, String activityAliasName, String sceneKey);
}
