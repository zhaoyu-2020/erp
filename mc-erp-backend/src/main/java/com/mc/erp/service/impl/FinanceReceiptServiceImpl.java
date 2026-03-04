package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FinanceReceiptQuery;
import com.mc.erp.entity.FinanceReceipt;
import com.mc.erp.mapper.FinanceReceiptMapper;
import com.mc.erp.service.FinanceReceiptService;
import com.mc.erp.vo.FinanceReceiptVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class FinanceReceiptServiceImpl extends ServiceImpl<FinanceReceiptMapper, FinanceReceipt>
        implements FinanceReceiptService {

    @Override
    public PageResult<FinanceReceiptVO> getPage(FinanceReceiptQuery query) {
        Page<FinanceReceipt> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<FinanceReceipt> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getReceiptNo()), FinanceReceipt::getReceiptNo, query.getReceiptNo());
        wrapper.eq(query.getCustomerId() != null, FinanceReceipt::getCustomerId, query.getCustomerId());
        wrapper.eq(query.getStatus() != null, FinanceReceipt::getStatus, query.getStatus());
        wrapper.orderByDesc(FinanceReceipt::getCreateTime);

        Page<FinanceReceipt> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(receipt -> {
            FinanceReceiptVO vo = new FinanceReceiptVO();
            BeanUtils.copyProperties(receipt, vo);
            // In reality, inject CustomerService to fetch customer name
            vo.setCustomerName("Customer " + receipt.getCustomerId());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
