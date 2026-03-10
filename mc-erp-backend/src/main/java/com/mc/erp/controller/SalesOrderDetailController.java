package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.SalesOrderDetailQuery;
import com.mc.erp.entity.SalesOrderDetail;
import com.mc.erp.service.SalesOrderDetailService;
import com.mc.erp.vo.SalesOrderDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales-order-details")
public class SalesOrderDetailController {

    @Autowired
    private SalesOrderDetailService salesOrderDetailService;

    @GetMapping("/page")
    public Result<PageResult<SalesOrderDetailVO>> getPage(SalesOrderDetailQuery query) {
        return Result.success(salesOrderDetailService.getPage(query));
    }

    @GetMapping("/order/{orderId}")
    public Result<List<SalesOrderDetailVO>> listByOrderId(@PathVariable Long orderId) {
        return Result.success(salesOrderDetailService.listByOrderId(orderId));
    }

    @GetMapping("/{id}")
    public Result<SalesOrderDetail> getById(@PathVariable Long id) {
        return Result.success(salesOrderDetailService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SalesOrderDetail detail) {
        return Result.success(salesOrderDetailService.saveDetail(detail));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SalesOrderDetail detail) {
        return Result.success(salesOrderDetailService.updateDetail(detail));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(salesOrderDetailService.removeById(id));
    }
}
