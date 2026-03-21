package com.epqas.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.common.feign.UserFeignClient;
import com.epqas.common.result.Result;
import com.epqas.exam.dto.ExaminationPaperDTO;
import com.epqas.exam.service.ExaminationPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/papers")
public class ExaminationPaperController {

    @Autowired
    private ExaminationPaperService paperService;

    @Autowired
    private UserFeignClient userFeignClient;

    @PostMapping
    public Result createPaper(@RequestBody ExaminationPaperDTO dto) {
        // Here we could get setterId from the token context in a real scenario
        paperService.createPaperWithQuestions(dto);
        return Result.success("Paper created successfully");
    }

    @PutMapping
    public Result updatePaper(@RequestBody ExaminationPaperDTO dto) {
        paperService.updatePaperWithQuestions(dto);
        return Result.success("Paper updated successfully");
    }

    @GetMapping("/{id}")
    public Result getPaper(@PathVariable("id") Long id) {
        ExaminationPaperDTO dto = paperService.getPaperWithQuestions(id);
        if (dto != null) {
            return Result.success(dto);
        }
        return Result.error("Paper not found");
    }

    @DeleteMapping("/{id}")
    public Result deletePaper(@PathVariable("id") Long id) {
        // Could also physically delete questions mapping, but leaving it simple for now
        paperService.removeById(id);
        return Result.success("Paper deleted successfully");
    }

    @GetMapping("/page")
    public Result getPaperPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<ExaminationPaperDTO> page = paperService.getPaperPage(current, size, courseId, keyword);
        return Result.success(page);
    }

    @GetMapping("/setters")
    public Result getSetters() {
        try {
            Result usersResult = userFeignClient.listUsers(1, 1, 100);
            if (usersResult != null && usersResult.getCode() == 200 && usersResult.getData() != null) {
                // The data is a Page object; extract records and filter by roleId=2
                LinkedHashMap<String, Object> pageData = (LinkedHashMap<String, Object>) usersResult.getData();
                List<LinkedHashMap<String, Object>> records = (List<LinkedHashMap<String, Object>>) pageData.get("records");
                List<LinkedHashMap<String, Object>> setters = new ArrayList<>();
                if (records != null) {
                    for (LinkedHashMap<String, Object> record : records) {
                        Object roleIdObj = record.get("roleId");
                        if (roleIdObj != null && Integer.valueOf(roleIdObj.toString()) == 2) {
                            LinkedHashMap<String, Object> setter = new LinkedHashMap<>();
                            setter.put("userId", record.get("userId"));
                            setter.put("realName", record.get("realName"));
                            setter.put("username", record.get("username"));
                            setters.add(setter);
                        }
                    }
                }
                return Result.success(setters);
            }
            return Result.error("Failed to fetch setters");
        } catch (Exception e) {
            return Result.error("Error fetching setters: " + e.getMessage());
        }
    }
}
