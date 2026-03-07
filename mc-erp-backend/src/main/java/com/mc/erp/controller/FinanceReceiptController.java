package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.FinanceReceiptQuery;
import com.mc.erp.entity.FinanceReceipt;
import com.mc.erp.service.FinanceReceiptService;
import com.mc.erp.vo.FinanceReceiptVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/finance-receipts")
public class FinanceReceiptController {

    @Autowired
    private FinanceReceiptService financeReceiptService;

    @GetMapping("/page")
    public Result<PageResult<FinanceReceiptVO>> getPage(FinanceReceiptQuery query) {
        return Result.success(financeReceiptService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<FinanceReceipt> getById(@PathVariable Long id) {
        return Result.success(financeReceiptService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody FinanceReceipt financeReceipt) {
        return Result.success(financeReceiptService.save(financeReceipt));
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody FinanceReceipt financeReceipt) {
        return Result.success(financeReceiptService.updateById(financeReceipt));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(financeReceiptService.removeById(id));
    }
}
