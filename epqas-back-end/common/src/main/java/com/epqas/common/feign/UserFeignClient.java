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
     * Creates a new user in the auth service.
     * Overrides the X-Role-Id header to simulate a system/admin call.
     * 
     * @param roleId System/Admin role id (e.g. 1)
     * @param user   The user to create
     * @return Result containing generated user ID
     */
    @PostMapping("/users")
    Result<Long> createUser(@RequestHeader("X-Role-Id") Integer roleId, @RequestBody User user);

    /**
     * Fetches an existing user from the auth service by ID.
     */
    @org.springframework.web.bind.annotation.GetMapping("/users/{id}")
    Result<User> getUserById(@RequestHeader("X-Role-Id") Integer roleId, @org.springframework.web.bind.annotation.PathVariable("id") Long id);
}
