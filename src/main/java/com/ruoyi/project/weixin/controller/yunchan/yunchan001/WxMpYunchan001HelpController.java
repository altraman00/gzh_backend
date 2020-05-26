package com.ruoyi.project.weixin.controller.yunchan.yunchan001;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.activities.help.HelpActivityServiceImpl;
import com.ruoyi.project.weixin.constant.HelpActivityConstant;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.IWxActivityTemplateService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.controller.yunchan.yunchan001
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月26日 10:32
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Slf4j
@RestController
@RequestMapping("/open/mp/yunchan001/help")
public class WxMpYunchan001HelpController {

    @Autowired
    private WxSendMsgServer wxSendMsgServer;

    @Autowired
    private HelpActivityServiceImpl helpActivityService;

    @Autowired
    private IWxActivityTemplateService iWxActivityTemplateService;

    @Autowired
    private IWxMpActivityTemplateService iWxMpActivityTemplateService;

    @Autowired
    private IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    @ApiOperation("获取助力任务海报信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="openId",value="openId",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/help/poster")
    public AjaxResult getTaskPoster(@RequestParam(value = "openId") String openId, @RequestParam(value = "appId") String appId){
        QueryWrapper<WxMpActivityTemplateMessage> queryWrapper = new QueryWrapper<>();

        //根据appid+活动别名?
        WxActivityTemplate wxActivityTemplate = iWxActivityTemplateService.findActivityTemplateByAlias(HelpActivityConstant.SCENE_EVENT_KEY);
        WxMpActivityTemplate wxMpActivityTemplate = iWxMpActivityTemplateService.findActivityTemplateByAppIdAndAlias(appId,helpActivityService.getActivityServiceImplClassName());
        String templateId = wxMpActivityTemplate.getTemplateId();
        queryWrapper.lambda()
                .eq(WxMpActivityTemplateMessage::getAppId, appId)
                .eq(WxMpActivityTemplateMessage::getActivityEnable,true)
                .eq(WxMpActivityTemplateMessage::getTemplateId,templateId);
        List<WxMpActivityTemplateMessage> messages = wxMpActivityTemplateMessageService.list(queryWrapper);

        //从活动的众多活动消息中找到海报的素材消息，然后再按照需求生成海报
        WxMpActivityTemplateMessage message = messages.stream().filter(wxMpTemplateMessage -> wxMpTemplateMessage.getScene()
                .equals(YunChan001Constant.SCENE_ACTIVITY_POSTER)).findFirst().orElse(null);
        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent()) && StringUtils.isNotBlank(message.getRepMediaId());
        String posterBase64 = null;
        if (hasAvailableMessage) {
            Map<String,Object> result = new HashMap<>(4);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("create poster");

            String wxMpQrParams = "";

//            File poster = wxSendMsgServer.getPosterFile(openId, message, appId,null, HelpActivityConstant.SCENE_EVENT_KEY);
            //qrCodeUrl为null时，生成的是公众号的二维码
            File poster = wxSendMsgServer.getPosterFile(openId, message, appId,null, wxMpQrParams);
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
