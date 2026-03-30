package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.ImportResult;
import com.mc.erp.dto.PurchaseOrderQuery;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.vo.PurchaseOrderVO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface PurchaseOrderService extends IService<PurchaseOrder> {
    PageResult<PurchaseOrderVO> getPage(PurchaseOrderQuery query);

    /**
     * 状态流转 —— 校验合法性后更新状态
     * @param totalFreight 流转到「已完成」时，若当前运费为空，由前端传入运费一并保存
     */
    void updateStatus(Long id, Integer targetStatus, BigDecimal totalFreight);

    /** 通过Excel批量导入采购订单（合同基本信息）*/
    ImportResult importContracts(MultipartFile file) throws Exception;

    /** 通过Excel批量导入采购订单明细 */
    ImportResult importDetails(MultipartFile file) throws Exception;
}
