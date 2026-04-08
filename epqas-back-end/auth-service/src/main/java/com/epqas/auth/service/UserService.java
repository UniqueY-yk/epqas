package com.epqas.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.common.entity.User;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface UserService extends IService<User> {
    /**
     * 分页查询用户
     * 
     * @param page     当前页
     * @param size     每页数量
     * @param username 用户名
     * @return 分页用户
     */
    Page<User> getUsersPage(Integer page, Integer size, String username);

    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 用户ID
     */
    Long createUser(User user);

    /**
     * 更新用户
     * 
     * @param user 用户信息
     * @return 是否更新成功
     */
    boolean updateUser(User user);
}
