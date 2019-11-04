package com.openxsl.config.tracing;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.*;

/**
 * @author heyc
 * @date ${DATA} 17:06
 */
public class TraceAutoConfig implements ImportSelector {

    /**
     * 自动配置类
     */
    private static String[] TRACE_AUTO_CONFIGS = {
    		"com.openxsl.config.tracing.repo.RedisBeanPostProcessor",
    		"com.openxsl.config.tracing.repo.MongoBeanPostProcessor",
            "com.openxsl.config.tracing.filter.MvcLoggingConfiguration",
            "com.openxsl.config.tracing.http.HttpClientBeanPostProcessor",
            "com.openxsl.config.tracing.quartz.SpringSchdBeanPostProcessor",
            "com.openxsl.config.tracing.quartz.ElasticJobBeanPostProcessor",
            "com.openxsl.config.tracing.thread.ThreadMethodBeanPostProcessor",
            "com.openxsl.config.tracing.thread.SingleThreadSchedulingConfigurer"
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
