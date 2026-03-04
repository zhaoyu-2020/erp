package com.mc.erp.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.erp.entity.User;
import com.mc.erp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitAdminRunner implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, "123456");
        User admin = userService.getOne(queryWrapper);
        if (admin == null) {
            admin = new User();
            admin.setUsername("123456");
            admin.setPassword("123456"); // Storing plaintext as requested for simplicity
            admin.setRealName("Admin");
            userService.save(admin);
            System.out.println("====== Admin User Initialized ======");
        }
    }
}
