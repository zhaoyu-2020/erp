package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.CustomsDocQuery;
import com.mc.erp.entity.CustomsDoc;
import com.mc.erp.enums.OperationType;
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

    @OperLog(module = "报关单证", type = OperationType.ADD, description = "新增报关单证")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody CustomsDoc customsDoc) {
        return Result.success(customsDocService.save(customsDoc));
    }

    @OperLog(module = "报关单证", type = OperationType.MODIFY, description = "修改报关单证")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody CustomsDoc customsDoc) {
        return Result.success(customsDocService.updateById(customsDoc));
    }

    @OperLog(module = "报关单证", type = OperationType.DELETE, description = "删除报关单证")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(customsDocService.removeById(id));
    }
}
