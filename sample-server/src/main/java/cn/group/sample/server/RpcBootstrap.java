package cn.group.sample.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;
/**
 * �û�ϵͳ����˵��������
 * ������������springcontext���Ӷ��������е�RpcServer
 * �༴�����û�ϵͳ�����б�ע��RpcServiceע���ҵ�񷢲���RpcServer��
 * 
 *
 */
public class RpcBootstrap {

    @SuppressWarnings("resource")
	public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
