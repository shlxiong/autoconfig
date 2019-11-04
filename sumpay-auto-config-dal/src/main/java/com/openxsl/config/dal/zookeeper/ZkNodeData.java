package com.openxsl.config.dal.zookeeper;

import org.apache.zookeeper.data.Stat;

/**
 * ZooKeeper数据
 * @author xiongsl
 */
public class ZkNodeData  {
	private Stat stat;
	private String data;
	
	public ZkNodeData(Stat stat) {
		this.setStat(stat);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Stat getStat() {
		return stat;
	}

	public void setStat(Stat stat) {
		this.stat = stat;
	}
	
}
