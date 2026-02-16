package com.epqas.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.auth.entity.User;
import com.epqas.auth.service.UserService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<Boolean> createUser(@RequestBody User user) {
        return Result.success(userService.save(user));
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateUser(@RequestBody User user) {
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<User>> getUserPage(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.page(new Page<>(current, size)));
    }
    
    @GetMapping
    public Result<List<User>> listUsers() {
        return Result.success(userService.list());
    }
}
