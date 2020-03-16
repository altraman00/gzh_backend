package com.ruoyi.project.weixin.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.dto.HelpInfoDTO;
import com.ruoyi.project.weixin.entity.WxActivityTask;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxTaskHelpRecord;
import com.ruoyi.project.weixin.entity.WxUser;
import com.ruoyi.project.weixin.service.IWxActivityTaskService;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxTaskHelpRecordService;
import com.ruoyi.project.weixin.service.WxUserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.framework.web.controller.BaseController;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 助力任务表 前端控制器
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */
@RestController
@RequestMapping("/open/wxtask")
@AllArgsConstructor
@Slf4j
public class WxActivityTaskController extends BaseController {

    private final WxUserService wxUserService;

    private final IWxActivityTaskService wxActivityTaskService;

    private final IWxActivityTemplateService iWxActivityTemplateService;

    private final IWxTaskHelpRecordService wxTaskHelpRecordService;

    @ApiOperation("获取助力任务完成信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="openId",value="openId",required=true,paramType="String")
    })
    @GetMapping("/help/info")
    public AjaxResult getTaskInfo(@RequestParam(value = "openId") String openId){
        WxUser wxuser = wxUserService.getByOpenId(openId);
        String wxUserId = wxuser.getId();
        WxActivityTask wxActivityTask = wxActivityTaskService.getOne(Wrappers.<WxActivityTask>lambdaQuery()
                .eq(WxActivityTask::getWxUserId, wxUserId).eq(WxActivityTask::getTemplateId, HelpActivityConstant.ACTIVITY_TEMPLATE_ID));
        String templateId = wxActivityTask.getTemplateId();
        WxActivityTemplate template = iWxActivityTemplateService.getById(templateId);
        HelpInfoDTO helpInfoDTO = new HelpInfoDTO();
        helpInfoDTO.setCompleteNum(wxActivityTask.getCompleteNum());
        helpInfoDTO.setNeedNum(template.getNeedNum());
        helpInfoDTO.setRewardUrl(template.getRewardUrl());
        helpInfoDTO.setStatus(wxActivityTask.getTaskStatus());
        // 被助力记录
        List<WxTaskHelpRecord> list = wxTaskHelpRecordService.list(Wrappers.<WxTaskHelpRecord>lambdaQuery().eq(WxTaskHelpRecord::getInviteWxUserId, wxUserId));
        List<WxUser> helpers = new ArrayList<>();
        for (WxTaskHelpRecord wxTaskHelpRecord : list) {
            WxUser wxUser = wxUserService.getById(wxTaskHelpRecord.getHelpWxUserId());
            helpers.add(wxUser);
        }
        helpInfoDTO.setHelpers(helpers);
        return AjaxResult.success(helpInfoDTO);
    }
}
