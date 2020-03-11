package com.ruoyi.project.weixin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.mapper.WxMpMapper;
import com.ruoyi.project.weixin.service.IWxMpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 公众号-模板关联表 服务实现类
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@Service
public class WxMpServiceImpl extends ServiceImpl<WxMpMapper, WxMp> implements IWxMpService {

    @Override
    public WxMp getByAppId(String appId) {
        QueryWrapper<WxMp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",appId);
        return this.getOne(queryWrapper);
    }
}
