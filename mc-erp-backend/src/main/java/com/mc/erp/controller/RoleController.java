package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.RoleQuery;
import com.mc.erp.entity.Role;
import com.mc.erp.service.RoleService;
import com.mc.erp.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/page")
    public Result<PageResult<RoleVO>> getPage(RoleQuery query) {
        return Result.success(roleService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Role role) {
        return Result.success(roleService.save(role));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Role role) {
        return Result.success(roleService.updateById(role));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(roleService.removeById(id));
    }
}
