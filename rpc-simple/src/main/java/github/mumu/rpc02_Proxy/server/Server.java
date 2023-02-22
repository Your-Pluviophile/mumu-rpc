package github.mumu.rpc02_Proxy.server;

import github.mumu.rpc01_bio.common.User;
import github.mumu.rpc01_bio.service.impl.UserServiceImpl;
import github.mumu.rpc02_Proxy.common.RPCRequest;
import github.mumu.rpc02_Proxy.common.RPCResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author mumu
 * @since 2023-02-21
 */
//服务端以BIO的方式监听Socket，如有数据，调用对应服务的实现类执行任务，将结果返回给客户端
//因为在网络传输过程都是request与response格式的数据了，客户端与服务器端就要负责封装与解析以上结构数据
public class Server {
    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();
        try {
            ServerSocket serverSocket = new ServerSocket(8899);
            System.out.println("服务端启动了");
            // BIO的方式监听Socket
            while (true) {
                Socket socket = serverSocket.accept();
                // 每来一个连接，就开启一个线程去处理
                new Thread(() -> {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        // 读取客户端传过来的request
                        RPCRequest request = (RPCRequest) ois.readObject();
                        // 反射调用对应方法
                        Method method = userService.getClass().getMethod(request.getMethodName(), request.getParamsTypes());
                        Object invoke = method.invoke(userService, request.getParams());
                        // 封装，写入response对象
                        oos.writeObject(RPCResponse.success(invoke));
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("从IO中读取数据错误");
                    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        System.out.println("从IO中读取数据错误");
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
        }

    }
}
