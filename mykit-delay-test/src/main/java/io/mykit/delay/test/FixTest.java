/**
 * Copyright 2019-2999 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mykit.delay.test;

import io.mykit.delay.common.utils.JobIdGenerator;
import io.mykit.delay.queue.JobMsg;
import io.mykit.delay.queue.redis.JobWrapp;
import io.mykit.delay.queue.redis.RedisQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description 测试添加任务
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FixTest {
    @Autowired
    private RedisQueue redisQueue;

    @Test
    public void pushTest() {
        long time = 1000 * (60 * new Random().nextInt(2) + 1);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int src = calendar.get(Calendar.SECOND);
        JobMsg job = new JobWrapp();
        job.setBody(String.format("{你应该在 %s 运行}", hour + ":" + min + ":" + src));
        job.setTopic("test1".concat(new Date().getSeconds() + ""));
        job.setDelay(time);
        job.setId(JobIdGenerator.getStringId());
        redisQueue.push(job);
        System.out.println(job.getBody());
        System.out.println("执行完成...");

//         try {
//             Thread.sleep(1000L);
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }
    }

}
