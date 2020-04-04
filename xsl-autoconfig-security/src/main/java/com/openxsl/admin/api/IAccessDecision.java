package com.openxsl.admin.api;

import com.openxsl.admin.entity.OperationLog;

/**
 * 访问控制接口
 * @author shuilin.xiong
 */
public interface IAccessDecision {
	
	public boolean accessable(IUser user, IRestrictedSource resource);
	
	public IRestrictedSource findResourceByUrl(String url);
	
	public int accessLog(OperationLog operation);

}
