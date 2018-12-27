package com.feather;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JedisLockMain {

    private static JedisPool jedisPool;

    static{
        GenericObjectPoolConfig<Jedis> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(10);
        genericObjectPoolConfig.setMaxIdle(5);
        genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(10);
        jedisPool = new JedisPool(genericObjectPoolConfig,"localhost",6379);
        //尝试获取jedis连接
        try{
            Jedis jedis = jedisPool.getResource();
        }catch(Exception e){
            System.err.println("Can not create connection to redis");
        }finally {
            System.exit(-1);
        }
    }

    private static boolean ResourceLock(String key,String requestId,int expireTime){
        Jedis jedis = jedisPool.getResource();
        try{
            SetParams setParams = new SetParams();
            setParams.nx().ex(expireTime);
            String result = jedis.set(key,requestId,setParams);
            return (result==null?"":result).equals("OK")?true:false;
        }finally {
            jedis.close();
        }
    }

    private static boolean ResourceUnLock(String key,String requestId){
        Jedis jedis = jedisPool.getResource();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        try{
            Object result = jedis.eval(script, Collections.singletonList(key),Collections.singletonList(requestId));
            return (long)result==1?true:false;
        }finally {
            jedis.close();
        }
    }

    public static void main(String[] args){
        String resouceKey = "123456789";
        String[] requestId = {"Server_Number_1","Server_Number_2"};
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for(int i=0;i<requestId.length;i++){
            int index = i;
            executorService.execute(()->{
               if(ResourceLock(resouceKey,requestId[index],10)){
                   System.out.println(requestId[index]+"加锁成功");
               }else{
                   System.out.println(requestId[index]+"加锁失败");
               }
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(ResourceUnLock(resouceKey,requestId[index])){
                    System.out.println(requestId[index]+"解锁成功");
                }else{
                    System.out.println(requestId[index]+"解锁失败");
                }
            });
        }
        executorService.shutdown();
    }
}
