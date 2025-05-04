package com.xxl.mq.core.test;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        /**
         * - MqFactory
         *      - spring
         *          - SpringFactory：扫描 @XxlMq                                          【线程数，默认单线程】
         *      - DefaultFactory：手动注册 MqHandler
         *      - BrokerClient
         *      - Start Thread
         * - Thread
         *      - 1、初始化 + 注册
         *      - 2、消息生产：queue；
         *      - 3、消息Pull：定期Pull（3s/次；锁定 ，30s内 + 50条；） + 负载自适应（空闲topic）； 【配置：间隔/3s、批次/50】
         *      - 4、消息消费：
         *          - 单消费者：Topic - MqTimeWheel；
         *            - 时间轮：
         *            - 消费线程 + 队列/余量；维护忙碌状态，容量<50%，空闲；
         * - OpenApi
         * - Consumer：
         *      - @XxlMq
         *      - MqHandler
         * MqHelper：
         *      - 生产消息：推送生产队列
         */

        // 1、init
        /*Bootstrap factory = new Bootstrap();
        factory.register("topic01", msg -> System.out.println(formatDate(new Date()) + " topic01 consume:" + msg));
        factory.register("topic02", msg -> System.out.println(formatDate(new Date()) + " topic02 consume:" + msg));
        factory.register("topic03", msg -> System.out.println(formatDate(new Date()) + " topic03 consume:" + msg));

        factory.start();

        // 2、mock
        for (int i = 1; i <= 10; i++) {
            String topic = "topic0" + (i % 3 + 1);
            Date effectTime = new Date(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(30) * 1000);
            factory.produce(new Message(topic, "effectTime = " + formatDate(effectTime), effectTime));
        }

        TimeUnit.SECONDS.sleep(60);
        factory.stop();*/
    }


}
