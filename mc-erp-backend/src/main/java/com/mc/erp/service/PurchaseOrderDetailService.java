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
}
