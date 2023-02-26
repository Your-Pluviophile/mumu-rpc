package github.mumu.transport.netty.client;

import github.mumu.cluster.ServiceDiscovery;
import github.mumu.common.enums.CompressTypeEnum;
import github.mumu.common.enums.SerializationTypeEnum;
import github.mumu.common.extension.ExtensionLoader;
import github.mumu.common.factory.SingletonFactory;
import github.mumu.protocol.codec.RpcMessageDecoder;
import github.mumu.protocol.codec.RpcMessageEncoder;
import github.mumu.protocol.constants.RpcConstants;
import github.mumu.protocol.dto.RpcMessage;
import github.mumu.protocol.dto.RpcRequest;
import github.mumu.protocol.dto.RpcResponse;
import github.mumu.transport.RpcRequestTransport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author mumu
 * @since 2023-02-23
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private final ServiceDiscovery serviceDiscovery;
    private final ChannelProvider channelProvider;
    private final UnprocessedRequests unprocessedRequests;

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                //客户端连接前的请求进行handler处理
                //Netty就会以给定的日志级别打印出LoggingHandler中的日志。
                //可以对入站\出站事件进行日志记录，从而方便我们进行问题排查。
                .handler(new LoggingHandler(LogLevel.INFO))
                //设置连接超时参数为5s，超过则连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // 如果15s之内没有发送数据给服务端，就发送一次心跳请求,发送一个心跳信息过去，以防止服务端关闭ctx
                        p.addLast(new IdleStateHandler(0, 15, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // build return value
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // get server address
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        // get  server address related channel
        Channel channel = getChannel(inetSocketAddress);
        //于上述的服务端心跳处理器，在触发读超时后会关闭通信管道，这导致客户端缓存的连接状态会出现不可用的情况，
        // 为了让客户端一直只能取到可用连接就需要对从缓存中获取到的连接做状态判断，如果可用直接返回，
        // 如果不可用则将连接从可用列表中删除然后取下一个可用连接。
        if (channel.isActive()) {
            // put unprocessed request
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.HESSIAN.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        //如果有可用的连接就复用
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            //如果没有就新建一个
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                //如果执行还没结束，就以指定的值作为其执行结果，并触发依赖它的其他阶段执行
                //complete用来告知CompletableFuture任务完成。会立即get()到
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }
}
