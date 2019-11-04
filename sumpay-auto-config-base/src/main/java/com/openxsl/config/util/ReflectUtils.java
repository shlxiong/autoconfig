package com.openxsl.config.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;

/**
 * alibaba-dubbo-commons
 * 
 * @author qian.lei
 * @author xiongsl
 */
public final class ReflectUtils {
	/**
     * void(V).
     */
    public static final char JVM_VOID = 'V';
    /**
     * boolean(Z).
     */
    public static final char JVM_BOOLEAN = 'Z';
    /**
     * byte(B).
     */
    public static final char JVM_BYTE = 'B';
    /**
     * char(C).
     */
    public static final char JVM_CHAR = 'C';
    /**
     * double(D).
     */
    public static final char JVM_DOUBLE = 'D';
    /**
     * float(F).
     */
    public static final char JVM_FLOAT = 'F';
    /**
     * int(I).
     */
    public static final char JVM_INT = 'I';
    /**
     * long(J).
     */
    public static final char JVM_LONG = 'J';
    /**
     * short(S).
     */
    public static final char JVM_SHORT = 'S';
	
    /**
     * java.lang.Object[][].class => "java.lang.Object[][]"
     */
	public static String getName(Class<?> c) {
        if (c.isArray()) {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append("[]");
                c = c.getComponentType();
            }
            while (c.isArray());

            return c.getName() + sb.toString();
        }
        return c.getName();
    }
	
    /**
     * boolean[].class => "[Z"
     * Object.class => "Ljava/lang/Object;"
     */
    public static String getDesc(Class<?> c) {
        StringBuilder ret = new StringBuilder();

        while (c.isArray()) {
            ret.append('[');
            c = c.getComponentType();
        }

        if (c.isPrimitive()) {
            String t = c.getName();
            if ("void".equals(t)) {
            	ret.append(JVM_VOID);
            } else if ("boolean".equals(t)) {
            	ret.append(JVM_BOOLEAN);
            } else if ("byte".equals(t)) {
            	ret.append(JVM_BYTE);
            } else if ("char".equals(t)) {
            	ret.append(JVM_CHAR);
            } else if ("double".equals(t)) {
            	ret.append(JVM_DOUBLE);
            } else if ("float".equals(t)) {
            	ret.append(JVM_FLOAT);
            } else if ("int".equals(t)) {
            	ret.append(JVM_INT);
            } else if ("long".equals(t)) {
            	ret.append(JVM_LONG);
            } else if ("short".equals(t)) {
            	ret.append(JVM_SHORT);
            }
        } else {
            ret.append('L');
            ret.append(c.getName().replace('.', '/'));
            ret.append(';');
        }
        return ret.toString();
    }
    
    /**
     * int do(int arg1) => "do(I)I"
     * void do(String arg1,boolean arg2) => "do(Ljava/lang/String;Z)V"
     */
    public static String getDesc(final Method m) {
        return new StringBuilder(m.getName())
        		.append(getDescWithoutMethodName(m))
        		.toString();
    }
    /**
     * 方法的参数描述
     * "(I)I", "()V", "(Ljava/lang/String;Z)V"
     */
    public static String getDescWithoutMethodName(Method m) {
        StringBuilder ret = new StringBuilder();
        ret.append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            ret.append(getDesc(parameterTypes[i]));
        }
        ret.append(')').append(getDesc(m.getReturnType()));
        return ret.toString();
    }
    /**
     * get constructor desc.
     * "()V", "(Ljava/lang/String;I)V"
     */
    public static String getDesc(final Constructor<?> c) {
        StringBuilder ret = new StringBuilder("(");
        Class<?>[] parameterTypes = c.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            ret.append(getDesc(parameterTypes[i]));
        }
        ret.append(')').append('V');
        return ret.toString();
    }
    
    /**
	 * 判断类是否为“简单类型”（布尔、数值、字符串和日期）
	 */
	public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class  
            || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls)
            || cls == Character.class;
    }
	public static boolean isArray(Class<?> cls){
		return cls.isArray() || Collection.class.isAssignableFrom(cls);
	}
	
	/**
     * 根据参数类型匹配构造函数，如果找不到则看父类
     * @param clazz     实体类
     * @param paramType 参数类型
     * @throws NoSuchMethodException
     */
    public static Constructor<?> findConstructor(Class<?> clazz, Class<?> paramType) throws NoSuchMethodException {
		try {
			return clazz.getConstructor(new Class<?>[] {paramType});
		} catch (NoSuchMethodException e) {
			for (Constructor<?> constructor : clazz.getConstructors()) { //父类
				if (Modifier.isPublic(constructor.getModifiers()) 
						&& constructor.getParameterTypes().length == 1
						&& constructor.getParameterTypes()[0].isAssignableFrom(paramType)) {
					return constructor;
				}
			}
			throw e;
		}
    }

}
