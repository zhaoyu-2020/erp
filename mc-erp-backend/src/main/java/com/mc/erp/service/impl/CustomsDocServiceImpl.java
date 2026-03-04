package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.CustomsDocQuery;
import com.mc.erp.entity.CustomsDoc;
import com.mc.erp.mapper.CustomsDocMapper;
import com.mc.erp.service.CustomsDocService;
import com.mc.erp.vo.CustomsDocVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class CustomsDocServiceImpl extends ServiceImpl<CustomsDocMapper, CustomsDoc> implements CustomsDocService {

    @Override
    public PageResult<CustomsDocVO> getPage(CustomsDocQuery query) {
        Page<CustomsDoc> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CustomsDoc> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getDocNo()), CustomsDoc::getDocNo, query.getDocNo());
        wrapper.eq(query.getStatus() != null, CustomsDoc::getStatus, query.getStatus());
        wrapper.eq(query.getSalesOrderId() != null, CustomsDoc::getSalesOrderId, query.getSalesOrderId());
        wrapper.orderByDesc(CustomsDoc::getCreateTime);

        Page<CustomsDoc> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(doc -> {
            CustomsDocVO vo = new CustomsDocVO();
            BeanUtils.copyProperties(doc, vo);
            // In reality, inject SalesOrderService to fetch order number
            vo.setSalesOrderNo("SO-REF-" + doc.getSalesOrderId());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
