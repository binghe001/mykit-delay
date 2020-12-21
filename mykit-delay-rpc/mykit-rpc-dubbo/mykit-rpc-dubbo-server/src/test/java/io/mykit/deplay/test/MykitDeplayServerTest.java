/**
 * Copyright 2020-9999 the original author or authors.
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
package io.mykit.deplay.test;

import io.mykit.delay.common.utils.Constants;
import io.mykit.delay.queue.redis.JobWrapp;
import io.mykit.delay.rpc.dubbo.MykitDelayDubboInterface;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

/**
 * @author binghe
 * @version 1.0.0
 * @description 测试Dubbo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MykitDeplayServerTest.class)
@ComponentScan(basePackages = "io.mykit.delay")
public class MykitDeplayServerTest {

    @DubboReference(version = "1.0.0")
    private MykitDelayDubboInterface mykitDelayDubboInterface;

    @BeforeClass
    public static void initClass(){
        System.setProperty(Constants.SOFT_HOME_KEY, "D:/Workspaces/mykit/mykit-delay/mykit-delay/mykit-delay-rpc/mykit-rpc-dubbo/mykit-rpc-dubbo-server/target/classes/");

    }
    @Test
    public void testPushJob(){
        JobWrapp jobWrapp = new JobWrapp();
        jobWrapp.setId(UUID.randomUUID().toString());
        jobWrapp.setBuckedName("test");
        jobWrapp.setBizKey("testCode");
        jobWrapp.setBody("测试定时调度");
        jobWrapp.setDelay(1800);
        jobWrapp.setTopic("testTopic");
        jobWrapp.setCreateTime(System.currentTimeMillis());
        mykitDelayDubboInterface.push(jobWrapp);
    }
}
