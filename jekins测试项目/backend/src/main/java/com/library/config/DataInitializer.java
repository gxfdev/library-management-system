package com.library.config;

import com.library.entity.User;
import com.library.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureAdminPassword();
        log.info("数据初始化完成");
    }

    private void ensureAdminPassword() {
        List<User> admins = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, "admin")
                        .last("LIMIT 1"));
        if (admins.isEmpty()) return;

        User admin = admins.get(0);
        String correctHash = passwordEncoder.encode("admin123");
        if (!passwordEncoder.matches("admin123", admin.getPassword())) {
            admin.setPassword(correctHash);
            userMapper.updateById(admin);
            log.info("管理员密码已修正为: admin123");
        }
    }
}
