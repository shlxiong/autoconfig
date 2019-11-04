package com.openxsl.config.rpcmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 通过fastjson将  Map转换为  String
 * @author xiongsl
 */
public class MapAdapter extends XmlAdapter<String, Map<String,Object>> {
	private final static String KEY_CLASS_PREFIX = "_class$";
	
	@Override
	public String marshal(Map<String,Object> map) throws Exception {
		return marshalMap(map);
	}

	@Override
	public Map<String,Object> unmarshal(String jsonStr) throws Exception {
		return unmarshalMap( JSON.parseObject(jsonStr) );
	}
	
	
	public static Map<String,Object> unmarshalMap(JSONObject map){
		Map<String,Object> result = new HashMap<String,Object>();
		Object entryValue;
		for (Map.Entry<String,Object> entry : map.entrySet()){
			String key = entry.getKey();
			entryValue = entry.getValue();
			if (key.endsWith(KEY_CLASS_PREFIX)) {
				continue;
			}
			String classKey = key + KEY_CLASS_PREFIX;
			String componentType = (String)map.get(classKey);
			if (entryValue instanceof JSONArray){
//				if (componentType == null){
//					throw new IllegalArgumentException("Please set a value of: "+classKey);
//				}
				try {
					Class<?> clazz = Class.forName(componentType);
					List<Object> list = new ArrayList<Object>();
					for (Object obj : (JSONArray)entryValue){
						list.add( parseJsonObject(obj, clazz));
					}
					result.put(key, list);
					clazz = null;
				} catch (ClassNotFoundException e) { //JAXBContext Error!
					result.put(key, entryValue);
				}catch (NullPointerException e) { //componentType==null
					result.put(key, entryValue);
				}
			}else{
				try{
					Class<?> clazz = Class.forName(componentType);
					result.put(key, map.getObject(key, clazz));
					clazz = null;
				}catch (ClassNotFoundException e) { //JAXBContext Error!
					result.put(key, entryValue);
				}catch (NullPointerException e) { //componentType==null
					result.put(key, entryValue);
				}
			}
		}
		
		return result;
	}
	@SuppressWarnings("unchecked")
	private static <T> T parseJsonObject(Object jsonObj, Class<T> clazz){
		if (jsonObj instanceof JSONObject){
			if (Map.class.isAssignableFrom(clazz)){  //HashMap
				return (T)jsonObj;
			}else{
				return JSON.toJavaObject((JSONObject)jsonObj, clazz);
			}
		}else{
			return JSON.parseObject(JSON.toJSONString(jsonObj), clazz);
		}
	}
	
	public static String marshalMap(Map<String,Object> map){
		Map<String, String> classMap = new HashMap<String, String>();
		Object entryValue;
		for (Map.Entry<String,Object> entry : map.entrySet()){
			entryValue = entry.getValue();
			if (entryValue != null){
				if (entry.getKey().endsWith(KEY_CLASS_PREFIX)) {
					continue;
				}
				String classKey = entry.getKey() + KEY_CLASS_PREFIX;
				if (entryValue instanceof Collection){
					try{
						Object first = ((Collection<?>)entryValue).iterator().next();
						classMap.put(classKey, first.getClass().getName());
					}catch(Exception e){
						//entry.getValue() is empty or element is null
					}
				}else if (entryValue.getClass().isArray()){
					classMap.put(classKey, 
							     entryValue.getClass().getComponentType().getName());
				}else{ //Long,Double or javaBean
					Class<?> clazz = entryValue.getClass();
					if (clazz==Long.class || clazz==Double.class ||
							!clazz.getName().startsWith("java.lang.")){
						classMap.put(classKey, clazz.getName());
					}
				}
			}
		}
		map.putAll(classMap);
		classMap.clear();
		return JSON.toJSONString(map);
	}

}
