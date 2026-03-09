package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.vo.SalesOrderVO;

public interface SalesOrderService extends IService<SalesOrder> {
    PageResult<SalesOrderVO> getPage(SalesOrderQuery query);
    
    /**
     * 计算并更新销售订单的利润
     * @param salesOrderId 销售订单ID
     */
    void calculateAndUpdateProfit(Long salesOrderId);
}
