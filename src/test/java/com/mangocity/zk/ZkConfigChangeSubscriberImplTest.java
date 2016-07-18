package com.mangocity.zk;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mangocity.zk.ConfigChangeListener;
import com.mangocity.zk.ConfigChangeSubscriber;
import com.mangocity.zk.ZkUtils;

/**
 * 测试配置文件的发布订阅
 * 
 * @author mbr.yangjie
 */
public class ZkConfigChangeSubscriberImplTest extends TestCase {
	private ZkClient zkClient;
	ConfigChangeSubscriber zkConfig;

	public void setUp() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("test-spring-config.xml");
		this.zkClient = ((ZkClient) ctx.getBean("zkClient"));
		this.zkConfig = ((ConfigChangeSubscriber) ctx.getBean("configChangeSubscriber"));
		ZkUtils.mkPaths(this.zkClient, "/zkSample/conf");
		if (!this.zkClient.exists("/zkSample/conf/test1.properties"))
			this.zkClient.createPersistent("/zkSample/conf/test1.properties");

		if (!this.zkClient.exists("/zkSample/conf/test2.properties"))
			this.zkClient.createPersistent("/zkSample/conf/test2.properties");
	}

	//step 1 运行该配置订阅者
	public void testSubscribe() throws IOException, InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		this.zkConfig.subscribe("test1.properties", new ConfigChangeListener() {
			public void configChanged(String key, String value) {
				System.out.println("test1接收到数据变更通知: key=" + key + ", value=" + value);
				latch.countDown();
			}
		});
		System.out.println(this.zkClient.readData("/zkSample/conf/test1.properties"));
		// this.zkClient.writeData("/zkSample/conf/test1.properties", "aa=1");
		boolean notified = latch.await(10L, TimeUnit.SECONDS);
		if (!notified)
			fail("客户端没有收到变更通知");
		Thread.sleep(60 * 1000L);
	}

	//step 2  运行该配置发布者,当配置节点发生改变,订阅该配置的节点配置进行更新
	public void testConfigPublisher() throws InterruptedException {
		this.zkClient.writeData("/zkSample/conf/test1.properties", "test=123sd45645");
		System.out.println(this.zkClient.readData("/zkSample/conf/test1.properties"));
	}

	public void testA() throws InterruptedException {
		List<String> list = this.zkClient.getChildren("/zkSample/conf");
		for (String s : list) {
			System.out.println("children:" + s);
		}

	}
	
	// public void tearDown() {
	// this.zkClient.delete("/zkSample/conf/test1.properties");
	// this.zkClient.delete("/zkSample/conf/test2.properties");
	// }
}