package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.Result;
import com.mc.erp.entity.SupplierAccount;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.SupplierAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/supplier-accounts")
public class SupplierAccountController {

    @Autowired
    private SupplierAccountService supplierAccountService;

    @GetMapping("/list")
    public Result<List<SupplierAccount>> listBySupplierId(@RequestParam Long supplierId) {
        return Result.success(supplierAccountService.listBySupplierId(supplierId));
    }

    @OperLog(module = "供应商账户", type = OperationType.ADD, description = "新增供应商账户")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody SupplierAccount account) {
        return Result.success(supplierAccountService.save(account));
    }

    @OperLog(module = "供应商账户", type = OperationType.MODIFY, description = "修改供应商账户")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody SupplierAccount account) {
        return Result.success(supplierAccountService.updateById(account));
    }

    @OperLog(module = "供应商账户", type = OperationType.DELETE, description = "删除供应商账户")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(supplierAccountService.removeById(id));
    }
}
