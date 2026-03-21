package com.epqas.auth.service;

import com.epqas.common.entity.User;
import com.epqas.common.result.Result;

import java.util.Map;

public interface AuthService {
    Result<Map<String, Object>> login(String username, String password, Integer roleId);

    Result<String> register(User user);
}
