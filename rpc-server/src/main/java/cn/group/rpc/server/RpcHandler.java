package cn.group.rpc.server;

import java.lang.reflect.Method;
import java.util.Map;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.group.rpc.common.RpcRequest;
import cn.group.rpc.common.RpcResponse;


/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018��6��30��
 * @category ��������ҵ����ȣ�
 * 			 ͨ�����캯������ġ�ҵ��ӿ�ʵ�֡�handlerMap,�����ÿͻ����������ҵ�񷽷�
 * 			 ����ҵ�񷽷�����ֵ��װ��response����д����һ��handler(������handler-RpcEncoder)
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest>{

	private static final Logger LOGGER  = LoggerFactory.getLogger(RpcHandler.class);

	private final Map<String,Object> handlerMap;
	
	public RpcHandler(Map<String,Object> handlerMap){
		this.handlerMap = handlerMap;
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request)
			throws Exception {
		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());
		try {
			//����request����������ҵ�����
			Object result = handle(request);
			response.setResult(result);
		} catch (Throwable e) {
		    response.setError(e);//���������־����
		}
		//д��outbundle(��RpcEncoder)������һ������(����)���͵�channel�и��ͻ���
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		
	}
	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();
		//��ȡ�õ�ʵ�������
		Object serviceBean = handlerMap.get(className);
		//�õ�Ҫ���õķ��������������ͣ�����ֵ
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		
		//�õ��ӿ���
		Class<?> clazz = Class.forName(className);
		
		//����ʵ��������ָ�����������ؽ��
		Method method = clazz.getMethod(methodName, parameterTypes);
		//����ķ����÷�
		return method.invoke(serviceBean, parameters);
	}
	public void excetionCaught(ChannelHandlerContext ctx,Throwable cause){
		LOGGER.error("server caught exception",cause);
		ctx.close();
	}
	
}
