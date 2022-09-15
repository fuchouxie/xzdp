package top.fcxie.minoritycomments.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.fcxie.minoritycomments.dto.LoginFormDTO;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.dto.UserDTO;
import top.fcxie.minoritycomments.entity.User;
import top.fcxie.minoritycomments.mapper.UserMapper;
import top.fcxie.minoritycomments.service.IUserService;
import top.fcxie.minoritycomments.utils.RedisConstants;
import top.fcxie.minoritycomments.utils.RegexUtils;
import top.fcxie.minoritycomments.utils.SystemConstants;
import top.fcxie.minoritycomments.utils.UserHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 用户服务实现类
 * @createDate 2022/9/3
 */

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        //1.校验手机号格式
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.不合法，返回错误信息
            return Result.fail("手机号格式有误");
        }
        //3.合法，生成验证码
        String code = RandomUtil.randomNumbers(6);
        //4.保存验证码到Redis并设置过期时间
        stringRedisTemplate.opsForValue().set(
                RedisConstants.LOGIN_CODE_KEY + phone,
                code,
                RedisConstants.LOGIN_CODE_TTL,
                TimeUnit.MINUTES);
        //5.发送验证码
        log.debug("发送验证码成功,验证码为:{}", code);
        //6.返回结果
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        //1.校验手机格式
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式有误");
        }
        //2.校验验证码
        String code = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        if(code == null || ! code.equals(loginForm.getCode())){
            return Result.fail("验证码有误");
        }
        //3.尝试通过手机号获取用户信息
        User user = query().eq("phone", phone).one();
        //4.判断是否存在
        if (user == null) {
            //5.新用户，注册用户
            user = createUserWithPhone(phone);
        }
        //6.为用户生成登陆凭证并将简要信息保存至redis
        //6.1.生成token
        String token = UUID.randomUUID().toString(true);
        //6.2.将对象转为适应redis hash结构的格式
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", userDTO.getId().toString());
        userMap.put("nickName", userDTO.getNickName());
        userMap.put("icon", userDTO.getIcon());
        //6.3.保存到redis
        stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, userMap);
        //6.4.设置有效期
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);
        //7.返回登陆凭证
        return Result.ok(token);
    }

    @Override
    public Result getUser(Long userId) {
        //1.查询用户信息
        User user = getById(userId);
        //2.封装简要信息
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //3.返回
        return Result.ok(userDTO);
    }

    @Override
    public Result logout(HttpServletRequest request) {
        //1.拿到请求头中authorization字段的token
        String token = request.getHeader("authorization");
        //2.清空登陆状态
        String key = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.delete(key);
        //3.返回结果
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        //1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));//设置随机用户名
        //2.写入数据库
        save(user);
        //3.返回
        return user;
    }
}
