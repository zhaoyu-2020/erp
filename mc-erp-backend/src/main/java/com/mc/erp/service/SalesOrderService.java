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

    /**
     * 根据所有收款明细，重新汇总并回写销售订单的已认领定金/尾款金额及状态。
     * 同时在新建订单时，根据合同金额和定金比例预填充各金额字段。
     * @param orderNo 销售订单号
     */
    void syncClaimAmounts(String orderNo);

    /** 通过Excel批量导入销售订单（合同基本信息）*/
    ImportResult importContracts(MultipartFile file) throws Exception;

    /** 通过Excel批量导入销售订单明细 */
    ImportResult importDetails(MultipartFile file) throws Exception;
}
