package com.openxsl.config.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

/**
 * ClassLoader/Reflection/Bean相关操作
 * @author xiongsl
 */
public final class BeanUtils extends org.springframework.beans.BeanUtils{
	private static final ClassLoader CLASS_LOADER = ClassUtils.getDefaultClassLoader();
	private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);
	
	public static ClassLoader getClassLoader() {
		return CLASS_LOADER;
	}
	public static Class<?> forName(String name) throws ClassNotFoundException{
		return ClassUtils.forName(name, CLASS_LOADER);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T instantiate(String className, Class<T> type)
				throws ClassNotFoundException,InstantiationException{
		Class<?> clazz = ClassUtils.forName(className, CLASS_LOADER);
		if (type.isAssignableFrom(clazz)) {
			return (T)org.springframework.beans.BeanUtils.instantiate(clazz);
		}else {
			throw new InstantiationException(className+" is not compatible with target "+type);
		}
	}
	/**
	 * 实例化多个对象
	 * @param type 接口或基类
	 * @param classNames 子类名
	 */
	public static <T> T[] instantiate(Class<T> type, String classNames){
		List<T> beans = new ArrayList<T>();
		StringTokenizer tokens = new StringTokenizer(classNames, ",\n");
		while (tokens.hasMoreTokens()) {
			String listenerClass = tokens.nextToken().trim();
			if (listenerClass.length() < 3) {
				continue;
			}
			try {
				beans.add(instantiate(listenerClass, type));
			}catch(Exception e) {
				logger.warn("instantiate listener error: ", e);
			}
		}
		@SuppressWarnings("unchecked")
		T[] target = (T[])Array.newInstance(type, beans.size()); //new T[beans.size()];
		return beans.toArray(target);
	}
	
	/**
	 * 修改私有属性值
	 * @param bean
	 * @param field
	 * @param value
	 */
	public static void setPrivateField(Object bean, String field, Object value) {
		try {
			if (bean == null || field == null || field.length() == 0) {
				return;
			}
			Field f = null;
			try{
				f = bean.getClass().getField(field);
			} catch (NoSuchFieldException nf) {
				Class<?> parentClass = bean.getClass();
				while (parentClass != Object.class && f == null) {
					try {
						f = parentClass.getDeclaredField(field);
					} catch (NoSuchFieldException sfe) {
						parentClass = parentClass.getSuperclass();
					}
				}
			}
			if (f != null) {
				if (f.isAccessible()) {
					f.set(bean, value);
				} else {
					f.setAccessible(true);
					f.set(bean, value);
					f.setAccessible(false);
				}
			}
		}catch(Exception e){
			logger.error("", e);
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> T getPrivateField(Object bean, String field) {
		if (bean == null || field == null || field.length() == 0) {
			return null;
		}
		Object value = null;
		try {
			Field f = null;
			try{
				f = bean.getClass().getField(field);
			} catch (NoSuchFieldException nf) {
				Class<?> parentClass = bean.getClass();
				while (parentClass != Object.class && f == null) {
					try {
						f = parentClass.getDeclaredField(field);
					} catch (NoSuchFieldException sfe) {
						parentClass = parentClass.getSuperclass();
					}
				}
			}
			if (f != null) {
				if (f.isAccessible()) {
					value = f.get(bean);
				} else {
					f.setAccessible(true);
					value = f.get(bean);
					f.setAccessible(false);
				}
			}
		}catch(Exception e){
			logger.error(" ", e);
		}
		return (T) value;
	}
	/**
	 * 递归地取出私有变量
	 * @param xpath 点隔开的多层属性（child.grandson...）
	 */
	public static Object getPrivateFieldHierarchy(Object bean, String xpath) {
		if (xpath==null || xpath.length()<1) {
			return null;
		}
		Object child = bean;
		for (String field : xpath.split("\\.")) {
			child = getPrivateField(child, field);
			if (child == null) {
				break;
			}
		}
		return child;
	}
	
	public static Class<?> getRawType(Class<?> genericClass, int index){
		ParameterizedType pt = (ParameterizedType)genericClass.getGenericSuperclass();
		if (pt != null) {
			return (Class<?>)pt.getActualTypeArguments()[index];
		} else {
			Type[] types = genericClass.getGenericInterfaces();
			if (types != null) {
				pt = (ParameterizedType)types[index];
				return (Class<?>)pt.getActualTypeArguments()[0];
			}
		}
		throw new IllegalArgumentException(genericClass+" maybe not a Generic-Class or interface");
	}
	
	public static URL getJarLocation(String className) throws ClassNotFoundException{
		return CLASS_LOADER.loadClass(className).getProtectionDomain()
					.getCodeSource().getLocation();
	}
	
	public static StackTraceElement[] getStatckTrace() {
		return Thread.currentThread().getStackTrace();
	}
	
	public static Method findBestMethod(Class<?> clazz, String methodName, Object...args){
		Method targetMethod = null, lastMethod = null;
		for (Method method : clazz.getMethods()) {  //public methods including super class
			int argc = method.getParameterCount();
			int maxinum = 0;
			if (method.getName().equals(methodName) && argc==args.length) {
				int cnt = 0;
				for (int i=0; i<argc; i++) {
					if (args[i]==null || method.getParameterTypes()[i].isAssignableFrom(args[i].getClass())) {
						cnt ++;
					}
				}
				if (cnt > 0) {
					if (cnt > maxinum) {
						targetMethod = method;
						maxinum = cnt;
					} else if (cnt == maxinum) {
						lastMethod = method;
					}
				}
			}
		}
		if (lastMethod != null) {
			throw new IllegalStateException(
					String.format("至少存在两个具有相同参数的同名方法：%s，请设置更详细的过滤条件", methodName));
		}
		return targetMethod;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Map<String,T> findFieldByType(Object bean, Class<T> type) {
		Map<String, T> objectMap = new HashMap<String, T>();
		Class<?> beanClass = bean.getClass();
		String fname;
		for (Field field : beanClass.getFields()) {
			if (type.isAssignableFrom(field.getType())) {
				fname = field.getName();
				try {
					objectMap.put(fname, (T)field.get(bean));
				} catch (IllegalArgumentException | IllegalAccessException e) {
				}
			}
		}
		//private or protected
		Class<?> parentClass = beanClass;
		while (parentClass != Object.class) {
			for (Field field : parentClass.getDeclaredFields()) {
				if (type.isAssignableFrom(field.getType())) {
					fname = field.getName();
					if (!objectMap.containsKey(fname)) {  //父子具有相同名的字段（覆盖）
						objectMap.put(fname, (T)getPrivateField(bean, fname));
					}
				}
			}
			parentClass = parentClass.getSuperclass();
		}
		return objectMap;
	}
	
	/**
     * 从ServletRequest中封装对象
     */
    @SuppressWarnings({"unchecked","rawtypes"})
	public static Object getRequestBean(ServletRequest request, Class<?> clazz){
    	Object bean = instantiate(clazz);
    	Class<?> propType;
    	for (PropertyDescriptor desc : BeanUtils.getPropertyDescriptors(clazz)){
    		String name = desc.getName();
    		if ("class".equals(name)) {
    			continue;
    		}
    		propType = desc.getPropertyType();
    		boolean flag = Collection.class.isAssignableFrom(propType);
    		try{
	    		if (flag || propType.isArray()){
	    			String[] strValues = request.getParameterValues(name);
	    			if (strValues != null){
	    				propType = propType.getComponentType();
	    				int len = strValues.length;
	    				Object array = Array.newInstance(propType, len);
	    				for (int i=0; i<len; i++){
	    					Array.set(array, i, valueOf(strValues[i], propType));
	    				}
	    				if (flag){  //Collection
	    					array = new ArrayList(Arrays.asList(array));
	    				}
	    				desc.getWriteMethod().invoke(bean, array);
	    			}
	    		}else{
	    			Object value = valueOf(request.getParameter(name), propType);
	    			desc.getWriteMethod().invoke(bean, value);
	    		}
    		}catch(Exception e){
    			System.err.println("ReflectUtils.getRequestBean() ignore field: "+name);
    		}
    	}
    	return bean;
    }
    /**
     * 将String值转换为指定类型(尤其通过Request.Parameter的值)
     */
    @SuppressWarnings("unchecked")
	public static <T> T valueOf(String value, Class<T> clazz){
		if (value == null){
			return null;
		}
		if (clazz == int.class){
			return (T)Integer.valueOf(value);
		}else if (clazz == float.class){
			return (T)Float.valueOf(value);
		}else if (clazz == boolean.class){
			return (T)Boolean.valueOf(value);
		}else if (clazz == long.class){
			return (T)Long.valueOf(value);
		}else if (clazz == double.class){
			return (T)Double.valueOf(value);
		}else if (clazz == String.class){
			return (T)value;
		}else if (Date.class.isAssignableFrom(clazz)){
			try{
				return (T)new Date(Long.parseLong(value));
			}catch(Exception e){
				String[] frmts = {"yyyy-MM-dd","yyyy-MM-dd HH:mm:ss",
								 "yyyy/MM/dd","yyyy/MM/dd HH:mm:ss",
								 "yyyyMMdd","yyyyMMdd HH:mm:ss"};
				for (String frmt : frmts){
					try{
						return (T)new SimpleDateFormat(frmt).parse(value);
					}catch(Exception ex){
					}
				}
			}
		}
		
		if (clazz.isInterface()){
			throw new IllegalArgumentException("Please specify a class of the interface ["+clazz.getName()+"]");
		}
		T obj = null;
		try {
			obj = clazz.newInstance();
			clazz.getDeclaredMethod("valueOf", String.class).invoke(obj, value);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return obj;
	}
	

}
