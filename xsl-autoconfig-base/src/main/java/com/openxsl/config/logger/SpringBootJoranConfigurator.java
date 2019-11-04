package com.openxsl.config.logger;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;

/**
 * 抄来的，用于解析spring自定义的标签
 * @author xiongsl
 */
public class SpringBootJoranConfigurator extends JoranConfigurator {
	private final Environment environment;
	
	public SpringBootJoranConfigurator(Environment environment) {
		Assert.notNull(environment, "'environment' must not be null");
		this.environment = environment;
	}
	
	/**
	 * 解析自定义的节点
	 */
	@Override
	public void addInstanceRules(RuleStore rs) {
		super.addInstanceRules(rs);
		rs.addRule(new ElementSelector("configuration/springProperty"),
					new SpringPropertyAction(environment));
		rs.addRule(new ElementSelector("*/springProfile"),
					new SpringProfileAction(environment));
		rs.addRule(new ElementSelector("*/springProfile/*"), new NOPAction());
	}

}
