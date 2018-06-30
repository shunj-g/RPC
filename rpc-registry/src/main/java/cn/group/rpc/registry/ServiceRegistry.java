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
 * @created 2018��6��30��
 * @category ����ע�ᣬZK���С�����ע����Ľ�ɫ
 * 			  ����ע�����з������ĵ�ַ��˿ڣ�
 *           ���Կͻ����ṩ�����ֵĹ���
 */
public class ServiceRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	private String registryAddress;
	
	public ServiceRegistry(String registryAddress){
		//zookeeper�ĵ�ַ
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
								//�����������ļ�������������ﵽ�㣬�ͷ����еȴ����̡߳�
								latch.countDown();
							}
						}
			});
			/*
			 * ʹ��ǰ�̵߳ȴ�ֱ���������������㣬�����̱߳��жϡ������ǰ����Ϊ�㣬
			 * ��÷����������ء������ǰ���������㣬��ô�����̵߳���Ŀ�ģ���ǰ�߳�
			 * �������ã�����������״̬��ֱ�����������������֮һ:
			 */
			latch.await();
		} catch (Exception e) {
			LOGGER.error("",e);
		}
		return zk;
	}
}
