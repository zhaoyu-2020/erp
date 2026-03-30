package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.OperationLogQuery;
import com.mc.erp.entity.OperationLog;
import com.mc.erp.mapper.OperationLogMapper;
import com.mc.erp.service.OperationLogService;
import com.mc.erp.vo.OperationLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Async
    @Override
    public void asyncSaveLog(OperationLog operationLog) {
        this.save(operationLog);
    }

    @Override
    public PageResult<OperationLogVO> getPage(OperationLogQuery query) {
        Page<OperationLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OperationLog> wrapper = buildWrapper(query);
        wrapper.orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> result = this.page(page, wrapper);
        List<OperationLogVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), voList);
    }

    @Override
    public OperationLogVO getDetail(Long id) {
        OperationLog log = this.getById(id);
        return log == null ? null : toVO(log);
    }

    @Override
    public List<OperationLogVO> listForExport(OperationLogQuery query) {
        LambdaQueryWrapper<OperationLog> wrapper = buildWrapper(query);
        wrapper.orderByDesc(OperationLog::getCreateTime);
        // 导出限制最大1万条
        wrapper.last("LIMIT 10000");
        return this.list(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public int cleanBefore(int days) {
        LocalDateTime before = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperationLog::getCreateTime, before);
        return baseMapper.delete(wrapper);
    }

    // ---- private helpers ----

    private LambdaQueryWrapper<OperationLog> buildWrapper(OperationLogQuery query) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getLogType())) {
            wrapper.eq(OperationLog::getLogType, query.getLogType());
        }
        if (StringUtils.hasText(query.getModule())) {
            wrapper.like(OperationLog::getModule, query.getModule());
        }
        if (StringUtils.hasText(query.getOperationType())) {
            wrapper.eq(OperationLog::getOperationType, query.getOperationType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(OperationLog::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getOperatorName())) {
            wrapper.like(OperationLog::getOperatorName, query.getOperatorName());
        }
        if (StringUtils.hasText(query.getOperatorIp())) {
            wrapper.like(OperationLog::getOperatorIp, query.getOperatorIp());
        }
        if (StringUtils.hasText(query.getStartTime())) {
            wrapper.ge(OperationLog::getCreateTime, LocalDateTime.parse(query.getStartTime(), DTF));
        }
        if (StringUtils.hasText(query.getEndTime())) {
            wrapper.le(OperationLog::getCreateTime, LocalDateTime.parse(query.getEndTime(), DTF));
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(OperationLog::getDescription, query.getKeyword())
                    .or().like(OperationLog::getRequestUrl, query.getKeyword())
            );
        }
        return wrapper;
    }

    private OperationLogVO toVO(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(log, vo);
        vo.setStatusLabel(log.getStatus() != null && log.getStatus() == 1 ? "成功" : "失败");
        return vo;
    }
}
