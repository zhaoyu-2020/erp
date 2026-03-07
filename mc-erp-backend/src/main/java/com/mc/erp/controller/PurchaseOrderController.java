package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.PurchaseOrderQuery;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.service.PurchaseOrderService;
import com.mc.erp.vo.PurchaseOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping("/page")
    public Result<PageResult<PurchaseOrderVO>> getPage(PurchaseOrderQuery query) {
        return Result.success(purchaseOrderService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrder> getById(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody PurchaseOrder purchaseOrder) {
        return Result.success(purchaseOrderService.save(purchaseOrder));
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody PurchaseOrder purchaseOrder) {
        return Result.success(purchaseOrderService.updateById(purchaseOrder));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(purchaseOrderService.removeById(id));
    }
}
