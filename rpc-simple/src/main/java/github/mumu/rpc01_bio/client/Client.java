package github.mumu.rpc01_bio.client;


import github.mumu.rpc01_bio.common.User;
import lombok.val;

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
        try {
            Socket socket = new Socket("127.0.0.1", 8899);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            //传给服务器id
            objectOutputStream.writeInt(new Random().nextInt());
            objectOutputStream.flush();
            //读取服务器传回的数据
            User user = (User)objectInputStream.readObject();
            System.out.println("服务端返回的User:"+user);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("客户端启动失败");
        }
        //暂不写关闭逻辑
    }
}
