package github.mumu.rpc01_bio.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author mumu
 * @since 2023-02-21
 */
/*
@Builder 注解为类生成相对略微复杂的构建器 API

它作用于类，将其变成建造者模式
可以以链的形式调用
初始化实例对象生成的对象是不可以变的，可以在创建对象的时候进行赋值
如果需要在原来的基础上修改可以加 set 方法，final 字段可以不需要初始化
它会生成一个全参的构造函数
 */
@Builder
@Data
public class User implements Serializable {
    // 客户端和服务端共有的
    private Integer id;
    private String userName;
    private Boolean sex;
}