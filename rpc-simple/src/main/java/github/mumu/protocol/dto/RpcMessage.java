package github.mumu.protocol.dto;


import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    //消息类型
    private byte messageType;
    //编码与解码方式
    private byte codec;
    //压缩格式
    private byte compress;
    //请求id
    private int requestId;
    //数据 RpcRequest or RpcResponse
    private Object data;

}
