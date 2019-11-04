package com.openxsl.config.apollo;

import com.ctrip.framework.apollo.spring.annotation.SpringValueProcessor;
import com.openxsl.config.util.BeanUtils;

/**
 * Apollo中处理spring @Value 属性的类。
 * 继承过来，为了替换SpringValueRegistry对象
 * @author xiongsl
 * @Create 2019-05-08
 */
public class SpringValueBeanProcessor extends SpringValueProcessor {

	public SpringValueBeanProcessor() {
		try {
			BeanUtils.setPrivateField(this, "springValueRegistry", AutoConfigValueRegistry.getInstance());
		}catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	@Override
//	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
//		super.postProcessBeanFactory(beanFactory);
		//二方包依赖，暂时去掉
//		List<ConfigChangeListener> listeners;
//		for (String namespace : ApolloPropertyInitializer.getNamespaces()) {
//			Config config = ConfigService.getConfig(namespace);
//			listeners = (List)BeanUtils.getPrivateField(config, "m_listeners");
//			for (ConfigChangeListener changeListener : listeners) {
//				BeanUtils.setPrivateField(changeListener, "springValueRegistry",
//								AutoConfigValueRegistry.getInstance());
//			}
//		}
//	}

}
