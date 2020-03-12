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
import com.ruoyi.project.weixin.vo.EditWxTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api("活动模板管理")
public class WxActivityTemplateController extends BaseController {


    private final IWxActivityTemplateService iWxActivityTemplateService;

    private final IWxActivityTemplateMessageService iWxActivityTemplateMessageService;

    private final IWxMpTemplateMessageService iWxMpTemplateMessageService;

    private final IWxMpService iWxMpService;


    @ApiOperation("查询默认活动模板")
    @GetMapping("/template/list")
    public AjaxResult getWxActivityTemplateList(){
        return AjaxResult.success(iWxActivityTemplateService.list());
    }

    @ApiOperation("绑定活动模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name="templateId",value="活动模板id",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/template/bind")
    public AjaxResult bindWxActivityTemplate(@RequestParam(value = "templateId") String templateId,@RequestParam(value = "appId") String appId){
        WxMp wxMp = iWxMpService.getByAppId(appId);
        wxMp.setTemplateId(templateId);
        iWxMpService.save(wxMp);
        // 查询出模板详细信息
        QueryWrapper<WxActivityTemplateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_id",templateId);
        List<WxActivityTemplateMessage> list = iWxActivityTemplateMessageService.list(queryWrapper);
        for (WxActivityTemplateMessage wxActivityTemplateMessage : list) {
            // 复制到公众号模板信息表
            WxMpTemplateMessage wxMpTemplateMessage = new WxMpTemplateMessage();
            wxMpTemplateMessage.setMpAppId(appId);
            BeanUtils.copyProperties(wxActivityTemplateMessage,wxMpTemplateMessage,"id","createId","createTime","updateId","updateTime","remark","delFlag");
            iWxMpTemplateMessageService.save(wxMpTemplateMessage);
        }
        return AjaxResult.success(wxMp);
    }

    @ApiOperation("查询公众号绑定的活动消息详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
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

    @ApiOperation("编辑消息内容")
    @PatchMapping("/template/message/{id}")
    public AjaxResult updateMpTemplateMessage(@PathVariable("id") String id,@RequestBody EditWxTemplateVO editWxTemplateVO){
        WxMpTemplateMessage query = iWxMpTemplateMessageService.getById(id);
        query.setRemark(editWxTemplateVO.getRemark());
        query.setRepMediaId(editWxTemplateVO.getRepMediaId());
        query.setRepContent(editWxTemplateVO.getRepContent());
        iWxMpTemplateMessageService.save(query);
        return AjaxResult.success(query);
    }
}
