package com.openxsl.admin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

import com.openxsl.admin.entity.Resource;

@Service
public class RestrictedMetaSource implements FilterInvocationSecurityMetadataSource {
	private final Map<RequestMatcher, Collection<ConfigAttribute>> requestMap
				= new HashMap<RequestMatcher, Collection<ConfigAttribute>>();
//	@Autowired
//	private AuthorizeService author;
	
	@PostConstruct
	public void refreshRequestMap() {
        List<Resource> resourceList = new ArrayList<Resource>();
        for (Resource resource : resourceList) {
            RequestMatcher requestMatcher = new AntPathRequestMatcher(resource.getFuncUrl());
            List<ConfigAttribute> list = new ArrayList<ConfigAttribute>();
            list.add(new SecurityConfig(""));
            requestMap.put(requestMatcher, list);
        }
    }

	@Override
	public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
		final HttpServletRequest request = ((FilterInvocation) object).getRequest();
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap
                .entrySet()) {
            if (entry.getKey().matches(request)) {
                return entry.getValue();
            }
        }
		return null;
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		Set<ConfigAttribute> allAttributes = new HashSet<ConfigAttribute>();
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap
                .entrySet()) {
            allAttributes.addAll(entry.getValue());
        }
        return allAttributes;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return FilterInvocation.class.isAssignableFrom(clazz);
	}

}
