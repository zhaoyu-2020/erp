package com.mc.erp.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockVO {
    private Long id;
    private Long productId;
    // For convenience in frontend display
    private String productCode;
    private String productName;
    private Integer currentQty;
    private Integer availableQty;
    private Integer lockedQty;
}
