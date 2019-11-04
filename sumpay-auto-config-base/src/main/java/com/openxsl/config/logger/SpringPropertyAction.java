package com.openxsl.config.logger;

import org.springframework.core.env.Environment;
import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * 取Spring Environment中的变量
 *    <springProperty name="" source="" defaultValue="" />
 *    
 * @author xiongsl
 */
class SpringPropertyAction extends Action {
	private static final String SOURCE_ATTRIBUTE = "source";
	private static final String DEFAULT_VALUE_ATTRIBUTE = "defaultValue";
	private final Environment environment;

	SpringPropertyAction(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void begin(InterpretationContext ic, String elementName, Attributes attributes)
			throws ActionException {
		String name = attributes.getValue(NAME_ATTRIBUTE);
		String source = attributes.getValue(SOURCE_ATTRIBUTE);
		Scope scope = ActionUtil.stringToScope(attributes.getValue(SCOPE_ATTRIBUTE));
		String defaultValue = attributes.getValue(DEFAULT_VALUE_ATTRIBUTE);
		if (OptionHelper.isEmpty(name) || OptionHelper.isEmpty(source)) {
			addError("The 'name' and 'source' attributes of <springProperty> must be set");
		}
		ActionUtil.setProperty(ic, name, getValue(source, defaultValue), scope);
	}

	private String getValue(String source, String defaultValue) {
		if (this.environment == null) {
			addWarn("No Spring Environment available to resolve " + source);
			return defaultValue;
		}
		String value = this.environment.getProperty(source);
		return value==null ? defaultValue : value;
//		if (value != null) {
//			return value;
//		}
//		int lastDot = source.lastIndexOf(".");
//		if (lastDot > 0) {
//			String prefix = source.substring(0, lastDot + 1);
//			RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(
//					this.environment, prefix);
//			return resolver.getProperty(source.substring(lastDot + 1), defaultValue);
//		}
//		return defaultValue;
	}

	@Override
	public void end(InterpretationContext ic, String name) throws ActionException {
	}

}
