package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.erp.entity.Role;
import com.mc.erp.entity.User;
import com.mc.erp.entity.UserRole;
import com.mc.erp.mapper.RoleMapper;
import com.mc.erp.mapper.UserMapper;
import com.mc.erp.mapper.UserRoleMapper;
import com.mc.erp.service.RoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RoleMenuService roleMenuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 加载角色，生成 ROLE_xxx 权限（用于 hasRole('admin') 这类粗粒度检查）
        List<Long> roleIds = userRoleMapper
                .selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()))
                .stream().map(UserRole::getRoleId).filter(Objects::nonNull).toList();
        if (!roleIds.isEmpty()) {
            roleMapper.selectList(new LambdaQueryWrapper<Role>().in(Role::getId, roleIds))
                    .forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getRoleCode())));
        }

        // 加载菜单权限字符串（用于 hasAuthority('order:cancel:settled') 这类细粒度检查）
        roleMenuService.getPermissionsByUserId(user.getId())
                .stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
