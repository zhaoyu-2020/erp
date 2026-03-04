package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.ProductQuery;
import com.mc.erp.entity.Product;
import com.mc.erp.vo.ProductVO;

public interface ProductService extends IService<Product> {
    PageResult<ProductVO> getPage(ProductQuery query);
}
