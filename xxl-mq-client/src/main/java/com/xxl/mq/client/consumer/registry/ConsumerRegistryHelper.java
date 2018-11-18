package com.xxl.mq.client.consumer.registry;

import com.xxl.mq.client.consumer.thread.ConsumerThread;
import com.xxl.rpc.registry.ServiceRegistry;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class ConsumerRegistryHelper {

    public static final String DEFAULT_GROUP = "DEFAULT";

    private ServiceRegistry serviceRegistry;
    public ConsumerRegistryHelper(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }


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
     * @param consumerThread
     */
    public void registerConsumer(ConsumerThread consumerThread) {
        // init data
        String registryKey = consumerThread.getMqConsumer().topic();
        String registryValPrefix = consumerThread.getMqConsumer().group().concat("[CONSUMER]");
        String registryVal = registryValPrefix.concat(consumerThread.getUuid());

        // registry consumer
        serviceRegistry.registry(registryKey, registryVal);
    }

    /**
     * isActice
     *
     * @param consumerThread
     * @return
     */
    public ActiveInfo isActice(ConsumerThread consumerThread){
        // init data
        String registryKey = consumerThread.getMqConsumer().topic();
        String registryValPrefix = consumerThread.getMqConsumer().group().concat("[CONSUMER]");
        String registryVal = registryValPrefix.concat(consumerThread.getUuid());

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
            if (onlineConsumerItem.equalsIgnoreCase(registryVal)) {
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
        String registryKey = topic;
        String registryValGropSplice = "[CONSUMER]";


        // load all consumer, find all groups
        Set<String> stringSet = new HashSet<>();
        TreeSet<String> onlineConsumerSet = serviceRegistry.discovery(registryKey);

        if (onlineConsumerSet!=null && onlineConsumerSet.size()>0) {
            for (String onlineConsumerItem : onlineConsumerSet) {
                    String[] onlineConsumerItemArr = onlineConsumerItem.split(registryValGropSplice);
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
