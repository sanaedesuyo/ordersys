package com.ordersys.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ordersys.user.entity.User;
import com.ordersys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    public User createUser(User user) {
        this.baseMapper.insert(user);
        return user;
    }
}
