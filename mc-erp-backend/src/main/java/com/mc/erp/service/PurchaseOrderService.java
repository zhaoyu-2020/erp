package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.PurchaseOrderQuery;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.vo.PurchaseOrderVO;

public interface PurchaseOrderService extends IService<PurchaseOrder> {
    PageResult<PurchaseOrderVO> getPage(PurchaseOrderQuery query);

    /**
     * 状态流转 —— 校验合法性后更新状态
     */
    void updateStatus(Long id, Integer targetStatus);
}
