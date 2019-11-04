package com.openxsl.config.dal.jdbc.sqlparse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openxsl.config.util.Patterns;

/**
 * JPA命名参数解析，不适合于Oracle
 * 
 * @author 001327-xiongsl
 */
public class NamedParameterParser {
	static final char ARG_CHAR = ':';   //参数的标识符，:name
	
	/**
	 * 解析sql语句中的参数名，并将参数名替换为'?'占位符
	 * @param namedSql
	 * @return 参数名
	 */
	public static List<String> getSqlArgumentNames(StringBuilder namedSql){
		List<String> argNames = new ArrayList<String>();
		String strSql = namedSql.toString();
		Matcher matcher = Patterns.SQL_INSERT.matcher(namedSql);
		if (matcher.matches()){
			for (String arg : matcher.group(3).split(SqlParser.SP_FIELD)){
				collectArgument(argNames, arg);
			}
		}else{
			String[] exprs;
			matcher = Patterns.SQL_DELETE.matcher(namedSql);
			if (matcher.matches()){
				exprs = matcher.group(3).split("\\s+(AND|OR)\\s+");  //where
				argNames.addAll(getSqlExprArgNames(exprs));
			}else{
				matcher = Patterns.SQL_UPDATE.matcher(namedSql);
				if (matcher.matches()){
					exprs = matcher.group(2).split(SqlParser.SP_FIELD);  //set
					argNames.addAll(getSqlExprArgNames(exprs));
					exprs = matcher.group(3).split("\\s+(AND|OR)\\s+");  //where
					argNames.addAll(getSqlExprArgNames(exprs));
				}else{ //SELECT嵌套查询有问题
//					String[] selectParts = getSelectPartitions(strSql);
//					if (selectParts[2] != null){
//						exprs = selectParts[2].split("\\s+(AND|OR)\\s+");  //where
//						argNames.addAll(getSqlExprArgNames(exprs));
//					}
//					if (selectParts[4] != null){
//						exprs = selectParts[4].split("\\s+(AND|OR)\\s+");  //having
//						argNames.addAll(getSqlExprArgNames(exprs));
//					}
					int start = -1;
					while ((start=strSql.indexOf(ARG_CHAR,start+1)) > 0) {
						String temp = strSql.substring(start).split("\\s+")[0];
						collectArgument(argNames, temp);
					}
				}
			}
		}
		
		for (String argName : argNames){
			strSql = strSql.replaceFirst(ARG_CHAR+argName, "\\?");
		}
		namedSql.delete(0, namedSql.length()).append(strSql);
		return argNames;
	}
	
	/**
	 * 解析多个sql表达式的参数名
	 * @param expressions
	 * @param separator  间隔符
	 * @return
	 */
	public static List<String> getSqlExprArgNames(String[] expressions){
		List<String> argNames = new ArrayList<String>(expressions.length);
		Matcher matcher;
		for (String expr : expressions){
			while (expr.charAt(0) == '('){
				expr = expr.substring(1);
			}
			while (expr.endsWith(")")){
				expr = expr.substring(0, expr.length()-1);
			}
			matcher = Patterns.SQL_EXPR.matcher(expr);
			matcher.matches();
			if (matcher.group(2).equals("BETWEEN")){
				for (String str : matcher.group(3).split("\\s+and\\s+")){
					collectArgument(argNames, str);
				}
			}else{
				String arg = matcher.group(3);
				collectArgument(argNames, arg);
			}
		}
		return argNames;
	}
	
	/**
	 * 解析Select语句：[SELECT, FROM, WHERE, GROUPBY, HAVING, ORDERBY]
	 * @param sql 简单查询语句，嵌套（如Oracle分页查询）语法还有待改进
	 * @return
	 */
	public static String[] getSelectPartitions(String sql){
		String[] partitions = new String[6];
		String regexp = "SELECT\\s+(\\S+)\\s+FROM\\s+(\\S+)(\\s+(.*))?";
		Matcher matcher = Pattern.compile(regexp).matcher(sql.trim());
		if (matcher.matches()){
			partitions[0] = matcher.group(1);    //fields
			partitions[1] = matcher.group(2);    //table
			String addition = matcher.group(3);  //where, group by, having, order by
			if (addition != null){
				addition = addition.trim();
				int idx = addition.indexOf("LIMIT ");
				if (idx != -1){
					addition = addition.substring(0, idx).trim();
				}
				idx = addition.indexOf("ORDER BY ");
				if (idx != -1){
					String orderStmt = addition.substring(idx);
					addition = addition.substring(0, idx).trim();
					partitions[5] = orderStmt.substring("ORDER BY ".length());
				}
				idx = addition.indexOf("GROUP BY ");
				if (idx != -1){
					String groupbyStmt = addition.substring(idx);
					addition = addition.substring(0, idx).trim();
					idx = groupbyStmt.indexOf(" HAVING ");
					if (idx != -1){
						String havingStmt = groupbyStmt.substring(idx+" HAVING ".length());
						groupbyStmt = groupbyStmt.substring(0, idx);
						partitions[4] = havingStmt;
					}
					partitions[3] = groupbyStmt.substring("GROUP BY ".length());
				}
				if (addition.length() > 0){
					String whereStmt = addition;
					partitions[2] = whereStmt.substring("WHERE ".length());
				}
			}
		}else{
			System.out.println("No matches -->"+sql);
		}
		return partitions;
	}
	
	private static final void collectArgument(List<String> results, String value) {
		if (value.indexOf(ARG_CHAR) > -1) {  //去掉 ':'， value为':param' 或  'f2+:param'
			results.add(value.substring(value.indexOf(ARG_CHAR)+1));
		} else {
			// 不是变量
		}
	}
	
	public static void main(String[] args) {
		String sql = "SELECT id,username FROM ft_user_info WHERE username=:userName AND password=:password"
				+ " GROUP BY username HAVING role=:roleId ORDER BY id";
//		getSelectPartitions(sql);
		StringBuilder bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		sql = "SELECT id,username FROM ft_user_info WHERE username=:userName AND password=:password"
//				+ " GROUP BY username HAVING role=:roleId";
////		getSelectPartitions(sql);
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		sql = "SELECT id,username FROM ft_user_info WHERE username=:userName AND password=:password ORDER BY id";
////		getSelectPartitions(sql);
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		sql = "SELECT id,username FROM ft_user_info GROUP BY username HAVING role=:roleId ORDER BY id";
////		getSelectPartitions(sql);
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		sql = "SELECT id,username FROM ft_user_info WHERE username=:userName AND password=:password";
////		getSelectPartitions(sql);
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		sql = "SELECT id,username FROM ft_user_info GROUP BY username HAVING role=:roleId";
////		getSelectPartitions(sql);
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		sql = "SELECT id,username FROM ft_user_info ORDER BY id";
////		getSelectPartitions(sql);
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		sql = "SELECT id,username FROM ft_user_info";
////		getSelectPartitions(sql);
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		
//		sql = "UPDATE ft_user_info SET full_name = :fullName WHERE id = 1";
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		
//		sql = "DELETE FROM ft_user_info WHERE id BTWEEN :id1 and :id2 AND (role = :role1 OR role = :role2)";
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
//		
//		sql = "INSERT INTO ft_user_info(id,username,password,full_name) VALUES (:userId,:userName,:password,:fullName)";
//		bufSql = new StringBuilder(sql);
//		System.out.println(getSqlArgumentNames(bufSql));
		
		sql = "UPDATE sms_segment_info SET provider = :provider,channel = :channel,chnl_bak = :chnlBak WHERE segment = :segment";
		sql = "UPDATE sms_channel_month SET succ = succ+:succ WHERE month = :month AND channel = :channel";
		bufSql = new StringBuilder(sql);
		System.out.println(getSqlArgumentNames(bufSql));
		System.out.println(bufSql.toString());
	}

}
