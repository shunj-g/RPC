package cn.group.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.group.rpc.common.RpcDecoder;
import cn.group.rpc.common.RpcEncoder;
import cn.group.rpc.common.RpcRequest;
import cn.group.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018年6月30日
 * @category 框架的RPC 客户端（用于发送RPC请求）
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse>{

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RpcClient.class);
	
	private String host;
	private int port;
	
	private RpcResponse response;
	
	private final Object obj = new Object();
	
	public RpcClient(String host,int port){
		this.host = host;
		this.port = port;
	}

	/**
	 * 
	 * @param request
	 * @return
	 * @category 连接服务端 发送消息
	 * @throws Exception
	 * 
	 */
	public RpcResponse send(RpcRequest request) throws Exception{
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
					 .handler(new ChannelInitializer<SocketChannel>(){

						@Override
						protected void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline()
							       .addLast(new RpcEncoder(RpcRequest.class))//OUT - 1
							       .addLast(new RpcDecoder(RpcResponse.class))//IN - 1
							       .addLast(RpcClient.this);//IN - 2
						}
					 }).option(ChannelOption.SO_KEEPALIVE,true);
			//连接服务器
			ChannelFuture future = bootstrap.connect(host,port).sync();
			//将request对象写入outBoundle处理后发出（即RpcEncode编码）
			future.channel().writeAndFlush(request).sync();
			
			//用线程等待的方式决定是否关闭连接
			//其意义是：先在次阻塞，等待获取到服务端的返回，被唤醒，从而关闭网络连接
			synchronized(obj){
				obj.wait();
			}
			if(null != response){
				future.channel().closeFuture().sync();
			}
			return response;
		}finally {
			group.shutdownGracefully();
		}
	}

	/**
	 * 读取服务端的返回结果
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response)
			throws Exception {
		this.response = response;
		/**
		 * 被唤醒的线程将无法继续进行，直到当前线程释放该对象上的锁。
		 * 觉醒的线程将以通常的方式与可能正在积极竞争的任何其他线程竞争，
		 * 以便在此对象上同步;例如，觉醒的线程作为下一个锁这个对象的线程
		 * 没有任何可靠的特权或缺点。
		 */
		synchronized(obj){
			obj.notifyAll();
		}
	}
	

	/**
	 * 
	 */
	public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause)
			throws Exception{
		LOGGER.error("client caught exception",cause);
		ctx.close();
	}
}
