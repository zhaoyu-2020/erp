package com.mc.erp.vo;

import com.mc.erp.entity.FreightFeeItem;
import com.mc.erp.entity.FreightOrderLog;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FreightOrderVO {
    private Long orderId;
    private String orderCode;
    private String saleOrderCode;
    private Long supplierId;
    private String supplierName;
    private Integer transportType;
    private String transportTypeLabel;
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
    private Integer orderStatus;
    private String orderStatusLabel;
    private BigDecimal totalOceanFreight;
    private BigDecimal totalGroundFee;
    private BigDecimal totalAmount;
    private String orderCurrency;
    private String departurePort;
    private String destinationPort;
    private LocalDateTime shipDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createUser;
    private String remark;
    private String cancelReason;

    /** 费用明细列表 */
    private List<FreightFeeItem> feeItems;
    /** 操作日志列表 */
    private List<FreightOrderLog> logs;
}
