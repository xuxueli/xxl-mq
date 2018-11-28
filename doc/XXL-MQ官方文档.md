## 《分布式消息队列XXL-MQ》

[![Build Status](https://travis-ci.org/xuxueli/xxl-mq.svg?branch=master)](https://travis-ci.org/xuxueli/xxl-mq)
[![Docker Status](https://img.shields.io/badge/docker-passing-brightgreen.svg)](https://hub.docker.com/r/xuxueli/xxl-mq-admin/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-mq/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-mq/)
[![GitHub release](https://img.shields.io/github/release/xuxueli/xxl-mq.svg)](https://github.com/xuxueli/xxl-mq/releases)
[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![donate](https://img.shields.io/badge/%24-donate-ff69b4.svg?style=flat-square)](http://www.xuxueli.com/page/donate.html)


## 一、简介

### 1.1 概述
XXL-MQ是一款轻量级分布式消息队列，支持 "并发消息、串行消息、广播消息、延迟消息、事务消息、失败重试、超时控制" 等消息特性。现已开放源代码，开箱即用。


### 1.2 特性

- 1、简单易用: 一行代码即可发布一条消息; 一行注解即可订阅一个消息主题;
- 2、轻量级: 部署简单，不依赖第三方服务，一分钟上手；
- 3、消息中心HA：消息中心支持集群部署,可大大提高系统可用性,以及消息吞吐能力;
- 4、消费者HA：消费者支持集群部署，保证消费者可用性；
- 5、三种消息模式: 
    - 并行消息：消息平均分配在该主题在线消费者，分片方式并行消费；适用于吞吐量较大的消息场景，如邮件发送、短信发送等业务逻辑
    - 串行消息：消息固定分配给该主题在线消费者中其中一个，FIFO方式串行消费；适用于严格限制并发的消息场景，如秒杀、抢单等排队业务逻辑；
    - 广播消息：消息将会广播发送给该主题在线消费者分组，全部分组都会消费该消息，但是一个分组下只会消费一次；适用于广播场景，如广播更新缓存等
- 6、延时消息: 支持设置消息的延迟生效时间, 到达设置的生效时间时该消息才会被消费；适用于延时消费场景，如订单超时取消等;
- 7、事务性: 消费者开启事务开关后,消息事务性保证只会成功执行一次;
- 8、失败重试: 支持设置消息的重试次数, 在消息执行失败后将会按照设置的值进行消息重试执行,直至重试次数耗尽或者执行成功;
- 9、超时控制: 支持自定义消息超时时间，消息消费超时将会主动中断；
- 10、吞吐量: 依赖于部署的消费中心集群和DB性能;DB可借助多表提升性能，不考虑DB的情况下，吞吐量可以无限横向扩展；
- 11、消息可见: 系统中每一条消息可通过Web界面在线查看,甚至支持编辑消息内容和消息状态;
- 12、消息可追踪: 支持追踪每一条消息的执行路径, 便于排查业务问题;
- 13、消息失败告警：支持以Topic粒度监控消息，存在失败消息时主动推送告警邮件；默认提供邮件方式失败告警，同时预留扩展接口，可方面的扩展短信、钉钉等告警方式；
- 14、容器化：提供官方docker镜像，并实时更新推送dockerhub，进一步实现产品开箱即用；


### 1.3 发展
于2015年中，我在github上创建XXL-MQ项目仓库并提交第一个commit，随之进行系统结构设计，UI选型，交互设计……

至今，XXL-MQ已接入多家公司的线上产品线，截止2016-09-18为止，XXL-MQ已接入的公司包括不限于：
    
	- 1、农信互联
	- ……

> 更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-mq/issues/1 ) 登记，登记仅仅为了产品推广。

欢迎大家的关注和使用，XXL-MQ也将拥抱变化，持续发展。


#### Why MQ

- 异步: 很多场景下，不会立即处理消息，此时可以在MQ中存储message，并在某一时刻再进行处理；
- 解耦: 不同进程间添加一层实现解耦，方便今后的扩展。
- 消除峰值: 在高并发环境下，由于来不及同步处理，请求往往会发生堵塞，比如大量的insert，update之类的请求同时到达mysql，直接导致无数的行锁表锁，甚至最后请求会堆积过多，从而触发too manyconnections错误。通过使用消息队列，我们可以异步处理请求，从而缓解系统的压力。
- 耗时业务: 在一些比较耗时的业务场景中, 可以耗时较多的业务解耦通过异步队列执行, 提高系统响应速度和吞吐量;

#### Why XXL-MQ

目前流行的ActiveMQ、RabbitMQ和ZeroMQ等消息队列的软件中，大多为了实现AMQP，STOMP，XMPP之类的协议，变得极其重量级(如新版本Activemq建议分配内存达1G+)，但在很多Web应用中的实际情况是：我们只是想找到一个缓解高并发请求的解决方案，一个轻量级的消息队列实现方式才是我们真正需要的。


### 1.4 下载

#### 文档地址

- [中文文档](http://www.xuxueli.com/xxl-mq/)

#### 源码仓库地址

源码仓库地址 | Release Download
--- | ---
[https://github.com/xuxueli/xxl-mq](https://github.com/xuxueli/xxl-mq) | [Download](https://github.com/xuxueli/xxl-mq/releases)
[https://gitee.com/xuxueli0323/xxl-mq](https://gitee.com/xuxueli0323/xxl-mq) | [Download](https://gitee.com/xuxueli0323/xxl-mq/releases)  


#### 技术交流
- [社区交流](http://www.xuxueli.com/page/community.html)

#### 中央仓库地址

```
<dependency>
    <groupId>com.xuxueli</groupId>
    <artifactId>xxl-mq-client</artifactId>
    <version>{最新Release版本}</version>
</dependency>
```

### 1.5 环境

- Maven3+
- Jdk1.7+
- Mysql5.6+


## 二、快速入门


### 2.1 初始化"消息中心数据库"
请下载项目源码并解压，获取 "消息中心数据库初始化SQL脚本" 并执行即可

"消息中心数据库初始化SQL脚本" 位置为:

    /xxl-mq/doc/db/xxl-mq-mysql.sql
    
消息中心支持集群部署，集群情况下各节点务必连接同一个mysql实例;

### 2.2 编译项目
解压源码,按照maven格式将源码导入IDE, 使用maven进行编译即可，源码结构如下：

    - /xxl-mq-admin                 ：消息中心，提供消息Broker、服务注册、消息在线管理功能；
    - /xxl-mq-client                ：客户端核心依赖, 提供API开发Producer和Consumer；
    - /xxl-mq-samples               ：接入项目参考示例, 可自行参考学习并使用；
        - /xxl-mq-samples-frameless     ：无框架示例项目，不依赖第三方框架，只需main方法即可启动运行；
        - /xxl-mq-samples-springboot    ：springboot版本示例项目；
        

### 2.3 配置部署“消息中心”

#### 步骤一：消息中心配置：
消息中心配置文件地址：

```
/xxl-mq/xxl-mq-admin/src/main/resources/application.properties
```

消息中心配置内容说明：

```
### 数据库配置
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xxl-mq?Unicode=true&characterEncoding=UTF-8

### 告警邮箱发送方配置
spring.mail.username=xxx@qq.com
spring.mail.password=xxx

### 注册心跳时间
xxl.mq.registry.beattime=10

### 注册信息磁盘存储目录，务必拥有读写权限；
xxl.mq.registry.data.filepath=/data/applogs/xxl-mq/registrydata

### 消息中心Broker服务端口
xxl-mq.rpc.remoting.port=7080

### 日志保存天数，超过该阈值的成功消息将会被自动清理；大于等于3时生效
xxl.mq.log.logretentiondays=3

### 登陆信息配置
xxl.mq.login.username=admin
xxl.mq.login.password=123456

``` 

#### 步骤二：部署项目：

如果已经正确进行上述配置，可将项目编译打包部署。
消息中心访问地址：http://localhost:8080/xxl-mq-admin (该地址接入方项目将会使用到，作为注册地址)，登录后运行界面如下图所示

![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_01.png "在这里输入图片标题")

至此“消息中心”项目已经部署成功。

#### 步骤三：消息中心集群（可选）：
消息中心支持集群部署，提升消息系统容灾和可用性。

消息中心集群部署时，几点要求和建议：
- DB配置保持一致；
- 登陆账号配置保持一致；
- 集群机器时钟保持一致（单机集群忽视）；
- 建议：推荐通过nginx为消息中心集群做负载均衡，分配域名。消息中心访问、客户端使用等操作均通过该域名进行。

#### 其他：Docker 镜像方式搭建消息中心：
- 下载镜像

```
// Docker地址：https://hub.docker.com/r/xuxueli/xxl-mq-admin/
docker pull xuxueli/xxl-mq-admin
```

- 创建容器并运行

```
docker run -p 8080:8080 -v /tmp:/data/applogs --name xxl-mq-admin  -d xuxueli/xxl-mq-admin

/**
* 如需自定义 mysql 等配置，可通过 "PARAMS" 指定；
* 配置项参考文件：/xxl-mq/xxl-mq-admin/src/main/resources/application.properties
*/
docker run -e PARAMS="--spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xxl-mq?Unicode=true&characterEncoding=UTF-8" -p 8080:8080 -v /tmp:/data/applogs --name xxl-mq-admin  -d xuxueli/xxl-mq-admin
```


### 2.4 接入XXL-MQ并使用

    接入XXL-MQ项目："xxl-mq-samples-springboot" (提供多种版本示例项目供参考选择，现以springboot版本为例讲解)
    作用：生产消息、消费消息；可直接部署，也可以将集成到现有业务项目中。

#### 步骤一：maven依赖
确认pom文件中引入了 "xxl-mq-client" 的maven依赖；

#### 步骤二："消息接入方"，属性配置
消息接入方配置，配置文件地址：

```
/xxl-mq/xxl-mq-samples/xxl-mq-samples-springboot/src/main/resources/application.properties
```

消息接入方配置，配置内容说明：

```
# 消息中心跟地址；支持配置多个，建议域名方式配置；
xxl.mq.admin.address=http://localhost:8080/xxl-mq-admin
```
    
#### 步骤三："消息接入方"，组件配置

```
@Bean
public XxlMqSpringClientFactory getXxlMqConsumer(){
    XxlMqSpringClientFactory xxlMqSpringClientFactory = new XxlMqSpringClientFactory();
    xxlMqSpringClientFactory.setAdminAddress(adminAddress);

    return xxlMqSpringClientFactory;
}
```    
    
#### 步骤四：部署"消息接入方"项目：

如果已经正确进行上述配置，可将项目编译打包部署。
springboot版本示例项目，访问地址：http://localhost:8081/


至此“消息接入方”示例项目已经部署结束。


#### 步骤五："消息接入方"集群（可选）：
消息接入方支持集群部署，提升消息系统可用性，同时提升消息处理能力。

消息接入方集群部署时，要求和建议：
- 消息中心跟地址（xxl.mq.admin.address）需要保持一致；


### 2.5 生产消息、消费消息

#### 生产消息

```
/**
 * 生产消息：并行消息
 */
XxlMqProducer.produce(new XxlMqMessage(topic, data));


/**
 * 生产消息：串行消费（ ShardingId 保持一致即可；如秒杀消息，可将 ShardingId 设置为商品ID，则该商户全部消息固定在一台机器消费；）
 */
XxlMqMessage mqMessage = new XxlMqMessage();
mqMessage.setTopic(topic);
mqMessage.setData(data);
mqMessage.setShardingId(1);

XxlMqProducer.produce(mqMessage);
			
/**
 * 生产消息：广播消费（ 消费者 IMqConsumer 注解的 group 属性修改不一致即可；一条消息将会广播给该主题全部在线 group，每个group都会消费，单个group只会消费一次； ）
 */
XxlMqProducer.broadcast(new XxlMqMessage(topic, data));


/**
 * 生产消息：延时消费（ EffectTime 设置为固定时间点即可；如订单30min超时取消，可将 EffectTime 设置为30min后的时间点，到时将会自动消费；）
 */
XxlMqMessage mqMessage = new XxlMqMessage();
mqMessage.setTopic(topic);
mqMessage.setData(data);
mqMessage.setEffectTime(effectTime);

XxlMqProducer.produce(mqMessage);


/**
 * 生产消息：失败重试消费（ RetryCount 设置重试次数即可；如发送短信消息，第三方服务不稳定时失败很常见，可设置 RetryCount 为3，失败是将会自动重试指定次数；）
 */
XxlMqMessage mqMessage = new XxlMqMessage();
mqMessage.setTopic(topic);
mqMessage.setData(data);
mqMessage.setRetryCount(3);

XxlMqProducer.produce(mqMessage);

……
```

更多消息属性、场景，可参考章节 "4.2 Message设计"；


#### 消费消息 

```
@MqConsumer(topic = "topic_1")
@Service
public class DemoAMqComsumer implements IMqConsumer {
    private Logger logger = LoggerFactory.getLogger(DemoAMqComsumer.class);

    @Override
    public MqResult consume(String data) throws Exception {
        logger.info("[DemoAMqComsumer] 消费一条消息:{}", data);
        return MqResult.SUCCESS;
    }

}
```

系统中每个消费者以 "IMqConsumer" 的形式存在, 规定如下:
 
     - 1、每个 "IMqConsumer" 需要继承 "com.xxl.mq.client.consumer.IMqConsumer" 接口;
     - 2、需要扫描为Spring的Bean实例, 需加上 "@Service" 注解并被Spring扫描;
     - 3、需要加上注解 "com.xxl.mq.client.consumer.annotation.MqConsumer"。该注解 "value" 值为订阅的消息主题, "type" 值为消息类型(TOPIC广播消息、QUEUE并发消息队列 和 SERIAL_QUEUE串行消息队列);


更多消费者属性、场景，可参考章节 "4.6 Consumer设计"；


#### 测试
首选启动消息中心，然后启动 "springboot版本示例项目"；

访问部署成功的 "springboot版本示例项目" 地址，浏览器访问展示如下如下：

![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_02.png "在这里输入图片标题")

该示例项目已经提供了多个消息生产与消费的实例：

- a、"并行消费" 测试：连续点击 "并行消费" 按钮4次，将会生产4条并行消息；

进入消息中心 "消息记录" 菜单，消息列表如下：
![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_06.png "在这里输入图片标题")

逐个查看消息流转日志如下：

![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_04.png "在这里输入图片标题")

可以注意 "锁定消息" 的 "消费者信息"，可以查看到当前消费者在集群中的排序 "rank"。

逐个查看每条消息对应消费者的 "rank" 属性，可以看到上面4条消息平局分配给不同 "rank" 的消费者，即平均分配给了不同消费者。测试正常；

- b、"串行消费" 测试：连续点击 "串行消费" 按钮4次，将会生产4条串行消费；

操作步骤同 "并行消息"。最后一步逐个查看每条消息对应消费者的 "rank" 属性，会发现全部一致，即固定分配给了一个消费者。测试正常


- c、"广播消息"：点击 "广播消息" 按钮一次，将会生产一条广播消息；

进入消息中心 "消息记录" 菜单，消息列表如下：

![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_07.png "在这里输入图片标题")

一条广播消息将会广播给该主题全部在线group，该消息主题存在2个消息group，所以会每个group创建一条，即两条消息。测试正常。

- d、其他测试：如延时消息、重试消息 …… 可自行测试；


## 三、消息中心，操作指南

### 3.1 运行报表：
运行报表界面，展示消息中心系统信息，如业务线、消息主题、消息数量等；支持日期分布图、成功比例图方式查看；

![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_01.png "在这里输入图片标题")

### 3.2 消息主题
消息主题界面，可查看在线消息主题列表；底层会周期性扫描消息记录，发型并录入新的消息主题，并展示在这里；
![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_08.png "在这里输入图片标题")

消息主题界面，支持为消息主题设置一些附属参数，提供一些增强功能；如负责人、告警邮箱等；

![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_11.png "在这里输入图片标题")

消息主题属性：
- 业务线：该消息所属业务线，方便分组管理；
- 负责人：该消息所属负责人；
- 告警邮箱：一个或多个，多个逗号分隔；消息消费失败时，将会周期性发送告警邮件；

### 3.3 消息记录
消息记录界面，可查看在线消息记录；支持筛选、查看消息流转轨迹；
![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_09.png "在这里输入图片标题")

- 消息在线管理功能：支持在线 "新增"、"编辑" 和 "删除" 消息记录； 

消息新增如下图所示，消息属性说明，可参考章节 "4.2 Message设计"；

![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_12.png "在这里输入图片标题")

- 消息手动清理：支持在线清理消息，可选择消息主题、状态、清理类型等；

![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_13.png "在这里输入图片标题")

### 3.4 业务线
业务先界面，可查看在线业务线列表，并管理维护；可通过自定义业务线，绑定消息主题，从而方便消息主题的分组管理；
![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_10.png "在这里输入图片标题")



## 四、系统设计

### 4.1 系统架构图

![输入图片说明](https://raw.githubusercontent.com/xuxueli/xxl-mq/master/doc/images/img_03.png "在这里输入图片标题")

#### 角色解释:

- Message : 消息实体;
- Broker : 消息代理中心, 负责连接Producer和Consumer;
- Topic : 消息主题, 每个消息队列的唯一性标示;
- Topic segment : 消息分段, 同一个Topic的消息队列,将会根据订阅的Consumer进行分片分组,每个Consumer拥有的消息片即一个segment;
- Producer : 消息生产者, 绑定一个消息Topic, 并向该Topic消息队列中生产消息;
- Consumer : 消息消费者, 绑定一个消息Topic, 只能消费该Topic消息队列中的消息;
- Consumer Group : 消费者分组，隔离消息；同一个Topic下一条消息消费一次；

#### 架构图模块解读:

- Server
    - Broker: 消息代理中心, 系统核心组成模块, 负责接受消息生产者Producer推送生产的消息, 同时负责提供RPC服务供消费者Consumer使用来消费消息; 
    - Message Queue: 消息存储模块, 目前底层使用mysql消息表;
- Registry Center
    - Broker Registry Center: Broker注册中心子模块, 供Broker注册RPC服务使用;
    - Consumer Registry Center: Consumer注册中心子模块, 供Consumer注册消费节点使用;
- Client
    - Producer: 消息生产者模块, 负责提供API接口供开发者调用,并生成和发送队列消息;
    - Consumer: 消息消费者模块, 负责订阅消息并消息;

### 4.2 Message设计

消息核心属性 | 说明
--- | ---
topic | 消息主题
group | 消息分组, 分组一致时消息仅消费一次；存在多个分组时，多个分组时【广播消费】；
data | 消息数据
retryCount | 重试次数, 执行失败且大于0时生效，每重试一次减一；
shardingId | 分片ID, 大于0时启用，否则使用消息ID；消费者通过该参数进行消息分片消费；分片ID不一致时分片【并发消费】、一致时【串行消费】；
timeout | 超时时间，单位秒；大于0时生效，处于锁定运行状态且运行超时时，将主动标记运行失败；
effectTime | 生效时间, new Date()立即执行, 否则在生效时间点之后开始执行;


### 4.3 Broker设计

Broker(消息代理中心)：系统核心组成模块, 负责接受消息生产者Producer推送生产的消息, 同时负责提供RPC服务供消费者Consumer使用来消费消息；

Broker支持集群部署, 集群节点之间地位平等, 集群部署情况下可大大提高系统的消息吞吐量。


Broker通过内置注册中心实现集群功能, 各节点在启动时会自动注册到注册中心, Producer或Consumer在生产消息或者消费消息时,将会通过内置注册中心自动感知到在线的Broker节点。

Broker在接收到Produce的生产消息的RPC调用时, 并不会立即存储该消息, 而是立即push到内存队列中, 同时立即响应RPC调用。 内存队列将会异步将队列中的消息数据存储到Mysql中。

Broker在接收到 "消息锁定" 等同步RPC调用时, 将会触发同步调用, 采用乐观锁方式锁定消息;


### 4.4 Registry Center设计

Registry Center(注册中心)主要分为两个子模块: Broker注册中心、Consumer注册中心;

- Broker注册中心子模块: 供Broker注册RPC服务使用;
- Consumer注册中心子模块: 供Consumer注册消费节点使用;


### 4.5 Producer设计

Producer(消息生产者), 兼容“异步批量多线程生产”+“同步生产”两种方式，提升消息发送性能；

底层通讯全异步化：消息新增 + 消息新增接受 + 消息回调 + 消息回调接受；仅批量PULL消息与锁消息非异步；


### 4.6 Consumer设计


MqConsumer注解属性 | 说明
--- | ---
group | 消息分组,
topic | 消息主题
transaction | 事务开关，开启消息事务性保证只会成功执行一次;关闭时可能重复消费，性能较优；


消费者通过 "多线程轮训 + 消息分片 + PULL + 消息锁定" 的方式来实现:

- 多线程轮训: 该模式下每个Consumer将会存在一个线程, 如存在多个Consumer, 多个Consumer将会并行消息同一主题下的消息, 大大提高消息的消费速度; 
    - 轮训延时自适应：线程轮训方式PULL消息，如若获取不到消息将会主动休眠，休眠时间依次递增10s，最长60s；即消息生产之后距离被消费存在 0~60s 的时间差，分钟范围内；
- 消息分片 : 队列中消息将会按照 "Registry Center" 中注册的Consumer列表顺序进行消息分段, 保证一条消息只会被分配给其中一个Consumer, 每个Consumer只会消费分配给自己的消息。 因此在多个Consumer并发消息时, 可以保证同一条消息不被多个Consumer竞争来重复消息。
    - 分片函数: MOD("消息分片ID", #{在线消费者总数}) = #{当前消费者排名} , 
    - 分片逻辑解释: 每个Consumer通过注册中心感知到在线所有的Consumer, 计算出在线Consumer总数total, 以及当前Consumer在所有Consumer中的排名rank; 把消息分片ID对在线Consumer总数total进行取模, 余数和当前Consumer排名rank一致的消息认定为分配给自己的消息;
- PULL : 每个Consumer将会轮训PULL消息分片分配给自己的消息, 顺序消费。
- 消息锁定: Consumer在消费每一条消息时,开启事务时，将会主动进行消息锁定, 通过数据库乐观锁来实现, 锁定成功后消息状态变更为执行中状态, 将不会被Consumer再次PULL到。因此, 可以更进一步保证每条消息只会被消费一次;
- 消息状态和日志: 消息执行结束后, 将会调用Broker的RPC服务修改消息状态并追加消息日志, Broker将会通过内存队列方式, 异步消息队列中变更存储到数据库中。


### 4.7 延时消息
支持设置消息的延迟生效时间, 到达设置的生效时间时该消息才会被消费；适用于延时消费场景，如订单超时取消等;

### 4.8 事务性
消费者开启事务开关后,消息事务性保证只会成功执行一次;

### 4.9 失败重试
支持设置消息的重试次数, 在消息执行失败后将会按照设置的值进行消息重试执行,直至重试次数耗尽或者执行成功;

### 4.10 超时控制
支持自定义消息超时时间，消息消费超时将会主动中断；



## 五、版本更新日志
### 5.1 版本V1.1.0 新特性
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

### 5.2 版本V1.1.1 特性
- 1、项目groupId改为com.xuxueli，为推送maven中央仓库做准备；
- 2、项目推送Maven中央仓库；
- 3、底层系统优化，CleanCode等；
- 4、修复confirm和alert弹框冲突导致消息列表错乱的问题；
- 5、优化ZK注册逻辑,ZK注册基础路径提前初始化；
- 6、broadcast 广播消息时ZK 发送方不进行watch, 否则发送方也会监听到；
- 7、修复一处因ReentrantLock导致可能死锁的问题；

### 5.3 版本V1.2.0 Release Notes[2018-11-28]
- 1、client端与Broker长链初始化优化，防止重复创建连接。
- 2、POM多项依赖升级；
- 3、UI组件升级；
- 4、规范项目目录结构；
- 6、超时控制；
- 5、通讯迁移至 xxl-rpc；
- 6、除了springboot类型示例；新增无框架示例项目 "xxl-mq-samples-frameless"。不依赖第三方框架，只需main方法即可启动运行；
- 7、消息生产，兼容“异步批量多线程生产”+“同步生产”两种方式，提升消息发送性能；
- 8、底层通讯全异步化：消息新增 + 消息新增接受 + 消息回调 + 消息回调接受；仅批量PULL消息与锁消息非异步；
- 9、串行消费优化，旧版本固定第一台消费，导致其压力过大；新版支持自定义shardingId从而实现串行消息的负载均衡，缓解单台压力；
- 10、广播消息优化，旧版本不支持消息持久化，新版本支持消息持久化，而且广播支持与串行结合实用，更加灵活；
- 11、并发消息、串行消息、广播消息全部优化重构，底层逻辑统一，方便后续维护扩展；
    - 串行：取消ZK依赖，废弃旧版ZK锁方式；优化为通过消息 shardingId 结合消费者排序取模方式；相同 shardingId 的消息将会固定被同一个消费者消费；
    - 并行：沿用旧版消费者排序取模方式，不过取模参数新增支持 shardingId 参数；确保消息平均分配给在线消费者；
    - 广播：取消ZK依赖，废弃旧版ZK方式；优化为通过消息 group 属性群发方式；每个group都会消费该消息，但相同group下消息仅被消费一次；
- 12、Broker服务支持自定义指定注册IP等信息，位置 "XxlMqBrokerImpl.initServer"；
- 13、Topic自动发现：消息中心支持动态发现Topic，并展示在消息主题列表，延时1min；
- 14、运行报表：支持展示在线业务线、消息主题、消息记录等信息、可在线查看消息日期分布图，成功分布图等；
- 15、业务线管理：支持设置业务线，用于分组管理消息主题；
- 16、消息主题管理：支持在线管理消息主题，自动发现消息主题；并支持完善消息主题扩展信息，如业务线、负责人、告警邮箱等；
- 17、消息记录界面，交互优化重构，进一步优化消息筛选、管理交互；
- 18、自动重试优化，任务重试时，生效时间重置为1min之后，重试次数减一；
- 19、记住密码功能优化，选中时永久记住；非选中时关闭浏览器即登出；
- 20、事务开关：支持设置消息事务开关，开启时事务保证消息精准消费一次；未开启时小概率存在重复消费，仅依靠注册中心分片检测避免重复，但性能略高；
- 21、告警功能：支持以Topic粒度监控消息，存在失败消息时主动推送告警邮件；
- 22、轨迹Log优化，新增、更新时记录核心数据；消息日志格式统一；
- 23、消息在线清理：在消息记录界面，支持在线清理消息数据；
- 24、过期消息自动清理：消息中心新增参数 “xxl-mq.log.logretentiondays”设置消息过期天数，过期成功消息将会自动清理；
- 25、超时强化，除了客户端支持超时控制外；服务端新增线程扫描，主动处理超时消息；消息超过 "生效时间 + 超时时间 + 1HOUT" 之后仍然未结束，将会主动标记为失败；
- 26、左侧菜单规范：运行报表（业务线，主题数，消息记录数；总消息成功率，日分布柱状图，总分布饼图） + 消息主题 + 消息记录 + 使用教程；
- 27、注册中心迁移至DB，基于 "long polling" 实现注册机器实时感知；注册中心代码及逻辑来源自“XXL-RPC原生轻量级注册中心”；
- 28、轻量级改造，移除对ZK依赖，仅依赖DB即可完整集群方式提供服务；缺点，非强一致性可能导致重复消费，开启事务开关可以避免该问题；
- 29、文档示例完善，包括：并发消息、串行消息、广播消息、延迟消息、失败重试消息、超时控制消息等；
- 30、文档完善：消息模型说明，延时消息说明、事务消息说明、失败重试、超时控制说明，


### 5.4 版本 v1.2.1 Release Notes[迭代中]


### TODO
- 会考虑移除 mysql 强依赖的，迁移 jpa 进一步提升通用型。
- producer消息，推送broker失败，先缓存本次文件；
- producer消息，生成UUID，推送失败重复推送，同时避免重复；
- 延迟消息方案优化：增加时间轮算法；
- producer：在线展示；
- consumer：topic+group；在线展示；
- 客户端，Server端支持消息落磁盘；发送失败，存储失败时，写磁盘，避免消息丢失；LocalQueue消息可能丢失，考虑LocalFile；
- 消息数据、Log使用text字段存储，为避免超长限制长度20000；后续考虑优化，尽量不限制数据长度、避免轨迹较多时Log超长问题；
- 消息告警功能增强，目前仅支持失败告警，考虑支持消息堆积告警、阻塞告警等，Topic扩展属性存储阈值；30分钟统计一次消息情况, 将会根据topic分组, 堆积超过阈值的topic将会在报警邮件报表中进行记录;
- accessToken安全校验；
- 消息主题界面，支持查看在线消费者列表；


## 六、其他

### 6.1 项目贡献
欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/xuxueli/xxl-mq/issues/) 讨论新特性或者变更。

### 6.2 用户接入登记
更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-mq/issues/1 ) 登记，登记仅仅为了产品推广。

### 6.3 开源协议和版权
产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。

- Licensed under the GNU General Public License (GPL) v3.
- Copyright (c) 2015-present, xuxueli.

---
### 捐赠
无论金额多少都足够表达您这份心意，非常感谢 ：）      [前往捐赠](http://www.xuxueli.com/page/donate.html )
