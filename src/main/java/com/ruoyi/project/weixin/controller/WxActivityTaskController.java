package com.ruoyi.project.weixin.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.dto.HelpInfoDTO;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.*;
import com.ruoyi.project.activities.help.HelpActivityServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 助力任务表 前端控制器
 * </p>
 *
 * @author zhangbin
 * @since 2020-03-11
 */

@Api(value = "WxActivityTaskController", tags = "助力相关接口")
@RestController
@RequestMapping("/open/wxtask")
@AllArgsConstructor
@Slf4j
public class WxActivityTaskController extends BaseController {

    private final WxUserService wxUserService;

    private final IWxMpService iWxMpService;

    private final IWxActivityTaskService wxActivityTaskService;

    private final IWxActivityTemplateService iWxActivityTemplateService;

    private final IWxTaskHelpRecordService wxTaskHelpRecordService;

    private final IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    private final HelpActivityServiceImpl helpActivityService;

    private final WxSendMsgServer wxSendMsgServer;

    private final IWxMpActivityTemplateService IWxMpActivityTemplateService;





    @ApiOperation("获取助力任务完成信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="openId",value="openId",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/help/info")
    public AjaxResult getTaskInfo(@RequestParam(value = "openId") String openId,@RequestParam(value = "appId") String appId){
        WxMp wxMp = iWxMpService.getByAppId(appId);
        if(wxMp != null){
            WxUser wxuser = wxUserService.getByOpenIdAndAppId(openId, appId);
            WxMpActivityTemplate wxMpActivityTemplate = IWxMpActivityTemplateService.findActivityTemplateByAppIdAndAlias(appId,HelpActivityConstant.SCENE_EVENT_KEY);
            String templateId = wxMpActivityTemplate.getTemplateId();
            String wxUserId = wxuser.getId();
            WxActivityTask wxActivityTask = wxActivityTaskService.getOne(Wrappers.<WxActivityTask>lambdaQuery()
                    .eq(WxActivityTask::getWxUserId, wxUserId)
                    .eq(WxActivityTask::getTemplateId, templateId)
                    .eq(WxActivityTask::getAppId,appId));
            HelpInfoDTO helpInfoDTO = new HelpInfoDTO();
            if (wxActivityTask == null) {
                wxActivityTask = new WxActivityTask();
                wxActivityTask.setCompleteNum(0);
                wxActivityTask.setTaskStatus(ConfigConstant.TASK_DOING);
                wxActivityTask.setWxUserId(wxUserId);
                wxActivityTask.setTemplateId(templateId);
                wxActivityTask.setAppId(appId);
                wxActivityTaskService.save(wxActivityTask);
            }
            helpInfoDTO.setCompleteNum(wxActivityTask.getCompleteNum());
            helpInfoDTO.setStatus(wxActivityTask.getTaskStatus());
            helpInfoDTO.setNeedNum(wxMpActivityTemplate.getNeedNum());

            // 被助力记录
            List<WxTaskHelpRecord> list = wxTaskHelpRecordService.list(Wrappers.<WxTaskHelpRecord>lambdaQuery().eq(WxTaskHelpRecord::getWxUserTaskId, wxActivityTask.getId()));
            List<WxUser> helpers = new ArrayList<>();
            for (WxTaskHelpRecord wxTaskHelpRecord : list) {
                WxUser wxUser = wxUserService.getById(wxTaskHelpRecord.getHelpWxUserId());
                helpers.add(wxUser);
            }


            Map<String, WxMpActivityTemplateMessage> messageMap = getTemplateMessage(appId, templateId);
            WxMpActivityTemplateMessage msgJpTitle = messageMap.get(HelpActivityConstant.SCENE_JP_TITLE);
            WxMpActivityTemplateMessage msgJpUrl = messageMap.get(HelpActivityConstant.SCENE_JP_URL);
            helpInfoDTO.setJpTitleName(msgJpTitle != null? msgJpTitle.getRepContent():"");
            helpInfoDTO.setRewardUrl(msgJpUrl != null ? msgJpUrl.getRepContent():"");

            helpInfoDTO.setHelpers(helpers);
            return AjaxResult.success(helpInfoDTO);
        }else {
            logger.debug("appid没有匹配对应WxMp对象 appId:{}", appId);
            return AjaxResult.success("appId is not found:" + appId);
        }

    }

    /**
     * 获得公众号的配置数据
     * @param appId
     * @return
     */
    private Map<String, WxMpActivityTemplateMessage> getTemplateMessage(String appId, String templateId) {
//        Map<String,WxMpTemplateMessage> messageMap = new HashMap<>();
        // 查询奖品名称返回到前端
        QueryWrapper<WxMpActivityTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(WxMpActivityTemplateMessage::getAppId, appId)
                .eq(WxMpActivityTemplateMessage::getActivityEnable,true)
                .eq(WxMpActivityTemplateMessage::getTemplateId,templateId);
        List<WxMpActivityTemplateMessage> messages = wxMpActivityTemplateMessageService.list(queryWrapper);

        Map<String, WxMpActivityTemplateMessage> messageMap = messages.stream().collect(Collectors.toMap(WxMpActivityTemplateMessage::getScene, p-> p));
        return messageMap;
    }



    @ApiOperation("获取助力任务海报信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="openId",value="openId",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/help/poster")
    public AjaxResult getTaskPoster(@RequestParam(value = "openId") String openId,@RequestParam(value = "appId") String appId){
            QueryWrapper<WxMpActivityTemplateMessage> queryWrapper = new QueryWrapper<>();

        WxMpActivityTemplate wxMpActivityTemplate = IWxMpActivityTemplateService.findActivityTemplateByAppIdAndAlias(appId,HelpActivityConstant.SCENE_EVENT_KEY);
        String templateId = wxMpActivityTemplate.getTemplateId();
        queryWrapper.lambda()
                .eq(WxMpActivityTemplateMessage::getAppId, appId)
                .eq(WxMpActivityTemplateMessage::getActivityEnable,true)
                .eq(WxMpActivityTemplateMessage::getTemplateId,templateId);
        List<WxMpActivityTemplateMessage> messages = wxMpActivityTemplateMessageService.list(queryWrapper);
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene().equals(HelpActivityConstant.SCENE_ACTIVITY_POSTER)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent()) && StringUtils.isNotBlank(message.getRepMediaId());
        String posterBase64 = null;
        if (hasAvailableMessage) {
            Map<String,Object> result = new HashMap<>(4);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("create poster");
            File poster = wxSendMsgServer.getPosterFile(openId, message, appId);
            stopWatch.stop();
            try {
                stopWatch.start("encode base64");
                posterBase64 = Base64.encodeBase64String(FileUtils.readFileToByteArray(poster));
                stopWatch.stop();
                log.info(stopWatch.prettyPrint());
            } catch (IOException e) {
                log.error("将海报文件编码成base64异常",e);
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
