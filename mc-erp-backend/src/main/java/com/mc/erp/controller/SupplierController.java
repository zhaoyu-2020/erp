package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.SupplierQuery;
import com.mc.erp.entity.Supplier;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.SupplierService;
import com.mc.erp.vo.SupplierVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/page")
    public Result<PageResult<SupplierVO>> getPage(SupplierQuery query) {
        return Result.success(supplierService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<Supplier> getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @OperLog(module = "供应商管理", type = OperationType.ADD, description = "新增供应商")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody Supplier supplier) {
        return Result.success(supplierService.save(supplier));
    }

    @OperLog(module = "供应商管理", type = OperationType.MODIFY, description = "修改供应商")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody Supplier supplier) {
        return Result.success(supplierService.updateById(supplier));
    }

    @OperLog(module = "供应商管理", type = OperationType.DELETE, description = "删除供应商")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(supplierService.removeById(id));
    }
}
