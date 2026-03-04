package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.CustomerQuery;
import com.mc.erp.entity.Customer;
import com.mc.erp.mapper.CustomerMapper;
import com.mc.erp.service.CustomerService;
import com.mc.erp.vo.CustomerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Override
    public PageResult<CustomerVO> getPage(CustomerQuery query) {
        Page<Customer> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getCustomerCode()), Customer::getCustomerCode, query.getCustomerCode());
        wrapper.like(StringUtils.hasText(query.getName()), Customer::getName, query.getName());
        wrapper.orderByDesc(Customer::getCreateTime);

        Page<Customer> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(cust -> {
            CustomerVO vo = new CustomerVO();
            BeanUtils.copyProperties(cust, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
