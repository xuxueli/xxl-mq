# 异步通讯框架xxl-mq
github地址：https://github.com/xuxueli/xxl-mq

git.osc地址：http://git.oschina.net/xuxueli0323/xxl-mq

博客地址(内附使用教程)：http://www.cnblogs.com/xuxueli/p/4918535.html

技术交流群(仅作技术交流)：367260654

## V1.1规划
- 1、消息新增;
- 2、重试次数,异常时,将会扣减重试次数,并不会修改状态;
- 3、Mq线程内,队列方式执行, Topic-ZK向Mq线程队列push消息实现广播;
- 4、文档,发布;

##### 角色
- mq: 消息
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
- producer: 生产者
- consumer: 消费者

- broker: 代理, 负责: 1、接收 producer 生产的消息, 2、向 consumer 推送订阅的消息, 3、接受 consumer 对消息的消费结果回调;
    - message 需要登记: 1、主题下一旦有消息数据,不可删除; 只可修改备注 ,1、broker 只服务登记的消息;2、登记便于报表统计;3、便于邮件报警;4、
- client: 提供 producer 和 consumer 支持;

## 简介：
	一款轻量级、设计极简的 “异步通讯框架” ；
	支持Topic和Queue两种异步通讯模型；
	去中心化，可插拔式，完美集成spring；
