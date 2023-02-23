package github.mumu.rpc07_guide.cluster.loadbalance;


import github.mumu.rpc07_guide.common.extension.SPI;
import github.mumu.rpc07_guide.protocol.dto.RpcRequest;

import java.util.List;


@SPI
public interface LoadBalance {
    /**
     * Choose one from the list of existing service addresses list
     *
     * @param serviceUrlList Service address list
     * @param rpcRequest
     * @return target service address
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
