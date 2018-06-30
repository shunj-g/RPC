package cn.group.rpc.registry;


import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018年6月30日
 * @category 服务注册，ZK是有“服务注册表”的角色
 * 			  用于注册所有服务器的地址与端口，
 *           并对客户端提供服务发现的功能
 */
public class ServiceRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	private String registryAddress;
	
	public ServiceRegistry(String registryAddress){
		//zookeeper的地址
		this.registryAddress = registryAddress;
	}
	
	public void register(String data){
		if(null != data){
			ZooKeeper zk = connectServer();
			if(null != zk){
				createNode(zk,data);
			}
		}
	}

	private void createNode(ZooKeeper zk, String data) {
		try {
			byte[] bytes = data.getBytes();
			if(null == zk.exists(Constant.ZK_REGISTRY_PATH, null)){
				zk.create(Constant.ZK_REGISTRY_PATH, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			
			String path = zk.create(Constant.ZK_DATA_PATH, bytes, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			LOGGER.debug("create zookeeper node ({} ==> {}",path,data);
		} catch (Exception e) {
		    LOGGER.error("",e);
		}
		
	}

	/**
	 * 
	 * @return zk
	 */
	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress,Constant.ZK_SESSION_TIMEOUT,
					new Watcher(){

						@Override
						public void process(WatchedEvent event) {
							if(event.getState() == Event.KeeperState.SyncConnected){
								//减少锁存器的计数，如果计数达到零，释放所有等待的线程。
								latch.countDown();
							}
						}
			});
			/*
			 * 使当前线程等待直到闩锁被计数到零，除非线程被中断。如果当前计数为零，
			 * 则该方法立即返回。如果当前计数大于零，那么出于线程调度目的，当前线程
			 * 将被禁用，并处于休眠状态，直到发生以下两种情况之一:
			 */
			latch.await();
		} catch (Exception e) {
			LOGGER.error("",e);
		}
		return zk;
	}
}
