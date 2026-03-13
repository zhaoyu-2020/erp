package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.service.SalesOrderService;
import com.mc.erp.vo.SalesOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @GetMapping("/page")
    public Result<PageResult<SalesOrderVO>> getOrderPage(SalesOrderQuery query) {
        return Result.success(salesOrderService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<SalesOrder> getById(@PathVariable Long id) {
        return Result.success(salesOrderService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody SalesOrder salesOrder) {
        return Result.success(salesOrderService.save(salesOrder));
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody SalesOrder salesOrder) {
        // 获取更新前的订单信息
        SalesOrder oldOrder = salesOrderService.getById(salesOrder.getId());
        
        // 更新订单
        boolean success = salesOrderService.updateById(salesOrder);
        
        // 如果状态变更为已完成（7），自动计算利润和损耗
        if (success && salesOrder.getStatus() != null && salesOrder.getStatus() == 7) {
            // 检查是否是状态发生变更
            if (oldOrder == null || !Integer.valueOf(7).equals(oldOrder.getStatus())) {
                salesOrderService.calculateAndUpdateProfit(salesOrder.getId());
                salesOrderService.calculateAndUpdateLoss(salesOrder.getId());
            }
        }
        
        return Result.success(success);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(salesOrderService.removeById(id));
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        salesOrderService.updateStatus(id, body.get("status"));
        return Result.success(null);
    }
}
