package com.mc.erp.service.impl;

import com.mc.erp.dto.ChangePasswordDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.config.JwtUtil;
import com.mc.erp.dto.UserQuery;
import com.mc.erp.entity.User;
import com.mc.erp.entity.UserRole;
import com.mc.erp.entity.Role;
import com.mc.erp.mapper.RoleMapper;
import com.mc.erp.mapper.UserMapper;
import com.mc.erp.mapper.UserRoleMapper;
import com.mc.erp.service.UserService;
import com.mc.erp.vo.UserVO;
import com.mc.erp.vo.UserWithRoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public PageResult<UserVO> getPage(UserQuery query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getUsername()), User::getUsername, query.getUsername());
        wrapper.like(StringUtils.hasText(query.getRealName()), User::getRealName, query.getRealName());
        wrapper.like(StringUtils.hasText(query.getPhone()), User::getPhone, query.getPhone());
        wrapper.like(StringUtils.hasText(query.getEmail()), User::getEmail, query.getEmail());
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> resultPage = this.page(page, wrapper);

        List<User> users = resultPage.getRecords();
        List<Long> userIds = users.stream().map(User::getId).filter(Objects::nonNull).toList();
        final Map<Long, List<Long>> roleIdsMap;
        if (userIds.isEmpty()) {
            roleIdsMap = Map.of();
        } else {
            var links = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userIds));
            roleIdsMap = links.stream().collect(Collectors.groupingBy(
                    UserRole::getUserId,
                    Collectors.mapping(UserRole::getRoleId, Collectors.toList())
            ));
        }

        var voList = users.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setRoleIds(roleIdsMap.getOrDefault(user.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public com.mc.erp.vo.LoginVO login(com.mc.erp.dto.LoginDTO loginDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = this.getOne(wrapper);
        if (user == null || !user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        com.mc.erp.vo.LoginVO loginVO = new com.mc.erp.vo.LoginVO();
        loginVO.setToken(jwtUtil.generateToken(user.getId(), user.getUsername()));
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        return loginVO;
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        if (userId == null) return List.of();
        var links = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        return links.stream().map(UserRole::getRoleId).filter(Objects::nonNull).distinct().toList();
    }

    @Override
    public boolean updateUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null) return false;
        // 先清空
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) return true;

        // 再插入
        for (Long roleId : roleIds.stream().filter(Objects::nonNull).distinct().toList()) {
            UserRole link = new UserRole();
            link.setUserId(userId);
            link.setRoleId(roleId);
            userRoleMapper.insert(link);
        }
        return true;
    }

    @Override
    public List<UserWithRoleVO> listWithRoles() {
        // 查询所有未删除用户
        List<User> users = this.list(new LambdaQueryWrapper<User>().orderByAsc(User::getId));
        if (users.isEmpty()) return List.of();

        List<Long> userIds = users.stream().map(User::getId).filter(Objects::nonNull).toList();

        // 查询用户-角色关联
        List<UserRole> links = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userIds));

        // 收集所有 roleId，查角色名称和编码
        List<Long> roleIds = links.stream().map(UserRole::getRoleId).filter(Objects::nonNull).distinct().toList();
        List<Role> roles = roleIds.isEmpty() ? List.of()
                : roleMapper.selectList(new LambdaQueryWrapper<Role>().in(Role::getId, roleIds));
        Map<Long, String> roleNameMap = roles.stream().collect(Collectors.toMap(Role::getId, Role::getRoleName));
        Map<Long, String> roleCodeMap = roles.stream().collect(Collectors.toMap(Role::getId, Role::getRoleCode));

        // userId -> roleNames
        Map<Long, List<String>> userRoleNamesMap = links.stream()
                .collect(Collectors.groupingBy(
                        UserRole::getUserId,
                        Collectors.mapping(
                                link -> roleNameMap.getOrDefault(link.getRoleId(), ""),
                                Collectors.filtering(s -> !s.isEmpty(), Collectors.toList()))));

        // userId -> roleCodes
        Map<Long, List<String>> userRoleCodesMap = links.stream()
                .collect(Collectors.groupingBy(
                        UserRole::getUserId,
                        Collectors.mapping(
                                link -> roleCodeMap.getOrDefault(link.getRoleId(), ""),
                                Collectors.filtering(s -> !s.isEmpty(), Collectors.toList()))));

        return users.stream().map(user -> {
            UserWithRoleVO vo = new UserWithRoleVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setRoleNames(userRoleNamesMap.getOrDefault(user.getId(), List.of()));
            vo.setRoleCodes(userRoleCodesMap.getOrDefault(user.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(dto.getOldPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(dto.getNewPassword());
        this.updateById(update);
    }
}
