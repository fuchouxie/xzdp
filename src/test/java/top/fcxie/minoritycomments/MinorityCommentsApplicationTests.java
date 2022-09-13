package top.fcxie.minoritycomments;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.fcxie.minoritycomments.entity.Voucher;
import top.fcxie.minoritycomments.mapper.VoucherMapper;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class MinorityCommentsApplicationTests {

    @Resource
    private VoucherMapper voucherMapper;

    @Test
    void contextLoads() {
    }

}
