package com.openxsl.config.loader;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;

import com.openxsl.config.ApolloConfigAdaptor;
import com.openxsl.config.EnvironmentLoader;
import com.openxsl.config.util.HexEncoder;
import com.openxsl.config.util.StringUtils;

/**
 * 从配置文件中提取指定前缀的属性
 * 2017-05-17 支持Base64串的解密
 * @author 001327
 * @see com.openxsl.config.loader.DomainPropertyLoader
 */
public class PrefixProperties implements InitializingBean, FactoryBean<Properties>{
	/**处理加密串*/
	public final static String PREF_SECRET = "Secret(";
	private final static int LEN = PREF_SECRET.length();
	
	private List<Resource> configLocations;
	private String regexp;    //正则匹配（优先）
	private String prefix;    //前缀匹配
	private boolean rewrite;
	private Properties props = new Properties();
	
	public static Properties get(String configLocation, String prefix,
						boolean rewrite){
		PrefixProperties loader = new PrefixProperties();
		loader.setConfigLocation(configLocation);
		loader.setPrefix(prefix);
		loader.setRewriteKeys(rewrite);
		try {
			loader.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loader.getObject();
	}
	/**
	 * 根据前缀过滤属性
	 * @param originalProps 原始属性
	 * @param prefix        前缀
	 * @param rewrite   	 是否去掉前缀
	 * @throws Exception
	 */
	public static Properties prefixProperties(Properties originalProps,
					String prefix, boolean rewrite) throws Exception {
		Pattern pattern = Pattern.compile("^("+prefix+"\\.)(.*)");
		return filterProperties(originalProps, pattern, rewrite);
	}
	/**
	 * 使用正则表达式过滤属性的键名
	 * @param originalProps 原始属性
	 * @param prefix        正则表达式（group(1)可能会被去掉）
	 * @param rewrite   	 是否去掉前缀
	 */
	public static Properties filterProperties(final Properties originalProps,
						final Pattern pattern, final boolean rewrite)
				throws Exception{
		Properties target = new Properties();
		for (Map.Entry<?,?> entry : originalProps.entrySet()){
			String key = (String)entry.getKey();
			Matcher matcher = pattern.matcher(key);
			if (matcher.matches()){
				String value = originalProps.get(key).toString().trim();
				value = StringUtils.processPlaceHolder(value, originalProps);
				if (rewrite){  //一定做prefix
					key = matcher.group(2);
				}
				if (value.startsWith(PREF_SECRET)){  //加密的
					value = HexEncoder.decode( stripSecret(value) );
				}
				target.setProperty(key, new String(value.getBytes("ISO-8859-1"),"UTF-8"));
			}
		}
		return target;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(configLocations, "尚未指定配置文件路径(setConfigLocation)");
		boolean flag = StringUtils.isEmpty(regexp);
		if (rewrite && flag){
			Assert.hasText(prefix, "尚未指定前缀(setPrefix)");
		}
		
		Properties props2 = new Properties();
		for (Resource res : configLocations){
			props2.putAll(EnvironmentLoader.readProperties(res));
		}
		ApolloConfigAdaptor.reloadProperties(props2);
		
		if (!flag) {  //regexp过滤第一遍，但不会rewrite
			Pattern pattern = Pattern.compile(regexp);
			props2 = filterProperties(props2, pattern, false);
		}
		if (!StringUtils.isEmpty(prefix)) {  //prefix
			Pattern pattern = Pattern.compile("^("+prefix+"\\.)(.*)");
			this.props = filterProperties(props2, pattern, rewrite);
		} else {
			this.props = props2;
		}
	}

	@Override
	public Properties getObject() {
		return props;
	}

	@Override
	public Class<?> getObjectType() {
		return Properties.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setConfigLocation(Resource configLocation) {
		this.configLocations = new ArrayList<Resource>(1);
		this.configLocations.add(configLocation);
	}
	public void setConfigLocations(List<Resource> locations) {
		if (locations != null){
			this.configLocations = locations;
		}
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}
	public void setRewriteKeys(boolean rewrite){
		this.rewrite = rewrite;
	}
	
	//PathMatchingResourcePatternResolver引入了commons-logging，从而造成失败
	final DefaultResourceLoader loader = new DefaultResourceLoader();
	public void setConfigLocation(String configLocation) {
		this.configLocations = new ArrayList<Resource>(1);
		this.addInclude(configLocation);
	}
	public void addInclude(String configLocation) {
		Assert.notNull(this.configLocations, "include location is null");
		try {
			//从目录或Url中找，包括classpath目录
			Resource resource = loader.getResource(configLocation);
			if (resource.isReadable()){
				this.configLocations.add(0, resource);
			}else{ //从jar文件中找，可以是‘classpath:’和‘classpath*:’
				String location = configLocation.substring(configLocation.indexOf(":")+1);
				if (location.startsWith("/")) {
					location = location.substring(1);
				}
				Enumeration<URL> resourceUrls = loader.getClassLoader().getResources(location);
				while (resourceUrls.hasMoreElements()) {
					URL url = resourceUrls.nextElement();
					this.configLocations.add(new UrlResource(url));
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * 去除“Secret(...)”字样
	 * @see {@link #filterProperties()}, {@link #recoverSecretText(String, boolean, boolean)
	 */
	public static final String stripSecret(String value){
		if (value.startsWith(PREF_SECRET)){
			if (value.endsWith(")")){
				value = value.substring(LEN, value.length()-1);
			}else{
				value = value.substring(LEN);
			}
		}
		return value;
	}
	
	/**
	 * 还原文本中包含Secret(...)的字符
	 * @param text  原始字符串
	 * @param mask  是否用掩码输出
	 * @param ifDecode  是否解密（即原串中是加密的）
	 */
	public static String recoverSecretText(String text, boolean mask, boolean ifDecode) {
		if (text == null) {
			return null;
		}
		String message = text;
		String format = PREF_SECRET.replace("(", "\\(%s\\)");
		int idx = -1;
		while ((idx=text.indexOf(PREF_SECRET,idx+1)) != -1) {
			int end = text.indexOf(")", idx+1);
			if (end == -1) {
				break;
			}
			String source = stripSecret(text.substring(idx, end));  //key
			String secret = String.format(format, source);  //Secret(key)
			if (ifDecode) {
				source = HexEncoder.decode(source);
			}
			if (mask) {
				message = message.replaceFirst(secret, "******");
			} else {
				message = message.replaceFirst(secret, source);
			}
		}
		return message;
	}
	
	public static void main(String[] args) {
		String templ = "登录名admin, 初始登录密码为Secret({0})，初始操作密码为Secret({1})";
		String text = MessageFormat.format(templ, "p@ssw0rd","654321");
		text = recoverSecretText(text, false, false);
		System.out.println(text);
		
		text = MessageFormat.format(templ, HexEncoder.encode("p@ssw0rd"),HexEncoder.encode("654321"));
		text = recoverSecretText(text, false, true);
		System.out.println(text);
	}
	
}
