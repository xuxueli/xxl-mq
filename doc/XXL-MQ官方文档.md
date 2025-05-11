## 《分布式消息队列XXL-MQ》

[![Actions Status](https://github.com/xuxueli/xxl-mq/workflows/Java%20CI/badge.svg)](https://github.com/xuxueli/xxl-mq/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-mq-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-mq-core/)
[![GitHub release](https://img.shields.io/github/release/xuxueli/xxl-mq.svg)](https://github.com/xuxueli/xxl-mq/releases)
[![GitHub stars](https://img.shields.io/github/stars/xuxueli/xxl-mq)](https://github.com/xuxueli/xxl-mq/)
[![Docker pulls](https://img.shields.io/docker/pulls/xuxueli/xxl-mq-admin)](https://hub.docker.com/r/xuxueli/xxl-mq-admin/)
[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![donate](https://img.shields.io/badge/%24-donate-ff69b4.svg?style=flat-square)](https://www.xuxueli.com/page/donate.html)

[TOCM]

[TOC]

## 一、简介

### 1.1 概述
XXL-MQ是一个分布式消息队列，具备“水平扩展、高吞吐（单机TPS 10W+）、海量消息、跨语言（RESTful）、实时性” 等特性，支持 “并发消息、串行消息、广播消息、延迟消息、失败重试消息、超时控制”多消息类型，现已开放源代码，开箱即用。

### 1.2 特性

- 1、简单易用: 一行代码即可发布一条消息，一行注解即可订阅一个消息主题，接入灵活方便；
- 2、轻量级: 除存储层（可选，支持MySQL/Blade），无第三方依赖；部署及运维低成本、轻量级。 
- 3、水平扩展：得益于存算分离系统设计，消息中心为无状态服务；消息中心（Broker）及消费者（Client）均支持集群扩展部署，线形提升消息生产及吞吐能力；
- 4、高可用/HA：消息中心内置注册发现能力，支持Broker服务以及Topic消费者动态注册；消息中心与消费者单节点故障时，可自动摘除故障节点，实现消息吞吐及消费故障转移；
- 5、高吞吐：消息生产及消费链路进行批量、并行及异步系统设计，消息存储进行冷数据及时清理归档设计，实现消息高吞吐（Mysql存储单机吞吐1W；Blade存储单机吞吐10W）。
- 6、海量消息：消息存储基于DB实现，支持Mysql、Blade多存储介质；消息存储进行冷热设计，并滚动式清理归档，支持海量消息堆积（Mysql存储支持千万级；Blade存储支持10亿级/理论无上限）。
- 7、存算分离：消息中心（Broker）设计为无状态服务，提供主题及消息控制台管理能力以及消息RPC服务能力，与消息存储层解耦；
- 8、跨语言/OpenAPI：提供语言无关的消息中心 OpenAPI（RESTFUL 格式），提供消息生产、拉取及消费等能力，实现多语言支持；
- 9、实时性：消息中心与消费者基于JsonRpc进行吞吐消费，支持毫秒级生产投递、秒级消费感知，延迟消息基于时间轮机制支持零延迟消费感知；
- 10、消息持久化：消息数据默认持久化存储，并支持Topic维度自定义清理归档策略，灵活控制消息数据滚动归档清理；
- 11、分区路由策略：针对消息数据进行分区并路由消费者，提供丰富路由策略，包括：Hash、轮询、随机、第一个、最后一个、广播；
- 12、归档策略：针对已消费完成数据滚动归档及清理，提供丰富归档策略，包括：归档保留7天、归档保留30天、归档保留90天、归档永久保留、不归档直接清理；
- 13、重试策略：针对消费失败消息，支持设置丰富重试策略，包括：固定间隔、线性间隔、随机间隔；
- 14、失败重试：针对消费失败消息，支持自定义重试次数、以及重试间隔基数，结合重试策略支持灵活消费失败重试，支持重试次数耗尽或者消费成功；
- 15、超时控制: 支持自定义消息超时时间，消息消费超时将会主动中断；
- 16、多种消息模式: 
    - 并行消息：针对单个Topic，支持针对消息数据进行分区范围切分，路由至多消费者并行消费。适用于吞吐量较大的消息场景，如邮件发送、短信发送等场景；
    - 串行消息：针对单个Topic，支持消息固定绑定分区，固定分配单个消费者FIFO串行消费。适用于严格限制并发的消息场景，如秒杀、抢单等场景；
    - 广播消息：消息将会广播发送给该主题全部在线消费者。适用于广播场景，如广播通知、广播更新缓存等；
    - 延时消息: 支持设置消息的延迟生效时间, 到达设置的生效时间时该消息才会被消费；适用于延时消费场景，如订单超时取消等；
    - 失败重试消息：针对单个Topic，支持设置消息失败重试次数、重试间隔基数，结合重试策略支持灵活消费失败重试，支持重试次数耗尽或者消费成功；
- 17、消息可视化: 提供消息中心Web控制台，可在线管理消息主题、消息数据，查看消费数据及消费轨迹等；
- 18、消息轨迹: 消费生产及消费轨迹日志会进行记录，并支持在线查看，辅助排查业务问题；
- 19、优先级：支持设置消息主题优先级，优先级越高，消费吞吐资源配置及保障越高；
- 20、消息失败告警：支持以Topic粒度监控消息，存在失败消息时主动推送告警邮件；默认提供邮件方式失败告警，同时预留扩展接口，可方面的扩展短信、钉钉等告警方式；
- 21、容器化：提供官方Docker镜像，并实时更新推送DockerHub，进一步实现产品开箱即用；
- 22、访问令牌（AccessToken）：为提升系统安全性，消息中心和消费者客户端进行安全性校验，校验AccessToken合法性；


### 1.3 发展
于2015年中，我在github上创建XXL-MQ项目仓库并提交第一个commit，随之进行系统结构设计，UI选型，交互设计……

至今，XXL-MQ已接入多家公司的线上产品线，截止2016-09-18为止，XXL-MQ已接入的公司包括不限于：
    
	- 1、农信互联
    - 2、源源科技
	- ……

> 更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-mq/issues/1 ) 登记，登记仅仅为了产品推广。

欢迎大家的关注和使用，XXL-MQ也将拥抱变化，持续发展。


#### Why MQ

- 1、**系统解耦**：生产者与消费者无需直接依赖，降低模块间耦合度，提升可维护性。  
- 2、**异步处理**：将耗时操作异步化，提高响应速度，增强用户体验。  
- 3、**削峰填谷**：缓解高并发请求对系统的冲击，防止系统雪崩。  
- 4、**保证最终一致性**：通过消息通知实现跨系统数据同步，支持重试机制确保一致性。  
- 5、**多通信模式诉求**：如点对点、发布订阅、延迟消息等，适应不同业务场景。  
- 6、**提高可靠性与容错能力**：消息持久化、失败重试、死信队列等机制保障消息不丢失。


#### Why XXL-MQ

Kafka、RabbitMQ等流行消息中间件，具备高吞吐及高性能等优势，但相对应的存在运维及搭建成本、高硬件资源消耗、依赖组件复杂度、冷启动成本等。如Kafka集群要求三节点部署、单独配置ZK（或启用KRaft）。

如果你期望 “轻量级、低运维成本、中等规模消息量级（TPS<10W；消息存储<10亿）”，轻量级消息队列 XXL-MQ 是一个适合的解决方案，对比特征如下： 

- 1、**部署轻量级**：除存储层（可选，支持MySQL/Blade），无第三方依赖；部署及运维低成本、轻量级。
- 2、**低运维成本**：可复用已有存储层基建能力，不需要额外运维；
- 3、**开发门槛低**：一行代码即可发布一条消息，一行注解即可订阅一个消息主题，接入灵活方便；
- 4、**特性丰富**：相较于传统消息中间件，具备丰富特性。详细参考章节 “1.2 特性”。
- 5、**二次开发**：系统基于流行语言及技术实现，可灵活进行二次开发，自定义扩展满足业务需求。


### 1.4 下载

#### 文档地址

- [中文文档](https://www.xuxueli.com/xxl-mq/)

#### 源码仓库地址

源码仓库地址 | Release Download
--- | ---
[https://github.com/xuxueli/xxl-mq](https://github.com/xuxueli/xxl-mq) | [Download](https://github.com/xuxueli/xxl-mq/releases)
[https://gitee.com/xuxueli0323/xxl-mq](https://gitee.com/xuxueli0323/xxl-mq) | [Download](https://gitee.com/xuxueli0323/xxl-mq/releases)  


#### 技术交流
- [社区交流](https://www.xuxueli.com/page/community.html)

#### 中央仓库地址

```
<dependency>
    <groupId>com.xuxueli</groupId>
    <artifactId>xxl-mq-core</artifactId>
    <version>{最新Release版本}</version>
</dependency>
```

### 1.5 环境

- Maven3+
- Jdk1.8+
- Mysql8.0+


## 二、快速入门


### 2.1 初始化"消息中心数据库"
请下载项目源码并解压，获取 "消息中心数据库初始化SQL脚本" 并执行即可

"消息中心数据库初始化SQL脚本" 位置为:

    /xxl-mq/doc/db/tables_xxl_mq.sql
    
消息中心支持集群部署，集群情况下各节点务必连接同一个mysql实例;

>注意：消息中心数据库，原生兼容支持 "MySQL、TIDB" 两种存储方式，前者支持千万级消息堆积，后者支持10亿级别消息堆积（TIDB理论上无上限）；
可视情况选择使用，当选择TIDB时，仅需要修改消息中心数据库连接jdbc地址配置即可，其他部分如SQL和驱动兼容MySQL和TIDB使用，不需修改。


### 2.2 编译项目
解压源码,按照maven格式将源码导入IDE, 使用maven进行编译即可，源码结构如下：

    - /xxl-mq-admin                     ：消息中心，提供消息Broker、服务注册、消息在线管理功能；
    - /xxl-mq-core                      ：客户端核心依赖, 提供API开发Producer和Consumer；
    - /xxl-mq-samples                   ：接入项目参考示例, 可自行参考学习并使用；
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
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xxl_mq?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai

### 告警邮箱发送方配置
spring.mail.username=xxx@qq.com
spring.mail.password=xxx

### 国际化配置 [必填]： 默认为 "zh_CN"/中文简体, 
xxl.mq.i18n=zh_CN

``` 

#### 步骤二：部署项目：

如果已经正确进行上述配置，可将项目编译打包部署。
消息中心访问地址：http://localhost:8080/xxl-mq-admin (该地址接入方项目将会使用到，作为注册地址)，登录后运行界面如下图所示

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_01.png "在这里输入图片标题")

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
* 如需自定义 mysql 等配置，可通过 "PARAMS" 指定，参数格式 RAMS="--key=value  --key2=value2" ；
* 配置项参考文件：/xxl-mq/xxl-mq-admin/src/main/resources/application.properties
*/
docker run -e PARAMS="--spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xxl-mq?Unicode=true&characterEncoding=UTF-8" -p 8080:8080 -p 7080 -v /tmp:/data/applogs --name xxl-mq-admin  -d xuxueli/xxl-mq-admin
```


### 2.4 接入XXL-MQ并使用

    接入XXL-MQ项目："xxl-mq-samples-springboot" (提供多种版本示例项目供参考选择，现以springboot版本为例讲解)
    作用：生产消息、消费消息；可直接部署，也可以将集成到现有业务项目中。

#### 步骤一：maven依赖
确认pom文件中引入了 "xxl-mq-core" 的maven依赖；

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


### 2.6 功能测试 & 性能测试

首选启动消息中心，然后启动 "springboot版本示例项目"；

访问部署成功的 "springboot版本示例项目" 地址，浏览器访问展示如下如下：

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_02.png "在这里输入图片标题")

该示例项目已经提供了多个消息生产与消费的实例：

#### a、"并行消费" 测试：连续点击 "并行消费" 按钮4次，将会生产4条并行消息；

进入消息中心 "消息记录" 菜单，消息列表如下：
![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_06.png "在这里输入图片标题")

逐个查看消息流转日志如下：

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_04.png "在这里输入图片标题")

可以注意 "锁定消息" 的 "消费者信息"，可以查看到当前消费者在集群中的排序 "rank"。

逐个查看每条消息对应消费者的 "rank" 属性，可以看到上面4条消息平局分配给不同 "rank" 的消费者，即平均分配给了不同消费者。测试正常；

#### b、"串行消费" 测试：连续点击 "串行消费" 按钮4次，将会生产4条串行消费；

操作步骤同 "并行消息"。最后一步逐个查看每条消息对应消费者的 "rank" 属性，会发现全部一致，即固定分配给了一个消费者。测试正常


#### c、"广播消息"：点击 "广播消息" 按钮一次，将会生产一条广播消息；

进入消息中心 "消息记录" 菜单，消息列表如下：

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_07.png "在这里输入图片标题")

一条广播消息将会广播给该主题全部在线group，该消息主题存在2个消息group，所以会每个group创建一条，即两条消息。测试正常。

#### d、"延时消息"：点击 “延时消息” 按钮一次，将会生产一条延时消息；
 
 进入消息中心 "消息记录" 菜单，可以查看消息 “生效时间”属性为 5min 之后，最终该消息在 5min 之后被消费执行。测定正常。

#### e、"性能测试" 测试：点击 “性能测试”按钮，将会批量发送10000条消息；

点击按钮后，页面下方展示文案 “Cost = 1055”，说明在 1055ms 之内客户端发送了 1000 条消息；

但是，由于测试代码中采用异步方式发送，消息发送事件与是否成功需要在消息中心中确认。

进入消息中心 “消息记录” 菜单，如下图，可以看到 10000 条消息创建事件最大为 “2018-12-02 04:51:54”，最小为 “2018-12-02 04:51:55”。说明在 1s 左右客户端成功发送了 10000 条消息，且 100% 投递成功，即单机TPS过万；

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_14.png "在这里输入图片标题")

然后进入 “运行报表” 界面，如下图，点击成功比例图可知，成功消费 10000 条，比例 100%。说明客户端发送的 10000 条消息 100% 消费成功。

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_15.png "在这里输入图片标题")

#### 其他测试
如延时消息、重试消息 …… 可自行参考示例代码测试；


## 三、消息中心，操作指南

### 3.1 运行报表：
运行报表界面，展示消息中心系统信息，如业务线、消息主题、消息数量等；支持日期分布图、成功比例图方式查看；

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_01.png "在这里输入图片标题")

### 3.2 消息主题
消息主题界面，可查看在线消息主题列表；底层会周期性扫描消息记录，发型并录入新的消息主题，并展示在这里；
![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_08.png "在这里输入图片标题")

消息主题界面，支持为消息主题设置一些附属参数，提供一些增强功能；如负责人、告警邮箱等；

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_11.png "在这里输入图片标题")

消息主题属性：
- 业务线：该消息所属业务线，方便分组管理；
- 负责人：该消息所属负责人；
- 告警邮箱：一个或多个，多个逗号分隔；消息消费失败时，将会周期性发送告警邮件；

### 3.3 消息记录
消息记录界面，可查看在线消息记录；支持筛选、查看消息流转轨迹；
![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_09.png "在这里输入图片标题")

- 消息在线管理功能：支持在线 "新增"、"编辑" 和 "删除" 消息记录； 

消息新增如下图所示，消息属性说明，可参考章节 "4.2 Message设计"；

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_12.png "在这里输入图片标题")

- 消息手动清理：支持在线清理消息，可选择消息主题、状态、清理类型等；

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_13.png "在这里输入图片标题")

### 3.4 业务线
业务先界面，可查看在线业务线列表，并管理维护；可通过自定义业务线，绑定消息主题，从而方便消息主题的分组管理；
![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_10.png "在这里输入图片标题")



## 四、系统设计

### 4.1 系统架构图

![输入图片说明](https://www.xuxueli.com/doc/static/xxl-mq/images/img_03.png "在这里输入图片标题")

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
group | 消息分组；为空时自动赋值UUID多分组【广播消费】；
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

### 4.11 海量数据堆积
消息中心数据库，原生兼容支持 "MySQL、TIDB" 两种存储方式，前者支持千万级消息堆积，后者支持百亿级别消息堆积（TIDB理论上无上限）；

可视情况选择使用，当选择TIDB时，仅需要修改消息中心数据库连接jdbc地址配置即可，其他部分如SQL和驱动兼容MySQL和TIDB使用，不需修改。



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


### 5.4 版本 v1.2.1 Release Notes[2018-12-02]
- 1、单机TPS过万：示例项目中新增功能测试、性能测试用例，以及消息生产、消费、成功率等方便的数据分析；可参考示例项目性能测试用例（章节 “2.6 功能测试 & 性能测试”），单机TPS过万；
- 2、底层long polling监控keys非法去重问题修复；
- 3、注册逻辑优化，批量注册，提高注册性能，降低注册中心压力；
- 4、消息中心RPC服务支持自定义注册IP地址；
- 5、消息中心内置注册中心线程数优化，精简；

### 5.5 版本 v1.2.2 Release Notes[2018-12-21]
- 1、访问令牌（accessToken）：为提升系统安全性，消息中心和客户端进行安全性校验，双方AccessToken匹配才允许通讯；
- 2、支持批量注册、摘除，提升注册发现性能；升级 xxl-rpc 至 v1.3.1;
- 3、升级 pom 依赖至较新版本；
- 4、表结构调整提升兼容性，表名转小写；
- 5、客户端取消Consumer非空的限制；

### 5.6 版本 v1.3.0 Release Notes[2025-02-07]
- 1、【增强】消费者分组属性 "group" 支持为空，为空时自动赋值UUID，方便实现多分组广播消费；
- 2、【增强】海量数据堆积：消息数据存储在DB中，原生兼容支持 "MySQL、TIDB" 两种存储方式，前者支持千万级消息堆积，后者支持百亿级别消息堆积（TIDB理论上无上限）；
- 3、【优化】消费者批量注册发现，提高注册发现性能；
- 4、【优化】消息流转日志格式优化，提升日志可读性；
- 5、【优化】升级xxl-rpc、xxl-registry至较新版本，Broker注册发现服务做适配性优化；

### 5.7 版本 v1.4.0 Release Notes[2025-05-11]
- 1、【重构】XXL-MQ 核心代码重构，模块化设计实现，提升可扩展性与稳定性；
    - 存算分离：Broker 计算；Blade/Mysql 存储；
    - 水平扩展：无状态；
    - 高吞吐：并行计算、异步计算；（TPS：Blade 10W / Mysql单机1W）
    - 海量消息：（Mysql日百万/Blade日十亿）
    - 消息轨迹：消息日志，追溯消息记录；
    - 多消费模式：Group + Partition，支持 广播消费、串行消费、分片消费；
    - 延迟消息：支持自定义延迟时间；
    - 失败重试：支持固定间隔策略、线性退避策略、指数退避策略等；
    - 失败告警：
    - AccessToken：
    - 容器化：
- 2、【增强】串行、并行、串并行结合、延时消息、重试消息、批量消息；



### TODO
- 会考虑移除 mysql 强依赖的，迁移 jpa 进一步提升通用型。
- producer消息，推送broker失败，先缓存本次文件；
- producer消息，生成UUID，推送失败重复推送，同时避免重复；
- 延迟消息方案优化：增加时间轮算法；
- 客户端，Server端支持消息落磁盘；发送失败，存储失败时，写磁盘，避免消息丢失；LocalQueue消息可能丢失，考虑LocalFile；
- 消息数据、Log使用text字段存储，为避免超长限制长度20000；后续考虑优化，尽量不限制数据长度、避免轨迹较多时Log超长问题；
- 消息告警功能增强，目前仅支持失败告警，考虑支持消息堆积告警、阻塞告警等，Topic扩展属性存储阈值；30分钟统计一次消息情况, 将会根据topic分组, 堆积超过阈值的topic将会在报警邮件报表中进行记录;
- 消息主题界面，支持查看在线消费者列表；consumer：topic+group 在线展示；producer：在线展示；
- 优先级队列，环境属性。
- 生产不丢：失败写磁盘文件；UUID，避免重复；
- 消费不丢：消息锁定逻辑调整。由“pull查询 + 前置锁定+消费后更新（先锁：不会重复消费）”，改为“先pull查询+消费后更新（不锁：可能重复消费）”；
- 超时消息处理：stuck消息，由标记“失败”改为标记“未消费”状态，可避免消费链路问题导致丢消息（现状，不会重复消费），但会导致小概率重复消费（需要实现幂等）；
- 消息等级：Topic优先级处理，拆分不同messagequeue；


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
无论金额多少都足够表达您这份心意，非常感谢 ：）      [前往捐赠](https://www.xuxueli.com/page/donate.html )
