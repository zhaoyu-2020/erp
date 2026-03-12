package com.mc.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinanceReceiptDetailDTO {
    private Long id;

    @NotNull(message = "销售订单ID不能为空")
    private Long salesOrderId;

    @NotBlank(message = "销售订单号不能为空")
    private String salesOrderNo;

    @NotNull(message = "绑定金额不能为空")
    private BigDecimal boundAmount;

    /** 绑定类型: 1=定金, 2=尾款 */
    private Integer bindType;
}
