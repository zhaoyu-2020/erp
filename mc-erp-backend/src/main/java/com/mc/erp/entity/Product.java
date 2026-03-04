package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String spuCode;
    private String hsCode;
    private String nameCn;
    private String nameEn;
    private BigDecimal taxRefundRate;
    private String unit;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
