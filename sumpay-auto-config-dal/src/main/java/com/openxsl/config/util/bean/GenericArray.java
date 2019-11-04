package com.openxsl.config.util.bean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.Assert;

/**
 * 同时支持Array和List操作
 * @author 001327
 */
//Jaxb还有问题
@XmlRootElement(name="list")
@XmlAccessorType(XmlAccessType.FIELD)
public class GenericArray<T> {
	@XmlElement(name="element")
	private List<T> components;
	private transient Class<?> type;
	
	public GenericArray(){}
	public GenericArray(Object arrays){
		this.set(arrays);
	}
	
	@SuppressWarnings("unchecked")
	public GenericArray<T> set(Object arrays){
		if (arrays instanceof List){
			components = (List<T>)arrays;
			if (components.size() > 0){
				type = components.get(0).getClass();
			}
		}else{
			Assert.isTrue(arrays.getClass().isArray(), "不是数组类型");
			type = arrays.getClass().getComponentType();
			int size = Array.getLength(arrays);
			components = new ArrayList<T>(size);
			for (int i=0; i<size; i++){
				components.add((T)Array.get(arrays, i));
			}
		}
		return this;
	}
	
	public Collection<T> list(){
		return components;
	}
	public int size(){
		return components.size();
	}
	public T get(int idx){
		return components.get(idx);
	}
	public Class<?> componentType(){
		return type;
	}
	
	public Collection<T> getComponents(){
		return components;
	}
}
