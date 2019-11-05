package com.openxsl.config.tracing.service.zookeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.condition.ConditionalOnPresent;
import com.openxsl.tracing.registry.model.Registration;

/**
 * Dubbo注册，由于dubbo框架会自行注册，这里不需要实现接口的方法，只利用BaseZkRegistry.find()方法
 * @author xiongsl
 */
@ScanConfig("DubboRegistry")
@ConditionalOnPresent(classes="com.alibaba.dubbo.rpc.RpcContext")
public class DubboRegistryImpl extends BaseZkRegistry {

	@Override
	public Class<?> getContextBeanType() {
		return null;
	}

	@Override
	public Registration convertRegistra(Object bean) {
		return null;
	}

	@Override
	protected String getNamespace() {
		return "/dubbo";
	}
	
	//subscribe()
	protected void subscribeProviders(String service) {
		SERVICES_OF_CONSUME.put(service, new DubboProviderListener(service));
		template.subscribeChildChanges(service+"/providers", SERVICES_OF_CONSUME.get(service));
	}
	
	@Override
	protected void resetProviderMap(String service, List<String> providers) {
		Map<String,List<Registration>> map = new HashMap<String,List<Registration>>();
		for (String url : providers) {
			URL dubboUrl = URL.valueOf(URL.decode(url));
			String dubboKey = dubboUrl.getServiceKey();
			Registration entry = new Registration();
			entry.setApplication(dubboUrl.getParameter(Constants.APPLICATION_KEY));
			entry.setOwner(dubboUrl.getParameter("owner"));
			entry.setInstanceId(dubboUrl.getAddress());
			if (!map.containsKey(dubboKey)) {
				map.put(dubboKey, new ArrayList<Registration>(2));
			}
			map.get(dubboKey).add(entry);
		}
		PROVIDER_MAP.putAll(map);
		map.clear();
	}
	
	class DubboProviderListener extends ProviderListener{

		public DubboProviderListener(String service) {
			super(service);
		}
		
		@Override
		protected void resetProviderMap(String service, List<String> children) {
			DubboRegistryImpl.this.resetProviderMap(service, children);
		}
		
	}

}
