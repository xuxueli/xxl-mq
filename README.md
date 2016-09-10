# 《分布式消息队列XXL-MQ》

## 一、简介

#### 1.1 概述
XXL-MQ是一个一款轻量级、简单易用的 “分布式消息队”，其核心设计目标是开发迅速、学习简单、轻量级、易扩展。现已开放源代码，开箱即用。

支持三种消息模式: 
- TOPIC(广播消息)模型: 发布/订阅模式, 一条消息将会广播发送给所有在线的Consumer。使用场景,: 如广播集群节点进行缓存更新、广播集群节点进行站点静态化等;
- QUEUE(并发队列)模型: 点对点模式, 消息进去队列之后, 只会被消费一次。同一Topic下的多个Consumer并行消费消息, 吞吐量较大。使用场景: 如邮件发送、短信发送等业务逻辑;
- SERIAL_QUEUE(串行队列)模型: 点对点模式, 消息进去队列之后, 只会被消费一次, 但是,同一个Topic下只会有一个Consumer串行消费消息, 适用于严格串行消费的消息。 使用场景: 秒杀、抢单等排队业务逻辑;

#### 1.2 特性
- 1、简单易用: 一行代码即可发布一条消息; 一行注解即可订阅一个消息主题;
- 2、部署简单: 除ZK之外不依赖第三方服务;
- 3、三种消息模式: TOPIC(广播消息)模型、QUEUE(并发队列)模型 和 SERIAL_QUEUE(串行队列)模型,下文将会详细讲解:
- 4、Broker集群、HA: Broker支持集群部署, 可大大提高系统可用性,以及消息吞吐能力;
- 5、吞吐量: 依赖于部署的Broker集群和Mysql性能;
- 5、消息可追踪: 支持追踪每一条消息的执行路径, 便于排查业务问题;
- 6、消息可见: 系统中每一条消息可通过Web界面在线查看,甚至支持编辑消息内容和消息状态;
- 7、一致性: QUEUE(并发队列)模型 和 SERIAL_QUEUE(串行队列)模型的消息,保证只会成功执行一次;
- 8、Delay: 支持设置消息的延迟生效时间, 到达设置的Delay执行时间时该消息才会被消费;
- 9、消息重试, 支持设置消息的重试次数, 在消息执行失败后将会按照设置的值进行消息重试执行,直至重试次数耗尽或者执行成功;

#### 1.3 背景

##### Why MQ
- 异步: 很多场景下，不会立即处理消息，此时可以在MQ中存储message，并在某一时刻再进行处理；
- 解耦: 不同进程间添加一层实现解耦，方便今后的扩展。
- 消除峰值: 在高并发环境下，由于来不及同步处理，请求往往会发生堵塞，比如大量的insert，update之类的请求同时到达mysql，直接导致无数的行锁表锁，甚至最后请求会堆积过多，从而触发too manyconnections错误。通过使用消息队列，我们可以异步处理请求，从而缓解系统的压力。
- 耗时业务: 在一些比较耗时的业务场景中, 可以耗时较多的业务解耦通过异步队列执行, 提高系统响应速度和吞吐量;

##### Why XXL-MQ
ActiveMQ、RabbitMQ和ZeroMQ等消息队列的软件中，大多为了实现AMQP，STOMP，XMPP之类的协议，变得极其重量级，但在很多Web应用中的实际情况是：我们只是想找到一个缓解高并发请求的解决方案，一个轻量级的消息队列实现方式才是我们真正需要的。

#### 1.4 下载
##### 源码地址 (将会在两个git仓库同步发布最新代码)

- [github地址](https://github.com/xuxueli/xxl-mq)
- [git.osc地址](http://git.oschina.net/xuxueli0323/xxl-mq)

##### 博客地址 (将会在两个博客同步更新文档)

- [oschina地址](http://my.oschina.net/xuxueli/blog/738918)
- [cnblogs地址](http://www.cnblogs.com/xuxueli/p/4918535.html)

##### 技术交流群(仅作技术交流)：367260654    [![image](http://pub.idqqimg.com/wpa/images/group.png)](http://shang.qq.com/wpa/qunwpa?idkey=4686e3fe01118445c75673a66b4cc6b2c7ce0641528205b6f403c179062b0a52 )

#### 1.5 环境
- Maven3+
- Jdk1.7+
- Tomcat7+
- Mysql5.5+
- Zookeeper3.4+

## 二、系统设计

#### 2.1 系统架构图

![输入图片说明](https://static.oschina.net/uploads/img/201609/10214138_SKhG.jpg "在这里输入图片标题")

##### 角色解释:
- Message : 消息实体;
- Broker : 消息代理中心, 负责连接Producer和Consumer;
- Topic : 消息主题, 每个消息队列的唯一性标示;
    - Topic segment : 消息分段, 同一个Topic的消息队列,将会根据订阅的Consumer进行分片分组,每个Consumer拥有的消息片即一个segment;
- Producer : 消息生产者, 绑定一个消息Topic, 并向该Topic消息队列中生产消息;
- Consumer : 消息消费者, 绑定一个消息Topic, 只能消费该Topic消息队列中的消息;
    - Consumer Group : 订阅同一个Topic的所有Consumer,认定为一个分组;

##### 架构图模块解读:
- 1、Server
    - 1.1、Broker: 系统核心组成模块, 负责接受消息生产者Producer推送生产的消息, 同时负责提供RPC服务供消费者Consumer使用来消费消息; 
    - 1.2、Message Queue: 消息存储模块, 目前底层使用mysql消息表;
- 2、Registry Center
    - 2.1、Broker Registry Center: Broker注册中心子模块, 供Broker注册RPC服务使用;
    - 2.2、Consumer Registry Center: Consumer注册中心子模块, 供Consumer注册消费节点使用;
- 3、Client
    - 3.1、Producer: 消息生产者模块, 负责提供API接口供开发者调用,并生成和发送队列消息;
    - 3.2、Consumer: 消息消费者模块, 负责订阅消息并消息;


#### 2.2 核心思想
提供轻量级、简单易用的 “分布式消息队” 功能，简化分布式消息队列开发。

#### 2.3 消息结构
- name: 消息主题
- destination:
    - TOPIC=广播, 消息推送给该主题下所有 consumer
    - QUEUE=串行队列, 消息队列方式执行, 支持Delay, 消息将会按照被生产的顺序被串行的消费, 即如存在多个 consumer 时, 只会有一个被激活进行消息消费;
    - CONCURRENT_QUEUE=并发队列, 消息队列方式执行, 支持Delay, 消息将会被多个broker并行消费, 不保证消费顺序, 消息会根据broker进行分组;
- data: 消息数据, Map<String, String>对象系列化的JSON字符串
- delay_time: 延迟执行的时间, new Date()立即执行, 否则在延迟时间点之后开始执行;
- add_time: 创建时间
- update_time: 更新时间
- status: 消息状态: NEW=新消息、ING=消费中、SUCCESS=消费成功、FAIL=消费失败、TIMEOUT=超时
- msg: 历史流转日志

#### 2.4 TOPIC(广播队列)模型 设计原理

#### 2.4 QUEUE(并发队列)模型 设计原理

#### 2.4 SERIAL_QUEUE(串行队列)模型 设计原理
    
#### 2.4 Broker剖析

#### 2.5 Registry Center剖析


## 三、快速入门
#### 3.1 准备工作
- 1、zookeeper集群;
- 2、编译项目

#### 3.2 配置Zookeeper地址

项目使用全局配置文件,便于不同项目复用。

配置文件需要配置在硬盘地址: “/data/webapps/xxl-conf.properties”

配置内容如下：
```
// zookeeper集群时，多个地址用逗号分隔
zkserver=127.0.0.1:2181
```

![输入图片说明]()

源码目录介绍：
- /db
- /doc
- /xxl-mq-broker        (消息代理中心, 同时提供消息在线管理功能)
- /xxl-mq-client        (公共依赖, 提供API开发Producer和Consumer)
- /xxl-mq-example       (示例example项目, 项目中开发了三种经典的消息模型, 可自行参考学习并使用）

#### 3.2 配置部署“消息代理中心”(支持集群部署)

##### 

#### 3.2 配置部署“示例example项目”

## 四、版本更新日志
#### 4.1 版本V1.1 新特性
- 1、简单易用: 一行代码即可发布一条消息; 一行注解即可订阅一个消息主题;
- 2、部署简单: 除ZK之外不依赖第三方服务;
- 3、三种消息模式: TOPIC(广播队列)模型、QUEUE(并发队列)模型 和 SERIAL_QUEUE(串行队列)模型,下文将会详细讲解:
- 4、Broker集群、HA: Broker支持集群部署, 可大大提高系统可用性,以及消息吞吐能力;
- 5、吞吐量: 依赖于部署的Broker集群和Mysql性能;
- 5、消息可追踪: 支持追踪每一条消息的执行路径, 便于排查业务问题;
- 6、消息可见: 系统中每一条消息可通过Web界面在线查看,甚至支持编辑消息内容和消息状态;
- 7、一致性: QUEUE(并发队列)模型 和 SERIAL_QUEUE(串行队列)模型的消息,保证只会成功执行一次;
- 8、Delay: 支持设置消息的延迟生效时间, 到达设置的Delay执行时间时该消息才会被消费;
- 9、消息重试, 支持设置消息的重试次数, 在消息执行失败后将会按照设置的值进行消息重试执行,直至重试次数耗尽或者执行成功;

#### 规划中
- 消息消费成功, 系统自动删除: cache_day: 成功消息保留时间, 大于0表示保留N天内消息,否则立刻删除成功消息;

## 五、其他

#### 7.1 报告问题
XXL-MQ托管在Github上，如有问题可在 [ISSUES](https://github.com/xuxueli/xxl-mq/issues/) 上提问，也可以加入技术交流群(仅作技术交流)：367260654

#### 7.2 接入登记
更多接入公司，欢迎在github [登记](https://github.com/xuxueli/xxl-mq/issues/2 )
