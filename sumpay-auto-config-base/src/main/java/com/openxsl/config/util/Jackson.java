package com.openxsl.config.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * @author xiongsl
 * <pre>
 *   @JsonRootName
 *   @JsonIgnore - 不序列化
 *   @JsonInclude(Include.NON_EMPTY) - 属性不为空时序列化此字段
 *   @JsonProperty("user_name") - 指定序列化时的字段名
 *   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") - 格式化日期属性
 *   @JsonUnwrapped(prefix = "user_") - 提升复合成员的属性作为本类的属性，但需要添加前缀
 *   @JsonSerialize(using=JsondataFormatSerialize.class) - 自定义序列化和反序列化的操作
 *   @JsonNaming(PropertyNamingStrategy) - 属性命名规则，如：驼峰
 *   @JsonPropertyOrder({}) - 属性排序（指定排序或按字母顺序）
 * </pre>
 */
public class Jackson {
	//线程安全
	private static ObjectMapper jsonMapper = new ObjectMapper();
	private static XmlMapper xmlMapper = new XmlMapper();
	
	static {
		jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//				.disable(SerializationFeature.INDENT_OUTPUT)
//				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.disable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)     //name()
				.disable(SerializationFeature.WRAP_ROOT_VALUE)
				.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)   //对Map.key做排序
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	public static String toJSONString(Object object) {
		try {
			return jsonMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	public static void writeFile(File file, Object value) throws IOException{
		jsonMapper.writeValue(file, value);
	}
	
	public static <T> T parseObject(String jsonStr, Class<T> clazz) {
		try {
			return jsonMapper.readValue(jsonStr, clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static <T> Map<String,T> parseMap(String jsonStr) throws IOException{
//		Map map = jsonMapper.readValue(jsonStr, Map.class);
		try {
			return jsonMapper.readValue(jsonStr, new TypeReference<Map<String,T>>(){});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static <T> List<T> parseArray(String jsonStr) {
		try {
			return jsonMapper.readValue(jsonStr, new TypeReference<List<T>>(){});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static <T> T readFile(File file, Class<T> clazz) throws IOException{
		return jsonMapper.readValue(file, clazz);
	}
	
	public static String toXml(Object object) {
		try {
			return xmlMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	public static void toXmlFile(File file, Object value) throws IOException{
		xmlMapper.writeValue(file, value);
	}
	
	public static <T> T parseXml(String xml, Class<T> clazz) {
		try {
			return xmlMapper.readValue(xml, clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static <T> T readXmlFile(File file, Class<T> clazz) throws IOException{
		return xmlMapper.readValue(file, clazz);
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(Jackson.toJSONString(java.time.LocalDate.now()));
		String jsonStr = "{\"name\":\"xiongsl\", \"sex\":1, \"address\": {\"province\":\"ZJ\", \"city\":\"hangzhou\"}}";
//		System.out.println(JSON.parseObject(jsonStr).get("address"));
		System.out.println(parseMap(jsonStr));
		jsonStr = "{\"name\":\"xiongsl\", \"sex\":\"male\"}";
		System.out.println(parseMap(jsonStr));
		jsonStr = "[1,2,3]";
		List<Integer> items = parseArray(jsonStr);
		System.out.println(items);
		System.out.println(items.get(0).getClass());
	}

}
