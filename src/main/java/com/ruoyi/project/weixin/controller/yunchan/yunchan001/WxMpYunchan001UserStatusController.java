package com.ruoyi.project.weixin.controller.yunchan.yunchan001;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.weixin.entity.yunchan.yunchan001.WxMpYunchan001UserStatus;
import com.ruoyi.project.weixin.service.yunchan.yunchan001.IWxMpYunchan001UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.framework.web.controller.BaseController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */
@RestController
@RequestMapping("/open/wx-mp-yunchan001-user-status")
public class WxMpYunchan001UserStatusController extends BaseController {

    @Autowired
    private IWxMpYunchan001UserStatusService wxMpYunchan001UserStatusService;


    @GetMapping("/openId/{openId}")
    public AjaxResult test(@PathVariable(value = "openId") String openId){

        WxMpYunchan001UserStatus one = wxMpYunchan001UserStatusService.getOne(Wrappers.<WxMpYunchan001UserStatus>lambdaQuery()
                .eq(WxMpYunchan001UserStatus::getOpenId, openId), false);

        return AjaxResult.success(one);

    }


}
