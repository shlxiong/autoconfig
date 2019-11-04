package com.openxsl.config.queue.impl;

import java.util.List;

import com.openxsl.config.queue.TaskQueue;
import com.openxsl.config.retry.Executable;

/**
 * @author xiongsl
 */
@SuppressWarnings("rawtypes")
public class JmsQueue implements TaskQueue {
	private String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object get() {
		// TODO Auto-generated method stub
//		javax.jms.QueueReceiver receiver;
//		receiver.receive();
		return null;
	}

	@Override
	public boolean put(Object obj) {
//		javax.jms.QueueSender sender;
//		sender.send();
		return false;
	}
	
	@Override
	public List<Object> getAll(){
		return null;
	}

	@Override
	public Executable getService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFair(boolean fair) {
		// TODO Auto-generated method stub
	}

}
