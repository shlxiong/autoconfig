package com.openxsl.config.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import com.openxsl.config.Environment;

/**
 * 关键类的版本检测
 * @author xiongsl
 */
public class Version {
	private static final Logger logger = LoggerFactory.getLogger(Version.class);
	
	public static String getVersion(Class<?> cls, String defaultVersion) {
        try {
            // 首先查找MANIFEST.MF规范中的版本号
            String version = cls.getPackage().getImplementationVersion();
            if (version == null || version.length() == 0) {
                version = cls.getPackage().getSpecificationVersion();
            }
            
            if (version == null || version.length() == 0) {
                // 如果规范中没有版本号，基于jar包名获取版本号
                CodeSource codeSource = cls.getProtectionDomain().getCodeSource();
                if (codeSource == null) {
                    logger.info("No codeSource for class {} when getVersion, use default version {}",
                    			cls.getName(), defaultVersion);
                } else {
                    String file = codeSource.getLocation().getFile();  
                    file = file.substring(file.lastIndexOf("/")+1);  //dubbo-rpc-http.2.5.7-SNAPSHOT.jar
                    if (file != null && file.length() > 0 && file.endsWith(".jar")) {
                        version = getJarVersion(file);
                    }
                }
            }
            return version == null || version.length() == 0 ? defaultVersion : version;
        } catch (Throwable e) { // 防御性容错
            logger.error("return default version, ignore exception ", e);
            return defaultVersion;
        }
    }
	public static String getJarVersion(String jarFile){
		Matcher matcher = Patterns.JAR_VERSION.matcher(jarFile);
		if (matcher.matches()){
			return matcher.group(2)
					+ (matcher.group(4)==null ? "" : matcher.group(4));
		}else{
			return null;
		}
	}

	public static boolean expectVersion(Class<?> clazz, String leastVersion, boolean failOnError) {
		checkDuplicate(clazz, failOnError);
		
		String version = getVersion(clazz, "0.0.1");
		String[] majorVersion = getMajorVersion(version).split("\\.");
		String[] laterVersion = getMajorVersion(leastVersion).split("\\.");
		boolean flag = false;
		for (int i = 0; i<majorVersion.length; i++){
			try {
				if (Integer.parseInt(majorVersion[i]) < Integer.parseInt(laterVersion[i])) {
					flag = true;
					break;
				}
			}catch(IndexOutOfBoundsException be) {  //laterVersion less bits
				break;
			}
		}
		if (flag) {
			String error = String.format("version of [%s] is expected more than '%s', but found '%s'",
								clazz.getName(), leastVersion,version);
			if (failOnError) {
				throw new IllegalStateException(error);
			}else {
				logger.error(error);
			}
		}
		return flag;
	}
	public static boolean expectVersionIfExist(String className, String leastVersion, boolean failOnError) {
		if (Environment.exists(className)) {
			try {
				Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
				return expectVersion(clazz, leastVersion, failOnError);
			} catch (ClassNotFoundException | LinkageError e) {
				if (failOnError) {
					throw new IllegalStateException("Can't load class: "+className);
				}
				logger.error("Can't load class: {}", className);
				return false;
			}
		}
		return true;
	}
	
    public static void checkDuplicate(Class<?> cls, boolean failOnError) {
        checkDuplicate(cls.getName().replace('.', '/') + ".class", failOnError);
    }

    public static void checkDuplicate(Class<?> cls) {
        checkDuplicate(cls, false);
    }

    private static void checkDuplicate(String classFile, boolean failOnError) {
    	Set<String> files = new HashSet<String>();
        try {
            Enumeration<URL> urls = ClassUtils.getDefaultClassLoader().getResources(classFile);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url != null) {
                    String file = url.getFile();
                    if (file != null && file.length() > 0) {
                    	int jarIdx = file.indexOf(".jar!");   //xxx.jar!/com/**/
                        if (jarIdx > 0) {
                        	file = file.substring(0, jarIdx+4);
                        }
                        files.add(file);
                    }
                }
            }
        } catch (Throwable e) { // 防御性容错
            logger.error(e.getMessage(), e);
        }
        // 如果有多个，就表示重复
        if (files.size() > 1) {
        	String className = classFile.substring(0,classFile.length()-6).replace('/', '.');
            String error = String.format("Duplicate class %s in %d-jars %s",
            							className, files.size(), files);
            if (failOnError) {
                throw new IllegalStateException(error);
            } else {
                logger.error(error);
            }
        }
    }
    
    private static final PathMatchingResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();
//    private static final AntPathMatcher pathMatcher = new AntPathMatcher("/");
	public static boolean hasResource(final String path, boolean failOnError) {
		boolean flag = false;
		String classpath = ClassUtils.getDefaultClassLoader().getResource("").getFile();
		try {
			if (ResourceUtils.isJarFileURL(new URL("file:"+path))) {
				if (path.indexOf("*") == -1) {
					flag = new File(classpath+"../lib/"+path).exists();
				} else {
					File libPath = new File(classpath+"../lib");
					String[] files = libPath.list(new FilenameFilter() {
								@Override
								public boolean accept(File dir, String name) {
									return RESOLVER.getPathMatcher().match(path, name);
								}
							});
					flag = (files!=null && files.length>0);
				}
			} else {
				flag = RESOLVER.getResources(path).length > 0;
			}
		} catch (IOException e) {
			logger.error("", e);
		}
		if (failOnError && !flag) {
			throw new IllegalStateException("Can't find resource: " + path);
		}
		return flag;
	}
	
	private static final Pattern MAJOR_VERSION = Pattern.compile("(\\d+(.\\d+)*)");
	private static String getMajorVersion(String version) {
		// '-' 会被当做负号
		if (version.contains("-")) {
			version = version.substring(0, version.indexOf("-"));
		}
		Matcher m = MAJOR_VERSION.matcher(version);
		if (m.find()) {
			return m.group(0);
		}else {
			return version;
		}
	}
	
	/**
	 * @See PropertiesLoaderTest.testVersion()
	public static void main(String[] args) throws Exception{
		String path = "D:/eclipse/workspace/dubbo-rpc-api-2.8.1-20170630.015942-5.jar";
		path = "D:/eclipse/workspace/dubbo-rpc-api-2.8.1-SNAPSHOTS.jar";
		path = "dubbo-rpc-api-2.8.1-20170630.015942-5.jar";
//		path = "spring-context-4.3.10.RELEASE.jar";
		String version = getJarVersion(path);
		String major = getMajorVersion(version);
		System.out.println(version+"   "+major);
//		String version = getVersion(ApplicationContext.class, "4.3.10.RELEASE");
//		System.out.println("version="+version+", major="+getMajorVersion(version));
//		
//		expectVersion(ApplicationContext.class, "4.3.10.SNAPSHOT", true);
//		
//		System.out.println(hasResource("openxsl-autoconfig-*.jar", true));
//		System.out.println(hasResource("Version.class", true));
	}
	*/
	
}
