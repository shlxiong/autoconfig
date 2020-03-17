package com.openxsl.config.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 集合类操作，包括数组
 * 
 * @author xiongsl
 */
@SuppressWarnings("unchecked")
public class CollectionUtils {
	
	/**
	 * 判断是否为空
	 */
	public static boolean isEmpty(Collection<?> collection){
		return collection==null || collection.size()==0;
	}
	public static <T> boolean arrayIsEmpty(T[] array){
		return array==null || array.length<1;
	}
	public static <T> boolean contains(T[] array, T obj){
		if (array==null || array.length<1){
			return false;
		}
		for (T elt : array){
			if (obj == null){
				if (elt == null) {
					return true;
				}
			}else if (obj.equals(elt)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断两个列表元素是否相同
	 */
	public static boolean equals(List<?> list1, List<?> list2){
		if (list1==null && list2==null){
			return true;
		}
		if (list1 == null || list2 == null) {
			return false;
		}
		if (list1.size() != list2.size()) {
			return false;
		}
		for (Object elt : list1){
			if (!list2.contains(elt)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 去重
	 */
	public static <T> List<T> distinct(List<T> list){
		if (list == null){
			return new ArrayList<T>(0);
		}
		Set<T> tempSet = new HashSet<T>(list.size());
		List<T> targets = new ArrayList<T>(list.size());
		for (T elt : list){
			if (tempSet.contains(elt)) {
				continue;
			}
			tempSet.add(elt);
			targets.add(elt);
		}
		tempSet.clear();
		tempSet = null;
		return targets;
	}
	
	/**
	 * 判断列表里的元素是否唯一
	 */
	public static <T> boolean isUnique(List<T> list){
		if (isEmpty(list)) {
			return true;
		}
		return new HashSet<T>(list).size() == list.size();
	}
	
	/**
	 * 数组元素在列表中的序号
	 */
	public static <T> int[] indexOf(List<T> list, Object... array){
		if (list == null){
			throw new IllegalArgumentException("list can not be null");
		}
		final int len = array.length;
		int[] indexes = new int[len];
		for (int i=0; i<len; i++){
			indexes[i] = list.indexOf(array[i]);
		}
		return indexes;
	}
	
	/**
	 * 将集合拼接为字符串
	 */
	public static String join(Collection<?> collection, String separator){
		if (collection==null || collection.size()==0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Object elt : collection){
			sb.append(elt).append(separator);
		}
		return sb.deleteCharAt(sb.length()-separator.length()).toString();
	}
		
    public static <K, V> Map<K, V> toMap(Object... pairs) {
	    Map<K, V> ret = new HashMap<K, V>();
	    if (pairs == null || pairs.length == 0) {
	    	return ret;
	    }
	
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }        
        final int len = pairs.length;
        for (int i = 0; i < len; i++) {
            ret.put((K)pairs[i], (V)pairs[++i]);
        }
	    return ret;
	}
	
    /**
     * List转化为数组
     * 如果 list.isEmpty，返回null
     */
	public static <T> T[] array(List<T> list){
		final int size = (list==null) ? 0 : list.size();
		if (size == 0){
			return null;
		}
		Class<?> entityClass = list.get(0).getClass();
		T[] array = (T[])Array.newInstance(entityClass, size);
		list.toArray(array);
		return array;
	}
	
	/**
	 * 将可变参数转为元素数组
	 */
	public static <T> T[] extractArgs(Class<T> clazz, T... args) {
		T[] examples = (T[])Array.newInstance(clazz, 0);
		if (args == null) {
			return examples;
		}
		List<T> sources = new ArrayList<T>(4);
		if (args.length == 1) {
			Object values = args[0];
			if (values.getClass().isArray()) {
				for (T elt : (T[])values) {
					sources.add(elt);
				}
			} else {
				sources.add(args[0]);
			}
		} else {  //0 or more than 1
			for (T elt : args) {
				sources.add(elt);
			}
		}
		
		return sources.toArray(examples);
	}
	
	public static String outAsString(Object[] array) {
		if (array == null) {
			return "null";
		} 
		StringBuilder buffer = new StringBuilder("[");
		for (Object elt : array) {
			buffer.append(elt).append(", ");
		}
		return buffer.length() < 2 ? buffer.append(']').toString()
					: buffer.toString().replaceAll(", $", "]");
	}

}
