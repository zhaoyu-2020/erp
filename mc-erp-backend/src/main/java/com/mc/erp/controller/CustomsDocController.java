package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.CustomsDocQuery;
import com.mc.erp.entity.CustomsDoc;
import com.mc.erp.service.CustomsDocService;
import com.mc.erp.vo.CustomsDocVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/customs-docs")
public class CustomsDocController {

    @Autowired
    private CustomsDocService customsDocService;

    @GetMapping("/page")
    public Result<PageResult<CustomsDocVO>> getPage(CustomsDocQuery query) {
        return Result.success(customsDocService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<CustomsDoc> getById(@PathVariable Long id) {
        return Result.success(customsDocService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody CustomsDoc customsDoc) {
        return Result.success(customsDocService.save(customsDoc));
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody CustomsDoc customsDoc) {
        return Result.success(customsDocService.updateById(customsDoc));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(customsDocService.removeById(id));
    }
}
