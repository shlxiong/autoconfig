package com.openxsl.config.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ClassUtils;

import com.openxsl.config.Environment;
import com.openxsl.config.util.BeanUtils;

/**
 * 加载SPI接口的实现，含@Order，@Autoload及子接口
 * 
 * @author xiongsl
 */
public class GraceServiceLoader {
	private static final Logger logger = LoggerFactory.getLogger(GraceServiceLoader.class);
	private static final ClassLoader CLASS_LOADER = ClassUtils.getDefaultClassLoader();

	/**
	 * 加载SPI的实现类（含@Order和@Autoload）
	 * @param type 组件类型
	 * @param instanceType 子类型 (可选)
	 */
	@SafeVarargs
	public static <T> List<T> loadServices(Class<T> type,
							Class<? extends T>... instanceType){
		List<T> services = new ArrayList<T>();
		ServiceLoader<T> serviceLoader = ServiceLoader.load(type);
		Iterator<T> itr = BeanUtils.getPrivateField(serviceLoader, "lookupIterator");
		if (itr != null) {
			ClassLoader loader = (ClassLoader)BeanUtils.getPrivateField(itr, "loader");
			while (itr.hasNext()) {
				try {
					String instanceName = (String)BeanUtils.getPrivateField(itr, "nextName");
					try {
						Class<?> serviceClass = Class.forName(instanceName, false, loader);
						if (shouldLoad(serviceClass, instanceType)) {
							T element = itr.next();
							services.add(element);
						}
			        } catch (ClassNotFoundException x) {
			        	 throw new ServiceConfigurationError("Provider " + instanceName + " not found");
			        }
				} catch(java.util.ServiceConfigurationError | LinkageError e) {
					logger.warn("*****load [{}] error: {}", type, e.getMessage());
				} finally {
					BeanUtils.setPrivateField(itr, "nextName", null);
				}
			}
		} else {  //safe compatient
			itr = ServiceLoader.load(type).iterator();
			while (itr.hasNext()) {
				try {
					T element = itr.next();   //instantiate
					if (shouldLoad(element.getClass(), instanceType)) {
						services.add(element);
					}
				} catch(java.util.ServiceConfigurationError | LinkageError e) {
					logger.warn("*****load [{}] error: {}", type, e.getMessage());
				}
			}
		}
		Collections.sort(services, new AnnotationAwareOrderComparator());
		return services;
	}
	
	/**
	 * 加载子接口
	 * @param type 组件类型
	 * @param instanceClass 类名
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> loadServices(Class<T> type, String instanceClass){
		Class<? extends T> instanceType = null;
		if (ClassUtils.isPresent(instanceClass, CLASS_LOADER)) {
			try {
				instanceType = (Class<? extends T>)ClassUtils.forName(instanceClass, CLASS_LOADER);
				return loadServices(type, instanceType);
			} catch (ClassNotFoundException | LinkageError e) {
				logger.warn("*****load [{}] error: {}", instanceClass, e.getMessage());
			}
		}
		return new ArrayList<T>(0);
	}
	
	/**
	 * 根据@Autoload等条件加载实例类
	 * @param serviceClass 实现类
	 * @param instanceType 子类型 (可选)
	 */
	private static boolean shouldLoad(Class<?> serviceClass, Class<?>... instanceType) {
		boolean flag = true;
		Autoload anno = serviceClass.getAnnotation(Autoload.class);
		if (anno != null) {
			String property = "".equals(anno.value()) ? anno.property() : anno.value();
			String presentClass = anno.presentClass();
			String missingClass = anno.missingClass();
			if (!"".equals(property)) {
				flag = Environment.getProperty(property, Boolean.class, false);
			}
			if (flag && !"".equals(presentClass)) {
				flag = Environment.exists(presentClass);
			}
			if (flag && !"".equals(missingClass)) {
				flag = !Environment.exists(missingClass);
			}
		}
		if (flag && instanceType.length > 0) {
			flag = instanceType[0].isAssignableFrom(serviceClass);
		}
		return flag;
	}
	
}
