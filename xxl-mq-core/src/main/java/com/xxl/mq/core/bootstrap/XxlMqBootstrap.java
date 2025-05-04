package com.xxl.mq.core.bootstrap;

import com.xxl.mq.core.consumer.Consumer;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.MessageData;
import com.xxl.mq.core.thread.ConsumerThread;
import com.xxl.mq.core.thread.PullThread;
import com.xxl.mq.core.thread.RegistryThread;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.exception.BizException;
import com.xxl.tool.jsonrpc.JsonRpcClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * xxl-mq bootstrap
 *
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqBootstrap {

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

    private String instanceUuid;

    public XxlMqBootstrap(String address, String accesstoken, String appname) {
        this.address = address;
        this.accesstoken = accesstoken;
        this.appname = appname;

        // uuid
        instanceUuid = UUID.randomUUID().toString().replaceAll("-", "");

        // instance
        xxlMqBootstrap = this;
    }

    public String getAddress() {
        return address;
    }

    public String getAccesstoken() {
        return accesstoken;
    }

    public String getAppname() {
        return appname;
    }

    // --------------------------------- start / stop ---------------------------------

    private RegistryThread registryThread = null;
    private PullThread pullThread = null;

    public void start() {
        // 0、initBrokerClient
        buildBrokerClient();

        // 1、registryThread
        registryThread = new RegistryThread();
        registryThread.start();

        // 2、produceThread
        // todo

        // 3、pullThread
        pullThread = new PullThread(this);
        pullThread.start();
    }

    public void stop() {
        // 1、registryThread
        registryThread.stop();

        // 2、produceThread

        // 3、pullThread + consumerThread
        pullThread.stop();
        for (ConsumerThread consumerThread : consumerMap.values()) {
            consumerThread.stop();
        }

    }


    // --------------------------------- client ---------------------------------

    private final String service = "brokerService";
    private final int timeout = 3 * 1000;
    private final List<BrokerService> clientList = new ArrayList<>();

    /**
     * broker client
     */
    private void buildBrokerClient(){
        if (StringTool.isBlank(address)) {
            throw new BizException("xxl-mq bootstrap address not config.");
        }

        // broker client
        List<String> addressList = Arrays.stream(address.split(",")).filter(StringTool::isNotBlank).collect(Collectors.toList());
        for (String url : addressList) {
            JsonRpcClient jsonRpcClient = new JsonRpcClient(url, timeout);
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


    // --------------------------------- consumer ---------------------------------

    private volatile Map<String, ConsumerThread> consumerMap = new ConcurrentHashMap<>();

    /**
     * register consumer
     *
     * @param topic
     * @param consumer
     */
    public void register(String topic, Consumer consumer) {

        // todo, support spring

        synchronized (XxlMqBootstrap.class) {
            if (consumerMap.containsKey(topic)) {
                System.out.println("register fail, topic conflict");
            }
            consumerMap.put(topic, new ConsumerThread(consumer));
        }
    }

    /**
     * get consumer
     *
     * @param topic
     * @return
     */
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


    // --------------------------------- produce ---------------------------------

    public void produce(MessageData message) {
        // brokerClient.produce(msg);   // todo
    }

}