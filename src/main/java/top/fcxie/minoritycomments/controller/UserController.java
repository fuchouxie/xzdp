package top.fcxie.minoritycomments.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.fcxie.minoritycomments.dto.LoginFormDTO;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.dto.UserDTO;
import top.fcxie.minoritycomments.service.IUserService;
import top.fcxie.minoritycomments.utils.UserHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 用户控制器
 * @createDate 2022/9/3
 */

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    /**
     * 发送验证码
     * @param phone 手机号码
     */
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone){
        return userService.sendCode(phone);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm){
        return userService.login(loginForm);
    }

    @GetMapping("/me")
    public Result me(){
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }


}
