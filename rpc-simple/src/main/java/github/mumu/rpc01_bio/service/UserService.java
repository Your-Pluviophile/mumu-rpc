package github.mumu.rpc01_bio.service;

import github.mumu.rpc01_bio.common.User;

/**
 * @author mumu
 * @since 2023-02-21
 */
public interface UserService {
    // 客户端通过这个接口调用服务端的实现类
    User getUserByUserId(Integer id);
}

