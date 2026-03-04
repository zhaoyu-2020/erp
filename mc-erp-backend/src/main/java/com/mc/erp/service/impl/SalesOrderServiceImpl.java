package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.mapper.SalesOrderMapper;
import com.mc.erp.service.SalesOrderService;
import com.mc.erp.vo.SalesOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {

    @Override
    public PageResult<SalesOrderVO> getPage(SalesOrderQuery query) {
        Page<SalesOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getOrderNo()), SalesOrder::getOrderNo, query.getOrderNo());
        wrapper.eq(StringUtils.hasText(query.getTradeTerm()), SalesOrder::getTradeTerm, query.getTradeTerm());
        wrapper.eq(query.getStatus() != null, SalesOrder::getStatus, query.getStatus());
        wrapper.orderByDesc(SalesOrder::getCreateTime);

        Page<SalesOrder> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(order -> {
            SalesOrderVO vo = new SalesOrderVO();
            BeanUtils.copyProperties(order, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
