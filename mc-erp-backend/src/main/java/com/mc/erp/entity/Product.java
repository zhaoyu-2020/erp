package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
  
    @NotBlank(message = "商品类型不能为空")
    private String type;

    @NotBlank(message = "规格不能为空")
    private String spec;

    @NotBlank(message = "材质不能为空")
    private String material;

    @NotBlank(message = "长度不能为空")
    private String length;

    @NotBlank(message = "米重不能为空")
    private String meterWeight;

    @NotBlank(message = "公差不能为空")
    private String tolerance;

    private String hsCode;

    @NotNull(message = "退税率不能为空")
    private BigDecimal taxRefundRate;

    @NotBlank(message = "中文名称不能为空")
    private String nameCn;
    private String nameEn;

    @NotBlank(message = "单位不能为空")
    private String unit;

    @NotBlank(message = "申报要素不能为空")
    private String declaration;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
