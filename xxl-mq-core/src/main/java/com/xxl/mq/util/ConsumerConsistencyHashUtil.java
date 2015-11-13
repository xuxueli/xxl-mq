package com.xxl.mq.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性hash
 * @author xuxueli 2015-10-26 22:50:29
 * 
 * 《普通hash方法》
 * NODE节点 : 有序数组或list列表, 如缓存redis服务器ip列表;
 * 数据节点 : 支持获取hashCode, 如redis key;
 * 映射方法 : key.hashCode % NODE.size
 * 优点 : 简单,高效;
 * 缺点 : NODE增减, 所有映射都会失效, 如redis服务某一个节点宕机, 所有持久化文件要做数据迁移, 其他缓存全部失效;
 * 
 * 《一致性hash算法》
 * hash环 : 抽象概念, 一张巨大的hash环装链表; 
 * NODE节点 : 对NODE进行hash计算, 散列对应到hash环上某一个位置;
 * NODE节点-虚拟节点 : 对每个NODE节点生成一定量虚拟节点, 每个NODE虚拟节点都对关联到一个真实NODE节点, 对虚拟节点进行hash计算, 散列对应到hash换上某一个位置;
 * 数据节点 : 对数据进行hash计算, 对应到hash环上某一个位置, 然后顺时针找到最近的NODE节点, 命中然后存储;
 * 
 * 优点 : NODE增减, 改节点原NODE节点的命中会漂移到相邻的后一个NODE节点, 不会造成整体失效, 只会影响其中一个节点;
 * 缺点 : 理解和维护起来, 需要一定学习成本;
 * 缺点(已解决) : NODE其中一个节点失效, 该节点数据瞬间映射到下一个节点, 会造成例如 “缓存雪崩”现象, 在此引入NODE虚拟节点, 可以将该节点数据, 平衡的散列到其他存活的NODE节点中;
 * 
 * --- 0 --------- node1_1 ----------- node2_2 --------- node1_2 ------ node2_1 ------ 2^64|0 ---
 * -------- key1 --------- key02 ---------
 */
public class ConsumerConsistencyHashUtil {

	private List<String> shardNodes;
	private final int NODE_NUM = 1000;
	private TreeMap<Long, String> virtualHash2RealNode = new TreeMap<Long, String>();

	/**
	 * init consistency hash ring, put virtual node on the 2^64 ring
	 */
	public void initVirtual2RealRing(List<String> shards) {
		this.shardNodes = shards;
		for (String node : shardNodes) {
			for (int i = 0; i < NODE_NUM; i++){
				long hashCode = hash("SHARD-" + node + "-NODE-" + i);
				virtualHash2RealNode.put(hashCode, node);
			}
		}
	}

	/**
	 * get real node by key's hash on the 2^64
	 */
	public String getShardInfo(String key) {
		long hashCode = hash(key);
		SortedMap<Long, String> tailMap = virtualHash2RealNode.tailMap(hashCode);
		if (tailMap.isEmpty()) {
			return virtualHash2RealNode.get(virtualHash2RealNode.firstKey());
		}
		return virtualHash2RealNode.get(tailMap.firstKey());
	}
	
	/**
     * prinf ring virtual node info
     */
     public void printMap() {
         System.out.println(virtualHash2RealNode);
     }

	/**
	 *  MurMurHash算法，是非加密HASH算法，性能很高，
	 *  比传统的CRC32,MD5，SHA-1（这两个算法都是加密HASH算法，复杂度本身就很高，带来的性能上的损害也不可避免）
	 *  等HASH算法要快很多，而且据说这个算法的碰撞率很低.
	 *  http://murmurhash.googlepages.com/
	 */
	public static Long hash(String key) {
		
		ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
		int seed = 0x1234ABCD;
		
		ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(
                    ByteOrder.LITTLE_ENDIAN);
            // for big-endian version, do this first:
            // finish.position(8-buf.remaining());
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;
	}
	
	/**
     * get hash code on 2^32 ring (md5散列的方式计算hash值)
     * @param digest
     * @param nTime
     * @return
     */
	public static long hash2(String key) {

		// md5 byte
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 not supported", e);
		}
		md5.reset();
		byte[] keyBytes = null;
		try {
			keyBytes = key.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unknown string :" + key, e);
		}

		md5.update(keyBytes);
		byte[] digest = md5.digest();

		// hash code, Truncate to 32-bits
		long hashCode = ((long) (digest[3] & 0xFF) << 24)
				| ((long) (digest[2] & 0xFF) << 16)
				| ((long) (digest[1] & 0xFF) << 8) 
				| (digest[0] & 0xFF);

		long truncateHashCode = hashCode & 0xffffffffL;
		return truncateHashCode;
	}
	 
	public static void main(String[] args) {
		List<String> shards = new ArrayList<String>();
   	 	shards.add("consumer-uuid-2");
   	 	shards.add("consumer-uuid-1");
   	 
		ConsumerConsistencyHashUtil sh = new ConsumerConsistencyHashUtil();
		sh.initVirtual2RealRing(shards);
		sh.printMap();
		
		int consumer1 = 0;
		int consumer2 = 0;
		for (int i = 0; i < 10000; i++) {
			String key = "consumer" + i;
			System.out.println(hash(key) + ":" + sh.getShardInfo(key));
			if ("consumer-uuid-1".equals(sh.getShardInfo(key))) {
				consumer1++;
			}
			if ("consumer-uuid-2".equals(sh.getShardInfo(key))) {
				consumer2++;
			}
		}
		System.out.println("consumer1:" + consumer1);
		System.out.println("consumer2:" + consumer2);
		
		/*long start = System.currentTimeMillis();
		for (int i = 0; i < 1000 * 1000 * 1000; i++) {
			if (i % (100 * 1000 * 1000) == 0) {
				System.out.println(i + ":" + hash("key1" + i));
			}
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);*/
	}

}
