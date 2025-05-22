package com.xxl.mq.core.bootstrap;

import com.xxl.mq.core.consumer.IConsumer;
import com.xxl.mq.core.consumer.annotation.XxlMq;
import com.xxl.mq.core.consumer.impl.MethodConsumer;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.thread.ConsumerThread;
import com.xxl.mq.core.thread.MessageThread;
import com.xxl.mq.core.thread.PullThread;
import com.xxl.mq.core.thread.RegistryThread;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.exception.BizException;
import com.xxl.tool.jsonrpc.JsonRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * xxl-mq bootstrap
 *
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(XxlMqBootstrap.class);


    // --------------------------------- instance ---------------------------------
    private static XxlMqBootstrap xxlMqBootstrap;
    public static XxlMqBootstrap getInstance() {
        if (xxlMqBootstrap == null) {
            throw new BizException("xxl-mq bootstrap not init.");
        }
        return xxlMqBootstrap;
    }


    // --------------------------------- base conf ---------------------------------

    private String address;
    private String accesstoken;
    private String appname;
    private int timeout;
    private int pullBatchsize;
    private int pullInterval;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAccesstoken() {
        return accesstoken;
    }

    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getPullBatchsize() {
        return pullBatchsize;
    }

    public void setPullBatchsize(int pullBatchsize) {
        this.pullBatchsize = pullBatchsize;
    }

    public int getPullInterval() {
        return pullInterval;
    }

    public void setPullInterval(int pullInterval) {
        this.pullInterval = pullInterval;
    }


    // --------------------------------- start / stop ---------------------------------

    private String instanceUuid;
    private RegistryThread registryThread = null;
    private MessageThread messageThread = null;
    private PullThread pullThread = null;

    public String getInstanceUuid() {
        return instanceUuid;
    }

    public MessageThread getMessageThread() {
        return messageThread;
    }

    public void start() {

        // 1、init param
        xxlMqBootstrap = this;
        instanceUuid = UUID.randomUUID().toString().replaceAll("-", "");

        // 2、build broker client
        buildBrokerClient();

        // 3、registryThread
        registryThread = new RegistryThread(this);
        registryThread.start();

        // 4、messageThread
        messageThread = new MessageThread(this);
        messageThread.start();

        // 5、pullThread
        pullThread = new PullThread(this);
        pullThread.start();
        logger.info(">>>>>>>>>>> xxl-mq XxlMqBootstrap started, instanceUuid = " + instanceUuid);
    }

    public void stop() {
        // 1、registryThread
        registryThread.stop();

        // 2、messageThread
        messageThread.stop();

        // 3、pullThread + consumerThread
        pullThread.stop();
        for (String topic : consumerThreadRepository.keySet()) {
            stopConsumerThread(topic);
        }
        logger.info(">>>>>>>>>>> xxl-mq XxlMqBootstrap stopped.");
    }


    // --------------------------------- client ---------------------------------

    private final String service = "brokerService";
    private final List<BrokerService> clientList = new ArrayList<>();

    /**
     * broker client
     */
    private void buildBrokerClient(){
        if (StringTool.isBlank(address)) {
            throw new BizException("xxl-mq bootstrap address can not be empty.");
        }

        if (StringTool.isBlank(accesstoken)) {
            throw new BizException("xxl-mq bootstrap accesstoken can not be empty.");
        }
        if (StringTool.isBlank(appname)) {
            throw new BizException("xxl-mq bootstrap appname can not be empty.");
        }
        if (!(timeout >=500 && timeout <= 3 * 1000)) {
            timeout = 3 * 1000;
        }

        // broker client
        List<String> addressList = Arrays.stream(address.split(",")).filter(StringTool::isNotBlank).collect(Collectors.toList());
        for (String url : addressList) {
            String finalUrl = url + "/openapi";

            JsonRpcClient jsonRpcClient = new JsonRpcClient(finalUrl, timeout);
            BrokerService brokerService = jsonRpcClient.proxy(service, BrokerService.class);
            clientList.add(brokerService);
        }
    }

    /**
     * load broker client
     *
     * @return
     */
    public BrokerService loadBrokerClient(){
        return clientList.get(ThreadLocalRandom.current().nextInt(clientList.size()));
    }


    // --------------------------------- consumer repository ---------------------------------

    private ConcurrentMap<String, IConsumer> consumerRepository = new ConcurrentHashMap<>();

    /**
     * load consumer
     */
    public IConsumer loadConsumer(String topic){
        return consumerRepository.get(topic);
    }

    /**
     * get consumer topic list
     */
    public List<String> getAllConsumerTopicList(){
        return new ArrayList<>(consumerRepository.keySet());
    }

    /**
     * registry consumer
     */
    public IConsumer registryConsumer(String topic, IConsumer consumer){
        logger.info(">>>>>>>>>>> xxl-mq register consumer success, topic:{}, consumer:{}", topic, consumer);
        return consumerRepository.put(topic, consumer);
    }

    /**
     * registry consumer of method
     */
    protected void registryMethodConsumer(XxlMq xxlMq, Object bean, Method executeMethod){
        if (xxlMq == null) {
            return;
        }

        // make and simplify the variables since they'll be called several times later
        String topic = xxlMq.value();
        Class<?> clazz = bean.getClass();
        String methodName = executeMethod.getName();
        if (topic.trim().isEmpty()) {
            throw new RuntimeException("xxl-mq method-consumer name invalid, for[" + clazz + "#" + methodName + "] .");
        }
        if (loadConsumer(topic) != null) {
            throw new RuntimeException("xxl-mq consumer[" + topic + "] naming conflicts.");
        }

        // execute method
        executeMethod.setAccessible(true);

        // init and destroy
        Method initMethod = null;
        Method destroyMethod = null;

        if (!xxlMq.init().trim().isEmpty()) {
            try {
                initMethod = clazz.getDeclaredMethod(xxlMq.init());
                initMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-mq method-consumer initMethod invalid, for[" + clazz + "#" + methodName + "] .");
            }
        }
        if (!xxlMq.destroy().trim().isEmpty()) {
            try {
                destroyMethod = clazz.getDeclaredMethod(xxlMq.destroy());
                destroyMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-mq method-consumer destroyMethod invalid, for[" + clazz + "#" + methodName + "] .");
            }
        }

        // registry consumer
        registryConsumer(topic, new MethodConsumer(bean, executeMethod, initMethod, destroyMethod));

    }


    // --------------------------------- consumer thread ---------------------------------

    private volatile Map<String, ConsumerThread> consumerThreadRepository = new ConcurrentHashMap<>();

    /**
     * lazy init consumer thread
     *
     * @param topic
     * @return
     */
    public ConsumerThread lazyInitConsumerThread(String topic) {

        ConsumerThread consumerThread = consumerThreadRepository.get(topic);

        // load - 1
        if (consumerThread == null) {
            synchronized (XxlMqBootstrap.class) {
                // load - 2
                consumerThread = consumerThreadRepository.get(topic);
                if (consumerThread == null) {

                    // init
                    IConsumer consumer = loadConsumer(topic);
                    if (consumer == null) {
                        throw new BizException("xxl-mq lazyInitConsumerThread fail, IConsumer[topic=" + topic + "] not found.");
                    }
                    consumerThreadRepository.put(topic, new ConsumerThread(xxlMqBootstrap, consumer));

                    // load - 3
                    consumerThread = consumerThreadRepository.get(topic);
                }
            }
        }
        return consumerThread;
    }

    /**
     * stop consumer thread
     *
     * @param topic
     */
    public void stopConsumerThread(String topic){
        ConsumerThread consumerThread = consumerThreadRepository.get(topic);
        if (consumerThread != null) {
            synchronized (XxlMqBootstrap.class) {
                consumerThread.stop();
                consumerThreadRepository.remove(topic);
            }
        }
    }

    /**
     * get free consumer topic list
     *
     * @return
     */
    public List<String> getFreeConsumerTopicList() {

        // prepare param
        List<String> allTopicList = getAllConsumerTopicList();
        List<String> busyTopicList = consumerThreadRepository.entrySet().stream()
                .filter(entry -> entry.getValue().isBusy())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // filter free topic
        List<String> freeTopicList = allTopicList.stream()
                .filter(topic -> !busyTopicList.contains(topic))
                .collect(Collectors.toList());

        return freeTopicList;
    }

    /**
     * stop idle consumer thread
     */
    public void stopIdleConsumerThead() {
        for (String topic : consumerThreadRepository.keySet()) {
            ConsumerThread consumerThread = consumerThreadRepository.get(topic);
            if (consumerThread != null && consumerThread.isIdle()) {
                stopConsumerThread(topic);
            }
        }
    }

}