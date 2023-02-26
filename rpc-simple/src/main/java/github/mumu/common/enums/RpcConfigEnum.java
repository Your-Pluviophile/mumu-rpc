package github.mumu.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mumu
 * @since 2023-02-26
 */

@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;

}
