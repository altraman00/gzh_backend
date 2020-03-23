package com.ruoyi.project.weixin.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.system.service.ISysDictDataService;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.entity.WxActivityTemplateMessage;
import com.ruoyi.project.weixin.entity.WxMp;
import com.ruoyi.project.weixin.entity.WxMpTemplateMessage;
import com.ruoyi.project.weixin.schedule.SchedulingRunnable;
import com.ruoyi.project.weixin.schedule.config.CronTaskRegistrar;
import com.ruoyi.project.weixin.service.IWxActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpService;
import com.ruoyi.project.weixin.service.IWxMpTemplateMessageService;
import com.ruoyi.project.weixin.service.impl.HelpActivityServiceImpl;
import com.ruoyi.project.weixin.utils.ImgUtils;
import com.ruoyi.project.weixin.vo.EditWxTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.config.CronTask;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
@RequestMapping("/wxactivity")
@AllArgsConstructor
@Api("活动模板管理")
@Slf4j
public class WxActivityTemplateController extends BaseController {


    private final IWxActivityTemplateService wxActivityTemplateService;

    private final IWxMpTemplateMessageService wxMpTemplateMessageService;

    @ApiOperation("查询默认活动模板")
    @GetMapping("/template/list")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult getWxActivityTemplateList(){
        return AjaxResult.success(wxActivityTemplateService.list());
    }

    @ApiOperation("绑定活动模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name="templateId",value="活动模板id",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/template/bind")
    public AjaxResult bindWxActivityTemplate(@RequestParam(value = "templateId") String templateId,@RequestParam(value = "appId") String appId){
        WxMp wxMp = wxMpTemplateMessageService.bindWxActivityTemplate(templateId, appId);
        return AjaxResult.success(wxMp);
    }

    @ApiOperation("查询公众号绑定的活动消息详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/template/message/list")
    @PreAuthorize("@ss.hasPermi('wxmp:wxsetting:index')")
    public AjaxResult getMpTemplateMessageList(@RequestParam(value = "appId") String appId) {
        List<WxMpTemplateMessage> list = wxMpTemplateMessageService.getMpTemplateMessageList(appId);
        return AjaxResult.success(list);
    }

    @ApiOperation("编辑消息内容")
    @PatchMapping("/template/message/{messageId}")
    public AjaxResult updateMpTemplateMessage(@PathVariable("messageId") String id,@RequestBody EditWxTemplateVO editWxTemplateVO){
        WxMpTemplateMessage wxMpTemplateMessage = wxMpTemplateMessageService.updateMpTemplateMessage(id, editWxTemplateVO);
        return AjaxResult.success(wxMpTemplateMessage);
    }

    @ApiOperation("活动启动/活动暂停")
    @ApiImplicitParams({
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @PatchMapping("/status/{appId}")
    public AjaxResult editActivityStatus(@PathVariable("appId") String appId,@RequestBody EditWxTemplateVO editWxTemplateVO) {
        // 查询出公众号绑定的活动消息
        Boolean status = editWxTemplateVO.getActivityEnable();
        wxMpTemplateMessageService.editActivityStatus(appId,status);
        return AjaxResult.success();
    }

    @ApiOperation("预览海报")
    @ApiImplicitParams({
            @ApiImplicitParam(name="messageId",value="消息Id",required=true,paramType="String")
    })
    @GetMapping("/template/{messageId}/poster/preview")
    public AjaxResult previewPoster(@PathVariable("messageId") String messageId) {
        Map<String, Object> map = wxMpTemplateMessageService.previewPoster(messageId);
        return AjaxResult.success(map);
    }
}
