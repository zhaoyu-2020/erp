package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.entity.PurchaseOrderItem;

import java.util.List;

public interface PurchaseOrderItemService extends IService<PurchaseOrderItem> {
    List<PurchaseOrderItem> listByPurchaseOrderId(Long purchaseOrderId);
}
