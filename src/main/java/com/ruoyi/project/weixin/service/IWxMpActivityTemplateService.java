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

    List<WxMpActivityTemplate> getActivityTemplatesByAppId(String appId);

    List<WxMpActivityTemplate> getActivityTemplatesByAppIdentify(String appIdentify);

    WxMpActivityTemplate findActivityTemplateByAppIdAndClassName(String appId, String activityClassName);

    WxMpActivityTemplate findActivityTemplateByAppIdAndTemplateId(String appId, String templateId);
}
