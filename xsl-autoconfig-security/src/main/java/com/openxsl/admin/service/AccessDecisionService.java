package com.openxsl.admin.service;

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
import com.openxsl.admin.dao.OperationLogDao;
import com.openxsl.admin.entity.OperationLog;
import com.openxsl.admin.entity.Resource;

@Service
public class AccessDecisionService implements IAccessDecision {
	// <url, Resource>
	private final Map<String,IRestrictedSource> resourceMap = new HashMap<String,IRestrictedSource>();
	private final AntPathMatcher matcher = new AntPathMatcher();
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private OperationLogDao logDao;
	
	@PostConstruct
	public void refresh() {
		synchronized (resourceMap) {
			List<Resource> resources = resourceService.queryRestricted(null);
			resourceMap.clear();
			for (Resource resource : resources) {
				resourceMap.put(resource.getUrl(), resource);   //TODO URL不唯一？
			}
			resources = null;
		}
	}
	
	@Override
	public boolean accessible(IUser user, IRestrictedSource resource) {
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
		return logDao.insert(operation);
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
