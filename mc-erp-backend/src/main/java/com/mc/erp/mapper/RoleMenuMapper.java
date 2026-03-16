package com.mc.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.erp.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 查询用户拥有的所有权限标识（通过角色-菜单关联）
     */
    @Select("SELECT DISTINCT m.permission " +
            "FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} " +
            "  AND m.permission IS NOT NULL AND m.permission <> '' " +
            "  AND m.is_deleted = 0")
    List<String> selectPermissionsByUserId(Long userId);
}
