package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SupplierQuery;
import com.mc.erp.entity.Supplier;
import com.mc.erp.mapper.SupplierMapper;
import com.mc.erp.service.SupplierService;
import com.mc.erp.vo.SupplierVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    @Override
    public PageResult<SupplierVO> getPage(SupplierQuery query) {
        Page<Supplier> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getSupplierCode()), Supplier::getSupplierCode, query.getSupplierCode());
        wrapper.like(StringUtils.hasText(query.getName()), Supplier::getName, query.getName());
        wrapper.orderByDesc(Supplier::getCreateTime);

        Page<Supplier> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(sup -> {
            SupplierVO vo = new SupplierVO();
            BeanUtils.copyProperties(sup, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
