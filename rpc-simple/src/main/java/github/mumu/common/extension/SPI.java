package github.mumu.common.extension;

import java.lang.annotation.*;

//四个元注解，用于注解我们自定义的类
@Documented //注解是否将包含在JavaDoc中
@Retention(RetentionPolicy.RUNTIME)//什么时候使用该注解
@Target(ElementType.TYPE)//注解用于什么地方
public @interface SPI {
}
