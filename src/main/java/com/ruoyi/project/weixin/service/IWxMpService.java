package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxMp;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 公众号-模板关联表 服务类
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
public interface IWxMpService extends IService<WxMp> {

    WxMp getByAppId(String appId);
}
