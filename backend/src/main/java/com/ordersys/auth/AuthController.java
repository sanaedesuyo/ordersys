package com.ordersys.auth;

import com.ordersys.auth.dto.AuthResponse;
import com.ordersys.auth.dto.LoginRequest;
import com.ordersys.auth.dto.RegisterRequest;
import com.ordersys.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 用户注册 */
    @PostMapping("/api/client/auth/register")
    public Result<AuthResponse> register(@RequestBody RegisterRequest req) {
        try {
            return Result.success(authService.register(req));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 用户登录 */
    @PostMapping("/api/client/auth/login")
    public Result<AuthResponse> clientLogin(@RequestBody LoginRequest req) {
        try {
            return Result.success(authService.login(req, "USER"));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 商家登录 */
    @PostMapping("/api/admin/auth/login")
    public Result<AuthResponse> adminLogin(@RequestBody LoginRequest req) {
        try {
            return Result.success(authService.login(req, "MERCHANT"));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
