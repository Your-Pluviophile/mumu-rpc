package github.mumu.rpc04_protocol.client;


import com.ganghuan.myRPCVersion4.common.RPCRequest;
import com.ganghuan.myRPCVersion4.common.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request);
}
