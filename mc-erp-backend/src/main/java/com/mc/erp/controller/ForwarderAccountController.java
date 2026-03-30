package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.Result;
import com.mc.erp.entity.ForwarderAccount;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.ForwarderAccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/forwarder-accounts")
public class ForwarderAccountController {

    @Autowired
    private ForwarderAccountService forwarderAccountService;

    @GetMapping("/list")
    public Result<List<ForwarderAccount>> listByForwarderId(@RequestParam Long forwarderId) {
        return Result.success(forwarderAccountService.listByForwarderId(forwarderId));
    }

    @OperLog(module = "货代账户", type = OperationType.ADD, description = "新增货代账户")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody ForwarderAccount account) {
        return Result.success(forwarderAccountService.save(account));
    }

    @OperLog(module = "货代账户", type = OperationType.MODIFY, description = "修改货代账户")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody ForwarderAccount account) {
        return Result.success(forwarderAccountService.updateById(account));
    }

    @OperLog(module = "货代账户", type = OperationType.DELETE, description = "删除货代账户")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(forwarderAccountService.removeById(id));
    }
}
