package com.openxsl.admin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import com.openxsl.admin.api.IAccessDecision;
import com.openxsl.admin.api.IRestrictedSource;
import com.openxsl.admin.api.IUser;
import com.openxsl.admin.dao.ResourceDao;
import com.openxsl.admin.dao.RoleResourceDao;
import com.openxsl.admin.entity.OperationLog;
import com.openxsl.admin.entity.Resource;
import com.openxsl.admin.entity.joint.RoleResource;

@Service
public class AccessDecisionService implements IAccessDecision {
	// <url, Resource>
	private final Map<String,IRestrictedSource> resourceMap = new HashMap<String,IRestrictedSource>();
	private final AntPathMatcher matcher = new AntPathMatcher();
	@Autowired
	private ResourceDao resourceDao;
	@Autowired
	private RoleResourceDao roleResourceDao;
	
	@PostConstruct
	public void refresh() {
		List<Resource> resources = resourceDao.queryRestricted(null);
		synchronized (resourceMap) {
			Map<Integer, List<String>> resourceRoles = new HashMap<Integer, List<String>>();
			for (RoleResource roleSource : roleResourceDao.queryAll()) {
				int roleId = roleSource.getRoleId();
				int resourceId = roleSource.getResourceId();
				resourceRoles.putIfAbsent(resourceId, new ArrayList<String>());
				resourceRoles.get(resourceId).add(String.valueOf(roleId));
			}
			
			resourceMap.clear();
			for (Resource resource : resources) {
				int resourceId = resource.getId();
				resource.setRoles(resourceRoles.get(resourceId));
				resourceMap.put(resource.getUrl(), resource);   //TODO URL不唯一？
			}
		}
		resources = null;
	}
	
	@Override
	public boolean accessable(IUser user, IRestrictedSource resource) {
		List<String> roles = resource.getAuthorities();
		if (roles.isEmpty()) {
			return true;
		} else {
			for (String roleId : roles) {
				if (user.hasRole(roleId)) {
					return true;
				}
			}
			return false;
		}
	}
	
	@Override
	public IRestrictedSource findResourceByUrl(String url) {
		IRestrictedSource resource = resourceMap.get(url);
		if (resource == null) {
			for (Map.Entry<String,IRestrictedSource> entry : resourceMap.entrySet()) {
				String key = entry.getKey();
				if (matcher.isPattern(key) && matcher.match(key, url)) {
					return entry.getValue();
				}
			}
		}
		return resource;
	}
	
	@Override
	public int accessLog(OperationLog operation) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
		return 0;
	}

}
