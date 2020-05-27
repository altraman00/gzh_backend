package com.ruoyi.project.activities.yunchan.yunchan001.vo;

import cn.hutool.json.JSONObject;
import lombok.Data;

/**
 * 问卷VO对象
 */
@Data
public class WenjuanVO {

    //提交用户openid
    private String openid;

    //answer:{"城市":"湖北武汉","年龄":20,"孕妈还是宝爸":"孕妈","孕妈目前状态":"备孕期","学习期望":["医学生理","孕期饮食"],......}
    private JSONObject answer;
}
