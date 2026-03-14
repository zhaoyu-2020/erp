package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.entity.FreightOrderLog;
import com.mc.erp.mapper.FreightOrderLogMapper;
import com.mc.erp.service.FreightOrderLogService;
import com.mc.erp.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FreightOrderLogServiceImpl extends ServiceImpl<FreightOrderLogMapper, FreightOrderLog>
        implements FreightOrderLogService {

    @Override
    public List<FreightOrderLog> listByOrderId(Long orderId) {
        return this.list(new LambdaQueryWrapper<FreightOrderLog>()
                .eq(FreightOrderLog::getOrderId, orderId)
                .orderByDesc(FreightOrderLog::getOperateTime));
    }

    @Override
    public void log(Long orderId, String orderCode, String operateType, String beforeContent, String afterContent, String remark) {
        FreightOrderLog log = new FreightOrderLog();
        log.setOrderId(orderId);
        log.setOrderCode(orderCode);
        log.setOperateType(operateType);
        log.setOperator(SecurityUtil.getCurrentUsername());
        log.setOperatorId(SecurityUtil.getCurrentUserId());
        log.setOperateTime(LocalDateTime.now());
        log.setBeforeContent(beforeContent);
        log.setAfterContent(afterContent);
        log.setOperateRemark(remark);
        this.save(log);
    }
}
