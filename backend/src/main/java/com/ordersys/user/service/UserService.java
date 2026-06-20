package com.ordersys.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ordersys.user.dto.ChangePasswordRequest;
import com.ordersys.user.dto.UpdateProfileRequest;
import com.ordersys.user.entity.User;
import com.ordersys.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        this.baseMapper.insert(user);
        return user;
    }

    public User updateProfile(Long userId, UpdateProfileRequest req) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (StringUtils.hasText(req.getName())) {
            user.setName(req.getName().trim());
        } else {
            throw new IllegalArgumentException("请填写姓名");
        }

        if (StringUtils.hasText(req.getPhone())) {
            String phone = req.getPhone().trim();
            Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, phone)
                    .ne(User::getId, userId));
            if (count > 0) {
                throw new IllegalArgumentException("该手机号已被使用");
            }
            user.setPhone(phone);
        } else {
            throw new IllegalArgumentException("请填写手机号");
        }

        updateById(user);
        return user;
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!StringUtils.hasText(req.getCurrentPassword())) {
            throw new IllegalArgumentException("请填写当前密码");
        }
        if (!StringUtils.hasText(req.getNewPassword()) || req.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("新密码至少 6 位");
        }
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("当前密码错误");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        updateById(user);
    }
}
