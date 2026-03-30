package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.Result;
import com.mc.erp.dto.ProductTypeCreateDTO;
import com.mc.erp.dto.ProductTypeUpdateDTO;
import com.mc.erp.entity.ProductType;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.ProductTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/product-types")
public class ProductTypeController {
    @Autowired
    private ProductTypeService productTypeService;

    @GetMapping("/list")
    public Result<List<ProductType>> listAll() {
        return Result.success(productTypeService.listAll());
    }

    @OperLog(module = "产品品名", type = OperationType.ADD, description = "新增产品品名")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody ProductTypeCreateDTO dto) {
        return Result.success(productTypeService.createType(dto));
    }

    @OperLog(module = "产品品名", type = OperationType.MODIFY, description = "修改产品品名")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id,
                                   @Valid @RequestBody ProductTypeUpdateDTO dto) {
        dto.setId(id);
        return Result.success(productTypeService.updateType(dto));
    }

    @OperLog(module = "产品品名", type = OperationType.DELETE, description = "删除产品品名")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productTypeService.deleteType(id));
    }
}
