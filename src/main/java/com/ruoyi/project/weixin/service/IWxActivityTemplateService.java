package com.ruoyi.project.weixin.service;

import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 活动模板表 服务类
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
public interface IWxActivityTemplateService extends IService<WxActivityTemplate> {


    /**
     * 根据别名找到对应的活动模板
     * @param alias
     * @return
     */
    WxActivityTemplate findActivityTemplateByAlias(String alias);

}
