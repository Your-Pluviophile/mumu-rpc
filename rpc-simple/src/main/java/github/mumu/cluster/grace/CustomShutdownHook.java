package github.mumu.cluster.grace;

import github.mumu.cluster.zk.util.CuratorUtils;
import github.mumu.common.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import github.mumu.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * 优雅下线
 * @author mumu
 * @since 2023-02-24
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        //RunTime.getRunTime().addShutdownHook的作用就是在JVM销毁前执行的一个线程
        //在jvm中增加一个关闭的钩子，当jvm关闭的时候，会执行系统中已经设置的所有通过方法addShutdownHook添加的钩子，
        // 当系统执行完这些钩子后，jvm才会关闭。所以这些钩子可以在jvm关闭的时候进行内存清理、对象销毁等操作。
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress =
                        new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                //在zookeeper上清除节点信息
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException ignored) {
            }
            //关闭线程池
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }
}
