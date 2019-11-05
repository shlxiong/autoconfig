package com.openxsl.config.tracing;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.*;

/**
 * 根据classpath装载 trace-api中的类
 * @author xiongsl
 */
public class TraceAutoConfig implements ImportSelector {

    /**
     * 自动配置类
     */
    private static String[] TRACE_AUTO_CONFIGS = {
    		"com.openxsl.tracing.repo.RedisBeanPostProcessor",
    		"com.openxsl.tracing.repo.MongoBeanPostProcessor",
            "com.openxsl.tracing.filter.MvcLoggingConfiguration",
            "com.openxsl.tracing.http.HttpClientBeanPostProcessor",
            "com.openxsl.tracing.quartz.SpringSchdBeanPostProcessor",
            "com.openxsl.tracing.quartz.ElasticJobBeanPostProcessor",
            "com.openxsl.tracing.thread.ThreadMethodBeanPostProcessor",
            "com.openxsl.tracing.thread.SingleThreadSchedulingConfigurer"
    };

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        List<String> classes = new ArrayList<>();
        for (String configClass : TRACE_AUTO_CONFIGS) {
            if (ClassUtils.isPresent(configClass, ClassUtils.getDefaultClassLoader())) {
                classes.add(configClass);
            }
        }
        return classes.toArray(new String[classes.size()]);
    }
}
