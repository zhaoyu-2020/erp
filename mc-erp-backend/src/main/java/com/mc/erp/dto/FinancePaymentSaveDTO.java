package com.mc.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FinancePaymentSaveDTO {
    private Long id;

    private String paymentNo;

    /** 采购订单ID */
    private Long purchaseOrderId;

    /** 采购订单号 */
    private String purchaseOrderNo;

    @NotNull(message = "付款金额不能为空")
    private BigDecimal amount;

    /** 币种(默认CNY) */
    private String currency;

    /** 绑定类型: 1=定金, 2=尾款, 3=运费 */
    private Integer bindType;

    @NotNull(message = "付款日期不能为空")
    private LocalDate paymentDate;

    /** 付款银行 */
    private String payingBank;

    /** 收款方账户名称 */
    private String payeeAccountName;

    /** 收款方账号 */
    private String payeeAccountNo;

    /** 收款方开户行 */
    private String payeeBankName;

    /** 收款方行号/SWIFT */
    private String payeeBankNo;

    /** 备注 */
    private String remark;
}
