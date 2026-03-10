package com.mc.erp.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupplierVO {
    private Long id;
    private String supplierCode;
    private String name;
    private String contactPerson;
    private String phone;
    private String address;
    /**
     * 产品类型(多个用英文逗号分隔)
     */
    private String productType;
    private LocalDateTime createTime;
}
