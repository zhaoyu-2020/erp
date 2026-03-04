package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.PurchaseOrderQuery;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.mapper.PurchaseOrderMapper;
import com.mc.erp.service.PurchaseOrderService;
import com.mc.erp.vo.PurchaseOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
        implements PurchaseOrderService {

    @Override
    public PageResult<PurchaseOrderVO> getPage(PurchaseOrderQuery query) {
        Page<PurchaseOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getPoNo()), PurchaseOrder::getPoNo, query.getPoNo());
        wrapper.eq(query.getStatus() != null, PurchaseOrder::getStatus, query.getStatus());
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);

        Page<PurchaseOrder> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(po -> {
            PurchaseOrderVO vo = new PurchaseOrderVO();
            BeanUtils.copyProperties(po, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
