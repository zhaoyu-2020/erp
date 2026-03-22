package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.entity.ForwarderAccount;
import com.mc.erp.mapper.ForwarderAccountMapper;
import com.mc.erp.service.ForwarderAccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForwarderAccountServiceImpl extends ServiceImpl<ForwarderAccountMapper, ForwarderAccount>
        implements ForwarderAccountService {

    @Override
    public List<ForwarderAccount> listByForwarderId(Long forwarderId) {
        return this.list(new LambdaQueryWrapper<ForwarderAccount>()
                .eq(ForwarderAccount::getForwarderId, forwarderId)
                .orderByAsc(ForwarderAccount::getId));
    }
}
