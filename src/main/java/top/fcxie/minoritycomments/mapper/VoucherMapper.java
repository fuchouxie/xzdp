package top.fcxie.minoritycomments.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import top.fcxie.minoritycomments.entity.Voucher;

import java.util.List;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 优惠券功能持久层接口
 * @createDate 2022/9/13
 */

public interface VoucherMapper extends BaseMapper<Voucher> {
    /**
     * 根据店铺id查询该店铺的所有优惠券信息
     * @param shopId
     * @return
     */
    @Select("select v.`id`, v.`shop_id`, v.`title`, v.`sub_title`, v.`rules`, v.`pay_value`,\n" +
            "            v.`actual_value`, v.`type`, sv.`stock` , sv.begin_time , sv.end_time " +
            "from tb_voucher v left join tb_seckill_voucher sv " +
            "on v.id = sv.voucher_id " +
            "where v.shop_id = #{shopId} and v.status = 1")
    @Results(id = "voucherMap", value = {
        @Result(id = true, column = "id", property = "id"),
        @Result(column = "shop_id", property = "shopId"),
        @Result(column = "title", property = "title"),
        @Result(column = "rules", property = "rules"),
        @Result(column = "pay_value", property = "payValue"),
        @Result(column = "actual_value", property = "actualValue"),
        @Result(column = "type", property = "type"),
        @Result(column = "begin_time", property = "beginTime"),
        @Result(column = "end_time", property = "endTime")
    })
    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
