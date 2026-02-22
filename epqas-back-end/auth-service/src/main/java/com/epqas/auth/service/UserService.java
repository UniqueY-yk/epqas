package com.epqas.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.common.entity.User;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface UserService extends IService<User> {
    Page<User> getUsersPage(Integer page, Integer size, String username);

    boolean createUser(User user);

    boolean updateUser(User user);
}
