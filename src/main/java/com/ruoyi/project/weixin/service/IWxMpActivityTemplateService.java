package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxMpActivityTemplete;

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

    List<WxMpActivityTemplete> getActivityTemplatesByAppId(String appId);

    List<WxMpActivityTemplete> getActivityTemplatesByAppIdentify(String appIdentify);

    WxMpActivityTemplete findActivityTemplateByAppIdAndClassName(String appId, String activityClassName);

    WxMpActivityTemplete findActivityTemplateByAppIdAndTemplateId(String appId, String templateId);
}
