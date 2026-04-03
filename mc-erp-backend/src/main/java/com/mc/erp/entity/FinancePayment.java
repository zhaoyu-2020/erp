package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_finance_payment")
public class FinancePayment {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 付款单号 */
    private String paymentNo;

    /** 采购订单ID */
    private Long purchaseOrderId;

    /** 采购订单号 */
    private String purchaseOrderNo;

    @NotNull(message = "付款金额不能为空")
    private BigDecimal amount;

    /** 币种(默认人民币) */
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

    /** 状态: 1=待审核, 2=已审核, 3=已驳回 */
    private Integer status;

    /** 审核人ID */
    private Long auditBy;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核备注 */
    private String auditRemark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableLogic
    private Integer isDeleted;
}
