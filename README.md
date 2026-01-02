<p align="center">
    <img src="https://www.xuxueli.com/doc/static/xxl-job/images/xxl-logo.jpg" width="150">
    <h3 align="center">XXL-MQ</h3>
    <p align="center">
        XXL-MQ, A lightweight distributed message queue framework.
        <br>
        <a href="https://www.xuxueli.com/xxl-mq/"><strong>-- Home Page --</strong></a>
        <br>
        <br>
        <a href="https://github.com/xuxueli/xxl-mq/actions">
            <img src="https://github.com/xuxueli/xxl-mq/workflows/Java%20CI/badge.svg" >
        </a>
        <a href="https://central.sonatype.com/artifact/com.xuxueli/xxl-mq-core">
            <img src="https://img.shields.io/maven-central/v/com.xuxueli/xxl-mq-core" >
        </a>
        <a href="https://github.com/xuxueli/xxl-mq/releases">
            <img src="https://img.shields.io/github/release/xuxueli/xxl-mq.svg" >
        </a>
        <a href="https://github.com/xuxueli/xxl-mq/">
            <img src="https://img.shields.io/github/stars/xuxueli/xxl-mq" >
        </a>
        <a href="https://hub.docker.com/r/xuxueli/xxl-mq-admin/">
            <img src="https://img.shields.io/docker/pulls/xuxueli/xxl-mq-admin" >
        </a>
        <a href="http://www.gnu.org/licenses/gpl-3.0.html">
         <img src="https://img.shields.io/badge/license-GPLv3-blue.svg" >
        </a>
        <a href="https://www.xuxueli.com/page/donate.html">
            <img src="https://img.shields.io/badge/%24-donate-ff69b4.svg?style=flat-square" >
        </a>
    </p>    
</p>


## Introduction

XXL-MQ is a lightweight distributed message queue framework, With featuring "lightweight, distributed, high throughput (TPS of over 100,000 per machine), and massive messages (in the hundreds of millions)". 
It supports multiple message types including "parallel messages, serial messages, sharded messages, broadcast messages, delayed messages, and failed retry messages".
Now, it's already open source, real "out-of-the-box".

XXL-MQ是一个轻量级分布式消息队列，具备“轻量级、分布式、高吞吐、海量消息” 等特性，支持 “并行消息、串行消息、分片消息、广播消息、延迟消息、失败重试消息”多消息类型，现已开放源代码，开箱即用。


## Documentation
- [中文文档](https://www.xuxueli.com/xxl-mq/)


## Features
- 1、简单易用: 一行代码即可发布一条消息，一行注解即可订阅一个消息主题，接入灵活方便；
- 2、轻量级: 除存储层（可选，支持MySQL/TiDB），无第三方依赖；部署及运维低成本、轻量级。
- 3、水平扩展：得益于存算分离系统设计，消息中心为无状态服务；消息中心（Broker）及消费者（Client）均支持集群扩展部署，线形提升消息生产及吞吐能力；
- 4、高可用/HA：消息中心内置注册发现能力，支持Broker服务以及Topic消费者动态注册；消息中心与消费者单节点故障时，可自动摘除故障节点，实现消息吞吐及消费故障转移；
- 5、高吞吐：消息生产及消费链路进行批量、并行及异步系统设计，消息存储进行冷数据及时清理归档设计，实现消息高吞吐且支持水平扩容（Mysql存储单机吞吐10W/秒；TiDB存储单机吞吐50W/秒）；
- 6、海量消息：消息存储基于DB实现，支持Mysql、TiDB多存储介质；消息存储进行冷热设计，支持海量消息堆积（Mysql存储支持亿级；TiDB存储支持100亿级）；支持滚动式清理归档，开启后理论无容量上限；
- 7、存算分离：消息中心（Broker）设计为无状态服务，提供主题及消息控制台管理能力以及消息RPC服务能力，与消息存储层解耦；
- 8、跨语言/OpenAPI：提供语言无关的消息中心 OpenAPI（RESTFUL 格式），提供消息生产、拉取及消费等能力，实现多语言支持；
- 9、实时性：消息中心与消费者基于JsonRpc进行吞吐消费，支持毫秒级生产投递、秒级消费感知，延迟消息基于时间轮机制支持零延迟消费感知；
- 10、消息持久化：消息数据默认持久化存储，并支持Topic维度自定义清理归档策略，灵活控制消息数据滚动归档清理；
- 11、分区路由策略：针对消息数据进行分区并路由消费者，提供丰富路由策略，包括：Hash、轮询、随机、第一个、最后一个、广播；
- 12、归档策略：针对已消费完成数据滚动归档及清理，提供丰富归档策略，包括：归档保留7天、归档保留30天、归档保留90天、归档永久保留、不归档直接清理；
- 13、重试策略：针对消费失败消息，支持设置丰富重试策略，包括：固定间隔、线性间隔、随机间隔；
- 14、失败重试：针对消费失败消息，支持自定义重试次数、以及重试间隔基数，结合重试策略支持灵活消费失败重试，支持重试次数耗尽或者消费成功；
- 15、超时控制: 支持自定义消息超时时间，消息消费超时将会主动中断；
- 16、多消息类型:
  - 并行消息：多个消费者并行消费数据，支持轮询或随机策略。适用于消息吞吐量较大的业务场景，如邮件发送、日志记录等。
  - 串行消息：同一时刻只有一个消费者消费数据，消息按照生产顺序FIFO串行消费。适用于有串行消费诉求的业务场景，如秒杀、抢单等场景；
  - 分片消息：支持根据业务参数进行Hash分片，相同分片的消息数据路由至同一个消费者FIFO串行消费，不同分片的消息数据路由至不同消费者并行执行。适用于有根据业务参数分片消费的业务场景，如短信发送，可实现同一个手机号（业务参数）的消息路由至单个消费者串行消息，同时全局消息分片并行消费。
  - 广播消息：消息发送后，广播发送给相关主题全部在线消费者。适用于广播消息场景，如广播通知、广播更新缓存等；
  - 延时消息：支持设置消息的延迟生效时间，到达设置的生效时间时该消息才会被消费。适用于延时消费场景，如订单超时取消、定时发送邮件等；
  - 失败重试消息：支持设置消息的失败重试次数，自定义重试间隔侧路，消息失败时会主动进行重试消费，直至重试次数耗尽或者消费成功。
- 17、消息可视化: 提供消息中心Web控制台，可在线管理消息主题、消息数据，查看消费数据及消费轨迹等；
- 18、消息轨迹: 消费生产及消费轨迹日志会进行记录，并支持在线查看，辅助排查业务问题；
- 19、优先级：支持设置消息主题优先级，优先级越高，消费吞吐资源配置及保障越高；
- 20、消息失败告警：支持以Topic粒度监控消息，存在失败消息时主动推送告警邮件；默认提供邮件方式失败告警，同时预留扩展接口，可方面的扩展短信、钉钉等告警方式；
- 21、容器化：提供官方Docker镜像，并实时更新推送DockerHub，进一步实现产品开箱即用；
- 22、访问令牌（AccessToken）：为提升系统安全性，消息中心和消费者客户端进行安全性校验，校验AccessToken合法性；


## Development
于2015年中，我在github上创建XXL-MQ项目仓库并提交第一个commit，随之进行系统结构设计，UI选型，交互设计……

至今，XXL-MQ已接入多家公司的线上产品线，截止2016-09-18为止，XXL-MQ已接入的公司包括不限于：

	- 1、农信互联
    - 2、源源科技
	- ……

> 更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-mq/issues/1 ) 登记，登记仅仅为了产品推广。

欢迎大家的关注和使用，XXL-MQ也将拥抱变化，持续发展。


## Communication

- [社区交流](https://www.xuxueli.com/page/community.html)


## Contributing
Contributions are welcome! Open a pull request to fix a bug, or open an [Issue](https://github.com/xuxueli/xxl-mq/issues/) to discuss a new feature or change.

欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/xuxueli/xxl-mq/issues/) 讨论新特性或者变更。


## Copyright and License
This product is open source and free, and will continue to provide free community technical support. Individual or enterprise users are free to access and use.

- Licensed under the GNU General Public License (GPL) v3.
- Copyright (c) 2015-present, xuxueli.

产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。


## Donate
No matter how much the amount is enough to express your thought, thank you very much ：）     [To donate](https://www.xuxueli.com/page/donate.html )

无论金额多少都足够表达您这份心意，非常感谢 ：）      [前往捐赠](https://www.xuxueli.com/page/donate.html )
