package github.mumu.transport.netty.server;

import github.mumu.config.RpcServiceConfig;

/**
 * 暴露服务
 * @author mumu
 * @since 2023-02-24
 */
public interface ServiceProvider {

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * @param rpcServiceName rpc service name
     * @return service object
     */
    Object getService(String rpcServiceName);

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void publishService(RpcServiceConfig rpcServiceConfig);

}