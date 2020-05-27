package com.ruoyi.project.activities.yunchan.yunchan001;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.activities.security.annotation.ApiH5;
import com.ruoyi.project.activities.security.annotation.ApiH5SkipToken;
import com.ruoyi.project.activities.yunchan.yunchan001.Yunchan001ActivityServiceImpl;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.server.WxSendMsgServer;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import io.swagger.annotations.Api;
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
import java.util.Map;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.controller.yunchan.yunchan001
 * @Description : 孕产助力活动的接口
 * @Author : xiekun
 * @Create Date : 2020年05月26日 10:32
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Api(value = "WxMpYunchan001HelpController", tags = "孕产001助力 相关接口")
@ApiH5
@Slf4j
@RestController
@RequestMapping("/open/mp/yunchan001/help")
public class WxMpYunchan001HelpController {

    @Autowired
    private WxSendMsgServer wxSendMsgServer;

    @Autowired
    private Yunchan001ActivityServiceImpl yunchan001ActivityService;

    @Autowired
    private IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;


    @ApiOperation("获取助力任务海报信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="openId",value="openId",required=true,paramType="String"),
            @ApiImplicitParam(name="appId",value="appId",required=true,paramType="String")
    })
    @GetMapping("/poster")
    public AjaxResult getTaskPoster(@RequestParam(value = "openId") String openId, @RequestParam(value = "appId") String appId){

        //查询助力海报的消息模版
        WxMpActivityTemplateMessage message = wxMpActivityTemplateMessageService.findMpTemplateMessage(appId
                , yunchan001ActivityService.getActivityServiceImplClassName()
                , YunChan001Constant.SCENE_ACTIVITY_POSTER);

        boolean hasAvailableMessage = message != null && StringUtils.isNotBlank(message.getRepContent()) && StringUtils.isNotBlank(message.getRepMediaId());
        String posterBase64 = null;
        if (hasAvailableMessage) {
            Map<String,Object> result = new HashMap<>(4);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("create poster");
            //公众号二维码的场景参数
            String wxMpQrParams = YunChan001Constant.MP_QRCODE_ACTIVITY_SCENE_EVENT_KEY + openId;
            //qrCodeUrl为null时，生成的是公众号的二维码
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

    @ApiH5SkipToken
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }


}
