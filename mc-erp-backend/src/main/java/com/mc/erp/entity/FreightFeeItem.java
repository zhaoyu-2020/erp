package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("erp_freight_fee_item")
public class FreightFeeItem {
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "费用类型不能为空")
    private Integer feeType;

    @NotBlank(message = "费用名称不能为空")
    private String feeName;

    @NotNull(message = "费用金额不能为空")
    private BigDecimal feeAmount;

    private String currency;
    private String billingMethod;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
