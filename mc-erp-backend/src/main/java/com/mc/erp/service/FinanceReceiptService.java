package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FinanceReceiptQuery;
import com.mc.erp.entity.FinanceReceipt;
import com.mc.erp.vo.FinanceReceiptVO;

public interface FinanceReceiptService extends IService<FinanceReceipt> {
    PageResult<FinanceReceiptVO> getPage(FinanceReceiptQuery query);
}
