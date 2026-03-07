package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.entity.PurchaseOrderItem;
import com.mc.erp.mapper.PurchaseOrderItemMapper;
import com.mc.erp.service.PurchaseOrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseOrderItemServiceImpl extends ServiceImpl<PurchaseOrderItemMapper, PurchaseOrderItem>
        implements PurchaseOrderItemService {

    @Override
    public List<PurchaseOrderItem> listByPurchaseOrderId(Long purchaseOrderId) {
        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderItem::getPurchaseOrderId, purchaseOrderId);
        return this.list(wrapper);
    }
}
