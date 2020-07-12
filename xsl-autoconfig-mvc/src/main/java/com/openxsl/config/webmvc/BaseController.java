package com.openxsl.config.webmvc;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.openxsl.config.Environment;
import com.openxsl.config.dal.RestInvoker;
import com.openxsl.config.dal.jdbc.Entity;
import com.openxsl.config.service.BaseService;
import com.openxsl.config.service.Refreshable;
import com.openxsl.config.util.KvPair;
import com.openxsl.config.util.NetworkUtils;

/**
 * 控制器的基类，具有 /get,/save,/delete,/refresh功能
 * @author xiongsl
 *
 * @param <S>
 * @param <T>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BaseController<S extends BaseService, T extends Entity> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final String SUCC_MSG = "{\"code\": 0, \"message\": \"操作成功\"}";
	protected final String FAIL_MSG = "{\"code\": -1, \"message\": \"操作失败\"}";
	
	@Autowired
	protected S service;
	@Autowired
    protected HttpServletRequest request;
	
	@RequestMapping(path = "{id}")
    @ResponseBody
	public T get(@PathVariable long id) {
		return (T)service.get(id);
	}
	
	@RequestMapping(path = "save",  method = RequestMethod.POST)
    @ResponseBody
    public Response save(T entity) {
		if (entity.getId() == null) {
			service.insert(entity);
		} else {
			service.update(entity);
		}
        return Response.SUCCESS;
    }

    @RequestMapping(path = "delete", method = RequestMethod.DELETE)
    @ResponseBody
    public Response delete(@RequestParam("ids") Serializable[] ids) {
    	service.delete(ids);
        return Response.SUCCESS;
    }
    
	@Autowired
	protected Refreshable refreshService;
	@Autowired
	private RestInvoker restInvoker;
	private String contextPath = Environment.getProperty("server.context-path", "");
	
	/**
	 * 刷新集群所有机器的内存数据
	 * @return
	 */
	@RequestMapping(path = "refresh")
    @ResponseBody
    public boolean refresh(){
		boolean flag = this.refreshLoacle();
		if (flag){
			String hosts = Environment.getProperty("server.hosts", "");
			for (String host : hosts.split(",")) {  //cluster
				if (!isLocalhost(host)) {
					this.refreshHostCache(host);   
				}
			}
		}
    	return flag;
    }
	
	@RequestMapping(path = "refreshCache.do", params = {"synchr=refresh"})
    @ResponseBody
	public boolean refreshLoacle() {
		try {
			logger.info("refresh local...");
			refreshService.refresh();
		} catch(Throwable t) {
			logger.error("刷新[localhost]内存失败：", t);
			return false;
		}
		return true;
	}
	
	protected boolean refreshHostCache(String hostIp) {
		try {
			String url = String.format("http://%s/%s/refreshCache.do",
								hostIp,contextPath);   //SmsManagController
			logger.info("refresh other-host: {}", url);
			restInvoker.postForm(url, new KvPair("synchr", "refresh"));
			return true;
		} catch(Throwable t) {
			logger.error("刷新[{}]内存失败：", hostIp, t);
			return false;
		}
	}
	
	private final Pattern pattern = Pattern.compile("(127.0.0.1|localhost)(:\\d+)?");
	private boolean isLocalhost(String hostPort) {
		return pattern.matcher(hostPort).find() 
				|| NetworkUtils.LOCAL_IP.equals(hostPort.split(":")[0]);
	}

}
