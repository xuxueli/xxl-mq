package com.xxl.mq.client.topic;

import com.xxl.mq.client.consumer.annotation.MqConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TopicHelper {

    public static final String DEFAULT_GROUP = "DEFAULT";

    public static List<String> getTotalGroupList(String topic){
        List<String> groupList = new ArrayList<>();
        // TODO

        if (!groupList.contains(DEFAULT_GROUP)) {
            groupList.add(DEFAULT_GROUP);
        }
        return groupList;
    }

    public static void registerConsumers(Set<String> topicList) {
        // TODO, registry topiclist
    }

    public static ActiveInfo isActice(MqConsumer annotation){

        // TODO，逻辑调整
        return new ActiveInfo(1, 2, null);
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
