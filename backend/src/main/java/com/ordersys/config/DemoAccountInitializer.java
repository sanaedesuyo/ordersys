package com.ordersys.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ordersys.user.entity.User;
import com.ordersys.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时确保演示账号密码可用（BCrypt 哈希与当前 PasswordEncoder 一致）。
 * 若数据库尚未迁移 role/password 字段，则跳过并打印提示。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoAccountInitializer implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "password123";
    private static final List<String> DEMO_PHONES = List.of(
            "13800138001", "13800138002", "18888888888"
    );

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String hash = passwordEncoder.encode(DEMO_PASSWORD);
            for (String phone : DEMO_PHONES) {
                userMapper.update(null, new LambdaUpdateWrapper<User>()
                        .eq(User::getPhone, phone)
                        .set(User::getPassword, hash));
            }

            Long merchantCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, "18888888888"));
            if (merchantCount == 0) {
                User merchant = new User();
                merchant.setName("店长");
                merchant.setPhone("18888888888");
                merchant.setAddress("本店");
                merchant.setRole("MERCHANT");
                merchant.setPassword(hash);
                userMapper.insert(merchant);
            }
            log.info("演示账号密码已同步（password123）");
        } catch (Exception e) {
            log.warn("演示账号初始化跳过：{}。请执行 docker/mysql/migrate-auth.sql 迁移数据库。", e.getMessage());
        }
    }
}
