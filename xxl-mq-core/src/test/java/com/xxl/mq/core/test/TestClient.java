package com.xxl.mq.core.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * MQ Client, TODO
 */
public class TestClient {

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
        Bootstrap factory = new Bootstrap();
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
        factory.stop();
    }

    public static class Bootstrap {

        private volatile Map<String, ConsumerThread> consumerMap = new ConcurrentHashMap<>();
        private RegistryThread registryThread = null;
        private BrokerClientMock brokerClient = null;
        private PullThread pullThread = null;

        public void start() {
            // init
            brokerClient = new BrokerClientMock();

            registryThread = new RegistryThread();
            registryThread.start();

            pullThread = new PullThread(this);
            pullThread.start();
        }

        public void stop() {
            registryThread.stop();
            pullThread.stop();
            for (ConsumerThread consumer : consumerMap.values()) {
                consumer.stop();
            }
        }

        public BrokerClientMock getBrokerClient() {
            return brokerClient;
        }

        public ConsumerThread getConsumer(String topic) {
            return consumerMap.get(topic);
        }

        public List<String> getFreeConsumers() {
            List<String> freeConsumer = consumerMap.entrySet().stream()
                    .filter(entry -> entry.getValue().isBusy())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            return freeConsumer;
        }

        public void register(String topic, Consumer consumer) {
            synchronized (Bootstrap.class) {
                if (consumerMap.containsKey(topic)) {
                    System.out.println("register fail, topic conflict");
                }
                consumerMap.put(topic, new ConsumerThread(consumer));
            }
        }

        public void produce(Message msg) {
            brokerClient.produce(msg);
        }

    }

    private static class ConsumerThread {
        private final Consumer consumer;
        private ScheduledThreadPoolExecutor scheduledExecutorService;

        public ConsumerThread(Consumer consumer) {
            this.consumer = consumer;
            this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        }

        public void stop() {
            List<Runnable> unfinishedTasks = new ArrayList<>();
            try {
                // 1. 温和关闭，不接收新任务
                scheduledExecutorService.shutdown();
                // 2. 等待20秒
                boolean terminated = scheduledExecutorService.awaitTermination(20, TimeUnit.SECONDS);
                // 3. 如果20秒后还未完全终止
                if (!terminated) {
                    // 4. 强制关闭并获取剩余任务
                    unfinishedTasks = scheduledExecutorService.shutdownNow();
                    // 5. 再次等待确保完全终止
                    scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
                }

            } catch (InterruptedException e) {
                // 6. 发生中断时，强制关闭
                unfinishedTasks = scheduledExecutorService.shutdownNow();
                //Thread.currentThread().interrupt();
            }
            System.out.println("unfinishedTasks = " + unfinishedTasks);
        }

        public void accept(Message msg) {
            scheduledExecutorService.schedule(
                    new MessageRunable(msg, consumer),
                    msg.getEffectTime().getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        public boolean isBusy() {
            return scheduledExecutorService.getQueue().size() > 10;
        }
    }

    public static class MessageRunable implements Runnable {

        private final Message msg;
        private final Consumer consumer;

        public MessageRunable(Message msg, Consumer consumer) {
            this.msg = msg;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            consumer.consume(msg);
        }
    }

    public static class PullThread {

        private volatile boolean running = false;
        private Thread pullThread = null;

        private final Bootstrap bootstrap;

        public PullThread(Bootstrap bootstrap) {
            this.bootstrap = bootstrap;
        }

        public void start() {
            running = true;
            pullThread = new Thread(() -> {
                while (running) {
                    List<Message> msgList = bootstrap.getBrokerClient().pull();
                    //System.out.println("pull running , FreeConsumers = " + bootstrap.getFreeConsumers());
                    if (msgList != null && !msgList.isEmpty()) {
                        for (Message msg : msgList) {
                            ConsumerThread consumer = bootstrap.getConsumer(msg.getTopic());
                            if (consumer == null) {
                                System.out.println("topic not found, msg = " + msg);
                                continue;
                            }
                            consumer.accept(msg);
                            ;
                        }
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            });
            pullThread.start();
        }

        public void stop() {
            running = false;
        }
    }

    public static class BrokerClientMock {
        private volatile LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

        public void start() {
            //
        }

        public void stop() {
            //
        }

        public void produce(Message msg) {
            messageQueue.add(msg);
        }

        public List<Message> pull() {
            List<Message> messageList = new ArrayList<>();
            messageQueue.drainTo(messageList, 50);
            return messageList;
        }
    }

    public static class RegistryThread {
        private volatile boolean running = false;
        private Thread registryThread = null;

        public void start() {
            running = true;

            // registry
            registryThread = new Thread(() -> {
                while (!running) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        System.out.println(formatDate(new Date()) + " registry beat...");
                    } catch (InterruptedException ignored) {
                    }
                }
            });
            registryThread.start();
        }

        public void stop() {
            running = false;
            registryThread.interrupt();
        }
    }

    public interface Consumer {
        void consume(Message msg);
    }

    public static class Message {
        private String topic;
        private String data;
        private Date effectTime;

        public Message(String topic, String data, Date effectTime) {
            this.topic = topic;
            this.data = data;
            this.effectTime = effectTime;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public Date getEffectTime() {
            return effectTime;
        }

        public void setEffectTime(Date effectTime) {
            this.effectTime = effectTime;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "topic='" + topic + '\'' +
                    ", data='" + data + '\'' +
                    ", effectTime=" + formatDate(effectTime) +
                    '}';
        }
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

}