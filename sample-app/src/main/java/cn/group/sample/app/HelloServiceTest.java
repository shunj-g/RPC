package cn.group.sample.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.group.rpc.client.RpcProxy;
import cn.group.sample.client.HelloService;
import cn.group.sample.client.Person;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018��6��30��
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest {
	
	@Autowired
	private RpcProxy rpcProxy;
	
	@Test
	public void helloTest1(){
		// ���ô����create����������HelloService�ӿ�
		HelloService helloService = rpcProxy.create(HelloService.class);
		
		// ���ô���ķ�����ִ��invoke
		String result = helloService.hello("World");
		System.out.println("����˷��ؽ����");
		System.out.println(result);
	}
	@Test
	public void helloTest2() {
		HelloService helloService = rpcProxy.create(HelloService.class);
		String result = helloService.hello(new Person("Yong", "Huang"));
		System.out.println("����˷��ؽ����");
		System.out.println(result);
	}

}
