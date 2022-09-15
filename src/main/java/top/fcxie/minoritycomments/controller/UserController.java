package top.fcxie.minoritycomments.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.fcxie.minoritycomments.dto.LoginFormDTO;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.dto.UserDTO;
import top.fcxie.minoritycomments.entity.UserInfo;
import top.fcxie.minoritycomments.service.IUserInfoService;
import top.fcxie.minoritycomments.service.IUserService;
import top.fcxie.minoritycomments.utils.UserHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

    @Resource
    private IUserInfoService userInfoService;

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


    /**
     * 登出功能
     * @return
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request){
        // TODO 实现登出功能
        return userService.logout(request);
    }

    /**
     * 获取当前用户
     * @return
     */
    @GetMapping("/me")
    public Result me(){
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    /**
     * 查看用户详情
     * @param userId
     * @return
     */
    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    /**
     * 查看指定用户的信息
     * @param userId
     * @return
     */
    @GetMapping("{id}")
    public Result user(@PathVariable("id") Long userId){
        return userService.getUser(userId);
    }

}
