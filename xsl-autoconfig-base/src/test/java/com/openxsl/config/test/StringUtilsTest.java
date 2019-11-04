package com.openxsl.config.test;

import java.util.HashMap;

import org.junit.Assert;

import com.openxsl.config.util.StringUtils;

import junit.framework.TestCase;

/**
 * @author xiongsl
 */
public class StringUtilsTest extends TestCase {
	
	public void test() {
		String[] array = {"a", "b", "c"};
		Assert.assertEquals("a,b,c", StringUtils.join(array));
		
		String str = "astr";
		Assert.assertEquals("Astr", StringUtils.getCapital(str));
		str = "a_str";
		Assert.assertEquals("aStr", StringUtils.splitToCamel(str, "_"));
		str = "aStr";
		Assert.assertEquals("a_str", StringUtils.camelToSplitName(str, "_"));
		
		str = "hello world";
		Assert.assertEquals("heorld", StringUtils.cutOff(str, 2, 5));
		Assert.assertEquals("hello world", StringUtils.cutOff(str, 11, 5));
		Assert.assertEquals("world", StringUtils.getTail(str, 5));
		
		int data = 12;
		Assert.assertEquals("0012", StringUtils.toFixedSizeStr(data, 4));
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("id", "1");
		map.put("age", "30");
		map.put("name", "jelly");
		Assert.assertEquals("age=30&id=1&name=jelly", StringUtils.toQueryString(map));
		
		try{
			Integer.parseInt("s");
		}catch(Exception e){
			str = StringUtils.getStackTrace(e);
			System.out.println(str);
			Throwable ex = StringUtils.getException(str);
			System.out.println(StringUtils.getStackTrace(ex));
		}
	}

}
