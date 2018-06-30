package cn.group.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018��6��30��
 * @category RPC����
 */
@SuppressWarnings("rawtypes")
public class RpcEncoder extends MessageToByteEncoder{

	private Class<?> genericClass;
	
	public RpcEncoder(Class<?> genericClass){
		this.genericClass = genericClass;
	}
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
			throws Exception {
		//�������л�
		if(genericClass.isInstance(msg)){
			byte[] data = SerializationUtil.serialize(msg);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
		
	}

}
