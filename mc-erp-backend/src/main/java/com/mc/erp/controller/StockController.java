package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.StockQuery;
import com.mc.erp.entity.Stock;
import com.mc.erp.service.StockService;
import com.mc.erp.vo.StockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
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

    @PostMapping
    public Result<Boolean> save(@RequestBody Stock stock) {
        return Result.success(stockService.save(stock));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Stock stock) {
        return Result.success(stockService.updateById(stock));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(stockService.removeById(id));
    }
}
