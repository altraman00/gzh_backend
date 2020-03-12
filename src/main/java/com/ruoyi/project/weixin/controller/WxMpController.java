package com.ruoyi.project.weixin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpTemplateMessage;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@Api("公众号相关")
@RestController
@RequestMapping("/wxmp")
@AllArgsConstructor
@Slf4j
public class WxMpController extends BaseController {

    private final IWxMpService iWxMpService;

    private final IWxActivityTemplateService iWxActivityTemplateService;

    private final WxMpService wxMpService;

    @ApiOperation("获取公众号基本信息")
    @ApiImplicitParam(name = "appIdentify", value = "公众号身份标识，目前单公众号，固定为online_study", dataType = "String",required = true)
    @GetMapping("/info")
    public AjaxResult getMpInfo(@RequestParam(value = "appIdentify") String appIdentify){
        Map<String,Object> map = new HashMap<>(16);
        // 根据定义好的公众号标识查找公众号
        QueryWrapper<WxMp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_identify",appIdentify);
        WxMp wxMp = iWxMpService.getOne(queryWrapper);
        map.put("wxMp", wxMp);
        // 查询当前公众号配置的活动模板
        WxActivityTemplate wxActivityTemplate = null;
        if (StringUtils.isNotEmpty(wxMp.getTemplateId())) {
            wxActivityTemplate = iWxActivityTemplateService.getById(wxActivityTemplate.getId());
        }
        map.put("template",wxActivityTemplate);
        return AjaxResult.success(map);
    }

    @ApiOperation("移动端微信授权")
    @ApiImplicitParam(name = "code", value = "微信授权code", dataType = "String",required = true)
    @GetMapping("/oauth2")
    public AjaxResult oauth2(@RequestParam(value = "code") String code){
        // 目前只支持单公众号，返回默认公众号,若改造多公众号需根据appId查询
        Map<String,Object> map = new HashMap<>(16);
        try {
            WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
            WxMpUser wxMpUser = wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);
            map.put("accessToken",wxMpOAuth2AccessToken);
            map.put("wxMpUser",wxMpUser);
        } catch (Exception e) {
            log.error("调用微信授权异常",e);
        }
        return AjaxResult.success(map);
    }
}
