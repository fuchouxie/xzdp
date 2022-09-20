package top.fcxie.minoritycomments;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("top.fcxie.minoritycomments.mapper")
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class MinorityCommentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinorityCommentsApplication.class, args);
    }

}
