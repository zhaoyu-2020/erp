package com.mc.erp.controller;

import com.mc.erp.common.Result;
import com.mc.erp.entity.SupplierAccount;
import com.mc.erp.service.SupplierAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/supplier-accounts")
public class SupplierAccountController {

    @Autowired
    private SupplierAccountService supplierAccountService;

    @GetMapping("/list")
    public Result<List<SupplierAccount>> listBySupplierId(@RequestParam Long supplierId) {
        return Result.success(supplierAccountService.listBySupplierId(supplierId));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SupplierAccount account) {
        return Result.success(supplierAccountService.save(account));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SupplierAccount account) {
        return Result.success(supplierAccountService.updateById(account));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(supplierAccountService.removeById(id));
    }
}
