package com.epqas.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epqas.auth.service.UserService;
import com.epqas.common.entity.User;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;

    /**
     * 获取统计数据
     * 
     * @return 统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.count());
        stats.put("adminCount", userService.count(new QueryWrapper<User>().eq("role_id", 1)));
        stats.put("setterCount", userService.count(new QueryWrapper<User>().eq("role_id", 2)));
        return Result.success(stats);
    }
}
