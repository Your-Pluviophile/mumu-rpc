package github.mumu.transport;

import github.mumu.common.extension.SPI;
import github.mumu.protocol.dto.RpcRequest;

/**
 * @author mumu
 * @since 2023-02-23
 */
@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server and get result
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
