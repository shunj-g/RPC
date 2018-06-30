package cn.group.sample.server;

import cn.group.rpc.server.RpcService;
import cn.group.sample.client.HelloService;
import cn.group.sample.client.Person;
/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018年7月1日
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    public String hello(String name) {
    	System.out.println("已经调用服务端接口实现，业务处理结果为：");
    	System.out.println("Hello! " + name);
        return "Hello! " + name;
    }

    public String hello(Person person) {
    	System.out.println("已经调用服务端接口实现，业务处理为：");
    	System.out.println("Hello! " + person.getFirstName() + " " + person.getLastName());
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
