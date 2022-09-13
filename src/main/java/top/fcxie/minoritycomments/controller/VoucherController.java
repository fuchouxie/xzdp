package top.fcxie.minoritycomments.controller;

import org.springframework.web.bind.annotation.*;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.Voucher;
import top.fcxie.minoritycomments.service.IVoucherService;

import javax.annotation.Resource;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 优惠券业务控制器
 * @createDate 2022/9/13
 */

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;


    /**
     * 新增普通券
     * @param voucher 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 新增秒杀券
     * @param voucher 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 查询店铺的优惠券列表
     * @param shopId 店铺id
     * @return 优惠券列表
     */
    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        return voucherService.queryVoucherOfShop(shopId);
    }

}
