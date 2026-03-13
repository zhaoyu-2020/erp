package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.ImportResult;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.vo.SalesOrderVO;
import org.springframework.web.multipart.MultipartFile;

public interface SalesOrderService extends IService<SalesOrder> {
    PageResult<SalesOrderVO> getPage(SalesOrderQuery query);

    /**
     * 状态流转 —— 校验合法性后更新状态，并触发对应的业务副作用
     */
    void updateStatus(Long id, Integer targetStatus);

    void calculateAndUpdateProfit(Long salesOrderId);

    void calculateAndUpdateLoss(Long salesOrderId);

    /** 通过Excel批量导入销售订单（合同基本信息）*/
    ImportResult importContracts(MultipartFile file) throws Exception;

    /** 通过Excel批量导入销售订单明细 */
    ImportResult importDetails(MultipartFile file) throws Exception;
}
