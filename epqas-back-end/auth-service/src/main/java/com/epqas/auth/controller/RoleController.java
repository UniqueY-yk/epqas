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

    /**
     * 创建角色
     * 
     * @param role 角色信息
     * @return 是否创建成功
     */
    @PostMapping
    public Result<Boolean> createRole(@RequestBody Role role) {
        return Result.success(roleService.save(role));
    }

    /**
     * 根据ID获取角色
     * 
     * @param id 角色ID
     * @return 角色
     */
    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable("id") Integer id) {
        return Result.success(roleService.getById(id));
    }

    /**
     * 更新角色
     * 
     * @param role 角色信息
     * @return 是否更新成功
     */
    @PutMapping
    public Result<Boolean> updateRole(@RequestBody Role role) {
        return Result.success(roleService.updateById(role));
    }

    /**
     * 删除角色
     * 
     * @param id 角色ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRole(@PathVariable("id") Integer id) {
        if (id != null && id <= 4) {
            return Result.error(400, "System default roles cannot be deleted.");
        }
        try {
            boolean success = roleService.removeById(id);
            if (!success) {
                return Result.error(404, "Role not found.");
            }
            return Result.success(true);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return Result.error(400, "Cannot delete role: it is currently assigned to one or more users.");
        } catch (Exception e) {
            return Result.error(500, "Failed to delete role.");
        }
    }

    /**
     * 分页查询角色
     * 
     * @param current 当前页
     * @param size    每页数量
     * @return 分页角色
     */
    @GetMapping("/page")
    public Result<Page<Role>> getRolePage(@RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(roleService.page(new Page<>(current, size)));
    }

    /**
     * 查询所有角色
     * 
     * @return 所有角色
     */
    @GetMapping
    public Result<List<Role>> listRoles() {
        return Result.success(roleService.list());
    }
}
