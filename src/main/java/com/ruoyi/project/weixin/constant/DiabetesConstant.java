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
public class DiabetesConstant {

    public final static String EVENT_SUBSCRIBE = "subscribe";
    public final static String EVENT_UNSUBSCRIBE = "unsubscribe";

    /**糖知家查看报告 /open/portal**/
    public static String DIABETES_TESTING_PORTAL_API = "/open/portal";


    /**糖知家，场景，有检测报告**/
    public static String DIABETES_SCENE_HAS_TESTING_REPORT = "has_testing_report";

    /**糖知家，场景，没有检测报告**/
    public static String DIABETES_SCENE_NOT_HAS_TESTING_REPORT = "not_has_testing_report";

    /**
     * 占位信息
     */
    public static String DIABETES_SUBSCRIBE_NICKNAME = "${关注者微信昵称}";

    /**活动url的参数**/
    public static String DIABETES_SUBSCRIBE_OPENID = "${openId}";

    /**
     * 用于微信带参二维码的活动标识
     */
    public static String SCENE_EVENT_KEY = "diabetes_testing_activity";

}
