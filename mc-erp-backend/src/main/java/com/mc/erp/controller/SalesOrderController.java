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
        return Result.success(salesOrderService.updateById(salesOrder));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(salesOrderService.removeById(id));
    }
}
