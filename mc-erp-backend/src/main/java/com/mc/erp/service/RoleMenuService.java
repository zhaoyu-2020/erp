package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.entity.RoleMenu;

import java.util.List;

public interface RoleMenuService extends IService<RoleMenu> {

    /** 查询角色拥有的菜单ID列表 */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /** 设置角色菜单（全量覆盖） */
    boolean updateRoleMenus(Long roleId, List<Long> menuIds);

    /** 查询用户拥有的所有权限标识 */
    List<String> getPermissionsByUserId(Long userId);
}
