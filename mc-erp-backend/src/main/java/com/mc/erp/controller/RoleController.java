package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.RoleQuery;
import com.mc.erp.entity.Role;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.RoleMenuService;
import com.mc.erp.service.RoleService;
import com.mc.erp.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMenuService roleMenuService;

    @GetMapping("/page")
    public Result<PageResult<RoleVO>> getPage(RoleQuery query) {
        return Result.success(roleService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @OperLog(module = "角色管理", type = OperationType.ADD, description = "新增角色")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody Role role) {
        return Result.success(roleService.save(role));
    }

    @OperLog(module = "角色管理", type = OperationType.MODIFY, description = "修改角色")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody Role role) {
        return Result.success(roleService.updateById(role));
    }

    @OperLog(module = "角色管理", type = OperationType.DELETE, description = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(roleService.removeById(id));
    }

    /** 用于下拉框选择角色 */
    @GetMapping("/list")
    public Result<List<RoleVO>> listAll() {
        return Result.success(
                roleService.list().stream().map(r -> {
                    RoleVO vo = new RoleVO();
                    vo.setId(r.getId());
                    vo.setRoleCode(r.getRoleCode());
                    vo.setRoleName(r.getRoleName());
                    vo.setDescription(r.getDescription());
                    vo.setCreateTime(r.getCreateTime());
                    return vo;
                }).collect(Collectors.toList())
        );
    }

    /** 查询角色拥有的菜单ID列表（用于权限配置页回显） */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long id) {
        return Result.success(roleMenuService.getMenuIdsByRoleId(id));
    }

    @OperLog(module = "角色管理", type = OperationType.AUTH_CHANGE, description = "设置角色菜单权限")
    /** 设置角色菜单（全量覆盖，用于权限配置页保存） */
    @PutMapping("/{id}/menus")
    public Result<Boolean> updateRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        return Result.success(roleMenuService.updateRoleMenus(id, menuIds));
    }
}
