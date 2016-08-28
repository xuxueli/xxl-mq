package com.xxl.mq.client;

import com.xxl.mq.client.rpc.netcom.NetComServerFactory;
import com.xxl.mq.client.service.ConsumerHandler;
import com.xxl.mq.client.service.annotation.MqConsumer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqConsumer  implements ApplicationContextAware {

    // ---------------------- server config ----------------------
    private static int port = 6070;
    public void setPort(int port) {
        XxlMqConsumer.port = port;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, Object> regitsryMap = new HashMap();
        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(MqConsumer.class);
        if (serviceMap!=null && serviceMap.size()>0) {
            for (Object serviceBean : serviceMap.values()) {
                if (serviceBean instanceof ConsumerHandler) {
                    String registryKey = ConsumerHandler.class.getName().concat(":").concat(serviceBean.getClass().getAnnotation(MqConsumer.class).value());
                    regitsryMap.put(registryKey, serviceBean);
                }
            }
        }

        try {
            new NetComServerFactory(port, regitsryMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
