package com.ordersys.user.controller;

import com.ordersys.common.Result;
import com.ordersys.user.entity.User;
import com.ordersys.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/user")
@RequiredArgsConstructor
public class ClientUserController {

    private final UserService userService;

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public Result<User> me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.getById(userId));
    }
}
