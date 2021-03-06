import com.ruoyi.RuoYiApplication;
import com.ruoyi.project.weixin.constant.ConfigConstant;
import com.ruoyi.project.weixin.entity.WxActivityTask;
import com.ruoyi.project.weixin.schedule.SchedulingRunnable;
import com.ruoyi.project.weixin.schedule.config.CronTaskRegistrar;
import com.ruoyi.project.weixin.service.IWxActivityTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @program: simple-demo
 * @description: 测试定时任务
 * @author: CaoTing
 * @date: 2019/5/23
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RuoYiApplication.class)
public class TaskTest {
    @Autowired
    private IWxActivityTaskService wxActivityTaskService;

    @Autowired
    CronTaskRegistrar cronTaskRegistrar;

    @Test
    public void testTask() throws InterruptedException {
        SchedulingRunnable task = new SchedulingRunnable("demoTask", "taskNoParams", null);
        cronTaskRegistrar.addCronTask(task, "0/10 * * * * ?","1");

        // 便于观察
        Thread.sleep(3000000);
    }

    @Test
    public void testHaveParamsTask() throws InterruptedException {
        SchedulingRunnable task = new SchedulingRunnable("demoTask", "taskWithParams", "haha", 23);
        cronTaskRegistrar.addCronTask(task, "0/10 * * * * ?","2");

        // 便于观察
        Thread.sleep(3000000);
    }

    @Test
    public void testSave() {
        WxActivityTask wxActivityTask = new WxActivityTask();
        wxActivityTask.setCompleteNum(0);
        wxActivityTask.setTaskStatus(ConfigConstant.TASK_DOING);
        wxActivityTask.setWxUserId("123");
        wxActivityTask.setTemplateId("1");
        wxActivityTask.setAppId("321");
        wxActivityTaskService.save(wxActivityTask);
        System.out.println(wxActivityTask);
    }
}
