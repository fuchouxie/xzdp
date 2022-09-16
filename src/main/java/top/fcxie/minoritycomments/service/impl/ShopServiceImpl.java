package top.fcxie.minoritycomments.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.Shop;
import top.fcxie.minoritycomments.mapper.ShopMapper;
import top.fcxie.minoritycomments.service.IShopService;
import top.fcxie.minoritycomments.utils.RedisConstants;
import top.fcxie.minoritycomments.utils.SystemConstants;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 店铺服务层实现类
 * @createDate 2022/9/5
 */

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
        //1.判断是否需要通过经纬度查询
        if(x == null || y == null){
            //2.根据类型分页查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            //返回数据
            return Result.ok(page.getRecords());
        }
        //3.通过经纬度以及分类分页查询
        //3.1.计算分页参数
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        //3.2.查询redis，按照距离排序分页
        String key = RedisConstants.SHOP_GEO_KEY + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                key,
                GeoReference.fromCoordinate(new Point(x, y)),
                new Distance(5000),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
        );
        //4.解析id
        if(results == null){
            return Result.ok();
        }
        //4.1.截取from-end
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        //判断截取后是否为空
        if(list.size() <= from){
            return Result.ok();
        }
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result->{
            //4.2.获取店铺id
            ids.add(Long.valueOf(result.getContent().getName()));
            //4.2.获取店铺id对应距离
            distanceMap.put(result.getContent().getName(), result.getDistance());
        });
        //5.根据id查询shop
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for(Shop shop : shops){
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        //5.返回数据
        return Result.ok(shops);
    }

    /**
     * 根据id查询店铺信息(解决缓存穿透问题)
     * @param id
     * @return
     */
    @Override
    public Result queryById(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        //1.查询redis中店铺的缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.缓存存在，直接返回
        if (!StrUtil.isBlank(json)) {
            return Result.ok(JSONUtil.toBean(json, Shop.class));
        }
        //3.缓存存在，但为空值，则说明是防止缓存穿透的空对象，直接返回
        if (json != null) {
            return Result.ok();
        }
        //4.缓存不存在，查询数据库
        Shop shop = getById(id);
        //5.判断数据库查询结果
        if(shop == null){
            //6.店铺不存在，缓存空对象（解决缓存穿透问题关键
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.ok();
        }
        //7.店铺存在，写入redis缓存
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //8.返回结果
        return Result.ok(shop);
    }

}
