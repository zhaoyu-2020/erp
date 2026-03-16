package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FreightForwarderQuery;
import com.mc.erp.entity.FreightForwarder;
import com.mc.erp.vo.FreightForwarderVO;

public interface FreightForwarderService extends IService<FreightForwarder> {
    PageResult<FreightForwarderVO> getPage(FreightForwarderQuery query);
}
