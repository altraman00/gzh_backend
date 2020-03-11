package com.ruoyi.project.weixin.controller;


import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.framework.web.controller.BaseController;

/**
 * <p>
 * 活动模板表 前端控制器
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@RestController
@RequestMapping("/wxactivity")
public class WxActivityTemplateController extends BaseController {

    @Autowired
    private IWxActivityTemplateService iWxActivityTemplateService;

}
