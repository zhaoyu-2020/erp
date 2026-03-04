package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.CustomerQuery;
import com.mc.erp.entity.Customer;
import com.mc.erp.service.CustomerService;
import com.mc.erp.vo.CustomerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/page")
    public Result<PageResult<CustomerVO>> getPage(CustomerQuery query) {
        return Result.success(customerService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Customer customer) {
        return Result.success(customerService.save(customer));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Customer customer) {
        return Result.success(customerService.updateById(customer));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(customerService.removeById(id));
    }
}
