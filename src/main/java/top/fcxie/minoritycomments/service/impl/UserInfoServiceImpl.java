package top.fcxie.minoritycomments.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.fcxie.minoritycomments.entity.UserInfo;
import top.fcxie.minoritycomments.mapper.UserInfoMapper;
import top.fcxie.minoritycomments.service.IUserInfoService;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
}
