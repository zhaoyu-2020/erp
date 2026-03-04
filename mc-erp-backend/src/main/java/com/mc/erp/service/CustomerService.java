package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.CustomerQuery;
import com.mc.erp.entity.Customer;
import com.mc.erp.vo.CustomerVO;

public interface CustomerService extends IService<Customer> {
    PageResult<CustomerVO> getPage(CustomerQuery query);
}
