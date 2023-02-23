package github.mumu.rpc07_guide.cluster.loadbalance.loadbalancer;


import github.mumu.rpc07_guide.cluster.loadbalance.AbstractLoadBalance;
import github.mumu.rpc07_guide.protocol.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        //生成调用列表的hashcode
        int identityHashCode = System.identityHashCode(serviceAddresses);
        //获取调用方法名
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // 以调用方法名为key,获取一致性hash选择器
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        //// 若不存在则创建新的选择器
        if (selector == null || selector.identityHashCode != identityHashCode) {
            // 创建ConsistentHashSelector时会生成所有虚拟结点
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            // 获取选择器
            selector = selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    static class ConsistentHashSelector {
        private final TreeMap<Long, String> virtualInvokers;// 虚拟结点

        private final int identityHashCode;// hashCode

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            // 创建TreeMap 来保存结点
            this.virtualInvokers = new TreeMap<>();
            // 生成调用结点HashCode
            this.identityHashCode = identityHashCode;
            // 对每个invoker（服务地址）生成replicaNumber个虚拟结点，并存放于TreeMap中
            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    // 根据md5算法为每4个结点生成一个消息摘要，摘要长为16字节128位。
                    byte[] digest = md5(invoker + i);
                    // 随后将128位分为4部分，0-31,32-63,64-95,95-128，并生成4个32位数，存于long中，long的高32位都为0
                    // 并作为虚拟结点的key。
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        // 选择结点
        public String select(String rpcServiceKey) {
            // 根据这个参数生成消息摘要
            byte[] digest = md5(rpcServiceKey);
            //调用hash(digest, 0)，将消息摘要转换为hashCode，这里仅取0-31位来生成HashCode
            //调用sekectForKey方法选择结点
            return selectForKey(hash(digest, 0));
        }
        //根据hashCode选择结点
        public String selectForKey(long hashCode) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            // 若不存在，那么选择treeMap中第一个结点
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }
    }
}
