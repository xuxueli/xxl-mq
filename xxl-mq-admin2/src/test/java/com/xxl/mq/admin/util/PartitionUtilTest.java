package com.xxl.mq.admin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PartitionUtilTest {

    @Test
    public void test(){
        System.out.println(PartitionUtil.allocatePartition(Arrays.asList("aa")));
        System.out.println(PartitionUtil.allocatePartition(Arrays.asList("aa", "bb")));
        System.out.println(PartitionUtil.allocatePartition(Arrays.asList("aa", "bb", "cc")));
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("instance_"+ i);
        }
        System.out.println(PartitionUtil.allocatePartition(list));
    }

    @Test
    public void test2(){

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add("instance_"+ i);
        }
        Map<String, PartitionUtil.PartitionRange> partitionRangeMap = PartitionUtil.allocatePartition(list);

        List<Integer> boundaryPoint = new ArrayList<>();
        for (Map.Entry<String, PartitionUtil.PartitionRange> entry : partitionRangeMap.entrySet()) {
            if (boundaryPoint.contains(entry.getValue().getPartitionIdFrom())) {
                System.out.println("PartitionIdFrom conflict: " + entry);
                Assertions.fail();  // PartitionIdFrom conflict, 会导致重复消费
            }
            if (boundaryPoint.contains(entry.getValue().getPartitionIdTo())) {
                System.out.println("getPartitionIdTo conflict: " + entry);
                Assertions.fail();  // getPartitionIdTo conflict, 会导致重复消费
            }
            boundaryPoint.add(entry.getValue().getPartitionIdFrom());
            boundaryPoint.add(entry.getValue().getPartitionIdTo());
        }

        Assertions.assertTrue(boundaryPoint.contains(1), "最小分片 遗漏");
        Assertions.assertTrue(boundaryPoint.contains(10000), "最小分片 遗漏");

        System.out.println(boundaryPoint);
    }

}
