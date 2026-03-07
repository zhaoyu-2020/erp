package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_stock")
public class Stock {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "商品ID不能为空")
    private Long productId;
    private Integer currentQty;
    private Integer availableQty;
    private Integer lockedQty;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
