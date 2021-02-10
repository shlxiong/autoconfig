package com.openxsl.admin.context;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.openxsl.config.util.BeanUtils;

@ConditionalOnClass(name={"com.github.pagehelper.PageHelper"})
@Component
public class MybatisEntityAutoConfigure implements ApplicationListener<ContextRefreshedEvent> {
	@Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;
	@Autowired
	private MybatisEntityInterceptor interceptor;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Configuration configuration;
		for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
			configuration = sqlSessionFactory.getConfiguration();
			List<Interceptor> interceptors = new ArrayList<Interceptor>(configuration.getInterceptors());
			//放到最后去
			interceptors.remove(interceptor);
			interceptors.add(interceptor);
			InterceptorChain chain = BeanUtils.getPrivateField(configuration, "interceptorChain");
			BeanUtils.setPrivateField(chain, "interceptors", interceptors);
		}
	}

}
