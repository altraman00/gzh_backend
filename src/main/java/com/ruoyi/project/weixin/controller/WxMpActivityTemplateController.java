package com.ruoyi.project.weixin.controller;


import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpTemplateMessage;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpTemplateMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.framework.web.controller.BaseController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xiekun
 * @since 2020-05-20
 */

@Api(value = "WxMpActivityTemplateController", tags = "公众号活动模版 相关接口")
@Slf4j
@RestController
@RequestMapping("/wxmp")
public class WxMpActivityTemplateController extends BaseController {

    @Autowired
    private IWxMpActivityTemplateService iWxMpActivityTemplateService;

    @Autowired
    private IWxMpTemplateMessageService wxMpTemplateMessageService;

    @ApiOperation("查询公众号绑定的活动模版")
    @GetMapping("/activity/template/list")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult getMpWxActivityTemplateList(@RequestParam(value = "appId") String appId){
        log.info("getMpWxActivityTemplateList,appId:{}",appId);
        List<WxMpActivityTemplate> activityTemplatesByAppId = iWxMpActivityTemplateService.getActivityTemplatesByAppId(appId);
        return AjaxResult.success(activityTemplatesByAppId);
    }

    @ApiOperation("启动/停止公众号绑定的活动模版")
    @PostMapping("/activity/template/{id}/do")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult operMpWxActivityTemplate(
            @PathVariable(value = "id") String id
            ,@RequestParam(value = "activityEnable") boolean activityEnable){
        log.info("operMpWxActivityTemplateList,activityEnable:{}",activityEnable);
        iWxMpActivityTemplateService.enableActivityTemplates(id,activityEnable);
        return AjaxResult.success();
    }

    @ApiOperation("删除公众号绑定的活动模版")
    @PostMapping("/activity/template/delete/{id}")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult deleteMpWxActivityTemplate(@PathVariable(value = "id") String id){
        log.info("deleteMpWxActivityTemplate，id:{}",id);
        iWxMpActivityTemplateService.deletedActivityTemplates(id);
        return AjaxResult.success();
    }

    @ApiOperation("启动/停止活动的具体某个消息")
    @PostMapping("/activity/template/msg/{id}/do")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult operMpWxActivityMsg(
             @PathVariable(value = "id") String id
            ,@RequestParam(value = "activityEnable") boolean activityEnable){
        log.info("operMpWxActivityTemplateList,activityEnable:{}",activityEnable);
        boolean update = wxMpTemplateMessageService.lambdaUpdate()
                .eq(WxMpTemplateMessage::getId, id)
                .set(WxMpTemplateMessage::isActivityEnable, activityEnable)
                .update();

        return AjaxResult.success(update);
    }

}
