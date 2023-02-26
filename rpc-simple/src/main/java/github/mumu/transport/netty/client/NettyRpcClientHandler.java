package github.mumu.transport.netty.client;

import github.mumu.common.enums.CompressTypeEnum;
import github.mumu.common.enums.SerializationTypeEnum;
import github.mumu.common.factory.SingletonFactory;
import github.mumu.protocol.constants.RpcConstants;
import github.mumu.protocol.dto.RpcMessage;
import github.mumu.protocol.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 真正处理业务逻辑的 NettyClientHandler
 * 覆写 channelRead、userEventTriggered、exceptionCaught三个方法。
 * channelRead ：处理服务端返回的数据，交由 CompletableFuture返回。
 * userEventTriggered：对Netty心跳检测事件进行处理，几秒没有写操作的话，发送一个心跳信息过去，以防止服务端关闭ctx。
 * exceptionCaught：处理异常。
 *
 */

/**
 * @author mumu
 * @since 2023-02-23
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    //channelRead()方法将会在收到消息时被调用
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcMessage){
                RpcMessage tmp = (RpcMessage) msg;
                byte messageType = tmp.getMessageType();
                //如果返回的是心跳检测的消息类型
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", tmp.getData());
                    //如果是正常的消息回复
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        }finally {
            //引用计数值为0时，表示此对象可回收。
            ReferenceCountUtil.release(msg);
        }
    }
    // 如果 IdleStateHandler 检测到了超时事件，则会触发 userEventTriggered 方法
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //判断检测到的信息
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                //重用连接，避免新建一个tcp长连接
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                //向服务端发送一个ping报文（说一下自己还活着）
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
