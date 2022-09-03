package top.fcxie.minoritycomments;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("top.fcxie.minoritycomments.mapper")
@SpringBootApplication
public class MinorityCommentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinorityCommentsApplication.class, args);
    }

}
