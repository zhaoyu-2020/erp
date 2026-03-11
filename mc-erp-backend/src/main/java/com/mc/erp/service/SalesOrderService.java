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

    /**
     * 计算并更新销售订单的损耗
     * 公式：损耗 = 定金收款金额 + 尾款金额 - 销售订单明细中所有价格汇总之和
     * @param salesOrderId 销售订单ID
     */
    void calculateAndUpdateLoss(Long salesOrderId);
}
