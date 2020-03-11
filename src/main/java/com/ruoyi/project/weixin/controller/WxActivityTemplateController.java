package com.ruoyi.project.weixin.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpTemplateMessage;
import com.ruoyi.project.weixin.service.IWxActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpService;
import com.ruoyi.project.weixin.service.IWxMpTemplateMessageService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@AllArgsConstructor
public class WxActivityTemplateController extends BaseController {


    private final IWxActivityTemplateService iWxActivityTemplateService;

    private final IWxActivityTemplateMessageService iWxActivityTemplateMessageService;

    private final IWxMpTemplateMessageService iWxMpTemplateMessageService;

    private final IWxMpService iWxMpService;

    /**
     * 查询默认活动模板
     * @return
     */
    @GetMapping("/template/list")
    public AjaxResult getWxActivityTemplateList(){
        return AjaxResult.success(iWxActivityTemplateService.list());
    }

    /**
     * 绑定活动模板
     * @return
     */
    @GetMapping("/template/bind")
    public AjaxResult bindWxActivityTemplate(String templateId,String appId){
        WxMp wxMp = iWxMpService.getByAppId(appId);
        wxMp.setTemplateId(templateId);
        iWxMpService.save(wxMp);
        // 查询出模板详细信息
        WxActivityTemplateMessage templateMessage = iWxActivityTemplateMessageService.getById(templateId);
        // 复制到公众号模板信息表
        WxMpTemplateMessage wxMpTemplateMessage = new WxMpTemplateMessage();
        BeanUtils.copyProperties(templateMessage,wxMpTemplateMessage,"id","createId","createTime","updateId","updateTime","remark","delFlag");
        iWxMpTemplateMessageService.save(wxMpTemplateMessage);
        return AjaxResult.success();
    }

    /**
     * 查询公众号绑定的活动消息详情
     */
    @GetMapping("/template/message/list")
    public AjaxResult getMpTemplateMessage(String appId) {
        // 查询出公众号绑定的活动消息
        WxMp wxMp = iWxMpService.getByAppId(appId);
        String templateId = wxMp.getTemplateId();
        if (StringUtils.isBlank(templateId)) {
            return AjaxResult.success();
        }
        QueryWrapper<WxMpTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",appId);
        queryWrapper.eq("template_id",templateId);
        List<WxMpTemplateMessage> list = iWxMpTemplateMessageService.list(queryWrapper);
        return AjaxResult.success(list);
    }

    @PatchMapping("/template/message/{id}")
    public AjaxResult updateMpTemplateMessage(@PathVariable("id") String id,@RequestBody WxMpTemplateMessage wxMpTemplateMessage){
        WxMpTemplateMessage query = iWxMpTemplateMessageService.getById(id);
        query.setRemark(wxMpTemplateMessage.getRemark());
        if (StringUtils.isNotBlank(wxMpTemplateMessage.getRepMediaId())) {
            // 图片消息
            wxMpTemplateMessage.setRepMediaId(wxMpTemplateMessage.getRepMediaId());
            wxMpTemplateMessage.setRepUrl(wxMpTemplateMessage.getRepUrl());
        } else {
            // 文字消息
            wxMpTemplateMessage.setRepContent(wxMpTemplateMessage.getRepContent());
        }
        return AjaxResult.success();
    }
}
