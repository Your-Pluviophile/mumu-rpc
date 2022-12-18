package rpc;


import com.mumu.rpc.User;
import rpc.stub.ClientStub;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws Exception {
        ClientStub clientStub = new ClientStub();
        System.out.println(clientStub.findUserById(123));
    }
}
