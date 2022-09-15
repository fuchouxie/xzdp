package top.fcxie.minoritycomments.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.fcxie.minoritycomments.dto.LoginFormDTO;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 用户服务层接口
 * @createDate 2022/9/3
 */

public interface IUserService extends IService<User> {

    Result sendCode(String phone);

    Result login(LoginFormDTO loginForm);

    Result getUser(Long userId);

    Result logout(HttpServletRequest request);
}
