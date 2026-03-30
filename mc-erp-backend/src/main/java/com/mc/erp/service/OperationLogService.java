package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.OperationLogQuery;
import com.mc.erp.entity.OperationLog;
import com.mc.erp.vo.OperationLogVO;

import java.util.List;

public interface OperationLogService extends IService<OperationLog> {

    /**
     * 异步保存操作日志
     */
    void asyncSaveLog(OperationLog log);

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLogVO> getPage(OperationLogQuery query);

    /**
     * 查询日志详情
     */
    OperationLogVO getDetail(Long id);

    /**
     * 按条件导出日志列表
     */
    List<OperationLogVO> listForExport(OperationLogQuery query);

    /**
     * 清理指定天数之前的日志
     */
    int cleanBefore(int days);
}
