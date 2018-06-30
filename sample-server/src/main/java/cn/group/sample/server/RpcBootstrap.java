package cn.group.sample.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;
/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018年7月1日
 * @category 用户系统服务端的启动入口
 * 其意义是启动springcontext，从而构造框架中的RpcServer
 * 亦即：将用户系统中所有标注了RpcService注解的业务发布到RpcServer中
 */
public class RpcBootstrap {

    @SuppressWarnings("resource")
	public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
