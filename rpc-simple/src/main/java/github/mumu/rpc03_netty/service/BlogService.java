package github.mumu.rpc03_netty.service;


import com.ganghuan.myRPCVersion3.common.Blog;

public interface BlogService {
    Blog getBlogById(Integer id);
}
