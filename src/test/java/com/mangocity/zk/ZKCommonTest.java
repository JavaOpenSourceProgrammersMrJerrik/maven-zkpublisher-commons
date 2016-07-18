package com.mangocity.zk;

import java.util.List;

import junit.framework.TestCase;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 测试zkClient常用方法
 * 
 * @author mbr.yangjie
 *
 */
public class ZKCommonTest extends TestCase {

	private ZkClient zkClient;

	public void setUp() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("test-spring-config.xml");
		this.zkClient = ((ZkClient) ctx.getBean("zkClient"));
	}

	// 操作节点常用方法
	public void testCreatePath() {
		if (!zkClient.exists("/lock")) {
			zkClient.createPersistent("/lock");
		}
		if (!zkClient.exists("/lock/lock_1")) {
			zkClient.createPersistent("/lock/lock_1", "objectLock");
		}
		zkClient.createPersistentSequential("/lock/seq_", "distribute_lock");
		System.out.println("CountChildren: " + zkClient.countChildren("/lock"));
	}

	public void testDeletePath() {
		System.out.println("删除节点 begin{}");
		zkClient.delete("/lock/lock_1");
		System.out.println("CountChildren: " + zkClient.countChildren("/lock"));
	}

	// 循环删除
	public void testDeleteRecursivePath() {
		System.out.println("循环删除节点 begin{}");
		zkClient.deleteRecursive("/lock");
		System.out.println("CountChildren: " + zkClient.countChildren("/lock"));
	}
	
	public void testListChildren(){
		List<String> children = zkClient.getChildren("/lock");
		System.out.println(children);
	}
}
