package com.ordersys.user.controller;

import com.ordersys.common.Result;
import com.ordersys.user.dto.ChangePasswordRequest;
import com.ordersys.user.dto.UpdateProfileRequest;
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

    /** 更新当前用户基本信息 */
    @PutMapping("/me")
    public Result<User> updateProfile(@RequestBody UpdateProfileRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        try {
            return Result.success(userService.updateProfile(userId, req));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 修改当前用户密码 */
    @PutMapping("/me/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        try {
            userService.changePassword(userId, req);
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
