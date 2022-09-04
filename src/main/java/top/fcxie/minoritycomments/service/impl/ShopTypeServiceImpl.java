package top.fcxie.minoritycomments.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.ShopType;
import top.fcxie.minoritycomments.mapper.ShopTypeMapper;
import top.fcxie.minoritycomments.service.IShopTypeService;
import top.fcxie.minoritycomments.utils.RedisConstants;

import javax.annotation.Resource;
import java.util.List;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 店铺类别服务实现类
 * @createDate 2022/9/4
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询店铺类别列表
     * @return
     */
    @Override
    public Result lists() {
        //1.在redis中查询店铺类别列表
        String json = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_TYPE_KEY);
        //2.判断是否存在
        if (!StrUtil.isBlank(json)) {
            //3.存在，直接返回
            return Result.ok(JSONUtil.toList(json, ShopType.class));
        }
        //4.不存在，进行数据库查询
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        //5.判断是否存在
        if (shopTypes == null) {
            //6.不存在返回错误
            return Result.fail("商户类型不存在");
        }
        //7.将查询结果缓存至redis
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopTypes));
        //8.返回结果
        return Result.ok(shopTypes);
    }
}
