package com.openxsl.config.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;

import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.BasicTest;
import com.openxsl.config.util.PeriodicEntity;

@ContextConfiguration(
		classes={RedisProperties.class, RedisConfiguration.class},
		locations={"classpath*:testerror.xml"}
)
@TestPropertySource(
		properties={"spring.jdbc.autowired=false","spring.jdbc.persistence.api=none"}
)
@AutoConfig(application="springboot-test")
public class RedisHelperTest extends BasicTest {
	@Autowired
	private GenericRedisHelper<TestModel> genericHelper;
	@Autowired
	private GenericRedisHelper<String> genericHelper2;
	@Autowired
	private MapRedisHelper<Serializable> mapHelper;
	@Autowired
	private ListRedisHelper<String> listHelper;
	
	public static <T> List<T> asList(T... args){
		return new ArrayList<T>(Arrays.asList(args));
	}
	
//	@Test
	public void testObject() {
		genericHelper.setExpires(5);
		TestModel model = new TestModel();
		model.setUsername("ketty");
		model.setAge(20);
		model.setWorkDate(new java.sql.Date(100, 2, 30));
		genericHelper.setEntityClass(TestModel.class);
		final String key = "test-model-1";
		genericHelper.save(key, model);
		model.setUsername("xiongsl");
		genericHelper.save("test-model-2", model);
		
		genericHelper.save("id", 1L);
		System.out.printf("before: %s\n", genericHelper.getObject("id4incr", Long.class));
		System.out.printf("incre: %s\n", genericHelper.increaseOrDecr("id4incr", 2, 30));
		System.out.printf("after: %s\n", genericHelper.getObject("id4incr", Long.class));
		
		//模糊查询、批量查询
		System.out.printf("模糊查询：%s\n", genericHelper.selectLike("test-model*"));
		List<String> keys = asList("test-model-1", "test-model-2");
		System.out.println(genericHelper.get(keys));
		
		model = (TestModel)genericHelper.get(key);
		System.out.println(JSON.toJSON(model));
		Assert.notNull(model, "没有取到对象");
		
		genericHelper.delete(key);
		model = (TestModel)genericHelper.get(key);
		System.out.println("deleted-1: "+JSON.toJSON(model));
		Assert.isNull(model, "没有删除");
		
		genericHelper.deleteLike("test-model*");
		model = (TestModel)genericHelper.get("test-model-2");
		System.out.println("deleted-*: "+JSON.toJSON(model));
		System.out.println(genericHelper.selectLike("test-model*"));
		
		genericHelper.save(key, new TestModel());
		try {
			Thread.sleep(5000);  //等待过期
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		model = (TestModel)genericHelper.get(key);
		System.out.println("expired: "+model);
		Assert.isNull(model, "应该已经过期");
		
		genericHelper.save(key, new TestModel());
		genericHelper.get(key, 10);
		try {
			Thread.sleep(5000);  //等待过期
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		model = (TestModel)genericHelper.get(key);
		System.out.println("no-expired: "+model);
		Assert.notNull(model, "应该还没有过期");
		
		System.out.println(genericHelper);
		genericHelper2.setEntityClass(String.class);
//		System.out.println(genericHelper2);
		
		String key3 = "who";
		genericHelper2.save(key3, "1");
		String value = genericHelper2.getAndSet(key3, "2");
		System.out.printf("who(get)=%s\n", value);
		System.out.printf("who(set)=%s", genericHelper2.get(key3));
	}
	
//	@Test
	public void testMap() {
		Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("id", 1L);
		map.put("name", "ketty");
		String key = "mytest_map";
		mapHelper.putAll(key, map);
		TestModel model = new TestModel();
		model.setUsername("ketty");
		model.setAge(20);
		mapHelper.put(key, "user", model);
		map = mapHelper.get(key);
		Assert.notNull(map, "没有获得对象");
		System.out.println("======="+map);
		
		mapHelper.put(key, "name", "hello!");
		mapHelper.put(key, "age", 20);
		System.out.println(mapHelper.get(key));
		
		mapHelper.increaseOrDecr(key, "age", 5);
		Integer age = (Integer)mapHelper.get(key, "age");
		System.out.println("age="+age);
		Assert.isTrue(age==25, "没有增加");
		
		mapHelper.remove(key, "age");
		age = (Integer)mapHelper.get(key).get("age");
		Assert.isNull(age, "没有删除age");
		
		mapHelper.clear(key);
		Assert.isTrue(mapHelper.get(key).isEmpty(), "没有删掉");
	}
	
	@Test
	public void testList() {
		final String key = "mytest_list";
		listHelper.clear(key);
		List<String> list = asList("one", "three");
		listHelper.addAll(key, list);
		List<String> result = listHelper.get(key);
		System.out.printf("init: %s\n", result.toString());
		
		listHelper.add(key, "four");
		result = listHelper.get(key);
		System.out.printf("add: %s\n", result.toString());
		boolean flag = listHelper.addIfAbsent(key, "four");
		Assert.isTrue(!flag, "addIfAbsent 插入重复");
		
		listHelper.add(key, 1, "two");
		String elt = listHelper.get(key, 1);
		Assert.isTrue(elt.equals("two"), "插入位置错误");
		result = listHelper.get(key);
		System.out.printf("add(%d): %s\n", 1, result.toString());
		
		listHelper.remove(key, 1);
		result = listHelper.get(key);
		System.out.printf("remove(%d): %s\n", 1, result.toString());
		
		listHelper.clear(key);
		Assert.isTrue(mapHelper.get(key).isEmpty(), "没有删掉");
		
		//测试PeriodicEntity
		PeriodicEntity<List<String>> entities = new PeriodicEntity<List<String>>();
		entities.setEntity(list);
		entities.setTtl(5);
		listHelper.save(key, entities);
		System.out.printf("save list: %s\n", listHelper.get(key));
		try {
			Thread.sleep(6000);
		}catch(Exception e) {
		}
		System.out.printf("list: %s", listHelper.get(key));
		Assert.isTrue(listHelper.get(key).isEmpty(), "没有过期");
	}

}
