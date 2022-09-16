package top.fcxie.minoritycomments.utils;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: Redis key常量
 * @createDate 2022/9/3
 */

public class RedisConstants {
    //验证码
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    //登陆信息
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 86400L;
    //店铺类别
    public static final String CACHE_SHOP_TYPE_KEY  = "cache:shopList";
    //店铺缓存
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final Long CACHE_NULL_TTL = 2L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    //feed流
    public static final String FEED_KEY = "feed:";
    //经纬度
    public static final String SHOP_GEO_KEY = "shop:geo:";
    //博客点赞
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    //用户关注
    public static final String FOLLOW_USER_KEY = "follows:";
}
