package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FinancePaymentVO {
    private Long id;
    private String paymentNo;
    /** 采购订单ID */
    private Long purchaseOrderId;
    /** 采购订单号 */
    private String purchaseOrderNo;
    private BigDecimal amount;
    private String currency;
    /** 绑定类型: 1=定金, 2=尾款, 3=运费 */
    private Integer bindType;
    private LocalDate paymentDate;
    private String payingBank;
    /** 收款方账户名称 */
    private String payeeAccountName;
    /** 收款方账号 */
    private String payeeAccountNo;
    /** 收款方开户行 */
    private String payeeBankName;
    /** 收款方行号/SWIFT */
    private String payeeBankNo;
    private String remark;
    /** 状态: 1=待审核, 2=已审核, 3=已驳回 */
    private Integer status;
    private String statusLabel;
    /** 审核人ID */
    private Long auditBy;
    /** 审核人姓名 */
    private String auditByName;
    /** 审核时间 */
    private LocalDateTime auditTime;
    /** 审核备注 */
    private String auditRemark;
    /** 创建人ID */
    private Long createId;
    /** 创建人姓名 */
    private String creatorName;
    private LocalDateTime createTime;
}
