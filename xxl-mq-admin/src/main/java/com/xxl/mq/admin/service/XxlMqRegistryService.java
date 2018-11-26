package com.xxl.mq.admin.service;

import com.xxl.mq.admin.core.result.ReturnT;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;

/**
 * @author xuxueli 2018-11-26
 */
public interface XxlMqRegistryService {

    /**
     * refresh registry-value, check update and broacase
     */
    ReturnT<String> registry(String biz, String env, List<String> keys, String value);

    /**
     * remove registry-value, check update and broacase
     */
    ReturnT<String> remove(String biz, String env, List<String> keys, String value);

    /**
     * discovery registry-data, read file
     */
    ReturnT<Map<String, List<String>>> discovery(String biz, String env, List<String> keys);

    /**
     * monitor update
     */
    DeferredResult<ReturnT<String>> monitor(String biz, String env, List<String> keys);

}
