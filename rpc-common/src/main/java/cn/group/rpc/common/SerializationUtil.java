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
 * @created 2018年6月30日
 * @category 发序列化工具类(基于Protostuff实现)
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
	 * @category 序列化（对象 -> 字节数组）
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> byte[] serialize(T obj){
		Class<T> clazz = (Class<T>) obj.getClass();
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try{
			Schema<T> schema = getSchema(clazz);
			return ProtostuffIOUtil.toByteArray(obj, schema, buffer);//序列化
		} catch (Exception e){
			throw new IllegalStateException(e.getMessage(),e);
		}finally{
			buffer.clear();
		}
	}
	/**
	 * @category 发序列化（字节数组-> 对象）
	 * @param in
	 * @param clazz
	 * @return message
	 */
	public static<T> T deserialize(byte[] in,Class<T> clazz){
		
		try {
			T message = (T) objenesis.newInstance(clazz);//序列化
			Schema<T> schema = getSchema(clazz);//获取类的schema
			//使用给定的模式将消息与字节数组合并
			ProtostuffIOUtil.mergeFrom(in, message, schema);
			return message;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(),e);
		}
		
	}
}
