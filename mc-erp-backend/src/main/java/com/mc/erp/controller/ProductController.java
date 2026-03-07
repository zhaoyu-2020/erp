package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.ProductQuery;
import com.mc.erp.entity.Product;
import com.mc.erp.service.ProductService;
import com.mc.erp.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/page")
    public Result<PageResult<ProductVO>> getPage(ProductQuery query) {
        return Result.success(productService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody Product product) {
        return Result.success(productService.save(product));
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody Product product) {
        return Result.success(productService.updateById(product));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productService.removeById(id));
    }
}
