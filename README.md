# 作者简介: 
冰河，高级软件架构师，Java编程专家，大数据架构师与编程专家，信息安全高级工程师，开源分布式消息引擎Mysum发起者、首席架构师及开发者，Mykit系列开源框架独立作者，Android开源消息组件Android-MQ独立作者，国内知名开源分布式数据库中间件Mycat核心架构师、开发者，精通Java, C, C++, Python, Hadoop大数据生态体系，熟悉MySQL、Redis内核，Android底层架构。多年来致力于分布式系统架构、微服务、分布式数据库、大数据技术的研究，曾主导过众多分布式系统、微服务及大数据项目的架构设计、研发和实施落地。在高并发、高可用、高可扩展性、高可维护性和大数据等领域拥有丰富的经验。对Hadoop、Spark、Storm等大数据框架源码进行过深度分析并具有丰富的实战经验。

# 作者联系方式
QQ：2711098650

# 项目简述
mykit-delay 冰河个人自主研发的简单、稳定、可扩展的延迟消息队列框架，提供精准的定时任务和延迟队列处理功能


#  项目模块说明
* mykit-delay-common: mykit-delay 延迟消息队列框架通用工具模块，提供全局通用的工具类
* mykit-delay-config: mykit-delay 延迟消息队列框架通用配置模块，提供全局配置
* mykit-delay-queue:  mykit-delay 延迟消息队列框架核心实现模块，目前所有主要的功能都在此模块实现
* mykit-delay-controller: mykit-delay 延迟消息队列框架Restful接口实现模块，对外提供Restful接口访问，兼容各种语言调用
* mykit-delay-core: mykit-delay 延迟消息队列框架的入口，整个框架的启动程序在此模块实现
* mykit-delay-test: mykit-delay 延迟消息队列框架通用测试模块，主要提供Junit单元测试用例

# 需求背景

* 用户下订单后未支付，30分钟后支付超时
* 在某个时间点通知用户参加系统活动
* 业务执行失败之后隔10分钟重试一次

类似的场景比较多 简单的处理方式就是使用定时任务 假如数据比较多的时候 有的数据可能延迟比较严重,而且越来越多的定时业务导致任务调度很繁琐不好管理。


# 队列设计
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
### 运行原理：
    
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

用nginx暴露服务,配置为轮询 在添加延迟消息的时候就可以流量平均分配。

目前系统中客户端并没有采用HTTP长连接的方式来消费消息,而是采用MQ的方式来消费数据这样客户端就可以不用关心延迟消息队列。只需要在发送MQ的时候拦截一下 如果是延迟消息就用延迟消息系统处理。

### 消息可恢复

实现恢复的原理 正常情况下一般都是记录日志,比如mysql的binlog等。

这里我们直接采用mysql数据库作为记录日志。

目前创建以下2张表:

* 消息表 字段包括整个消息体
* 消息流转表 字段包括消息ID、变更状态、变更时间、zset扫描线程Name、host/ip

定义zset扫描线程Name是为了更清楚的看到消息被分发到具体哪个zset中。前提是zset的key和监控zset的线程名称要有点关系 这里也可以是zset key。

#### 举个例子

假如redis服务器宕机了,重启之后发现数据也没有了。所以这个恢复是很有必要的,只需要从表1也就是消息表中把消息状态不等于已消费的数据全部重新分发到延迟队列中去,然后同步一下状态就可以了。

当然恢复单个任务也可以这么干。

### 关于高可用

分布式协调还是选用zookeeper。

如果有多个实例最多同时只能有1个实例工作 这样就避免了分布式竞争锁带来的坏处,当然如果业务需要多个实例同时工作也是支持的,也就是一个消息最多只能有1个实例处理,可以选用zookeeper或者redis就能实现分布式锁了。

最终做了一下测试多实例同时运行,可能因为会涉及到锁的问题性能有所下降,反而单机效果很好。所以比较推荐基于docker的主备部署模式。

# 运行模式

* 支持 master,slave （HA）需要配置`mykit.delay.registry.serverList` zk集群地址列表
* 支持 cluster 会涉及到分布式锁竞争 效果不是很明显  分布式锁采用`redis`的 `setNx`实现
* StandAlone 

推荐使用master slave的模式

##### Usage

#### 消息体 

以JSON数据格式参数 目前只提供了`http`协议


* body                 业务消息体
* delay                延时毫秒 距`createTime`的间隔毫秒数
* id                   任务ID 系统自动生成 任务创建成功返回
* status               状态 默认不填写  
* topic                标题
* subtopic             保留字段 
* ttl                  保留字段
* createTime           创建任务时间 非必填 系统默认

#### 添加任务
 

````
/push  
  POST application/json

{"body":"{hello world}","delay":10000,"id":"20","status":0,"topic":"ces","subtopic":"",ttl":12}
````

#### 删除任务

 删除任务 需要记录一个JobId
 
````
/delete?jobId=xxx
   GET
````
#### 恢复单个任务

 用于任务错乱 脑裂情况 根据日志恢复任务
 
````
/reStoreJob?JobId=xxx
   GET
````
#### 恢复所有未完成的任务 

  根据日志恢复任务
 
 ````
 /reStore?expire=true
    GET
 ````
 
 参数`expire` 表示是否需要恢复已过期还未执行的数据
 
#### 清空队列数据 

  根据日志中未完成的数据清空队列中全部数据
  
  `清空之后 会删除缓存中的所有任务`
 ````
 /clearAll
    GET
 ````
 

   
#### 客户端获取队列方式

目前默认实现了`RocketMQ`与`ActiveMQ`的推送方式。依赖MQ的方式来实现延时框架与具体业务系统的耦合。

##### 消息体中消息与`RocketMQ`和 `ActiveMQ` 消息字段对应关系

mykit-delay   | RocketMQ | ActiveMQ|                备注            |
---           | ---      | ---     | ---          
topic         | topic    | topic   | 点对点发送队列名称或者主题名称    |         
subtopic      | subtopic | subtopic| 点对点发送队列子名称或者主题子名称 |    
body          | 消息内容  | 消息内容 |    消息内容                     |
         
### 关于系统配置
延迟框架与具体执行业务系统的交互方式通过延迟框架配置实现，具体配置文件位置为mykit-delay-config项目下的`resources/properties/starter.properties`文件中。

## 后期优化

* 分区(buck)支持动态设置
* redis与数据库数据一致性的问题 （`重要`）
* 实现自己的推拉机制
* 支持可切换实现方式 目前只是依赖Redis实现，后续待优化
* 支持Web控制台管理队列
* 实现消息消费`TTL`机制 

## 测试
 需要配置好数据库地址和Redis的地址 如果不是单机模式 也需要配置好Zookeeper
 
 运行mykit-delay-test模块下的测试类`io.mykit.delay.test.PushTest`添加任务到队列中 
 
 启动mykit-delay-test模块下的`io.mykit.delay.TestDelayQueue`消费前面添加数据 为了方便查询效果 默认的消费方式是`consoleCQ` 控制台输出


# 扩展

支持zset队列个数可配置 避免大数据带来高延迟的问题。

目前存在日志和redis元数据有可能不一致的问题 如mysql挂了,写日志不会成功，后续会通过Redis直接同步binlog的方式来解决此问题。