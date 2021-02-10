package com.openxsl.admin.service;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openxsl.admin.dao.OperationLogDao;
import com.openxsl.admin.entity.OperationLog;
import com.openxsl.config.dal.jdbc.BaseService;
import com.openxsl.config.util.NetworkUtils;

@Service
public class OperationLogService extends BaseService<OperationLogDao, OperationLog, Long>{
	@Autowired
	private OperationLogDao logDao;
	
	public int saveLog(HttpServletRequest request, String uri, String userId) {
		OperationLog operation = getOperateInfo(request, uri);
		operation.setUserName(userId);
		return logDao.insert(operation);
	}
	
	public static OperationLog getOperateInfo(HttpServletRequest request, String uri) {
		//User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36
		String agent = request.getHeader("User-Agent");
		OperationLog operLog = new OperationLog();
		operLog.setOperSys(getClientOperSys(agent));
		operLog.setBrowser(getClientBrowser(agent));
		operLog.setHostIp(NetworkUtils.getRequestHost(request));
		String operation = '['+request.getMethod()+"]" + uri;
		operLog.setOperation(operation);
		operLog.setOperateDate(new Date());
		return operLog;
	}
	
	private static String getClientOperSys(String agent) {
		String operSys = "";
		if (agent.indexOf("Windows") > 0) {
			operSys = "Windows";
			int start = agent.indexOf("Windows NT ");  //(Windows NT 10.0; Win64; x64)
			if (start > 0) {
				int end = agent.indexOf(";", start);
				if (end == -1) {
					end = agent.indexOf(")", start);
				}
				try {
					start += "Windows NT ".length();
					float version = Float.parseFloat(agent.substring(start, end));
					if (version < 6) {
						operSys = "Windows XP";
					} else if (version > 6) {
						operSys = "Windows 10";
					} else {
						operSys = "Windows 7";
					}
				} catch (NumberFormatException ne) {}
			}
		} else if(agent.indexOf("iPhone") > 0) {
			operSys = "iPhone";
		} else if(agent.indexOf("Android")>0) {
			operSys = "Android";
		}
		return operSys;
	}
	private static String getClientBrowser(String agent) {
		String browser = "Internet Explorer";
		if (agent.indexOf("Firefox") > 0) {
			int idx = agent.indexOf("Firefox");
			browser = agent.substring(idx, agent.indexOf(".", idx));
		} else if (agent.indexOf("Chrome") > 0) {
			int idx = agent.indexOf("Chrome");
			browser = agent.substring(idx, agent.indexOf(".", idx));
		} else if (agent.indexOf("Trident") > 0) {
			int idx = agent.indexOf("Trident");
			browser = agent.substring(idx, agent.indexOf(".", idx));
		} else if (agent.indexOf("Opera") > 0) {
			int idx = agent.indexOf("Opera");
			browser = agent.substring(idx, agent.indexOf(".", idx));
		} else if(agent.indexOf("Safari") > 0) {
			int idx = agent.indexOf("Safari");
			browser = agent.substring(idx, agent.indexOf(".", idx));
		} else if (agent.indexOf("QQBrowser") > 0) {
			int idx = agent.indexOf("QQBrowser");
			browser = agent.substring(idx, agent.indexOf(".", idx));
		}
		return browser;
	}

}
