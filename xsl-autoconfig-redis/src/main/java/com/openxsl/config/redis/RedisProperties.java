package com.openxsl.config.redis;

import java.util.HashSet;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.util.Assert;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;

/**
 * 配置属性（包括连接池配置、单机模式、哨兵模式、集群模式）
 * 哨兵模式：master + sentinels
 * 集群模式：clusterNodes + maxRedirects  (暂没采用这种方式)
 * @author xiongsl
 */
@ScanConfig
public class RedisProperties implements InitializingBean{
	private JedisPoolConfig poolConfig;
	private JedisShardInfo shardInfo;
	private RedisClusterConfiguration clusterConf;
	private RedisSentinelConfiguration sentinelConf;
	private String registryUrl;
	
	@Value("${redis.pool.maxActive:8}")
	private int maxActive;
	@Value("${redis.pool.maxIdle:8}")
	private int maxIdle;
	@Value("${redis.pool.minIdle:0}")
	private int minIdle;
	@Value("${redis.pool.maxWait:-1}")
	private int maxWait;
	@Value("${redis.pool.testOnBorrow:true}")
	private boolean testOnBorrow;
	
	@Value("${redis.host:}")
	private String host;
	@Value("${redis.port:6379}")
	private int port;
	@Value("${redis.timeout:-1}")
	private int timeout;
	@Value("${redis.password:}")
	private String password;
	@Value("${redis.database:0}")
	private int database;
	
	@Value("${redis.cluster.nodes:}")
	private String clusterNodes;
	@Value("${redis.cluster.maxRedirects:5}")
	private int maxRedirects;
	
	@Value("${redis.sentinel.nodes:}")
	private String sentinelNodes;
	@Value("${redis.sentinel.master:}")
	private String master;
	
	@Value("${tracing.enable.redis:false}")
	private boolean shouldTracing;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.poolConfig = new JedisPoolConfig();  //testWhileIdle=true
		poolConfig.setMaxTotal(maxActive);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWaitMillis(maxWait);
		poolConfig.setTestOnBorrow(testOnBorrow);
		
		if (clusterNodes.length() > 0) {
			this.clusterConf = new RedisClusterConfiguration(StringUtils.split2(clusterNodes, ","));
			clusterConf.setMaxRedirects(maxRedirects);
			registryUrl = String.format("redis-cluster://%s/%s", clusterNodes,database);
		}else if (sentinelNodes.length() > 0) {
			Assert.isTrue(master.length()>0, "'redis.master' must not be empty while sentinel-mode");
			this.sentinelConf = new RedisSentinelConfiguration(master, 
					new HashSet<String>(StringUtils.split2(sentinelNodes, ",")));
			registryUrl = String.format("redis-sentinel://%s/%s", sentinelNodes,database);
		}else {
			Assert.isTrue(host.length()>0, "'redis.host' must not be empty while standalone-mode");
			registryUrl = String.format("redis-maslaver://%s:%d/%s", host,port,database);
		}
		this.shardInfo = new JedisShardInfo(host, port, timeout);
		if (!StringUtils.isEmpty(password)) {
			this.shardInfo.setPassword(password);
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getDatabase() {
		return database;
	}

	public void setDatabase(int database) {
		this.database = database;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public int getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}

	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public String getClusterNodes() {
		return clusterNodes;
	}

	public void setClusterNodes(String clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	public int getMaxRedirects() {
		return maxRedirects;
	}

	public void setMaxRedirects(int maxRedirects) {
		this.maxRedirects = maxRedirects;
	}

	public String getSentinelNodes() {
		return sentinelNodes;
	}

	public void setSentinelNodes(String sentinelNodes) {
		this.sentinelNodes = sentinelNodes;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}
	
	public JedisPoolConfig getPoolConfig() {
		return poolConfig;
	}

	public JedisShardInfo getShardInfo() {
		return shardInfo;
	}

	public RedisClusterConfiguration getClusterConf() {
		return clusterConf;
	}

	public RedisSentinelConfiguration getSentinelConf() {
		return sentinelConf;
	}

	public String getRegistryUrl() {
		return registryUrl;
	}

	public boolean shouldTracing() {
		return shouldTracing;
	}

}
