package top.fcxie.minoritycomments.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.dto.UserDTO;
import top.fcxie.minoritycomments.entity.Follow;
import top.fcxie.minoritycomments.mapper.FollowMapper;
import top.fcxie.minoritycomments.service.IFollowService;
import top.fcxie.minoritycomments.service.IUserService;
import top.fcxie.minoritycomments.utils.RedisConstants;
import top.fcxie.minoritycomments.utils.UserHolder;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @version V1.0
 * @author fuchouxie
 * @description: 用户关注服务层实现类
 * @createDate 2022/9/15
 */

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        String key = RedisConstants.FOLLOW_USER_KEY + userId;
        //2.判断是关注还是取关
        if (isFollow) {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            //3.关注，添加关联表记录
            boolean isSuccess = save(follow);
            //添加redis记录
            if (isSuccess) {
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        }else {
            //4.取关，删除关联表记录
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));

            //删除redis记录
            if (isSuccess) {
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        //1.查询当前用户
        Long userId = UserHolder.getUser().getId();
        //2.在redis中查询
        String key = RedisConstants.FOLLOW_USER_KEY + userId;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, followUserId.toString());
        //3.根据结果返回
        return Result.ok(BooleanUtil.isTrue(isMember));
    }

    @Override
    public Result followCommons(Long followUserId) {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        String key1 = RedisConstants.FOLLOW_USER_KEY + userId;
        String key2 = RedisConstants.FOLLOW_USER_KEY + followUserId;
        //2.在redis中求两个用户的交集
        Set<String> commons = stringRedisTemplate.opsForSet().intersect(key1, key2);
        //3.判断是否存在交集
        if(commons == null || commons.isEmpty()){
            return Result.ok();
        }
        //4.构造用户集合
        List<Long> ids = commons.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> users = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        //5.返回
        return Result.ok(users);
    }
}
