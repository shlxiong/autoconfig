package com.openxsl.config.util;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class ClassLoaderUtils {
	/** Suffix for array class names: "[]" */
    public static final String  ARRAY_SUFFIX            = "[]";
    /** Prefix for internal array class names: "[L" */
    private static final String INTERNAL_ARRAY_PREFIX   = "[L";
    
    public static Map<Class<?>, Class<?>> prototypeClassMaps = new HashMap<Class<?>, Class<?>>(8);
    private static Map<String, Class<?>> primitiveNameMap = new HashMap<String, Class<?>>(8);
    static {
    	prototypeClassMaps.put(int.class, Integer.class);
    	prototypeClassMaps.put(long.class, Long.class);
    	prototypeClassMaps.put(float.class, Float.class);
    	prototypeClassMaps.put(double.class, Double.class);
    	prototypeClassMaps.put(boolean.class, Boolean.class);
    	prototypeClassMaps.put(char.class, Character.class);
    	prototypeClassMaps.put(byte.class, Byte.class);
    	prototypeClassMaps.put(short.class, Short.class);
		
		for (Map.Entry<Class<?>, Class<?>> entry : prototypeClassMaps.entrySet()){
			primitiveNameMap.put(entry.getKey().getName(), entry.getValue());
		}
    }
	
	public static ClassLoader getClassLoader(Class<?> cls) {
    	ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
        	try {
        		cl = cls.getClassLoader();
        	} catch (Throwable ex) {
        		//
        	}
        	if (cl == null) {
        		cl = ClassLoaderUtils.class.getClassLoader();
        	}
        }
        return cl;
    }
	
	public static Class<?> forName(String name, Class<?> loaderClass)throws ClassNotFoundException{
		return forName(name, getClassLoader(loaderClass));
	}
	
	public static Class<?> forName(String name, ClassLoader classLoader)
            	throws ClassNotFoundException, LinkageError {
        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length()-ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        int internalArrayMarker = name.indexOf(INTERNAL_ARRAY_PREFIX);
        if (internalArrayMarker != -1 && name.endsWith(";")) {
            String elementClassName = null;
            if (internalArrayMarker == 0) {
                elementClassName = name
                        .substring(INTERNAL_ARRAY_PREFIX.length(), name.length()-1);
            } else if (name.startsWith("[")) {
                elementClassName = name.substring(1);
            }
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = getClassLoader(ClassLoaderUtils.class);
        }
        return classLoaderToUse.loadClass(name);
    }
	
	public static Class<?> resolvePrimitiveClassName(String name) {
        if (name != null) {
            return (Class<?>) primitiveNameMap.get(name);
        }else{
        	return null;
        }
    }
	public static String toShortString(Object obj){
        if (obj == null){
            return "null";
        }
        return obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);
    }

}
