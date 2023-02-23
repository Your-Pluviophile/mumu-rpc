package github.mumu.rpc07_guide.cluster.zk;

import github.mumu.rpc07_guide.cluster.ServiceDiscovery;
import github.mumu.rpc07_guide.cluster.loadbalance.LoadBalance;
import github.mumu.rpc07_guide.cluster.zk.util.CuratorUtils;
import github.mumu.rpc07_guide.common.enums.RpcErrorMessageEnum;
import github.mumu.rpc07_guide.common.exception.RpcException;
import github.mumu.rpc07_guide.common.extension.ExtensionLoader;
import github.mumu.rpc07_guide.common.utils.CollectionUtil;
import github.mumu.rpc07_guide.protocol.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // load balancing
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
