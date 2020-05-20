package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.mapper.WxMpActivityTemplateMapper;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.service.impl
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月19日 18:34
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Service
public class WxMpActivityTemplateServiceImpl implements IWxMpActivityTemplateService {

    @Autowired
    private WxMpActivityTemplateMapper wxMpActivityTemplateMapper;

    @Autowired
    private IWxMpService wxMpService;

    @Override
    public List<WxMpActivityTemplate> getActivityTemplatesByAppId(String appId) {
        return wxMpActivityTemplateMapper.selectList(Wrappers.<WxMpActivityTemplate>lambdaQuery().eq(WxMpActivityTemplate::getAppId, appId));
    }

    @Override
    public List<WxMpActivityTemplate> getActivityTemplatesByAppIdentify(String appIdentify) {
        WxMp one = wxMpService.getOne(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getAppIdentify, appIdentify),false);
        return getActivityTemplatesByAppId(one.getAppId());
    }

    @Override
    public WxMpActivityTemplate findActivityTemplateByAppIdAndClassName(String appId, String activityClassName) {
        WxMpActivityTemplate wxMpActivityTemplate = wxMpActivityTemplateMapper.selectOne(Wrappers.<WxMpActivityTemplate>lambdaQuery()
                .eq(WxMpActivityTemplate::getAppId, appId)
                .eq(WxMpActivityTemplate::getTemplateClass, activityClassName));
        return wxMpActivityTemplate;
    }

    @Override
    public WxMpActivityTemplate findActivityTemplateByAppIdAndTemplateId(String appId, String templateId) {
        WxMpActivityTemplate wxMpActivityTemplate = wxMpActivityTemplateMapper.selectOne(Wrappers.<WxMpActivityTemplate>lambdaQuery()
                .eq(WxMpActivityTemplate::getAppId, appId)
                .eq(WxMpActivityTemplate::getTemplateId, templateId));
        return wxMpActivityTemplate;
    }


}
