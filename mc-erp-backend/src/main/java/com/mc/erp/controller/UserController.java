package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.UpdateUserRolesDTO;
import com.mc.erp.dto.UserQuery;
import com.mc.erp.entity.User;
import com.mc.erp.service.UserService;
import com.mc.erp.vo.UserVO;
import com.mc.erp.vo.UserWithRoleVO;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/page")
    public Result<PageResult<UserVO>> getPage(UserQuery query) {
        return Result.success(userService.getPage(query));
    }

    /**
     * 查询所有用户（含角色名称），用于前端创建人/业务人员搜索下拉
     */
    @GetMapping("/list-with-roles")
    public Result<List<UserWithRoleVO>> listWithRoles() {
        return Result.success(userService.listWithRoles());
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody User user) {
        return Result.success(userService.save(user));
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody User user) {
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long id) {
        return Result.success(userService.getRoleIds(id));
    }

    @PutMapping("/{id}/roles")
    public Result<Boolean> updateUserRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesDTO dto) {
        return Result.success(userService.updateUserRoles(id, dto == null ? null : dto.getRoleIds()));
    }
}
