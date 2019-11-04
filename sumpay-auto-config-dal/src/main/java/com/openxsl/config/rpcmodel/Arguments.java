package com.openxsl.config.rpcmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import com.openxsl.config.config.NotEmpty;

/**
 * 参数对象
 * @author xiongsl
 */
@XmlType(name="Arguments", namespace="http://rpcmodel.config.openxsl.com/")
@XmlAccessorType(XmlAccessType.FIELD)   //XmlAccessType.PROPERTY
@XmlRootElement(name="Arguments")
@SuppressWarnings("serial")
public class Arguments extends SignatureBean {//extends LinkedMultiValueMap<String,Object> {
    /** 服务ID */
	@NotNull
    private String serviceId;
    /** 客户端设备指纹，如IP地址、MAC编号等 */
    private String hostId;
    /** 业务系统编码，与证书名字相同 */
    @NotNull
    private String bizSys;
    /** 业务数据的ID，比如订单号、流水号 */
    @NotNull
    private String dataId;
    /** 同步或异步调用，默认同步调用 */
    private boolean async = false;
    /** 请求时间 */
    private String timestamp = String.valueOf(System.currentTimeMillis());
    /** 接口版本 */
	private String version;
	
    /** 服务参数，请求时可以逐个列出（K=V），也可以json形式（params={K:V}） */
    @NotEmpty
    @XmlJavaTypeAdapter(MapAdapter.class)
    private Map<String, Object> params = new HashMap<String, Object>();
    /** 本系统的ID，在处理前为空 */
    private String taskId;

    public Arguments() {
    }
    public Arguments(String serviceId){
		this.setServiceId(serviceId);
	}

    public static Arguments valueOf(String jsonStr) {
        return JSON.parseObject(jsonStr, Arguments.class);
    }
//    public static Arguments valueOf(String jsonStr){
//        Arguments arguments = new Arguments();
//        arguments.params.putAll( MapAdapter.unmarshalMap(JSON.parseObject(jsonStr)) );
//        return arguments;
//    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public Map<String, Object> toMap() {
        return new HashMap<String, Object>(params);
    }
    
    public String toXml(){
    	final String placeHolder = "<key>${value}</key>";
    	StringBuilder buffer = new StringBuilder("<Arguments>\n");
    	for (Map.Entry<String,String> entry : headers().entrySet()){
    		buffer.append(placeHolder.replace("key", entry.getKey())
					  .replace("${value}", entry.getValue()));
    	}
    	buffer.append("<params>");
    	Object value;
    	for (Map.Entry<String,Object> entry : params.entrySet()){
    		value = entry.getValue();
    		if (value != null){
    			buffer.append(placeHolder.replace("key", entry.getKey())
  					  .replace("${value}", value.toString()));
    		}
    	}
    	buffer.append("</params>");
    	return buffer.append("\n</Arguments>").toString();
    }
    
    @JSONField(serialize=false)
    public Map<String, String> headers(){
    	Map<String, String> header = new HashMap<String, String>();
    	header.put("serviceId", serviceId);
    	header.put("hostId", hostId);
    	header.put("bizSys", bizSys);
		header.put("dataId", dataId);
    	header.put("async", String.valueOf(async));
    	header.put("version", version);
    	header.put("timestamp", timestamp);
    	return header;
    }

    /**
     * @LastModified 2016-08-26 从params中取明细数据
     */
    private List<String> details;
    @JSONField(serialize=false)
    @SuppressWarnings("unchecked")
    public List<String> getDetails(){
    	if (details == null){
    		Object temp = params.remove("details");
    		if (temp != null) {
	    		if (temp instanceof List) {
	    			details = (List<String>) temp;
	    		} else {
	    			details = JSON.parseArray(temp.toString(), String.class);
	    		}
    		}
    	}
    	if (details == null) {
    		details = new ArrayList<String>(0);
    	}
    	return details;
    }
    @SuppressWarnings("unchecked")
	public void setDetails(String name){
    	details = (List<String>)params.remove(name);
    }
    
    public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		if (params != null) {
			this.params = params;
		}
	}

	public Object get(String param){
		return params.get(param);
	}
	public void put(String key, Object value) {
		params.put(key, value);
    }
	public Object remove(String key) {
		return params.remove(key);
	}

    public void clear() {
    	params.clear();
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostIp(String hostId) {
        this.hostId = hostId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

	public String getBizSys() {
		return bizSys;
	}

	public void setBizSys(String bizSys) {
		this.bizSys = bizSys;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
