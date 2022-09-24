package top.fcxie.minoritycomments.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
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

    @Resource
    private RedissonClient redissonClient;


    /**
     * 优惠券秒杀
     * @param voucherId
     * @return
     */
    @Override
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
        //4.使用redis作分布式锁进行互斥(保险1)
        Long userId = UserHolder.getUser().getId();
        //4.1 尝试获取锁
        RLock lock = redissonClient.getLock("order" + userId);
        boolean isLock = lock.tryLock();
        //4.2 判断是否获得锁
        if(!isLock){
            //4.3 未获得锁，说明已有其他线程执行往下的逻辑，返回失败
            return Result.fail("不可重复下单");
        }
        //5.获得锁后，通过获取代理对象执行接下来的逻辑
        /**
         * 问：为什么需要通过代理对象来执行创建秒杀订单逻辑？
         * 答：
         * 1、因为创建秒杀订单涉及两个主要操作：扣减库存、创建订单，对于我们的秒杀业务，我们当然
         * 希望这两个操作是原子性的，即两个操作只能同时成功或同时失败，不然数据库就会出现数据不一致的问题。
         * 2、根据第1点的需求，显然我们可以使用事务来达成这一目标，但问题就出现在这里。
         * 我们的事务是交由Spring去实现的，Spring采用AOP来实现事务功能，其底层采用动态代理模式，为我们的创建订单方法进行了增强，
         * 因此如果我们只是通过调用当前对象的创建订单此时就会出现经典的事务失效问题。
         * 3、解决事务失效问题的核心就是拿到代理对象，通过代理对象来执行生成秒杀订单逻辑。
         */
        try {
            //5.1 获取带有事务的代理对象
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            //8.返回 通过代理对象进行调用
            return proxy.createVoucherOrder(voucherId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 创建秒杀订单
     * @param voucherId
     * @return
     */
    @Transactional
    public Result createVoucherOrder(Long voucherId){
        //4.确保一人一单
        Long userId = UserHolder.getUser().getId();
        int count = query()
                .eq("user_id", userId)
                .eq("voucher_id", voucherId)
                .count();
        if (count > 0) {
            return Result.fail("你已拥有该优惠券了哦");
        }

        //5.CAS扣减库存(保险2)
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

    /**
     * 模拟存在一人多单的秒杀
     * @param voucherId
     * @return
     */
    @Transactional
    public Result seckillVoucher1(Long voucherId) {
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
        Long userId = UserHolder.getUser().getId();
        //5.扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
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

    /**
     * 模拟存在超卖问题的秒杀
     * @param voucherId
     * @return
     */
    @Transactional
    public Result seckillVoucher2(Long voucherId) {
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
        int count = query()
                .eq("user_id", userId)
                .eq("voucher_id", voucherId)
                .count();
        if (count > 0) {
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
