package com.xxl.mq.admin.service;

import com.xxl.mq.admin.core.model.XxlCommonRegistryData;
import com.xxl.mq.admin.core.result.ReturnT;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;

/**
 * common regsitry, borrowed from "xxl-registry"
 *
 * @author xuxueli 2018-11-26
 */
public interface XxlCommonRegistryService {

    /**
     * refresh registry-value, check update and broacase
     */
    ReturnT<String> registry(String accessToken, List<XxlCommonRegistryData> xxlCommonRegistryDataList);

    /**
     * remove registry-value, check update and broacase
     */
    ReturnT<String> remove(String accessToken, List<XxlCommonRegistryData> xxlCommonRegistryDataList);

    /**
     * discovery registry-data, read file
     */
    ReturnT<Map<String, List<String>>> discovery(String accessToken, List<String> keys);

    /**
     * monitor update
     */
    DeferredResult<ReturnT<String>> monitor(String accessToken, List<String> keys);

}
