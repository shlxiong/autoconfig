package com.openxsl.config.filedata.upload.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.openxsl.config.filedata.upload.dao.UploadDao;
import com.openxsl.config.filedata.upload.model.FileItem;
import com.openxsl.config.filedata.upload.model.UploadConfig;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.service.Refreshable;
import com.openxsl.config.util.BeanUtils;
import com.openxsl.config.util.StringUtils;

@Service
public class UploadRegistry implements Refreshable, ApplicationContextAware {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<String,UploadConfig> configMap = new HashMap<String,UploadConfig>();
	private final Map<String,UploadService> serviceMap = new HashMap<String,UploadService>();
	@Autowired
	private UploadDao dao;
	private ApplicationContext context;
	
	public UploadConfig getUploadConfig(String serviceId) {
		return configMap.get(serviceId);
	}
	
	public UploadService getUploadService(String serviceId) {
		return serviceMap.get(serviceId);
	}
	
	public List<FileItem> queryFileItems(String serviceId, String name, String dataId) {
		QueryMap<String> params = new QueryMap<String>(4);
		params.put("serviceId", serviceId);
		params.put("itemName", name);
		params.put("dataId", dataId);
		return dao.queryFileItems(params);
	}

	@PostConstruct
	@Override
	public boolean refresh() {
		configMap.clear();
		serviceMap.clear();
		for (UploadConfig config : dao.getAllUploadConfigs()) {
			configMap.put(config.getServiceId(), config);
			if (!StringUtils.isEmpty(config.getServiceClass())) {
				UploadService uploadService = null;
				try {
					uploadService = (UploadService)context.getBean(
							BeanUtils.forName(config.getServiceClass()));
				} catch(ClassCastException | ClassNotFoundException ce) {
					logger.error("get context-bean error: ", ce);
				} catch (BeansException be) {
					try {
						uploadService = BeanUtils.instantiate(
								config.getServiceClass(), UploadService.class);
					} catch(Exception ex) {
						logger.error("instantiate bean error: ", ex);
					}
				}
				serviceMap.put(config.getServiceId(), uploadService);
			}
		}
		return true;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

}
