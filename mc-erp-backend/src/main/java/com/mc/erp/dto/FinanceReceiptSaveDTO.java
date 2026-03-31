package com.mc.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class FinanceReceiptSaveDTO {
    private Long id;

    private String serialNo;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @NotBlank(message = "币种不能为空")
    private String currency;

    @NotNull(message = "收款日期不能为空")
    private LocalDate receiptDate;

    private String receivingBank;

    /** 收款汇率 */
    private BigDecimal exchangeRate;

    @Valid
    private List<FinanceReceiptDetailDTO> details;
}
