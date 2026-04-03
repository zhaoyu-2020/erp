package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FinancePaymentAuditDTO;
import com.mc.erp.dto.FinancePaymentQuery;
import com.mc.erp.dto.FinancePaymentSaveDTO;
import com.mc.erp.entity.FinancePayment;
import com.mc.erp.vo.FinancePaymentVO;

public interface FinancePaymentService extends IService<FinancePayment> {
    PageResult<FinancePaymentVO> getPage(FinancePaymentQuery query);

    FinancePaymentVO getDetail(Long id);

    void savePayment(FinancePaymentSaveDTO dto);

    void updatePayment(FinancePaymentSaveDTO dto);

    void deletePayment(Long id);

    /** 管理员审核付款单（通过/驳回），审核通过后同步采购订单付款字段 */
    void audit(FinancePaymentAuditDTO dto);
}
