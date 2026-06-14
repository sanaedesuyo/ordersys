package com.ordersys.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordersys.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
