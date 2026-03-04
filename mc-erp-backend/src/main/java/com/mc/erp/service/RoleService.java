package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.RoleQuery;
import com.mc.erp.entity.Role;
import com.mc.erp.vo.RoleVO;

public interface RoleService extends IService<Role> {
    PageResult<RoleVO> getPage(RoleQuery query);
}
