package com.liushao.redislockframework;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisFactory {
	
	private final static String HOST = "10.19.18.58";
	private final static int PORT = 9040;
	private final static String AUTH = "migu_redis123!";
	private final static int MAX_IDLE = 10;
	private final static int MAX_ACTIVE = 50;
	private final static int TIMEOUT = 0;
	private final static int MAX_WAIT = 100000;//等待可用连接的最大时间(毫秒)，默认值-1，表示永不超时。若超过等待时间，则抛JedisConnectionException
	private final static boolean TEST_ON_BORROW = true;   //使用连接时，测试连接是否可用
	private final static boolean TEST_ON_RETURN = true;   //返回连接时，测试连接是否可用
	private static JedisPool jedisPool;

	public static JedisPoolConfig getPoolConfig() throws IOException{
		Properties properties = new Properties();
		
		InputStream in = RedisFactory.class.getClassLoader().getResourceAsStream("redis.properties");
		
		try {
			properties.load(in);
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(Integer.parseInt(properties.getProperty("maxIdle", "100")));
			config.setMinIdle(Integer.parseInt(properties.getProperty("minIdle", "1")));
			config.setMaxTotal(Integer.parseInt(properties.getProperty("maxTotal", "1000")));
			return config;
		} finally {
			in.close();
		}
		
	}
	
	public static RedisClient getDefaultClient(){
//		JedisPool pool = new JedisPool("127.0.0.1");
		JedisPoolConfig config = new JedisPoolConfig();  
	    config.setMaxIdle(MAX_IDLE);  
	    config.setMaxWaitMillis(MAX_WAIT);
	    config.setTestOnBorrow(TEST_ON_BORROW);  
	    config.setTestOnReturn(TEST_ON_RETURN);  
	    jedisPool = new JedisPool(config, HOST, PORT, TIMEOUT,AUTH,10);
		RedisClient client = new RedisClient(jedisPool);
		return client;
	}
}
