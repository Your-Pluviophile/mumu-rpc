package com.mumu.rpc;

import com.mumu.rpc.IUserService;
import com.mumu.rpc.User;

public class UserServiceImpl implements IUserService {

    @Override
    public User findUserById(Integer id) {
        return new User(id,"carl");
    }
}
