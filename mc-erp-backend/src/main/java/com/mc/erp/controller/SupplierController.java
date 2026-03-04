package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.SupplierQuery;
import com.mc.erp.entity.Supplier;
import com.mc.erp.service.SupplierService;
import com.mc.erp.vo.SupplierVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
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

    @PostMapping
    public Result<Boolean> save(@RequestBody Supplier supplier) {
        return Result.success(supplierService.save(supplier));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Supplier supplier) {
        return Result.success(supplierService.updateById(supplier));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(supplierService.removeById(id));
    }
}
