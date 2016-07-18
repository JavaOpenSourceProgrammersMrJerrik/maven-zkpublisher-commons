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

	// step 1 运行该配置订阅者
	public void testSubscribe() throws IOException, InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		this.zkConfig.subscribe("test1.properties", new ConfigChangeListener() {
			public void configChanged(String key, String value) {
				System.out.println("ConfigChangeListener 接收到数据变更通知: key=" + key + ", value=" + value);
				//latch.countDown();
			}
		});
		System.out.println(this.zkClient.readData("/zkSample/conf/test1.properties"));
		latch.await(200L,TimeUnit.SECONDS);//两百秒后退出
	}

	// step 2 运行该配置发布者,当配置节点发生改变,订阅该配置的节点配置进行更新
	public void testConfigPublisher() throws InterruptedException {
		System.out.println("动态修改配置1....begin{}");
		this.zkClient.writeData("/zkSample/conf/test1.properties", "test=hello world");

		//发布第二次配置
		TimeUnit.SECONDS.sleep(5);
		System.out.println("动态修改配置2....begin{}");
		this.zkClient.writeData("/zkSample/conf/test1.properties", "test=update config");
		
		TimeUnit.SECONDS.sleep(3);
		System.out.println("删除节点....begin{}");
		this.zkClient.delete("/zkSample/conf/test1.properties");
	}

	public void testA() throws InterruptedException {
		List<String> list = this.zkClient.getChildren("/zkSample/conf");
		for (String s : list) {
			System.out.println("children:" + s);
		}

	}

}