package com.epqas.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.common.entity.User;
import com.epqas.auth.mapper.UserMapper;
import com.epqas.auth.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
