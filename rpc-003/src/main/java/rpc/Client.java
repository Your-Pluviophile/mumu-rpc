package rpc;


import com.mumu.rpc.IUserService;
import rpc.stub.ClientStub;

public class Client {
    public static void main(String[] args) throws Exception {
        IUserService service = (IUserService) ClientStub.getStub(IUserService.class);
        System.out.println(service.findUserById(123));
    }
}
