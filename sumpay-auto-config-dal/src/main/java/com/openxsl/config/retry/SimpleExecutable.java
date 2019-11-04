package com.openxsl.config.retry;

import java.util.List;

import com.openxsl.config.exception.ServiceException;
import com.openxsl.config.queue.TaskQueue;

/**
 * 子类只需要实现 execute()方法
 * @author 001327
 * @created 2017-06-02
 */
@SuppressWarnings("serial")
public class SimpleExecutable<P, R> implements Executable<P, R> {
	
	@Override
	public R execute(P args, int fails) throws ServiceException {
		throw new UnsupportedOperationException("No implements");
	}

	@Override
	public List<P> load() {
		return null;
	}

	@Override
	public TaskQueue getSuccQueue() {
		return null;
	}

	@Override
	public TaskQueue getFailQueue() {
		return null;
	}

}
