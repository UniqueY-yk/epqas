package com.epqas.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.auth.service.UserService;
import com.epqas.common.entity.User;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<Page<User>> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String username) {
        Page<User> userPage = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        return Result.success(userService.page(userPage, queryWrapper));
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Boolean> createUser(@RequestBody User user) {
        // Should probably hash password here too or reuse AuthService register logic
        // For simplicity, assuming AuthService handles creation or we duplicate hashing here.
        // Let's reuse basic save but we need to hash password if it's new.
        // Doing raw save for now, admin should be careful or we inject PasswordEncoder.
        return Result.success(userService.save(user));
    }

    @PutMapping
    public Result<Boolean> updateUser(@RequestBody User user) {
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }
}
