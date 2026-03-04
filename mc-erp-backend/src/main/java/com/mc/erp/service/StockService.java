package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.StockQuery;
import com.mc.erp.entity.Stock;
import com.mc.erp.vo.StockVO;

public interface StockService extends IService<Stock> {
    PageResult<StockVO> getPage(StockQuery query);
}
