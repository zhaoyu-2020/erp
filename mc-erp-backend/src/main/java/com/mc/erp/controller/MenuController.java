package com.mc.erp.controller;

import com.mc.erp.common.Result;
import com.mc.erp.entity.Menu;
import com.mc.erp.service.MenuService;
import com.mc.erp.vo.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/tree")
    public Result<List<MenuVO>> getTreeList() {
        return Result.success(menuService.getTreeList());
    }

    @GetMapping("/{id}")
    public Result<Menu> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody Menu menu) {
        return Result.success(menuService.save(menu));
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody Menu menu) {
        return Result.success(menuService.updateById(menu));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(menuService.removeById(id));
    }
}
