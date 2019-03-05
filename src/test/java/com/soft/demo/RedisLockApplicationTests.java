package com.soft.demo;

import com.soft.demo.service.MiaoShaController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisLockApplicationTests {

	@Autowired
	MiaoShaController miaoShaController;

	Long time=null;

	@Before
	public void before(){
		time = System.currentTimeMillis();
		System.out.println("redis lock start ......");
	}

	@After
	public void after(){
		long wait = System.currentTimeMillis() - time;
		System.out.println("redis lock end ......"+"耗费时间为:"+ wait);
	}

	//@Test
	public void test(){
		System.out.println("random:"+new Random().nextInt(500));
	}

	@Test
	public void contextLoads() throws InterruptedException {
		CountDownLatch cd = new CountDownLatch(500);
		for (int i=0;i<501;i++){
			new Thread(()->{
				try {
					cd.await();
					boolean result = miaoShaController.miaosha("apple");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
			cd.countDown();
		}
		Thread.sleep(50*1000L);
	}

}
