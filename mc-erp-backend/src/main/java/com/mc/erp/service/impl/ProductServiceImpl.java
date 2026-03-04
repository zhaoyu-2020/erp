package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.ProductQuery;
import com.mc.erp.entity.Product;
import com.mc.erp.mapper.ProductMapper;
import com.mc.erp.service.ProductService;
import com.mc.erp.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public PageResult<ProductVO> getPage(ProductQuery query) {
        Page<Product> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getSpuCode()), Product::getSpuCode, query.getSpuCode());
        wrapper.like(StringUtils.hasText(query.getNameCn()), Product::getNameCn, query.getNameCn());
        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(prod -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(prod, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
