package rpc.stub;

import com.mumu.rpc.IUserService;
import com.mumu.rpc.User;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

public class ClientStub {
    public static Object getStub(Class Clazz){
        InvocationHandler h = new InvocationHandler(){
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket s = new Socket("127.0.0.1", 8888);

                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

                String clazzName = Clazz.getName();
                String methodName = method.getName();
                Class[] parametersTypes = method.getParameterTypes();

                oos.writeUTF(clazzName);
                oos.writeUTF(methodName);
                oos.writeObject(parametersTypes);
                oos.writeObject(args);
                oos.flush();


                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                Object o = ois.readObject();


                oos.close();
                s.close();
                return o;
            }
        };
        Object o = Proxy.newProxyInstance(IUserService.class.getClassLoader(), new Class[]{IUserService.class}, h);
        return o;

    }
}
