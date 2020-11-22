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

import io.mykit.delay.common.utils.IpUtils;
import io.mykit.delay.queue.JobMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description Rdb存储
 */
public class RdbStore {
    public static final  Logger LOGGER         = LoggerFactory.getLogger(RdbStore.class);
    private static final String TABLE_NAME     = "mykit_delay_queue_job";
    private static final String LOG_TABLE_NAME = "mykit_delay_queue_job_log";

    private DataSource dataSource;

    public RdbStore(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean insertJob(JobMsg jobMsg) {
        boolean result = false;
        String sql = "INSERT INTO `" + TABLE_NAME + "` (`id`, `topic`, `subtopic`, `delay`, `create_time`, `body`, " +
                "`status`, `ttl`, `update_time`,`bizKey`) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?);";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, jobMsg.getId());
            preparedStatement.setString(2, jobMsg.getTopic());
            preparedStatement.setString(3, jobMsg.getSubtopic());
            preparedStatement.setLong(4, jobMsg.getDelay());
            preparedStatement.setLong(5, jobMsg.getCreateTime());
            preparedStatement.setString(6, jobMsg.getBody());
            preparedStatement.setInt(7, jobMsg.getStatus());
            preparedStatement.setLong(8, jobMsg.getTtl());
            preparedStatement.setObject(9, new Date());
            preparedStatement.setString(10, jobMsg.getBizKey());
            preparedStatement.execute();
            insertLogJob(jobMsg, conn);
            result = true;
        } catch (final SQLException ex) {
            LOGGER.error("INSERT数据发生错误", ex);
        }
        return result;
    }

    public boolean updateJobsStatus(JobMsg jobMsg) {
        boolean result = false;
        String  sql    = "UPDATE  `" + TABLE_NAME + "` SET `status` =? ,  `update_time` = ? WHERE `id`=? ";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, jobMsg.getStatus());
            preparedStatement.setObject(2, new Date());
            preparedStatement.setString(3, jobMsg.getId());
            preparedStatement.execute();
            insertLogJob(jobMsg, conn);
            result = true;
        } catch (final SQLException ex) {
            LOGGER.error("UPDATE数据发生错误", ex);
        } finally {

        }
        return result;
    }

    /**
     * 添加运行规矩日志 注意 这个方法不会自动关闭数据库链接
     */
    public boolean insertLogJob(JobMsg jobMsg, Connection conn) {
        boolean result = false;
        String sql = "INSERT INTO `" + LOG_TABLE_NAME + "` (`id`, `status`,`thread`,`update_time`,`host`) "
                + "VALUES (?, ?, ?, ?,?);";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, jobMsg.getId());
            preparedStatement.setInt(2, jobMsg.getStatus());
            preparedStatement.setString(3, Thread.currentThread().getName());
            preparedStatement.setObject(4, new Date());
            preparedStatement.setString(5, IpUtils.getHostAndIp());
            preparedStatement.execute();
            result = true;
        } catch (final SQLException ex) {
            LOGGER.error("INSERT日志数据发生错误", ex);
        }
        return result;
    }

    /**
     * 获取待恢复的数据行数 这里是获取 不是已完成 并且不是已删除的数据
     */
    public int getNotFinshDataCount() {
        String countSql = "SELECT  count(1) FROM `" + TABLE_NAME + "`  where `status` <> 3 and `status` <> 4 ";
        int    count    = 0;
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(countSql)) {
            ResultSet rs = preparedStatement.executeQuery();
            if (rs != null && rs.next()) {
                count = rs.getInt(1);
            }
        } catch (final SQLException ex) {
            LOGGER.error("COUNT数据发生错误", ex);
        }
        return count;
    }

    public JobMsg getJobMyId(String jobId) {
        String sql = "SELECT  `id`,`topic`,`subtopic`,`delay`,`create_time`,`body`,`status`,`ttl`,`bizkey` FROM `" +
                TABLE_NAME + "`  " +
                "where `id` = ?";
        List<JobMsg> msgList = Lists.newArrayList();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, jobId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs != null && rs.next()) {
                String id         = rs.getString("id");
                String bizKey     = rs.getString("bizkey");
                String topic      = rs.getString("topic");
                String subtopic   = rs.getString("subtopic");
                long   delay      = rs.getLong("delay");
                long   createTime = rs.getLong("create_time");
                String body       = rs.getString("body");
                int    status     = rs.getInt("status");
                long   ttl        = rs.getLong("ttl");
                JobMsg msg        = new JobMsg();
                msg.setStatus(status);
                msg.setTtl(ttl);
                msg.setBizKey(bizKey);
                msg.setBody(body);
                msg.setTopic(topic);
                msg.setCreateTime(createTime);
                msg.setSubtopic(subtopic);
                msg.setDelay(delay);
                msg.setId(id);
                return msg;
            }
        } catch (final SQLException ex) {
            LOGGER.error("getJobMyId数据发生错误", ex);
        }
        return null;
    }

    public List<JobMsg> getNotFinshDataList(int start, int size) {
        String sql = "SELECT  `id`,`topic`,`subtopic`,`delay`,`create_time`,`body`,`status`,`ttl`,`bizkey` FROM `" +
                TABLE_NAME + "`  " +
                "where `status` <> 3 and `status` <> 4 order by create_time ,delay limit ?, ?";
        List<JobMsg> msgList = Lists.newArrayList();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, start);
            preparedStatement.setInt(2, size);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs != null && rs.next()) {
                String id         = rs.getString("id");
                String bizKey     = rs.getString("bizkey");
                String topic      = rs.getString("topic");
                String subtopic   = rs.getString("subtopic");
                long   delay      = rs.getLong("delay");
                long   createTime = rs.getLong("create_time");
                String body       = rs.getString("body");
                int    status     = rs.getInt("status");
                long   ttl        = rs.getLong("ttl");
                JobMsg msg        = new JobMsg();
                msg.setStatus(status);
                msg.setTtl(ttl);
                msg.setBizKey(bizKey);
                msg.setBody(body);
                msg.setTopic(topic);
                msg.setCreateTime(createTime);
                msg.setSubtopic(subtopic);
                msg.setDelay(delay);
                msg.setId(id);
                msgList.add(msg);
            }
        } catch (final SQLException ex) {
            LOGGER.error("getNotFinshDataList数据发生错误", ex);
        } finally {

        }
        return msgList;
    }
}
