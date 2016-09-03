package com.xxl.mq.client.rpc.util;

import java.util.Properties;

/**
 * 环境基类
 * @author xuxueli 2015-8-28 10:37:43
 */
public class Environment {


	/**
	 * rpc service address on zookeeper, service path : /xxl-mq/rpc/registrykey01/address01
     */
	public static final String ZK_SERVICES_PATH = "/xxl-mq/rpc";

	/**
	 * consumer name on zookeepr, consumerpath : /xxl-mq/consumer01/address01
     */
	public static final String ZK_CONSUMER_PATH = "/xxl-mq/consumer";

	/**
	 * zk config file
	 */
	private static final String ZK_ADDRESS_FILE = "/data/webapps/xxl-conf.properties";

	/**
	 * zk address
	 */
	public static final String ZK_ADDRESS;		// zk地址：格式	ip1:port,ip2:port,ip3:port
	
	static {
		Properties prop = PropertiesUtil.loadFileProperties(ZK_ADDRESS_FILE);
		ZK_ADDRESS = PropertiesUtil.getString(prop, "zkserver");
	}
	
	public static void main(String[] args) {
		System.out.println(ZK_ADDRESS);
	}

}

