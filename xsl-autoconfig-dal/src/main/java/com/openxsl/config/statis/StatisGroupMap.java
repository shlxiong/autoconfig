package com.openxsl.config.statis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.openxsl.config.util.BeanUtils;

/**
 * 做groupby的聚合Map
 * @author shuilin.xiong
 *
 */
@SuppressWarnings("serial")
public class StatisGroupMap extends JSONObject {//HashMap<String, Object>{
	private final static Logger logger = LoggerFactory.getLogger(StatisGroupMap.class);
	private transient String id = "";
	
	public StatisGroupMap(Object bean) {
		String json = JSON.toJSONString(bean);
		this.putAll(JSON.parseObject(json));
		try {
			this.id = (String)BeanUtils.findDeclaredMethod(bean.getClass(), "groupId").invoke(bean);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public static <T> List<StatisGroupMap> groupby(List<T> results, 
						BiConsumer<StatisGroupMap,StatisGroupMap> mergeFunction){
		final int TOTAL = results==null ? 0 : results.size();
		if (TOTAL == 0) {
			return Collections.emptyList();
		}
		List<StatisGroupMap> groupList = new ArrayList<StatisGroupMap>(TOTAL);
		StatisGroupMap statis, previous = new StatisGroupMap(results.get(0));
		for (int i=1; i<TOTAL; i++) {
			statis = new StatisGroupMap(results.get(i));
			if (previous.similarWith(statis)) {
				mergeFunction.accept(previous, statis);
			} else {
				groupList.add(previous);
				previous = statis;
			}
		}
		if (!groupList.contains(previous)) {
			groupList.add(previous);
		}
		return groupList;
	}
	
	public static <T> List<T> groupBy(List<T> results, BiConsumer<T,T> mergeFunction){
		final int TOTAL = results==null ? 0 : results.size();
		if (TOTAL == 0) {
			return Collections.emptyList();
		}
		List<T> groupList = new ArrayList<T>(TOTAL);
		T statis, previous = results.get(0);
		for (int i=1; i<TOTAL; i++) {
			statis = results.get(i);
			if (StatisGroupMap.similarWith(previous, statis)) {
				mergeFunction.accept(previous, statis);
			} else {
				groupList.add(previous);
				previous = statis;
			}
		}
		if (!groupList.contains(previous)) {
			groupList.add(previous);
		}
		return groupList;
	}
	
	/**
	 * 根据关键字做分组
	 * @param results 源数组
	 * @param funcGroup 产生关键字的方法
	 * @param mergeFunction 合并相同对象的方法
	 */
	public static <T> List<T> groupBy(List<T> results, Function<T,Object> funcGroup,
						BiConsumer<T,T> mergeFunction){
		final int TOTAL = results==null ? 0 : results.size();
		if (TOTAL == 0) {
			return Collections.emptyList();
		}
		List<T> groupList = new ArrayList<T>(TOTAL);
		T statis, previous = results.get(0);
		for (int i=1; i<TOTAL; i++) {
			statis = results.get(i);
			if (funcGroup.apply(previous).equals(funcGroup.apply(statis))) {
				mergeFunction.accept(previous, statis);
			} else {
				groupList.add(previous);
				previous = statis;
			}
		}
		if (!groupList.contains(previous)) {
			groupList.add(previous);
		}
		return groupList;
	}
	
	/**
	 * 排序
	 * @param results 源数组
	 * @param funcGroup 排序方法
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> void orderBy(List<T> results, Function<T,Comparable> funcGroup) {
		if (results==null || results.size() < 1) {
			return;
		}
		Collections.sort(results, (u,v) -> funcGroup.apply(u).compareTo(funcGroup.apply(v)));
	}
	
	
	/**
	 * 更换了 groupBy属性，需要重新排序
	 */
	public static <T> void orderBy(List<T> results) {
		if (results==null || results.size() < 1) {
			return;
		}
		Class<?> clazz = results.get(0).getClass();
		Collections.sort(results, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				try {
					String id1 = (String)BeanUtils.findDeclaredMethod(clazz, "groupId").invoke(o1);
					String id2 = (String)BeanUtils.findDeclaredMethod(clazz, "groupId").invoke(o2);
					return id1.compareTo(id2);
				} catch (Exception e) {
					return 0;
				}
			}
		});
	}
	
	public boolean similarWith(StatisGroupMap other) {
		return id.equals(other.getId());
	}
	private static <T> boolean similarWith(T one, T other) {
		if (one==null && other==null)   return true;
		else if (one==null || other == null)   return false;
		try {
			Class<?> clazz = one.getClass();
			Object id1 = BeanUtils.findDeclaredMethod(clazz, "groupId").invoke(one);
			Object id2 = BeanUtils.findDeclaredMethod(clazz, "groupId").invoke(other);
			if (id1 == null) {
				return id2 == null;
			} else {
				return id2!=null && id1.equals(id2);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return false;
	}
	
	/**
	 * 某个数字属性相加
	 */
	public void sum(StatisGroupMap other, String attribute) {
		Number v1 = (Number)this.get(attribute);
		Number v2 = (Number)other.get(attribute);
		if (v2 != null) {
			if (v1 == null) {
				this.put(attribute, v2);
			} else {
				if (v1 instanceof Long || v1 instanceof Integer) {
					v1 = v1.longValue() + v2.longValue();
				} else {
					v1 = v1.doubleValue() + v2.doubleValue();
				}
				this.put(attribute, v1);
			}
		}
	}
	/**
	 * 求属性的较大值
	 */
	public void max(StatisGroupMap other, String attribute) {
		Number v1 = (Number)this.get(attribute);
		Number v2 = (Number)other.get(attribute);
		if (v2 != null) {
			if (v1 == null) {
				this.put(attribute, v2);
			} else {
				if (v1 instanceof Long || v1 instanceof Integer) {
					v1 = Math.max(v1.longValue(), v2.longValue());
				} else {
					v1 = Math.max(v1.longValue(), v2.longValue());
				}
				this.put(attribute, v1);
			}
		}
	}
	/**
	 * 求某一个group的属性值不为null的记录数（groupBy方法中遍历）
	 * @param attribute
	 */
	public void count(StatisGroupMap other, String attribute) {
		Number v1 = (Number)this.get(attribute);
		Number v2 = (Number)other.get(attribute);
		if (v2 != null) {
			if (v1 == null) {
				this.put(attribute, 1);
			} else {
				this.put(attribute, v1.intValue() + 1);
			}
		}
	}
	/**
	 * 记录数加1（groupBy方法中遍历）
	 * @param attribute
	 */
	public void count2(String attribute) {
		Number v1 = (Number)this.get(attribute);
		if (v1 == null) {
			this.put(attribute, 2);
		} else {
			this.put(attribute, v1.intValue()+1);
		}
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
