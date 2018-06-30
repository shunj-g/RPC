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
 * @created 2018��6��30��
 */
public class ServiceDiscovery {

	//��¼��
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);
	//������
	private CountDownLatch latch = new CountDownLatch(1);
	
	private volatile List<String> dataList = new ArrayList<String>();
	
	private String registryAddress;
	
	/**
	 * 
	 * @param registryAddress
	 * @category zk����
	 */
	public ServiceDiscovery(String registryAddress){
		this.registryAddress = registryAddress;
		ZooKeeper zk = connectServer();
		if(null != zk){
			watchNode(zk);
		}
	}

	/**
	 * @category �����½ڵ�
	 * @return data
	 */
	public String discover(){
		String data = null;
		int size = dataList.size();
		//�����½ڵ㣬ʹ�ü���
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
							//�ڵ�ı�
							if(event.getType() == Event.EventType.NodeChildrenChanged){
								watchNode(zk);
							}
						}
					});
			List<String> dataList = new ArrayList<String>();
			//ѭ���ӽڵ�
			for(String node : nodeList){
				//��ȡ�ڵ��еķ�������ַ
				byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH+"/"+node, false, null);
				//�洢��list��
				dataList.add(new String(bytes));
			}
			LOGGER.debug("node data:{}",dataList);
			//���ڵ���Ϣ��¼�ڳ�Ա����
			this.dataList = dataList;
		} catch (Exception e) {
			LOGGER.error("",e);
		}
		
	}

	/**
	 * @category ����
	 * @return
	 */
	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress,Constant.ZK_SESSION_TIMEOUT,
					new Watcher(){

						public void process(WatchedEvent event) {
							//�ͻ��˴�������״̬
							if(event.getState() == Event.KeeperState.SyncConnected){
								/**
								 * �����������ļ�������������ﵽ�㣬�ͷ����еȴ����̡߳�
								 * �����ǰ���������㣬��ô�������ݼ�������¼���Ϊ�㣬
								 * ��ô���еȴ��߳̽����������ã������̵߳��ȡ�
								 * �����ǰ����Ϊ�㣬��ôʲôҲ���ᷢ����
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
