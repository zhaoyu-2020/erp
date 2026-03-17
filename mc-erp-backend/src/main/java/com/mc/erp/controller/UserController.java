package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.ChangePasswordDTO;
import com.mc.erp.dto.UpdateUserRolesDTO;
import com.mc.erp.dto.UserQuery;
import com.mc.erp.entity.User;
import com.mc.erp.service.UserService;
import com.mc.erp.util.SecurityUtil;
import com.mc.erp.vo.UserVO;
import com.mc.erp.vo.UserWithRoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/page")
    public Result<PageResult<UserVO>> getPage(UserQuery query) {
        return Result.success(userService.getPage(query));
    }

    /**
     * 查询所有用户（含角色名称），用于前端创建人/业务人员搜索下拉。
     * 所有已登录用户可用，无需特殊权限。
     */
    @GetMapping("/list-with-roles")
    public Result<List<UserWithRoleVO>> listWithRoles() {
        return Result.success(userService.listWithRoles());
    }

    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody User user) {
        return Result.success(userService.save(user));
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody User user) {
        return Result.success(userService.updateById(user));
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long id) {
        return Result.success(userService.getRoleIds(id));
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping("/{id}/roles")
    public Result<Boolean> updateUserRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesDTO dto) {
        return Result.success(userService.updateUserRoles(id, dto == null ? null : dto.getRoleIds()));
    }

    /**
     * 修改当前登录用户的密码（本人操作，无需特殊权限）
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.changePassword(userId, dto);
        return Result.success(null);
    }
}
