package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SupplierQuery;
import com.mc.erp.entity.Supplier;
import com.mc.erp.vo.SupplierVO;

public interface SupplierService extends IService<Supplier> {
    PageResult<SupplierVO> getPage(SupplierQuery query);
}
