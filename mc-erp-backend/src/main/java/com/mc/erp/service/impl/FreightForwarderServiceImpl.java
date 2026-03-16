package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FreightForwarderQuery;
import com.mc.erp.entity.FreightForwarder;
import com.mc.erp.mapper.FreightForwarderMapper;
import com.mc.erp.service.FreightForwarderService;
import com.mc.erp.vo.FreightForwarderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FreightForwarderServiceImpl extends ServiceImpl<FreightForwarderMapper, FreightForwarder>
        implements FreightForwarderService {

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
}
