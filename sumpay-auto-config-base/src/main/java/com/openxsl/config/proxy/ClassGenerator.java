package com.openxsl.config.proxy;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.openxsl.config.util.BeanUtils;
import com.openxsl.config.util.KvPair;
import com.openxsl.config.util.ReflectUtils;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

/**
 * Javassist字节码增强
 * @author qian.lei
 * @company Alibaba-inc
 * @modified 2018-01-10 by shuilin.xiongsl
 * 
 * <pre><code>
 * ClassGenerator generator = ClassGenerator.newInstance(clazz);
 * generator.addInterface(..);
 * generator.setSuperClass(..);
 * generator.addClassAnnotation(..);
 * generator.addField(..);   generator.addFieldWithAnnotation(..);
 * generator.addMethod(..);  generator.addMethodWithAnnotation(..);
 * Class<?> enhancer = generator.toClass();
 * ....
 * generator.release();
 * generator = null;
 * </code></pre>
 */
public final class ClassGenerator {
    private static final String SIMPLE_NAME_TAG = "<init>";
    private static final Map<ClassLoader, ClassPool> POOL_MAP = new ConcurrentHashMap<ClassLoader, ClassPool>(); //ClassLoader - ClassPool
    
    private final AtomicLong CLASS_NAME_COUNTER = new AtomicLong(0);
    private final ClassPool pool;
    private final ConstPool constPool;
    private final CtClass targetClass;
    private String className, superClass;
    private Set<String> intrfaces = new HashSet<String>(2);
    private List<String> constructors;  //字节码
    
//    public static void main(String[] args) throws Exception{
//    	ClassGenerator generator = newInstance(CountServiceImpl.class);
//    	Method m = CountServiceImpl.class.getDeclaredMethod("foo", String.class);
//    	System.out.println(generator.getMethodParamNames(m));
//    	System.out.println(generator.getSourceArguments(m));
//	}
    
    public static ClassGenerator newInstance(String clazz) {
        return newInstance(clazz, Thread.currentThread().getContextClassLoader());
    }
    public static ClassGenerator newInstance(Class<?> clazz) {
        return newInstance(clazz.getName());
    }

    public static ClassGenerator newInstance(String clazz, ClassLoader loader) {
        if (!POOL_MAP.containsKey(loader)) {   //ClassPool.getDefault();
            ClassPool pool = new ClassPool(true);
            pool.appendClassPath(new LoaderClassPath(loader));
            POOL_MAP.put(loader, pool);
        }
        return new ClassGenerator(loader, clazz);
    }

    public static boolean isDynamicClass(Class<?> cls) {
        return ClassGenerator.DC.class.isAssignableFrom(cls);
    }
    
    private ClassGenerator(ClassLoader loader, String className) {
        this.pool = POOL_MAP.get(loader);
        CtClass tempClass = null;
        while (tempClass == null) {
        	long serial = CLASS_NAME_COUNTER.getAndIncrement();
    		this.className = String.format("%s_G%d", className,serial);
	    	try {
	    		tempClass = pool.makeClass(this.className);
	        } catch (RuntimeException e) {
	        	//从同一个类代理而来，可能会报错
	        	if (!e.getMessage().endsWith("frozen class (cannot edit)")){
	        		throw e;
	        	}
	        }
        }
    	this.targetClass = tempClass;
    	this.constPool = targetClass.getClassFile().getConstPool();
        this.constructors = new ArrayList<String>(2);
    }

    private static String modifier(int mod) {
    	String accessible = Modifier.isPublic(mod) ? "public"
        		: (Modifier.isProtected(mod) ? "protected"
        				: (Modifier.isPrivate(mod) ? "private" : ""));
    	StringBuilder buffer = new StringBuilder(accessible);
    	if (Modifier.isStatic(mod)) {
    		buffer.append(" static");
    	}
    	if (Modifier.isFinal(mod)) {
    		buffer.append(" final");
    	}
    	return buffer.toString();
    }

    //=========================== instance method ==========================//
    public String getClassName() {
        return className;
    }

    public ClassGenerator addInterface(String interfaceName) {
    	if (interfaceName != null && interfaceName.trim().length() > 0) {
    		intrfaces.add(interfaceName);
    	}
        return this;
    }

    public ClassGenerator setSuperClass(String superClass) {
    	this.superClass = superClass;
        return this;
    }
    
    public ClassGenerator addField(String code) throws CannotCompileException{
    	targetClass.addField(CtField.make(code, targetClass));
//        fields.add(code);
        return this;
    }

    public ClassGenerator addField(String name, int modifier, Class<?> type, String... def)
    					throws CannotCompileException{
    	//private T var;
        StringBuilder sb = new StringBuilder();
        sb.append(modifier(modifier)).append(' ').append(ReflectUtils.getName(type)).append(' ');
        sb.append(name);
        if (def.length > 0) {
            sb.append('=');
            sb.append(def[0]);
        }
        sb.append(';');
        return addField(sb.toString());
    }
    
    public ClassGenerator addFieldWithAnnotation(String field, int modifier, Class<?> fieldType,
    					ProxyAnnotation... annotation)throws NotFoundException, CannotCompileException {
		CtField targetField;
		try {
		    targetField = targetClass.getField(field);
		}catch (NotFoundException nfe) {
			targetField = new CtField(pool.get(fieldType.getName()), field, targetClass);
			targetField.setModifiers(modifier);
			targetClass.addField(targetField);
		}
		this.addAnnoation(constPool, targetField, annotation);
		return this;
	}

    public CtMethod addMethod(String code) {
    	try {
    		CtMethod method = CtNewMethod.make(code, targetClass);
			targetClass.addMethod(method);
			return method;
		} catch (CannotCompileException e) {
			throw new RuntimeException(e);
		}
    }
//    public CtMethod addMethod(Method method) {
//    	try {
//			targetClass.addMethod(this.copyMethod(method));
//		} catch (CannotCompileException | NotFoundException e) {
//			throw new RuntimeException(e);
//		}
//        return this;
//    }
    public CtMethod addMethod(String name, int modifier, Class<?> returnTypes, Class<?>[] paramTypes,
							String body, Class<?>... exceptTypes) {
		//public T name(P arg0)throws Exception {code;}
		StringBuilder sb = new StringBuilder(modifier(modifier));
		sb.append(' ').append(ReflectUtils.getName(returnTypes)).append(' ').append(name);
		sb.append(this.formatArgumentDefinition(paramTypes));   //(arg0,arg2...)
		if (exceptTypes.length > 0) {
			sb.append(" throws ");
			for (int i = 0; i < exceptTypes.length; i++) {
				sb.append(ReflectUtils.getName(exceptTypes[i])).append(",");
			}
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append('{').append(body).append(";}");
		return this.addMethod(sb.toString());
	}
    public CtMethod addMethodWithAnnotation(Method method, String body, ProxyAnnotation... annotation)
				throws NotFoundException, CannotCompileException {
    	CtMethod targetMethod = this.addMethod(method.getName(), method.getModifiers(),
    						method.getReturnType(),	method.getParameterTypes(),
    						body, method.getExceptionTypes());
    	//必须一起提交编译，否则会修改参数名
//		CtMethod targetMethod = this.addMethod(method);
//		if (body.length() > 0) {
//			targetMethod.setBody(body);
//		}
		
		this.addAnnoation(constPool, targetMethod, annotation);
		return targetMethod;
	}

    

    public ClassGenerator addConstructor(String code) {
        constructors.add(code);
        return this;
    }
    public ClassGenerator addConstructor(int modifier, Class<?>[] paramTypes, String body, Class<?>... excepTypes) {
    	//public <init>(...) throws {code;}
        StringBuilder sb = new StringBuilder();
        sb.append(modifier(modifier)).append(' ').append(SIMPLE_NAME_TAG);
        sb.append(this.formatArgumentDefinition(paramTypes));   //(arg0,arg2...)
        if (excepTypes != null && excepTypes.length > 0) {
            sb.append(" throws ");
            for (int i = 0; i < excepTypes.length; i++) {
                sb.append(ReflectUtils.getName(excepTypes[i])).append(',');
            }
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append('{').append(body).append('}');
        return addConstructor(sb.toString());
    }
    
    public ClassGenerator addClassAnnotation(ProxyAnnotation... annotation) throws NotFoundException {
    	this.addAnnoation(constPool, targetClass, annotation);
    	return this;
    }
    
//    public CtClass getCtClass(String className) throws NotFoundException {
//        return pool.get(className);
//    }

    public Class<?> toClass() {
        return this.toClass(BeanUtils.getClassLoader(), getClass().getProtectionDomain());
    }

    public Class<?> toClass(ClassLoader loader, ProtectionDomain pd) {
        try {
            if (superClass != null) {
                targetClass.setSuperclass(pool.get(superClass));
            }
            targetClass.addInterface(pool.get(DC.class.getName())); // add dynamic class tag.
            if (intrfaces != null) {
                for (String cl : intrfaces) {
                	targetClass.addInterface(pool.get(cl));
                }
            }
            targetClass.addConstructor(CtNewConstructor.defaultConstructor(targetClass));
            for (String code : constructors) {
                String[] sn = targetClass.getSimpleName().split("\\$+"); // inner class name include $.
                targetClass.addConstructor(CtNewConstructor.make(code.replaceFirst(SIMPLE_NAME_TAG, sn[sn.length-1]), targetClass));
            }
            try {
				targetClass.writeFile("./target/classes");   //输出class文件到工程路径
			} catch (IOException e) {
				//e.printStackTrace();
			}
            //loader.defineClass();
            return targetClass.toClass(loader, pd);
        } catch (NotFoundException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    public void release() {
        if (targetClass != null) {
        	targetClass.detach();
        }
        if (intrfaces != null) {
        	intrfaces.clear();
        }
        if (constructors != null) {
        	constructors.clear();
        }
    }

//    /**
//     * Copy方法的声明和实现，如果是接口的方法，则会变成abstract
//     */
//    private CtMethod copyMethod(Method m) throws NotFoundException, CannotCompileException {
//    	CtMethod method = pool.get(m.getDeclaringClass().getName())
//        			.getMethod(m.getName(), ReflectUtils.getDescWithoutMethodName(m));
//    	return CtNewMethod.copy(method, targetClass, null);
//    }

//    private CtConstructor copyConstructor(Constructor<?> c)
//    			throws NotFoundException, CannotCompileException {
//    	CtConstructor constructor = pool.get(c.getDeclaringClass().getName())
//    			.getConstructor(ReflectUtils.getDesc(c));
//    	return CtNewConstructor.copy(constructor, targetClass, null);
//    }
    /**
     * 给类/方法/属性增加注解
     * @param constPool
     * @param payloads  标识“类/方法/属性”
     * @param proxyAnno 多个注解
     * @throws NotFoundException
     */
    private final void addAnnoation(ConstPool constPool, Object payloads,
							ProxyAnnotation... proxyAnnos) throws NotFoundException {
		AnnotationsAttribute annotationAttr = 
				new AnnotationsAttribute(constPool,	AnnotationsAttribute.visibleTag);
		for (ProxyAnnotation proxyAnno : proxyAnnos) {
			Annotation annotation = new Annotation(proxyAnno.getType(), constPool);
	    	if (proxyAnno.getAttributes() != null) {
				for (KvPair pair : proxyAnno.getAttributes()) {
					annotation.addMemberValue(pair.getName(),
								this.getMemberValues(pair.getValue()));
				}
	    	}
	    	annotationAttr.addAnnotation(annotation);
		}
		
		if (payloads instanceof CtMethod) {
			((CtMethod)payloads).getMethodInfo().addAttribute(annotationAttr);
		}else if (payloads instanceof CtClass) {
			((CtClass)payloads).getClassFile().addAttribute(annotationAttr);
		}else if (payloads instanceof CtField) {
			((CtField)payloads).getFieldInfo().addAttribute(annotationAttr);
		}
	}
    
    /**
     * 参数名（arg0, arg1, ...）
     */
    public String getMethodParamNames(Method m) throws NotFoundException, CannotCompileException {
    	StringBuilder buffer = new StringBuilder();
    	String signature = this.formatArgumentDefinition(m.getParameterTypes());  //(Type arg0,Type arg1)
    	if ("()".equals(signature)) {
    		return "";
    	}
		for (String typeAndName : signature.split(",")) {
			buffer.append(typeAndName.split(" ")[1]);
		}
    	buffer.deleteCharAt(buffer.length()-1);
        return buffer.toString();
    }
    /**
     * WARN: 接口的参数名取不到，类则可以
     */
    public String getSourceArguments(Method m) throws NotFoundException, CannotCompileException {
    	StringBuilder buffer = new StringBuilder();
    	CtMethod method = pool.get(m.getDeclaringClass().getName())
    			.getMethod(m.getName(), ReflectUtils.getDescWithoutMethodName(m));
    	method = CtNewMethod.copy(method, targetClass, null);
    	CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute != null) {
	        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);  
	        if (attr == null) {
	            throw new RuntimeException("LocalVariable is null");
	        }
	        final int pos = Modifier.isStatic(method.getModifiers()) ? 0 : 1,
	        		  len = method.getParameterTypes().length;
	        for (int i = 0; i < len; i++)  {
	        	buffer.append(attr.variableName(i + pos));
	        }
        }
        return buffer.toString();
    }
    
    private String formatArgumentDefinition(Class<?>[] paramTypes) {
    	StringBuilder buffer = new StringBuilder("(");
    	if (paramTypes!=null && paramTypes.length>0) {
    		int i = 0;
	    	for (Class<?> pt : paramTypes) {
	    		buffer.append(pt.getName()).append(" arg"+ i++).append(",");
	    	}
	    	buffer.deleteCharAt(buffer.length()-1);
    	}
    	return buffer.append(')').toString();
    }
    
    private MemberValue getMemberValues(Object value) throws NotFoundException {
		final CtClass type = pool.get(value.getClass().getName());
		if (type == CtClass.booleanType) {
            return new BooleanMemberValue((boolean)value, constPool);
		} else if (type == CtClass.intType) {
            return new IntegerMemberValue((int)value, constPool);
		} else if (type == CtClass.floatType) {
            return new FloatMemberValue(constPool);
		} else if (value.getClass() == String.class) {
            return new StringMemberValue((String)value, constPool);
		} else if (type.isArray()) {
        	ArrayMemberValue arrayValues = new ArrayMemberValue(constPool);
        	Object[] array = (Object[])value;
        	MemberValue[] mvs = new MemberValue[array.length];
        	int i = 0;
        	for (Object element : array) {
        		mvs[i++] = getMemberValues(element);
        	}
        	arrayValues.setValue(mvs);
            return arrayValues;
        } else if (type.isAnnotation()) {
            Annotation info = new Annotation(constPool, type);
            return new AnnotationMemberValue(info, constPool);
        } else {
            EnumMemberValue emv = new EnumMemberValue(constPool);
            emv.setType(type.getName());
            emv.setValue(String.valueOf(value));
            return emv;
        }
    }
    
    // dynamic class tag interface.
    public static interface DC {
    } 
    /**
     * 代理类的注解
     * @author xiongsl
     */
    public static class ProxyAnnotation{
    	private String annotationType;
    	private KvPair[] attributes;
    	
    	public ProxyAnnotation(String type, KvPair... attributes) {
    		this.annotationType = type;
    		if (attributes.length > 0) {
    			this.attributes = attributes;
    		}
    	}
		public String getType() {
			return annotationType;
		}
		public KvPair[] getAttributes() {
			return attributes;
		}
    }
    
}
