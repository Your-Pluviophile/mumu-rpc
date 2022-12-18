package com.mumu.rpc;

import com.mumu.rpc.User;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws Exception {
        //创建socket通信
        Socket s = new Socket("127.0.0.1", 8888);
        //给服务端写入数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(123);
        s.getOutputStream().write(baos.toByteArray());
        s.getOutputStream().flush();
        //接受服务端返回的数据
        DataInputStream dis = new DataInputStream(s.getInputStream());
        int id = dis.readInt();
        String name = dis.readUTF();
        User user = new User(id, name);

        System.out.println(user);

        dos.close();
        s.close();
    }
}
