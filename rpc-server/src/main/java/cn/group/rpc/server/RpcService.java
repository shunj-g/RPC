package cn.group.rpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018��6��30��
 * @category RPC����ע�⣨��ע�ڷ���ʵ�������棩
 */
@Target({ElementType.TYPE})//ע�����ڽӿ���
@Retention(RetentionPolicy.RUNTIME)//JVM����������Ҳ����ע�ͣ���˿���ͨ��������ƶ�ȡע�����Ϣ
@Component
public @interface RpcService {
	Class<?> value();
}
