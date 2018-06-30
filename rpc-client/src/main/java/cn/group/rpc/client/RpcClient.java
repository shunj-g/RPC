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
 * @created 2018��6��30��
 * @category ��ܵ�RPC �ͻ��ˣ����ڷ���RPC����
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
	 * @category ���ӷ���� ������Ϣ
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
			//���ӷ�����
			ChannelFuture future = bootstrap.connect(host,port).sync();
			//��request����д��outBoundle����󷢳�����RpcEncode���룩
			future.channel().writeAndFlush(request).sync();
			
			//���̵߳ȴ��ķ�ʽ�����Ƿ�ر�����
			//�������ǣ����ڴ��������ȴ���ȡ������˵ķ��أ������ѣ��Ӷ��ر���������
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
	 * ��ȡ����˵ķ��ؽ��
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response)
			throws Exception {
		this.response = response;
		/**
		 * �����ѵ��߳̽��޷��������У�ֱ����ǰ�߳��ͷŸö����ϵ�����
		 * ���ѵ��߳̽���ͨ���ķ�ʽ��������ڻ����������κ������߳̾�����
		 * �Ա��ڴ˶�����ͬ��;���磬���ѵ��߳���Ϊ��һ�������������߳�
		 * û���κοɿ�����Ȩ��ȱ�㡣
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
