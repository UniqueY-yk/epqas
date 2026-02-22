package com.epqas.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.common.entity.User;
import com.epqas.auth.mapper.UserMapper;
import com.epqas.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<User> getUsersPage(Integer page, Integer size, String username) {
        Page<User> userPage = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        return this.page(userPage, queryWrapper);
    }

    @Override
    public Long createUser(User user) {
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        this.save(user); // Mybatis plus automatically sets the ID on the entity
        return user.getUserId();
    }

    @Override
    public boolean updateUser(User user) {
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()
                && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return this.updateById(user);
    }
}
