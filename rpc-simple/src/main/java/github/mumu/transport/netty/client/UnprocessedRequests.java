package github.mumu.transport.netty.client;

import github.mumu.protocol.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mumu
 * @since 2023-02-23
 */
public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future) {
            //如果执行还没结束，就以指定的值作为其执行结果，并触发依赖它的其他阶段执行。
            //所有在 Future.get() 被阻塞的客户端都会得到 result
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
