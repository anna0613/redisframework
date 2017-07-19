package com.liushao.redislockframework;


import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class SecKillTest {
	private static Long commidityId1 = 10000001L;
	private static Long commidityId2 = 10000002L;
//	private static JedisPool pool; //线程池对象  
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
	/*@Before
	public synchronized void  beforeTest() throws IOException{
		
		
//		jedisPool = new JedisPool("127.0.0.1");
		jedisPool = new JedisPool(HOST,PORT);
		jedisPool.getResource().select(10);
		
		JedisPoolConfig config = new JedisPoolConfig();  
	    config.setMaxIdle(MAX_IDLE);  
	    config.setMaxWaitMillis(MAX_WAIT);
	    config.setTestOnBorrow(TEST_ON_BORROW);  
	    config.setTestOnReturn(TEST_ON_RETURN);  
	    jedisPool = new JedisPool(config, HOST, PORT, TIMEOUT,AUTH,10); //新建连接池，如有密码最后加参数  
		
	}*/
	
	@Test
	public void testSecKill()throws Exception{
		int threadCount = 1000;
		int splitPoint = 500;
		CountDownLatch endCount = new CountDownLatch(threadCount);
		CountDownLatch beginCount = new CountDownLatch(1);
		SecKillImpl testClass = new SecKillImpl();
		
		Thread[] threads = new Thread[threadCount];
		//起500个线程，秒杀第一个商品
		for(int i= 0;i < splitPoint;i++){
			threads[i] = new Thread(new  Runnable() {
				public void run() {
					try {
						//等待在一个信号量上，挂起
						beginCount.await();
						//用动态代理的方式调用secKill方法
						System.out.println("beginCount await结束 前");
						SeckillInterface proxy = (SeckillInterface) Proxy.newProxyInstance(SeckillInterface.class.getClassLoader(), 
							new Class[]{SeckillInterface.class}, new CacheLockInterceptor(testClass));
						proxy.secKill("test", commidityId1);
						endCount.countDown();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			threads[i].start();

		}
		
		for(int i= splitPoint;i < threadCount;i++){
			threads[i] = new Thread(new  Runnable() {
				public void run() {
					try {
						//等待在一个信号量上，挂起
						beginCount.await();
						//用动态代理的方式调用secKill方法 
						System.out.println("beginCount await结束 后");
						SeckillInterface proxy = (SeckillInterface) Proxy.newProxyInstance(SeckillInterface.class.getClassLoader(), 
							new Class[]{SeckillInterface.class}, new CacheLockInterceptor(testClass));
						proxy.secKill("test", commidityId2);
						//testClass.testFunc("test", 10000001L);
						endCount.countDown();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			threads[i].start();

		}
		
		
		long startTime = System.currentTimeMillis();
		//主线程释放开始信号量，并等待结束信号量
		System.out.println("beginCount 没开始");
		beginCount.countDown();
		System.out.println("beginCount 开始");
		
		try {
			//主线程等待结束信号量
			endCount.await();
			//观察秒杀结果是否正确
			System.out.println(SecKillImpl.inventory.get(commidityId1));
			System.out.println(SecKillImpl.inventory.get(commidityId2));
			System.out.println("error count" + CacheLockInterceptor.ERROR_COUNT);
			System.out.println("total cost " + (System.currentTimeMillis() - startTime));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*//获取连接资源以及回收使用完的连接
	private static Jedis getJedisResource () {  
        try {  
            if (jedisPool!=null) {  
                Jedis resource = jedisPool.getResource();  
                return resource;  
            }   
            return null;  
        } catch(Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
    }
	
	private static void returnResource (Jedis used) {  
        
        if(jedisPool!=null){  
        	jedisPool.returnResource(used);  
        }  
          
    } 
	
	@Test  
    public void testPool() {  
        Jedis resource = getJedisResource();  
        resource.select(2);  
        resource.lpush("Countries", "USA"); //redis对list头的插入元素操作  
        resource.lpush("Countries", "UK");  
        resource.lpush("Countries", "CHINA");  
        System.out.println(resource.rpop("Countries")); //list末尾弹出元素  
        returnResource(resource);  
    }  */
}
