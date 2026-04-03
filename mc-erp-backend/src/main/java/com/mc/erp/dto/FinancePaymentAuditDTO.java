package com.mc.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinancePaymentAuditDTO {
    @NotNull(message = "付款单ID不能为空")
    private Long id;

    /** true=审核通过, false=驳回 */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    /** 审核备注 */
    private String auditRemark;
}
