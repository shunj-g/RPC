package cn.group.rpc.common;

/**
 * 
 * @author shunj-g
 * @version 1.0.0
 * @created 2018年6月30日
 * @category 封装RPC响应 封装相应Object
 */
public class RpcResponse {

	private String requestId;
	private Throwable error;
	private Object result;
	

    public boolean isError() {
        return error != null;
    }
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public Throwable getError() {
		return error;
	}
	public void setError(Throwable error) {
		this.error = error;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	
	
}
