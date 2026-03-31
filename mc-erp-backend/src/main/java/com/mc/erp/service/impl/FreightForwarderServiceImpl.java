package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FreightForwarderQuery;
import com.mc.erp.entity.FreightForwarder;
import com.mc.erp.entity.FreightOrder;
import com.mc.erp.mapper.FreightForwarderMapper;
import com.mc.erp.service.FreightForwarderService;
import com.mc.erp.service.FreightOrderService;
import com.mc.erp.vo.FreightForwarderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@Service
public class FreightForwarderServiceImpl extends ServiceImpl<FreightForwarderMapper, FreightForwarder>
        implements FreightForwarderService {

    @Autowired
    private FreightOrderService freightOrderService;

    @Override
    public PageResult<FreightForwarderVO> getPage(FreightForwarderQuery query) {
        Page<FreightForwarder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<FreightForwarder> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getForwarderCode()), FreightForwarder::getForwarderCode, query.getForwarderCode());
        wrapper.like(StringUtils.hasText(query.getName()), FreightForwarder::getName, query.getName());
        wrapper.like(StringUtils.hasText(query.getFreightType()), FreightForwarder::getFreightType, query.getFreightType());
        wrapper.like(StringUtils.hasText(query.getMarketAdvantage()), FreightForwarder::getMarketAdvantage, query.getMarketAdvantage());
        wrapper.orderByDesc(FreightForwarder::getCreateTime);

        Page<FreightForwarder> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(forwarder -> {
            FreightForwarderVO vo = new FreightForwarderVO();
            BeanUtils.copyProperties(forwarder, vo);
            return vo;
        }).toList();

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public boolean removeById(Serializable id) {
        long refCount = freightOrderService.count(new LambdaQueryWrapper<FreightOrder>()
                .eq(FreightOrder::getForwarderId, id));
        if (refCount > 0) {
            throw new RuntimeException("该货代下还有 " + refCount + " 个海运订单，无法删除");
        }
        return super.removeById(id);
    }
}
