package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SalesOrderDetailQuery;
import com.mc.erp.entity.SalesOrderDetail;
import com.mc.erp.vo.SalesOrderDetailVO;

import java.util.List;

public interface SalesOrderDetailService extends IService<SalesOrderDetail> {
    PageResult<SalesOrderDetailVO> getPage(SalesOrderDetailQuery query);
    List<SalesOrderDetailVO> listByOrderId(Long orderId);
    boolean saveDetail(SalesOrderDetail detail);
    boolean updateDetail(SalesOrderDetail detail);
}
