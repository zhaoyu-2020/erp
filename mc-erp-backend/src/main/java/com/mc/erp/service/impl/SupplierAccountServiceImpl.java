package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.entity.SupplierAccount;
import com.mc.erp.mapper.SupplierAccountMapper;
import com.mc.erp.service.SupplierAccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierAccountServiceImpl extends ServiceImpl<SupplierAccountMapper, SupplierAccount>
        implements SupplierAccountService {

    @Override
    public List<SupplierAccount> listBySupplierId(Long supplierId) {
        return this.list(new LambdaQueryWrapper<SupplierAccount>()
                .eq(SupplierAccount::getSupplierId, supplierId)
                .orderByAsc(SupplierAccount::getId));
    }
}
