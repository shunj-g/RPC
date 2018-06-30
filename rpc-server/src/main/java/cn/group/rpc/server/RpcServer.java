package cn.group.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import cn.group.rpc.common.RpcDecoder;
import cn.group.rpc.common.RpcEncoder;
import cn.group.rpc.common.RpcRequest;
import cn.group.rpc.common.RpcResponse;
import cn.group.rpc.registry.ServiceRegistry;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018��6��30��
 * @category ��ܵ�RPC�����������ڽ��û�ϵͳ��ҵ���෢��ΪRPC����
 * 			 ʹ���Ǽ������û�ͨ��spring-bean�ķ�ʽע�뵽�û���ҵ��ϵͳ��
 * 			 ���ڱ���ʵ����ApplicationContextAware InitializitingBean
 * 			spring���챾�����ǻ����setApplicationContext()�������Ӷ������ڷ���
 * 			��ͨ���Զ���ע�����û���ҵ��ӿں�ʵ�ֻ������afterPropertiesSet()������
 * 			�ڷ���������netty������
 */
public class RpcServer implements ApplicationContextAware,InitializingBean{

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
	
	private String serverAddress;
	
	private ServiceRegistry serviceRegistry;
	
	//���ڴ洢ҵ��ӿں�ʵ�����ʵ������(��spring����)
	private Map<String,Object> handlerMap = new HashMap<String,Object>();
	
	public RpcServer(String serverAddress){
		this.serverAddress = serverAddress;
	}
	//�������󶨵ĵ�ַ�Ͷ˿���spring�ڹ��챾���Ǵ������ļ��д���
	public RpcServer(String serverAddress,ServiceRegistry serviceRegistery){
		this.serverAddress = serverAddress;
		//������zookeeperע�����Ʒ���Ĺ�����
		this.serviceRegistry = serviceRegistery;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup,workerGroup)
					 .channel(NioServerSocketChannel.class)
					 .childHandler(new ChannelInitializer<SocketChannel>(){

						@Override
						protected void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline()
									.addLast(new RpcDecoder(RpcRequest.class))//ע�����IN-1
									.addLast(new RpcEncoder(RpcResponse.class))//ע�����OUT
									.addLast(new RpcHandler(handlerMap));//ע��RpcHandler IN-2
						}
						 
					 }).option(ChannelOption.SO_BACKLOG, 128)
					 .childOption(ChannelOption.SO_KEEPALIVE,true);
			
			String[] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);
			/**
			 * ͨ��δ��Ҫôδ��ɣ�Ҫô����ɡ���I/O������ʼʱ��������һ���µ�future����
			 * �µ�δ�������δ��ɵġ�������û�гɹ���ʧ�ܣ�Ҳû��ȡ������ΪI/O������δ��ɡ�
			 * ���I/O�����ɹ��ء�ʧ�ܵػ�ͨ��ȡ����ɣ���δ���������Ϊʹ�ø��������Ϣ��ɣ�
			 * ����ʧ�ܵ�ԭ����ע�⣬��ʹ���Ϻ�ȡ�������������״̬��
			 * 
			 */
			ChannelFuture future = bootstrap.bind(host,port).sync();
			LOGGER.debug("server started on port {}",port);
			
			if(null != serviceRegistry){
				serviceRegistry.register(serverAddress);
			}
			future.channel().closeFuture().sync();
		} finally{
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	/***
	 * @param ctx
	 * @category ͨ��ע�⣬��ȡ��ע��RPC����ע���ҵ����Ľӿں�ʵ������󣬽���ŵ�handlerMap��
	 */
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		Map<String,Object> serviceBeanMap = ctx.
				getBeansWithAnnotation(RpcService.class);
		if(MapUtils.isNotEmpty(serviceBeanMap)){
			for(Object serviceBean : serviceBeanMap.values()){
				//��ҵ��ʵ�����ϵ��Զ���ע���л�ȡ��value���ӻ�ȡ��ҵ��ӿ�ȫ��
				String interfaceName = serviceBean.getClass().
						getAnnotation(RpcService.class).value().getName();
				handlerMap.put(interfaceName, serviceBean);
			}
		}
		
	}

	
}
