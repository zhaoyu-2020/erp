package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.entity.ForwarderAccount;

import java.util.List;

public interface ForwarderAccountService extends IService<ForwarderAccount> {
    List<ForwarderAccount> listByForwarderId(Long forwarderId);
}
