package com.ruoyi.project.weixin.controller;


import com.ruoyi.project.activities.security.annotation.ApiH5;
import com.ruoyi.project.activities.security.annotation.CurrentUser;
import com.ruoyi.project.activities.security.entity.SysUserInfo;
import com.ruoyi.project.common.BaseResponse;
import com.ruoyi.project.common.ResultCode;
import com.ruoyi.project.weixin.dto.ActivityBehaviorEventVO;
import com.ruoyi.project.weixin.entity.WxMpUserActivityBehavior;
import com.ruoyi.project.weixin.service.IWxMpUserActivityBehaviorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.framework.web.controller.BaseController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xiekun
 * @since 2020-05-28
 */
@Api(value = "WxMpUserActivityBehaviorController", tags = "活动的用户行为接口")
@ApiH5
@RestController
@RequestMapping("/open/wxmp/user/activity/behavior")
public class WxMpUserActivityBehaviorController extends BaseController {

    @Autowired
    private IWxMpUserActivityBehaviorService wxMpUserActivityBehaviorService;

    @ApiOperation(value = "记录事件")
    @ApiResponses(value={@ApiResponse(code=200, message="OK")})
    @PostMapping("")
    public BaseResponse<?> eventRecord(
            @CurrentUser SysUserInfo userInfo,
            @RequestBody ActivityBehaviorEventVO behaviorEventVO){
        logger.info("【behaviorEvent】userInfo:{}",userInfo);
        String openId = userInfo.getOpenId();

        logger.info("【behaviorEvent】eventVO:{}", behaviorEventVO);

        WxMpUserActivityBehavior behaviorEntity = new WxMpUserActivityBehavior();
        behaviorEntity.setOpenId(openId);
        behaviorEntity.setAppId(behaviorEventVO.getAppId());
        behaviorEntity.setScene(behaviorEventVO.getScene());
        behaviorEntity.setPageUrl(behaviorEventVO.getPageUrl());
        behaviorEntity.setActivityName(behaviorEventVO.getActivityName());
        wxMpUserActivityBehaviorService.save(behaviorEntity);
        return new BaseResponse<>(ResultCode.SUCCESS);
    }

}
