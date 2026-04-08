package com.epqas.auth.service;

import com.epqas.common.entity.User;
import com.epqas.common.result.Result;

import java.util.Map;

public interface AuthService {
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @param roleId   角色ID
     * @return 登录结果
     */
    Result<Map<String, Object>> login(String username, String password, Integer roleId);

    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @return 注册结果
     */
    Result<String> register(User user);
}
