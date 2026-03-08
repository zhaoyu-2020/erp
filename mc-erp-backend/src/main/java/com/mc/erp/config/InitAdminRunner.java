package com.mc.erp.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.erp.entity.User;
import com.mc.erp.entity.Role;
import java.util.List;
import com.mc.erp.service.UserService;
import com.mc.erp.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitAdminRunner implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, "admin");
        User admin = userService.getOne(queryWrapper);
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword("123456"); // Storing plaintext as requested for simplicity
            admin.setRealName("Admin");
            userService.save(admin);
            System.out.println("====== Admin User Initialized ======");
        }

        // Ensure admin role exists and assign it to the admin user
        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(Role::getRoleCode, "admin");
        Role adminRole = roleService.getOne(roleWrapper);
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setRoleCode("admin");
            adminRole.setRoleName("超级管理员");
            adminRole.setDescription("系统最高权限");
            roleService.save(adminRole);
            System.out.println("====== Admin Role Initialized ======");
        }

        userService.updateUserRoles(admin.getId(), List.of(adminRole.getId()));
    }
}
