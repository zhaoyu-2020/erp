package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.entity.RoleMenu;
import com.mc.erp.mapper.RoleMenuMapper;
import com.mc.erp.service.RoleMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        if (roleId == null) return List.of();
        return this.list(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId))
                .stream().map(RoleMenu::getMenuId).filter(Objects::nonNull).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRoleMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null) return false;
        this.remove(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) return true;
        for (Long menuId : menuIds.stream().filter(Objects::nonNull).distinct().toList()) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            this.baseMapper.insert(rm);
        }
        return true;
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        if (userId == null) return List.of();
        return this.baseMapper.selectPermissionsByUserId(userId);
    }
}
