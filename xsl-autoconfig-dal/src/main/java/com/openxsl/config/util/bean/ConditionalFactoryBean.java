package com.openxsl.config.util.bean;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import com.openxsl.config.util.expr.AbstractExpression;
import com.openxsl.config.util.expr.impl.EqualExpression;
import com.openxsl.config.util.expr.impl.NotEqualExpression;
import com.openxsl.config.util.expr.impl.NotNullExpression;

/**
 * 选择性地创建Bean <pre>
 * 若使用System.properties中的变量，表达式格式：$key = v1
 * 若使用PropertyPlaceHolder中的变量，表达式格式：${key} = v1
 * 
 * <bean class="com.openxsl.config.dal.ConditionFactoryBean"
 *       p:beanType="ClassA" p:if="${key} = v1"
 *       p:defaultType="defClass"/>
 * 或
 * <bean class="com.openxsl.config.dal.ConditionFactoryBean"
 *       p:beanType="ClassA" p:if="${key} = v1"
 *       p:default-ref="bean"/>  或  p:defaultRef="bean"
 * 或
 * 
 * <bean id="testLimiter2" class="com.openxsl.config.dal.bean.ConditionalFactoryBean"
 *       p:defaultType="defClass">
 *	   <property name="switch" value="${database}" />
 *     <property name="cases">
 *	       <map>
 *			   <entry key="mysql" value="com.openxsl.config.dal.template.impl.MysqlPageLimiter" />
 *			   <entry key="oracle" value="com.openxsl.config.dal.template.impl.OraclePageLimiter" />
 *		   </map>
 *	   </property>
 * </bean>
 * 
 * </pre>
 * @author 001327
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class ConditionalFactoryBean<T> implements FactoryBean<T>, InitializingBean, ApplicationContextAware{//, BeanNameAware {
	private T bean;               //返回的对象
	private T defaultBean;
	/*满足If条件创建一个Bean*/
	private String expression;
	private Class<?> beanType;
	private String beanRef;
	/*默认的Bean*/
	private Class<?> defaultType;
	private String defaultRef;
	/*Case语句，根据变量值创建不同的Bean*/
	private String caseName;
	private Map<String, String> casesMap;
	
	private ApplicationContext context;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.initDefaultBean();
		Assert.notNull(expression==null && caseName==null && defaultBean==null,
				"没有指定环境变量或默认类型");
		
		AbstractExpression expr = this.getExpression();
		if (expr != null){
			if (expression != null){ 
				if (expr.evaluate(null)){
					if (beanRef != null){
						bean = (T)context.getBean(beanRef);
					}else if (beanType != null){
						bean = this.initSpringBean(beanType);
					}
				}
			}else {
				for (Map.Entry<String,String> entry : casesMap.entrySet()){
					expr.setOperant(entry.getKey());
					if (expr.evaluate(null)){
						Class<?> caseType = Class.forName(entry.getValue());
						bean = (T)this.initSpringBean(caseType);
						break;
					}
				}
			}
		}
		if (bean == null){
			bean = defaultBean;
		}
	}
	private void initDefaultBean(){
		if (defaultRef != null){
			defaultBean = (T)context.getBean(defaultRef);
		}else if (defaultType != null){
			defaultBean = this.initSpringBean(defaultType);
		}
	}
	
//	ExpressionParser expr = new SpelExpressionParser();  #{bean.prop}
//	Object value = expr.parseExpression(expression).getValue();
	private AbstractExpression getExpression(){
		if (expression != null){  //${springsec.authen.userRepo} = jdbc
			String[] temps = expression.split(" ");
			if (temps.length > 2){
				return temps[1].equals("=") ? new EqualExpression(temps[0], temps[2])
						: new NotEqualExpression(temps[0], temps[1], temps[2]);
			}else{
				return new NotNullExpression(temps[0]);
			}
		}else if (caseName != null){  //switch, "" is temporary
			return new EqualExpression(caseName, "");
		}else{
			return null;
		}
	}

	@Override
	public T getObject() throws Exception {
		return bean;
	}

	@Override
	public Class<?> getObjectType() {
		return bean==null ? null : bean.getClass();
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
	
	public void setIf(String expression){
		this.expression = expression;
	}
	public void setBeanType(String clazz) throws ClassNotFoundException{
		this.beanType = Class.forName(clazz);
	}
	public void setBeanRef(String beanRef){
		this.beanRef = beanRef;
	}
//	public void setBean(T bean){
//		this.bean = bean;
//	}
	
	public void setSwitch(String name){
		this.caseName = name;
	}
	public void setCases(Map<String,String> map){
		this.casesMap = map;
	}
	
	public void setDefaultType(String clazz) throws ClassNotFoundException{
		this.defaultType = Class.forName(clazz);
	}
	public void setDefault(T bean){
		this.defaultBean = bean;
	}
	public void setDefaultRef(String beanName){
		this.defaultRef = beanName;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
	}
	
//	private String beanName;
//	@Override
//	public void setBeanName(String name) {
//		this.beanName = name;
//	}
	
	private T initSpringBean(Class<?> clazz){
		T bean = (T)context.getAutowireCapableBeanFactory().createBean(clazz);
		try{
			Method initMethod = clazz.getDeclaredMethod("initiate");
			if (initMethod != null){
				initMethod.invoke(bean, new Object[]{});
			}
		}catch(NoSuchMethodException nme){
			//
		}catch(Exception e){
			e.printStackTrace();
		}
		return bean;
	}
	
}
