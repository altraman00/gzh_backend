package com.ruoyi.project.weixin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpService;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 活动模板表 前端控制器
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@RestController
@RequestMapping("/wxmp")
@AllArgsConstructor
public class WxMpController extends BaseController {

    private final IWxMpService iWxMpService;

    private final IWxActivityTemplateService iWxActivityTemplateService;

    @GetMapping("/info")
    public AjaxResult getMpInfo(){
        // 目前只支持单公众号，返回默认公众号,若改造多公众号需根据appId查询
        WxMp wxMp = iWxMpService.list().get(0);
        Map<String,Object> map = new HashMap<>(16);
        String appId = wxMp.getAppId();
        map.put("appId", appId);
        map.put("name",wxMp.getAppName());
        // 查询当前公众号配置的活动模板
        WxActivityTemplate wxActivityTemplate = null;
        if (StringUtils.isNotEmpty(wxMp.getTemplateId())) {
            wxActivityTemplate = iWxActivityTemplateService.getById(wxActivityTemplate.getId());
        }
        map.put("template",wxActivityTemplate);
        return AjaxResult.success(map);

    }
}
