package top.fcxie.minoritycomments.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.VoucherOrder;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 优惠券订单服务层接口
 * @createDate 2022/9/13
 */

public interface IVoucherOrderService extends IService<VoucherOrder> {
    Result seckillVoucher(Long voucherId);

    Result createVoucherOrder(Long voucherId);
}
