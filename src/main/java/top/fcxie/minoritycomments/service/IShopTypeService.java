package top.fcxie.minoritycomments.service;


import com.baomidou.mybatisplus.extension.service.IService;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.ShopType;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 店铺类别服务层接口
 * @createDate 2022/9/4
 */

public interface IShopTypeService extends IService<ShopType> {

    Result lists();

}
