package com.openxsl.config.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import com.alibaba.fastjson.JSON;

//import com.alibaba.dubbo.common.utils.ReflectUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MapUtils {
	
	/**
	 * 只保留某些key的值，如果modify=false，将返回一个新的HashMap对象
	 * @param map    数据源，有可能会被修改
	 * @param keys   需要保留的key
	 * @param modify 是否修改参数map
	 */
	public static Map retainsEntries(Map<?,?> map, Collection<?> keys, boolean modify){
		if (modify){
			for (Object key : new HashSet(map.keySet())){
				if (!keys.contains(key)){
					map.remove(key);
				}
			}
			return map;
		}else{
			Map targetMap = new HashMap(keys.size());
			for (Map.Entry<?,?> entry : map.entrySet()){
				if (keys.contains(entry.getKey())){
					targetMap.put(entry.getKey(), entry.getValue());
				}
			}
			return targetMap;
		}
	}
	
	/**
	 * 删除一些key，如果modify=false，将返回一个新的HashMap对象
	 */
	public static Map removeEntries(Map<?,?> map, Collection<?> keys, boolean modify){
		if (modify){
			for (Object key : new HashSet(map.keySet())){
				if (keys.contains(key)){
					map.remove(key);
				}
			}
			return map;
		}else{
			Map targetMap = new HashMap(keys.size());
			for (Map.Entry<?,?> entry : map.entrySet()){
				if (!keys.contains(entry.getKey())){
					targetMap.put(entry.getKey(), entry.getValue());
				}
			}
			return targetMap;
		}
	}
	
	/**
	 * 判断两个Map对象是否相等
	 */
	public static boolean mapEquals(Map<?, ?> map1, Map<?, ?> map2) {
		if (map1 == null && map2 == null) {
			return true;
		}
		if (map1 == null || map2 == null) {
			return false;
		}
		if (map1.size() != map2.size()) {
			return false;
		}
		Object key, value1, value2;
		for (Map.Entry<?, ?> entry : map1.entrySet()) {
			key = entry.getKey();
			value1 = entry.getValue();
			value2 = map2.get(key);
			if (value1 == null){
				if (value2 != null) {
					return false;
				}
			}else if (!value1.equals(value2)) {
				return false;
			}
		}
		return true;
	}
	
	public static Map<Object,Object> newMap(boolean sortable, Object... kvs){
		final int len = kvs.length;
		final int size = len / 2;   //奇数的丢掉
		Map<Object,Object> results = sortable? new LinkedHashMap<Object,Object>(size)
				: new HashMap<Object,Object>(size);
		for (int i=0,j=0; j<size; j++){
			results.put(kvs[i], kvs[++i]);
		}
		return results;
	}
	
	/**
	 * 将Map扁平化处理。即将多层次的Map对象，转换为只有一层的结构，key的层级间隔符为"/"
	 */
	public static Map<String,?> flattenMap(Map<String,Object> map, String path){
		final boolean cascade = (path.length() > 0);
		if (cascade){
			path += "/";
		}
		Map<String,Object> resultMap = new TreeMap<String,Object>();
		String newKey, childKey;
		Object value;
		for (String key : new TreeSet<String>(map.keySet())){
			newKey = path + key;
			value = map.get(key);
			if (value==null || ReflectUtils.isPrimitive(value.getClass())){
				resultMap.put(newKey, value);
			}else if (value instanceof Map){ //JSONObject
				resultMap.putAll(flattenMap((Map)value, newKey));
			}else if (value instanceof Collection){
				childKey = newKey + "$[_idx_]";
				int i = 0;
				for (Object elt : (Collection<?>)value){
					if (elt instanceof Map){
						resultMap.putAll(flattenMap((Map)elt, 
									childKey.replace("_idx_", String.valueOf(i))));
					}else{
						resultMap.put(childKey.replace("_idx_", String.valueOf(i)),
									elt);
					}
					i++;
				}
			}
		}
		
		return resultMap;
	}
	public static Map<String,?> flattenJson2Map(String json, String path){
		json = json.trim();
		if (json.charAt(0)=='[' && json.endsWith("]")){  //数组
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("/List/", JSON.parseArray(json));
			return flattenMap(resultMap, "");
		}else{ //JSONObject
			return flattenMap(JSON.parseObject(json), "");
		}
	}
	
	/**
	 * 当Map中不存在Key时才插入
	 * @see Hashtable#putIfAbsent
	 * @param map
	 * @param key
	 * @param value
	 */
	public static <T> void putIfAbsent(Map<String,T> map, String key, T value) {
		if (!map.containsKey(key) && value!=null) {
			map.put(key, value);
		}
	}
	//@see Hashtable#putIfAbsent
	public static <T> void putIfAbsent(Properties props, String key, String value) {
		if (!props.containsKey(key) && value!=null) {
			props.setProperty(key, value);
		}
	}
	
	public static Map<String,String> fromString(String mapStr) {
		Map<String,String> map = new HashMap<String,String>();
		StringTokenizer tokens = new StringTokenizer(mapStr, "=, {}");
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			map.put(token, tokens.nextToken());
		}
		return map;
	}

}
