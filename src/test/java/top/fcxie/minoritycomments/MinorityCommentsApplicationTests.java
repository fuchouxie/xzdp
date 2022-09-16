package top.fcxie.minoritycomments;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.fcxie.minoritycomments.entity.Shop;
import top.fcxie.minoritycomments.entity.Voucher;
import top.fcxie.minoritycomments.mapper.VoucherMapper;
import top.fcxie.minoritycomments.service.IShopService;
import top.fcxie.minoritycomments.utils.RedisConstants;
import top.fcxie.minoritycomments.utils.RedisIdWorker;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class MinorityCommentsApplicationTests {

    @Autowired
    private IShopService shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }
    @Test
    void testGlobalId() {
        long id = redisIdWorker.nextId("order");
        System.out.println(id);
    }

    @Test
    void loadShopData(){
        //1.根据typeId做分类查询，并保存到集合中
        List<List<Shop>> shops = new ArrayList<>();
        for(int i = 1; i <= 2; i++){
            List<Shop> shopOfType = shopService.query().eq("type_id", i).list();
            shops.add(shopOfType);
        }
        //2.根据分类写入redis
        for (List<Shop> shopList : shops) {
            String key = "";
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(shopList.size());
            //构造GeoLocation
            for (Shop shop : shopList) {
                key = RedisConstants.SHOP_GEO_KEY + shop.getTypeId();
                locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(),
                        new Point(shop.getX(), shop.getY())));
            }
            //同一分类写入redis
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }
}
