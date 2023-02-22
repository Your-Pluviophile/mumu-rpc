package github.mumu.rpc01_bio.service.impl;

import github.mumu.rpc01_bio.common.User;
import github.mumu.rpc01_bio.service.UserService;

import java.util.Random;
import java.util.UUID;

/**
 * @author mumu
 * @since 2023-02-21
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUserByUserId(Integer id) {
        System.out.println("客户端查询了"+id+"的用户");
        // 模拟从数据库中取用户的行为
        Random random = new Random();
        User user = User.builder().userName(UUID.randomUUID().toString())
                .id(id)
                .sex(random.nextBoolean()).build();
        return user;
    }
}