package github.mumu.rpc07_guide.transport.netty.client;

import github.mumu.rpc07_guide.common.enums.CompressTypeEnum;
import github.mumu.rpc07_guide.common.enums.SerializationTypeEnum;
import github.mumu.rpc07_guide.common.factory.SingletonFactory;
import github.mumu.rpc07_guide.protocol.constants.RpcConstants;
import github.mumu.rpc07_guide.protocol.dto.RpcMessage;
import github.mumu.rpc07_guide.protocol.dto.RpcResponse;
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
                    //这个方法的含义是：如果执行还没结束，就以指定的值作为其执行结果，并触发依赖它的其他阶段执行。
                    //因为我们并没有交给 completableFuture 任何任务，所以其实就是用事件（channelRead）驱动一下异步工作。
                    //这时 invoke 方法中的 completableFuture.get(); 就可以得到 rpcResponse 了。
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        }finally {
            //ReferenceCountUtil.release()其实是ByteBuf.release()方法（从ReferenceCounted接口继承而来）的包装。
            // netty4中的ByteBuf使用了引用计数（netty4实现了一个可选的ByteBuf池），每一个新分配的ByteBuf的引用计数值为1，
            // 每对这个ByteBuf对象增加一个引用，需要调用ByteBuf.retain()方法，而每减少一个引用，需要调用ByteBuf.release()方法。
            // 当这个ByteBuf对象的引用计数值为0时，表示此对象可回收。
            ReferenceCountUtil.release(msg);
        }
    }
    // 如果 IdleStateHandler 检测到了超时事件，则会触发 userEventTriggered 方法
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //判断此时心跳检测传回来的状态
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                //重用连接，避免新建一个tcp长连接
                //向服务端发送心跳检测报文
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
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
