package com.epqas.auth.controller;

import com.epqas.auth.service.AuthService;
import com.epqas.common.entity.User;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        // User DTO usually better, but reusing Entity for speed as per requirements
        return authService.login(user.getUsername(), user.getPasswordHash(), user.getRoleId());
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        return authService.register(user);
    }
}
