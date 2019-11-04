package com.openxsl.config.groovy;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;

/**
 * Groovy工具类
 * @author xiongsl
 */
public class GroovyInvoker {
	private static Logger logger = LoggerFactory.getLogger(GroovyInvoker.class);
	
	//======================= GroovyShell适合单个表达式 =======================
	/**
	 * 计算groovy脚本(调用Shell)
	 */
	public static Object evaluate(String script, Map<String, Object> params) {
		Binding binding = new Binding(params);
		GroovyShell shell = new GroovyShell(binding);
		return shell.evaluate(script);
	}
	
	//============== JSR-223  ScriptEngineManager ========================
	/**
	 * 调用脚本的某个方法，如果脚本中有static main()，将会执行它
	 * @param script
	 * @param method 方法名
	 * @param params 参数
	 */
	public static Object buildAndInvoke(String script, String method, Object[] params) {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
            engine.eval(script);   //将执行main()
            Invocable inv = (Invocable) engine;
            return inv.invokeFunction(method, params);
        } catch (Exception e) {
        	logger.error("", e);
            return null;
        }
    }
	public static Object buildScript(String script, Map<String, Object> params) {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
            Bindings bindings = engine.createBindings();
            bindings.putAll(params);
            return engine.eval(script, bindings);
        } catch (Exception e) {
        	logger.error("", e);
            return null;
        }
    }
	
	//======================= GroovyScriptEngine多个脚本 ======================
	/**
	 * 调用简单的Groovy源文件的方法
	 */
	public static Object runGroovyFile(String filePath, String method, Object params) {
        try {
        	File file = new File(filePath);
        	String[] path = {file.getParent()};
            GroovyScriptEngine engine = new GroovyScriptEngine(path);
            GroovyObject object = (GroovyObject) engine.loadScriptByName(file.getName()).newInstance();
            return object.invokeMethod(method, params);
//            Script script = engine.createScript(file.getName(), new Binding());
//               //=>InvokerHelper.createScript(loadScriptByName(scriptName), binding);
//            return script.invokeMethod(method, params);
        } catch (Exception e) {
        	logger.error("", e);
            return null;
        }
    }
	
	/**
	 * 调用简Groovy源文件的main()
	 */
	public static void runGroovyFile(String filePath, Map<String, Object> params) {
        try {
        	File file = new File(filePath);
        	String[] path = {file.getParent()};
            GroovyScriptEngine engine = new GroovyScriptEngine(path);
            engine.run(file.getName(), new Binding(params));
//            => engine.createScript(file.getName(), new Binding(params)).run();
        } catch (Exception e) {
        	logger.error("", e);
        }
    }
	
	//================== 适合加载Java接口的实现类 ========================
	public static Object executeScript(String groovyFile, String method, Object args)
				throws Exception {
		GroovyObject object = (GroovyObject)loadClassOfSource(groovyFile).newInstance();
		return object.invokeMethod(method, args);
	}
	public static Object executeScriptClass(String classFile, String method, Object args) 
				throws Exception {
		GroovyObject object = (GroovyObject)loadClassOfBinary(classFile).newInstance();
		return object.invokeMethod(method, args);
	}
	
	static Class<?> loadClassOfSource(String sourceFile) throws CompilationFailedException, IOException{
		GroovyClassLoader groovyClassLoader = getGroovyClassLoader();
		try {
			//产生“script_timestamp_hashcode.groovy”的脚本对象（PermGen）
			return groovyClassLoader.parseClass(new File(sourceFile));
		}finally {
			if (groovyClassLoader != null) {
				groovyClassLoader.close();
			}
		}
	}
	static Class<?> loadClassOfBinary(String binaryFile) throws IOException{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(binaryFile));
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    byte[] bytes = new byte[512];
	    int off = 0, readed = 0;
	    while ((readed=bis.read(bytes, off, 512)) != -1){
	    	bos.write(bytes, 0, readed);
	    	off += readed;
		}
	    bos.flush();
	    GroovyClassLoader groovyClassLoader = getGroovyClassLoader();
	    try {
	    	return groovyClassLoader.defineClass(null, bos.toByteArray());
	    }finally {
	    	if (groovyClassLoader != null) {
	    		groovyClassLoader.close();
	    	}
			if (bos != null) {
				bos.close();
			}
			if (bis != null) {
				bis.close();
			}
		}
	}
	//如果同一个GroovyClassLoader，会导致old script class无法被回收
	static GroovyClassLoader getGroovyClassLoader() {
		return new GroovyClassLoader(ClassUtils.getDefaultClassLoader());
	}
	
	/*
	public static void main(String[] args) throws Exception{
		Object result;
		String filepath = "E:/openxsl/conf/springboot-test/groovy/HelloWorld.groovy";
		result = GroovyInvoker.runGroovyFile(filepath, "hello", "xiongsl");
		System.out.println("===================="+result);
		GroovyInvoker.runGroovyFile(filepath, null);  //main
		System.out.println("--------------------");
		System.out.println();
		
		filepath = "E:/openxsl/conf/springboot-test/groovy/Hello.groovy";
		result = executeScript(filepath, "getTime", new java.util.GregorianCalendar().getTime());
		System.out.println(result);
		System.out.println();
		
		String script = "def getTime(date){return date.getTime();}\n"
				+ "def sayHello(name,age){return 'Hello,I am ' + name + ',age=' + age;}\n"
				+ "static void main(args){println('main method invoked. passby engine.eval()'); return this; }";
		result = buildAndInvoke(script, "sayHello", new Object[] {"tomcat", 20});
		System.out.println(result);
		System.out.println("====================");
		Map<String,Object> params = new java.util.HashMap<String,Object>(2);
		params.put("name", "tomcat");
		params.put("age", 20);
		result = buildScript(script, params);
		System.out.println(result);
	}
	*/

}
