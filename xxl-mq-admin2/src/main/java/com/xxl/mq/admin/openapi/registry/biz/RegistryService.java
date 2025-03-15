package com.xxl.mq.admin.openapi.registry.biz;

import com.xxl.mq.admin.openapi.common.model.OpenApiResponse;
import com.xxl.mq.admin.openapi.registry.model.RegisterRequest;

/**
 * @author xuxueli 2018-12-03
 */
public interface RegistryService {

    /**
     * register
     *
     * logic：
     *      1、async run -> write db + broadcast message -> refresh cache + push client
     *      2、single-client register single-app
     *
     * @param request   client instance
     * @return
     */
    OpenApiResponse register(RegisterRequest request);

    /**
     * unregister
     *
     * @param request
     * @return
     */
    OpenApiResponse unregister(RegisterRequest request);


}
