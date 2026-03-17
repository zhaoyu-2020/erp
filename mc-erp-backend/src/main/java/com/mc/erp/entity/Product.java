package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "公差不能为空")
    private String tolerance;

    private String meterWeight;

    private String hsCode;

    private BigDecimal taxRefundRate;

    // private String nameCn;
    // private String nameEn;

    private String unit;

    private String declaration;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableLogic
    private Integer isDeleted;
}
