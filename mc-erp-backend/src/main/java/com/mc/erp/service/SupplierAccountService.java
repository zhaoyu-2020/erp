package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.entity.SupplierAccount;

import java.util.List;

public interface SupplierAccountService extends IService<SupplierAccount> {
    List<SupplierAccount> listBySupplierId(Long supplierId);
}
