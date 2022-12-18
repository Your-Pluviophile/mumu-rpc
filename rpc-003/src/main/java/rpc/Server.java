package rpc;

import com.mumu.rpc.IUserService;
import com.mumu.rpc.User;

import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static boolean running = true;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8838);
        while (running) {//持续监听8838端口
            Socket s = serverSocket.accept();
            process(s);
            s.close();
        }
    }

    private static void process(Socket s) throws Exception{
        //创建socket的输入输出流
        //包装流
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        //动态代理
        String claszzName = ois.readUTF();
        String methodName = ois.readUTF();
        Class[] parameterTypes = (Class[]) ois.readObject();
        Object[] args = (Object[]) ois.readObject();

        Class clazz = null;
        //从服务注册列表找到具体的类 -> Spring注入
        clazz = UserServiceImpl.class;

        Method method = clazz.getMethod(methodName, parameterTypes);
        Object o = (Object) method.invoke(clazz.newInstance(), args);

        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        oos.writeObject(o);
        oos.flush();

    }
}
