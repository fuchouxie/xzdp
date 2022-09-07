package top.fcxie.minoritycomments.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.Shop;
import top.fcxie.minoritycomments.mapper.ShopMapper;
import top.fcxie.minoritycomments.service.IShopService;
import top.fcxie.minoritycomments.utils.SystemConstants;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 店铺服务层实现类
 * @createDate 2022/9/5
 */

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    /**
     * 根据店铺类型分页查询
     * @param typeId
     * @param current
     * @param x
     * @param y
     * @return
     */
    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        Page<Shop> page = query()
                .eq("type_id", typeId)
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    /**
     * 根据id查询店铺信息
     * @param id
     * @return
     */
    @Override
    public Result queryById(Long id) {
        Shop shop = getById(id);
        return Result.ok(shop);
    }

}
