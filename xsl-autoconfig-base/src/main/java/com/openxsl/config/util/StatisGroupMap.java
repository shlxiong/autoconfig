package com.openxsl.config.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 做groupby的聚合Map
 * @author shuilin.xiong
 */
@SuppressWarnings("serial")
public class StatisGroupMap extends JSONObject {//HashMap<String, Object>{
	private final Logger logger = LoggerFactory.getLogger(getClass());
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
	
	public boolean similarWith(StatisGroupMap other) {
		return id.equals(other.getId());
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
	 * 求某一个group的总记录数（groupBy方法中遍历）
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
