package com.openxsl.config.webmvc;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.openxsl.config.Environment;
import com.openxsl.config.dal.http.RestInvoker;
import com.openxsl.config.dal.jdbc.BaseService;
import com.openxsl.config.dal.jdbc.Entity;
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
public class BaseController<S extends BaseService, T extends Entity, PK extends Serializable> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final String SUCC_MSG = "{\"code\": 0, \"message\": \"操作成功\"}";
	protected final String FAIL_MSG = "{\"code\": -1, \"message\": \"操作失败\"}";
	
	@Autowired
	protected S service;
	@Autowired
    protected HttpServletRequest request;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) throws Exception {
//		binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, true));
		binder.registerCustomEditor(java.sql.Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
	}
	
	@GetMapping(path = "get/{id}")
    @ResponseBody
	public Response get(@PathVariable PK id) {
		return Response.build(() -> service.get(id));
	}
	
	@PostMapping(path = "save")
    @ResponseBody
    public Response save(@Valid T entity) {
		return Response.build(() -> {
			if (entity.getId() == null) {
				service.insert(entity);
			} else {
				service.update(entity);
			}
		});
    }
	
	protected void checkExists(T entity) {
	}

    @RequestMapping(path = "delete", method = RequestMethod.POST)
    @ResponseBody
    public Response delete(@RequestParam("ids") PK[] ids) {
    	return Response.build(() -> {
    		if (ids.length < 1) {
    			throw new IllegalArgumentException("没有提交任何数据");
    		}
    		if (service.delete(ids) < 1) {
    			throw new IllegalArgumentException("操作失败");
    		}
    	});
    }

	@Autowired
	protected Refreshable refreshService;
	@Autowired
	private RestInvoker restInvoker;
	private String contextPath = Environment.getProperty("server.context-path", "msg");
	
	/**
	 * 刷新集群所有机器的内存数据
	 * @return
	 */
	public boolean refreshCache() {
		try {
			logger.info("refresh local...");
			refreshService.refresh();    //local
		} catch(Throwable t) {
			logger.error("刷新[localhost]内存失败：", t);
			return false;
		}
		
		String hosts = Environment.getProperty("server.hosts", "");
		for (String host : hosts.split(",")) {  //cluster
			if (!isLocalhost(host)) {
				this.refreshHostCache(host);   
			}
		}
		return true;
	}
	
	protected boolean refreshHostCache(String hostIp) {
		try {
			String url = String.format("http://%s/%s/sms/manage/refreshCache.do",
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
