# 《分布式消息队列XXL-MQ》

## 一、简介

#### 1.1 概述
XXL-MQ是一款轻量级、简单易用的 “分布式消息队列”，其核心设计目标是简化分布式消息队列开发。现已开放源代码，开箱即用。

支持三种消息模式: 

消息模式 | 特征说明 | 适用场景
--- | --- | ---
TOPIC(广播消息)模型 | 发布/订阅模式, 一条消息将会广播发送给对应Topic下所有在线的Consumer | 如广播集群节点进行缓存更新、广播集群节点进行站点静态化等
QUEUE(并发队列)模型 | 点对点模式, 消息进去队列之后, 只会被消费一次。同一Topic下的多个Consumer并行消费消息, 吞吐量较大 | 如邮件发送、短信发送等业务逻辑
SERIAL_QUEUE(串行队列)模型 | 点对点模式, 消息进去队列之后, 只会被消费一次。 但是,同一个Topic下只会有一个Consumer串行消费消息, 适用于严格限制并发的场景 | 秒杀、抢单等排队业务逻辑

至今，XXL-MQ已接入多家公司的线上产品线，截止2016-09-18为止，XXL-MQ已接入的公司包括不限于：
    
	- 1、农信互联
	- ……

#### 1.2 特性

- 1、简单易用: 一行代码即可发布一条消息; 一行注解即可订阅一个消息主题;
- 2、部署简单: 除ZK之外不依赖第三方服务, 基于Netty + Zookeeper实现;
- 3、三种消息模式: TOPIC(广播消息)模型、QUEUE(并发队列)模型 和 SERIAL_QUEUE(串行队列)模型,下文将会详细讲解:
- 4、Broker集群、HA: Broker支持集群部署, 可大大提高系统可用性,以及消息吞吐能力;
- 5、吞吐量: 依赖于部署的Broker集群和Mysql性能;
- 5、消息可追踪: 支持追踪每一条消息的执行路径, 便于排查业务问题;
- 6、消息可见: 系统中每一条消息可通过Web界面在线查看,甚至支持编辑消息内容和消息状态;
- 7、一致性: QUEUE(并发队列)模型 和 SERIAL_QUEUE(串行队列)模型的消息,保证只会成功执行一次;
- 8、Delay执行: 支持设置消息的延迟生效时间, 到达设置的Delay执行时间时该消息才会被消费 ,提供DelayQueue的功能;
- 9、消息重试: 支持设置消息的重试次数, 在消息执行失败后将会按照设置的值进行消息重试执行,直至重试次数耗尽或者执行成功;

#### 1.3 背景

##### Why MQ

- 异步: 很多场景下，不会立即处理消息，此时可以在MQ中存储message，并在某一时刻再进行处理；
- 解耦: 不同进程间添加一层实现解耦，方便今后的扩展。
- 消除峰值: 在高并发环境下，由于来不及同步处理，请求往往会发生堵塞，比如大量的insert，update之类的请求同时到达mysql，直接导致无数的行锁表锁，甚至最后请求会堆积过多，从而触发too manyconnections错误。通过使用消息队列，我们可以异步处理请求，从而缓解系统的压力。
- 耗时业务: 在一些比较耗时的业务场景中, 可以耗时较多的业务解耦通过异步队列执行, 提高系统响应速度和吞吐量;

##### Why XXL-MQ

目前流行的ActiveMQ、RabbitMQ和ZeroMQ等消息队列的软件中，大多为了实现AMQP，STOMP，XMPP之类的协议，变得极其重量级(如新版本Activemq建议分配内存达1G+)，但在很多Web应用中的实际情况是：我们只是想找到一个缓解高并发请求的解决方案，一个轻量级的消息队列实现方式才是我们真正需要的。

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
    - 1.1、Broker: 消息代理中心, 系统核心组成模块, 负责接受消息生产者Producer推送生产的消息, 同时负责提供RPC服务供消费者Consumer使用来消费消息; 
    - 1.2、Message Queue: 消息存储模块, 目前底层使用mysql消息表;
- 2、Registry Center
    - 2.1、Broker Registry Center: Broker注册中心子模块, 供Broker注册RPC服务使用;
    - 2.2、Consumer Registry Center: Consumer注册中心子模块, 供Consumer注册消费节点使用;
- 3、Client
    - 3.1、Producer: 消息生产者模块, 负责提供API接口供开发者调用,并生成和发送队列消息;
    - 3.2、Consumer: 消息消费者模块, 负责订阅消息并消息;

#### 2.2 Message设计: 消息核心参数

- id: 消息唯一标识
- name: 消息主题Topic, 每个消息队列的唯一标识
- delayTime: 消息延迟执行的时间, 当前时间超过延迟执行时间该消息才会被消费掉。
- status: 延迟执行的时间, new Date()立即执行, 否则在延迟时间点之后开始执行;
- msg: 消息状态: NEW=新消息、ING=消费中、SUCCESS=消费成功、FAIL=消费失败
- retryCount: 剩余重试次数, 在消息执行失败后将会按照设置的值进行消息重试执行,直至重试次数耗尽或者执行成功; 

#### 2.3 Broker设计

**Broker(消息代理中心)**, 系统核心组成模块, 负责接受消息生产者Producer推送生产的消息, 同时负责提供RPC服务供消费者Consumer使用来消费消息; 

**Broker支持集群部署**, 集群节点之间地位平等, 集群部署情况下可大大提高系统的消息吞吐量。

Broker通过Zookeeper实现集群功能, 各节点在启动时会自动注册到注册中心, Producer或Consumer在生产消息或者消费消息时,将会通过ZK自动感知到在线的Broker节点。

Broker在接收到Produce的生产消息的RPC调用时, 并不会立即存储该消息, 而是立即push到内存队列中, 同时立即响应RPC调用。 内存队列将会异步将队列中的消息数据存储到Mysql中。

Broker在接收到 "消息锁定" 等同步RPC调用时, 将会触发同步调用, 采用乐观锁方式锁定消息;

#### 2.4 Producer设计

Producer(消息生产者), Producer可以生成三种模式的消息

- TOPIC(广播消息)模型 : Producer生产该类型消息, 主要通过ZK来实现, 可结合 章节2.6.1 来理解; 
- QUEUE(并发队列)模型 : Producer生产该类型消息, 主要通过Broker的RPC服务来实现, 没生产一条消息,将会向Broker集群发送一条RPC调用, Broker将会立即将消息加入内存队列响应RPC调用, 内存队列将会异步将该消息存储在Mysql中;
- SERIAL_QUEUE(串行队列)模型 : 同 "QUEUE(并发队列)模型" 逻辑;


#### 2.5 Registry Center设计

Registry Center(注册中心)主要分为两个子模块: Broker注册中心、Consumer注册中心;

- Broker注册中心子模块: ZK中指定有一个固定的Broker注册位置, 每个Broker节点将会在该固定位置新增一个 "EPHEMERAL" 类型的ZK子节点, 赋值为该Broker的地址, 因此, Producer或Consumer可自动感知在线的节点来生产或消费消息;
- Consumer注册中心子模块: 每个消息主题Topic在ZK中对应一个指定的注册位置, 该消息主题下的Consumer将会在该位置新增一个 "EPHEMERAL" 类型的ZK子节点, 赋值为该Consumer的唯一ID。 因此, 每个Consumer可以感知同一Topic下在线的所有Consumer以及排序, 这在 "QUEUE(并发队列)模型" 消息的分片中, 以及 "SERIAL_QUEUE(串行队列)模型" 消息的串行执行中将会起到关键性作用,下文将会详细讲解;

#### 2.6 Consumer设计: 三种核心消息模型剖析

##### 2.6.1 TOPIC(广播消息)模型

**"TOPIC(广播消息)模型"** 通过ZK来实现, 该类型消息在"Registry Center" 中分配一个指定的消息监听节点, Producer通过对该节点赋值触发ZK的NodeDataChanged广播, 而该消息队列的Consumer在启动时会主动监听该节点, 在监听到NodeDataChanged时将会根据节点数据自动生成一条广播消息并执行。从而,实现了消息广播功能;

因为通过ZK节点来传输消息数据, 因此消息数据大小不可超过1M, 底层对消息长度做了限制。同时, 广播类型消息将不会落地;

该类型Consumer内部为了一个**内存队列**, ZK的NodeDataChanged触发生成的广播将会推送到对应Consumer的内存队列中异步执行, 因此广播消息并不会堵塞, 而且可保证消息串行平稳执行。

##### 2.6.2 QUEUE(并发队列)模型

"QUEUE(并发队列)模型" 通过 "多线程轮训 + 消息分片 + PULL + 消息锁定" 的方式来实现:

- 多线程轮训: 该模式下每个Consumer将会存在一个线程, 如存在多个Consumer, 多个Consumer将会并行消息同一主题下的消息, 大大提高消息的消费速度; 
- 消息分片 : 队列中消息将会按照 "Registry Center" 中注册的Consumer列表顺序进行消息分段, 保证一条消息只会被分配给其中一个Consumer, 每个Consumer只会消费分配给自己的消息。 因此在多个Consumer并发消息时, 可以保证同一条消息不被多个Consumer竞争来重复消息。
    - 分片函数: MOD("消息主键ID", #{在线消费者总数}) = #{当前消费者排名} , 
    - 分片逻辑解释: 每个Consumer通过注册中心感知到在线所有的Consumer, 计算出在线Consumer总数total, 以及当前Consumer在所有Consumer中的排名rank; 把消息主键ID对在线Consumer总数total进行取模, 余数和当前Consumer排名rank一致的消息认定为分配给自己的消息;
- PULL : 每个Consumer将会轮训PULL消息分片分配给自己的消息, 顺序消费。
- 消息锁定: Consumer在消费每一条消息时,将会主动进行消息锁定, 通过数据库乐观锁来实现, 锁定成功后消息状态变更为执行中状态, 将不会被Consumer再次PULL到。因此, 可以更进一步保证每条消息只会被消费一次;
- 消息状态和日志: 消息执行结束后, 将会调用Broker的RPC服务修改消息状态并追加消息日志, Broker将会通过内存队列方式, 异步消息队列中变更存储到数据库中。

##### 2.6.3 SERIAL_QUEUE(串行队列)模型

"SERIAL_QUEUE(串行队列)模型" 通过 "单线程轮训 + PULL" 的方式来实现, 

- 单线程轮训: 该模式下, Consumer可以通过 "Registry Center" 感知到在线的所有Consumer, 规定只有最大的Consumer节点对应的线程拥有执行权限, 其余节点将会进入睡眠状态, 保证只会有一个Consuemr消费队列中数据;
- PULL : 该模式下, 只会有一个存活状态的Consumer, 因此队列中所有消息都会被分配给该Consumer, 该Consumer将会轮训获取一定数量的消息, 顺序消费;
- 消息锁定: 同 "QUEUE(并发队列)模型" 逻辑;
- 消息状态和日志: 同 "QUEUE(并发队列)模型" 逻辑;

#### 2.8 消息重试

Delay : 支持设置消息的延迟生效时间, 到达设置的Delay执行时间时该消息才会被消费 ,提供DelayQueue的功能;

#### 2.7 消息延迟执行

当状态为执行失败的消息, 并且剩余重试次数大于零时, Broker竟会扣减一次剩余重试次数, 同时将失败的状态改为初始状态并记录重试日志, 初始状态的消息将会被Consumer重新消息。


## 三、快速入门
#### 3.1 编译项目
![输入图片说明](https://static.oschina.net/uploads/img/201609/11210919_0UU2.png "在这里输入图片标题")

源码目录介绍：
- /db
- /doc
- /xxl-mq-broker        (消息代理中心, 同时提供消息在线管理功能)
- /xxl-mq-client        (公共依赖, 提供API开发Producer和Consumer)
- /xxl-mq-example       (消息生产和消费example示例项目, 项目中开发了三种经典的消息模型, 可自行参考学习并使用）

#### 3.2 初始化数据库

执行源码目录下SQL脚本 "/xxl-mq/db/xxl-mq-mysql.sql" , 初始化MQ数据库表; 

#### 3.3 配置全局Zookeeper地址

**"Broker项目" 和 "XXL-MQ接入项目", 使用同样的方式进行Zookeeper地址配置, 配置文件并不在项目中, 而在项目所在硬盘指定绝对目录中, 便于ZK配置文件统一**

配置文件配置在项目所在硬盘绝对地址: “/data/webapps/xxl-conf.properties”

配置内容如下：
```
// zookeeper集群时，多个地址用逗号分隔
zkserver=127.0.0.1:2181
```


#### 3.4 配置部署“消息代理中心”(支持集群部署)

##### 配置JDBC连接

![输入图片说明](https://static.oschina.net/uploads/img/201609/11211811_Nsu5.png "在这里输入图片标题")

##### 配置登录账号密码

![输入图片说明](https://static.oschina.net/uploads/img/201609/11211638_b8OS.png "在这里输入图片标题")

#### 3.5 接入XXL-MQ并使用 (以示例项目"xxl-mq-example"为例,进行讲解)

##### 加入XXL-MQ的maven依赖

![输入图片说明](https://static.oschina.net/uploads/img/201609/11212448_dYNJ.png "在这里输入图片标题")

##### 生产消息

- 1、生产TOPIC广播消息
```
XxlMqProducer.broadcast("消息主题", "消息数据, Map<String, String>格式");
```

![输入图片说明](https://static.oschina.net/uploads/img/201609/11214345_VZb0.png "在这里输入图片标题")

- 2、生产QUEUE、SERIAL_QUEUE消息 (QUEUE和SERIAL_QUEUE两种消息格式完全一样, 生产消息的方式相同; 不同之处在于Consumer测配置不同)
```
XxlMqProducer.produce("消息主题", "消息数据, Map<String, String>格式");
```

![输入图片说明](https://static.oschina.net/uploads/img/201609/11214051_uCkR.png "在这里输入图片标题")

##### 消费消息 

( 如果系统仅仅负责生产消息, 可忽略掉该配置; )

- 1、配置Consumer工厂, 扫描 "MqConsumer" 目录

![输入图片说明](https://static.oschina.net/uploads/img/201609/11215050_S4ql.png "在这里输入图片标题")

- 2、开发 "MqConsumer"
 
系统中每个消费者以 "MqConsumer" 的形式存在, 规定如下:
 
     - 1、每个 "MqConsumer" 需要继承 "com.xxl.mq.client.consumer.IMqConsumer" 接口;
     - 2、需要扫描为Spring的Bean实例, 如加上 "@Service" 注解并被Spring扫描;
     - 3、需要加上注解 "com.xxl.mq.client.consumer.annotation.MqConsumer"。该注解 "value" 值为订阅的消息主题, "type" 值为消息类型(TOPIC广播消息、QUEUE并发消息队列 和 SERIAL_QUEUE串行消息队列);


系统中已经提供了 (TOPIC、QUEUE和SERIAL_QUEUE) 三种模式消息Consumer的示例, 参考如下:

"QUEUE并发消息队列" 模式的Consumer开发示例: 

![输入图片说明](https://static.oschina.net/uploads/img/201609/11220216_wmvO.png "在这里输入图片标题")

"SERIAL_QUEUE串行消息队列" 模式的Consumer开发示例: 

![输入图片说明](https://static.oschina.net/uploads/img/201609/11220520_VnqX.png "在这里输入图片标题")

"TOPIC广播消息" 模式的Consumer开发示例: 

![输入图片说明](https://static.oschina.net/uploads/img/201609/11220713_k49L.png "在这里输入图片标题")

##### 测试

示例项目(xxl-mq-example)已经提供了三种格式消息的 "消息生成示例代码" 和 "消息消费示例代码", 本次测试在此基础上进行;

我在本地测试时: 启动两台Tomcat-8080和Tomcat-8081, 端口分别为8080和8081, 各自都部署 "xxl-mq-example 消息生产和消费示例项目" 和 "xxl-mq-broker 消息代理中心项目";

"xxl-mq-example 消息生产和消费示例项目" 部署在根路径下, 访问地址为: http://localhost:8080/ , 可以在线查看消息 QUEUE和ERIAL_QUEUE 消息记录,并且可以对消息进行 "查询(统计某个消息主题下消息堆积情况,消费情况)"、"新增"、"编辑(失败消息修改重试次数进行重试, 修改消息数据, 修改消息Delay执行时间从而让消息在指定时间后才执行)"和"删除"等操作;

![输入图片说明](https://static.oschina.net/uploads/img/201609/11224914_qvEC.png "在这里输入图片标题")

"xxl-mq-broker 消息代理中心项目" 部署在二级路径 "/example" 下, 访问地址 http://localhost:8080/example/  可进入示例项目提供的三种消息的发送界面, 在界面上点击按钮,即可生成三种消息, 可以跟踪消费方消费日志跟踪消息消费情况;

![输入图片说明](https://static.oschina.net/uploads/img/201609/11224752_9fgb.png "在这里输入图片标题")


**1、测试 "QUEUE (并行消费队列)" : **

操作: 访问 "xxl-mq-broker 消息代理中心项目" 进入提供的消息生产测试页面, 点击 "QUEUE (并行消费队列)= mqconsumer-01" 按钮

现象: 进入 "消息代理中心", 如下图点击每条消息对应的 "历史流转日志" 按钮, 可查看每一条消息的流转信息;

![输入图片说明](https://static.oschina.net/uploads/img/201609/11225032_xnyN.png "在这里输入图片标题")

![输入图片说明](https://static.oschina.net/uploads/img/201609/11225136_6xjZ.png "在这里输入图片标题")

说明: 上图所示, 第一: 消息状态为SUCCESS, 说明消费成功; 第二: 消息ID=903, "rank=1, total=2", 意思是当前该消息队列存在两个Consumer, 该消息被排序为1(排名从0开始)的的Consumer消费掉; 通过上文 "QUEUE" 消息分片逻辑可知, 该消息ID对总consumer取模余数为1, 可消费该消息的Consumer的排名一致,说明消息分片成功;


**2、测试 "SERIAL_QUEUE (串行消费队列)" : **

操作: 访问 "xxl-mq-broker 消息代理中心项目" 进入提供的消息生产测试页面, 点击 "SERIAL_QUEUE (串行消费队列)= mqconsumer-02" 按钮

现象: 进入 "消息代理中心", 如下图点击每条消息对应的 "历史流转日志" 按钮, 可查看每一条消息的流转信息;

![输入图片说明](https://static.oschina.net/uploads/img/201609/11231241_JNbP.png "在这里输入图片标题")

![输入图片说明](https://static.oschina.net/uploads/img/201609/11231319_SB97.png "在这里输入图片标题")

说明: 上图所示, 第一: 消息状态为SUCCESS, 说明消费成功; 第二: 消息ID=910, "rank=0, total=1", 意思是当前该消息队列存在1个存活的Consumer, 该消息被排序为0(排名从0开始)的的Consumer消费掉; 通过上文 "SERIAL_QUEUE" 消费逻辑可知, 该类型消息对应的Consumer只有一个处于存活状态, 虽然两个Tomcat集群部署,但是只有一个处于存活状态, 它将串行消费掉队列中所有消息, 说明串行消费成功;

**3、测试 "TOPIC (广播消息)" : **

操作: 访问地址 http://localhost:8080/example/ ,点击 "TOPIC (广播消息)= mqconsumer-03" 按钮

现象: 两台Tomcat-8080和Tomcat-8081下, 都打印了以下日志, 
```
2016-09-11 22:37:10 xxl-mq-example [com.xxl.mq.example.mqcomsumer.DemoCMqComsumer]-[Thread-18]-[consume]-[25]-[INFO] TOPIC(广播消息): mqconsumer-02消费一条消息:{"时间戳":"1473604630889"}
```

说明: "TOPIC (广播消息)" 发送成功, 监听该消息主题 "mqconsumer-03" 的 "DemoCMqComsumer" 都收到了广播消息并执行成功, 说明测试成功;


## 四、版本更新日志
#### 4.1 版本V1.1 新特性
- 1、简单易用: 一行代码即可发布一条消息; 一行注解即可订阅一个消息主题;
- 2、部署简单: 除ZK之外不依赖第三方服务;
- 3、三种消息模式: TOPIC(广播消息)模型、QUEUE(并发队列)模型 和 SERIAL_QUEUE(串行队列)模型,下文将会详细讲解:
- 4、Broker集群、HA: Broker支持集群部署, 可大大提高系统可用性,以及消息吞吐能力;
- 5、吞吐量: 依赖于部署的Broker集群和Mysql性能;
- 5、消息可追踪: 支持追踪每一条消息的执行路径, 便于排查业务问题;
- 6、消息可见: 系统中每一条消息可通过Web界面在线查看,甚至支持编辑消息内容和消息状态;
- 7、一致性: QUEUE(并发队列)模型 和 SERIAL_QUEUE(串行队列)模型的消息,保证只会成功执行一次;
- 8、Delay执行: 支持设置消息的延迟生效时间, 到达设置的Delay执行时间时该消息才会被消费 ,提供DelayQueue的功能;
- 9、消息重试: 支持设置消息的重试次数, 在消息执行失败后将会按照设置的值进行消息重试执行,直至重试次数耗尽或者执行成功;

#### 规划中
- Broker中过期消息自动清理: 新增参数cache_day, 单位/天, 含义: 针对消费成功的消息, Broker缓存时间为cache_day天, 超过cache_day的消费成功的消息, 将会被删除;
- 消息堆积报警: 新增参数alarm_num: 消息堆积报警, 30分钟统计一次消息情况, 将会根据topic分组, 堆积超过阈值的topic将会在报警邮件报表中进行记录;

## 五、其他

#### 7.1 报告问题
XXL-MQ托管在Github上，如有问题可在 [ISSUES](https://github.com/xuxueli/xxl-mq/issues/) 上提问，也可以加入技术交流群(仅作技术交流)：367260654

#### 7.2 接入登记
更多接入公司，欢迎在github [登记](https://github.com/xuxueli/xxl-mq/issues/1 )
