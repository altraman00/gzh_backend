package com.ruoyi.project.weixin.dto;

import com.ruoyi.project.weixin.entity.WxUser;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class HelpInfoDTO {

    @ApiModelProperty("完成人数")
    private Integer completeNum;

    @ApiModelProperty("任务完成需要的总数")
    private Integer needNum;

    @ApiModelProperty("资料包领取奖励的地址")
    private String rewardUrl;

    @ApiModelProperty("状态 1-进行中 2-已完成")
    private Integer status;

    @ApiModelProperty("助力者信息")
    private List<WxUser> helpers;
}
