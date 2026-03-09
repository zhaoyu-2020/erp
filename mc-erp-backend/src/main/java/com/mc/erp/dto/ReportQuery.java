package com.mc.erp.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReportQuery {
    private LocalDate startDate;
    private LocalDate endDate;
    private String period; // day, month, quarter, year
    private Long salespersonId;
    private Long customerId;
    private Long supplierId;
    private String orderBy;
}
