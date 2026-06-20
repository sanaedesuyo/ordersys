package com.ordersys.legacy;

import com.ordersys.common.Result;
import com.ordersys.user.entity.User;
import com.ordersys.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 兼容旧版 frontend/ 的用户接口（无鉴权）。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class LegacyUserController {

    private final UserService userService;

    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        return Result.success(userService.createUser(user));
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
}
