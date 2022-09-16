package top.fcxie.minoritycomments.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.dto.ScrollResult;
import top.fcxie.minoritycomments.dto.UserDTO;
import top.fcxie.minoritycomments.entity.Blog;
import top.fcxie.minoritycomments.entity.Follow;
import top.fcxie.minoritycomments.entity.User;
import top.fcxie.minoritycomments.mapper.BlogMapper;
import top.fcxie.minoritycomments.service.IBlogService;
import top.fcxie.minoritycomments.service.IFollowService;
import top.fcxie.minoritycomments.service.IUserService;
import top.fcxie.minoritycomments.utils.RedisConstants;
import top.fcxie.minoritycomments.utils.SystemConstants;
import top.fcxie.minoritycomments.utils.UserHolder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 笔记服务层实现类
 * @createDate 2022/9/14
 */

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Resource
    private IFollowService followService;

    @Override
    public Result saveBlog(Blog blog) {
        //1.获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        //2.保存探店博文
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            return Result.fail("新增博客失败");
        }
        //3.查询博客作者的粉丝
        List<Follow> fans = followService.query().eq("follow_user_id", blog.getUserId()).list();
        //4.推送笔记id给所有粉丝
        for(Follow fan : fans){
            //4.1.获取粉丝id
            Long userId = fan.getUserId();
            //4.2.进行推送
            stringRedisTemplate.opsForZSet()
                    .add(RedisConstants.FEED_KEY + userId, blog.getId().toString(), System.currentTimeMillis());
        }
        //5.返回id
        return Result.ok(blog.getId());
    }

    @Override
    public Result queryHotBlog(Integer current) {
        //1.根据点赞数降序进行分页查询
        Page<Blog> page = query().orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        //2.获取笔记列表
        List<Blog> blogs = page.getRecords();
        //3.封装其他信息
        for(Blog blog : blogs){
            //3.1.封装笔记的作者信息
            this.queryBlogUser(blog);
            //3.2.封装当前用户的点赞状态
            this.isBlogLike(blog);
        }
        //5.返回
        return Result.ok(blogs);
    }

    @Override
    public Result queryBlogById(Long id) {
        //1.查询数据库
        Blog blog = getById(id);
        //2.判断博客是否存在
        if(blog == null){
            //3.不存在返回错误信息
            return Result.fail("博客不存在");
        }
        //4.查询blog对应的用户
        queryBlogUser(blog);
        //5.设置当前用户点赞状态
        isBlogLike(blog);
        //5.返回
        return Result.ok(blog);
    }

    @Override
    public Result likeBlog(Long id) {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        //2.查询当前博客
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        //3.判断是否点赞
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        //4.未点赞，可以点赞
        if(score == null){
            //4.1.数据库点赞数递增
            boolean success = update().setSql("liked = liked + 1")
                    .eq("id", id)
                    .update();
            if (success) {
                //4.2.保存用户到redis的博客点赞集合set
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        }else{
            //5.已点赞，撤销点赞
            //5.1.数据库点赞数递减
            boolean success = update().setSql("liked = liked - 1")
                    .eq("id", id)
                    .update();
            if (success) {
                //5.2.把用户从redis的博客点赞集合删除
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        //1.根据博客id查询前五名用户
        Set<String> userIdList = stringRedisTemplate.opsForZSet().range(RedisConstants.BLOG_LIKED_KEY + id.toString(), 0, 4);
        //2.判断是否不为空
        if(userIdList == null || userIdList.isEmpty()){
            return Result.ok();
        }
        //3.根据集合用户id查询用户信息
        List<UserDTO> lists = new LinkedList<>();
        for(String userId : userIdList){
            User user = userService.getById(userId);
            UserDTO userDTO = new UserDTO();
            BeanUtil.copyProperties(user, userDTO);
            lists.add(userDTO);
        }
        //4.返回
        return Result.ok(lists);
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        //2.查询收件箱
        String key = RedisConstants.FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 3);
        //3.非空判断
        if(typedTuples == null || typedTuples.isEmpty()){
            return Result.ok();
        }
        //4.解析数据：blogId、minTime(时间戳)、offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            //4.1.获取id
            ids.add(Long.valueOf(typedTuple.getValue()));
            //4.2.获取分数（时间戳
            long time = typedTuple.getScore().longValue();
            if(time == minTime){
                os++;
            }else{
                minTime = time;
                os = 1;
            }
        }
        //5.根据id查询blog
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query()//查询
                .in("id", ids)//通过in查询
                .last("ORDER BY FIELD(id,"+ idStr +")")//最后一条sql
                .list();//返回集合
        //6.封装博客用户信息以及点赞状态
        for(Blog blog : blogs){
            //6.1.查询blog对应的用户
            queryBlogUser(blog);
            //6.2.设置当前用户点赞状态
            isBlogLike(blog);
        }

        //7.封装结果并返回
        ScrollResult res = new ScrollResult();
        res.setList(blogs);
        res.setMinTime(minTime);
        res.setOffset(os);
        return Result.ok(res);
    }

    /**
     * 查询并封装博客的作者信息
     * @param blog
     */
    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    /**
     * 查询并封装当前用户所访问博客的点赞状态
     * @param blog
     */
    private void isBlogLike(Blog blog){
        UserDTO user = UserHolder.getUser();
        if(user == null){
            blog.setIsLike(false);
            return;
        }
        Long userId = user.getId();
        //查询当前用户点赞状态
        Double score = stringRedisTemplate.opsForZSet()
                .score(RedisConstants.BLOG_LIKED_KEY + blog.getId(), userId.toString());
        blog.setIsLike(score != null);
    }
}
