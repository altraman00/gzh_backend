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

    @ApiModelProperty("场景：进入活动页:entry_index；分享助力海报:share_poster；开始问卷:wenjuan_start；" +
            "提交问卷:wenjuan_commit；助力跟踪页:help_status；关注公众号:subcribe_vipcn；" +
            "关注助教:subcribe_teacher")
    private String scene;

    @ApiModelProperty("页面地址")
    private String pageUrl;

    @ApiModelProperty("appId")
    private String appId;

    @ApiModelProperty("活动名称")
    private String activityName;

}
