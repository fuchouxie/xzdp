package top.fcxie.minoritycomments.controller;

import org.springframework.web.bind.annotation.*;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.service.IFollowService;

import javax.annotation.Resource;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 用户关注控制器
 * @createDate 2022/9/15
 */

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    /**
     * 关注or取关 用户
     * @param id
     * @param isFollow
     * @return
     */
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long id, @PathVariable("isFollow") Boolean isFollow){
        return followService.follow(id, isFollow);
    }

    /**
     * 查看当前用户是否关注目标用户
     * @param id
     * @return
     */
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long id){
        return followService.isFollow(id);
    }

    /**
     * 查看共同关注
     * @param id
     * @return
     */
    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long id){
        return followService.followCommons(id);
    }

}
