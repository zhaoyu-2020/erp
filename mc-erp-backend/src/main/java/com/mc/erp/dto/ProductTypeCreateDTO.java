package com.mc.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductTypeCreateDTO {
    @NotBlank(message = "产品类型不能为空")
    private String typeName;
}
