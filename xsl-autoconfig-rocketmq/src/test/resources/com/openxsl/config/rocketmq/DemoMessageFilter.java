package com.openxsl.config.rocketmq;

import org.apache.rocketmq.common.filter.FilterContext;
import org.apache.rocketmq.common.filter.MessageFilter;
import org.apache.rocketmq.common.message.MessageExt;

public class DemoMessageFilter implements MessageFilter {

	@Override
	public boolean match(MessageExt msg, FilterContext context) {
//		context.getConsumerGroup()
		String keys = msg.getKeys();
		try {
		return keys!=null && keys.indexOf("Hello")!=-1;
		}finally {
			System.out.println("============match: "+(keys!=null && keys.indexOf("Hello")!=-1));
		}
	}

}
