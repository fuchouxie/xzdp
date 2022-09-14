package top.fcxie.minoritycomments.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.entity.Blog;
/**
 * @version V1.0
 * @author fuchouxie
 * @description: 笔记服务层接口
 * @createDate 2022/9/14
 */

public interface IBlogService extends IService<Blog> {
    Result saveBlog(Blog blog);

    Result queryHotBlog(Integer current);

    Result queryBlogById(Long id);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long id);
}
