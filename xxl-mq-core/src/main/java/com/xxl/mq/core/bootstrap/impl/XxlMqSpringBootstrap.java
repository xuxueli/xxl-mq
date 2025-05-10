package com.xxl.mq.core.bootstrap.impl;

import com.xxl.mq.core.bootstrap.XxlMqBootstrap;
import com.xxl.mq.core.consumer.annotation.XxlMq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * xxl-mq spring bootstrap
 *
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqSpringBootstrap extends XxlMqBootstrap implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(XxlMqSpringBootstrap.class);


    // ---------------------- start / stop ----------------------

    @Override
    public void afterSingletonsInstantiated() {
        // scan consumer
        scanConsumers();

        // super start
        super.start();
    }

    @Override
    public void destroy() throws Exception {
        // super stop
        super.stop();
    }

    /**
     * scca consumer
     */
    private void scanConsumers(){
        if (applicationContext == null) {
            return;
        }
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {

            // get bean
            Object bean = null;
            Lazy onBean = applicationContext.findAnnotationOnBean(beanDefinitionName, Lazy.class);
            if (onBean!=null){
                logger.debug("xxl-mq annotation scan, skip @Lazy Bean:{}", beanDefinitionName);
                continue;
            }else {
                bean = applicationContext.getBean(beanDefinitionName);
            }

            // filter method
            Map<Method, XxlMq> annotatedMethods = null;   // referred to ：org.springframework.context.event.EventListenerMethodProcessor.processBean
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                        new MethodIntrospector.MetadataLookup<XxlMq>() {
                            @Override
                            public XxlMq inspect(Method method) {
                                return AnnotatedElementUtils.findMergedAnnotation(method, XxlMq.class);
                            }
                        });
            } catch (Throwable ex) {
                logger.error("xxl-mq method-consumer resolve error for bean[" + beanDefinitionName + "].", ex);
            }
            if (annotatedMethods==null || annotatedMethods.isEmpty()) {
                continue;
            }

            // generate and regist method consumer
            for (Map.Entry<Method, XxlMq> methodXxlMqEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlMqEntry.getKey();
                XxlMq xxlMq = methodXxlMqEntry.getValue();
                // registry
                registryMethodConsumer(xxlMq, bean, executeMethod);
            }

        }
    }

    // ---------------------- applicationContext ----------------------

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
