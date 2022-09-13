package top.fcxie.minoritycomments.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.dto.UserDTO;
import top.fcxie.minoritycomments.entity.SeckillVoucher;
import top.fcxie.minoritycomments.entity.VoucherOrder;
import top.fcxie.minoritycomments.mapper.VoucherOrderMapper;
import top.fcxie.minoritycomments.service.ISeckillVoucherService;
import top.fcxie.minoritycomments.service.IVoucherOrderService;
import top.fcxie.minoritycomments.service.IVoucherService;
import top.fcxie.minoritycomments.utils.RedisIdWorker;
import top.fcxie.minoritycomments.utils.UserHolder;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 优惠券订单服务层实现类
 * @createDate 2022/9/13
 */

@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Override
    @Transactional
    public Result seckillVoucher(Long voucherId) {
        //1.查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        //2.判断秒杀时间
        //2.1 判断开始时间
        LocalDateTime now = LocalDateTime.now();
        if(voucher.getBeginTime().isAfter(now)){
            return Result.fail("秒杀活动未开始");
        }
        //2.2 判断结束时间
        if(voucher.getEndTime().isBefore(now)){
            return Result.fail("秒杀活动已结束");
        }
        //3.判断库存
        if(voucher.getStock() < 1){
            return Result.fail("库存不足");
        }
        //4.确保一人一单
        Long userId = UserHolder.getUser().getId();
        int count = query().eq("user_id", userId)
                .eq("voucher_id", voucherId)
                .count();
        if(count > 0){
            return Result.fail("你已拥有该优惠券了哦");
        }
        //5.扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!success) {
            return Result.fail("库存不足！");
        }
        //6.创建订单
        VoucherOrder order = new VoucherOrder();
        //6.1.订单id
        long orderId = redisIdWorker.nextId("order");
        order.setId(orderId);
        //6.2.用户id
        order.setUserId(userId);
        //6.3.代金券id
        order.setVoucherId(voucherId);
        //7.写回数据库
        save(order);
        return Result.ok(orderId);
    }

}
