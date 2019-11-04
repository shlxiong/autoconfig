package com.openxsl.config.queue.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;

import com.openxsl.config.queue.Listener;
import com.openxsl.config.queue.QueueFactory;
import com.openxsl.config.queue.TaskQueue;
import com.openxsl.config.retry.RetryExecutor;
import com.openxsl.config.retry.RetryStrategy;
import com.openxsl.config.thread.GrouppedThreadFactory;

/**
 * @author xiongsl
 */
public class MemQueueListener implements Listener {
    private TaskQueue queue;
    public void setQueue(TaskQueue queue){
        this.queue = queue;
    }
    public void setQueueName(String queueName){
        this.queue = QueueFactory.getQueue(queueName);
    }
    
    protected ExecutorService pool;
    private int consumers;
    public void setConsumers(int n){
        this.consumers = n;
    }
    private RetryStrategy strategy = RetryStrategy.DEFAULT;
    public void setRetry(RetryStrategy strategy){
    	this.strategy = strategy;
    }

    @PostConstruct
    @Override
    public void start() {
        if (queue.getService() != null){
        	try {
	            for (Object elt : queue.getService().load()){
	                queue.put(elt);
	            }
        	}catch (NullPointerException npe) {
        	}
        }
        
//        pool = new ThreadPoolExecutor(consumers, consumers, 60, TimeUnit.SECONDS,
//                        new ArrayBlockingQueue<Runnable>(10),
//                        new GrouppedThreadFactory(queue.getName()+"-consumer"),
//                        new ThreadPoolExecutor.CallerRunsPolicy() );
        pool = new GrouppedThreadFactory(queue.getName()+"-consumer")
        			.newThreadPool(consumers, consumers, 60);
        String listenerName = "MemQueueListener-"+queue.getName();
        GrouppedThreadFactory.newThread(listenerName, true,
        		new Runnable() {
		        	@Override
		            public void run(){
		                while (true){
		                    pool.submit(new MyCallable(queue.get()));
		                }
		            }
        		}).start();
    }
    
    class MyCallable implements Callable<Object>{
        private Object data;
        
        public MyCallable(Object data){
            this.data = data;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object call() throws Exception {
            return RetryExecutor.execute(queue.getService(), data, strategy);
        }
        
    }
	
}
