package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FreightOrderQuery;
import com.mc.erp.entity.FreightOrder;
import com.mc.erp.vo.FreightOrderVO;

public interface FreightOrderService extends IService<FreightOrder> {

    /**
     * 分页查询海运订单
     */
    PageResult<FreightOrderVO> getPage(FreightOrderQuery query);

    /**
     * 查询海运订单详情（含操作日志）
     */
    FreightOrderVO getDetail(Long orderId);

    /**
     * 创建海运订单
     */
    Long createOrder(FreightOrder order);

    /**
     * 修改海运订单（按状态限制可编辑字段）
     */
    boolean updateOrder(FreightOrder order);

    /**
     * 删除海运订单（仅草稿状态）
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
}
