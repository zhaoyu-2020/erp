package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.CustomerQuery;
import com.mc.erp.entity.Customer;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.CustomerService;
import com.mc.erp.vo.CustomerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/page")
    public Result<PageResult<CustomerVO>> getPage(CustomerQuery query) {
        return Result.success(customerService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<CustomerVO> getById(@PathVariable Long id) {
        return Result.success(customerService.getByIdWithSalesUser(id));
    }

    @OperLog(module = "客户管理", type = OperationType.ADD, description = "新增客户")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody Customer customer) {
        return Result.success(customerService.save(customer));
    }

    @OperLog(module = "客户管理", type = OperationType.MODIFY, description = "修改客户")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody Customer customer) {
        return Result.success(customerService.updateById(customer));
    }

    @OperLog(module = "客户管理", type = OperationType.DELETE, description = "删除客户")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(customerService.removeById(id));
    }
}
