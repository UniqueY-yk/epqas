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

    /**
     * 创建试卷
     * 
     * @param dto 试卷信息
     * @return 创建结果
     */
    @PostMapping
    public Result createPaper(@RequestBody ExaminationPaperDTO dto) {
        // 在实际场景中，可以从令牌上下文中获取 setterId
        paperService.createPaperWithQuestions(dto);
        return Result.success("Paper created successfully");
    }

    /**
     * 更新试卷
     * 
     * @param dto 试卷信息
     * @return 更新结果
     */
    @PutMapping
    public Result updatePaper(@RequestBody ExaminationPaperDTO dto) {
        paperService.updatePaperWithQuestions(dto);
        return Result.success("Paper updated successfully");
    }

    /**
     * 获取试卷详情
     * 
     * @param id 试卷ID
     * @return 试卷详情
     */
    @GetMapping("/{id}")
    public Result getPaper(@PathVariable("id") Long id) {
        ExaminationPaperDTO dto = paperService.getPaperWithQuestions(id);
        if (dto != null) {
            return Result.success(dto);
        }
        return Result.error("Paper not found");
    }

    /**
     * 删除试卷
     * 
     * @param id 试卷ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deletePaper(@PathVariable("id") Long id) {
        // 也可以物理删除问题映射，但暂时保持简单
        paperService.removeById(id);
        return Result.success("Paper deleted successfully");
    }

    /**
     * 获取试卷分页数据
     * 
     * @param current  当前页
     * @param size     每页数量
     * @param courseId 课程ID
     * @param keyword  关键词
     * @return 试卷分页数据
     */
    @GetMapping("/page")
    public Result getPaperPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<ExaminationPaperDTO> page = paperService.getPaperPage(current, size, courseId, keyword);
        return Result.success(page);
    }

    /**
     * 获取试卷出题人列表
     * 
     * @return 出题人列表
     */
    @GetMapping("/setters")
    public Result getSetters() {
        try {
            Result usersResult = userFeignClient.listUsers(1, 1, 100);
            if (usersResult != null && usersResult.getCode() == 200 && usersResult.getData() != null) {
                // 数据是一个 Page 对象；提取记录并按 roleId=2 过滤
                LinkedHashMap<String, Object> pageData = (LinkedHashMap<String, Object>) usersResult.getData();
                List<LinkedHashMap<String, Object>> records = (List<LinkedHashMap<String, Object>>) pageData
                        .get("records");
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
