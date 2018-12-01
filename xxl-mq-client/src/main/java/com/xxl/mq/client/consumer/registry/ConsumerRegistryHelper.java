package com.xxl.mq.client.consumer.registry;

import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.consumer.thread.ConsumerThread;
import com.xxl.mq.client.registry.XxlRegistryServiceRegistry2;
import com.xxl.registry.client.model.XxlRegistryParam;

import java.util.*;

/**
 * @author xuxueli 2018-11-18 21:18:10
 */
public class ConsumerRegistryHelper {

    private XxlRegistryServiceRegistry2 serviceRegistry;
    public ConsumerRegistryHelper(XxlRegistryServiceRegistry2 serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }


    // ---------------------- util ----------------------
    private static final String SpaceMark = "_consumer_";

    private static String makeRegistryKey(String topic){
        String registryKey = SpaceMark.concat(topic);
        return registryKey;
    }
    private static String makeRegistryValPrefix(String group){
        String registryValPrefix = group.concat(SpaceMark);
        return registryValPrefix;
    }
    private static String makeRegistryVal(String group, String consumerUuid){
        String registryValPrefix = makeRegistryValPrefix(group);
        String registryVal = registryValPrefix.concat(consumerUuid);
        return registryVal;
    }
    private static String parseGroupFromRegistryVal(String registryVal){
        String[] onlineConsumerItemArr = registryVal.split(SpaceMark);
        if (onlineConsumerItemArr!=null && onlineConsumerItemArr.length>1) {
            String group = onlineConsumerItemArr[0];
            return group;
        }
        return null;
    }

    // ---------------------- api ----------------------

    /**
     * consumer registry
     *
     * @param consumerThreadList
     */
    public void registerConsumer(List<ConsumerThread> consumerThreadList) {

        List<XxlRegistryParam> registryParamList = new ArrayList<>();
        for (ConsumerThread consumerThread: consumerThreadList) {
            String registryKey = makeRegistryKey(consumerThread.getMqConsumer().topic());
            String registryVal = makeRegistryVal(consumerThread.getMqConsumer().group(), consumerThread.getUuid());
            registryParamList.add(new XxlRegistryParam(registryKey, registryVal));
        }

        serviceRegistry.registry(registryParamList);
    }

    /**
     * consumer registry remove
     */
    public void removeConsumer(List<ConsumerThread> consumerThreadList){
        List<XxlRegistryParam> registryParamList = new ArrayList<>();
        for (ConsumerThread consumerThread: consumerThreadList) {
            String registryKey = makeRegistryKey(consumerThread.getMqConsumer().topic());
            String registryVal = makeRegistryVal(consumerThread.getMqConsumer().group(), consumerThread.getUuid());
            registryParamList.add(new XxlRegistryParam(registryKey, registryVal));
        }

        serviceRegistry.remove(registryParamList);
    }

    /**
     * isActice
     *
     * @param consumerThread
     * @return
     */
    public ActiveInfo isActice(ConsumerThread consumerThread){
        // init data
        String registryKey = makeRegistryKey(consumerThread.getMqConsumer().topic());
        String registryValPrefix = makeRegistryValPrefix(consumerThread.getMqConsumer().group());
        String registryVal = makeRegistryVal(consumerThread.getMqConsumer().group(), consumerThread.getUuid());

        // load all consumer
        TreeSet<String> onlineConsumerSet = serviceRegistry.discovery(registryKey);
        if (onlineConsumerSet==null || onlineConsumerSet.size()==0) {
            return null;
        }

        // filter by group
        TreeSet<String> onlineConsumerSet_group = new TreeSet<String>();
        for (String onlineConsumerItem : onlineConsumerSet) {
            if (onlineConsumerItem.startsWith(registryValPrefix)) {
                onlineConsumerSet_group.add(onlineConsumerItem);
            }
        }
        if (onlineConsumerSet_group==null || onlineConsumerSet_group.size()==0) {
            return null;
        }

        // rank
        int rank = -1;
        for (String onlineConsumerItem : onlineConsumerSet_group) {
            rank++;
            if (onlineConsumerItem.equals(registryVal)) {
                break;
            }
        }
        if (rank == -1) {
            return null;
        }

        return new ActiveInfo(rank, onlineConsumerSet_group.size(), onlineConsumerSet_group.toString());
    }

    /**
     * get total group list
     */
    public Set<String> getTotalGroupList(String topic){
        // init data
        String registryKey = makeRegistryKey(topic);


        // load all consumer, find all groups
        Set<String> groupSet = new HashSet<>();
        TreeSet<String> onlineConsumerRegistryValList = serviceRegistry.discovery(registryKey);

        if (onlineConsumerRegistryValList!=null && onlineConsumerRegistryValList.size()>0) {
            for (String onlineConsumerRegistryValItem : onlineConsumerRegistryValList) {
                String groupItem = parseGroupFromRegistryVal(onlineConsumerRegistryValItem);
                if (groupItem!=null && groupItem.length()>1) {
                    groupSet.add(groupItem);
                }
            }
        }

        if (!groupSet.contains(MqConsumer.DEFAULT_GROUP)) {
            groupSet.add(MqConsumer.DEFAULT_GROUP);
        }
        return groupSet;
    }

    public static class ActiveInfo{
        // consumer rank
        public int rank;
        // alive num
        public int total;
        // registry rank info
        public String registryRankInfo;

        public ActiveInfo(int rank, int total, String registryRankInfo) {
            this.rank = rank;
            this.total = total;
            this.registryRankInfo = registryRankInfo;
        }

        @Override
        public String toString() {
            return "ActiveInfo{" +
                    "rank=" + rank +
                    ", total=" + total +
                    ", registryRankInfo='" + registryRankInfo + '\'' +
                    '}';
        }
    }

}
