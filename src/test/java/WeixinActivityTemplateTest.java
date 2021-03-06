import com.ruoyi.RuoYiApplication;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplate;
import com.ruoyi.project.weixin.entity.WxMpActivityTemplateMessage;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateMessageService;
import com.ruoyi.project.weixin.service.IWxMpActivityTemplateService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

/**
 * @program: simple-demo
 * @description: 测试定时任务
 * @author: CaoTing
 * @date: 2019/5/23
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RuoYiApplication.class)
public class WeixinActivityTemplateTest {
    @Autowired
    private IWxMpActivityTemplateService iWxMpActivityTemplateService;

    @Autowired
    private IWxMpActivityTemplateMessageService wxMpActivityTemplateMessageService;

    @Test
    public void testFindMasterTemplate(){
        String appid = "wxd3fc86ade86ec00d";
        WxMpActivityTemplate wxMpActivityTemplate = iWxMpActivityTemplateService.getMasterActivityTemplate(appid);
        Assert.assertNotNull(wxMpActivityTemplate);
        Assert.assertTrue(wxMpActivityTemplate.getTemplateName().equals("糖尿病风险检测拉新活动"));
    }

    @Test
    public void testFindFirstTemplate(){
        String appid = "wxd3fc86ade86ec00d";
        WxMpActivityTemplate wxMpActivityTemplate = iWxMpActivityTemplateService.getFirstAvalibleTemplate(appid);
        Assert.assertNotNull(wxMpActivityTemplate);
        Assert.assertTrue(wxMpActivityTemplate.getTemplateName().equals("五人助力"));
    }


    @Test
    public void testFindActivityTemplateMessage(){
        Map<String, WxMpActivityTemplateMessage> result = wxMpActivityTemplateMessageService.findEnabledActivityTemplateMessages("wxd3fc86ade86ec00d","4",new String[]{"activity_rule","has_complete","activity_poster"});
        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() == 3);
        WxMpActivityTemplateMessage templateMessage = result.get("has_complete");
        Assert.assertNotNull(templateMessage);

    }

}
