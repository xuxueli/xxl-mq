package com.xxl.mq.client.rpc.util;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * ZooKeeper cfg client (Watcher + some utils)
 * @author xuxueli 2015年8月26日21:36:43
 *
 *         Zookeeper
 *         从设计模式角度来看，是一个基于观察者模式设计的分布式服务管理框架，它负责存储和管理大家都关心的数据，然后接受观察者的注册
 *         ，一旦这些数据的状态发生变化，Zookeeper 就将负责通知已经在 Zookeeper
 *         上注册的那些观察者做出相应的反应，从而实现集群中类似 Master/Slave 管理模式
 *
 *         1、统一命名服务（Name Service）:将有层次的目录结构关联到一定资源上，广泛意义上的关联，也许你并不需要将名称关联到特定资源上，
 *         你可能只需要一个不会重复名称。
 *
 *         2、配置管理（Configuration Management）：分布式统一配置管理：将配置信息保存在
 *         Zookeeper 的某个目录节点中，然后将所有需要修改的应用机器监控配置信息的状态，一旦配置信息发生变化，每台应用机器就会收到
 *         Zookeeper 的通知，然后从 Zookeeper 获取新的配置信息应用到系统中
 *
 *         3、集群管理（Group
 *         Membership）:Zookeeper 能够很容易的实现集群管理的功能，如有多台 Server 组成一个服务集群，那么必须
 *         要一个“总管”知道当前集群中每台机器的服务状态，一旦有机器不能提供服务，集群中其它集群必须知道，从而做出调整重新分配服务策略。
 *         同样当增加集群的服务能力时，就会增加一台或多台 Server，同样也必须让“总管”知道。
 *
 *         4、共享锁（Locks）：
 *         5、队列管理：a、当一个队列的成员都聚齐时，这个队列才可用，否则一直等待所有成员到达，这种是同步队列。b、队列按照 FIFO 方式
 *         进行入队和出队操作，例如实现生产者和消费者模型。
 *
 *         集中式配置管理 动态更新
 *
	<!-- zookeeper -->
	<dependency>
		<groupId>org.apache.zookeeper</groupId>
		<artifactId>zookeeper</artifactId>
		<version>3.4.6</version>
	</dependency>
 *
 */
public class ZookeeperUtil {
	private static Logger logger = LoggerFactory.getLogger(ZookeeperUtil.class);

	// ------------------------------ zookeeper new instance ------------------------------
	public static ZooKeeper getNewInstance(String multAddress , Watcher watcher) throws IOException {
		ZooKeeper zooKeeper = new ZooKeeper(multAddress, 30000, watcher);
		return zooKeeper;
	}

	/*final CountDownLatch countDownLatch = new CountDownLatch(1);
	countDownLatch.countDown();
	countDownLatch.await();*/
	/*private static ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(true);
	if (INSTANCE_INIT_LOCK.tryLock(2, TimeUnit.SECONDS)) {}*/

	// ------------------------------ util ------------------------------

	/**
	 * create node path with parent path (如果父节点不存在,循环创建父节点, 因为父节点不存在zookeeper会抛异常)
	 * @param path	()
	 */
	public static Stat createWithParent(ZooKeeper zooKeeper, String path){
		// valid
		if (path==null || path.trim().length()==0) {
			return null;
		}

		try {
			Stat stat = zooKeeper.exists(path, false);
			if (stat == null) {
				//  valid parent, createWithParent if not exists
				if (path.lastIndexOf("/") > 0) {
					String parentPath = path.substring(0, path.lastIndexOf("/"));
					Stat parentStat = zooKeeper.exists(parentPath, false);
					if (parentStat == null) {
						createWithParent(zooKeeper, parentPath);
					}
				}
				// create desc node path
				zooKeeper.create(path, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			return zooKeeper.exists(path, false);
		} catch (KeeperException e) {
			logger.error("", e);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
		return null;
	}

	/**
	 * delete path
	 * @param path
	 */
	public static void deletePath(ZooKeeper zooKeeper, String path){
		try {
			Stat stat = zooKeeper.exists(path, false);
			if (stat != null) {
				zooKeeper.delete(path, stat.getVersion());
			} else {
				logger.info(">>>>>>>>>> path not found :{}", path);
			}
		} catch (KeeperException e) {
			logger.error("", e);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}

	/**
	 * set data to node
	 * @param path
	 * @param data
	 * @return
	 */
	public static Stat setPathDataByKey(ZooKeeper zooKeeper, String path, String data) {
		try {
			Stat stat = zooKeeper.exists(path, false);
			if (stat == null) {
				createWithParent(zooKeeper, path);
				stat = zooKeeper.exists(path, false);
			}
			return zooKeeper.setData(path, data.getBytes(),stat.getVersion());
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}

	/**
	 * get data from node
	 * @param path
	 * @return
	 */
	public static String getPathData(ZooKeeper zooKeeper, String path){
		try {
			Stat stat = zooKeeper.exists(path, false);
			if (stat != null) {
				String znodeValue = null;
				byte[] resultData = zooKeeper.getData(path, false, null);
				if (resultData != null) {
					znodeValue = new String(resultData);
				}
				return znodeValue;
			} else {
				logger.info(">>>>>>>>>> path not found: {}", path);
			}
		} catch (KeeperException e) {
			logger.error("", e);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
		return null;
	}

	/**
	 * get data of child note list
	 * @return
	 */
	public static Map<String, String> getChildListData(ZooKeeper zooKeeper, String parentPath){
		Map<String, String> childListData = new HashMap<String, String>();
		try {
			List<String> childPathList = zooKeeper.getChildren(parentPath, false);
			if (childPathList!=null && childPathList.size()>0) {
				for (String childNode : childPathList) {
					String data = getPathData(zooKeeper, parentPath + "/" + childNode);
					childListData.put(childNode, data);
				}
			}
		} catch (KeeperException e) {
			logger.error("", e);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
		return childListData;
	}

}