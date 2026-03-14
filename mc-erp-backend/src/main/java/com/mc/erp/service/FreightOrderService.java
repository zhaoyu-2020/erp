package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FreightOrderQuery;
import com.mc.erp.entity.FreightFeeItem;
import com.mc.erp.entity.FreightOrder;
import com.mc.erp.vo.FreightOrderVO;

import java.util.List;

public interface FreightOrderService extends IService<FreightOrder> {

    /**
     * 分页查询货代订单
     */
    PageResult<FreightOrderVO> getPage(FreightOrderQuery query);

    /**
     * 查询货代订单详情（含费用明细、操作日志）
     */
    FreightOrderVO getDetail(Long orderId);

    /**
     * 创建货代订单（含费用明细）
     */
    Long createOrder(FreightOrder order, List<FreightFeeItem> feeItems);

    /**
     * 修改货代订单（按状态限制可编辑字段）
     */
    boolean updateOrder(FreightOrder order, List<FreightFeeItem> feeItems);

    /**
     * 删除货代订单（仅草稿状态）
     */
    boolean deleteOrder(Long orderId);

    /**
     * 提交订单（草稿 → 已提交）
     */
    boolean submitOrder(Long orderId);

    /**
     * 结算订单（已提交 → 已结算）
     */
    boolean settleOrder(Long orderId);

    /**
     * 作废订单
     */
    boolean cancelOrder(Long orderId, String cancelReason);

    /**
     * 重新计算订单费用合计
     */
    void recalculateTotals(Long orderId);
}
