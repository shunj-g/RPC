package cn.group.sample.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;
/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018��7��1��
 * @category �û�ϵͳ����˵��������
 * ������������springcontext���Ӷ��������е�RpcServer
 * �༴�����û�ϵͳ�����б�ע��RpcServiceע���ҵ�񷢲���RpcServer��
 */
public class RpcBootstrap {

    @SuppressWarnings("resource")
	public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
