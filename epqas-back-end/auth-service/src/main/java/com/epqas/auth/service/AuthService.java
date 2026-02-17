package com.epqas.auth.service;

import com.epqas.common.entity.User;
import com.epqas.common.result.Result;

public interface AuthService {
    Result<String> login(String username, String password, Integer roleId);

    Result<String> register(User user);
}
