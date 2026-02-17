package com.epqas.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epqas.auth.mapper.UserMapper;
import com.epqas.auth.service.AuthService;
import com.epqas.common.entity.User;
import com.epqas.common.result.Result;
import com.epqas.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Result<String> login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            return Result.error("User not found");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return Result.error("Invalid password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return Result.error("Account is inactive");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("roleId", user.getRoleId());
        
        String token = jwtUtils.generateToken(username, claims);
        return Result.success(token);
    }

    @Override
    public Result<String> register(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (userMapper.exists(queryWrapper)) {
            return Result.error("Username already exists");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        // Default role? Let's say 4 (Student) for now, or require it
        if (user.getRoleId() == null) {
            user.setRoleId(4); // Default to student
        }
        
        userMapper.insert(user);
        return Result.success("User registered successfully");
    }
}
