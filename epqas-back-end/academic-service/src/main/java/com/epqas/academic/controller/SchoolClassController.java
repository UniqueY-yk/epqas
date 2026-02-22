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

    @GetMapping
    public Result<Page<SchoolClass>> listClasses(@RequestHeader("X-Role-Id") Integer roleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String className) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(schoolClassService.getClassesPage(page, size, className));
    }

    @PostMapping
    public Result<Boolean> createClass(@RequestHeader("X-Role-Id") Integer roleId,
            @RequestBody SchoolClass schoolClass) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(schoolClassService.save(schoolClass));
    }

    @PutMapping
    public Result<Boolean> updateClass(@RequestHeader("X-Role-Id") Integer roleId,
            @RequestBody SchoolClass schoolClass) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(schoolClassService.updateById(schoolClass));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteClass(@RequestHeader("X-Role-Id") Integer roleId, @PathVariable Integer id) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(schoolClassService.removeById(id));
    }
}
