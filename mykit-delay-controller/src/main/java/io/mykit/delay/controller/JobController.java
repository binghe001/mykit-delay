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
package io.mykit.delay.controller;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.mykit.delay.common.utils.IpUtils;
import io.mykit.delay.common.utils.JobIdGenerator;
import io.mykit.delay.common.utils.ResponseMessage;
import io.mykit.delay.common.utils.Status;
import io.mykit.delay.queue.JobMsg;
import io.mykit.delay.queue.redis.JobWrapp;
import io.mykit.delay.queue.redis.RdbStore;
import io.mykit.delay.queue.redis.RedisQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description 提供任务操作的Restful接口
 * <pre>
 *     提供HTTP方式操作任务
 *     1、/push 添加任务
 *     2、/delete 删除任务
 *     3、/finish 完成任务  暂未实现
 * </pre>
 */
@RestController
@RequestMapping(value = "/")
public class JobController {
    public static final Logger LOGGER    = LoggerFactory.getLogger(JobController.class);
    private static final int      PAGE_SIZE = 500;
    @Resource(name = "redisQueueImpl")
    private RedisQueue reidsQueue;
    @Resource(name = "rdbStore")
    private RdbStore store;

    @RequestMapping(value = "/push", method = RequestMethod.POST, headers = "content-type=" + MediaType.APPLICATION_JSON_VALUE)
    public ResponseMessage push(@RequestBody JobWrapp jobMsg) {
        try {
            Assert.notNull(jobMsg.getTopic(), "参数topic错误");
            if (StringUtils.isEmpty(jobMsg.getId())) {
                jobMsg.setId(createId(jobMsg));
            }
            reidsQueue.push(jobMsg);
            return ResponseMessage.ok(jobMsg.getId());
        } catch (Exception e) {
            return ResponseMessage.error(e.getMessage());
        }
    }

    private String createId(JobMsg msg) {
        List<String> r = Lists.newArrayList(msg.getTopic());
        if (!StringUtils.isEmpty(msg.getBizKey())) {
            r.add(msg.getBizKey());
        }
        r.add(IpUtils.getIp());
        r.add(JobIdGenerator.getStringId());
        return Joiner.on(":").join(r);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public ResponseMessage delete(String jobId) {
        try {
            reidsQueue.delete(jobId);
            return ResponseMessage.ok();
        } catch (Exception e) {
            return ResponseMessage.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/finish", method = RequestMethod.GET)
    public ResponseMessage finish(String jobId) {
        try {
            return ResponseMessage.error("功能暂未开放");
        } catch (Exception e) {
            return ResponseMessage.error(e.getMessage());
        }
    }

    /**
     * 恢复单个job
     */
    @RequestMapping(value = "/reStoreJob", method = RequestMethod.GET)
    public ResponseMessage reStoreJob(@RequestParam("jobId") String jobId) {
        try {
            Assert.notNull(jobId, "JobId 不能为空");
            JobMsg job = reidsQueue.getJob(jobId);
            if (job == null) {
                LOGGER.warn("Job在元数据池中不存在 {}  ", jobId);
            }
            reidsQueue.delete(jobId);
            JobMsg msg = store.getJobMyId(jobId);
            Assert.notNull(msg, "Job不存在!");
            msg.setStatus(Status.Restore.ordinal());
            LOGGER.info("正在恢复任务{}  ", msg.getId());
            reidsQueue.push(msg);
            return ResponseMessage.ok();
        } catch (Exception e) {
            return ResponseMessage.error(e.getMessage());
        }
    }

    /**
     * 提供一个方法 假设缓存中间件出现异常 以及数据错乱的情况 提供恢复功能
     *
     * @param expire 过期的数据是否需要重发 true需要, false不需要 默认为true
     */
    @RequestMapping(value = "/reStore", method = RequestMethod.GET)
    public ResponseMessage reStore(Boolean expire) {
        long startTime = System.currentTimeMillis();
        int  count     = 0;
        try {
            if (expire == null) {
                expire = true;
            }
            count = store.getNotFinshDataCount();
            LOGGER.info("正在恢复数据{}", count);
            int          pageCount = (double) count / PAGE_SIZE == 0 ? count / PAGE_SIZE : count / PAGE_SIZE + 1;
            List<JobMsg> msgs      = Lists.newArrayList();
            for (int i = 1; i <= pageCount; i++) {
                msgs.addAll(store.getNotFinshDataList((i - 1) * PAGE_SIZE, PAGE_SIZE));
            }
            //1、首先情况数据
            this.reidsQueue.clear();
            long time  = System.currentTimeMillis();
            int  index = 1;
            for (JobMsg msg : msgs) {
                if (!expire && msg.getCreateTime() + msg.getDelay() < time) {
                    continue;
                }
                msg.setStatus(Status.Restore.ordinal());
                LOGGER.info("正在恢复任务{} ({}/{}) ", msg.getId(), index, count);
                this.reidsQueue.push(msg);
                index++;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseMessage.error(e.getMessage());
        }
        long entTime = System.currentTimeMillis();

        return ResponseMessage.ok(String.format("花费了%s ms，恢复 %s 行数据", entTime - startTime, count));
    }


    @RequestMapping(value = "/clearAll", method = RequestMethod.GET)
    public ResponseMessage clearAll() {
        long startTime = System.currentTimeMillis();
        int  count     = 0;
        try {
            count = store.getNotFinshDataCount();
            LOGGER.info("正在清空队列数据 行数 {}", count);
            int          pageCount = (double) count / PAGE_SIZE == 0 ? count / PAGE_SIZE : count / PAGE_SIZE + 1;
            List<JobMsg> msgs      = Lists.newArrayList();
            for (int i = 1; i <= pageCount; i++) {
                msgs.addAll(store.getNotFinshDataList((i - 1) * PAGE_SIZE, PAGE_SIZE));
            }
            int index = 1;
            for (JobMsg msg : msgs) {
                LOGGER.info("正在删除任务{} ({}/{}) ", msg.getId(), index, count);
                reidsQueue.delete(msg.getId());
                index++;
            }
            //this.reidsQueue.clear();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseMessage.error(e.getMessage());
        }
        long entTime = System.currentTimeMillis();

        return ResponseMessage.ok(String.format("花费了%s ms，删除 %s 行数据", entTime - startTime, count));
    }
}
