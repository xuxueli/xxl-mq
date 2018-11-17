package com.xxl.mq.client.consumer.registry;

import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.rpc.util.IpUtil;

import java.util.*;

public class ConsumerRegistryHelper {

    public static final String DEFAULT_GROUP = "DEFAULT";
    public static final String CONSUMER_REGISTRY_RAMDOM_VALUE = IpUtil.getIp()
            .concat("_")
            .concat(String.valueOf(System.currentTimeMillis()))
            .concat("_")
            .concat(String.valueOf(10000 + new Random().nextInt(40000)));

    /**
     * consumer registry
     *
     *  /---/
     *      /topic1/
     *             /group1____
     *             /group1/ip-xxx02
     *      /topic2/
     *             /group1/ip-xxx02
     *
     * @param mqConsumer
     */
    public static void registerConsumer(MqConsumer mqConsumer) {
        // init data
        String registryKey = mqConsumer.topic();
        String registryValPrefix = mqConsumer.group().concat("____");
        String registryVal = registryValPrefix.concat(CONSUMER_REGISTRY_RAMDOM_VALUE);

        // registry consumer
        XxlMqClientFactory.getServiceRegistry().registry(registryKey, registryVal);
    }

    /**
     * isActice
     *
     * @param mqConsumer
     * @return
     */
    public static ActiveInfo isActice(MqConsumer mqConsumer){
        // init data
        String registryKey = mqConsumer.topic();
        String registryValPrefix = mqConsumer.group().concat("____");
        String registryVal = registryValPrefix.concat(CONSUMER_REGISTRY_RAMDOM_VALUE);

        // load all consumer
        TreeSet<String> onlineConsumerSet = XxlMqClientFactory.getServiceRegistry().discovery(registryKey);
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
            if (onlineConsumerItem.equalsIgnoreCase(registryVal)) {
                break;
            }
        }
        if (rank == -1) {
            return null;
        }

        return new ActiveInfo(rank, onlineConsumerSet_group.size(), onlineConsumerSet_group.toString());
    }

    public static Set<String> getTotalGroupList(String topic){
        Set<String> stringSet = new HashSet<>();

        // load all consumer, find all groups
        String registryKey = topic;
        TreeSet<String> onlineConsumerSet = XxlMqClientFactory.getServiceRegistry().discovery(registryKey);
        if (onlineConsumerSet!=null && onlineConsumerSet.size()>0) {
            for (String onlineConsumerItem : onlineConsumerSet) {
                    String[] onlineConsumerItemArr = onlineConsumerItem.split("____");
                    if (onlineConsumerItemArr!=null && onlineConsumerItemArr.length>1) {
                        String groupItem = onlineConsumerItemArr[0];
                        stringSet.add(groupItem);
                    }
            }
        }

        if (!stringSet.contains(DEFAULT_GROUP)) {
            stringSet.add(DEFAULT_GROUP);
        }
        return stringSet;
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
