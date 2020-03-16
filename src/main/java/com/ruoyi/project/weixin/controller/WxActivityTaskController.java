package com.ruoyi.project.weixin.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.dto.HelpInfoDTO;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.service.*;
import com.ruoyi.project.weixin.service.impl.HelpActivityServiceImpl;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    private final IWxMpTemplateMessageService wxMpTemplateMessageService;

    private final HelpActivityServiceImpl helpActivityService;

    @ApiOperation("获取助力任务完成信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="openId",value="openId",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/help/info")
    public AjaxResult getTaskInfo(@RequestParam(value = "openId") String openId,@RequestParam(value = "appId") String appId){
        WxUser wxuser = wxUserService.getByOpenId(openId);
        String wxUserId = wxuser.getId();
        WxActivityTask wxActivityTask = wxActivityTaskService.getOne(Wrappers.<WxActivityTask>lambdaQuery()
                .eq(WxActivityTask::getWxUserId, wxUserId)
                .eq(WxActivityTask::getTemplateId, HelpActivityConstant.ACTIVITY_TEMPLATE_ID)
                .eq(WxActivityTask::getAppId,appId));
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

    @ApiOperation("获取助力任务海报信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="openId",value="openId",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/help/poster")
    public AjaxResult getTaskPoster(@RequestParam(value = "openId") String openId,@RequestParam(value = "appId") String appId){
        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WxMpTemplateMessage::getAppId, appId).eq(WxMpTemplateMessage::getTemplateId,HelpActivityConstant.ACTIVITY_TEMPLATE_ID);
        List<WxMpTemplateMessage> messages = wxMpTemplateMessageService.list(queryWrapper);
        WxMpTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_ACTIVITY_POSTER)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent()) && StringUtils.isNotBlank(message.getRepMediaId());
        String posterBase64 = null;
        if (hasAvailableMessage) {
            Map<String,Object> result = new HashMap<>(4);
            File poster = helpActivityService.getPosterFile(openId, message);
            try {
                posterBase64 = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(poster));
            } catch (IOException e) {
                log.info("将海报文件编码成base64异常",e);
            } finally {
                if (poster.exists()) {
                    poster.delete();
                }
            }
            result.put("posterBase64",posterBase64);
            String name = poster.getName();
            result.put("suffix", name.substring(name.lastIndexOf(".")+1));
            return AjaxResult.success(result);
        } else {
            return AjaxResult.error();
        }
    }
}
