package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.entity.FreightOrderLog;

import java.util.List;

public interface FreightOrderLogService extends IService<FreightOrderLog> {

    /**
     * 获取某订单的所有操作日志
     */
    List<FreightOrderLog> listByOrderId(Long orderId);

    /**
     * 记录操作日志
     */
    void log(Long orderId, String orderCode, String operateType, String beforeContent, String afterContent, String remark);
}
