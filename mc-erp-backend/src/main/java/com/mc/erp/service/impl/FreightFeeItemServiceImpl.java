package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.entity.FreightFeeItem;
import com.mc.erp.entity.FreightOrder;
import com.mc.erp.mapper.FreightFeeItemMapper;
import com.mc.erp.service.FreightFeeItemService;
import com.mc.erp.service.FreightOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FreightFeeItemServiceImpl extends ServiceImpl<FreightFeeItemMapper, FreightFeeItem>
        implements FreightFeeItemService {

    @Autowired
    @Lazy
    private FreightOrderService freightOrderService;

    @Override
    public List<FreightFeeItem> listByOrderId(Long orderId) {
        return this.list(new LambdaQueryWrapper<FreightFeeItem>()
                .eq(FreightFeeItem::getOrderId, orderId)
                .orderByAsc(FreightFeeItem::getFeeType)
                .orderByAsc(FreightFeeItem::getItemId));
    }

    @Override
    @Transactional
    public boolean saveFeeItems(Long orderId, List<FreightFeeItem> items) {
        FreightOrder order = freightOrderService.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("货代订单不存在");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == 2) {
            throw new IllegalStateException("已结算订单不允许修改费用");
        }
        // 删除旧明细
        this.remove(new LambdaQueryWrapper<FreightFeeItem>().eq(FreightFeeItem::getOrderId, orderId));
        // 保存新明细
        if (items != null && !items.isEmpty()) {
            for (FreightFeeItem item : items) {
                item.setItemId(null);
                item.setOrderId(orderId);
                if (item.getFeeAmount() != null && item.getFeeAmount().signum() < 0) {
                    throw new IllegalArgumentException("费用金额不能为负数");
                }
            }
            this.saveBatch(items);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean deleteFeeItem(Long itemId) {
        FreightFeeItem item = this.getById(itemId);
        if (item == null) {
            return false;
        }
        FreightOrder order = freightOrderService.getById(item.getOrderId());
        if (order != null && order.getOrderStatus() != null && order.getOrderStatus() == 2) {
            throw new IllegalStateException("已结算订单不允许删除费用");
        }
        boolean removed = this.removeById(itemId);
        return removed;
    }
}
