package com.ruoyi.project.activities.yunchan.yunchan001;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.activities.security.annotation.ApiH5;
import com.ruoyi.project.activities.security.annotation.CurrentUser;
import com.ruoyi.project.activities.security.entity.SysUserInfo;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001HelpUserRecord;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001HelpUserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001HelpUserRecordService;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001HelpUserStatusService;
import com.ruoyi.project.common.BaseResponse;
import com.ruoyi.project.common.ResultCode;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.dto.HelpInfoDTO;
import com.ruoyi.project.weixin.entity.*;
import com.ruoyi.project.weixin.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 助力记录表 前端控制器
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */

@Api(value = "WxMpYunchan001HelpUserRecordController", tags = "孕产001助力详情 相关接口")
@ApiH5
@RestController
@RequestMapping("/open/mp/yunchan001/user/record")
public class Yunchan001HelpUserRecordController extends BaseController {

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private IWxMpService iWxMpService;

    @Autowired
    private IWxMpActivityTemplateService iWxMpActivityTemplateService;

    @Autowired
    private IWxMpYunchan001HelpUserStatusService wxMpYunchan001HelpUserStatusService;

    @Autowired
    private IWxMpYunchan001HelpUserRecordService wxMpYunchan001HelpUserRecordService;

    @Autowired
    private IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    @ApiOperation("获取助力任务完成信息")
    @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    @GetMapping("/help/info")
    public AjaxResult getTaskInfo(@CurrentUser SysUserInfo sysUserInfo, @RequestParam(value = "appId") String appId){
        String openId = sysUserInfo.getOpenId();
        WxMp wxMp = iWxMpService.getByAppId(appId);
        if(wxMp != null){
            WxUser wxuser = wxUserService.getByOpenIdAndAppId(openId);

            //根据别名找模版
            WxMpActivityTemplate wxMpActivityTemplate = iWxMpActivityTemplateService.findActivityTemplateByAppIdAndAlias(appId, YunChan001Constant.ACTIVITY_ALIAS_NAME);
            String templateId = wxMpActivityTemplate.getTemplateId();
            Integer needNum = wxMpActivityTemplate.getNeedNum();
            String wxUserId = wxuser.getId();

            //查询当前助力状态
            WxMpYunchan001HelpUserStatus wxMpYunchan001HelpUserStatus = wxMpYunchan001HelpUserStatusService.getOne(Wrappers.<WxMpYunchan001HelpUserStatus>lambdaQuery()
                    .eq(WxMpYunchan001HelpUserStatus::getWxUserId, wxUserId)
                    .eq(WxMpYunchan001HelpUserStatus::getTemplateId, templateId)
                    .eq(WxMpYunchan001HelpUserStatus::getAppId,appId));

            //如果没有助力状态信息，则创建一个
            if (wxMpYunchan001HelpUserStatus == null) {
                wxMpYunchan001HelpUserStatus = new WxMpYunchan001HelpUserStatus();
                wxMpYunchan001HelpUserStatus.setCompleteNum(0);
                wxMpYunchan001HelpUserStatus.setTaskStatus(ConfigConstant.TASK_DOING);
                wxMpYunchan001HelpUserStatus.setWxUserId(wxUserId);
                wxMpYunchan001HelpUserStatus.setTemplateId(templateId);
                wxMpYunchan001HelpUserStatus.setAppId(appId);
                wxMpYunchan001HelpUserStatusService.save(wxMpYunchan001HelpUserStatus);
            }

            //
            HelpInfoDTO helpInfoDTO = new HelpInfoDTO();
            helpInfoDTO.setCompleteNum(wxMpYunchan001HelpUserStatus.getCompleteNum());
            helpInfoDTO.setStatus(wxMpYunchan001HelpUserStatus.getTaskStatus());
            helpInfoDTO.setNeedNum(wxMpActivityTemplate.getNeedNum());

            // 被助力记录
            List<WxMpYunchan001HelpUserRecord> list = wxMpYunchan001HelpUserRecordService.list(Wrappers.<WxMpYunchan001HelpUserRecord>lambdaQuery()
                    .eq(WxMpYunchan001HelpUserRecord::getYunchan001HelpUserStatusId, wxMpYunchan001HelpUserStatus.getId())
                    .orderByAsc(WxMpYunchan001HelpUserRecord::getCreateTime)
                    .last("limit 0,"+ needNum +"")
            );

            //获取所有的助力的好友的信息
            List<String> collect = list.stream().map(t -> t.getHelpWxUserId()).collect(Collectors.toList());
            List<WxUser> helpers = Lists.newArrayList();
            if(collect.size()>0){
                helpers = wxUserService.lambdaQuery().in(true,WxUser::getId, collect).list();
            }

            //获得公众号的配置数据
            Map<String, WxMpActivityTemplateMessage> messageMap = getTemplateMessage(appId, templateId);
            WxMpActivityTemplateMessage msgJpTitle = messageMap.get(YunChan001Constant.SCENE_JP_TITLE);
            WxMpActivityTemplateMessage msgJpUrl = messageMap.get(YunChan001Constant.SCENE_JP_URL);
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

}
