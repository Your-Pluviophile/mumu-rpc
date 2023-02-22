package github.mumu.rpc01_bio.server;

import github.mumu.rpc01_bio.common.User;
import github.mumu.rpc01_bio.service.impl.UserServiceImpl;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author mumu
 * @since 2023-02-21
 */
//服务端以BIO的方式监听Socket，如有数据，调用对应服务的实现类执行任务，将结果返回给客户端
public class Server {
    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();
        try {
            ServerSocket serverSocket = new ServerSocket(8899);
            System.out.println("服务端启动了");
            // BIO的方式监听Socket
            while (true) {
                Socket socket = serverSocket.accept();
                // 没来一个连接，就开启一个线程去处理
                new Thread(() -> {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        // 读取客户端传过来的id
                        Integer id = ois.readInt();
                        User userByUserId = userService.getUserByUserId(id);
                        // 写入User对象给客户端
                        oos.writeObject(userByUserId);
                        oos.flush();
                    } catch (IOException e) {
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
