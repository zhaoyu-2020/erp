package com.mc.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.erp.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
