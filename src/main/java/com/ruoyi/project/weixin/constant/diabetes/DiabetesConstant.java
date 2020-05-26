package com.ruoyi.project.weixin.constant.diabetes;

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

    /**活动别名，可以用于代表用户是通过yunchan001活动添加的新用户**/
    String ACTIVITY_ALIAS_NAME = "diabetes";

    String EVENT_SUBSCRIBE = "subscribe";
    String EVENT_UNSUBSCRIBE = "unsubscribe";

    /** 糖知家查看报告 /open/portal **/
    String DIABETES_TESTING_PORTAL_API = "/open/portal";

    /** 用于微信带参二维码的活动标识 **/
    String SCENE_EVENT_KEY_DIABETES_TEST_H5 = "diabetes_test_h5";

}
