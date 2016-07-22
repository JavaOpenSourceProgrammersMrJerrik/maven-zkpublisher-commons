package com.mangocity.zk;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 测试zkClient发布订阅
 * 
 * @author mbr.yangjie
 *
 */
public class ZKPubSubscribe extends TestCase {

	private ZkClient zkClient;

	private CountDownLatch latch = new CountDownLatch(1);

	public void setUp() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("test-spring-config.xml");
		this.zkClient = ((ZkClient) ctx.getBean("zkClient"));
	}

	// 子节点变化监听器
	public void testSubscribeChildChanges() throws InterruptedException {
		zkClient.subscribeChildChanges("/lock", new IZkChildListener() {
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				System.out.println("parentPath: " + parentPath);
				System.out.println("currentChilds: " + currentChilds);
			}
		});
		latch.await();
	}

	public void testSubscribeChildChangesCreatePath() throws InterruptedException {
		if (!zkClient.exists("/lock")) {
			zkClient.createPersistent("/lock");
		}
		if (!zkClient.exists("/lock/hello")) {
			zkClient.createPersistent("/lock/hello", "helloworld");
		}

		TimeUnit.SECONDS.sleep(3);
		zkClient.createPersistent("/lock/lock3", "helloworld");
	}

	/***************************************************/
	// 数据变化监听器
	public void testSubscribeDataChanges() throws InterruptedException {
		zkClient.subscribeDataChanges("/lock", new IZkDataListener() {
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				System.out.println("handleDataChange begin{} dataPath: " + dataPath + " ,data: " + data);
			}

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				System.out.println("handleDataDeleted begin{} dataPath: " + dataPath);
			}
		});
		latch.await();
	}

	// 往节点上写数据,如果数据发生变化,则通知监听器
	public void testSubscribeDataChangesWriteData() throws InterruptedException {
		zkClient.writeData("/lock", "testSubscribeDataChanges");

		TimeUnit.SECONDS.sleep(3);

		System.out.println("重新写入数据 begin{}");
		zkClient.writeData("/lock", "重新写入数据");

		TimeUnit.SECONDS.sleep(3);

		System.out.println("重新写入数据 begin{}");
		zkClient.writeData("/lock", null);
	}

	public void testSubscribeStateChanges() throws InterruptedException {
		zkClient.subscribeStateChanges(new IZkStateListener() {
			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				System.out.println("handleStateChanged begin{}" + state.name());
			}

			@Override
			public void handleNewSession() throws Exception {
				System.out.println("handleNewSession begin{}");
			}
		});
		latch.await();
	}

}
