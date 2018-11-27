package com.xxl.mq.client.registry;

import com.xxl.rpc.registry.ServiceRegistry;
import com.xxl.rpc.util.BaseHttpUtil;
import com.xxl.rpc.util.BasicJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2018-11-24 22:48:57
 */
public class CommonServiceRegistry extends ServiceRegistry {
    private static Logger logger = LoggerFactory.getLogger(CommonServiceRegistry.class);

    public static final String REGISTRY_CENTER = "REGISTRY_CENTER";

    // param
    private String registryCenterAddress = null;

    private List<String> adminAddressArr = null;


    private volatile ConcurrentMap<String, TreeSet<String>> registryData = new ConcurrentHashMap<String, TreeSet<String>>();
    private volatile ConcurrentMap<String, TreeSet<String>> discoveryData = new ConcurrentHashMap<String, TreeSet<String>>();

    private Thread registryThread;
    private Thread discoveryThread;
    private volatile boolean registryThreadStop = false;


    @Override
    public void start(Map<String, String> param) {
        this.registryCenterAddress = param.get(REGISTRY_CENTER);

        // valid
        if (registryCenterAddress==null || registryCenterAddress.trim().length()==0) {
            throw new RuntimeException("xxl-mq registryCenterAddress can not be empty");
        }
        // admin address
        adminAddressArr = new ArrayList<>();
        if (registryCenterAddress.contains(",")) {
            adminAddressArr.add(registryCenterAddress);
        } else {
            adminAddressArr.addAll(Arrays.asList(registryCenterAddress.split(",")));
        }

        // registry thread
        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!registryThreadStop) {
                    try {
                        if (registryData.size() > 0) {

                            // change k-v to v-k
                            Map<String, Set<String>> regL2KMap = new HashMap<>(); // v-k[]
                            for (String regK : registryData.keySet()) {    // k - v[]
                                TreeSet<String> regL = registryData.get(regK);
                                for (String regVItem:regL) {
                                    Set<String> regKList = regL2KMap.get(regVItem);
                                    if (regKList == null) {
                                        regKList = new TreeSet<>();
                                        regL2KMap.put(regVItem, regKList);
                                    }
                                    regKList.add(regK);
                                }
                            }

                            // total registry
                            for (String vItem : regL2KMap.keySet()) {
                                doRegistry(adminAddressArr, regL2KMap.get(vItem), vItem);
                            }
                            logger.info(">>>>>>>>>> xxl-mq, refresh registry data success, registryData = {}", registryData);

                        }
                    } catch (Exception e) {
                        if (!registryThreadStop) {
                            logger.error(">>>>>>>>>> xxl-mq, refresh thread error.", e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (Exception e) {
                        if (!registryThreadStop) {
                            logger.error(">>>>>>>>>> xxl-mq, refresh thread error.", e);
                        }
                    }
                }
                logger.info(">>>>>>>>>> xxl-mq, refresh thread stoped.");
            }
        });
        registryThread.setName("xxl-mq, CommonServiceRegistry refresh thread.");
        registryThread.setDaemon(true);
        registryThread.start();

        // discovery thread
        discoveryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!registryThreadStop) {
                    try {
                        // long polling, monitor, timeout 30s
                        if (discoveryData.size() > 0) {
                            doMonitor(adminAddressArr, discoveryData.keySet());

                            // refreshDiscoveryData, all
                            refreshDiscoveryData(discoveryData.keySet());
                        }
                    } catch (Exception e) {
                        if (!registryThreadStop) {
                            logger.error(">>>>>>>>>> xxl-mq, refresh thread error.", e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        if (!registryThreadStop) {
                            logger.error(">>>>>>>>>> xxl-mq, refresh thread error.", e);
                        }
                    }
                }
                logger.info(">>>>>>>>>> xxl-mq, refresh thread stoped.");
            }
        });
        discoveryThread.setName("xxl-mq, NativeServiceRegistry refresh thread.");
        discoveryThread.setDaemon(true);
        discoveryThread.start();



        logger.info(">>>>>>>>>> xxl-mq, CommonServiceRegistry init success. [registryCenterAddress={}]", registryCenterAddress);
    }

    @Override
    public void stop() {
        registryThreadStop = true;
        if (registryThread != null) {
            registryThread.interrupt();
        }
        if (discoveryThread != null) {
            discoveryThread.interrupt();
        }
    }

    /**
     * refreshDiscoveryData, some or all
     */
    private void refreshDiscoveryData(Set<String> keys){
        if (keys.size() > 0) {
            // discovery mult
            Map<String, List<String>> keyValueListData = doDiscovery(adminAddressArr, keys);
                if (keyValueListData!=null) {
                for (String keyItem: keyValueListData.keySet()) {
                    discoveryData.put(keyItem, new TreeSet<String>(keyValueListData.get(keyItem)));
                }
            }
            logger.info(">>>>>>>>>> xxl-mq, refresh discovery data success, discoveryData = {}", discoveryData);
        }
    }

    @Override
    public boolean registry(Set<String> keys, String value) {
        // local cache
        for (String key : keys) {

            TreeSet<String> values = registryData.get(key);
            if (values == null) {
                values = new TreeSet<>();
                registryData.put(key, values);
            }
            values.add(value);
        }

        // remove mult
        boolean ret = doRegistry(adminAddressArr, keys, value);

        return ret;
    }

    @Override
    public boolean remove(Set<String> keys, String value) {
        for (String key : keys) {
            TreeSet<String> values = discoveryData.get(key);
            if (values != null) {
                values.remove(value);
            }
        }

        // remove mult
        boolean ret = doRemove(adminAddressArr, keys, value);

        return ret;
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {

        // find from local
        Map<String, TreeSet<String>> registryDataTmp = new HashMap<String, TreeSet<String>>();
        for (String key : keys) {
            TreeSet<String> valueSet = discoveryData.get(key);
            if (valueSet != null) {
                registryDataTmp.put(key, valueSet);
            }
        }

        // not find all, find from remote
        if (keys.size() != registryDataTmp.size()) {

            // refreshDiscoveryData, some, first use
            refreshDiscoveryData(keys);

            // find from local
            for (String key : keys) {
                TreeSet<String> valueSet = discoveryData.get(key);
                if (valueSet != null) {
                    registryDataTmp.put(key, valueSet);
                }
            }

        }

        return registryDataTmp;
    }

    @Override
    public TreeSet<String> discovery(String key) {
        Map<String, TreeSet<String>> keyValueSetTmp = discovery(new HashSet<String>(Arrays.asList(key)));
        if (keyValueSetTmp!=null) {
            return keyValueSetTmp.get(key);
        }
        return null;
    }



    // ---------------------- registry api ----------------------

    /**
     * get and valid
     *
     * @param url
     * @param params
     * @param timeout
     * @return
     */
    private static Map<String, Object> getAndValid(String url, Map<String, String> params, int timeout){

        // param
        boolean firstParam = true;
        for (String key: params.keySet()) {
            url += firstParam?"?":"&";
            if (firstParam) {
                firstParam = false;
            }
            url += key + "=" + params.get(key);
        }

        // resp json
        String respJson = BaseHttpUtil.get(url, timeout);
        if (respJson == null) {
            return null;
        }

        // parse obj
        Map<String, Object> respObj = new BasicJsonParser().parseMap(respJson);
        int code = Integer.valueOf(String.valueOf(respObj.get("code")));
        if (code != 200) {
            logger.info("discovery fail, msg={}", (respObj.containsKey("msg")?respObj.get("msg"):respJson) );
            return null;
        }
        return respObj;
    }

    /**
     * discovery
     *
     * @param adminAddressArr
     * @param keys
     * @return
     */
    private static Map<String, List<String>> doDiscovery(List<String> adminAddressArr, Set<String> keys) {
        for (String adminAddressUrl: adminAddressArr) {

            // url + param
            String url = adminAddressUrl + "/registry/discovery";
            Map<String, String> params = new HashMap<>();
            for (String key:keys) {
                params.put("keys", key);
            }

            // get and valid
            Map<String, Object> respObj = getAndValid(url, params, 10);

            // parse
            if (respObj!=null && respObj.containsKey("data")) {
                Map<String, List<String>> data = (Map<String, List<String>>) respObj.get("data");
                return data;
            }
        }

        return null;
    }

    /**
     * registry
     *
     * @param adminAddressArr
     * @param keys
     * @param value
     * @return
     */
    private static boolean doRegistry(List<String> adminAddressArr, Set<String> keys, String value) {

        for (String adminAddressUrl: adminAddressArr) {

            // url + param
            String url = adminAddressUrl + "/registry/registry";
            Map<String, String> params = new HashMap<>();
            for (String key:keys) {
                params.put("keys", key);
            }
            params.put("value", value);


            // get and valid
            Map<String, Object> respObj = getAndValid(url, params, 10);

            return respObj!=null?true:false;
        }
        return false;
    }

    /**
     * remove
     *
     * @param adminAddressArr
     * @param keys
     * @param value
     * @return
     */
    private static boolean doRemove(List<String> adminAddressArr, Set<String> keys, String value) {

        for (String adminAddressUrl: adminAddressArr) {

            // url + param
            String url = adminAddressUrl + "/registry/remove";
            Map<String, String> params = new HashMap<>();
            for (String key:keys) {
                params.put("keys", key);
            }
            params.put("value", value);

            // get and valid
            Map<String, Object> respObj = getAndValid(url, params, 10);

            return respObj!=null?true:false;
        }
        return false;
    }

    /**
     * monitor
     *
     * @param adminAddressArr
     * @param keys
     * @return
     */
    private static boolean doMonitor(List<String> adminAddressArr, Set<String> keys) {

        for (String adminAddressUrl: adminAddressArr) {

            // url + param
            String url = adminAddressUrl + "/registry/monitor";
            Map<String, String> params = new HashMap<>();
            for (String key:keys) {
                params.put("keys", key);
            }

            // get and valid
            Map<String, Object> respObj = getAndValid(url, params, 60);

            return respObj!=null?true:false;
        }
        return false;
    }

}
