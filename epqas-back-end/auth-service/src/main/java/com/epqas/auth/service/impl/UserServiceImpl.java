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

    /**
     * 分页查询用户
     * 
     * @param page     当前页
     * @param size     每页数量
     * @param username 用户名
     * @return 分页用户
     */
    @Override
    public Page<User> getUsersPage(Integer page, Integer size, String username) {
        Page<User> userPage = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        return this.page(userPage, queryWrapper);
    }

    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 用户ID
     */
    @Override
    public Long createUser(User user) {
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        this.save(user);
        return user.getUserId();
    }

    /**
     * 更新用户
     * 
     * @param user 用户信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateUser(User user) {
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()
                && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return this.updateById(user);
    }
}
