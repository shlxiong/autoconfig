package com.openxsl.config.dal.freemarker;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.cache.TemplateLoader;

public class StringTemplateLoader implements TemplateLoader {
	private Map<String,StringTemplateSource> templates =
					  new HashMap<String,StringTemplateSource>();

	public void putTemplate(String name, String source){
		templates.put(name, 
				new StringTemplateSource(source, System.currentTimeMillis()));
	}
	@Override
	public StringTemplateSource findTemplateSource(String name) throws IOException {
		return templates.get(name);
	}

	@Override
	public long getLastModified(Object templateSource) {
		return ((StringTemplateSource)templateSource).lastModified;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding)
				throws IOException {
		return new StringReader(((StringTemplateSource)templateSource).source);
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
		//每次parse完后都会执行
	}
	@Override
	public void finalize(){
		templates.clear();
	}
	
	public static void main(String[] args) {
		String expr = "<Finance><Message id=\"XHH${taskId}\"><OCReq id=\"OCReq\">\n"
				+ "<version>1.0.0</version><instId>XHH</instId><certId>${certId}</certId><date>${datetime}</date>\n"
				+ "<accountName>${accountName}</accountName><certType>${certType}</certType><certNo>@IDCARD(${certNo})</certNo>\n"
				+ "<Cstno>${p2pAcct}</Cstno><mobile>@MOBILE(${mobile})</mobile><extension>${remark}</extension>\n"
				+ "</OCReq></Message></Finance>";
		new StringTemplateLoader().new StringTemplateSource(expr, 1);
	}
	
	public class StringTemplateSource{
		private String source;
		private long lastModified;
		private List<String> expressions = new ArrayList<String>(4);
		private List<String> parameters = new ArrayList<String>();
		
		StringTemplateSource(String source, long lastModified) {
            this.lastModified = lastModified;
            
            String replaces, variant;
            int at = -1, left = 0, right;
            while ((at=source.indexOf("@", at+1)) != -1){ //@IDCARD(${name})
            	left = source.indexOf("(", at+1);
            	right = source.indexOf(")", at+1);
            	if (left>at && right>left){
            		replaces = source.substring(at, right+1);
            		variant = source.substring(left+1, right);
            		expressions.add(replaces.substring(1));
            		source = source.replace(replaces, variant);
            	}else{
            		at += 1;
            	}
            }
            
            String template = source;
			while ((left=template.indexOf("${")+2) != 1){
				right = template.indexOf("}", left);
				if (right == -1) {
					break;
				}
				String name = template.substring(left, right);
				parameters.add(name);
				template = template.substring(right+1);
			}
            
            this.source = source;
        }
		
//        public boolean equals(Object obj) {
//            if(obj instanceof StringTemplateSource) {
//                return name.equals(((StringTemplateSource)obj).name);
//            }
//            return false;
//        }
//        
//        public int hashCode() {
//            return name.hashCode();
//        }
		
		public List<String> getValidatorExprs(){
			return this.expressions;
		}
		public List<String> getParameters(){
			return this.parameters;
		}
	}

}
