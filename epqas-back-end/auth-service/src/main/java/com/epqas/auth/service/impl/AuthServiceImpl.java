package com.epqas.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epqas.auth.mapper.UserMapper;
import com.epqas.auth.service.AuthService;
import com.epqas.common.entity.User;
import com.epqas.common.result.Result;
import com.epqas.common.utils.JwtUtils;
import com.epqas.common.entity.Role;
import com.epqas.auth.mapper.RoleMapper;
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

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Result<Map<String, Object>> login(String username, String password, Integer roleId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            return Result.error("User not found");
        }

        if (roleId != null && !roleId.equals(user.getRoleId())) {
            return Result.error("Identity mismatch. Please select the correct role.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return Result.error("Invalid password");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            return Result.error("Account is inactive");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("roleId", user.getRoleId());

        String token = jwtUtils.generateToken(username, claims);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("realName", user.getRealName());
        return Result.success(result);
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
            Role studentRole = roleMapper.selectOne(new QueryWrapper<Role>().eq("role_name", "Student"));
            if (studentRole != null) {
                user.setRoleId(studentRole.getRoleId());
            } else {
                throw new RuntimeException("Default role 'Student' not found");
            }
        }

        userMapper.insert(user);
        return Result.success("User registered successfully");
    }
}
