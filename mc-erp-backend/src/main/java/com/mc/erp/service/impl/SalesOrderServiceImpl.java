package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.entity.User;
import com.mc.erp.service.CustomerService;
import com.mc.erp.service.SalesOrderService;
import com.mc.erp.service.UserService;
import com.mc.erp.mapper.SalesOrderMapper;
import com.mc.erp.vo.SalesOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Override
    public PageResult<SalesOrderVO> getPage(SalesOrderQuery query) {
        Page<SalesOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getOrderNo()), SalesOrder::getOrderNo, query.getOrderNo());
        wrapper.eq(query.getSalespersonId() != null, SalesOrder::getSalespersonId, query.getSalespersonId());
        wrapper.eq(query.getCustomerId() != null, SalesOrder::getCustomerId, query.getCustomerId());
        wrapper.eq(StringUtils.hasText(query.getTradeTerm()), SalesOrder::getTradeTerm, query.getTradeTerm());
        wrapper.eq(query.getStatus() != null, SalesOrder::getStatus, query.getStatus());
        wrapper.orderByDesc(SalesOrder::getCreateTime);

        Page<SalesOrder> resultPage = this.page(page, wrapper);

        Set<Long> userIds = resultPage.getRecords().stream().map(SalesOrder::getSalespersonId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> userNameMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getUsername));

        Set<Long> customerIds = resultPage.getRecords().stream().map(SalesOrder::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> customerNameMap = customerIds.isEmpty()
                ? Collections.emptyMap()
                : customerService.listByIds(customerIds).stream().collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));

        var voList = resultPage.getRecords().stream().map(order -> {
            SalesOrderVO vo = new SalesOrderVO();
            BeanUtils.copyProperties(order, vo);
            vo.setSalespersonName(userNameMap.get(order.getSalespersonId()));
            vo.setCustomerName(customerNameMap.get(order.getCustomerId()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
