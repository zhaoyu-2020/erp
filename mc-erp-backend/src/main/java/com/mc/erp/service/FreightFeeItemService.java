package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.entity.FreightFeeItem;

import java.util.List;

public interface FreightFeeItemService extends IService<FreightFeeItem> {

    /**
     * 获取某订单的所有费用明细
     */
    List<FreightFeeItem> listByOrderId(Long orderId);

    /**
     * 批量保存费用明细并重新计算订单费用
     */
    boolean saveFeeItems(Long orderId, List<FreightFeeItem> items);

    /**
     * 删除费用明细并重新计算订单费用
     */
    boolean deleteFeeItem(Long itemId);
}
