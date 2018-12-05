package com.xxl.mq.admin.service.impl;

import com.xxl.mq.admin.core.model.XxlCommonRegistry;
import com.xxl.mq.admin.core.model.XxlCommonRegistryData;
import com.xxl.mq.admin.core.model.XxlCommonRegistryMessage;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.core.util.JacksonUtil;
import com.xxl.mq.admin.core.util.PropUtil;
import com.xxl.mq.admin.dao.IXxlCommonRegistryDao;
import com.xxl.mq.admin.dao.IXxlCommonRegistryDataDao;
import com.xxl.mq.admin.dao.IXxlCommonRegistryMessageDao;
import com.xxl.mq.admin.service.XxlCommonRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * xxl native regsitry, borrowed from "xxl-rpc"
 *
 * @author xuxueli 2018-11-26
 */
@Service
public class XxlCommonRegistryServiceImpl implements XxlCommonRegistryService, InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(XxlCommonRegistryServiceImpl.class);


    @Resource
    private IXxlCommonRegistryDao xxlCommonRegistryDao;
    @Resource
    private IXxlCommonRegistryDataDao xxlCommonRegistryDataDao;
    @Resource
    private IXxlCommonRegistryMessageDao xxlCommonRegistryMessageDao;

    @Value("${xxl.mq.registry.data.filepath}")
    private String registryDataFilePath;
    @Value("${xxl.mq.registry.beattime}")
    private int registryBeatTime;
    @Value("${xxl.mq.registry.accessToken}")
    private String accessToken;

    @Override
    public ReturnT<String> registry(String accessToken, List<XxlCommonRegistryData> xxlCommonRegistryDataList) {
        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "AccessToken Invalid");
        }
        if (xxlCommonRegistryDataList==null || xxlCommonRegistryDataList.size()==0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "RegistryData Invalid.");
        }
        for (XxlCommonRegistryData registryData: xxlCommonRegistryDataList) {
            if (registryData.getKey()==null || registryData.getKey().trim().length()==0 || registryData.getKey().trim().length()>255) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "RegistryData Key Invalid[0~255]");
            }
            if (registryData.getValue()==null || registryData.getValue().trim().length()==0 || registryData.getValue().trim().length()>255) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "RegistryData Value Invalid[0~255]");
            }
        }

        // add queue
        registryQueue.addAll(xxlCommonRegistryDataList);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> remove(String accessToken, List<XxlCommonRegistryData> xxlCommonRegistryDataList) {
        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "AccessToken Invalid");
        }
        if (xxlCommonRegistryDataList==null || xxlCommonRegistryDataList.size()==0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "RegistryData Invalid.");
        }
        for (XxlCommonRegistryData registryData: xxlCommonRegistryDataList) {
            if (registryData.getKey()==null || registryData.getKey().trim().length()==0 || registryData.getKey().trim().length()>255) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "RegistryData Key Invalid[0~255]");
            }
            if (registryData.getValue()==null || registryData.getValue().trim().length()==0 || registryData.getValue().trim().length()>255) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "RegistryData Value Invalid[0~255]");
            }
        }

        // add queue
        removeQueue.addAll(xxlCommonRegistryDataList);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<Map<String, List<String>>> discovery(String accessToken, List<String> keys) {
        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "AccessToken Invalid");
        }
        if (keys==null || keys.size()==0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "keys Invalid.");
        }
        for (String key: keys) {
            if (key==null || key.trim().length()==0 || key.trim().length()>255) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "Key Invalid[0~255]");
            }
        }

        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (String key: keys) {
            XxlCommonRegistryData xxlCommonRegistryData = new XxlCommonRegistryData();
            xxlCommonRegistryData.setKey(key);

            List<String> dataList = new ArrayList<String>();
            XxlCommonRegistry fileXxlCommonRegistry = getFileRegistryData(xxlCommonRegistryData);
            if (fileXxlCommonRegistry!=null) {
                dataList = fileXxlCommonRegistry.getDataList();
            }

            result.put(key, dataList);
        }

        return new ReturnT<Map<String, List<String>>>(result);
    }

    @Override
    public DeferredResult<ReturnT<String>> monitor(String accessToken, List<String> keys) {
        // init
        DeferredResult deferredResult = new DeferredResult(registryBeatTime * 3 * 1000L, new ReturnT<>(ReturnT.FAIL_CODE, "Monitor timeout."));

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            deferredResult.setResult(new ReturnT<>(ReturnT.FAIL_CODE, "AccessToken Invalid"));
            return deferredResult;
        }
        if (keys==null || keys.size()==0) {
            deferredResult.setResult(new ReturnT<>(ReturnT.FAIL_CODE, "keys Invalid."));
            return deferredResult;
        }
        for (String key: keys) {
            if (key==null || key.trim().length()==0 || key.trim().length()>255) {
                deferredResult.setResult(new ReturnT<>(ReturnT.FAIL_CODE, "Key Invalid[0~255]"));
                return deferredResult;
            }
        }

        // monitor by client
        for (String key: keys) {
            String fileName = parseRegistryDataFileName(key);

            List<DeferredResult> deferredResultList = registryDeferredResultMap.get(fileName);
            if (deferredResultList == null) {
                deferredResultList = new ArrayList<>();
                registryDeferredResultMap.put(fileName, deferredResultList);
            }

            deferredResultList.add(deferredResult);
        }

        return deferredResult;
    }

    /**
     * update Registry And Message
     */
    private void checkRegistryDataAndSendMessage(XxlCommonRegistryData xxlCommonRegistryData){
        // data json
        List<XxlCommonRegistryData> xxlCommonRegistryDataList = xxlCommonRegistryDataDao.findData(xxlCommonRegistryData.getKey());
        List<String> valueList = new ArrayList<>();
        if (xxlCommonRegistryDataList!=null && xxlCommonRegistryDataList.size()>0) {
            for (XxlCommonRegistryData dataItem: xxlCommonRegistryDataList) {
                valueList.add(dataItem.getValue());
            }
        }
        String dataJson = JacksonUtil.writeValueAsString(valueList);

        // update registry and message
        XxlCommonRegistry xxlCommonRegistry = xxlCommonRegistryDao.load(xxlCommonRegistryData.getKey());
        boolean needMessage = false;
        if (xxlCommonRegistry == null) {
            xxlCommonRegistry = new XxlCommonRegistry();
            xxlCommonRegistry.setKey(xxlCommonRegistryData.getKey());
            xxlCommonRegistry.setData(dataJson);
            xxlCommonRegistryDao.add(xxlCommonRegistry);
            needMessage = true;
        } else {
            if (!xxlCommonRegistry.getData().equals(dataJson)) {
                xxlCommonRegistry.setData(dataJson);
                xxlCommonRegistryDao.update(xxlCommonRegistry);
                needMessage = true;
            }
        }

        if (needMessage) {
            // sendRegistryDataUpdateMessage (registry update)
            sendRegistryDataUpdateMessage(xxlCommonRegistry);
        }

    }

    /**
     * send RegistryData Update Message
     */
    private void sendRegistryDataUpdateMessage(XxlCommonRegistry xxlRpcRegistry){
        String registryUpdateJson = JacksonUtil.writeValueAsString(xxlRpcRegistry);

        XxlCommonRegistryMessage registryMessage = new XxlCommonRegistryMessage();
        registryMessage.setData(registryUpdateJson);
        xxlCommonRegistryMessageDao.add(registryMessage);
    }
    
    // ------------------------ broadcase + file data ------------------------

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean executorStoped = false;
    private volatile List<Integer> readedMessageIds = Collections.synchronizedList(new ArrayList<Integer>());

    private volatile LinkedBlockingQueue<XxlCommonRegistryData> registryQueue = new LinkedBlockingQueue<XxlCommonRegistryData>();
    private volatile LinkedBlockingQueue<XxlCommonRegistryData> removeQueue = new LinkedBlockingQueue<XxlCommonRegistryData>();
    private Map<String, List<DeferredResult>> registryDeferredResultMap = new ConcurrentHashMap<>();

    public static XxlCommonRegistryData staticRegistryData;

    @Override
    public void afterPropertiesSet() throws Exception {

        /**
         * registry registry data         (client-num/10 s)
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        XxlCommonRegistryData xxlCommonRegistryData = registryQueue.take();
                        if (xxlCommonRegistryData !=null) {

                            // refresh or add
                            int ret = xxlCommonRegistryDataDao.refresh(xxlCommonRegistryData);
                            if (ret == 0) {
                                xxlCommonRegistryDataDao.add(xxlCommonRegistryData);
                            }

                            // valid file status
                            XxlCommonRegistry fileXxlCommonRegistry = getFileRegistryData(xxlCommonRegistryData);
                            if (fileXxlCommonRegistry!=null && fileXxlCommonRegistry.getDataList().contains(xxlCommonRegistryData.getValue())) {
                                continue;     // "Repeated limited."
                            }

                            // checkRegistryDataAndSendMessage
                            checkRegistryDataAndSendMessage(xxlCommonRegistryData);
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });

        /**
         * remove registry data         (client-num/start-interval s)
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        XxlCommonRegistryData xxlCommonRegistryData = removeQueue.take();
                        if (xxlCommonRegistryData != null) {

                            // delete
                            xxlCommonRegistryDataDao.deleteDataValue(xxlCommonRegistryData.getKey(), xxlCommonRegistryData.getValue());

                            // valid file status
                            XxlCommonRegistry fileXxlCommonRegistry = getFileRegistryData(xxlCommonRegistryData);
                            if (fileXxlCommonRegistry!=null && !fileXxlCommonRegistry.getDataList().contains(xxlCommonRegistryData.getValue())) {
                                continue;   // "Repeated limited."
                            }

                            // checkRegistryDataAndSendMessage
                            checkRegistryDataAndSendMessage(xxlCommonRegistryData);
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });

        /**
         * broadcase new one registry-data-file     (1/1s)
         *
         * clean old message   (1/10s)
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        // new message, filter readed
                        List<XxlCommonRegistryMessage> messageList = xxlCommonRegistryMessageDao.findMessage(readedMessageIds);
                        if (messageList!=null && messageList.size()>0) {
                            for (XxlCommonRegistryMessage message: messageList) {
                                readedMessageIds.add(message.getId());

                                // from registry、add、update、deelete，ne need sync from db, only write
                                XxlCommonRegistry xxlCommonRegistry = JacksonUtil.readValue(message.getData(), XxlCommonRegistry.class);

                                // default, sync from db （aready sync before message, only write）

                                // sync file
                                setFileRegistryData(xxlCommonRegistry);
                            }
                        }

                        // clean old message;
                        if (System.currentTimeMillis() % registryBeatTime ==0) {
                            xxlCommonRegistryMessageDao.cleanMessage(10);
                            readedMessageIds.clear();
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });

        /**
         *  clean old registry-data     (1/10s)
         *
         *  sync total registry-data db + file      (1+N/10s)
         *
         *  clean old registry-data file
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {

                        // + static registry
                        if (staticRegistryData != null) {
                            registryQueue.add(staticRegistryData);
                        }

                        // clean old registry-data in db
                        xxlCommonRegistryDataDao.cleanData(registryBeatTime * 3);

                        // + clean old registry in db
                        xxlCommonRegistryDao.cleanDead();

                        // sync registry-data, db + file
                        int offset = 0;
                        int pagesize = 1000;
                        List<String> registryDataFileList = new ArrayList<>();

                        List<XxlCommonRegistry> registryList = xxlCommonRegistryDao.pageList(offset, pagesize);
                        while (registryList!=null && registryList.size()>0) {

                            for (XxlCommonRegistry registryItem: registryList) {

                                // default, sync from db
                                List<XxlCommonRegistryData> xxlCommonRegistryDataList = xxlCommonRegistryDataDao.findData(registryItem.getKey());
                                List<String> valueList = new ArrayList<String>();
                                if (xxlCommonRegistryDataList!=null && xxlCommonRegistryDataList.size()>0) {
                                    for (XxlCommonRegistryData dataItem: xxlCommonRegistryDataList) {
                                        valueList.add(dataItem.getValue());
                                    }
                                }
                                String dataJson = JacksonUtil.writeValueAsString(valueList);

                                // check update, sync db
                                if (!registryItem.getData().equals(dataJson)) {
                                    registryItem.setData(dataJson);
                                    xxlCommonRegistryDao.update(registryItem);
                                }

                                // sync file
                                String registryDataFile = setFileRegistryData(registryItem);

                                // collect registryDataFile
                                registryDataFileList.add(registryDataFile);
                            }


                            offset += 1000;
                            registryList = xxlCommonRegistryDao.pageList(offset, pagesize);
                        }

                        // clean old registry-data file
                        cleanFileRegistryData(registryDataFileList);

                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(registryBeatTime);
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });


    }

    @Override
    public void destroy() throws Exception {
        executorStoped = true;
        executorService.shutdownNow();
    }


    // ------------------------ file opt ------------------------

    // get
    public XxlCommonRegistry getFileRegistryData(XxlCommonRegistryData xxlCommonRegistryData){

        // fileName
        String fileName = parseRegistryDataFileName(xxlCommonRegistryData.getKey());

        // read
        Properties prop = PropUtil.loadProp(fileName);
        if (prop!=null) {
            XxlCommonRegistry fileXxlCommonRegistry = new XxlCommonRegistry();
            fileXxlCommonRegistry.setData(prop.getProperty("data"));
            fileXxlCommonRegistry.setDataList(JacksonUtil.readValue(fileXxlCommonRegistry.getData(), List.class));
            return fileXxlCommonRegistry;
        }
        return null;
    }
    private String parseRegistryDataFileName(String key){
        // fileName
        String fileName = registryDataFilePath
                .concat(File.separator).concat(key)
                .concat(".properties");
        return fileName;
    }

    // set
    public String setFileRegistryData(XxlCommonRegistry xxlCommonRegistry){

        // fileName
        String fileName = parseRegistryDataFileName(xxlCommonRegistry.getKey());

        // valid repeat update
        Properties existProp = PropUtil.loadProp(fileName);
        if (existProp != null && existProp.getProperty("data").equals(xxlCommonRegistry.getData())
                ) {
            return new File(fileName).getPath();
        }

        // write
        Properties prop = new Properties();
        prop.setProperty("data", xxlCommonRegistry.getData());

        PropUtil.writeProp(prop, fileName);

        logger.info(">>>>>>>>>>> xxl-mq, setFileRegistryData: key={}, data={}", xxlCommonRegistry.getKey(), xxlCommonRegistry.getData());


        // brocast monitor client
        List<DeferredResult> deferredResultList = registryDeferredResultMap.get(fileName);
        if (deferredResultList != null) {
            registryDeferredResultMap.remove(fileName);
            for (DeferredResult deferredResult: deferredResultList) {
                deferredResult.setResult(new ReturnT<>(ReturnT.FAIL_CODE, "Monitor key update."));
            }
        }

        return new File(fileName).getPath();
    }
    // clean
    public void cleanFileRegistryData(List<String> registryDataFileList){
        filterChildPath(new File(registryDataFilePath), registryDataFileList);
    }

    public void filterChildPath(File parentPath, final List<String> registryDataFileList){
        if (!parentPath.exists() || parentPath.list()==null || parentPath.list().length==0) {
            return;
        }
        File[] childFileList = parentPath.listFiles();
        for (File childFile: childFileList) {
            if (childFile.isFile() && !registryDataFileList.contains(childFile.getPath())) {
                childFile.delete();

                logger.info(">>>>>>>>>>> xxl-mq, cleanFileRegistryData, RegistryData Path={}", childFile.getPath());
            }
            if (childFile.isDirectory()) {
                if (parentPath.listFiles()!=null && parentPath.listFiles().length>0) {
                    filterChildPath(childFile, registryDataFileList);
                } else {
                    childFile.delete();
                }

            }
        }

    }


}
