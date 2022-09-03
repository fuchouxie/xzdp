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
}
