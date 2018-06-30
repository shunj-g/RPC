package cn.group.rpc.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;


/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018��6��30��
 * @category �����л�������(����Protostuffʵ��)
 */
public class SerializationUtil {

	private static Map<Class<?>,Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>,Schema<?>>();
	
	private static Objenesis objenesis = new ObjenesisStd(true);
	
	private SerializationUtil(){
	}
	/**
	 * @category 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked"})
	private static <T> Schema<T> getSchema(Class<T> clazz){
		Schema<T> schema = (Schema<T>) cachedSchema.get(clazz);
		if(null == schema){
			schema = RuntimeSchema.createFrom(clazz);
			if(null == schema){
				cachedSchema.put(clazz, schema);
			}
		}
		return schema;
	}
	/**
	 * @category ���л������� -> �ֽ����飩
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> byte[] serialize(T obj){
		Class<T> clazz = (Class<T>) obj.getClass();
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try{
			Schema<T> schema = getSchema(clazz);
			return ProtostuffIOUtil.toByteArray(obj, schema, buffer);//���л�
		} catch (Exception e){
			throw new IllegalStateException(e.getMessage(),e);
		}finally{
			buffer.clear();
		}
	}
	/**
	 * @category �����л����ֽ�����-> ����
	 * @param in
	 * @param clazz
	 * @return message
	 */
	public static<T> T deserialize(byte[] in,Class<T> clazz){
		
		try {
			T message = (T) objenesis.newInstance(clazz);//���л�
			Schema<T> schema = getSchema(clazz);//��ȡ���schema
			//ʹ�ø�����ģʽ����Ϣ���ֽ�����ϲ�
			ProtostuffIOUtil.mergeFrom(in, message, schema);
			return message;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(),e);
		}
		
	}
}
