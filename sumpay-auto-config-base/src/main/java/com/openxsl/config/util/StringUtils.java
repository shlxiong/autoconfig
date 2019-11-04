package com.openxsl.config.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String对象操作工具
 * @author xiongsl
 */
public final class StringUtils {
	
	public static boolean isEmpty(String str){
		return str==null || str.length()==0 || str.equals("null");
	}
	
	public static boolean isEquals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }
	
	/**
	 * 取尾部的若干个字符
	 * @param source
	 * @param len
	 * @return
	 */
	public static String getTail(String source, int len){
		if (source==null || source.length()<len){
			return null;
		}else{
			return source.substring(source.length()-len);
		}
	}
	
	/**
	 * 将字符串剪掉一部分（与substring相反）
	 * @param source
	 * @param pos
	 * @param len
	 * @return
	 */
	public static String cutOff(String source, int pos, int len){
		if (source==null){
			return null;
		}
		
		final int size = source.length();
		final int start = pos+len;
		if (size < pos){
			return source;
		}else if (size < start){
			return source.substring(0, pos);
		}else{
			return source.substring(0, pos) + source.substring(start);
		}
	}
	public static String deleteAt(String source, int pos) {
		if (source==null){
			return null;
		}
		
		final int size = source.length();
		if (pos < 0 || pos > size) {
			return source;
		} else {
			return new StringBuilder(source).deleteCharAt(pos).toString();
		}
	}
	/**
	 * 数字型串转换为固定长度
	 */
	public static String toFixedSizeStr(int idx, int length){
		StringBuilder buffer = new StringBuilder();
		for (int i=0; i<length; i++){
			buffer.append("0");
		}
		buffer.append(idx);
		return buffer.substring(buffer.length()-length);
	}
	
	/**
	 * 占位符
	 */
	public static String processPlaceHolder(final String placeholder, Properties props){
		StringBuilder buffer = new StringBuilder(placeholder);
		int left = placeholder.indexOf("${"), right;
		String k, v;
		while (left > -1){
			right = placeholder.indexOf("}", left+2);
			if (right > left){
				k = placeholder.substring(left+2, right);
				int idx = k.indexOf(":");
				String def = null;
				if (idx != -1) {
					k = k.substring(0, idx);
					def = k.substring(idx+1);
				}
				if (props == null){
					v = System.getProperty(k, def);
				}else{
					v = props.getProperty(k, System.getProperty(k, def));
				}
				if (v == null){
					throw new IllegalStateException(
							String.format("未设置变量【%s】的值: %s", placeholder,k));
				}
				buffer.replace(left, right+1, v);
			}
			left = placeholder.indexOf("${", right+1);
		}
		return buffer.toString();
	}
	
	public static boolean isJavaIdentifier(String s) {
        if (isEmpty(s) || !Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (char ch : s.toCharArray()) {
            if (!Character.isJavaIdentifierPart(ch)) {
                return false;
            }
        }
        return true;
    }
	
	public static String join(String[] array, String separator){
		if (array==null || array.length==0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String str : array){
			sb.append(str).append(separator);
		}
		return sb.deleteCharAt(sb.length()-separator.length()).toString();
	}
	public static String join(String[] array){
		return join(array, ",");
	}
	public static List<String> split2(String source, String separator){
		String[] array = source.split(separator);
		List<String> list = new ArrayList<String>(array.length);
		for (String str : array){
			list.add(str.trim());
		}
		array = null;
		return list;
	}
	/**
	 * 按分隔符拆分，并去除空格（trim）
	 */
	public static String[] split(String source, String separator) {
		String[] array = source.split(separator, -1);
		for (int i=0,len=array.length; i<len; i++) {
			array[i] = array[i].trim();
		}
		return array;
	}
	/**
	 * 求字符串中第N次出现的位置
	 * @return
	 */
	public static int indexOf(String source, String substr, int loop) {
		int index = source.indexOf(substr);
		if (index == -1 || loop < 0) {  //第一次
			return index;
		}
		for (int i=0; i<loop; i++) {
			int next = source.indexOf(substr, index+1);
			if (next == -1) {
				break;
			} else {
				index = next;
			}
		}
		return index;
	}
	
	/**
	 * 驼峰单词转换为分隔符连接（比如数据库字段用下划线）
	 * @param camelName
	 * @param split
	 * @return
	 */
	public static String camelToSplitName(String camelName, String splitor) {
	    if (camelName == null || camelName.length() == 0) {
	        return camelName;
	    }
	    StringBuilder buf = new StringBuilder();
	    for (char ch : camelName.toCharArray()) {
	        if (Character.isUpperCase(ch)) {
	        	buf.append(splitor).append(Character.toLowerCase(ch));
	        } else if (buf != null) {
	            buf.append(ch);
	        }
	    }
	    return buf.toString();
	}
	
	public static String splitToCamel(String source, String splitor){
		if (source == null || source.length() == 0) {
	        return source;
	    }
		StringBuilder buf = new StringBuilder();
		int i = 0;
		for (String str : source.split(splitor)){
			if (i == 0){
				buf.append(str);
				i++;
			}else{
				buf.append(getCapital(str));
			}
		}
		return buf.toString();
	}
	public static String getCapital(String source){
		return Character.toUpperCase(source.charAt(0)) + source.substring(1);
	}
	
	/**
	 * 将Map对象转换成URL字符串，自动按参数名排序，以方便加密等场景
	 */
	public static String toQueryString(Map<String, String> ps) {
		StringBuilder buf = new StringBuilder();
		if (ps != null && ps.size() > 0) {
			for (Map.Entry<String, String> entry : new TreeMap<String, String>(ps).entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (value != null && value.length() > 0) {
					if (buf.length() > 0) {
						buf.append("&");
					}
					buf.append(key);
					buf.append("=");
					buf.append(value);
				}
			}
		}
		return buf.toString();
	}
	
	public static Map<String, String> parseQueryString(String queryString){
		if (queryString == null || queryString.length() == 0){
            return new HashMap<String, String>(1);
		}
		String[] temps = queryString.split("\\&");
		Map<String, String> map = new HashMap<String, String>(temps.length);
		for (String kv : temps){
			int index = kv.indexOf("=");
			if (index > 0){
				map.put(kv.substring(0, index), kv.substring(index+1));
			}
		}
		return map;
	}
	
	public static String getStackTrace(Throwable t){
		StringWriter sw = new StringWriter();
        PrintWriter p = new PrintWriter(sw);
        try {
            t.printStackTrace(p);
            return sw.toString();
        } finally {
            p.close();
        }
	}
	private static final Pattern P_STACK_TRACE = 
				Pattern.compile("^at (.*)\\.(\\w+)\\((\\w+.java):(\\d+)\\)$");
	private static final Pattern UNKOWN_STACK_TRACE = 
			Pattern.compile("^at (.*)\\.(\\w+)\\(.*\\)$");
	public static Throwable getException(String stackTrace){
		String[] traces = stackTrace.split("\n");
		//java.lang.NumberFormatException: For input string: "s"
		String classAndMessage = traces[0];
		int idx = classAndMessage.indexOf(":");
		String className = classAndMessage.substring(0, idx);
		String message = classAndMessage.substring(idx+1).trim();
		Throwable ex = null;
		try{
			ex = (Throwable)Class.forName(className).newInstance();
		}catch(Exception e){
			try{  //new Exception(message);
				ex = (Throwable)Class.forName(className)
							.getConstructor(String.class).newInstance(message);
			}catch(Exception e1){
				throw new IllegalArgumentException("Can't find default constructor or "
						+ "1-arg(String) constructor for exception-class:"+className);
			}
		}
		int len = traces.length;
		StackTraceElement[] elements = new StackTraceElement[len-1];
		Matcher matcher;
		for (int i=1; i<len; i++){
			//at java.lang.Integer.parseInt(Integer.java:527)
			matcher = P_STACK_TRACE.matcher(traces[i].trim());
			if (matcher.matches()){
				elements[i-1] = new StackTraceElement(matcher.group(1), 
									matcher.group(2), //method
									matcher.group(3),  //fileName
									Integer.parseInt(matcher.group(4)));
			} else {
				matcher = UNKOWN_STACK_TRACE.matcher(traces[i].trim());
				if (matcher.matches()){
					elements[i-1] = new StackTraceElement(matcher.group(1), 
							matcher.group(2), //method
							"UnkownSource",  //fileName
							0);
				}
			}
		}
		ex.setStackTrace(elements);
		if (!StringUtils.isEmpty(message)){
			BeanUtils.setPrivateField(ex, "detailMessage", message);
		}
		return ex;
	}
	
	public static boolean parseBoolean(String str){
		if (str==null || str.length()<1){
			return false;
		}
		return  "1".equals(str) || "T".equalsIgnoreCase(str)
				|| Boolean.parseBoolean(str)
				|| "on".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str);
	}
	
	/**
	 * @see StringUtilsTest.test
	public static void main(String[] args) {
		Matcher m = P_STACK_TRACE  //Pattern.compile("^at (.*)\\((\\w+.java):(\\d+)\\)$")
				   .matcher("at java.lang.Integer.parseInt(Integer.java:527)");
		System.out.println(m.matches());
		System.out.println(m.group(1));
		System.out.println(m.group(2));
		System.out.println(m.group(3));
		System.out.println(m.group(4));
		
		try{
			Integer.parseInt("s");
		}catch(Exception e){
			String str = getStackTrace(e);
			System.out.println(str);
			Throwable ex = getException(str);
			System.out.println(getStackTrace(ex));
		}
	}
	*/

}
