package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SupplierQuery;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.entity.Supplier;
import com.mc.erp.mapper.SupplierMapper;
import com.mc.erp.service.PurchaseOrderService;
import com.mc.erp.service.SupplierService;
import com.mc.erp.vo.SupplierVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Override
    public PageResult<SupplierVO> getPage(SupplierQuery query) {
        Page<Supplier> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();

    wrapper.like(StringUtils.hasText(query.getSupplierCode()), Supplier::getSupplierCode, query.getSupplierCode());
    wrapper.like(StringUtils.hasText(query.getName()), Supplier::getName, query.getName());
    wrapper.like(StringUtils.hasText(query.getProductType()), Supplier::getProductType, query.getProductType());
    wrapper.orderByDesc(Supplier::getCreateTime);

        Page<Supplier> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(sup -> {
            SupplierVO vo = new SupplierVO();
            BeanUtils.copyProperties(sup, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public boolean removeById(Serializable id) {
        long refCount = purchaseOrderService.count(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getSupplierId, id));
        if (refCount > 0) {
            throw new RuntimeException("该供应商下还有 " + refCount + " 个采购订单，无法删除");
        }
        return super.removeById(id);
    }
}
