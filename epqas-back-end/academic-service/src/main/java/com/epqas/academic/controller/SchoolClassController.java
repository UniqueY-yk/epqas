package com.epqas.academic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.academic.entity.SchoolClass;
import com.epqas.academic.service.SchoolClassService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes")
public class SchoolClassController {

    @Autowired
    private SchoolClassService schoolClassService;

    @PostMapping
    public Result<Boolean> createClass(@RequestBody SchoolClass schoolClass) {
        return Result.success(schoolClassService.save(schoolClass));
    }

    @GetMapping("/{id}")
    public Result<SchoolClass> getClassById(@PathVariable Integer id) {
        return Result.success(schoolClassService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateClass(@RequestBody SchoolClass schoolClass) {
        return Result.success(schoolClassService.updateById(schoolClass));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteClass(@PathVariable Integer id) {
        return Result.success(schoolClassService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<SchoolClass>> getClassPage(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(schoolClassService.page(new Page<>(current, size)));
    }
    
    @GetMapping
    public Result<List<SchoolClass>> listClasses() {
        return Result.success(schoolClassService.list());
    }
}
