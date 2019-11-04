package com.openxsl.config.redis;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.openxsl.config.autodetect.ScanConfig;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis配置对象（jedisPoolConfig，redisConnectionFactory，redisTemplate，redisHelper）
 * @author xiongsl
 */
@SuppressWarnings({"rawtypes","unchecked"})
@ScanConfig
public class RedisConfiguration {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	//单机版
//	@Bean(name="jedisPool")
//  @Autowired
//  public JedisPool jedisPool(RedisProperties redisProperties,
//    					@Qualifier("jedisPoolConfig")JedisPoolConfig config) {
//		return new JedisPool(config, 
//						redisProperties.getHost(), redisProperties.getPort(),
//						redisProperties.getTimeout(),
//						redisProperties.getPassword(),
//						redisProperties.getDatabase());
//    }
	
//	//哨兵（一主多备）
//	public JedisSentinelPool jedisHAPool(RedisProperties redisProperties,
//						@Qualifier("jedisPoolConfig")JedisPoolConfig config) {
//		return new JedisSentinelPool(master, sentinels, config, timeout, password, database);
//	}
//	
//	//集群（分shard）
//	public ShardedJedisPool jedisClusterPool(RedisProperties redisProperties,
//						@Qualifier("jedisPoolConfig")JedisPoolConfig config) {
//		return ShardedJedisPool(config, List<JedisSharedInfo>);
//	}
	
	@Bean(name="jedisPoolConfig")
    public JedisPoolConfig jedisPoolConfig(RedisProperties redisProperties) {
        return redisProperties.getPoolConfig();
    }
	
	@Bean
	@DependsOn("jedisPoolConfig")
	public JedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties,
						JedisPoolConfig config) {
		JedisConnectionFactory connectionFactory = null;
		if (redisProperties.getClusterConf() != null) {
			logger.info("redis in [cluster] mode");
			connectionFactory = new JedisConnectionFactory(redisProperties.getClusterConf());
		}else if (redisProperties.getSentinelConf() != null) {
			logger.info("redis in [sentinel] mode");
			connectionFactory = new JedisConnectionFactory(redisProperties.getSentinelConf());
		}else {  //普通的主从模式？
			logger.info("redis in [standalone] mode");
			connectionFactory = new JedisConnectionFactory();
		}
		connectionFactory.setShardInfo(redisProperties.getShardInfo());
		connectionFactory.setPoolConfig(config);
		connectionFactory.setDatabase(redisProperties.getDatabase());
		connectionFactory.afterPropertiesSet();
		return connectionFactory;
	}
	
	@Bean("redisTemplate")
	@Scope("prototype")
	public RedisTemplate<String,Serializable> redisTemplate(RedisProperties redisProperties,
						JedisConnectionFactory connectionFactory) {
//		String redisUrl = redisProperties.getRegistryUrl();
//		boolean tracing = redisProperties.shouldTracing();
//		FlexibleRedisTemplate<String,Serializable> template = 
//					new FlexibleRedisTemplate<String,Serializable>(redisUrl, tracing);
		
		RedisTemplate template = new RedisTemplate();
		template.setConnectionFactory(connectionFactory);
		return template;
	}
	
	//一不小心就会使用Jdk序列化
	@Bean
	@DependsOn("redisTemplate")
	@Scope("prototype")
	public GenericRedisHelper redisHelper(RedisTemplate<String,Serializable> redisTemplate) {
		GenericRedisHelper helper = new GenericRedisHelper();
		helper.setTemplate(redisTemplate);
//		return RedisHelperTracingAdvisors.getProxyBean(helper);
		return helper;
	}
	
	@Bean
	@DependsOn("redisTemplate")
	@Scope("prototype")
	public MapRedisHelper mapRedisHelper(RedisTemplate<String,Serializable> redisTemplate) {
		MapRedisHelper helper = new MapRedisHelper();
		helper.setTemplate(redisTemplate);
//		return RedisHelperTracingAdvisors.getProxyBean(helper);
		return helper;
	}
	
	@Bean
	@DependsOn("redisTemplate")
	@Scope("prototype")
	public ListRedisHelper listRedisHelper(RedisTemplate<String,Serializable> redisTemplate) {
		ListRedisHelper helper = new ListRedisHelper();
		helper.setTemplate(redisTemplate);
//		return RedisHelperTracingAdvisors.getProxyBean(helper);
		return helper;
	}
    
}
