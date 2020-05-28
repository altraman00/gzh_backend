import com.ruoyi.RuoYiApplication;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001HelpUserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001UserStatus;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001HelpUserStatusService;
import com.ruoyi.project.activities.yunchan.yunchan001.service.IWxMpYunchan001UserStatusService;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant;
import com.ruoyi.project.weixin.entity.WxActivityTask;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.schedule.SchedulingRunnable;
import com.ruoyi.project.weixin.schedule.config.CronTaskRegistrar;
import com.ruoyi.project.weixin.service.IWxActivityTaskService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.ruoyi.project.weixin.constant.yunchan.YunChan001Constant.SCENE_AIDE_TEACHER_QRCODE;

/**
 * @program: simple-demo
 * @description: 测试定时任务
 * @author: CaoTing
 * @date: 2019/5/23
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RuoYiApplication.class)
public class Yunchan001Test {
    @Autowired
    private IWxMpYunchan001HelpUserStatusService wxMpYunchan001HelpUserStatusService;

    @Autowired
    private IWxMpYunchan001UserStatusService userStatusService;

    @Autowired
    private IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    @Test
    public void testFind(){
        List<WxMpYunchan001HelpUserStatus> result = wxMpYunchan001HelpUserStatusService.list();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.size()>0);
    }

    @Test
    public void testFindFirstStage(){
        WxMpYunchan001UserStatus result = userStatusService.findUserStatusByOpenId("123456");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getWxuserId().equals("11112222"));
    }

    @Test
    public void testUnlockFirstStage(){
        WxMpYunchan001UserStatus result = userStatusService.findUserStatusByOpenId("123456");

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getFirstStageStatus().equals(WxMpYunchan001UserStatus.LOCK_STATUS_LOCKED));
        userStatusService.unlockFirstStage("123456");
        WxMpYunchan001UserStatus result02 = userStatusService.findUserStatusByOpenId("123456");
        Assert.assertNotNull(result02);
        Assert.assertTrue(result02.getFirstStageStatus().equals(WxMpYunchan001UserStatus.LOCK_STATUS_UNLOCK));

    }

    @Test
    public void testFindTeacherTemplate(){
        //查询老师二维码的的list
        Map<String, WxMpActivityTemplateMessage> mpTemplateMessageMap = wxMpActivityTemplateMessageService.findActivityTemplateMessagesByTemplateAlias(
                "wxd3fc86ade86ec00d", YunChan001Constant.ACTIVITY_ALIAS_NAME,new String[]{SCENE_AIDE_TEACHER_QRCODE});
        WxMpActivityTemplateMessage mpTemplateMessage = mpTemplateMessageMap.get(YunChan001Constant.SCENE_AIDE_TEACHER_QRCODE);

        List<String> strings = Arrays.asList(mpTemplateMessage.getRepContent().split(","));
        System.out.println(strings.size());
    }

    public static void main(String[] args) {
        String x = "http://mmbiz.qpic.cn/mmbiz_jpg/edhFubIbNKIGJF9AOHI06xJzdzb7IYDCmajA34U1Ncwgg4ichMJibPn8VPMkr6COlzw1qLUXVpTibcRgAN8YT8kAQ/0?wx_fmt=jpeg";
        System.out.println(x.split(",").length);
    }

}
