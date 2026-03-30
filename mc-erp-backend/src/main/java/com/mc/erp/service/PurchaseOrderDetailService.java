package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.PurchaseOrderDetailQuery;
import com.mc.erp.entity.PurchaseOrderDetail;
import com.mc.erp.vo.PurchaseOrderDetailVO;

import java.util.List;

public interface PurchaseOrderDetailService extends IService<PurchaseOrderDetail> {
    PageResult<PurchaseOrderDetailVO> getPage(PurchaseOrderDetailQuery query);
    List<PurchaseOrderDetailVO> listByOrderId(Long orderId);
    boolean saveDetail(PurchaseOrderDetail detail);
    boolean updateDetail(PurchaseOrderDetail detail);
    /** 重新计算并更新采购订单的合同总吨数、合同总金额、结算总数量、结算总金额 */
    void recalculateOrderTotals(Long purchaseOrderId);

    /** 检查指定采购订单是否至少有一条明细填写了结算数量（actualQuantity） */
    boolean hasSettledQuantity(Long purchaseOrderId);
}
