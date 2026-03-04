package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.CustomsDocQuery;
import com.mc.erp.entity.CustomsDoc;
import com.mc.erp.service.CustomsDocService;
import com.mc.erp.vo.CustomsDocVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
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
    public Result<Boolean> save(@RequestBody CustomsDoc customsDoc) {
        return Result.success(customsDocService.save(customsDoc));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody CustomsDoc customsDoc) {
        return Result.success(customsDocService.updateById(customsDoc));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(customsDocService.removeById(id));
    }
}
