package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;

import java.util.List;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.service
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月19日 18:13
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public interface IWxMpActivityTemplateService {


    /**
     * 根据appid查询所有的活动
     * @param appId
     * @return
     */
    List<WxMpActivityTemplate> getActivityTemplatesByAppId(String appId);


    /**
     * 根据活动的别名身份查询活动
     * @param appIdentify
     * @return
     */
    List<WxMpActivityTemplate> getActivityTemplatesByAppIdentify(String appIdentify);


    /**
     * 根据appid和活动实现类名查询活动
     * @param appId
     * @param activityClassName
     * @return
     */
    WxMpActivityTemplate findActivityTemplateByAppIdAndClassName(String appId, String activityClassName);


    /**
     * 根据appid和模版id查询活动
     * @param appId
     * @param templateId
     * @return
     */
    WxMpActivityTemplate findActivityTemplateByAppIdAndTemplateId(String appId, String templateId);


    /**
     * 启动或停止活动
     * @param id
     */
    void enableActivityTemplates(String id, boolean activityEnable);

    void deletedActivityTemplates(String id,boolean deletedFlag);


}
