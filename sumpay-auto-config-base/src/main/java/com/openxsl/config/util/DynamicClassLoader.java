package com.openxsl.config.util;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * 动态ClassLoader
 * @author xiongsl
 */
public class DynamicClassLoader extends ClassLoader{
	private ConcurrentMap<String, Class<?>> loadedClassMap
					= new ConcurrentHashMap<String, Class<?>>();
	private URLClassLoader classloader;
	
	public DynamicClassLoader(ClassLoader parent) {
	   super(parent);
	   classloader = new URLClassLoader(new URL[]{}, parent);
	}
	public DynamicClassLoader(){
		//Thread classloader or ClassLoaderUtils classloader
		this(ClassLoaderUtils.getClassLoader(null));
	}
	
	/**
	 * 增加一个lib路径
	 * @param libPath
	 * @throws IOException
	 */
	public void addLibPath(String libPath) throws IOException{
		Set<URL> urlSet = new HashSet<URL>(Arrays.asList(classloader.getURLs()));
		for (URL url : this.getJars(libPath)){
			urlSet.add(url);
		}
		URL[] urls = new URL[urlSet.size()];
		urlSet.toArray(urls);
		synchronized (classloader){
			classloader.close();
			classloader = new URLClassLoader(urls, super.getParent());
		}
		urlSet.clear();
		urlSet = null;
	}
	/**
	 * 在classloader中增加一个jar
	 * @param jarfile
	 * @throws IOException
	 */
	public void addJar(String jarfile)throws IOException{
		Set<URL> urlSet = new HashSet<URL>(Arrays.asList(classloader.getURLs()));
		URL url = new URL(jarfile);
		if (!urlSet.contains(url)){
			urlSet.add(url);
			URL[] urls = new URL[urlSet.size()];
			urlSet.toArray(urls);
			synchronized (classloader){
				classloader.close();
				classloader = new URLClassLoader(urls, super.getParent());
			}
		}
		urlSet.clear();
		urlSet = null;
	}
	
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException{
		if (loadedClassMap.containsKey(className)){
			return loadedClassMap.get(className);
		}else{
			Class<?> clazz = this.loadClassFromJars(classloader, className);
			if (clazz != null){
				loadedClassMap.put(className, clazz);
			}
			return clazz;
		}
	}
	
	/**
	 * 从某个目录的package子目录（classes）或jar文件（lib）中加载类
	 * @param classPath  classes或libpath: 相对路径或以“file:”开始的
	 * @param className  完整的类名
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> loadClass(String classPath, String className)
				throws ClassNotFoundException {
		if (loadedClassMap.containsKey(className)){
			return loadedClassMap.get(className);
		}
		
		if (classPath == null){
			return Class.forName(className);
		}else if (!classPath.startsWith("file:")){
			classPath = this.getParent().getResource(".") + classPath;
		}
		int idx = className.indexOf(".class");
        if (idx != -1){
    	    className = className.substring(0, idx);
        }
        
        Class<?> clazz = null;
	    try {
	        byte[] classData = readByteCode(classPath, className);
	        clazz = this.defineClass(className, classData, 0, classData.length);
	    } catch (IOException e) {
	    	//FileNotFound OR MalformedURL，从jar里面找
	    	URLClassLoader classloader = new URLClassLoader(
	    				this.getJars(classPath), getParent());
	    	clazz = this.loadClassFromJars(classloader, className);
	    }
	    if (className != null){
	    	loadedClassMap.put(className, clazz);
	    }
	    return clazz;
    }
	
	/**
	 * 类放在文件夹里面
	 * @param classpath  classes路径
	 * @param className  全类名
	 * @return
	 */
	private byte[] readByteCode(String classpath, String className) throws IOException{
		final String separator = File.separator;
		classpath = classpath.replace("\\", separator);
		if (!classpath.endsWith(separator)){
			classpath += separator;
		}
		String url = classpath + className.replace(".", separator) + ".class";
		InputStream is = new URL(url).openConnection().getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] classData = null;
        try{
	        int data = is.read();
	        while (data != -1) {
	      	    buffer.write(data);
	    	    data = is.read();
	        }
	        classData = buffer.toByteArray();
        }finally{
        	is.close();
            buffer.close();
        }
        return classData;
	}
	
	private Class<?> loadClassFromJars(URLClassLoader classloader, String className)
				throws ClassNotFoundException{
		try{
			return classloader.loadClass(className);
		}finally{
			if (classloader != this.classloader){
				try {
					classloader.close();
				} catch (IOException e) {
				}
				classloader = null;
			}
		}
	}
	
	@Override
	protected void finalize(){
		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}
	
	/**
	 * 取得目录下 jar文件的URL
	 * @param libPath
	 * @return
	 */
	private URL[] getJars(String libPath){
		File[] jarFiles = null;
		try {
			URI uri = new URI(libPath);
			jarFiles = new File(uri).listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			});
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		if (jarFiles == null){
			return null;
		}

		URL[] urls = new URL[jarFiles.length];
		int i = 0;
		for (File file : jarFiles) {
			try {
				urls[i++] = file.toURI().toURL();
			} catch (MalformedURLException e) {
			}
        }
		jarFiles = null;
		return urls;
	}
	
	static void lookupSourceFromManifest(StringBuilder classPath, File file) 
	        	throws URISyntaxException, IOException {
        JarFile jar = new JarFile(file);
        Attributes attr = null;
        if (jar.getManifest() != null) {
            attr = jar.getManifest().getMainAttributes();
        }
        if (attr != null) {
            String cp = attr.getValue("Class-Path");
            if (cp != null){
	            for (String fileName : cp.split(" ")) {
	                URI uri = new URI(fileName);
	                File f2;
	                if (uri.isAbsolute()) {
	                    f2 = new File(uri);
	                } else {
	                    f2 = new File(file, fileName);
	                }
	                if (f2.exists()) {
	                    classPath.append(f2.getCanonicalPath());
	                    classPath.append(" ");
	                }
	            }
            }
        }
        jar.close();
    }
	
//	private boolean compile(String javaFile) throws IOException {  
//        // 启动编译器  
//        Process p = Runtime.getRuntime().exec(
//                "javac -classpath " + getClass().getResource("/").getPath()
//                        + " -Xlint:unchecked " + javaFile);
//        // 等待编译结束 
//        try {
//            p.waitFor();
//        } catch (InterruptedException ie) {
//            System.out.println(ie);
//        }
//        // 检查返回码，看编译是否出错。  
//        return p.exitValue() == 0;
//    }
	
	public static void main(String[] args) throws Exception{
		DynamicClassLoader loader = new DynamicClassLoader();
		String className = "javax.mail.Address";
//		try{
//			System.out.println(Class.forName(className));
//		}catch(ClassNotFoundException cnfe){
//			System.out.println(loader.loadClass("file:/D:/tmp", className));
//		}
		
		loader.addJar("file:/D:/tmp/mail-1.4.4.jar");
		System.out.println(loader.loadClass(className));
		
		System.out.println(Class.forName(className));
	}

}
