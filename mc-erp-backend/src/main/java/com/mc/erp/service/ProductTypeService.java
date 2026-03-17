package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.dto.ProductTypeCreateDTO;
import com.mc.erp.dto.ProductTypeUpdateDTO;
import com.mc.erp.entity.ProductType;

import java.util.List;

public interface ProductTypeService extends IService<ProductType> {
    List<ProductType> listAll();

    boolean createType(ProductTypeCreateDTO dto);

    boolean updateType(ProductTypeUpdateDTO dto);

    boolean deleteType(Long id);
}
