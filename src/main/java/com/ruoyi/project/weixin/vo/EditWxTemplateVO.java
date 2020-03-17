package com.ruoyi.project.weixin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EditWxTemplateVO {

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("内容，若类型为文字，则为文字内容；若类型为海报，则为上传接口中返回的url")
    private String repContent;

    @ApiModelProperty("若类型为海报，则为上传接口中返回的MediaId")
    private String repMediaId;

    private Boolean activityEnable;


}
