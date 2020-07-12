package com.openxsl.config.util.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.dom4j.Element;
import org.springframework.util.Assert;

import com.openxsl.config.util.ReflectUtils;
import com.openxsl.config.util.bean.GenericArray;

public class XmlPersister extends XmlUtils {
	
	@SuppressWarnings("rawtypes")
	public static void store(Object data, OutputStream os) throws XmlException{
		if (data == null) {
			return;
		}
		if (ReflectUtils.isArray(data.getClass())){
			//TODO("XXClass nor any of its super class is known to this context.")
			GenericArray<?> array = new GenericArray(data);
			if (array.size() > 0){
//				createMarshaller(array.componentType());
				storeObject(array, os);
			}
		}else{
			//TODO if Map
			storeObject(data, os);  //POJO
		}
		try {
			os.flush();
		} catch (IOException e) {
			throw new XmlException(e);
		}
	}
	
	public static <T> List<T> load(InputStream is, Class<T> clazz, int start, int count) throws XmlException{
		Element root = getRoot(is);
		Assert.notNull(root, "Xml文件不存在或格式不对");
		int max = root.elements().size();  //nodeCount();
		if (start < 0) {
			start = 0;
		}
		if (count < 0) {
			count = Integer.MAX_VALUE;  // all left
		}
		Assert.isTrue(max>=start, "起始位置不对(start)");
		count = Math.min(count, max-start);
		
//		root.elementIterator();
		List<T> lstResult = new ArrayList<T>(count);
		for (int i=start,end=start+count; i<end; i++){
			String xml = getChild(root, null, i).asXML();
			lstResult.add(xml2Object(xml, clazz));
		}
		return lstResult;
	}
	public static <T> T find(InputStream is, Class<T> clazz, String key) throws XmlException{
		Element root = getRoot(is);
		Assert.notNull(root, "Xml文件不存在或格式不对");
		Element element = root.elementByID(key);
		if (element == null) {
			return null;
		}
		return xml2Object(element.asXML(), clazz);
	}
	
	private static void storeObject(Object object, OutputStream os) throws XmlException{
		if (object == null) {
			return;
		}
		try {
			//TODO 输出格式
			createMarshaller(object.getClass()).marshal(object, os);
		} catch (JAXBException e) {
			throw new XmlException(e);
		}
	}
	private final static Map<Class<?>, Marshaller> MARSHAL_MAP =
				new HashMap<Class<?>, Marshaller>();
	private static Marshaller createMarshaller(Class<?> clazz) throws JAXBException{
		if (!MARSHAL_MAP.containsKey(clazz)){
			MARSHAL_MAP.put(clazz, 
					JAXBContext.newInstance(clazz).createMarshaller());
		}
		return MARSHAL_MAP.get(clazz);
	}

}
