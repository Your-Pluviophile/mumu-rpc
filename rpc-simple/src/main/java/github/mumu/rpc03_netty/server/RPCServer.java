package github.mumu.rpc03_netty.server;

public interface RPCServer {
    void start(int port);
    void stop();
}
