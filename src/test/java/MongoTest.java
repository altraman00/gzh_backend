import com.ruoyi.RuoYiApplication;
import com.ruoyi.project.activities.yunchan.yunchan001.entity.WxMpYunchan001Wenjuan;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RuoYiApplication.class)
public class MongoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testMongo(){
        Assert.assertNotNull(mongoTemplate);

        List<WxMpYunchan001Wenjuan> userList = new ArrayList<>();

        userList.add(new WxMpYunchan001Wenjuan("001"));
        userList.add(new WxMpYunchan001Wenjuan("002"));

        mongoTemplate.insert(userList, "bi_yunchan_001_wenjuan");



    }
}
