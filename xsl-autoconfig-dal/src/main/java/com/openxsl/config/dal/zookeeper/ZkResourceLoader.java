package com.openxsl.config.dal.zookeeper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.WritableResource;

/**
 * 读取Zookeeper的数据
 * @author xiongsl
 */
public class ZkResourceLoader extends DefaultResourceLoader {
	
	@Override
	public ZookeeperResource getResource(String location){
		return new ZookeeperResource(location);
	}
	
	public class ZookeeperResource extends AbstractResource implements WritableResource{ //implements Resource {
		private String zknode;

		public ZookeeperResource(String path) {
			this.zknode = path;
		}

		@Override
		public String getDescription() {
			return String.format("ZookeeperResource[%s]", zknode);
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return new ZkNodeInpustream(zknode);
		}
		
		@Override
		public boolean isWritable() {
			return true;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return new ZkNodeOuputstream(zknode);
		}
		
	}
	
	public class ZkNodeInpustream extends InputStream{
		private String node;
		public ZkNodeInpustream(String node){
			this.node = node;
		}

		@Override
		public int read() throws IOException {
			return 0;
		}
		public String getNode(){
			return node;
		}
		
	}
	public class ZkNodeOuputstream extends OutputStream{
		private String node;
		public ZkNodeOuputstream(String node){
			this.node = node;
		}

		@Override
		public void write(int b) throws IOException {
		}
		public String getNode(){
			return node;
		}
		
	}

}
