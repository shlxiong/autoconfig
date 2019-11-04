package com.openxsl.config.util.common;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openxsl.config.util.Patterns;

@SuppressWarnings("unchecked")
public class XmlUtils {
	public static final String PATH_SEPARATOR = "/";
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final Logger logger = LoggerFactory.getLogger(XmlUtils.class);
	
	public static Document getDocument(String xml){
		try{
			return DocumentHelper.parseText(xml);
		}catch(DocumentException e){
			logger.error("{} error:", xml,e);
			return null;
		}
	}
	
	public static Element getRoot(String xml){
		try{
			return getDocument(xml).getRootElement();
		}catch(Exception e){
			return null;
		}
	}
	
	public static Element getRoot(java.io.File file){
		SAXReader reader = new SAXReader();
		try {
			return reader.read(file).getRootElement();
		} catch (DocumentException e) {
			logger.error("", e);
			return null;
		}
	}
	public static Element getRoot(java.io.InputStream is){
		SAXReader reader = new SAXReader();
		try {
			return reader.read(is).getRootElement();
		} catch (DocumentException e) {
			logger.error("", e);
			return null;
		}
	}
	
	/**
	 * 返回根节点的子节点数目
	 * @param xml
	 * @return
	 */
	public static int childs(String xml){
		try{
			return getRoot(xml).elements().size();
		}catch(Exception e){
			return 0;
		}
	}
	
	public static List<Element> getChildren(Element node, String name){
		if (name==null || name.equals("*")){
			return node.elements();
		}else{
			return node.elements(name);
		}
	}
	
	/**
	 * 指定名字下的第n个子节点
	 */
	public static Element getChild(Element node, String name, int idx) throws XmlException{
		List<Element> children = getChildren(node, name);
		try{
			if (children.isEmpty()){
				throw new XmlException(node.getName()+" has no children: "+name);
			}else{
				try{
					return children.get(idx);
				}catch(IndexOutOfBoundsException iob){
					throw new XmlException("index must between [0,"+(children.size()-1)+"]");
				}
			}
		}finally{
			children = null;
		}
	}
	
	/**
	 * 获得节点的路径，如果路径中某个节点名称有多个兄弟节点，则标出下标
	 * @param node
	 * @return
	 */
	public static String getPath(Element node){
		if (node.isRootElement()){  //root
			return node.getPath();
		}
		Element parent = node.getParent();
		String parentPath = getPath(parent) + PATH_SEPARATOR;
		List<Element> siblings = parent.elements(node.getName());
		int size = siblings.size();
		if (size == 1){
			return parentPath + node.getName();
		}else{
			int i = 0;
			//final int len = String.valueOf(size).length();
			for (Element elt : siblings){
				if (elt.equals(node)){
					String idx = String.valueOf(i); //StringUtils.toFixedSizeStr(i,len)
					return parentPath + node.getName()
							+ "[i]".replace("i", idx);
				}
				i++;
			}
		}
		return parentPath;
	}
	
	/**
	 * 得到指定路径的下级子孙节点
	 */
	public static List<Element> getGrandChildren(Element node, String xpath)
					throws XmlException{
		int idx = xpath.lastIndexOf(PATH_SEPARATOR);
		String lastName = xpath.substring(idx+1);
		Matcher matcher = Patterns.ARRAY.matcher(lastName);
		if (matcher.matches()){
			lastName = matcher.group(1);
		}
		if (idx > 0){
			xpath = xpath.substring(0, idx);
			Element child = node;
			for (String name : xpath.split(PATH_SEPARATOR)){
				if (name.length() == 0) {
					continue;
				}
				matcher = Patterns.ARRAY.matcher(name);
				if (matcher.matches()){ //name[0]
					child = getChild(child, matcher.group(1), 
									 Integer.parseInt(matcher.group(2)) );
				}else{
					child = child.element(name);
				}
			}
			return child.elements(lastName);
		}else{
			return node.elements(lastName);
		}
	}
	
	public static boolean hasChild(Element node){
		return node.elements().size() > 0;
	}
	public static boolean isNullNode(Element node){
		return !node.hasContent() && node.attributes().isEmpty();
	}
	
	/**
	 * 将节点下所有内容转换成java.util.Map数据
	 */
	public static Map<String,String> node2Map(Element node){
		Map<String,String> resultMap = new TreeMap<String,String>();
		List<Element> children = node.elements();
		for (Element child : children){
			String xpath = getPath(child);
			if (hasChild(child)){
				resultMap.putAll(node2Map(child));
			}else if (node.hasContent()){
				resultMap.put(xpath, child.getTextTrim());
			}
			for (Attribute attr : (List<Attribute>)child.attributes()){
				resultMap.put(xpath+"."+attr.getName(), attr.getValue());
			}
		}
		return resultMap;
	}
	
	public static <T> T xml2Object(String xml, Class<T> clazz) throws XmlException{
		try {
			javax.xml.transform.Source source = new StreamSource(new StringReader(xml));
			return (T)JAXBContext.newInstance(clazz).createUnmarshaller()
							.unmarshal(source);
		} catch (Exception e) {
			throw new XmlException(e);
		}
	}
	
	public static String object2Xml(Object object)throws XmlException{
		if (object == null) {
			return null;
		}
		StringWriter writer = new StringWriter();
		try {
			JAXBContext.newInstance(object.getClass()).createMarshaller()
					.marshal(object, writer);
			return writer.toString();
		} catch (JAXBException e) {
			throw new XmlException(e);
		}
	}
	
	public static String removeHeader(String xml){
		Matcher m = Patterns.XMLHEADER.matcher(xml);
		if (m.matches()){
			return m.group(3);
		}else{
			return xml;
		}
	}
	
	public static void main(String[] args) throws XmlException {
		String xml = "<parent>"
				+ "<level_1>aaaa</level_1>"
				+ "<level_1>"
				+ "  <level_2 id='1'>bbbb</level_2>"
				+ "  <level_2 id='2'>"
				+ "     <level_3>cccc</level_3><level_3>${value}</level_3>"
				+ "  </level_2>"
				+ "</level_1>"
				+ "</parent>";
		Element root = getRoot(xml);
		Element grandSon = getGrandChildren(root, "/level_1[1]/level_2").get(1);
		String path = getPath(grandSon);
		System.out.println("path:"+path);
		String relativePath = path.substring(root.getPath().length());
		relativePath = relativePath.substring(0, relativePath.lastIndexOf("["));
		System.out.println("relative:"+relativePath);
		Element node = getGrandChildren(root, relativePath).get(1);
		System.out.println(node.equals(grandSon));  //true
		Map<String,String> tempMap = node2Map(root);
		System.out.println(tempMap);
		
		String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Finance><Message id=\"XHH1921\"><DPRes id=\"DPRes\"><version>1.0.0</version>";
		System.out.println(XmlUtils.removeHeader(xml2));
		xml = "<Finance><Message id=\"XHH1921\"><DPRes id=\"DPRes\">"
			+ "<List><Record><name>x1</name><amount>1000.00</amount></Record><Record><name>x2</name><amount>2000.00</amount></Record></List>"
			+ "</DPRes></Message></Finance>";
		System.out.println(node2Map(getRoot(xml)));
	}
	
	@SuppressWarnings("serial")
	public static class XmlException extends IOException{
		private int code;
		
		public XmlException(){
			super();
		}
		
		public XmlException(String message){
			super(message);
		}
		
		public XmlException(Throwable ex){
			super(ex);
		}
		
		public XmlException(int code, String message){
			super(message);
			this.code = code;
		}
		
		public int getCode(){
			return code;
		}
		
	}

}
