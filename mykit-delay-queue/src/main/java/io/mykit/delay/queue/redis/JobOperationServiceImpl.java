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
package io.mykit.delay.queue.redis;

import com.google.common.collect.Lists;

import io.mykit.delay.common.utils.FastJsonConvert;
import io.mykit.delay.common.utils.NamedUtil;
import io.mykit.delay.common.utils.Status;
import io.mykit.delay.queue.JobMsg;
import io.mykit.delay.queue.redis.bucket.BucketTask;
import io.mykit.delay.queue.redis.support.RedisQueueProperties;
import io.mykit.delay.queue.redis.support.RedisSupport;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description 任务操作服务实现类
 */
public class JobOperationServiceImpl implements JobOperationService {
    private RedisSupport redisSupport;
    private RedisQueueProperties properties;

    public void setRedisSupport(RedisSupport redisSupport) {
        this.redisSupport = redisSupport;
    }

    private String getPoolName() {
        String name = NamedUtil.buildRealTimeName(properties.getPrefix(), properties.getName(), properties.getOriginPool());
        return name;
    }

    private String geReadyName() {
        String name = NamedUtil.buildRealTimeName(properties.getPrefix(), properties.getName(), properties.getReadyName());
        return name;
    }

    public JobMsg getJob(String jobId) {
        Assert.notNull(jobId, "非法参数");
        String json = redisSupport.getHashKey(getPoolName(), jobId);
        JobMsg job  = FastJsonConvert.convertJSONToObject(json, JobWrapp.class);
        return job;
    }

    @Override
    public void addJobToPool(JobMsg jobMsg) {
        redisSupport.hashPut(getPoolName(), jobMsg.getId(), FastJsonConvert.convertObjectToJSON(jobMsg));
    }

    @Override
    public void removeJobToPool(String jobId) {
        redisSupport.deleteHashKeys(getPoolName(), jobId);
    }

    @Override
    public void updateJobStatus(String jobId, Status status) {
        JobMsg msg = getJob(jobId);
        Assert.notNull(msg, String.format("JobId %s 数据已不存在", jobId));
        msg.setStatus(status.ordinal());
        addJobToPool(msg);
    }

    @Override
    public void deleteJobToPool(String jobId) {
        redisSupport.deleteHashKeys(getPoolName(), jobId);
    }

    @Override
    public void addBucketJob(String bucketName, String JobId, double score) {
        redisSupport.zadd(bucketName, JobId, score);
    }


    @Override
    public void removeBucketKey(String bucketName, String jobId) {
        redisSupport.zrem(bucketName, jobId);
    }

    @Override
    public void addReadyTime(String readyName, String jobId) {

        redisSupport.rightPush(readyName, jobId);
    }


    @Override
    public String getReadyJob() {
        return redisSupport.leftPop(geReadyName());
    }

    @Override
    public List<String> getReadyJob(int size) {
        List<String> root = redisSupport.lrange(geReadyName(), 0, size);
        if (null != root && root.size() > 0) {
            root = Lists.reverse(root);
        }
        return root;
        /*
        for(int i=0;i<size;i++){
            String job=getReadyJob();
            if(StringUtils.isEmpty(job)){
               break;
            }
            root.add(job);
        }
        */
        // return root;
    }

    @Override
    public boolean removeReadyJob(String jobId) {

        return redisSupport.lrem(getReadyJob(), jobId);
    }

    @Override
    public String getBucketTop1Job(String bucketName) {
        double      to   = Long.valueOf(System.currentTimeMillis() + BucketTask.TIME_OUT).doubleValue();
        Set<String> sets = redisSupport.zrangeByScore(bucketName, 0, to, 0, 1);
        if (sets != null && sets.size() > 0) {
            String jobMsgId = Objects.toString(sets.toArray()[0]);
            return jobMsgId;
        }
        return null;
    }

    @Override
    public List<String> getBucketTopJobs(String bucketName, int size) {
        double                                 to   = Long.valueOf(System.currentTimeMillis() + BucketTask.TIME_OUT).doubleValue();
        Set<ZSetOperations.TypedTuple<String>> sets = redisSupport.zrangeByScoreWithScores(bucketName, 0, to, 0, size);
        List<String>                           lsts = Lists.newArrayList();
        if (sets != null && sets.size() > 0) {
            Iterator<ZSetOperations.TypedTuple<String>> it = sets.iterator();
            while (it.hasNext()) {
                ZSetOperations.TypedTuple<String> curr = it.next();
                if (curr.getScore() <= System.currentTimeMillis()) {
                    lsts.add(curr.getValue());
                } else {
                    break;
                }
            }
            // String jobMsgId = Objects.toString(sets.toArray()[0]);
            return lsts;
        }
        return null;
    }

    @Override
    public void clearAll() {
        int buckSize = properties.getBucketSize();
        if (buckSize <= 0) {
            buckSize = 1;
        }
        List<String> lst = Lists.newArrayList();
        for (int i = 1; i <= buckSize; i++) {
            lst.add(NamedUtil.buildBucketName(properties.getPrefix(), properties.getName(), i));
        }
        lst.add(getPoolName());
        lst.add(geReadyName());
        redisSupport.deleteKey(lst.toArray(new String[]{}));
    }

    public void setProperties(RedisQueueProperties properties) {
        this.properties = properties;
    }
}
