package com.openxsl.config.logger;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.InPlayListener;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.util.OptionHelper;

/**
 * 根据Spring Profile，是否include一个节点
 *    <springProfile name="dev">
 *    
 * @author xiongsl
 */
class SpringProfileAction extends Action implements InPlayListener {
	private final Environment environment;

	private int depth = 0;
	private boolean acceptsProfile;
	private List<SaxEvent> events;

	SpringProfileAction(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void begin(InterpretationContext ic, String name, Attributes attributes)
					throws ActionException {
		this.depth++;
		if (this.depth != 1) {
			return;
		}
		ic.pushObject(this);
		this.acceptsProfile = acceptsProfiles(ic, attributes);
		this.events = new ArrayList<SaxEvent>();
		ic.addInPlayListener(this);
	}

	private boolean acceptsProfiles(InterpretationContext ic, Attributes attributes) {
		String[] profileNames = StringUtils.trimArrayElements(StringUtils
				.commaDelimitedListToStringArray(attributes.getValue(NAME_ATTRIBUTE)));
		if (profileNames.length != 0) {
			for (String profileName : profileNames) {
				OptionHelper.substVars(profileName, ic, this.context);
			}
			return this.environment.acceptsProfiles(profileNames);
		}
		return false;
	}

	@Override
	public void end(InterpretationContext ic, String name) throws ActionException {
		this.depth--;
		if (this.depth != 0) {
			return;
		}
		ic.removeInPlayListener(this);
		verifyAndPop(ic);
		if (this.acceptsProfile) {
			addEventsToPlayer(ic);
		}
	}

	private void verifyAndPop(InterpretationContext ic) {
		Object o = ic.peekObject();
		Assert.state(o != null, "Unexpected null object on stack");
		Assert.isInstanceOf(SpringProfileAction.class, o, "logback stack error");
		Assert.state(o == this, "ProfileAction different than current one on stack");
		ic.popObject();
	}

	private void addEventsToPlayer(InterpretationContext ic) {
		Interpreter interpreter = ic.getJoranInterpreter();
		this.events.remove(0);
		this.events.remove(this.events.size() - 1);
		interpreter.getEventPlayer().addEventsDynamically(this.events, 1);
	}

	@Override
	public void inPlay(SaxEvent event) {
		this.events.add(event);
	}

}
