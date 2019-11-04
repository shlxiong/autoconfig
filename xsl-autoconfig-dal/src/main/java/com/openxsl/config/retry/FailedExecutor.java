package com.openxsl.config.retry;

import com.openxsl.config.queue.TaskQueue;

public interface FailedExecutor<P, R> extends Executable<P, R> {
	
	@Override
	default TaskQueue getFailQueue() {
		return null;
	}

}
