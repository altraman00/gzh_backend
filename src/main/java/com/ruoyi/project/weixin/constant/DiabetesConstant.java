package com.ruoyi.project.weixin.constant;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.constant
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月14日 16:27
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public interface DiabetesConstant {

    /**糖知家查看报告 /testing/report?openId=xxx**/
    String DIABETES_TESTING_REPORT_API = "/testing/report";

    /**糖知家更新公众号关注状态**/
    String DIABETES_TESTING_USER_SUBCRIBE_API = "/user/gzh/subscribe";


    /**糖知家，场景，已经关注公众号**/
    String DIABETES_SCENE_SUBSCRIBED_VIPCN_YES = "has_subscribed_vipcn";

    /**糖知家，场景，未关注公众号**/
    String DIABETES_SCENE_SUBSCRIBED_VIPCN_NO = "hasnot_subscribed_vipcn";

    /**
     * 占位信息
     */
    String DIABETES_SUBSCRIBE_NICKNAME = "${关注者微信昵称}";

    /**活动url的参数**/
    String DIABETES_SUBSCRIBE_OPENID = "${openId}";

    /**
     * 用于微信带参二维码的活动标识
     */
    String SCENE_EVENT_KEY = "diabetes_testing_activity";

}
