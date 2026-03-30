package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.StockQuery;
import com.mc.erp.entity.Stock;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.StockService;
import com.mc.erp.vo.StockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/page")
    public Result<PageResult<StockVO>> getPage(StockQuery query) {
        return Result.success(stockService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<Stock> getById(@PathVariable Long id) {
        return Result.success(stockService.getById(id));
    }

    @OperLog(module = "仓储管理", type = OperationType.ADD, description = "新增库存")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody Stock stock) {
        return Result.success(stockService.save(stock));
    }

    @OperLog(module = "仓储管理", type = OperationType.MODIFY, description = "修改库存")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody Stock stock) {
        return Result.success(stockService.updateById(stock));
    }

    @OperLog(module = "仓储管理", type = OperationType.DELETE, description = "删除库存")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(stockService.removeById(id));
    }
}
