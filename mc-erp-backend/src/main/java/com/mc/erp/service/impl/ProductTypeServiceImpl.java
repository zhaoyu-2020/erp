package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.dto.ProductTypeCreateDTO;
import com.mc.erp.dto.ProductTypeUpdateDTO;
import com.mc.erp.entity.Product;
import com.mc.erp.entity.ProductType;
import com.mc.erp.mapper.ProductMapper;
import com.mc.erp.mapper.ProductTypeMapper;
import com.mc.erp.service.OperationLogService;
import com.mc.erp.service.ProductTypeService;
import com.mc.erp.util.LogHelper;
import com.mc.erp.enums.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductTypeServiceImpl extends ServiceImpl<ProductTypeMapper, ProductType> implements ProductTypeService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OperationLogService operationLogService;

    @Override
    public List<ProductType> listAll() {
        return this.list(new LambdaQueryWrapper<ProductType>()
                .orderByAsc(ProductType::getId));
    }

    @Override
    public boolean createType(ProductTypeCreateDTO dto) {
        String normalizedName = dto.getTypeName() == null ? "" : dto.getTypeName().trim();
        if (!StringUtils.hasText(normalizedName)) {
            String errMsg = "产品品名不能为空";
            operationLogService.asyncSaveLog(LogHelper.buildErrorLog("产品品名", OperationType.ADD, "新增品名失败", errMsg));
            throw new RuntimeException(errMsg);
        }

        long existCount = this.count(new LambdaQueryWrapper<ProductType>()
                .eq(ProductType::getTypeName, normalizedName));
        if (existCount > 0) {
            String errMsg = "产品品名已存在";
            operationLogService.asyncSaveLog(LogHelper.buildErrorLog("产品品名", OperationType.ADD, "新增品名失败", errMsg));
            throw new RuntimeException(errMsg);
        }

        ProductType entity = new ProductType();
        entity.setTypeName(normalizedName);
        if (StringUtils.hasText(dto.getTypeNameEn())) {
            entity.setTypeNameEn(dto.getTypeNameEn().trim());
        }
        return this.save(entity);
    }

    @Override
    public boolean updateType(ProductTypeUpdateDTO dto) {
        ProductType existing = this.getById(dto.getId());
        if (existing == null) {
            String errMsg = "产品品名不存在";
            operationLogService.asyncSaveLog(LogHelper.buildErrorLog("产品品名", OperationType.MODIFY, "修改品名失败", errMsg));
            throw new RuntimeException(errMsg);
        }
        String normalizedName = dto.getTypeName() == null ? "" : dto.getTypeName().trim();
        if (!StringUtils.hasText(normalizedName)) {
            String errMsg = "产品品名不能为空";
            operationLogService.asyncSaveLog(LogHelper.buildErrorLog("产品品名", OperationType.MODIFY, "修改品名失败", errMsg));
            throw new RuntimeException(errMsg);
        }
        // 检查名称是否被其他记录占用
        long existCount = this.count(new LambdaQueryWrapper<ProductType>()
                .eq(ProductType::getTypeName, normalizedName)
                .ne(ProductType::getId, dto.getId()));
        if (existCount > 0) {
            String errMsg = "产品品名已存在";
            operationLogService.asyncSaveLog(LogHelper.buildErrorLog("产品品名", OperationType.MODIFY, "修改品名失败", errMsg));
            throw new RuntimeException(errMsg);
        }
        existing.setTypeName(normalizedName);
        existing.setTypeNameEn(StringUtils.hasText(dto.getTypeNameEn()) ? dto.getTypeNameEn().trim() : null);
        return this.updateById(existing);
    }

    @Override
    public boolean deleteType(Long id) {
        ProductType existing = this.getById(id);
        if (existing == null) {
            String errMsg = "产品品名不存在";
            operationLogService.asyncSaveLog(LogHelper.buildErrorLog("产品品名", OperationType.DELETE, "删除品名失败", errMsg));
            throw new RuntimeException(errMsg);
        }
        // 检查是否有产品引用该品名
        long refCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getProductTypeId, id));
        if (refCount > 0) {
            String errMsg = "该品名下还有 " + refCount + " 个产品，无法删除";
            operationLogService.asyncSaveLog(LogHelper.buildErrorLog("产品品名", OperationType.DELETE, "删除品名失败", errMsg));
            throw new RuntimeException(errMsg);
        }
        return this.removeById(id);
    }
}
