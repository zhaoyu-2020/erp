package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.FinancePaymentAuditDTO;
import com.mc.erp.dto.FinancePaymentQuery;
import com.mc.erp.dto.FinancePaymentSaveDTO;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.FinancePaymentService;
import com.mc.erp.vo.FinancePaymentVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/finance-payments")
public class FinancePaymentController {

    @Autowired
    private FinancePaymentService financePaymentService;

    @GetMapping("/page")
    public Result<PageResult<FinancePaymentVO>> getPage(FinancePaymentQuery query) {
        return Result.success(financePaymentService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<FinancePaymentVO> getById(@PathVariable Long id) {
        return Result.success(financePaymentService.getDetail(id));
    }

    @OperLog(module = "财务付款", type = OperationType.ADD, description = "新增付款单")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody FinancePaymentSaveDTO dto) {
        financePaymentService.savePayment(dto);
        return Result.success(null);
    }

    @OperLog(module = "财务付款", type = OperationType.MODIFY, description = "修改付款单")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody FinancePaymentSaveDTO dto) {
        financePaymentService.updatePayment(dto);
        return Result.success(null);
    }

    @OperLog(module = "财务付款", type = OperationType.DELETE, description = "删除付款单")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        financePaymentService.deletePayment(id);
        return Result.success(true);
    }

    @OperLog(module = "财务付款", type = OperationType.STATUS_CHANGE, description = "审核付款单")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody FinancePaymentAuditDTO dto) {
        financePaymentService.audit(dto);
        return Result.success(null);
    }
}
