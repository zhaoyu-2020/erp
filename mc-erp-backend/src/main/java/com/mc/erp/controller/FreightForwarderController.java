package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.FreightForwarderQuery;
import com.mc.erp.entity.FreightForwarder;
import com.mc.erp.service.FreightForwarderService;
import com.mc.erp.vo.FreightForwarderVO;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
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

@RestController
@Validated
@RequestMapping("/api/v1/freight-forwarders")
public class FreightForwarderController {

    @Autowired
    private FreightForwarderService freightForwarderService;

    @GetMapping("/page")
    public Result<PageResult<FreightForwarderVO>> getPage(FreightForwarderQuery query) {
        return Result.success(freightForwarderService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<FreightForwarderVO> getById(@PathVariable Long id) {
        FreightForwarder forwarder = freightForwarderService.getById(id);
        if (forwarder == null) return Result.success(null);
        FreightForwarderVO vo = new FreightForwarderVO();
        BeanUtils.copyProperties(forwarder, vo);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody FreightForwarder freightForwarder) {
        return Result.success(freightForwarderService.save(freightForwarder));
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody FreightForwarder freightForwarder) {
        return Result.success(freightForwarderService.updateById(freightForwarder));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(freightForwarderService.removeById(id));
    }
}
