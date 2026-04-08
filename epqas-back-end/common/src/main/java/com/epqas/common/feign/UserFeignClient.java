package com.epqas.common.feign;

import com.epqas.common.entity.User;
import com.epqas.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface UserFeignClient {

    /**
     * 在认证服务中创建新用户。
     * 
     * @param roleId 系统/管理员角色ID（例如1）
     * @param user   要创建的用户
     * @return 包含生成的用户ID的结果
     */
    @PostMapping("/users")
    Result<Long> createUser(@RequestHeader("X-Role-Id") Integer roleId, @RequestBody User user);

    /**
     * 从认证服务通过ID获取现有用户。
     * 
     * @param roleId 系统/管理员角色ID（例如1）
     * @param id     用户ID
     * @return 包含用户信息的列表
     */
    @org.springframework.web.bind.annotation.GetMapping("/users/{id}")
    Result<User> getUserById(@RequestHeader("X-Role-Id") Integer roleId,
            @org.springframework.web.bind.annotation.PathVariable("id") Long id);

    /**
     * 从认证服务获取分页用户列表。
     * 
     * @param roleId 系统/管理员角色ID（例如1）
     * @param page   当前页
     * @param size   每页数量
     * @return 包含用户信息的列表
     */
    @org.springframework.web.bind.annotation.GetMapping("/users")
    Result listUsers(@RequestHeader("X-Role-Id") Integer roleId,
            @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "1") Integer page,
            @org.springframework.web.bind.annotation.RequestParam(value = "size", defaultValue = "100") Integer size);
}
