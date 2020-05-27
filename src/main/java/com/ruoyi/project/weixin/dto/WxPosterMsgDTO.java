package com.ruoyi.project.weixin.dto;

import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.dto
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月15日 18:07
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Data
public class WxPosterMsgDTO {

    @ApiModelProperty( "openId")
    private String openId;

    @ApiModelProperty( "海报消息模版")
    WxMpActivityTemplateMessage wxMpTemplateMessage;

}
