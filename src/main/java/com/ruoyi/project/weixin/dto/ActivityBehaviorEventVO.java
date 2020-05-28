package com.ruoyi.project.weixin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Project : bf-diabetes-testing-backend
 * @Package Name : com.sunlands.feo.diabetes.web.VO
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月12日 13:59
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Data
public class ActivityBehaviorEventVO {

    @ApiModelProperty("场景：页面浏览:scan_page；分享:share；开始测评:strat_testing；" +
            "提交测评:submit_testing；查看测评:check_testing；关注公众号:subcribe_vipcn；" +
            "关注顾问:subcribe_adviser")
    private String scene;

    @ApiModelProperty("页面地址")
    private String pageUrl;

    @ApiModelProperty("appId")
    private String appId;

    @ApiModelProperty("活动名称")
    private String activityName;

}
