package com.epqas.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epqas.auth.mapper.RoleMapper;
import com.epqas.common.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class RoleInitializer implements CommandLineRunner {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 初始化默认角色
     * 
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("Checking default roles...");

        List<String> defaultRoles = Arrays.asList("系统管理员", "命题教师", "任课教师", "学生");

        for (String roleName : defaultRoles) {
            QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("role_name", roleName);
            if (roleMapper.selectCount(queryWrapper) == 0) {
                Role role = new Role();
                role.setRoleName(roleName);
                if ("系统管理员".equals(roleName)) {
                    role.setDescription("系统维护与全局访问权限");
                } else if ("命题教师".equals(roleName)) {
                    role.setDescription("负责设计试题和试卷");
                } else if ("任课教师".equals(roleName)) {
                    role.setDescription("班级教师，查看报告");
                } else {
                    role.setDescription("参加考试并查看自己的成绩");
                }
                roleMapper.insert(role);
                log.info("Initialized role: {}", roleName);
            }
        }
    }
}
