package com.openxsl.admin.api;

import org.springframework.security.access.AccessDecisionVoter;

import com.openxsl.admin.entity.OperationLog;

/**
 * 访问控制接口
 * @author shuilin.xiong
 */
public interface IAccessDecision extends AccessDecisionVoter<Object> {
	
	public boolean accessible(IUser user, IRestrictedSource resource);
	
	public IRestrictedSource findResourceByUrl(String url);
	
	public int accessLog(OperationLog operation);

}
