package com.soft.demo.lock;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class LockRedisImpl implements Lock {

    private StringRedisTemplate stringRedisTemplate;
    private String resourceName;
    int timeout;

    Lock lock = new ReentrantLock();

    public LockRedisImpl(StringRedisTemplate stringRedisTemplate,String resourceName,int timeout){
        this.stringRedisTemplate = stringRedisTemplate;
        this.resourceName = resourceName;
        this.timeout = timeout;
    }

    @Override
    public void lock() {

        try{
            //保证每台机器上有一个线程抢到锁，链接redis
            lock.lock();
            //尝试获取锁
            while (!tryLock()){
                stringRedisTemplate.execute(new RedisCallback<Long>() {
                    @Override
                    public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                        try {
                            CountDownLatch cd = new CountDownLatch(1);
                            // subscribe异步触发
                            redisConnection.subscribe((message,pattern)->{
                                //收到消息，不管结果，直接抢锁
                                cd.countDown();
                            },("lock_"+resourceName).getBytes());
                            //线程进入订阅等待锁的状态
                            cd.await(timeout,TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return 0L;
                    }
                });
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public boolean tryLock() {
        Boolean lockResult = stringRedisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Boolean result = redisConnection.set(resourceName.getBytes(),"".getBytes(), Expiration.seconds(timeout),
                        RedisStringCommands.SetOption.SET_IF_ABSENT);
                return result;
            }
        });
        return lockResult;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        stringRedisTemplate.delete(resourceName);
        stringRedisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Long value = redisConnection.publish(("lock_"+resourceName).getBytes(),"".getBytes());
                return value;
            }
        });
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
