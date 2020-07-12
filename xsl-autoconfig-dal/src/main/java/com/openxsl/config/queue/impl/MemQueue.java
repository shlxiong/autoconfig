package com.openxsl.config.queue.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.openxsl.config.queue.QueueFactory;
import com.openxsl.config.queue.TaskQueue;
import com.openxsl.config.retry.Executable;

/**
 * 内存队列
 * BlockingQueue方法：
 * 插入：add()-抛异常                 put()-阻塞        offer()-丢弃
 * 删除：remove()-抛异常        take()-阻塞     poll()-null
 * TOP：element()-抛异常                                      peek()-null
 */
@SuppressWarnings("rawtypes")
public class MemQueue implements TaskQueue, InitializingBean {
    private String name;
    private int size = 256;
    private boolean fair = true;
    private BlockingQueue<Object> queue;
    private Executable service;
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public MemQueue() {
    }
    public MemQueue(String name, boolean fair) {
    	this.setName(name);
    	this.setFair(true);
    }
    
    @Override
    public void afterPropertiesSet(){
    	if (name == null) {
    		throw new RuntimeException();
    	}
        QueueFactory.put(this);
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object get() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public boolean put(Object obj) {
        try {
            queue.put(obj);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<Object> getAll(){
        List<Object> results = new ArrayList<Object>();
        queue.drainTo(results);
        return results;
    }
    
    @Override
    public Executable getService() {
        return service;
    }
    
    public void setName(String name){
        this.name = name;
    }
    public void setService(Executable service){
        this.service = service;
    }
    public void setSize(int size) {
        this.size = size;
        logger.info("{} size:{}", name,size);
        queue = fair ? new ArrayBlockingQueue<Object>(size)
        	  : new PriorityBlockingQueue<Object>(size+1);
    }
    
    @Override
	public void setFair(boolean fair) {
    	this.fair = fair;
    }
    
    @Override
	public String toString() {
    	return new StringBuilder("MemQueue@").append(this.hashCode())
    			.append("{name:").append(name)
    			.append(", size:").append(size).append(", service:")
    			.append(service).append(", fair:").append(fair)
    			.append("}").toString();
    }
    
    public static void main(String[] args) throws InterruptedException {
		BlockingQueue<String> queue = new PriorityBlockingQueue<String>();
		queue.put("b");
		queue.put("a");
		System.out.println(queue.take());  //a
		System.out.println(queue.take());  //b
		queue = new ArrayBlockingQueue<String>(2);
		queue.put("b");
		queue.put("a");
		System.out.println(queue.take());  //b
		System.out.println(queue.take());  //a
	}

}
