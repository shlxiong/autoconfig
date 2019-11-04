package com.openxsl.config.dal.freemarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.openxsl.config.dal.freemarker.validator.Validators;
import com.openxsl.config.util.common.XmlUtils;
import com.openxsl.config.util.MapUtils;
import com.openxsl.config.util.StringUtils;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 报文模板转换类，Request时通过传入参数生成Xml，Response时取回模板里面的参数值
 * @author 001327-xiongsl
 */
@Component
@ImportResource(locations="classpath*:spring/dal/dal-validators.xml")
public class TemplateTransfer {
	private final static String DETAILS = "details";
	private final static String LIST_PATH = "/List/";
	
	private Map<String, Template> paramTemplates = new HashMap<String, Template>();
	private Map<String, ResultTemplate> resultTemplates = new HashMap<String, ResultTemplate>();
	@Resource
	private Validators validators;
	
	/**
	 * 参数模板
	 */
	private StringTemplateLoader loader = new StringTemplateLoader();
	public void putTemplate(String serviceKey, String template) {
		if (template == null) {
			return;
		}
		if (!paramTemplates.containsKey(serviceKey)) {
			@SuppressWarnings("deprecation")
			Configuration cfg = new Configuration();
			cfg.setDefaultEncoding("UTF-8");
		 	cfg.setTemplateLoader(loader);
			loader.putTemplate(serviceKey, template);
			try {
				paramTemplates.put(serviceKey, cfg.getTemplate(serviceKey));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void clearTemplate(){
		paramTemplates.clear();
		try{
			loader.finalize();
		}catch(Exception e){
		}
	}

	private Pattern variantPattern = Pattern.compile("^Expression (.*) is undefined");
	public String getTemplateText(String serviceKey, Map<String,?> map) {
		try {
			Template template = paramTemplates.get(serviceKey);
			template.setNumberFormat("0.##");
			if (validators != null){
				this.validateParameters(serviceKey, map);
			}
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
		} catch(InvalidReferenceException ire){
			String message = (ire.getCause()==null) ? ire.getMessage()
					: ire.getCause().getMessage();
			Matcher matcher = variantPattern.matcher(message);
			if (matcher.find()){
				throw new IllegalArgumentException("转换模板出错，缺少参数："+matcher.group(1), ire);
			}else{
				throw new IllegalArgumentException("转换模板出错，可能参数没对应", ire);
			}
		}catch (TemplateException e) {
			throw new IllegalArgumentException("转换模板出错，可能参数没对应", e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	@SuppressWarnings("unchecked")
	public List<String> getTemplateParameters(String serviceKey){
		List<String> paramNames;
		try {
			paramNames = loader.findTemplateSource(serviceKey).getParameters();
		} catch (IOException e) {
			paramNames = Collections.EMPTY_LIST;
		}
		return paramNames;
	}
	/**
	 * 校验参数模板，如果有不正确的数据，将throw IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	private void validateParameters(String serviceKey, Map<String,?> argsMap){
		List<String> paramExprs;
		try {
			paramExprs = loader.findTemplateSource(serviceKey).getValidatorExprs();
		} catch (IOException e) {
			paramExprs = Collections.EMPTY_LIST;
		}
		List<String> exceptions = validators.validate(paramExprs, argsMap);
		if (exceptions.size() > 0){
			throw new IllegalArgumentException("参数校验错误：\n"+exceptions);
		}
	}
	
	
	/**
	 * 结果模板
	 */
	public void putResultTemplate(String serviceKey, String template, String jsonOrXml) {
		if (template == null) {
			return;
		}
		if (resultTemplates.get(serviceKey) == null) {
			resultTemplates.put(serviceKey, new ResultTemplate(template, jsonOrXml));
		}
	}
	public Map<String,String> getResultTemplate(String serviceKey){
		if (resultTemplates.containsKey(serviceKey)){
			return resultTemplates.get(serviceKey).getTemplate();
		}else{
			return null;
		}
	}
	public void clearResultTemplate(){
		resultTemplates.clear();
	}
	
	public Map<String,Object> getTempateResult(String serviceKey, Map<String,?> dataMap){
		Map<String,String> templateMap = resultTemplates.get(serviceKey).getTemplate();
		Map<String,Object> returnMap = new HashMap<String,Object>(templateMap.size());
		List<Map<String,Object>> detailsMap = null;
		String xpath;
		for (Map.Entry<String,String> entry : templateMap.entrySet()) {
			xpath = entry.getValue();
			if (xpath.indexOf(LIST_PATH) != -1){  //List/Record，And only one list
				detailsMap = this.getListResult(xpath, dataMap, entry.getKey());
				returnMap.put(DETAILS, detailsMap);
			}else if (dataMap.get(xpath) != null){
				returnMap.put(entry.getKey(), dataMap.remove(xpath));
			}
		}
		
		return returnMap;
	}
	/**
	 * 根据模板，取出变量值
	 * @return
	 */
	public Map<String,Object> getTempateResult(String serviceKey, String content, String jsonOrXml) {
		if (resultTemplates.get(serviceKey) == null) {
			Map<String,Object> resultMap = new HashMap<String,Object>(1);
			resultMap.put("value", content);
			return resultMap;
		}
		
		Map<String, ?> tempMap = "json".equals(jsonOrXml) ? MapUtils.flattenJson2Map(content,"")
								: XmlUtils.node2Map(XmlUtils.getRoot(content));
		return this.getTempateResult(serviceKey, tempMap);
	}
	private List<Map<String,Object>> getListResult(String xpath, 
						Map<String,?> valueMap, String attrName){
		// xpath=/Finance/Message/PTRRes/List/Record/attrName
		// valueMap.key=/Finance/Message/PTRRes/List/Record[8]/attrName
		int idx = xpath.indexOf(LIST_PATH) + LIST_PATH.length();
		final String record = xpath.substring(0, xpath.indexOf("/", idx));
		final String nodeName = xpath.substring(record.length()+1);
		final Pattern pattern = Pattern.compile(record+"\\[(\\d+)\\]/(\\S+)");
		List<Map<String,Object>> resultMaps = new ArrayList<Map<String,Object>>();
		Map<String, Object> map;
		for (Map.Entry<String,?> entry : valueMap.entrySet()){
			if (!entry.getKey().startsWith(record)) {
				continue;
			}
			int i = 0;
			Matcher matcher = pattern.matcher(entry.getKey());
			if (matcher.matches()){
				i = Integer.parseInt(matcher.group(1));
				if (!matcher.group(2).equals(nodeName)) {
					continue;
				}
			}else if (!entry.getKey().equals(xpath)) {
				continue;  //只有一条记录且key不相等
			}
			if (resultMaps.size() != i+1){
				map = new HashMap<String, Object>();
				resultMaps.add(map);
			}else{
				map = resultMaps.get(i);
			}
			map.put(attrName, entry.getValue());
		}
		return resultMaps;
	}
	
	class ResultTemplate{
		//<name, xpath>
		private Map<String, String> template;  
		
		public ResultTemplate(String template, String jsonOrXml) {
			Map<String,?> tempMap = "json".equals(jsonOrXml) ? MapUtils.flattenJson2Map(template,"")
							: XmlUtils.node2Map(XmlUtils.getRoot(template));
			this.template = new HashMap<String, String>();
			String placeHolder, key;
			for (Map.Entry<String,?> entry : tempMap.entrySet()){
				placeHolder = String.valueOf(entry.getValue());
				if (placeHolder.charAt(0)=='$' && placeHolder.endsWith("}")){
					placeHolder = placeHolder.substring(2, placeHolder.length()-1);
					key = entry.getKey();
					if (key.indexOf("[0]") != -1){ //json数组: List/user[0]/username
						key = StringUtils.cutOff(key, key.indexOf("[0]"), 3);
					}
					this.template.put(placeHolder, key);
				}
			}
			tempMap.clear();
		}
		
		public Map<String,String> getTemplate(){
			return template;
		}
		
	}
	
	public static void main(String[] args) {
		String json = "[11,22,33]";
		System.out.println(MapUtils.flattenJson2Map(json,""));
		json = "{'name':'x1', 'age':'10','address':{'province':'zhejiang','city':'hangzhou'}}";
		System.out.println(MapUtils.flattenJson2Map(json,""));
		
	}
	
}
