package com.epqas.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.common.entity.Role;
import com.epqas.auth.service.RoleService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping
    public Result<Boolean> createRole(@RequestBody Role role) {
        return Result.success(roleService.save(role));
    }

    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable Integer id) {
        return Result.success(roleService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateRole(@RequestBody Role role) {
        return Result.success(roleService.updateById(role));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRole(@PathVariable Integer id) {
        return Result.success(roleService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<Role>> getRolePage(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(roleService.page(new Page<>(current, size)));
    }

    @GetMapping
    public Result<List<Role>> listRoles() {
        return Result.success(roleService.list());
    }
}
