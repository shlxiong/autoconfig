package com.openxsl.config.logger.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.MDC;
import org.slf4j.NDC;

/**
 * @author heyc
 */
public class LoggerContext {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";
    public static final String PARENT_ID_KEY = "parentId";

    private static TransmittableThreadLocal<String> TRACE_ID = new TransmittableThreadLocal<String>();

    private static TransmittableThreadLocal<String> SPAN_ID = new TransmittableThreadLocal<String>();

    private static TransmittableThreadLocal<String> PARENT_ID = new TransmittableThreadLocal<String>();

    /**
     * getTraceId
     * @return
     */
    public static String getTraceId(){
        if(TRACE_ID.get() == null){
            TRACE_ID.set(IdUtil.getTraceId());
        }
        return TRACE_ID.get();
    }

    public static void setTraceId(String traceId){
        if (traceId != null){
            TRACE_ID.set(traceId);
        }else {
            TRACE_ID.remove();
        }
    }

    /**
     * getSpanId
     * @return
     */
    public static String getSpanId(){
        if (SPAN_ID.get() == null){
            SPAN_ID.set(IdUtil.generateShortUuid());
        }
        return SPAN_ID.get();
    }

    public static void setSpanId(String spanId){
        if (spanId != null){
            SPAN_ID.set(spanId);
        } else {
            SPAN_ID.remove();
        }
    }

    /**
     * getParentId
     * @return
     */
    public static String getParentId(){
        if (PARENT_ID.get() == null){
            PARENT_ID.set(getSpanId());
        }
        return PARENT_ID.get();
    }

    public static void setParentId(String parentId){
        if (parentId != null){
            PARENT_ID.set(parentId);
        } else {
            PARENT_ID.remove();
        }
    }

    public static void initialize(){
        try{
            MDC.put(LoggerContext.TRACE_ID_KEY, getTraceId());
            MDC.put(LoggerContext.SPAN_ID_KEY, getSpanId());
            MDC.put(LoggerContext.PARENT_ID_KEY, getParentId());
        }catch (Exception e){
        }
    }

    public static void push(String tag){
        NDC.push(tag);
    }

    public static String pop(){
        return NDC.pop();
    }

    /**
     * clear
     */
    public static void clear(){
        TRACE_ID.remove();
        SPAN_ID.remove();
        PARENT_ID.remove();
    }

}