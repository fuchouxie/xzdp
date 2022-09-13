package top.fcxie.minoritycomments.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.service.IVoucherOrderService;

import javax.annotation.Resource;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 优惠券订单控制器
 * @createDate 2022/9/13
 */

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }

}
