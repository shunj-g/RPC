package cn.group.rpc.registry;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018年6月30日
 * @category ZK的配置常量定义
 */
public class Constant {

	public static final int ZK_SESSION_TIMEOUT = 5000;//zk超时时间
	public static final String ZK_REGISTRY_PATH = "/Registry";
	public static final String ZK_DATA_PATH = ZK_REGISTRY_PATH +"/data";
}
