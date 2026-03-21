package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 资金收付预期半月图视图对象
 */
@Data
public class HalfMonthCashFlowVO {
    /** 时间段标签, 如 "3月15日" */
    private String periodLabel;
    /** 时间段起始日期 */
    private LocalDate periodStart;
    /** 时间段截止日期 */
    private LocalDate periodEnd;
    /** 预计收款总额 (销售合同额-已收定金) */
    private BigDecimal expectedCollection;
    /** 预计付款总额 (采购合同额-已付定金) */
    private BigDecimal expectedPayment;
    /** 涉及的收款销售订单号列表 */
    private List<String> collectionOrderNos;
    /** 涉及的付款采购订单号列表 */
    private List<String> paymentOrderNos;
}
