package com.ordersys.user.controller;

import com.ordersys.common.Result;
import com.ordersys.user.entity.User;
import com.ordersys.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

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
