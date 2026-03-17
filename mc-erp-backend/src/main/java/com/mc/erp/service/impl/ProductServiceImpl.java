package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.ProductQuery;
import com.mc.erp.entity.Product;
import com.mc.erp.entity.ProductType;
import com.mc.erp.mapper.ProductMapper;
import com.mc.erp.mapper.ProductTypeMapper;
import com.mc.erp.service.ProductService;
import com.mc.erp.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductTypeMapper productTypeMapper;

    @Override
    public PageResult<ProductVO> getPage(ProductQuery query) {
        Page<Product> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (query.getProductTypeId() != null) {
            wrapper.eq(Product::getProductTypeId, query.getProductTypeId());
        }
        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> resultPage = this.page(page, wrapper);

        // 批量查品名
        List<Long> typeIds = resultPage.getRecords().stream()
                .map(Product::getProductTypeId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, ProductType> typeMap = typeIds.isEmpty() ? Map.of() :
                productTypeMapper.selectBatchIds(typeIds).stream()
                        .collect(Collectors.toMap(ProductType::getId, t -> t));

        List<ProductVO> voList = resultPage.getRecords().stream().map(prod -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(prod, vo);
            if (prod.getProductTypeId() != null) {
                ProductType pt = typeMap.get(prod.getProductTypeId());
                if (pt != null) {
                    vo.setTypeName(pt.getTypeName());
                    vo.setTypeNameEn(pt.getTypeNameEn());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
