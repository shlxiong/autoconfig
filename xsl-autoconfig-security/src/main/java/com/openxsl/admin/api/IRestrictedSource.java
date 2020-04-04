package com.openxsl.admin.api;

import java.util.List;

/**
 * 授权的资源
 * @author xiongsl
 */
public interface IRestrictedSource {   //extends SecurityMetadataSource {
	
	public String getUrl();
	
	public List<String> getAuthorities();

}
