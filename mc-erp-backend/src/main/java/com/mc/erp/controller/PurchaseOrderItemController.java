package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.Result;
import com.mc.erp.entity.PurchaseOrderItem;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.PurchaseOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-order-items")
public class PurchaseOrderItemController {

    @Autowired
    private PurchaseOrderItemService purchaseOrderItemService;

    @GetMapping("/order/{orderId}")
    public Result<List<PurchaseOrderItem>> listByOrder(@PathVariable Long orderId) {
        return Result.success(purchaseOrderItemService.listByPurchaseOrderId(orderId));
    }

    @OperLog(module = "采购加工项", type = OperationType.ADD, description = "新增采购加工项")
    @PostMapping
    public Result<Boolean> save(@RequestBody PurchaseOrderItem item) {
        return Result.success(purchaseOrderItemService.save(item));
    }

    @OperLog(module = "采购加工项", type = OperationType.MODIFY, description = "修改采购加工项")
    @PutMapping
    public Result<Boolean> update(@RequestBody PurchaseOrderItem item) {
        return Result.success(purchaseOrderItemService.updateById(item));
    }

    @OperLog(module = "采购加工项", type = OperationType.DELETE, description = "删除采购加工项")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(purchaseOrderItemService.removeById(id));
    }
}
