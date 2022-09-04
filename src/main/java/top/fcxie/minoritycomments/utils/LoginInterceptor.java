package top.fcxie.minoritycomments.utils;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fcxie.minoritycomments.dto.UserDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 登录拦截器
 * @createDate 2022/9/4
 */

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取当前用户
        UserDTO user = UserHolder.getUser();
        //2.判断用户是否存在
        if (user == null) {
            //3.不存在，进行拦截
            response.setStatus(401);
            return false;
        }
        //4.存在，放行
        return true;
    }

}
