package com.ruoyi.project.weixin.constant.yunchan;

/**
 * @Project : gzh_backend
 * @Package Name : com.ruoyi.project.weixin.constant.yunchan
 * @Description : TODO
 * @Author : xiekun
 * @Create Date : 2020年05月25日 16:06
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public interface YunChan001Constant {

    /**
     * 用于微信公众号带参二维码的场景标识
     * 完整 {prefix}_class_yunchan001@help#{openid}
     */
    String MP_QRCODE_SCENE_EVENT_KEY = "class_yunchan001@help#";


    /**代表用户是通过yunchan001活动添加的新用户**/
    String H5_SCENE_EVENT_KEY = "yunchan001";


    /**
     * 场景值
     */
    String SCENE_HELP_SUCCESS = "help_success";
    String SCENE_HAS_HELP = "has_help";
    String SCENE_ACTIVITY_RULE = "activity_rule";
    String SCENE_BE_HELPED = "be_helped";
    String SCENE_TASK_COMPLETE = "task_complete";
    String SCENE_ACTIVITY_POSTER = "activity_poster";
    String SCENE_SCHEDULE_INVITER = "schedule_invite";
    String SCENE_HAS_COMPLETE = "has_complete";

    String SCENE_JP_TITLE = "jp_title";
    String SCENE_JP_URL = "jp_url";

    /**
     * 占位信息
     */
    String PLACEHOLDER_INVITER_NICKNAME = "${上级好友微信昵称}";
    String PLACEHOLDER_BE_RECOMMEND_NICKNAME = "${被推荐人昵称}";
    String PLACEHOLDER_SUBSCRIBE_NICKNAME = "${关注者微信昵称}";
    String PLACEHOLDER_LACK_NUM = "${缺少个数}";

    /**
     * 活动id
     */
    String ACTIVITY_TEMPLATE_ID = "1";

    Integer POSTER_SIZE = 200 * 1024;

}
