package com.ordersys.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordersys.auth.dto.AuthResponse;
import com.ordersys.auth.dto.LoginRequest;
import com.ordersys.auth.dto.RegisterRequest;
import com.ordersys.user.entity.User;
import com.ordersys.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest req) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (count > 0) throw new IllegalArgumentException("手机号已注册");

        User user = new User();
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userMapper.insert(user);

        String token = jwtUtil.generate(user.getId(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getName(), user.getRole());
    }

    public AuthResponse login(LoginRequest req, String requiredRole) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("手机号或密码错误");
        }
        if (!user.getRole().equals(requiredRole)) {
            throw new IllegalArgumentException("账号角色不匹配");
        }
        String token = jwtUtil.generate(user.getId(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getName(), user.getRole());
    }
}
