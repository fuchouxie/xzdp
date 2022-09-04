package top.fcxie.minoritycomments.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fcxie.minoritycomments.dto.UserDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 登陆状态刷新拦截器
 * @createDate 2022/9/4
 */

@Slf4j
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 拦截已登陆的用户进行token刷新，未登录则放行交给登陆拦截器
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.拿到请求头中authorization字段的token
        String token = request.getHeader("authorization");
        //2.校验字段合法性
        if (StrUtil.isBlank(token)) {
            return true;
        }
        //3.根据token在redis中查询用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(RedisConstants.LOGIN_USER_KEY + token);

        //4.判断用户是否存在
        if (userMap.isEmpty()) {
            //5.用户不存在，表示未登录，放行交给登陆拦截器
            return true;
        }
        //6.用户存在，取出用户简要信息
        UserDTO user = BeanUtil.mapToBean(userMap, UserDTO.class, false);
        //7.保存到ThreadLocal
        UserHolder.saveUser(user);
        //8.刷新token存活时间
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY, RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);
        //9.放行
        return true;
    }

    /**
     * 主动释放，避免内存泄露
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
