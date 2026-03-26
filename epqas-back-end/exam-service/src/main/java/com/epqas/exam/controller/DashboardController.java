package com.epqas.exam.controller;

import com.epqas.exam.service.ExaminationPaperService;
import com.epqas.exam.service.ExaminationService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private ExaminationPaperService paperService;

    @Autowired
    private ExaminationService examinationService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("paperCount", paperService.count());
        stats.put("examinationCount", examinationService.count());
        return Result.success(stats);
    }
}
