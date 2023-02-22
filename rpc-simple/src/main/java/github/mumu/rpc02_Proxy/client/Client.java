package github.mumu.rpc02_Proxy.client;


import github.mumu.rpc01_bio.common.User;
import github.mumu.rpc02_Proxy.service.UserService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * @author mumu
 * @since 2023-02-21
 */
//客户端建立Socket连接，传输Id给服务端，得到返回的User对象
public class Client {
    public static void main(String[] args) {
        //生成客户端的动态代理构造类，可以根据我们传入的类动态生成代理类
        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 8899);
        UserService proxy = clientProxy.getProxy(UserService.class);
        // 服务的方法1,每当此代理对象调用任意方法时，都会调用invoke()
        User userByUserId = proxy.getUserByUserId(10);
        System.out.println("从服务端得到的user为：" + userByUserId);
        // 服务的方法2
        User user = User.builder().userName("张三").id(100).sex(true).build();
        Integer integer = proxy.insertUserId(user);
        System.out.println("向服务端插入数据："+integer);
    }
}
