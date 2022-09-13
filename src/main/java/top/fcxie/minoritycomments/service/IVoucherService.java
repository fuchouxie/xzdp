package top.fcxie.minoritycomments.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.Voucher;

public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
