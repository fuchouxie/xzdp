package top.fcxie.minoritycomments.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.SeckillVoucher;
import top.fcxie.minoritycomments.entity.Voucher;
import top.fcxie.minoritycomments.mapper.VoucherMapper;
import top.fcxie.minoritycomments.service.ISeckillVoucherService;
import top.fcxie.minoritycomments.service.IVoucherService;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private VoucherMapper voucherMapper;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        List<Voucher> vouchers = voucherMapper.queryVoucherOfShop(shopId);
        return Result.ok(vouchers);
    }

    @Override
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
    }
}
