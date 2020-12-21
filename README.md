# 作者及联系方式
作者：冰河  
QQ：2711098650  
微信：sun_shine_lyz  
微信公众号： 冰河技术

## 项目简述

Mykit体系中提供的简单、稳定、可扩展的延迟消息队列框架，提供精准的定时任务和延迟队列处理功能。


##  项目模块说明

* mykit-delay-common: mykit-delay 延迟消息队列框架通用工具模块，提供全局通用的工具类
* mykit-delay-config: mykit-delay 延迟消息队列框架通用配置模块，提供全局配置
* mykit-delay-queue:  mykit-delay 延迟消息队列框架核心实现模块，目前所有主要的功能都在此模块实现
* mykit-delay-controller: mykit-delay 延迟消息队列框架Restful接口实现模块，对外提供Restful接口访问，兼容各种语言调用
* mykit-delay-core: mykit-delay 延迟消息队列框架的入口，整个框架的启动程序在此模块实现
* mykit-delay-rpc：mykit-delay延时消息队列的RPC模块，支持Dubbo、brpc、grpc、Motan、Sofa、SpringCloud、SpringCloud Alibaba等主流RPC的实现
* mykit-delay-test: mykit-delay 延迟消息队列框架通用测试模块，主要提供Junit单元测试用例

## 需求背景

* 用户下订单后未支付，30分钟后支付超时
* 在某个时间点通知用户参加系统活动
* 业务执行失败之后隔10分钟重试一次

类似的场景比较多 简单的处理方式就是使用定时任务 假如数据比较多的时候 有的数据可能延迟比较严重,而且越来越多的定时业务导致任务调度很繁琐不好管理。

## 队列设计

整体架构设计如下图所示。

![](https://img-blog.csdnimg.cn/2020112300574198.png)

### 开发前需要考虑的问题

* 及时性 消费端能按时收到
* 同一时间消息的消费权重
* 可靠性 消息不能出现没有被消费掉的情况
* 可恢复 假如有其他情况 导致消息系统不可用了 至少能保证数据可以恢复
* 可撤回 因为是延迟消息 没有到执行时间的消息支持可以取消消费
* 高可用 多实例 这里指HA/主备模式并不是多实例同时一起工作
* 消费端如何消费

当然初步选用redis作为数据缓存的主要原因是因为redis自身支持zset的数据结构(score 延迟时间毫秒) 这样就少了排序的烦恼而且性能还很高,正好我们的需求就是按时间维度去判定执行的顺序 同时也支持map list数据结构。

### 简单定义一个消息数据结构

```
private String topic;/***topic**/
private String id;/***自动生成 全局惟一 snowflake**/
private String bizKey;
private long delay;/***延时毫秒数**/
private int priority;//优先级
private long ttl;/**消费端消费的ttl**/
private String body;/***消息体**/
private long createTime=System.currentTimeMillis();
private int status= Status.WaitPut.ordinal();
```

### 运行原理

* 用Map来存储元数据。id作为key,整个消息结构序列化(json/…)之后作为value,放入元消息池中。
* 将id放入其中(有N个)一个zset有序列表中,以createTime+delay+priority作为score。修改状态为正在延迟中
* 使用timer实时监控zset有序列表中top 10的数据 。 如果数据score<=当前时间毫秒就取出来,根据topic重新放入一个新的可消费列表(list)中,在zset中删除已经取出来的数据,并修改状态为待消费
* 客户端获取数据只需要从可消费队列中获取就可以了。并且状态必须为待消费 运行时间需要<=当前时间的 如果不满足 重新放入zset列表中,修改状态为正在延迟。如果满足修改状态为已消费。或者直接删除元数据。

### 客户端

因为涉及到不同程序语言的问题,所以当前默认支持http访问方式。

* 添加延时消息添加成功之后返回消费唯一ID POST /push {…..消息体}
* 删除延时消息 需要传递消息ID GET /delete?id=
* 恢复延时消息 GET /reStore?expire=true|false expire是否恢复已过期未执行的消息。
* 恢复单个延时消息 需要传递消息ID GET /reStore/id
* 获取消息 需要长连接 GET /get/topic

用Nginx暴露服务,配置为轮询 在添加延迟消息的时候就可以流量平均分配。

目前系统中客户端并没有采用HTTP长连接的方式来消费消息,而是采用MQ的方式来消费数据这样客户端就可以不用关心延迟消息队列。只需要在发送MQ的时候拦截一下 如果是延迟消息就用延迟消息系统处理。

### 消息可恢复

实现恢复的原理 正常情况下一般都是记录日志,比如mysql的binlog等。

这里我们直接采用mysql数据库作为记录日志。

目前创建以下2张表:

* 消息表 字段包括整个消息体
* 消息流转表 字段包括消息ID、变更状态、变更时间、zset扫描线程Name、host/ip

定义zset扫描线程Name是为了更清楚的看到消息被分发到具体哪个zset中。前提是zset的key和监控zset的线程名称要有点关系 这里也可以是zset key。

**支持消息恢复**

假如redis服务器宕机了,重启之后发现数据也没有了。所以这个恢复是很有必要的,只需要从表1也就是消息表中把消息状态不等于已消费的数据全部重新分发到延迟队列中去,然后同步一下状态就可以了。

当然恢复单个任务也可以这么干。

**数据表设计**

这里，我就直接给出创建数据表的SQL语句。SQL语句存放在mykit-delay-config模块下的`src/main/resources/sql`目录下。

```sql
DROP TABLE IF EXISTS `mykit_delay_queue_job`;
CREATE TABLE `mykit_delay_queue_job` (
  `id` varchar(128) NOT NULL,
  `bizkey` varchar(128) DEFAULT NULL,
  `topic` varchar(128) DEFAULT NULL,
  `subtopic` varchar(250) DEFAULT NULL,
  `delay` bigint(20) DEFAULT NULL,
  `create_time` bigint(20) DEFAULT NULL,
  `body` text,
  `status` int(11) DEFAULT NULL,
  `ttl` int(11) DEFAULT NULL,
  `update_time` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mykit_delay_queue_job_ID_STATUS` (`id`,`status`),
  KEY `mykit_delay_queue_job_STATUS` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for mykit_delay_queue_job_log
-- ----------------------------
DROP TABLE IF EXISTS `mykit_delay_queue_job_log`;
CREATE TABLE `mykit_delay_queue_job_log` (
  `id` varchar(128) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `thread` varchar(60) DEFAULT NULL,
  `update_time` datetime(3) DEFAULT NULL,
  `host` varchar(128) DEFAULT NULL,
  KEY `mykit_delay_queue_job_LOG_ID_STATUS` (`id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

### 关于高可用

分布式协调还是选用zookeeper。

如果有多个实例最多同时只能有1个实例工作 这样就避免了分布式竞争锁带来的坏处,当然如果业务需要多个实例同时工作也是支持的,也就是一个消息最多只能有1个实例处理,可以选用zookeeper或者redis就能实现分布式锁了。

最终做了一下测试多实例同时运行,可能因为会涉及到锁的问题性能有所下降,反而单机效果很好。所以比较推荐基于docker的主备部署模式。

## 运行模式

* 支持 master,slave （HA）需要配置`mykit.delay.registry.serverList` zk集群地址列表
* 支持 cluster 会涉及到分布式锁竞争 效果不是很明显  分布式锁采用`redis`的 `setNx`实现
* StandAlone 

目前，经过测试，**推荐使用master slave的模式**，并且，在升级版本中，进一步增强了Master Slave模式。后期会优化Cluster模式。

## 如何接入

为了提供一个统一的精准定时任务和延时队列框架，mykit-delay提供了HTTP Rest接口和RPC方式供其他业务系统调用，接口使用简单方便，只需要简单的调用接口，传递相应的参数即可。

RPC方式调用，后续支持的方式有：

* Dubbo（已实现）
* brpc（预留支持）
* grpc（预留支持）
* Motan（预留支持）
* Sofa（预留支持）
* SpringCloud（预留支持）
* SpringCloud Alibaba（预留支持）

## HTTP方式接入

### 消息体 

以JSON数据格式参数 目前提供了`http` 协议。


* body                    业务消息体
* delay                   延时毫秒 距`createTime`的间隔毫秒数
* id                         任务ID 系统自动生成 任务创建成功返回
* status                  状态 默认不填写  
* topic                     标题
* subtopic               保留字段 
* ttl                          保留字段
* createTime           创建任务时间 非必填 系统默认

### 启动HTTP Rest服务

首先，从GitHub Clone项目到本地

```bash
git clone https://github.com/sunshinelyz/mykit-delay.git
```

然后进入mykit-delay框架目录。

```bash
cd mykit-delay
```

执行Maven命令

```bash
mvn clean package -Dmaven.test.skip=true
```

接下来，进入 `mykit-delay-core` 的 `target` 目录下，运行如下命令。

```bash
java -jar mykit-delay-core-xxx.jar
```

其中，xxx是版本号，以实际下载的版本号为准。

接下来，就可以调用HTTP Restful接口来使用mykit-delay框架了。

### 添加任务


````
/push  
    POST application/json
{"body":"{hello world}","delay":10000,"id":"20","status":0,"topic":"ces","subtopic":"",ttl":12}
````

### 删除任务

删除任务 需要记录一个JobId

````
/delete?jobId=xxx
   GET
````

### 恢复单个任务

用于任务错乱 脑裂情况 根据日志恢复任务

````
/reStoreJob?JobId=xxx
   GET
````

### 恢复所有未完成的任务 

根据日志恢复任务

 ````
 /reStore?expire=true
    GET
 ````

参数`expire` 表示是否需要恢复已过期还未执行的数据

### 清空队列数据 

根据日志中未完成的数据清空队列中全部数据。清空之后 会删除缓存中的所有任务

 ```
/clearAll
  GET
 ```

## Dubbo方式接入

### 消息体 

以JSON数据格式参数 目前提供了`http` 协议。


* body                    业务消息体
* delay                   延时毫秒 距`createTime`的间隔毫秒数
* id                         任务ID 系统自动生成 任务创建成功返回
* status                  状态 默认不填写  
* topic                     标题
* subtopic               保留字段 
* ttl                          保留字段
* createTime           创建任务时间 非必填 系统默认

### 启动Dubbo服务

首先，从GitHub Clone项目到本地

```bash
git clone https://github.com/sunshinelyz/mykit-delay.git
```

然后进入mykit-delay框架目录。

```bash
cd mykit-delay
```

执行Maven命令

```bash
mvn clean package -Dmaven.test.skip=true
```

接下来，进入 `mykit-rpc-dubbo`模块下的 `mykit-rpc-dubbo-server`服务 的 `target` 目录下，运行如下命令。

```bash
mykit-rpc-dubbo-server-xxx.jar
```

其中，xxx是版本号，以实际下载的版本号为准。

### 引入mykit-delay依赖

以Dubbo方式接入mykit-delay，需要引入mykit-delay的依赖，如下所示。

```xml
<dependency>
    <groupId>io.mykit.delay</groupId>
    <artifactId>mykit-rpc-dubbo-common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

然后，在需要调用Dubbo服务的类中以如下方式注入MykitDelayDubboInterface。

```
@DubboReference(version = "1.0.0")
private MykitDelayDubboInterface mykitDelayDubboInterface;
```

其中，MykitDelayDubboInterface接口的定义如下所示。

```
/**
 * @author binghe
 * @version 1.0.0
 * @description 发布的Dubbo接口
 */
public interface MykitDelayDubboInterface {
    /**
     * 推送消息
     */
    ResponseMessage push(JobWrapp jobMsg);
    /**
     * 删除任务
     */
    ResponseMessage delete(String jobId);
    /**
     * 完成任务
     */
    ResponseMessage finish(String jobId);
    /**
     * 恢复单个任务
     */
    ResponseMessage reStoreJob(String jobId);
    /**
     * 提供一个方法 假设缓存中间件出现异常 以及数据错乱的情况 提供恢复功能
     * @param expire 过期的数据是否需要重发 true需要, false不需要 默认为true
     */
    ResponseMessage reStore(Boolean expire);
    /**
     * 清除所有的任务
     */
    ResponseMessage clearAll();
}
```

接下来，就可以以Dubbo方式接入mykit-delay框架了。

注意：无论是以HTTP方式，还是以RPC方式启动mykit-delay服务，都需要通过如下方式加载基本配置信息。

```
StartGetReady.ready(ConsumeQueueProvider.class.getName());
```

## 客户端获取队列方式

目前默认实现了`RocketMQ`与`ActiveMQ`的推送方式。依赖MQ的方式来实现延时框架与具体业务系统的解耦。同时，框架已SPI的形式加载相应的MQ，也就是说，集成MQ的方式是可扩展的。

### 消息体中消息与`RocketMQ`和 `ActiveMQ` 消息字段对应关系

| mykit-delay | RocketMQ | ActiveMQ | 备注                               |
| ----------- | -------- | -------- | ---------------------------------- |
| topic       | topic    | topic    | 点对点发送队列名称或者主题名称     |
| subtopic    | subtopic | subtopic | 点对点发送队列子名称或者主题子名称 |
| body        | 消息内容 | 消息内容 | 消息内容                           |

### 关于系统配置

延迟框架与具体执行业务系统的交互方式通过延迟框架配置实现，具体配置文件位置为mykit-delay-config项目下的`resources/properties/starter.properties`文件中。

## 测试

 需要配置好数据库地址和Redis的地址 如果不是单机模式 也需要配置好Zookeeper

 运行mykit-delay-test模块下的测试类`io.mykit.delay.test.PushTest`添加任务到队列中 

 启动mykit-delay-test模块下的`io.mykit.delay.TestDelayQueue`消费前面添加数据 为了方便查询效果 默认的消费方式是`consoleCQ` 控制台输出


# 扩展

支持zset队列个数可配置，避免大数据带来高延迟的问题。进一步增强框架的高可用。  
目前存在日志和redis元数据有可能不一致的问题 如mysql挂了,写日志不会成功，后续会通过Redis直接同步binlog的方式来解决此问题。

# 近期规划

* brpc、grpc、Motan、Sofa、SpringCloud、SpringCloud Alibaba等RPC扩展
* 支持RabbitMQ、Kafka等消息中间件
* 分区(buck)支持动态设置
* redis与数据库数据一致性的问题 （`重要`）
* 实现自己的推拉机制
* 支持可切换实现方式，目前只是依赖Redis实现，后续待优化，支持更多的可配置选项
* 支持Web控制台管理队列
* 实现消息消费`TTL`机制 
* 增加对框架和定时任务的监控


# 扩展

支持zset队列个数可配置 避免大数据带来高延迟的问题。

# 扫一扫关注微信公众号

**你在刷抖音，玩游戏的时候，别人都在这里学习，成长，提升，人与人最大的差距其实就是思维。你可能不信，优秀的人，总是在一起。** 
  
扫一扫关注冰河技术微信公众号  
![微信公众号](https://img-blog.csdnimg.cn/20200906013715889.png)  
 
