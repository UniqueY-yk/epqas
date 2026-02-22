package com.epqas.auth.controller;

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
    public Result<Page<User>> listUsers(@RequestHeader("X-Role-Id") Integer roleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(userService.getUsersPage(page, size, username));
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@RequestHeader("X-Role-Id") Integer roleId, @PathVariable Long id) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Boolean> createUser(@RequestHeader("X-Role-Id") Integer roleId, @RequestBody User user) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(userService.createUser(user));
    }

    @PutMapping
    public Result<Boolean> updateUser(@RequestHeader("X-Role-Id") Integer roleId, @RequestBody User user) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(userService.updateUser(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@RequestHeader("X-Role-Id") Integer roleId, @PathVariable Long id) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(userService.removeById(id));
    }
}
