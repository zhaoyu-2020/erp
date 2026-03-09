package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.StockQuery;
import com.mc.erp.entity.Stock;
import com.mc.erp.mapper.StockMapper;
import com.mc.erp.service.StockService;
import com.mc.erp.vo.StockVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    @Override
    public PageResult<StockVO> getPage(StockQuery query) {
        Page<Stock> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(query.getProductId() != null, Stock::getProductId, query.getProductId());
        wrapper.orderByDesc(Stock::getCreateTime);

        Page<Stock> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(stock -> {
            StockVO vo = new StockVO();
            BeanUtils.copyProperties(stock, vo);
            // In a real application, you would typically fetch the product details here
            // using the ProductService to populate productCode and productName securely.
            vo.setProductName("Product " + stock.getProductId());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
