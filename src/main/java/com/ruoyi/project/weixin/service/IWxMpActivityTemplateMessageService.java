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
     * 根据appid，活动实现类查找模版中的全部消息
     * @param appId
     * @param activityClassName
     * @return
     */
    List<WxMpActivityTemplateMessage> getMpTemplateMessage(String appId,String activityClassName);


    /**
     * 根据appid，活动实现类，和场景值查找活动消息
     * @param appId
     * @param activityClassName
     * @param scene
     * @return
     */
    WxMpActivityTemplateMessage findMpTemplateMessage(String appId,String activityClassName,String scene);


    /**
     * 根据appid
     * @param appId
     * @param templateId
     * @param strings
     * @return
     */
    Map<String, WxMpActivityTemplateMessage> findActivityTemplateMessages(String appId, String templateId, String[] strings);

    /**
     * 根据appid
     * @param appId
     * @param templateAlias
     * @param strings
     * @return
     */
    Map<String, WxMpActivityTemplateMessage> findActivityTemplateMessagesByTemplateAlias(String appId, String templateAlias, String[] strings);


}
