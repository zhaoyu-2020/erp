package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.vo.SalesOrderVO;

public interface SalesOrderService extends IService<SalesOrder> {
    PageResult<SalesOrderVO> getPage(SalesOrderQuery query);

    /**
     * 状态流转 —— 校验合法性后更新状态，并触发对应的业务副作用
     */
    void updateStatus(Long id, Integer targetStatus);

    void calculateAndUpdateProfit(Long salesOrderId);

    void calculateAndUpdateLoss(Long salesOrderId);
}
