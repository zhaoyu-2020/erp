package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.entity.ProductType;
import com.mc.erp.mapper.ProductTypeMapper;
import com.mc.erp.service.ProductTypeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductTypeServiceImpl extends ServiceImpl<ProductTypeMapper, ProductType> implements ProductTypeService {
    @Override
    public List<ProductType> listAll() {
        return this.list(new LambdaQueryWrapper<ProductType>()
                .orderByAsc(ProductType::getId));
    }

    @Override
    public boolean createType(String typeName) {
        String normalizedName = typeName == null ? "" : typeName.trim();
        if (!StringUtils.hasText(normalizedName)) {
            throw new RuntimeException("产品类型不能为空");
        }

        long existCount = this.count(new LambdaQueryWrapper<ProductType>()
                .eq(ProductType::getTypeName, normalizedName));
        if (existCount > 0) {
            throw new RuntimeException("产品类型已存在");
        }

        ProductType entity = new ProductType();
        entity.setTypeName(normalizedName);
        return this.save(entity);
    }
}
