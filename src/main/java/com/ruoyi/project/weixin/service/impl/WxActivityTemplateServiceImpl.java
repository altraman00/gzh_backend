package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.mapper.WxActivityTemplateMapper;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;

/**
 * <p>
 * 活动模板表 服务实现类
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Service
public class WxActivityTemplateServiceImpl extends ServiceImpl<WxActivityTemplateMapper, WxActivityTemplate> implements IWxActivityTemplateService {

    @Override
    public WxActivityTemplate findActivityTemplateByAlias(String alias) {
        WxActivityTemplate template = this.baseMapper.selectOne(Wrappers.<WxActivityTemplate>lambdaQuery().eq(WxActivityTemplate::getAlias,alias));
        return template;
    }
}
