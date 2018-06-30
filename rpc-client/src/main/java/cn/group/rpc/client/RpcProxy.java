package cn.group.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;


import cn.group.rpc.common.RpcRequest;
import cn.group.rpc.common.RpcResponse;
import cn.group.rpc.registry.ServiceDiscovery;


/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018��6��30��
 * @category RPC �������ڴ���RPC �ķ������
 */
public class RpcProxy {

	private String serverAddress;
	private ServiceDiscovery serviceDiscovery;
	
	public RpcProxy(String serverAddress){
		this.serverAddress = serverAddress;
	}
	
	public RpcProxy(ServiceDiscovery serviceDiscovery){
		this.serviceDiscovery = serviceDiscovery;
	}
	/**
	 * 
	 * @param interfaceClass
	 * @category ��������
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> interfaceClass){
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class<?>[]{interfaceClass}, new InvocationHandler(){

					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						//����RpcRequest����װ�������������
						RpcRequest request = new RpcRequest();
						request.setRequestId(UUID.randomUUID().toString());
						//�õ��������������ҵ��ӿ�����
						request.setClassName(method.getDeclaringClass().getName());
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);
						//���ҷ���
						if(null != serviceDiscovery){
							serverAddress = serviceDiscovery.discover();
						}
						//�����ȡ����ĵ�ַ
						String[] array = serverAddress.split(":");
						String host = array[0];
						int port = Integer.parseInt(array[1]);
						//����Nettyʵ�ֵ�RpcClient,���ӷ����
						RpcClient client = new RpcClient(host,port);
						//ͨ��Netty���������������
						RpcResponse response = client.send(request);
						//������Ϣ
						if(response.isError()){
							throw response.getError();
						}else{
							return response.getResult();
						}
					}
		});
	}
}
