package cn.group.rpc.registry;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018年6月30日
 */
public class ServiceDiscovery {

	//记录器
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);
	//锁存器
	private CountDownLatch latch = new CountDownLatch(1);
	
	private volatile List<String> dataList = new ArrayList<String>();
	
	private String registryAddress;
	
	/**
	 * 
	 * @param registryAddress
	 * @category zk链接
	 */
	public ServiceDiscovery(String registryAddress){
		this.registryAddress = registryAddress;
		ZooKeeper zk = connectServer();
		if(null != zk){
			watchNode(zk);
		}
	}

	/**
	 * @category 发现新节点
	 * @return data
	 */
	public String discover(){
		String data = null;
		int size = dataList.size();
		//存在新节点，使用即可
		if(size > 0){
			if(1 == size){
				data = dataList.get(0);
				LOGGER.debug("using only data{}",data);
			}else{
				data = dataList.get(ThreadLocalRandom.current().nextInt(size));
				LOGGER.debug("using random data:{}",data);
			}
		}
		return data;
	}
	/**
	 * 
	 * @param zk
	 */
	private void watchNode(final ZooKeeper zk) {
		try {
			List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH,
					new Watcher(){

						public void process(WatchedEvent event) {
							//节点改变
							if(event.getType() == Event.EventType.NodeChildrenChanged){
								watchNode(zk);
							}
						}
					});
			List<String> dataList = new ArrayList<String>();
			//循环子节点
			for(String node : nodeList){
				//获取节点中的服务器地址
				byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH+"/"+node, false, null);
				//存储到list中
				dataList.add(new String(bytes));
			}
			LOGGER.debug("node data:{}",dataList);
			//将节点信息记录在成员变量
			this.dataList = dataList;
		} catch (Exception e) {
			LOGGER.error("",e);
		}
		
	}

	/**
	 * @category 链接
	 * @return
	 */
	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress,Constant.ZK_SESSION_TIMEOUT,
					new Watcher(){

						public void process(WatchedEvent event) {
							//客户端处于连接状态
							if(event.getState() == Event.KeeperState.SyncConnected){
								/**
								 * 减少锁存器的计数，如果计数达到零，释放所有等待的线程。
								 * 如果当前计数大于零，那么它将被递减。如果新计数为零，
								 * 那么所有等待线程将被重新启用，用于线程调度。
								 * 如果当前计数为零，那么什么也不会发生。
								 */
								latch.countDown();
							}
						}
			});
			latch.await();
		} catch (Exception e) {
			LOGGER.error("",e);
		}
		return zk;
	}
	
}
