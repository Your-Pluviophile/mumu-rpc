package github.mumu.rpc02_Proxy.client;

import github.mumu.rpc02_Proxy.common.RPCRequest;
import github.mumu.rpc02_Proxy.common.RPCResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author mumu
 * @since 2023-02-22
 */
@AllArgsConstructor
public class ClientProxy implements InvocationHandler {
    // 传入参数Service接口的class对象，反射封装成一个request
    private String host;
    private int port;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //反射构造请求
        RPCRequest request = RPCRequest.builder().interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsTypes(method.getParameterTypes()).build();
        // 把数据传输（JDK的socket）封装到底层
        RPCResponse response = IOClient.sendRequest(host, port, request);
        return response.getData();
    }
    <T> T getProxy(Class<T> clazz){
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},this);
        return (T)o;
    }
}
