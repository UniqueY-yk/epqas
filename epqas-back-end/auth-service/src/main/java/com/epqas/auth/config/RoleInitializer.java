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

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking default roles...");

        List<String> defaultRoles = Arrays.asList("Administrator", "Question Setter", "Course Instructor", "Student");

        for (String roleName : defaultRoles) {
            QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("role_name", roleName);
            if (roleMapper.selectCount(queryWrapper) == 0) {
                Role role = new Role();
                role.setRoleName(roleName);
                if ("Administrator".equals(roleName)) {
                    role.setDescription("System maintenance and global access");
                } else if ("Question Setter".equals(roleName)) {
                    role.setDescription("Responsible for designing questions and papers");
                } else if ("Course Instructor".equals(roleName)) {
                    role.setDescription("Classroom teacher, views reports");
                } else {
                    role.setDescription("Takes exams and views own grades");
                }
                roleMapper.insert(role);
                log.info("Initialized role: {}", roleName);
            }
        }
    }
}
