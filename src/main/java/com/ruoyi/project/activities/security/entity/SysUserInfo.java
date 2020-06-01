package com.ruoyi.project.activities.security.entity;

import com.ruoyi.framework.config.swagger.IgnoreSwaggerParameter;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Project : zhaopin-cloud
 * @Package Name : com.sunlands.zhaopin.framework.security.entity
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2019年11月28日 22:04
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysUserInfo {

    @IgnoreSwaggerParameter
    private String id;

    @IgnoreSwaggerParameter
    private String openId;

    @IgnoreSwaggerParameter
    @ApiModelProperty(value = "微信昵称",hidden = true)
    private String nickName;


}
