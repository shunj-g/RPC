package cn.group.rpc.common;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @time 2018��6��30��
 * @category RPC�Ľ�����
 */
public class RpcDecoder extends ByteToMessageDecoder{

	private Class<?> genericClass;//һ����
	
	//���캯�����뷴���л���class
	public RpcDecoder(Class<?> genericClass){
		this.genericClass = genericClass;
	}
	
	@Override
	protected void decode(
			ChannelHandlerContext ctx,//������ͨ�����������ṩ�ĸ��ַ���֮һ��֪ͨͬһ�ܵ�������Ĵ������. 
			ByteBuf in,//
			List<Object> out//
			) throws Exception {
		/**
		 * �������ж�
		 */
		if(in.readableBytes() < 4){//�ܵ�readInt()��Ӱ��
			return;
		}
		//�ڴ˻������б�ǵ�ǰ��ȡ������������ͨ������resetReaderIndex()
		//����ǰreaderIndex�ض�λ����ǵ�readerIndex����ǵ�readerIndex
		//�ĳ�ʼֵΪ0��
		in.markReaderIndex();//
		//readInt():IndexOutOfBoundsException��������⡣�ɶ����ֽ�С��4
		int dataLength = in.readInt();
		if(dataLength < 0){
			ctx.close();
		}
		if(in.readableBytes() < dataLength){
			/* IndexOutOfBoundsException����
			 * �����ǰ��writerIndexС�ڱ�ǵ�readerIndex�� 
			 */
			in.resetReaderIndex();
		}
		/**
		 * ��ByteBufת��Ϊbyte[]
		 */
		byte[] data = new byte[dataLength];
		in.readByte();
		/**
		 * ��dataת��ΪObject
		 */
		Object obj = SerializationUtil.deserialize(data,genericClass);
		out.add(obj);
	}
	
}
