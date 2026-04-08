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

    /**
     * 分页查询用户
     * 
     * @param roleId   角色ID
     * @param page     当前页
     * @param size     每页数量
     * @param username 用户名
     * @return 分页用户
     */
    @GetMapping
    public Result<Page<User>> listUsers(@RequestHeader("X-Role-Id") Integer roleId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "username", required = false) String username) {
        if (roleId == null || roleId == 4)
            return Result.error(403, "Access Denied");
        return Result.success(userService.getUsersPage(page, size, username));
    }

    /**
     * 根据ID获取用户
     * 
     * @param roleId 角色ID
     * @param id     用户ID
     * @return 用户
     */
    @GetMapping("/{id}")
    public Result<User> getUser(@RequestHeader("X-Role-Id") Integer roleId, @PathVariable("id") Long id) {
        if (roleId == null || roleId == 4)
            return Result.error(403, "Access Denied");
        return Result.success(userService.getById(id));
    }

    /**
     * 创建用户
     * 
     * @param roleId 角色ID
     * @param user   用户信息
     * @return 用户ID
     */
    @PostMapping
    public Result<Long> createUser(@RequestHeader("X-Role-Id") Integer roleId, @RequestBody User user) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(userService.createUser(user));
    }

    /**
     * 更新用户
     * 
     * @param roleId 角色ID
     * @param user   用户信息
     * @return 是否更新成功
     */
    @PutMapping
    public Result<Boolean> updateUser(@RequestHeader("X-Role-Id") Integer roleId, @RequestBody User user) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(userService.updateUser(user));
    }

    /**
     * 删除用户
     * 
     * @param roleId 角色ID
     * @param id     用户ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@RequestHeader("X-Role-Id") Integer roleId, @PathVariable("id") Long id) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(userService.removeById(id));
    }
}
