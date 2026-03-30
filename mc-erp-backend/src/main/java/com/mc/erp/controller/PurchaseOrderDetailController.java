package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.PurchaseOrderDetailQuery;
import com.mc.erp.entity.PurchaseOrderDetail;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.PurchaseOrderDetailService;
import com.mc.erp.vo.PurchaseOrderDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-order-details")
public class PurchaseOrderDetailController {

    @Autowired
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @GetMapping("/page")
    public Result<PageResult<PurchaseOrderDetailVO>> getPage(PurchaseOrderDetailQuery query) {
        return Result.success(purchaseOrderDetailService.getPage(query));
    }

    @GetMapping("/order/{orderId}")
    public Result<List<PurchaseOrderDetailVO>> listByOrderId(@PathVariable Long orderId) {
        return Result.success(purchaseOrderDetailService.listByOrderId(orderId));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrderDetail> getById(@PathVariable Long id) {
        return Result.success(purchaseOrderDetailService.getById(id));
    }

    @OperLog(module = "采购订单明细", type = OperationType.ADD, description = "新增采购订单明细")
    @PostMapping
    public Result<Boolean> save(@RequestBody PurchaseOrderDetail detail) {
        return Result.success(purchaseOrderDetailService.saveDetail(detail));
    }

    @OperLog(module = "采购订单明细", type = OperationType.MODIFY, description = "修改采购订单明细")
    @PutMapping
    public Result<Boolean> update(@RequestBody PurchaseOrderDetail detail) {
        return Result.success(purchaseOrderDetailService.updateDetail(detail));
    }

    @OperLog(module = "采购订单明细", type = OperationType.DELETE, description = "删除采购订单明细")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        PurchaseOrderDetail detail = purchaseOrderDetailService.getById(id);
        Long purchaseOrderId = detail != null ? detail.getPurchaseOrderId() : null;
        boolean result = purchaseOrderDetailService.removeById(id);
        if (result && purchaseOrderId != null) {
            purchaseOrderDetailService.recalculateOrderTotals(purchaseOrderId);
        }
        return Result.success(result);
    }
}
