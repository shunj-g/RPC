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
 * @created 2018年6月30日
 * @category 框架的RPC服务器（用于将用户系统的业务类发布为RPC服务）
 * 			 使用是即可由用户通过spring-bean的方式注入到用户的业务系统中
 * 			 由于本类实现了ApplicationContextAware InitializitingBean
 * 			spring构造本对象是会调用setApplicationContext()方法，从而可以在方法
 * 			中通过自定义注解获得用户的业务接口和实现还会调用afterPropertiesSet()方法，
 * 			在方法中启动netty服务器
 */
public class RpcServer implements ApplicationContextAware,InitializingBean{

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
	
	private String serverAddress;
	
	private ServiceRegistry serviceRegistry;
	
	//用于存储业务接口和实现类的实例对象(由spring构造)
	private Map<String,Object> handlerMap = new HashMap<String,Object>();
	
	public RpcServer(String serverAddress){
		this.serverAddress = serverAddress;
	}
	//服务器绑定的地址和端口有spring在构造本类是从配置文件中传入
	public RpcServer(String serverAddress,ServiceRegistry serviceRegistery){
		this.serverAddress = serverAddress;
		//用于向zookeeper注册名称服务的工具类
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
									.addLast(new RpcDecoder(RpcRequest.class))//注册解码IN-1
									.addLast(new RpcEncoder(RpcResponse.class))//注册编码OUT
									.addLast(new RpcHandler(handlerMap));//注册RpcHandler IN-2
						}
						 
					 }).option(ChannelOption.SO_BACKLOG, 128)
					 .childOption(ChannelOption.SO_KEEPALIVE,true);
			
			String[] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);
			/**
			 * 通道未来要么未完成，要么已完成。当I/O操作开始时，将创建一个新的future对象。
			 * 新的未来最初是未完成的——它既没有成功、失败，也没有取消，因为I/O操作尚未完成。
			 * 如果I/O操作成功地、失败地或通过取消完成，则未来将被标记为使用更具体的信息完成，
			 * 例如失败的原因。请注意，即使故障和取消都属于已完成状态。
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
	 * @category 通过注解，获取标注了RPC服务注解的业务类的接口和实现类对象，将其放到handlerMap中
	 */
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		Map<String,Object> serviceBeanMap = ctx.
				getBeansWithAnnotation(RpcService.class);
		if(MapUtils.isNotEmpty(serviceBeanMap)){
			for(Object serviceBean : serviceBeanMap.values()){
				//从业务实现类上的自定义注解中获取到value，从获取到业务接口全名
				String interfaceName = serviceBean.getClass().
						getAnnotation(RpcService.class).value().getName();
				handlerMap.put(interfaceName, serviceBean);
			}
		}
		
	}

	
}
