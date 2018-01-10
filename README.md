<p align="center">
    <a href="http://www.xuxueli.com/xxl-mq/">
        <img src="https://raw.githubusercontent.com/xuxueli/xxl-job/master/doc/images/xxl-logo.jpg" width="150">
    </a>
    <h3 align="center">XXL-MQ</h3>
    <p align="center">
        XXL-MQ, A lightweight distributed message queue framework.
        <br>
        <a href="http://www.xuxueli.com/xxl-mq/"><strong>-- Browse website. --</strong></a>
        <br>
        <br>
        <a href="https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-mq/">
            <img src="https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-mq/badge.svg" >
        </a>
         <a href="https://github.com/xuxueli/xxl-mq/releases">
             <img src="https://img.shields.io/github/release/xuxueli/xxl-mq.svg" >
         </a>
         <a href="http://www.gnu.org/licenses/gpl-3.0.html">
             <img src="https://img.shields.io/badge/license-GPLv3-blue.svg" >
         </a>
         <a href="http://www.xuxueli.com/page/donate.html">
            <img src="https://img.shields.io/badge/%24-donate-ff69b4.svg?style=flat-square" >
         </a>
    </p>    
</p>


## Introduction

XXL-MQ is a lightweight distributed message queue framework. 
Supports multiple message models such as serial messages, parallel messages, and broadcast messages.
Now, it's already open source, real "out-of-the-box".

XXL-MQ是一款轻量级分布式消息队列，支持串行、并行和广播等多种消息模型。现已开放源代码，开箱即用。


## Documentation
- [中文文档](http://www.xuxueli.com/xxl-mq/)


## Features
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


## Communication

- [社区交流](http://www.xuxueli.com/page/community.html)


## Contributing
Contributions are welcome! Open a pull request to fix a bug, or open an [Issue](https://github.com/xuxueli/xxl-mq/issues/) to discuss a new feature or change.

欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/xuxueli/xxl-mq/issues/) 讨论新特性或者变更。

## 接入登记
更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-mq/issues/1 ) 登记，登记仅仅为了产品推广。


## Copyright and License
This product is open source and free, and will continue to provide free community technical support. Individual or enterprise users are free to access and use.

- Licensed under the GNU General Public License (GPL) v3.
- Copyright (c) 2015-present, xuxueli.

产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。


## Donate
No matter how much the amount is enough to express your thought, thank you very much ：）     [To donate](http://www.xuxueli.com/page/donate.html )

无论金额多少都足够表达您这份心意，非常感谢 ：）      [前往捐赠](http://www.xuxueli.com/page/donate.html )
