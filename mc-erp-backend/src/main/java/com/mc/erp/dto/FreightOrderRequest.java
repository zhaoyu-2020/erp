package com.mc.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FreightOrderRequest {

    private Long orderId;

    @NotBlank(message = "销售订单号不能为空")
    private String saleOrderCode;

    @NotNull(message = "货代ID不能为空")
    private Long forwarderId;

    @NotNull(message = "运输类型不能为空")
    private Integer transportType;

    private String containerType;
    private Integer containerQty;
    private Integer isLcl;
    private String containerNo;
    private BigDecimal bulkWeight;
    private BigDecimal bulkVolume;
    private String shippingSpace;

    private Integer needInsurance;
    private BigDecimal insuredAmount;
    private BigDecimal premium;
    private String insuranceCurrency;
    private String insuranceRemark;

    private String orderCurrency;
    private String departurePort;
    private String destinationPort;
    private LocalDateTime shipDate;
    private LocalDateTime estimatedArrivalDate;
    private String remark;

    /** 海运费 */
    private BigDecimal oceanFreight;
    /** 地面费用 */
    private BigDecimal groundFee;
}
