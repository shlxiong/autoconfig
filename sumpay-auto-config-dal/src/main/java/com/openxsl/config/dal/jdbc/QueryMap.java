package com.openxsl.config.dal.jdbc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <pre>
 * 查询条件是有顺序的，所以不能用HashMap
 * 该对象的Key是String，是对象的属性(不是字段名)，或者属性+操作符
 * </pre>
 * @author 001327-xiongsl
 * @param <V>
 */
@SuppressWarnings("serial")
public class QueryMap<V> extends LinkedHashMap<String, V> {
	
	public QueryMap(){}
	public QueryMap(String key, V value){
		this.put(key, value);
	}
	public QueryMap(int size){
		super(size);
	}
	public QueryMap(Map<String, V> examples){
		this(examples.size());
		for (Map.Entry<String,V> entry : examples.entrySet()){
			this.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static class Orderby{
		public static int ASC = 0;
		public static int DESC = 1;
		private String field;
		private String sort;
		
		public Orderby(String field, int sort){
			this.setField(field);
			this.setSort(sort==0 ? "asc" : "desc");
		}
		public Orderby(String field, String sortStr){
			this.setField(field);
			if (sortStr==null || sortStr.equalsIgnoreCase("asc")){
				this.setSort("asc");
			}else{
				this.setSort("desc");
			}
		}
		
		public String toString(){
			return new StringBuilder(field).append(" ").append(sort).toString();
		}
		public String toSql(){
			return " ORDER BY " + toString();
		}
		
		public String getField() {
			return field;
		}
		public void setField(String field) {
			this.field = field;
		}
		public String getSort() {
			return sort;
		}
		public void setSort(String sort) {
			this.sort = sort;
		}
	}

}
