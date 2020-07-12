package com.openxsl.config.rpcmodel;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.openxsl.config.util.StringUtils;

/**
 * Rpc对象-返回结果
 * @author xiongsl
 */
@SuppressWarnings("serial")
@XmlType(name="Result", namespace="http://rpcmodel.config.openxsl.com/")
@XmlAccessorType(XmlAccessType.FIELD)  
@XmlRootElement(name="Result")
public class Result extends SignatureBean{
	/** 本系统的ID */
	@NotNull
	private String taskId;
	/** 返回值 */
	@NotNull
	@XmlJavaTypeAdapter(MapAdapter.class)
	private Map<String,Object> values = new HashMap<String,Object>(8);
	/** 代码，0表示成功，此时exception为空 */
	@NotNull
	private String code = "0";
	/** 提示信息 */
	private String message;
	/** 异常堆栈信息 */
	private String exception;

	private transient String bizSys;
	private transient String format = "json";
	
	public Result(){
	}
	public Result(String taskId){
		this.setTaskId(taskId);
	}
	public Result(String taskId, Map<String,Object> values){
		this(taskId);
		this.setValue(values);
	}
	public Result(String taskId, int code, Throwable e){
		this(taskId);
		this.setCode(String.valueOf(code));
		if (e != null){
	    	this.setException(StringUtils.getStackTrace(e));
	    	this.setMessage(this.getCause(e).getMessage());
		}
	}
	
	private Throwable getCause(Throwable ex) {
		if (ex.getCause() == null) {
			return ex;
		} else {
			Throwable cause = ex.getCause(); 
			while (cause.getCause() != null) {
	    		cause = cause.getCause();
	    	}
			return cause;
		}
	}
	
	public static Result valueOf(String json){
		return JSON.parseObject(json, Result.class);
	}
	public static Result fromMap(Map<String,Object> mapValue){
		Result result = new Result();
		result.setTaskId((String)mapValue.remove("taskId"));
		result.setMessage((String)mapValue.remove("message"));
		result.setException((String)mapValue.remove("exception"));
		//其余的值全部放在returnValue中
		result.setValue(mapValue);
		return result;
	}
	@Override
	public String toString(){
		if ("xml".equals(format)){
			StringWriter writer = new StringWriter();
			try {
				JAXBContext.newInstance(getClass()).createMarshaller()
							.marshal(this, writer);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
			return writer.toString();
		}else{
			return JSON.toJSONString(this);
		}
	}
	
	/**
     * @LastModified 2016-08-26 从params中取明细数据
     */
	@JSONField(serialize=false)
	public List<String> getDetails(){
		List<?> details = (List<?>)values.get("details");
		if (details != null){
			List<String> list = new ArrayList<String>(details.size());
			for (Object elt : details) {
				list.add(JSON.toJSONString(elt));
			}
			return list;
//			return JSON.parseArray(details, String.class);
		}else{
			return null;
		}
	}
	public String withoutDetails(){
		String json = JSON.toJSONString(this);
		Result other = JSON.parseObject(json, Result.class);
		other.getValue().remove("details");
		return other.toString();
	}
	
	public void putValue(String name, String value){
		values.put(name, value);
	}
	
	public boolean successful(){
		return exception != null;
	}

	@JSONField(ordinal=0)
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	@JSONField(ordinal=2)
	public Map<String,Object> getValue() {
		return values;
	}
	public void setValue(Map<String,Object> returnValue) {
		this.values = returnValue;
	}
	@JSONField(ordinal=3)
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@JSONField(ordinal=1)
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@JSONField(ordinal=4)
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	public String getBizSys() {
		return bizSys;
	}
	public void setBizSys(String bizSys) {
		this.bizSys = bizSys;
	}

}
