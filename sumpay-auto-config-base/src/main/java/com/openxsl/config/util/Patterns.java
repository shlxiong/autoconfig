package com.openxsl.config.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用的正则表达式
 * @author xiongsl
 */
public class Patterns {
	
	/**数组表达式：(\\w+)\\[(\\d+)\\]*/
	public static final Pattern ARRAY = Pattern.compile("(\\w+)\\[(\\d+)\\]");
	/**XML头部表达式： ^(<\\?xml (.*)\\?>)(.*)*/
	public static final Pattern XMLHEADER = Pattern.compile("^(<\\?xml (.*)\\?>)(.*)");
	@Deprecated
	/**XML节点之间的空格、回车等：\\s*|t|r|n*/
	public static final Pattern COMPACTRIM = Pattern.compile("\\s*|t|r|n");
	/**身份证号*/
	public static final Pattern PERSON_ID = Pattern.compile("^(\\d{2})\\d{4}(\\d{8})\\d{3}(\\d|X)$");
	/**15位身份证号*/
	public static final Pattern PERSON_ID15 = Pattern.compile("^(\\d{2})\\d{4}(\\d{6})\\d{3}$");
	/**手机号*/
	public static final Pattern MOBILE = Pattern.compile("^1(3|4|5|7|8)\\d{9}$");
	/**占位符变量：^\\$\\{(.*)\\}$*/
	public static final Pattern PLACE_HOLDER = Pattern.compile("^\\$\\{(.*)\\}$");
	/**函数、表达式(\\w+)\\((.*)\\)$*/
	public static final Pattern FUNCTION = Pattern.compile("(\\w+)\\((.*)\\)$");
	/**IP地址__按'位'正则：[1~255](.[0~255]){3}$*/
	public static final Pattern HOST_IP = Pattern.compile("^([1-9]\\d?|(1\\d{2}|2[0-4]\\d|25[0-5]))(\\.([1-9]?\\d|(1\\d{2}|2[0-4]\\d|25[0-5]))){3}$");
	/**调子邮件：^[a-zA-Z0-9_\\.-]+ @ ([a-zA-Z0-9-]+\\.)+ [a-zA-Z0-9]{2,4}$*/
	public static final Pattern EMAIL = Pattern.compile("^[a-zA-Z0-9_\\.-]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z0-9]{2,4}$");
	
	/**Insert语句：INSERT INTO {table} ({f1,f2}) VALUES ({?,?})*/
	public static final Pattern SQL_INSERT = Pattern.compile(
				"^INSERT\\s+INTO\\s+(\\S+)\\s*\\((.*)\\)\\s*VALUES\\s*\\((.*)\\)$");
	/**Delete语句：DELETE {FROM} {table} WHERE {f1=v1}*/
	public static final Pattern SQL_DELETE = Pattern.compile(
				"^DELETE\\s+(FROM)?\\s+(\\S+)\\s+WHERE\\s+(.*)");
	/**Update语句：UPDATE {table} SET {f1=v1} WHERE {f2=v2}*/
	public static final Pattern SQL_UPDATE = Pattern.compile(
				"^UPDATE\\s+(\\S+)\\s+SET\\s+(.*)\\s+WHERE\\s+(.*)");
	/**DDL语句，^(CREATE|DROP|ALTER)\\s+(TABLE|INDEX)\\s+(\\S+)(.*)*/
	public static final Pattern SQL_DDL = Pattern.compile(
			"^(CREATE|DROP|ALTER)\\s+(TABLE|INDEX)\\s+(\\S+)(.*)");
	/**Sql表达式：{field} {operator} {operant}*/
	public static final Pattern SQL_EXPR = Pattern.compile(
				"(\\w+)\\s+(=|<|>|<>|!=|IN|LIKE|IS|BTWEEN)\\s+(.*)");
	
	/**jar文件版本：^([A-Za-z\\-]+)-(\\d+(.\\d+)*)([.-].*)*.jar$*/
	public static final Pattern JAR_VERSION = Pattern.compile(
				"^([A-Za-z\\-]+)-(\\d+(.\\d+)*)([.-].*)*.jar$");
	
	/**
	 * 取正则表达式第N部分的值
	 */
	public static String getRegexpValue(String regexp, String source, int index) {
		Matcher matcher = Pattern.compile(regexp).matcher(source);
		if (matcher.matches() || matcher.find()) {
			try {
				return matcher.group(index);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				matcher = null;
			}
		} 
		return "";
	}
	
}
