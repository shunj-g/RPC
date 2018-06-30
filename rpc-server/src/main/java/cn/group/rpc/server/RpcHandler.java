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
 * @created 2018年6月30日
 * @category 处理具体的业务调度，
 * 			 通过构造函数传入的“业务接口实现”handlerMap,来调用客户端所请求的业务方法
 * 			 并将业务方法返回值封装成response对象写入下一个handler(即编码handler-RpcEncoder)
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
			//根据request来处理具体的业务调用
			Object result = handle(request);
			response.setResult(result);
		} catch (Throwable e) {
		    response.setError(e);//将错误的日志保存
		}
		//写入outbundle(即RpcEncoder)进行下一步处理(编码)后送到channel中给客户端
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		
	}
	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();
		//获取得到实现类对象
		Object serviceBean = handlerMap.get(className);
		//得到要调用的方法名，参数类型，参数值
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		
		//拿到接口类
		Class<?> clazz = Class.forName(className);
		
		//调用实现类对象的指定方法并返回结果
		Method method = clazz.getMethod(methodName, parameterTypes);
		//经典的反射用法
		return method.invoke(serviceBean, parameters);
	}
	public void excetionCaught(ChannelHandlerContext ctx,Throwable cause){
		LOGGER.error("server caught exception",cause);
		ctx.close();
	}
	
}
