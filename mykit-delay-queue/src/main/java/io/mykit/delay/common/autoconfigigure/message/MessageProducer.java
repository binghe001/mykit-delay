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
package io.mykit.delay.common.autoconfigigure.message;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.google.common.util.concurrent.*;
import io.mykit.delay.common.utils.Constants;
import io.mykit.delay.queue.JobMsg;
import io.mykit.delay.queue.core.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 消息生产者
 */
public class MessageProducer  implements Closeable {
    public static final Logger            LOGGER    = LoggerFactory.getLogger(MessageProducer.class);
    public static final ExecutorService   EXECUTORS = Executors.newFixedThreadPool(2);
    private static      DefaultMQProducer PRODUCER;
    private             String            namesrvAddr;

    /**
     * @return
     */
    public static boolean send(Job msg) {
        Assert.notNull(msg, "参数错误");
        Message message = new Message();
        message.setTopic(msg.getTopic());
        if (!StringUtils.isEmpty(msg.getSubtopic())) {
            message.setTags(msg.getSubtopic());
        }
        message.setKeys(msg.getBizKey());
        Serializable data = msg.getBody();
        if (data != null) {
            message.setBody(((String) data).getBytes(Charset.forName(Constants.CODE_UTF8)));
        } else {
            message.setBody("".getBytes(Charset.forName(Constants.CODE_UTF8)));
        }

        try {
            SendResult send = PRODUCER.send(message);
        } catch (MQClientException | MQBrokerException | RemotingException | InterruptedException e) {
            LOGGER.error(String.format("消息发送失败[%s]", message.toString()), e);
            return false;
        }
        return true;
    }

    //guava异步发送mq
    public static void sendAsyncMessage(final JobMsg job) {

        ListeningExecutorService guavaExecutor = MoreExecutors.listeningDecorator(EXECUTORS);

        final ListenableFuture<Boolean> listenableFuture = guavaExecutor.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return MessageProducer.send(job);
            }
        });
        Futures.addCallback(listenableFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean mqMessageStatus) {
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOGGER.error(throwable.getMessage());
            }
        });
    }

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    protected void init() {
        if (PRODUCER == null) {
            PRODUCER = new DefaultMQProducer("Producer");
            PRODUCER.setNamesrvAddr(namesrvAddr);
            try {
                PRODUCER.start();
            } catch (MQClientException e) {
                LOGGER.error("消息发送端初始化失败", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 关闭消息发送端，同时释放资源，由容器自动处理，程序中不能调用此方法
     */
    @Override
    public void close() throws IOException {
        if (PRODUCER != null) {
            LOGGER.info("shutdowing mq...");
            PRODUCER.shutdown();
        }
    }
}
