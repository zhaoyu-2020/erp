package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.CustomsDocQuery;
import com.mc.erp.entity.CustomsDoc;
import com.mc.erp.vo.CustomsDocVO;

public interface CustomsDocService extends IService<CustomsDoc> {
    PageResult<CustomsDocVO> getPage(CustomsDocQuery query);
}
