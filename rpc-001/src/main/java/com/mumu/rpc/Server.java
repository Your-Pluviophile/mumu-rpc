package com.mumu.rpc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static boolean running = true;
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8838);
        while(running){//持续监听8838端口
            Socket s = serverSocket.accept();
            process(s);
            s.close();
        }
    }

    private static void process(Socket s) throws IOException {
        //创建socket的输入输出流
        //data包装流
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        //读取数据
        int id = dis.readInt();
        //查询数据
        User user = new UserServiceImpl().findUserById(id);
        //给客户端返回数据
        dos.writeInt(user.getId());
        dos.writeUTF(user.getName());
        dos.flush();

    }
}
