package top.fcxie.minoritycomments.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.Follow;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 用户关注服务层接口
 * @createDate 2022/9/15
 */

public interface IFollowService extends IService<Follow> {
    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result followCommons(Long followUserId);
}
