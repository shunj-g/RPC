package cn.group.rpc.common;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @time 2018年6月30日
 * @category RPC的解码器
 */
public class RpcDecoder extends ByteToMessageDecoder{

	private Class<?> genericClass;//一般类
	
	//构造函数传入反序列化的class
	public RpcDecoder(Class<?> genericClass){
		this.genericClass = genericClass;
	}
	
	@Override
	protected void decode(
			ChannelHandlerContext ctx,//您可以通过调用这里提供的各种方法之一来通知同一管道中最近的处理程序. 
			ByteBuf in,//
			List<Object> out//
			) throws Exception {
		/**
		 * 缓冲流判断
		 */
		if(in.readableBytes() < 4){//受到readInt()的影响
			return;
		}
		//在此缓冲区中标记当前读取器索引。可以通过调用resetReaderIndex()
		//将当前readerIndex重定位到标记的readerIndex。标记的readerIndex
		//的初始值为0。
		in.markReaderIndex();//
		//readInt():IndexOutOfBoundsException——如果这。可读性字节小于4
		int dataLength = in.readInt();
		if(dataLength < 0){
			ctx.close();
		}
		if(in.readableBytes() < dataLength){
			/* IndexOutOfBoundsException——
			 * 如果当前的writerIndex小于标记的readerIndex。 
			 */
			in.resetReaderIndex();
		}
		/**
		 * 将ByteBuf转化为byte[]
		 */
		byte[] data = new byte[dataLength];
		in.readByte();
		/**
		 * 将data转化为Object
		 */
		Object obj = SerializationUtil.deserialize(data,genericClass);
		out.add(obj);
	}
	
}
