package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.FinanceReceiptQuery;
import com.mc.erp.dto.FinanceReceiptSaveDTO;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.FinanceReceiptService;
import com.mc.erp.vo.FinanceReceiptVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public Result<FinanceReceiptVO> getById(@PathVariable Long id) {
        return Result.success(financeReceiptService.getWithDetails(id));
    }

    @OperLog(module = "财务收款", type = OperationType.ADD, description = "新增收款单")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody FinanceReceiptSaveDTO dto) {
        financeReceiptService.saveWithDetails(dto);
        return Result.success(null);
    }

    @OperLog(module = "财务收款", type = OperationType.MODIFY, description = "修改收款单")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody FinanceReceiptSaveDTO dto) {
        financeReceiptService.updateWithDetails(dto);
        return Result.success(null);
    }

    @OperLog(module = "财务收款", type = OperationType.DELETE, description = "删除收款单")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        financeReceiptService.deleteWithDetails(id);
        return Result.success(true);
    }
}
