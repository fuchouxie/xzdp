package top.fcxie.minoritycomments.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.Shop;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 店铺服务层接口
 * @createDate 2022/9/5
 */

public interface IShopService extends IService<Shop> {

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);

    Result queryById(Long id);
}
