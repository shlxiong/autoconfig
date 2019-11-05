package com.openxsl.config.tracing.service.zookeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.dal.zookeeper.ZooKeeperTemplate;
import com.openxsl.config.dal.zookeeper.ZooKeeperTemplate.ZkChildListener;
import com.openxsl.config.tracing.service.RegisterServiceFactory;
import com.openxsl.config.tracing.service.RegistryService;
import com.openxsl.tracing.registry.model.Registration;

/**
 * Zookeeper注册服务（基类），针对每种protocol都有实现
 * @author xiongsl
 */
public abstract class BaseZkRegistry implements RegistryService {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final Map<String,List<Registration>> PROVIDER_MAP
					= new HashMap<String,List<Registration>>();
	protected final Map<String, ProviderListener> SERVICES_OF_CONSUME
					= new HashMap<String, ProviderListener>();
	@Autowired
	protected ZooKeeperTemplate template;
	
	@PostConstruct
	public void initiate() {
		template.setRootPath(this.getNamespace());
		RegisterServiceFactory.registerService(this.getNamespace(), this);
		this.initiateProviderMap();
	}

	@Override
	public void registerServer(Registration registration) {
		String serviceKey = registration.getServiceKey().serialize();
		String provider = registration.getServiceFeature().serialize();
		String subPath = String.format("%s/providers/%s", serviceKey,provider);
		template.create(subPath, "", true);
		this.subscribeProviders(serviceKey);   //同步PROVIDER_MAP
	}

	@Override
	public void registerClient(Registration registration) {
		String serviceKey = registration.getServiceKey().serialize();
		if (SERVICES_OF_CONSUME.containsKey(serviceKey)) {  //已经注册过
			return;
		}
		
		String provider = registration.getServiceFeature().serialize();
		String subPath = String.format("%s/consumers/%s", serviceKey,provider);
		template.create(subPath, "", true);
		this.subscribeProviders(serviceKey);
	}
	
	@Override
	public Registration find(String serviceKey) {
		if (!PROVIDER_MAP.containsKey(serviceKey)) {
			String providerUrl = String.format("%s/providers", serviceKey);
			List<String> children = template.getChildren(providerUrl);
			if (children.size() > 0) {
				this.resetProviderMap(serviceKey, children);
			}
		}
		try {
			return PROVIDER_MAP.get(serviceKey).get(0);
		}catch(Exception e) {
			return null;
		}
	}
	
	@Override
	public void subscribe(String serviceKey) {
		if (SERVICES_OF_CONSUME.get(serviceKey) != null) {  //已经注册过
			return;
		}
		this.subscribeProviders(serviceKey);
	}
	
	protected abstract String getNamespace();
	
	protected void subscribeProviders(String service) {
		SERVICES_OF_CONSUME.put(service, new ProviderListener(service));
		template.subscribeChildChanges(service+"/providers", SERVICES_OF_CONSUME.get(service));
	}
	
	protected void resetProviderMap(String service, List<String> providers) {
		if (providers.isEmpty()) {
			PROVIDER_MAP.remove(service);
		} else {
			PROVIDER_MAP.put(service, new ArrayList<Registration>(2));
			for (String jsonStr : providers) {
				Registration entry = JSON.parseObject(jsonStr, Registration.class);
				PROVIDER_MAP.get(service).add(entry);
			}
		}
	}
	
	private void initiateProviderMap() {
		for (String service : template.getChildren("/")) {
			this.resetProviderMap(service, template.getChildren(service+"/providers"));
		}
		logger.info("{} found providers: {}", getNamespace(), PROVIDER_MAP.size());
	}
	
	class ProviderListener implements ZkChildListener{
		private final String service;
		
		public ProviderListener(String service) {
			this.service = service;
		}

		@Override
		public void childChanged(String subPath, List<String> children) {
			logger.info("{} childChanged___________", subPath);
			this.resetProviderMap(service, children);
		}
		
		protected void resetProviderMap(String service, List<String> children) {
			BaseZkRegistry.this.resetProviderMap(service, children);
		}
		
	}
	
}
