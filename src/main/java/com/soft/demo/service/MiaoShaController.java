package com.soft.demo.service;

import com.soft.demo.lock.LockRedisImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.locks.Lock;

/**
 * 秒杀操作服务
 */

@Controller
public class MiaoShaController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    GoodsService goodsService;

    Lock lock;


    @PostConstruct
    public void init(){
        lock = new LockRedisImpl(stringRedisTemplate,"apple_2",600);
    }


    /**
     * 秒杀商品
     */
    public boolean miaosha(String goodsCode) throws InterruptedException {

        try{
            lock.lock();
            boolean result = goodsService.buy(goodsCode);
            System.out.println("秒杀结果 :"+result);
            if(result){
                //秒杀成功更新库存到redis
                int count = goodsService.getCount(goodsCode);
                //模拟多核cpu的线程切换等待
                if(count % 2==1){
                    Thread.sleep(new Random().nextInt(500));
                }
                stringRedisTemplate.opsForValue().set(goodsCode,String.valueOf(count));
                return true;
            }
        }finally {
            lock.unlock();
        }

        return false;
    }

    /**
     * 添加商品
     * @param goodsCode
     * @param count
     */
    public void insertGoods(String goodsCode,int count){
        goodsService.insertGoods(goodsCode, count);
    }

}
