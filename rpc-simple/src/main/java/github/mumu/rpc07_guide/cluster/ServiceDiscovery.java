package github.mumu.rpc07_guide.cluster;



import github.mumu.rpc07_guide.common.extension.SPI;
import github.mumu.rpc07_guide.protocol.dto.RpcRequest;

import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscovery {
    /**
     * lookup service by rpcServiceName
     *
     * @param rpcRequest rpc service pojo
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
