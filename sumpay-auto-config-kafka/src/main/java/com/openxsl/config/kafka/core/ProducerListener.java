package com.openxsl.config.kafka.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class ProducerListener implements ListenableFutureCallback<Object>{
	private final String topic;
	private final Logger logger;
	private boolean success;
	
	public ProducerListener(String topic, Logger... logger) {
		this.topic = topic;
		if (logger.length > 0) {
			this.logger = logger[0];
		} else {
			this.logger = LoggerFactory.getLogger(getClass());
		}
	}

	@Override
	public void onFailure(Throwable ex) {
		success = false;
		logger.error("send kafka(topic={}) failed: ", topic, ex);
	}

	@Override
	public void onSuccess(Object result) {
		success = true;
		logger.info("send kafka(topic={}) success!", topic);
	}

	public boolean isSuccess() {
		return success;
	}

}
