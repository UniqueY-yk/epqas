package com.epqas.academic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.academic.entity.SchoolClass;
import com.epqas.academic.service.SchoolClassService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes")
public class SchoolClassController {

    @Autowired
    private SchoolClassService schoolClassService;

    /**
     * 获取班级列表
     * 
     * @param roleId    角色ID
     * @param page      页码
     * @param size      每页数量
     * @param className 班级名称
     * @return 班级列表
     */
    @GetMapping
    public Result<Page<SchoolClass>> listClasses(@RequestHeader("X-Role-Id") Integer roleId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "className", required = false) String className) {
        if (roleId == null)
            return Result.error(403, "Access Denied");
        return Result.success(schoolClassService.getClassesPage(page, size, className));
    }

    /**
     * 创建班级
     * 
     * @param roleId      角色ID
     * @param schoolClass 班级信息
     * @return 是否创建成功
     */
    @PostMapping
    public Result<Boolean> createClass(@RequestHeader("X-Role-Id") Integer roleId,
            @RequestBody SchoolClass schoolClass) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(schoolClassService.save(schoolClass));
    }

    /**
     * 更新班级
     * 
     * @param roleId      角色ID
     * @param schoolClass 班级信息
     * @return 是否更新成功
     */
    @PutMapping
    public Result<Boolean> updateClass(@RequestHeader("X-Role-Id") Integer roleId,
            @RequestBody SchoolClass schoolClass) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(schoolClassService.updateById(schoolClass));
    }

    /**
     * 删除班级
     * 
     * @param roleId 角色ID
     * @param id     班级ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteClass(@RequestHeader("X-Role-Id") Integer roleId, @PathVariable("id") Integer id) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(schoolClassService.removeById(id));
    }
}
