package com.ruoyi.project.activities.yunchan.yunchan001;


import com.ruoyi.project.activities.security.annotation.ApiH5;
import com.ruoyi.project.activities.security.annotation.ApiH5SkipToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.framework.web.controller.BaseController;

/**
 * <p>
 * 活动任务表 前端控制器
 * </p>
 *
 * @author xiekun
 * @since 2020-05-25
 */

@ApiH5
@RestController
@RequestMapping("/open/mp/yunchan001/user/status")
public class WxMpYunchan001HelpUserStatusController extends BaseController {

    @GetMapping("/hello1")
    public String hello1(){
        return "hello";
    }

    @ApiH5SkipToken
    @GetMapping("/hello2")
    public String hello2(){
        return "hello";
    }


}
