package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.RoleQuery;
import com.mc.erp.entity.Role;
import com.mc.erp.mapper.RoleMapper;
import com.mc.erp.service.RoleService;
import com.mc.erp.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public PageResult<RoleVO> getPage(RoleQuery query) {
        Page<Role> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getRoleCode()), Role::getRoleCode, query.getRoleCode());
        wrapper.like(StringUtils.hasText(query.getRoleName()), Role::getRoleName, query.getRoleName());
        wrapper.orderByDesc(Role::getCreateTime);

        Page<Role> resultPage = this.page(page, wrapper);

        var voList = resultPage.getRecords().stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }
}
