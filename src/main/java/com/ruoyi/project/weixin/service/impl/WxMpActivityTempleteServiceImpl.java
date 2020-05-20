package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplete;
import com.ruoyi.project.weixin.mapper.WxMpActivityTempleteMapper;
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
public class WxMpActivityTempleteServiceImpl implements IWxMpActivityTemplateService {

    @Autowired
    private WxMpActivityTempleteMapper wxMpActivityTempleteMapper;

    @Autowired
    private IWxMpService wxMpService;

    @Override
    public List<WxMpActivityTemplete> getActivityTemplatesByAppId(String appId) {
        return wxMpActivityTempleteMapper.selectList(Wrappers.<WxMpActivityTemplete>lambdaQuery().eq(WxMpActivityTemplete::getAppId, appId));
    }

    @Override
    public List<WxMpActivityTemplete> getActivityTemplatesByAppIdentify(String appIdentify) {
        WxMp one = wxMpService.getOne(Wrappers.<WxMp>lambdaQuery().eq(WxMp::getAppIdentify, appIdentify),false);
        return getActivityTemplatesByAppId(one.getAppId());
    }

    @Override
    public WxMpActivityTemplete findActivityTemplateByAppIdAndClassName(String appId, String activityClassName) {
        WxMpActivityTemplete wxMpActivityTemplete = wxMpActivityTempleteMapper.selectOne(Wrappers.<WxMpActivityTemplete>lambdaQuery()
                .eq(WxMpActivityTemplete::getAppId, appId)
                .eq(WxMpActivityTemplete::getTemplateClass, activityClassName));
        return wxMpActivityTemplete;
    }

    @Override
    public WxMpActivityTemplete findActivityTemplateByAppIdAndTemplateId(String appId, String templateId) {
        WxMpActivityTemplete wxMpActivityTemplete = wxMpActivityTempleteMapper.selectOne(Wrappers.<WxMpActivityTemplete>lambdaQuery()
                .eq(WxMpActivityTemplete::getAppId, appId)
                .eq(WxMpActivityTemplete::getTemplateId, templateId));
        return wxMpActivityTemplete;
    }


}
